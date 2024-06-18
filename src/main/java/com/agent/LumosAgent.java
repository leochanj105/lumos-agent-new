package com.agent;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

import com.agent.compile.CompileUtils;
import com.agent.inst.LInst;

import soot.Body;
import soot.G;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.Stmt;
import soot.options.Options;
import tracing.LumosInstrumentation;
import tracing.TimestampedInstrumentation;

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
    public static Map<String, SootMethod> methodMap = new ConcurrentHashMap<>();
    public static Map<String, SootClass> classMap = new ConcurrentHashMap<>();
    public static Map<String, Body> bodyMap = new ConcurrentHashMap<>();

    public static Set<LumosInstrumentation> allTPs = new HashSet<>();
    public static Set<LInst> allInsts = new HashSet<>();
    public static Map<String, Set<LumosInstrumentation>> methodTPMap = new HashMap<>();
    public static Map<String, Set<LInst>> activeInsts = new HashMap<>();
    // public static Set<DBInstrumentationPoint> allTPs = new HashSet<>();
    // public static HashMap<String, Set<TracePoint>> methodTPMap = new HashMap<>();
    public static String logger = "stdout";
    // public static String rrClass = "com.mycompany.app.App";
    
    public static Set<SootMethod> entryMethods = new HashSet<>();
    public static Set<String> entryClasses = new HashSet<>();
    public static String rrClass = "com.lumos.trace.LumosTracer";
    public static String tracerJar = "/tmp/LumosTracer.jar";
    
    public static byte[] forTest;
    public static String testclass;
    // public static String jarpath = "/app/opentelemetry-api-trace-0.13.1.jar";
    public static String jarpath = "/app/opentelemetry-javaagent.jar";
    // public static String cpath = "/app/classes";
    public static String cpath = "";
    public static List<String> includeList;
    public static List<String> processList;
    public static Set<String> skippedClasses = new HashSet<>(Arrays.asList(new String[]{
        "java.util.stream.StreamSpliterators$SliceSpliterator$OfDouble",
        "java.util.stream.Tripwire",
        "java.util.stream.StreamSpliterators$IntWrappingSpliterator",
        "java.util.Collections$UnmodifiableMap$UnmodifiableEntrySet",
        "java.util.stream.Collectors",
        "java.util.TreeMap$EntrySpliterator",
        "java.util.Collections$CopiesList",
        "java.util.stream.StreamSpliterators$LongWrappingSpliterator",
        "java.util.stream.StreamSpliterators$WrappingSpliterator",
        "java.util.stream.SliceOps",
        "java.util.stream.DistinctOps$1",
        "java.util.stream.StreamSpliterators$SliceSpliterator$OfRef",
        "java.util.Tripwire",
        "java.util.stream.StreamSpliterators$DistinctSpliterator",
        "java.util.stream.AbstractPipeline",
        "java.util.stream.StreamSpliterators$DoubleWrappingSpliterator",
    }));
    // public static String jarpa
    // "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\lumos-experiment\\ts-launcher\\opentelemetry-javaagent.jar";

    // public static String cpath =
    // "C:\\Users\\jchen\\Desktop\\Academic\\lumos\\lumos-experiment\\ts-launcher\\target\\classes";a
    public static boolean allOn = false;
    public static boolean ORMContextOn =false;
    public static boolean SOInjectOn =false;
    public static boolean TPInstOn =false;
    public static List<Thread> buildTasks = new ArrayList<>();
    public static void taskSync(){
        for(Thread t: buildTasks){
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        buildTasks.clear();
    }

    public static Thread getTaskThread(Runnable r) {
        Thread t = new Thread(r);
        t.start();
        return t;
    }

    public static void addTask(Runnable r) {
        Thread t = getTaskThread(r);
        buildTasks.add(t);
    }
    public static String getRRField(){
        return "<"+rrClass+": java.lang.ThreadLocal rrOn>";
    }
    public static boolean checkORMClass(String clsname) {
        return false;
        // return (clsname.endsWith("order.domain.Order") || clsname.endsWith("other.domain.Order") ||
        //         clsname.endsWith("sso.domain.LoginValue") ||
        //         clsname.endsWith("com.trainticket.domain.AddMoney")
        //         || clsname.endsWith("inside_payment.domain.AddMoney") ||
        //         clsname.endsWith("com.trainticket.domain.Payment") || clsname.endsWith("inside_payment.domain.Payment")
        //         ||
        //         clsname.endsWith("sso.domain.Account") || clsname.endsWith("inside_payment.domain.DrawBack"));
    }

    public static boolean checkSkipped(SootClass cls){
        return skippedClasses.contains(cls.toString());
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(tracerJar);
        } catch (IOException e) {
            e.printStackTrace();
        }
        inst.appendToBootstrapClassLoaderSearch(jarFile);
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(
                    ClassLoader loader,
                    String className,
                    Class<?> classBeingRedefined, // null if class was not previously loaded
                    ProtectionDomain protectionDomain,
                    byte[] classFileBuffer) {

                if (loader != null
                        // && loader.getClass().getName().contains("LaunchedURLClassLoader")) {
                        && loader.getClass().getName().contains("Loader")) {
                    if (LumosAgent.cloader == null) {
                        System.out.println("Hooked " + loader);
                        LumosAgent.cloader = loader;
                    }
                }
                // String targetName = className.replace(File.separatorChar, '.');
                // if (entryClasses.contains(targetName)) {
                    // p("!! "+loader+"\n"+classBeingRedefined);
                    // while(!analyzeReady){
                    //     AgentThread.sleep(500);
                    // }

                    // SootClass entryClass = Scene.v().getSootClass(targetName);
                    // return CompileUtils.compileClass(entryClass);
                // }
                return classFileBuffer;
            }
        });
        // play();
        AgentThread t = new AgentThread(inst);
        Thread thread = new Thread(t);
        thread.start();
        
        long maxMemory = Runtime.getRuntime().maxMemory();
        System.out.println("Maximum memory (bytes): " +
                (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));
        // tplay();
        // t.refreshInsts();
        // while(!analyzeReady){
        //     AgentThread.sleep(1000);
        // }
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        Thread thread = new Thread(new AgentThread(inst));
        thread.start();
    }

    public static byte[] addFieldToClass(SootClass sclass, Type type, String fieldname) {
        return addFieldToClass(sclass, fieldname, fieldname, false);
    }

    public static byte[] addFieldToClass(SootClass sclass, Type type, String fieldname, boolean isStatic) {
        int mod = soot.Modifier.PUBLIC;
        if(isStatic){
            mod |= soot.Modifier.STATIC;
        }
        sclass.addField(Scene.v().makeSootField(fieldname, type, mod));
        byte[] bytecode = CompileUtils.compileClass(sclass);
        return bytecode;
    }

    public static byte[] addFieldToClass(SootClass sclass, String type, String fieldname) {
        return addFieldToClass(sclass, RefType.v(type), fieldname, false);
    }

    public static byte[] addFieldToClass(SootClass sclass, String type, String fieldname, boolean isStatic) {
        return addFieldToClass(sclass, RefType.v(type), fieldname, isStatic);
    }


    public static Map<String, byte[]> addField(String classname, String type, String fieldname) {
        Map<String, byte[]> cmap = new HashMap<>();
        SootClass sclass = findClass(classname);
        cmap.put(sclass.toString(), addFieldToClass(sclass, type, fieldname));
        return cmap;
    }

    public static String repoToObjClass(String repoName) {
        return "";
    }

    public static boolean addTP(LumosInstrumentation tp) {
        if (allTPs.contains(tp)) {
            return false;
        }
        allTPs.add(tp);
        if (!methodTPMap.containsKey(tp.getSm())) {
            methodTPMap.put(tp.getSm(), new HashSet<>());
        }

        methodTPMap.get(tp.getSm()).add(tp);
        return true;
    }

    public static void removeAllTPs() {
        for (String m : methodTPMap.keySet()) {
            Set<LumosInstrumentation> lms = methodTPMap.get(m);
            if (lms != null) {
                lms.clear();
            }
        }
        allTPs.clear();
    }

    public static boolean removeTP(LumosInstrumentation tp) {
        if (!allTPs.contains(tp)) {
            return false;
        }
        allTPs.remove(tp);
        if (methodTPMap.containsKey(tp.getSm())) {
            methodTPMap.get(tp.getSm()).remove(tp);
            return true;
        }
        return false;
    }

    public static void setupSoot(Collection<String> cpath, List<String> pdir) {
    // public static void setupSoot(String path) {
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

        String classpath = "";
        for (String cp : cpath) {
            classpath += cp + File.pathSeparator;
        }
        if (classpath.charAt(classpath.length() - 1) == File.pathSeparatorChar) {
            classpath = classpath.substring(0, classpath.length() - 1);
        }
        Options.v().set_soot_classpath(classpath);
        Options.v().set_java_version(8);
        processList = new ArrayList<String>();

        Options.v().set_process_dir(pdir);

        // Options.v().set_no_bodies_for_excluded(true);
        // Options.v().set_print_tags_in_output(true);

        // Use original names
        Options.v().setPhaseOption("jb", "optimize:false");
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("jb", "preserve-source-annotations:true");
        Options.v().setPhaseOption("jb", "stabilize-local-names:true");
        // Need this to avoid the need to provide an entry point
        // Options.v().setPhaseOption("cg", "all-reachable:true");

        // Need this to include all subtypes
        // Options.v().setPhaseOption("cg", "library:any-subtype");
        setExcludes();
        setIncludes();
        if (allOn) {
            p("loading otlp classes...");
            Scene.v().addBasicClass("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span",
                    SootClass.SIGNATURES);
            Scene.v().addBasicClass("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.SpanContext",
                    SootClass.SIGNATURES);
        }
        Scene.v().loadBasicClasses();
        // Scene.v().addBasicClass("io.opentelemetry.api.trace.Span",
        // SootClass.SIGNATURES);
        Scene.v().loadNecessaryClasses();
    }

    public static void setExcludes() {
        String[] exClasses = { "org.apache.hadoop.ant.*",
                "org.apache.hadoop.record.*", "org.apache.hadoop.metrics.*",
                "org.apache.hadoop.log.*",
                "org.apache.hadoop.metrics2.*",
                "org.apache.hadoop.hdfs.server.namenode.NameNodeHttpServer",
                "org.apache.hadoop.http.*",
                "org.apache.hadoop.hdfs.web.*",
                "org.apache.hadoop.hdfs.server.datanode.*",
                "org.apache.hadoop.fs.shell.*" };
        List<String> excludePackagesList = Arrays.asList(exClasses);
        Options.v().set_exclude(excludePackagesList);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_print_tags_in_output(true);
    }

    public static void setIncludes() {
        includeList = new ArrayList<String>();
        // includeList.add("java.lang.*");
        includeList.add("java.lang.*");
        includeList.add("java.util.*");
        Options.v().set_include(includeList);
        Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.String", SootClass.SIGNATURES);
        // Scene.v().addBasicClass("java.lang.Map", SootClass.SIGNATURES);
        // Scene.v().addBasicClass("java.lang.HashMap", SootClass.SIGNATURES);
        // Scene.v().addBasicClass("java.util.ArrayList", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.Object", SootClass.SIGNATURES);
        // Scene.v().addBasicClass("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span",
    }

    public static void p(String s) {
        System.out.println(s);
    }

    public static void setupClass(String service) {
        p("setting up class...");
        for (Iterator<SootClass> iter = Scene.v().getApplicationClasses().snapshotIterator(); iter
                .hasNext();) {
            SootClass cls = iter.next();
            List<SootMethod> sms = cls.getMethods();
            for (SootMethod sm : sms) {
                if (sm.isAbstract() || sm.isNative()) {
                    continue;
                }
                sm.retrieveActiveBody();
                // if(cls.getName().contains("App")){
                //     p("## " + sm+":\n"+sm.getActiveBody());
                // }
                // addTask(new Runnable() {
                    // @Override
                // public void run() {
                methodMap.put(sm.getSignature(), sm);
                bodyMap.put(sm.toString(), ((Body) sm.getActiveBody().clone()));
                // }
                // });
            }
            classMap.put(cls.toString(), cls);
        }
        // taskSync();
        // p("----Analysis Done------");
        // analyzeReady = true;
    }

    public static void analyzePath(String path) {
        // Options.v().set
        p("Analyzing " + path);
        // setupSoot(path);
        for (SootClass cls : Scene.v().getApplicationClasses()) {
            // p(""+cls);
            if (cls.toString().contains("conf.HttpAspect")) {
                continue;
            }
            for (SootMethod sm : cls.getMethods()) {
                // p(""+sm);
                if (sm.isAbstract()) {
                    continue;
                }
                sm.retrieveActiveBody();
                methodMap.put(sm.getSignature(), sm);
                bodyMap.put(sm.toString(), ((Body) sm.getActiveBody().clone()));
            }
            classMap.put(cls.toString(), cls);
            // CompileUtils.outputJimple(cls, "AAA");

        }
        // p("analyzeReady: " + analyzeReady);
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

    public static SootMethod findMethod(String... names) {
        for (String s : methodMap.keySet()) {
            boolean match = true;
            for (String name : names) {
                if (!s.contains(name)) {
                    match = false;
                    break;
                }
            }
            if (match) {
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

    public static void addEntryMethods(){
        // Global.addEntryMethods();
        // LumosAgent.entryMethods = Global.entryMethods;
        LumosAgent.entryMethods.add(Scene.v().getSootClass("com.mycompany.app.Work").getMethodByName("work"));
        p(entryMethods+"");
        for(SootMethod sm : entryMethods){
            entryClasses.add(sm.getDeclaringClass().getName());
        }

        for (SootMethod toggleM : entryMethods) {
            p("adding to " + toggleM.getName());
            Body b = findBodyNoClone(toggleM.toString());
            List<Stmt> stmts = CompileUtils.generateRRtoggle(b, getRRField(), true);
            CompileUtils.insertAt(b.getUnits(), stmts, CompileUtils.firstStmt(b), true);
            for (Stmt ret : CompileUtils.getReturnStmts(b)) {
                stmts = CompileUtils.generateRRtoggle(b, getRRField(), false);
                b.getUnits().insertBefore(stmts, ret);
            }
            toggleM.setActiveBody(b);
        }
    }
    public static void turnOnRR(){
    }
    public static void tplay() {
        addEntryMethods();
        // turnOnRR();
        p("----Analysis Done------");
        analyzeReady = true;
        readInsts();
        // analyzePath(cpath);
    }

    public static void readInsts() {
        p("reading inst files...");
        for (String s : CompileUtils.readFrom("/home/jingyuan/lumos/inst")) {
            LInst inst = LInst.fromSummary(s);
            activate(inst);
        }
    }

    public static void activate(LInst inst) {
        if (allInsts.contains(inst)) {
            return;
        }
        allInsts.add(inst);
        activeInsts.computeIfAbsent(inst.sm.toString(),
                e -> new HashSet<>()).add(inst);
    }

    public static Map<String, byte[]> instrument() {
        Map<String, byte[]> cmap = new ConcurrentHashMap<>();
        Set<SootClass> scToCompile = new HashSet<>();
        for (String smstr : activeInsts.keySet()) {
            SootMethod sm = Scene.v().getMethod(smstr);
            SootClass sclass = findClass(sm.getDeclaringClass().getName());
            if (checkSkipped(sclass)) {
                continue;
            }
            // if(!sclass.getName().contains("java.lang.")){ continue;}
            List<Stmt> targetstmts = new ArrayList<>();
            List<LInst> targetInsts = new ArrayList<>();

            Body b = findBody(smstr);
            // p("?? " + b);
            addTask(new Runnable() {
                @Override
                public void run() {
                    // This two-step way is needed to avoid inserted stmts
                    // from breaking the labeling
                    for (LInst inst : activeInsts.get(smstr)) {
                        // inst.body = b;
                        Stmt stmt = inst.getActualStmt(b);
                        targetstmts.add(stmt);
                        targetInsts.add(inst);
                    }

                    for (int i = 0; i < targetstmts.size(); i++) {
                        // Stmt stmt = targetstmts.get(i);
                        LInst inst = targetInsts.get(i);
                        // if (!(inst instanceof ConcurrencyInst)) {
                        // if(!sclass.getName().contains("app.App")){
                        // }

                        // try {
                        inst.instrument(b);
                        b.validate();
                        // } catch (Exception e) {
                        // p(b+"");
                        // e.printStackTrace();
                        // throw new RuntimeException();
                        // }
                        // }
                    }
                    // p(b+"");
                    sm.setActiveBody(b);
                }
            });
            scToCompile.add(sclass);
        }
        taskSync();

        for (SootClass sclass : scToCompile) {
            addTask(new Runnable() {
                @Override
                public void run() {
                    byte[] bytecode = CompileUtils.compileClass(sclass);
                    cmap.put(sclass.toString(), bytecode);
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
                    // e.printStackTrace();
                    // }

                }
            });
            // forTest = bytecode;
            // if(sclass.getShortName().contains("IntWrappingSpliterator")){
            //     try {
            //         Class<?> mc = Class.forName("java.util.stream.StreamSpliterators$IntWrappingSpliterator");
            //         p(mc.getClassLoader()+"");
            //         for(Constructor<?> ct: mc.getConstructors()){
            //             System.out.println("== "+ct+" :: " + Modifier.toString(ct.getModifiers()));
            //         }
            //         for(Method m : mc.getMethods()){
            //             System.out.println("== "+m+" :: " + Modifier.toString(m.getModifiers()));
            //         }
            //     } catch (ClassNotFoundException e) {
            //         e.printStackTrace();
            //     }
                
            //     for(SootMethod mm : sclass.getMethods()){
            //         System.out.println(mm.getSignature()+" :: " + Modifier.toString(mm.getModifiers()));

            //     }

            // }
        }
        taskSync();
        return cmap;
    }

    public static Map<String, byte[]> instrumentOld() {
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
                if (!(inst instanceof TimestampedInstrumentation)) {
                    List<Stmt> inserts = inst.addInsts();
                    if (inserts.size() > 0) {
                        CompileUtils.insertAt(units, inserts, stmt, inst.isBefore());
                    }
                }
            }

            for (int i = 0; i < targetstmts.size(); i++) {
                Stmt stmt = targetstmts.get(i);
                LumosInstrumentation inst = targetInsts.get(i);
                if (inst instanceof TimestampedInstrumentation) {
                    List<Stmt> inserts = inst.addInsts();
                    if (inserts.size() > 0) {
                        CompileUtils.insertAt(units, inserts.get(0),stmt, true);
                        inserts.remove(0);
                        CompileUtils.insertAt(units, inserts, stmt, false);
                        for (Unit uu : units) {
                            p(uu + "");
                        }
                    }

                }
            }
            sm.setActiveBody(b);
            scToCompile.add(sclass);
        }

        for (SootClass sclass : scToCompile) {
            byte[] bytecode = CompileUtils.compileClass(sclass);
            forTest = bytecode;
            // if(sclass.)
            cmap.put(sclass.toString(), bytecode);

        }
        return cmap;
    }

    public static void main(String args[]) {
        tplay();
    }

}
