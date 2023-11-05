package com.agent;

import java.lang.instrument.Instrumentation;
import javassist.ClassPool;
import javassist.LoaderClassPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ClassLoader;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.agent.compile.CompileUtils;

import java.net.URLClassLoader;

import soot.Body;
import soot.jimple.JimpleBody;
import soot.G;
import soot.PatchingChain;
import soot.RefType;
import soot.SootField;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.options.Options;
import tracing.TracePoint;
import tracing.*;

/**
 * Hello world!
 *
 */
public class LumosAgent {
    // public static ClassLoader loader;

    // public Agent(ClassLoader loader){
    // this.loader = loader;
    // }

    public static boolean playGroundFlag = false;
    public static ClassLoader cloader = null;
    public static boolean analyzeReady = false;
    public static Map<String, SootMethod> methodMap = new HashMap<>();
    public static Map<String, SootClass> classMap = new HashMap<>();
    public static Map<String, Body> bodyMap = new HashMap<>();

    public static Set<LumosInstrumentation> allTPs = new HashSet<>();
    public static HashMap<String, Set<LumosInstrumentation>> methodTPMap = new HashMap<>();

    // public static Set<DBInstrumentationPoint> allTPs = new HashSet<>();
    // public static HashMap<String, Set<TracePoint>> methodTPMap = new HashMap<>();
    
    public static byte[] forTest;
    public static String testclass;
    // public static String jarpath = "/app/opentelemetry-api-trace-0.13.1.jar";
    public static String jarpath = "/app/opentelemetry-javaagent.jar";
    public static String cpath = "/app/classes";
    // public static String jarpath = "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\lumos-experiment\\ts-launcher\\opentelemetry-javaagent.jar";

    // public static String cpath = "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\lumos-experiment\\ts-launcher\\target\\classes";
    public static boolean ORMContextOn = false;
    public static void premain(String agentArgs, Instrumentation inst) {

        // ClassPool classPool = ClassPool.getDefault();
        // classPool.appendClassPath(new
        // LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        // System.out.println("[XXXX] " + classPool);
        setupSoot(cpath);
        analyzePath(cpath);
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(
                    ClassLoader loader,
                    String className,
                    Class<?> classBeingRedefined, // null if class was not previously loaded
                    ProtectionDomain protectionDomain,
                    byte[] classFileBuffer) {

                if (loader != null
                    && loader.getClass().getName().contains("LaunchedURLClassLoader")
                ) {
                    if(LumosAgent.cloader == null){
                        System.out.println("Hooked " + loader);
                        LumosAgent.cloader = loader;
                    }
                    if(!ORMContextOn){
			return classFileBuffer;
		    }
                    String targetName = "order.domain.Order";
                    String actualName = targetName.replace('.', File.separatorChar);
                    // if(className.contains("domain/Order")){
                    //     System.out.println(className + ", " + loader.getClass());
                    //     System.out.println(actualName);
                    // }
                    
                    if(className.equals(actualName)){
                        System.out.println("adding to " + className);
                        SootClass sclass = null;
                        while(sclass == null){
                            sclass = findClassExact(targetName);
                        }

                        // byte[] cbuffer = addFieldToClass(sclass, "java.lang.String", "LumosContext");
                        addFieldToClass(sclass, "java.util.HashMap", "LumosContext");
                        /*
                        for(SootMethod method: sclass.getMethods()){
                            if(method.getName().contains("<init>")){
                                Body b = findBodyNoClone(method.toString());
                                System.out.println(method);
                                if(b !=null){
                                    List<Stmt> initStmts = CompileUtils.generateInit(b, "LumosContext");
                                    CompileUtils.insertAt(b.getUnits(), ((JimpleBody) b).getFirstNonIdentityStmt(), initStmts, true);

                                    initStmts.forEach(stmt->{System.out.println(stmt);});
                                    
                                    System.out.println("Inserted for " + method);
                                    method.setActiveBody(b);
                                }
                            }
                        }
			*/
                        byte[] cbuffer = CompileUtils.compileClass(sclass);
                        
                        System.out.println("added to " + className);
                        playGroundFlag = true;
                        return cbuffer;
                    }
                }

                return classFileBuffer;
            }
        });
        // play();
        
        Thread thread = new Thread(new AgentThread(inst));
        thread.start();
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        Thread thread = new Thread(new AgentThread(inst));
        thread.start();
    }

    public static byte[] addFieldToClass(SootClass sclass, String type, String fieldname){
        sclass.addField(Scene.v().makeSootField(fieldname, RefType.v(type), soot.Modifier.PUBLIC));
        byte[] bytecode = CompileUtils.compileClass(sclass);
        return bytecode;
    }

    public static Map<String, byte[]> addField(String classname, String type, String fieldname){
        Map<String, byte[]> cmap = new HashMap<>();
        SootClass sclass = findClass(classname);
        cmap.put(sclass.toString(), addFieldToClass(sclass, type, fieldname));
        return cmap;
    }

    public static boolean addTP(LumosInstrumentation tp) {
        if(allTPs.contains(tp)){
            return false;
        }
        allTPs.add(tp);
        if (!methodTPMap.containsKey(tp.getSm())) {
            methodTPMap.put(tp.getSm(), new HashSet<>());
        }
        
        methodTPMap.get(tp.getSm()).add(tp);
        return true;
    }

    public static boolean removeTP(LumosInstrumentation tp) {
        if(!allTPs.contains(tp)){
            return false;
        }
        allTPs.remove(tp);
        if (methodTPMap.containsKey(tp.getSm())) {
            methodTPMap.get(tp.getSm()).remove(tp);
            return true;
        }
        return false;
    }

    public static void setupSoot(String path) {
        G.reset();

        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_whole_program(true);
        Options.v().set_validate(true);

        Options.v().set_allow_phantom_elms(true);

        // This is needed to prevent compile error for unimplemented
        // methods in interfaces !!
        Options.v().set_ignore_resolution_errors(true);

        // Need this to makesure paramter names are kept!!
        // Spring annotations rely on this!!
        Options.v().set_write_local_annotations(true);

        // Options.v().set_soot_classpath(path);
        // p(File.pathSeparator);
        Options.v().set_soot_classpath(jarpath + File.pathSeparator + path);
        Options.v().set_java_version(8);

        // Options.v().class
        // Options.v().set_process_dir(Collections.singletonList(sourceDirectory));
        // List<String> processList = new ArrayList<String>();

        String arr[] = { path };
        Options.v().set_process_dir(Arrays.asList(arr));

        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_print_tags_in_output(true);

        // Use original names
        Options.v().setPhaseOption("jb", "optimize:false");
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("jb", "preserve-source-annotations:true");
        Options.v().setPhaseOption("jb", "stabilize-local-names:true");
        // Need this to avoid the need to provide an entry point
        Options.v().setPhaseOption("cg", "all-reachable:true");

        // Need this to include all subtypes
        // Options.v().setPhaseOption("cg", "library:any-subtype");

        Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.String", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.Map", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.HashMap", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.util.ArrayList", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.Object", SootClass.SIGNATURES);
        // Scene.v().addBasicClass("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span",
        Scene.v().addBasicClass("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span",
                SootClass.SIGNATURES);
        Scene.v().addBasicClass("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.SpanContext",
                SootClass.SIGNATURES);
        // Scene.v().addBasicClass("io.opentelemetry.api.trace.Span",
        // SootClass.SIGNATURES);
        Scene.v().loadNecessaryClasses();
    }

    public static void p(String s) {
        System.out.println(s);
    }

    public static void analyzePath(String path) {
        // Options.v().set
        p("Analyzing " + path);
        setupSoot(path);
        for (SootClass cls : Scene.v().getApplicationClasses()) {
            if (cls.toString().contains("conf.HttpAspect")) {
                continue;
            }
            for (SootMethod sm : cls.getMethods()) {
                if (sm.isAbstract()) {
                    continue;
                }

                sm.retrieveActiveBody();
                methodMap.put(sm.getSignature(), sm);
                bodyMap.put(sm.getSignature(), ((Body) sm.getActiveBody().clone()));
            }
            classMap.put(cls.toString(), cls);
            // CompileUtils.outputJimple(cls, "AAA");
        }
        // p("----------");
        analyzeReady = true;
    }

    public static Body findBody(String name) {
        for (String s : bodyMap.keySet()) {
            if (s.contains(name)) {
                return ((Body) bodyMap.get(s).clone());
            }
        }
        return null;
    }

    public static Body findBodyNoClone(String name) {
        for (String s : bodyMap.keySet()) {
            if (s.contains(name)) {
                return bodyMap.get(s);
            }
        }
        return null;
    }

    public static SootMethod findMethod(String name) {
        for (String s : methodMap.keySet()) {
            if (s.contains(name)) {
                return methodMap.get(s);
            }
        }
        return null;
    }

    public static SootMethod findMethod(String ...names) {
        for (String s : methodMap.keySet()) {
            boolean match = true;
            for(String name: names){
                if (!s.contains(name)) {
                    match = false;
                    break;
                }
            }
            if(match){
                return methodMap.get(s);
            }
        }
        return null;
    }

    public static SootClass findClassExact(String name) {
        for (String s : classMap.keySet()) {
            if (s.equals(name)) {
                return classMap.get(s);
            }
        }
        return null;
    }

    public static SootClass findClass(String name) {
        for (String s : classMap.keySet()) {
            if (s.contains(name)) {
                return classMap.get(s);
            }
        }
        return null;
    }

    public static void play() {
        setupSoot(cpath);
        analyzePath(cpath);

        String methodName = "sendInsidePayment";
        String valueName = "$stack29";
        String stmtString = "$stack29 = virtualinvoke $stack28.<java.lang.Boolean: boolean booleanValue()>()";

        TracePoint tp = new TracePoint("11", methodName, stmtString,103, valueName);
        addTP(tp);
        instrument();
    }

    public static Map<String, byte[]> instrument() {
        Map<String, byte[]> cmap = new HashMap<>();
        Set<SootClass> scToCompile = new HashSet<>();
        for (String smstr : methodTPMap.keySet()) {
            SootMethod sm = findMethod(smstr);
            Body b = findBody(sm.toString());
            SootClass sclass = findClass(sm.getDeclaringClass().getName());
            List<Stmt> targetstmts = new ArrayList<>();
            List<LumosInstrumentation> targetInsts = new ArrayList<>();
            PatchingChain<Unit> units = b.getUnits();

            // This two-step way is needed to avoid inserted stmts
            // from breaking the labeling
            for (LumosInstrumentation inst : methodTPMap.get(smstr)) {
                inst.setBody(b);
                Stmt stmt = inst.getActualStmt();
                targetstmts.add(stmt);
                targetInsts.add(inst);
            }

            for (int i = 0; i < targetstmts.size(); i++) {
                Stmt stmt = targetstmts.get(i);
                LumosInstrumentation inst = targetInsts.get(i);
                List<Stmt> inserts = inst.addInsts();
                CompileUtils.insertAt(units, stmt, inserts, inst.isBefore());
            }
            sm.setActiveBody(b);
            scToCompile.add(sclass);
        }


        for (SootClass sclass : scToCompile) {
            byte[] bytecode = CompileUtils.compileClass(sclass);
            forTest = bytecode;
            // if(sclass.)
            cmap.put(sclass.toString(), bytecode);

            // String dirname = "AAA";
            // File outputDir = new File(dirname);
            // if (!outputDir.exists()) {
            //     outputDir.mkdir();
            // }
            // File file2 = new File(dirname + "/" + sclass.getName() + ".class");
            // FileOutputStream classout;
            // try {
            //     classout = new FileOutputStream(file2);
            //     classout.write(bytecode);
            //     classout.close();
            // } catch (FileNotFoundException e) {
            //     e.printStackTrace();
            // } catch (IOException e) {
            //     // TODO Auto-generated catch block
            //     e.printStackTrace();
            // }
        }
        return cmap;
    }

    public static void main(String args[]) {
        play();
        // p(bytecode.toString());
        // p(sclass.toString());

    }

    
}
