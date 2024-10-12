package com.agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

import com.agent.compile.CompileUtils;
import com.agent.inst.ConcurrencyInst;
import com.agent.inst.LInst;
import com.agent.inst.ValueRecordingInst;

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
    public static String logger = "log4j";
    public static String mode = "on";

    // public static String logger = "stdout";
    // public static String rrClass = "com.mycompany.app.App";
    
    public static Set<SootMethod> entryMethods = new HashSet<>();
    public static Set<SootMethod> boundaryMethods = new HashSet<>();
    public static Set<SootMethod> pausedMethods = new HashSet<>();
    public static Set<String> entryClasses = new HashSet<>();
    public static String rrClass = "com.lumos.tracer.LumosTracer";
    public static String tracerJar = "/tmp/LumosTracer.jar";
    public static String bootstrapJar = "/tmp/LumosTracer-bootstrap.jar";
    public static String jrePath = "/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar";
    
    public static byte[] forTest;
    public static String testclass;
    // public static String jarpath = "/app/opentelemetry-api-trace-0.13.1.jar";
    public static String jarpath = "/app/opentelemetry-javaagent.jar";
    // public static String cpath = "/app/classes";
    public static String cpath = "";
    public static List<String> includeList;
    public static List<String> processList;
    public static Set<SootClass> baseInstClasses = new HashSet<>();
    // public static boolean 

    public static Set<String> skippedClasses = new HashSet<>(Arrays.asList(new String[]{

    // %Issue: these classes are static and will generate new native methods 
    // at runtime
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
        "javax.xml.parsers.SecuritySupport$1",
        "javax.xml.parsers.SecuritySupport$2",
        "javax.xml.parsers.SecuritySupport$3",
        "javax.xml.parsers.SecuritySupport$4",
    // Same issue, but with only one method problematic/missing
        "java.time.format.DateTimeFormatterBuilder",
    // don't understand why yet....
        // "com.google.common.base.Joiner$2",
        // "com.google.common.base.Joiner$3",
        // "com.google.common.collect.SingletonImmutableList$1",
    // interface; it reports verifyError even if we don't instrument anything...
        "java.time.chrono.ChronoLocalDateTime",
        "java.time.chrono.Chronology",
        "java.time.chrono.ChronoZonedDateTime",
    // Currently removed for stopping infinite recursion in tracer
        
        "java.lang.ref.WeakReference",
        "java.lang.ref.Reference",
        "java.lang.ThreadLocal$ThreadLocalMap",
        "java.lang.ThreadLocal$ThreadLocalMap$Entry",
        "java.lang.ThreadLocal",
        "java.util.WeakHashMap",
        "java.lang.Thread",

        "java.util.WeakHashMap$EntrySet",
        "java.util.WeakHashMap$Values",
        "java.util.WeakHashMap$ValueIterator",
        "java.nio.channels.spi.AbstractSelector",
        "sun.nio.ch.SelectionKeyImpl",
        "java.nio.channels.spi.AbstractSelectionKey",
        "java.nio.channels.spi.AbstractSelectableChannel",
        
        "java.util.concurrent.atomic.AtomicInteger",
        "java.util.concurrent.atomic.AtomicLong",
        "java.util.concurrent.atomic.AtomicBoolean",
        "sun.misc.Unsafe",
        "java.util.concurrent.locks.AbstractQueuedSynchronizer",
        "java.util.concurrent.locks.ReentrantLock",
        // "java.util.concurrent.atomic.AtomicInteger",
        // "java.lang.ref.ReferenceQueue$Lock",
        // "java.lang.ref.ReferenceQueue$Null",
        // "java.lang.ThreadGroup",
        // "sun.misc.Cleaner"

        // These got us stuck for some reason...
        "javax.xml.parsers.FactoryFinder",
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
        String csn = cls.toString();
        return skippedClasses.contains(csn) ||
            csn.contains("java.util.concurrent");
            // ||
            // csn.contains("com.google.common.cache") ||
            // csn.contains("org.apache.hadoop.security");
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        JarFile tracerJarFile = null;
        JarFile slf4jJarFile = null;
        JarFile slf4j_log4j12JarFile = null;

        JarFile toolsJarFile = null;
        try {
            tracerJarFile = new JarFile(bootstrapJar);
            // slf4jJarFile = new JarFile("/tmp/slf4j-api.jar");
            toolsJarFile = new JarFile("/usr/lib/jvm/java-8-openjdk-amd64/lib/tools.jar");
            // slf4j_log4j12JarFile = new JarFile("/tmp/slf4j-log4j12.jar");
        } catch (IOException e) {
            e.printStackTrace();
        }
        inst.appendToBootstrapClassLoaderSearch(tracerJarFile);
        // if(inst !=null)
        //     return;
        // inst.appendToBootstrapClassLoaderSearch(toolsJarFile);
        mode = System.getProperty("mode");
        // inst.appendToBootstrapClassLoaderSearch(slf4jJarFile);

        // inst.appendToBootstrapClassLoaderSearch(slf4j_log4j12JarFile);
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
                return classFileBuffer;
            }
        });
        AgentThread t = new AgentThread(inst);
        Thread thread = new Thread(t);
        thread.start();
        
        long maxMemory = Runtime.getRuntime().maxMemory();
        System.out.println("Maximum memory (bytes): " +
                (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));
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
        // processList = new ArrayList<String>();

        Options.v().set_process_dir(pdir);

        // Options.v().set_no_bodies_for_excluded(true);
        // Options.v().set_print_tags_in_output(true);

        // Use original names
        Options.v().setPhaseOption("jb", "optimize:false");
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("jb", "preserve-source-annotations:true");
        Options.v().setPhaseOption("jb", "stabilize-local-names:true");
        // %Issue: lambda unresolved
        Options.v().setPhaseOption("jb", "model-lambdametafactory:false");
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
        setEntryPoint();
    }

    public static void setExcludes() {
        String[] exClasses = { "org.apache.hadoop.ant.*",
                "org.apache.hadoop.record.*", 
                "org.apache.hadoop.log.*",
                //"org.apache.hadoop.metrics2.*",
                //"org.apache.hadoop.metrics.*",
                "org.apache.hadoop.hdfs.server.namenode.NameNodeHttpServer",
                "org.apache.hadoop.http.*",
                "org.apache.hadoop.hdfs.web.*",
                "org.apache.hadoop.hdfs.server.datanode.*",
                "org.apache.hadoop.fs.shell.*",
                // "edu.brown.cs.*"
        };
        List<String> excludePackagesList = Arrays.asList(exClasses);
        Options.v().set_exclude(excludePackagesList);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_print_tags_in_output(true);
    }

    public static void setIncludes() {
        includeList = new ArrayList<String>();
        includeList.add("java.lang.*");
        includeList.add("java.util.*");

        includeList.add("java.lang.invoke.*");
        includeList.add("java.io.*");
        includeList.add("java.text.*");
        includeList.add("java.nio.*");
        includeList.add("java.rmi.server.*");
        includeList.add("java.sql.*");
        includeList.add("java.security.*");
        includeList.add("java.net.*");
        includeList.add("java.math.*");
        includeList.add("java.time.*");

        includeList.add("javax.management.*");
        includeList.add("javax.ws.rs.*");
        includeList.add("javax.xml.*");
        includeList.add("javax.net.*");
        includeList.add("javax.crypto.*");
        includeList.add("javax.security.*");
        includeList.add("javax.naming.*");
        includeList.add("javax.servlet.*");


        includeList.add("com.sun.org.apache.*");
        includeList.add("com.sun.jersey.*");
        includeList.add("com.sun.jmx.*");
        includeList.add("com.sun.xml.*");
        includeList.add("com.sun.naming.*");
        includeList.add("com.sun.java_cup.*");
        includeList.add("com.sun.crypto.provider.*");
        includeList.add("com.sun.net.ssl.*");
        includeList.add("com.sun.research.*");

        includeList.add("sun.security.*");
        includeList.add("sun.net.*");
        includeList.add("sun.text.*");
        includeList.add("sun.nio.*");
        includeList.add("sun.misc.*");
        includeList.add("sun.management.*");
        includeList.add("sun.util.*");
        includeList.add("sun.invoke.util.*");
        includeList.add("sun.reflect.*");

        includeList.add("org.apache.commons.daemon.*");
        includeList.add("org.xml.sax.*");
        includeList.add("org.w3c.dom.*");
        includeList.add("com.google.common.*");
        includeList.add("jdk.internal.misc.*");
        includeList.add("jdk.net.*");
    
        includeList.add("java.beans.*");
        includeList.add("com.sun.beans.*");
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

    public static void p(Object s) {
        p(s+"");
    }
    public static void setEntryPoint(){
        List<SootMethod> entryList = new ArrayList<>();
        SootClass nnClass = Scene.v().getSootClass("org.apache.hadoop.hdfs.server.namenode.NameNode");
        SootMethod nnMain = nnClass.getMethodByName("main");
        entryList.add(nnMain);
        Scene.v().setEntryPoints(entryList);
    }

    public static void setupClass(String service) {
        p("setting up class...");
/*
        Options.v().setPhaseOption("cg", "safe-forname:true");
        // Options.v().setPhaseOption("cg", "safe-newinstance:true");
        Options.v().setPhaseOption("cg", "resolve-all-abstract-invokes:true");
        Options.v().setPhaseOption("cg", "types-for-invoke:true");
        Options.v().setPhaseOption("cg", "verbose:false");
        Options.v().set_whole_program(true);
        List<String> dynamicClasses = new ArrayList<>();
        dynamicClasses.add("org.apache.hadoop.ipc.ProtobufRpcEngine");
        Options.v().set_dynamic_class(dynamicClasses);
        Scene.v().loadNecessaryClasses();

        Transform sparkConfig = new Transform("cg.spark", null);
        Options.v().setPhaseOption("cg", "verbose:false");
        PhaseOptions.v().setPhaseOption(sparkConfig, "enabled:true");
        // PhaseOptions.v().setPhaseOption(sparkConfig, "vta:true");
        // PhaseOptions.v().setPhaseOption(sparkConfig, "on-fly-cg:false");
        // PhaseOptions.v().setPhaseOption(sparkConfig, "types-for-sites:true");
       // PhaseOptions.v().setPhaseOption(sparkConfig, "field-based:true");
//       PhaseOptions.v().setPhaseOption(sparkConfig,"cs-demand:true");
        // PhaseOptions.v().setPhaseOption(sparkConfig, "verbose:true");
        PhaseOptions.v().setPhaseOption(sparkConfig, "apponly:false");
        long start,end;
        // 123
        Map<String, String> phaseOptions = PhaseOptions.v().getPhaseOptions(sparkConfig);
        p("starting spark pta...");
        start = System.nanoTime();
        SparkTransformer.v().transform(sparkConfig.getPhaseName(), phaseOptions);
        end = System.nanoTime();
        p("spark-2 time: " + (end-start)/1e9+" seconds");
        // CHATransformer.v().transform();
        // p("CHA done");
        */
        // Scene.v().getReachableMethods().listener().forEachRemaining(mc -> {
        // Scene.v().getReachableMethods().listener().forEachRemaining( ->{
        List<String> retrieveHistory = CompileUtils.readFrom("/home/jingyuan/lumos/retrieveHistory");
        for(String s:retrieveHistory){
            // %Issue: lambda is currently unresolved
            if(s.contains("$lambda_")){
                continue;
            }
            if(s.contains("log4j")){
                continue;
            }
            if(s.contains("edu.brown.cs")){
                continue;
            }
            SootMethod sm = Scene.v().getMethod(s);
            // for (SootMethod sm : cls.getMethods()) {
                if (sm.isAbstract() || sm.isNative()) {
                    continue;
                }
                // System.out.println("reading " + sm);
                sm.retrieveActiveBody();
                methodMap.put(sm.getSignature(), sm);
                bodyMap.put(sm.toString(), ((Body) sm.getActiveBody().clone()));
            // }
            SootClass cls = sm.getDeclaringClass();
            classMap.put(cls.toString(), cls);
            // if(cls.getName().contains("$lambda_")){
            //     String pkg = cls.getPackageName();
                
            // }
        }
        // });
        p("----Analysis Done------");
        analyzeReady = true;
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

    public static void loadBody (SootMethod sm){
        sm.retrieveActiveBody();
    }

    public static void loadBodies(SootClass sc){
        for(SootMethod sm:sc.getMethods()){
            loadBody(sm);
        }
    }

    public static SootMethod findMethod(String name) {
        return Scene.v().getMethod(name);
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
        // LumosAgent.entryMethods.add(Scene.v().getSootClass("com.mycompany.app.Work").getMethodByName("work"));
        SootClass ecls = Scene.v().getSootClass("org.apache.hadoop.hdfs.server.namenode.NameNodeRpcServer");
        for(SootMethod sm : ecls.getMethods()){
            String mname = sm.getName();
            String specialName = System.getProperty("specialName");
            if (mname.equals("<init>") ||
                    mname.equals("join") ||
                    mname.equals("start") ||
                    mname.equals("stop") ||
                    mname.equals("<clinit>") ||
                    mname.equals("checkNNStartup") ||
                    (specialName != null && !mname.equals(specialName))) {
                continue;
            }
            entryMethods.add(sm);
        }

        // entryMethods.add(Scene.v().getSootClass(
        //     "org.apache.hadoop.hdfs.server.blockmanagement.PendingReplicationBlocks$PendingReplicationMonitor").
        //         getMethodByName("pendingReplicationCheck"));
        // entryMethods.add(Scene.v().getSootClass(
        //     "org.apache.hadoop.hdfs.server.blockmanagement.DecommissionManager$Monitor").
        //         getMethodByName("check"));
        // entryMethods.add(Scene.v().getSootClass(
        //     "org.apache.hadoop.hdfs.server.blockmanagement.HeartbeatManager").
        //         getMethodByName("heartbeatCheck"));
        // entryMethods.add(Scene.v().getSootClass(
        //     "org.apache.hadoop.hdfs.server.blockmanagement.BlockManager").
        //         getMethodByName("computeDatanodeWork"));
        // entryMethods.add(Scene.v().getSootClass(
        //     "org.apache.hadoop.hdfs.server.blockmanagement.BlockManager").
        //         getMethodByName("processPendingReplications"));
        // entryMethods.add(Scene.v().getSootClass(
        //     "org.apache.hadoop.hdfs.server.namenode.FSNamesystem").
        //         getMethodByName("checkAvailableResources"));
        // entryMethods.add(Scene.v().getSootClass(
        //     "org.apache.hadoop.hdfs.server.namenode.FSNamesystem").
        //         getMethodByName("nameNodeHasResourcesAvailable"));
        // entryMethods.add(Scene.v().getSootClass(
        //     "org.apache.hadoop.hdfs.server.namenode.FSNamesystem").
        //         getMethodByName("enterSafeMode"));
        // entryMethods.add(Scene.v().getSootClass(
        //     "org.apache.hadoop.hdfs.server.namenode.FSNamesystem").
        //         getMethodByName("isInSafeMode"));
        p(entryMethods.size() + "");
        for (SootMethod sm : entryMethods) {
            entryClasses.add(sm.getDeclaringClass().getName());
        }

        for (SootMethod toggleM : entryMethods) {
            p("adding to " + toggleM.getName());
            Body b = getBody(toggleM);
            List<Stmt> stmts = CompileUtils.generateRRtoggle(b, getRRField(), true);
            CompileUtils.insertAt(b.getUnits(), stmts, CompileUtils.firstStmt(b), true);
            for (Stmt ret : CompileUtils.getReturnStmts(b)) {
                stmts = CompileUtils.generateRRtoggle(b, getRRField(), false);
                b.getUnits().insertBefore(stmts, ret);
            }
            toggleM.setActiveBody(b);
        }
    }

    public static void addAsBoundary(String sc){
        addAsBoundary(Scene.v().getSootClass(sc));
    }
    public static void addAsBoundary(SootClass sc){
        for(SootMethod sm : sc.getMethods()){
            // if(sc.getShortName().equals("ClassLoader")){
            //     p("## " + sm +", " + sm.getSource() +", " + sm.hasActiveBody());
            // }
            boundaryMethods.add(sm);
        }
    }

    public static void addBoundaries() {
        addAsBoundary("java.lang.ClassLoader");
        // for(SootMethod sm : clder.getMethods()){
        //     String sname = sm.getName();
        //     if(sname.equals("loadClass") ||
        //             sname.equals("getClassLoadingLock")){
        //         boundaryMethods.add(sm);
        //     }
        // }
        
        for(SootClass sc: Scene.v().getClasses()){
            String pkg = sc.getPackageName();
            if(pkg.startsWith("edu.brown.cs") || pkg.contains("metrics")){
                if(sc.resolvingLevel() >= SootClass.BODIES){
                    addAsBoundary(sc);
                }
            }
        }
        // SootClass fslogc = Scene.v().getSootClass("org.apache.hadoop.hdfs.server.namenode.FSEditLog");
        // addAsBoundary(fslogc);

        // SootClass sbuilderc = Scene.v().getSootClass("java.lang.StringBuilder");
        // addAsBoundary(sbuilderc);
        // SootMethod checkerm = Scene.v().getMethod("<org.apache.hadoop.hdfs.server.namenode.FSNamesystem: org.apache.hadoop.hdfs.server.namenode.FSPermissionChecker getPermissionChecker()>");
        // boundaryMethods.add(checkerm);
        SootMethod forkm = Scene.v().getMethod("<edu.brown.cs.systems.baggage.Baggage: edu.brown.cs.systems.baggage.DetachedBaggage fork()>");
        boundaryMethods.add(forkm);

        // addAsBoundary("org.apache.hadoop.ipc.Server$ExceptionsHandler");
        // addAsBoundary("edu.brown.cs.systems.baggage.Baggage");
        for (SootMethod bm : boundaryMethods) {
            // if(!bm.hasActiveBody() && bm.getSource() == null){
            //     continue;
            // }
            Body b;
            try {
                b = getBody(bm);
            } catch (RuntimeException e) {
                continue;
            }

            p("adding to " + bm.getName());
            List<Stmt> stmts = CompileUtils.generateRRsave(b, getRRField());
            stmts.addAll(CompileUtils.generateRRtoggle(b, getRRField(), false));
            CompileUtils.insertAt(b.getUnits(), stmts, CompileUtils.firstStmt(b), true);
            for (Stmt ret : CompileUtils.getReturnStmts(b)) {
                stmts = CompileUtils.generateRRrestore(b, getRRField());
                b.getUnits().insertBefore(stmts, ret);
            }
            bm.setActiveBody(b);
            // p(b);
        }

        
        SootMethod ehm = Scene.v().getMethod("<org.apache.hadoop.ipc.Server$Handler: void run()>");
        boundaryMethods.add(ehm);
        Body b = getBody(ehm);

        for (Unit u : b.getUnits()) {
            Stmt stmt = (Stmt) u;
            if (stmt.toString().contains("UndeclaredThrowableException")) {
                List<Stmt> stmts = CompileUtils.generateRRtoggle(b, getRRField(), false);
                b.getUnits().insertAfter(stmts, stmt);
                break;
            }
        }
        // p(b);
        ehm.setActiveBody(b);
        
    }

    public static void addPauseStmts(){
        SootClass clder = Scene.v().getSootClass("org.apache.hadoop.util.Shell");
        for(SootMethod sm : clder.getMethods()){
            String sname = sm.getName();
            if(sname.equals("runCommand")){
                pausedMethods.add(sm);
            }
        }
        for (SootMethod bm : pausedMethods) {
            p("adding to " + bm.getName());
            Body b = getBody(bm);
            
            List<Stmt> stmts = CompileUtils.generateRRsave(b, getRRField());
            stmts.addAll(CompileUtils.generateRRtoggle(b, getRRField(), false));
            CompileUtils.insertAt(b.getUnits(), stmts, CompileUtils.firstStmt(b), true);
            for (Stmt ret : CompileUtils.getReturnStmts(b)) {
                stmts = CompileUtils.generateRRrestore(b, getRRField());
                b.getUnits().insertBefore(stmts, ret);
            }
            bm.setActiveBody(b);
            p(b);
        }
    }
    // %Issue: currently we only support instrumenting once;
    // the getBody should ideally cache original body for future instrumentation
    public static Body getBody(SootMethod sm){
            if(!sm.hasActiveBody()){
                sm.retrieveActiveBody();
            }
            return sm.getActiveBody();
    }

    public static Body getBody(String s){
        return getBody(Scene.v().getMethod(s));
    }

    public static void turnOnRR(){
    }
    public static void lplay() {
        addEntryMethods();
        addBoundaries();
        // turnOnRR();
        p("----Analysis Done------");
        analyzeReady = true;
        readInsts();
        // analyzePath(cpath);
    }

    public static LInst fromSummary(String summary){
        String[] items = summary.split(LInst.SEPARATOR);
        String type = items[0];
        SootMethod sm = LumosAgent.findMethod(items[1]);
        sm.retrieveActiveBody();
        String stmt = items[2];
        int lineNum = Integer.valueOf(items[3]);
        if(type.equals("concurrency")){
            return new ConcurrencyInst(sm, stmt, lineNum, type);
        }
        else{
            return new ValueRecordingInst(sm, stmt, lineNum, type);
        }
    }
    public static void readInsts() {
        p("reading inst files...");
        String instFile = System.getProperty("instFile");
        List<String> allInsts = CompileUtils.readFrom(instFile);
        SootClass sc = Scene.v().getSootClass("java.time.temporal.TemporalQueries");
        SootMethod sm = sc.getMethodByName("<clinit>");

        for (String s : allInsts) {
            // if(!s.contains("$lambda")){
            //     continue;
            // }
            // %Issue: lambda currently unresolved
            if(s.contains("$lambda_")){
                continue;
            }
            LInst inst = fromSummary(s);
            activate(inst);
        }

        String baseInstFile = System.getProperty("baseInstFile");
        allInsts = CompileUtils.readFrom(baseInstFile);
        for(String s : allInsts){
            if(s.contains("$lambda_")){
                continue;
            }
            String[] items = s.split(LInst.SEPARATOR);
            SootMethod m = LumosAgent.findMethod(items[1]);
            SootClass c = m.getDeclaringClass();
            baseInstClasses.add(c);
        }
    }

    public static void activate(LInst inst) {
        if (allInsts.contains(inst)) {
            return;
        }
        allInsts.add(inst);

        // if (inst.sm.getDeclaringClass().getShortName().equals("FSNamesystem")) {
        //     p("ADD INST " + inst);
        //     p(inst.sm.toString());
        // }
        activeInsts.computeIfAbsent(inst.sm.toString(),
                e -> new HashSet<>()).add(inst);
    }

    public static Map<String, byte[]> instrument() {
        Map<String, byte[]> cmap = new ConcurrentHashMap<>();
        Set<SootClass> scToCompile = new HashSet<>();
        for (String smstr : activeInsts.keySet()) {
            // p("handling " + smstr + "...");
            SootMethod sm = Scene.v().getMethod(smstr);
            // SootClass sclass = findClass(sm.getDeclaringClass().getName());
            SootClass sclass = sm.getDeclaringClass();
            // if(sclass.getPackageName().contains("java.")){
            //     continue;
            // }
            if (checkSkipped(sclass)) {
                continue;
            }
            // p("found inst in " + sclass);
            // %Issue: this method is too large
            if(sm.getSignature().contains("org.apache.hadoop.util.PureJavaCrc32: void <clinit>()")){ continue;}
            // if(!sclass.getName().contains(")){ continue;}
            List<Stmt> targetstmts = new ArrayList<>();
            List<LInst> targetInsts = new ArrayList<>();

            Body b = (Body) getBody(smstr).clone();


            addTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        long start, end;
                        start = System.nanoTime();
                        // This two-step way is needed to avoid inserted stmts
                        // from breaking the labeling
                        for (LInst inst : activeInsts.get(smstr)) {
                            Stmt stmt = inst.getActualStmt(b);
                            targetstmts.add(stmt);
                            targetInsts.add(inst);
                        }
                        for (int i = 0; i < targetstmts.size(); i++) {
                            // if (sclass.getName().equals("java.time.chrono.ChronoLocalDateTime")) {
                            //     break;
                            // }
                            LInst inst = targetInsts.get(i);
                            if(!mode.equals("off")){
                                inst.instrument(b);
                            }
                            b.validate();
                        }
                        // if (smstr.contains("AtomicBoolean: boolean get")) {
                        //     p("## " + smstr);
                        //     p(b + "");
                        // }
                        sm.setActiveBody(b);
                        end = System.nanoTime();
                        // p("Time=" + (end-start)/1e9+"s");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            // p("added for compiling of " + sclass);
            scToCompile.add(sclass);
        }
        taskSync();
        p("Instrumentation done.");
        p("Now compiling...");
        entryMethods.forEach(m -> scToCompile.add(m.getDeclaringClass()));
        boundaryMethods.forEach(m -> scToCompile.add(m.getDeclaringClass()));
        scToCompile.addAll(baseInstClasses);
        for (SootClass sclass : scToCompile) {
            for(SootMethod sm: sclass.getMethods()){
                if(!sm.hasActiveBody()){
                    if(!sm.isAbstract() && !sm.isNative()){
                        sm.retrieveActiveBody();
                    }
                }
            }
            // p("compiling " + sclass);
            // if (sclass.getName().equals("java.time.chrono.ChronoLocalDateTime")) {
            //     Printer.v().printTo(sclass, new PrintWriter(System.out, true));
                // compile("ChronoLocalDateTime", CompileUtils.compileClass(sclass));
                // try {
                //     Class<?> mc = Class.forName("org.apache.hadoop.hdfs.server.namenode.NameCache$UseCount",
                //             true, LumosAgent.cloader);
                //     for(Method m : mc.getMethods()){
                //         System.out.println("J: " + m+" :: " + Modifier.toString(m.getModifiers()));
                //     }
                //     for(SootMethod mm : sclass.getMethods()){
                //         System.out.println("S: " + mm.getSignature() + " :: " +
                //                 Modifier.toString(mm.getModifiers()));
                //     }
                // } catch (ClassNotFoundException e) {
                //     e.printStackTrace();
                // }

            // }
            addTask(new Runnable() {
                @Override
                public void run() {
                    try{
                        byte[] bytecode = CompileUtils.compileClass(sclass);
                        cmap.put(sclass.toString(), bytecode);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }

                }
            });
        }
        taskSync();
        p("Compilation done");
        return cmap;
    }
    public static void compile(String name, byte[] bytecode){
        String dirname = "AAA";
        File outputDir = new File(dirname);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        File file2 = new File(dirname + "/" + name + ".class");
        FileOutputStream classout;
        try {
            classout = new FileOutputStream(file2);
            classout.write(bytecode);
            classout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, byte[]> instrumentOld() {
        Map<String, byte[]> cmap = new HashMap<>();
        Set<SootClass> scToCompile = new HashSet<>();
        for (String smstr : methodTPMap.keySet()) {
            SootMethod sm = findMethod(smstr);
            //Body b = findBody(sm.toString());
            Body b = (Body)getBody(sm).clone();
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
        lplay();
    }

}
