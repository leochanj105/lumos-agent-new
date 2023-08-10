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
import java.util.List;
import java.util.Map;

import com.agent.compile.CompileUtils;

import java.net.URLClassLoader;

import soot.Body;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.options.Options;

/**
 * Hello world!
 *
 */
public class LumosAgent {
    // public static ClassLoader loader;

    // public Agent(ClassLoader loader){
    // this.loader = loader;
    // }
    public static ClassLoader cloader = null;
    public static Map<String, SootMethod> methodMap = new HashMap<>();
    public static Map<String, SootClass> classMap = new HashMap<>();
    public static Map<String, Body> bodyMap = new HashMap<>();

    public static byte[] forTest;
    public static String testclass;
    public static String cpath = "/app/classes";
    public static String jarpath = "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\lumos-experiment\\ts-launcher\\opentelemetry-javaagent.jar";
    // public static String jarpath =
    // "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\lumos-experiment\\ts-launcher\\opentelemetry-javaagent\\io\\opentelemetry\\javaagent\\shaded";
    // public static String jarpath =
    // "C:\\Users\\jchen\\.m2\\repository\\io\\opentelemetry\\opentelemetry-api-trace\\0.13.1\\opentelemetry-api-trace-0.13.1.jar";
    // public static String cpath =
    // "/mnt/c/Users/jchen/Desktop/Academic/lumos/lumos-experiment/ts-launcher/target/classes";
    // public static String cpath = "/app/classes";
    // public clas
    // public static String cpath =
    // "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\test";

    public static void premain(String agentArgs, Instrumentation inst) {

        // ClassPool classPool = ClassPool.getDefault();
        // classPool.appendClassPath(new
        // LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        // System.out.println("[XXXX] " + classPool);
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(
                    ClassLoader loader,
                    String className,
                    Class<?> classBeingRedefined, // null if class was not previously loaded
                    ProtectionDomain protectionDomain,
                    byte[] classFileBuffer) {
                // return transformed class file.

                if (LumosAgent.cloader == null && loader != null
                        && loader.getClass().getName().contains("LaunchedURLClassLoader")) {
                    System.out.println("Hooked " + loader);
                    LumosAgent.cloader = loader;
                }
                // p(className);
                // if(className.contains("ServiceImpl")){
                // System.out.println("[VVVV] " + loader + ": " + new LoaderClassPath(loader));
                // ClassPool pool = new ClassPool(true);
                // ClassPool.doPruning = true;
                // pool.appendClassPath(new LoaderClassPath(loader));
                // System.out.println("[VVV] " + pool);
                // try{
                // System.out.println("[VV] " + pool.get("travel.service.TravelServiceImpl"));
                // }
                // catch(Exception e){
                // e.printStackTrace();
                // }
                // }
                return classFileBuffer;
            }
        });
        play();
        Thread thread = new Thread(new AgentThread(inst));
        thread.start();
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        Thread thread = new Thread(new AgentThread(inst));
        thread.start();
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
        Options.v().setPhaseOption("jb.ls", "enabled:false");
        // Need this to avoid the need to provide an entry point
        Options.v().setPhaseOption("cg", "all-reachable:true");

        // Need this to include all subtypes
        // Options.v().setPhaseOption("cg", "library:any-subtype");

        Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.String", SootClass.SIGNATURES);
        // Scene.v().addBasicClass("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span",
        Scene.v().addBasicClass("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span",
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
        p("----------");

    }

    public static Body findBody(String name) {
        for (String s : bodyMap.keySet()) {
            if (s.contains(name)) {
                return ((Body) bodyMap.get(s).clone());
            }
        }
        return null;
    }

    public static SootMethod finMethod(String name) {
        for (String s : methodMap.keySet()) {
            if (s.contains(name)) {
                return methodMap.get(s);
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

        // Options.v().set_soot_classpath((jarpath + ":" +
        // Options.v().soot_classpath()));
        // SootClass sc =
        // Scene.v().loadClassAndSupport("io.opentelemetry.api.trace.Span");
        // analyzePath(jarpath);
        // );
        // SootMethod mm = sc.getMethodByName("current");
        // p(Scene.v().getSootClassPath());
        // Jimple.v().newStaticInvokeExpr(null, null)
        // p(sc.getMethods().toString());
        // p(sc.getPackageName().toString());
        for (SootClass scc : Scene.v().getClasses()) {
            if (scc.toString().contains("opentelemetry")) {
                p(scc.toString());
                p(scc.getMethods().toString());
            }
        }
        // if (true)
        // return;
        analyzePath(cpath);
        String methodName = "sendInsidePayment";
        // String methodName = "foo";
        String valueName = "$stack29";
        // String valueName = "l1";
        String stmtString = "$stack29 = virtualinvoke $stack28.<java.lang.Boolean: boolean booleanValue()>()";
        // String stmtString = "l1 = l0 + 1";

        Body b = findBody(methodName);
        SootMethod sm = finMethod(methodName);
        SootClass sclass = findClass(sm.getDeclaringClass().getName());
        testclass = sclass.toString();

        Value local = CompileUtils.findLocal(b, valueName);
        Stmt stmt = CompileUtils.findStmt(b, stmtString);
        List<Stmt> inserts = CompileUtils.generateTPStmts(b, local,
                Collections.emptyList(), false);
        p(inserts.toString());
        CompileUtils.insertAt(b, stmt, inserts, false);
        sm.setActiveBody(b);

        byte[] bytecode = CompileUtils.compileClass(sclass);
        forTest = bytecode;

        // String dirname = "AAA";
        // File outputDir = new File(dirname);
        // if (!outputDir.exists()) {
        // outputDir.mkdir();
        // }
        // File file2 = new File(dirname + "/" + sclass.getName() + ".class");
        // FileOutputStream classout;
        // try {
        // classout = new FileOutputStream(file2);
        // classout.write(bytecode);
        // classout.close();
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }

    public static void main(String args[]) {
        play();
        // p(bytecode.toString());
        // p(sclass.toString());

    }
}
