package com.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

import com.agent.compile.CompileUtils;
import com.agent.inst.ConcurrencyInst;
import com.agent.inst.ExperimentInst;
import com.agent.inst.LInst;
import com.agent.inst.NondInst;
import com.agent.inst.PhasedTracingInst;
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

    // public static Set<LumosInstrumentation> allTPs = new HashSet<>();
    public static Set<LInst> allInsts = new HashSet<>();
    // public static Map<String, Set<LumosInstrumentation>> methodTPMap = new HashMap<>();
    public static Map<String, Set<LInst>> activeInsts = new HashMap<>();
    // public static Set<DBInstrumentationPoint> allTPs = new HashSet<>();
    // public static HashMap<String, Set<TracePoint>> methodTPMap = new HashMap<>();
    public static String logger = "log4j";
    public static String mode = "on";

    // public static String logger = "stdout";
    // public static String rrClass = "com.mycompany.app.App";
    // [FIXME] add components from env vars
    public static String component = "nn";
    public static Set<SootMethod> entryMethods = new HashSet<>();
    public static Set<SootMethod> boundaryMethods = new HashSet<>();
    public static Set<SootMethod> pausedMethods = new HashSet<>();
    public static Set<String> entryClasses = new HashSet<>();
    public static String rrClass = "com.lumos.tracer.LumosTracer";
    public static String tracerJar = System.getenv("LUMOS_TRACER_DIR") + "/LumosTracer.jar";
    public static String bootstrapJar = System.getenv("LUMOS_TRACER_DIR") + "/LumosTracer-bootstrap.jar";
    // public static String jrePath = System.getenv("JAVA_HOME") + "/lib/openjdk/jre/lib/rt.jar";
    public static String jrePath;
    public static String toolsJarPath;
    public static String verbose;
    static{
        String isDev = System.getProperty("Dev");
        if(isDev != null){
            jrePath = System.getenv("JAVA_HOME") + "/jre/lib/rt.jar";
            toolsJarPath = System.getenv("JAVA_HOME") + "/tools.jar";
        }
        else{
            jrePath = System.getenv("JAVA_HOME") + "/lib/openjdk/jre/lib/rt.jar";
            toolsJarPath = System.getenv("JAVA_HOME") + "/lib/openjdk/lib/tools.jar";
        }
        String comp = System.getProperty("component");
        if (comp != null && comp.equals("dn")) {
            component = "dn";
        }
        String vb = System.getProperty("verbose");
        if (vb != null && vb.contains("debug")) {
            verbose = "debug";
        } else {
            verbose = "performance";
        }
    }

    public static Map<String, Map<String, String>> translationMap;
    public static Map<String, String> invTranslationMap;
    public static byte[] forTest;
    public static String testclass;
    // public static String jarpath = "/app/opentelemetry-api-trace-0.13.1.jar";
    // public static String jarpath = "/app/opentelemetry-javaagent.jar";
    // public static String cpath = "/app/classes";
    public static String cpath = "";
    public static List<String> includeList;
    public static List<String> processList;
    public static Set<SootClass> baseInstClasses = new HashSet<>();
    public static boolean TimeOn = true;
    // public static boolean 

    public static Set<String> skippedClasses = new HashSet<>(Arrays.asList(new String[]{

    // FIXME: these classes are static and will generate new native methods 
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
            toolsJarFile = new JarFile(toolsJarPath);
            // slf4jJarFile = new JarFile("/tmp/slf4j-api.jar");
            // slf4j_log4j12JarFile = new JarFile("/tmp/slf4j-log4j12.jar");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // inst.appendToBootstrapClassLoaderSearch(tracerJarFile);
        // if(inst !=null)
        //     return;
        // inst.appendToBootstrapClassLoaderSearch(toolsJarFile);
        mode = System.getProperty("mode");

        String componentStr = System.getProperty("component");
        if(componentStr != null){
            component = componentStr;
        }

        String ton = System.getProperty("TimeOn");
        if (ton != null && ton.equals("false")) {
            LumosAgent.TimeOn = false;
        }
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

    // public static boolean addTP(LumosInstrumentation tp) {
    //     if (allTPs.contains(tp)) {
    //         return false;
    //     }
    //     allTPs.add(tp);
    //     if (!methodTPMap.containsKey(tp.getSm())) {
    //         methodTPMap.put(tp.getSm(), new HashSet<>());
    //     }

    //     methodTPMap.get(tp.getSm()).add(tp);
    //     return true;
    // }

    // public static void removeAllTPs() {
    //     for (String m : methodTPMap.keySet()) {
    //         Set<LumosInstrumentation> lms = methodTPMap.get(m);
    //         if (lms != null) {
    //             lms.clear();
    //         }
    //     }
    //     allTPs.clear();
    // }

    // public static boolean removeTP(LumosInstrumentation tp) {
    //     if (!allTPs.contains(tp)) {
    //         return false;
    //     }
    //     allTPs.remove(tp);
    //     if (methodTPMap.containsKey(tp.getSm())) {
    //         methodTPMap.get(tp.getSm()).remove(tp);
    //         return true;
    //     }
    //     return false;
    // }

    public static void setSootOptions(){
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
        Options.v().set_java_version(8);
        // Use original names
        
        Options.v().setPhaseOption("jb", "optimize:false");
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("jb", "preserve-source-annotations:true");
        Options.v().setPhaseOption("jb", "stabilize-local-names:true");
        
        // %FIXME: lambda unresolved
        Options.v().setPhaseOption("jb", "model-lambdametafactory:false");
        // Need this to avoid the need to provide an entry point
        Options.v().setPhaseOption("cg", "all-reachable:true");

    
    }
    public static void loadClasses(){
        Scene.v().loadBasicClasses();
        Scene.v().loadNecessaryClasses();
    }

    public static void setupSoot(Collection<String> cpath, List<String> pdir) {
        setSootOptions();
        String classpath = "";
        for (String cp : cpath) {
            classpath += cp + File.pathSeparator;
        }
        if (classpath.charAt(classpath.length() - 1) == File.pathSeparatorChar) {
            classpath = classpath.substring(0, classpath.length() - 1);
        }
        Options.v().set_soot_classpath(classpath);
        Options.v().set_process_dir(pdir);
        setExcludes();
        setIncludes();
        loadClasses();
    }

    public static void setExcludes() {
        String[] exClasses = {};
        if (component.equals("nn")) {
            exClasses = new String[]{"org.apache.hadoop.ant.*","org.apache.hadoop.record.*","org.apache.hadoop.log.*",
            // "org.apache.hadoop.metrics2.*",
            // "org.apache.hadoop.metrics.*",
            "org.apache.hadoop.hdfs.server.namenode.NameNodeHttpServer","org.apache.hadoop.http.*","org.apache.hadoop.hdfs.web.*","org.apache.hadoop.hdfs.server.datanode.*","org.apache.hadoop.fs.shell.*",
            // "edu.brown.cs.*"
            };
        }
        else if (component.equals("dn")){
            exClasses = new String[]{"org.apache.hadoop.ant.*","org.apache.hadoop.record.*","org.apache.hadoop.log.*",
            // "org.apache.hadoop.metrics2.*",
            // "org.apache.hadoop.metrics.*",
            "org.apache.hadoop.hdfs.server.namenode.NameNodeHttpServer","org.apache.hadoop.http.*","org.apache.hadoop.hdfs.web.*","org.apache.hadoop.hdfs.server.namenode.*","org.apache.hadoop.fs.shell.*",
            // "edu.brown.cs.*"
            };
        }
        List<String> excludePackagesList = Arrays.asList(exClasses);
        Options.v().set_exclude(excludePackagesList);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_print_tags_in_output(true);
    }

    public static void setIncludes() {
        includeList = new ArrayList<String>();
        includeList.add("java.lang.*");
/*
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
        */
        Options.v().set_include(includeList);
        Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.String", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.Object", SootClass.SIGNATURES);
        // Scene.v().addBasicClass("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span",
    }

    public static void p(String s) {
        System.out.println(s);
    }

    public static void p(Object s) {
        p(s+"");
    }
    // public static void setEntryPoint(){
    //     List<SootMethod> entryList = new ArrayList<>();
    //     SootClass nnClass = Scene.v().getSootClass("org.apache.hadoop.hdfs.server.namenode.NameNode");
    //     SootMethod nnMain = nnClass.getMethodByName("main");
    //     entryList.add(nnMain);
    //     Scene.v().setEntryPoints(entryList);
    // }

    public static void setupClass(String service) {
        p("setting up class...");
        List<String> retrieveHistory = CompileUtils.readFrom("/home/jingyuan/lumos/retrieveHistory");
        for(String s:retrieveHistory){
            // FIXME: lambda is currently unresolved
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

    public static void analyzePath() {
        for (SootClass cls : Scene.v().getApplicationClasses()) {
            // p(""+cls);
            if (cls.toString().contains("conf.HttpAspect")) {
                continue;
            }
            for (SootMethod sm : cls.getMethods()) {
                // p(""+sm);
                if (sm.isAbstract() || sm.isNative()) {
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
        for (SootMethod sm : sc.getMethods()) {
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
        String specialName = System.getProperty("entry");
        // LumosAgent.entryMethods.add(Scene.v().getSootClass("com.mycompany.app.Work").getMethodByName("work"));
        if (component.equals("nn")) {
            SootClass ecls = Scene.v().getSootClass("org.apache.hadoop.hdfs.server.namenode.NameNodeRpcServer");
            for (SootMethod sm : ecls.getMethods()) {
                String mname = sm.getName();
                if (mname.equals("<init>") ||
                        mname.equals("join") ||
                        mname.equals("start") ||
                        mname.equals("stop") ||
                        mname.equals("<clinit>") ||
                        mname.equals("checkNNStartup") ||
                        mname.equals("verifyRequest") ||
                        mname.equals("verifySoftwareVersion")||
                        mname.equals("verifyLayoutVersion")||
                        mname.equals("getClientMachine") ||
                        mname.equals("getServiceRpcAddress") ||
                        mname.equals("getRpcAddress") ||
                        mname.equals("getRemoteUser") ||
                        mname.equals("checkPathLength") ||
                        mname.equals("readOp") ||
                        (specialName != null && !specialName.equals("any") && !mname.equals(specialName))) {
                    continue;
                }
                entryMethods.add(sm);
            }
            if (specialName.equals("any")) {

                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.blockmanagement.BlockManager")
                        .getMethodByName("computeDatanodeWork"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.blockmanagement.BlockManager")
                        .getMethodByName("processPendingReplications"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.blockmanagement.BlockManager")
                        .getMethodByName("rescanPostponedMisreplicatedBlocks"));

                entryMethods.add(Scene.v().getMethod("<org.apache.hadoop.hdfs.server.namenode.FSNamesystem$LazyPersistFileScrubber: void clearCorruptLazyPersistFiles()>"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.namenode.FSNamesystem")
                        .getMethodByName("checkAvailableResources"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.namenode.FSNamesystem")
                        .getMethodByName("nameNodeHasResourcesAvailable"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.blockmanagement.PendingReplicationBlocks$PendingReplicationMonitor")
                        .getMethodByName("pendingReplicationCheck"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.blockmanagement.DecommissionManager$Monitor")
                        .getMethodByName("check"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.blockmanagement.HeartbeatManager")
                        .getMethodByName("heartbeatCheck"));

                // entryMethods.add(Scene.v().getMethod("<org.apache.hadoop.hdfs.server.blockmanagement.CacheReplicationMonitor: void rescan()>"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.namenode.LeaseManager")
                        .getMethodByName("checkLeases"));
                // logSync
            }
        }
        else if(component.equals("dn")){
            // SootClass ecls = Scene.v().getSootClass("org.apache.hadoop.hdfs.server.datanode.BPOfferService");
            // entryMethods.add(ecls.getMethodByName("processCommandFromActive"));
            SootClass recvCls = Scene.v().getSootClass("org.apache.hadoop.hdfs.server.datanode.DataXceiver");
            for (SootMethod sm : recvCls.getMethods()) {
                String mname = sm.getName();
                if (mname.equals("readBlock") ||
                        mname.equals("writeBlock") ||
                        mname.equals("replaceBlock") ||
                        mname.equals("copyBlock") ||
                        mname.equals("blockChecksum") ||
                        mname.equals("transferBlock") ||
                        mname.equals("requestShortCircuitFds") ||
                        mname.equals("releaseShortCircuitFds") ||
                        mname.equals("requestShortCircuitShm")) {
                    entryMethods.add(sm);
                }
            }
            entryMethods.add(
                    Scene.v().getMethod("<org.apache.hadoop.hdfs.server.datanode.DirectoryScanner: void reconcile()>"));
        }

        SootMethod protoM = Scene.v().getMethod(
                "<org.apache.hadoop.hdfs.protocolPB.PBHelper: org.apache.hadoop.hdfs.protocol.proto.DatanodeProtocolProtos$DatanodeCommandProto convert(org.apache.hadoop.hdfs.server.protocol.DatanodeCommand)>");
        for (SootMethod toggleM : entryMethods) {
            p("adding to " + toggleM.getName());
            Body b = getBody(toggleM);
            List<Stmt> stmts = CompileUtils.generateStartRecording(toggleM.toString());
            CompileUtils.insertAt(b.getUnits(), stmts, CompileUtils.firstStmt(b), true);
            // if(component.equals("dn") || !toggleM.toString().contains("sendHeartbeat")){
                for (Stmt ret : CompileUtils.getReturnStmts(b)) {
                    stmts = CompileUtils.generateEndRecording();
                    b.getUnits().insertBefore(stmts, ret);
                }
            // } else {
            //     Body pb = getBody(protoM);
            //     for (Stmt ret : CompileUtils.getReturnStmts(pb)) {
            //         stmts = CompileUtils.generateEndRecording();
            //         pb.getUnits().insertBefore(stmts, ret);
            //     }
            //     p("%%"+pb);
            //     protoM.setActiveBody(pb);
            // }
            toggleM.setActiveBody(b);
        }
        if(component.equals("nn")){
            // entryMethods.add(protoM);
        }
        else{
            SootMethod beginM1 = Scene.v()
                    .getMethod("<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.protocol.HeartbeatResponse sendHeartBeat()>");
            Body b = beginM1.getActiveBody();
            CompileUtils.insertAt(b.getUnits(), CompileUtils.generateStartRecording("offerService"),
                    CompileUtils.firstStmt(b), false);
            beginM1.setActiveBody(b);

            SootMethod endM1 = Scene.v()
                    .getMethod("<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: void processQueueMessages()>");
            b = endM1.getActiveBody();
            for (Stmt ret : CompileUtils.getReturnStmts(b)) {
                CompileUtils.insertAt(b.getUnits(), CompileUtils.generateEndRecording(),
                        ret, true);
            }
            endM1.setActiveBody(b);

            SootMethod beginM2 = Scene.v()
                    .getMethod("<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.apache.hadoop.hdfs.protocol.ExtendedBlock popNextSuspectBlock()>");
            b = beginM2.getActiveBody();
            CompileUtils.insertAt(b.getUnits(), CompileUtils.generateStartRecording("VolumeScanner"),
                    CompileUtils.firstStmt(b), false);
            beginM2.setActiveBody(b);

            SootMethod endM2 = Scene.v()
                    .getMethod("<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: long runLoop(org.apache.hadoop.hdfs.protocol.ExtendedBlock)>");
            b = endM2.getActiveBody();
            for (Stmt ret : CompileUtils.getReturnStmts(b)) {
                CompileUtils.insertAt(b.getUnits(), CompileUtils.generateEndRecording(),
                        ret, true);
            }
            endM2.setActiveBody(b);
            entryMethods.add(beginM1);
            entryMethods.add(endM1);
            entryMethods.add(beginM2);
            entryMethods.add(endM2);
            // SootMethod sm2 = Scene.v().getMethod("<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: void run()>");
            // Body b2 = sm2.getActiveBody();
            // Stmt start2 = CompileUtils.searchStmt(b2, "l4 = this", -1);
            // CompileUtils.insertAt(b2.getUnits(), CompileUtils.generateStartRecording(sm2.getName()),
            //         start2, false);
            // Stmt end2 = CompileUtils.searchStmt(b2,
            //         "iter = specialinvoke this.<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: long runLoop(org.apache.hadoop.hdfs.protocol.ExtendedBlock)>(suspectBlock)",
            //         -1);
            // CompileUtils.insertAt(b2.getUnits(), CompileUtils.generateEndRecording(),
            //         end2, false);
            // sm2.setActiveBody(b2);

            // p(sm1.getActiveBody());
            // p(sm2.getActiveBody());
        }

        for (SootMethod sm : entryMethods) {
            entryClasses.add(sm.getDeclaringClass().getName());
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
        // SootMethod forkm = Scene.v().getMethod("<edu.brown.cs.systems.baggage.Baggage: edu.brown.cs.systems.baggage.DetachedBaggage fork()>");
        // boundaryMethods.add(forkm);
        // addAsBoundary("org.apache.hadoop.ipc.Server$ExceptionsHandler");
        // addAsBoundary("edu.brown.cs.systems.baggage.Baggage");
        for (SootMethod bm : boundaryMethods) {
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
        }
    }

    // FIXME: currently we only support instrumenting once;
    // the getBody should ideally cache original body for future instrumentation
    public static Body getBody(SootMethod sm){
            if(!sm.hasActiveBody()){
                sm.retrieveActiveBody();
            }
            return sm.getActiveBody();
    }

    public static String removeQuotes(String s){
        return s.substring(1, s.length()-1);
    }
    public static Body getBody(String s){
        return getBody(Scene.v().getMethod(s));
    }

    public static void lplay() {

        addEntryMethods();

        // addBoundaries();
        loadInstrumentation();
        p("----Analysis Done------");
        analyzeReady = true;
        // [FIXME] We need: 1) value recording for local + snapshot; 2) timestamps
        // readInstsDoop();
    }
    
    // Load all possible instrumentation
    // Local/snapshot
    // For each selected RNode/WNode for inDepth and boundary, match all
    // instrumentations
    public static void loadInstrumentation() {
        String allStr = System.getProperty("AllInst");
        boolean all = allStr != null && allStr.equals("true");
        Set<String> inDepthInsts = new HashSet<>();
        if (!all) {
            String inDepthFile = "/home/jingyuan/neo4j/cypher/indepth.csv";
            List<String> inDepthNodes = CompileUtils.readFrom(inDepthFile);
            inDepthNodes.remove(0);
            for (String s : inDepthNodes) {
                String[] rawItems = s.split("\t");
                String methodAndInst = removeQuotes(rawItems[1]);
                inDepthInsts.add(methodAndInst);
            }

            Map<String, Set<String>> witnessMap = new HashMap<>();
            String WitnessFile = System.getProperty("Witness");
            List<String> Witnesses = CompileUtils.readFrom(WitnessFile);
            for (String s : Witnesses) {
                String[] rawItems = s.split("\t");
                String methodAndInst = rawItems[0];
                String v = rawItems[1];
                String local = v.substring(v.indexOf("/") + 1);
                witnessMap.computeIfAbsent(methodAndInst, e -> new HashSet<>()).add(local);
            }

            // boundaries
            // Locals: just log it
            // Normal fields: just log the left hand of assign
            // [*]: log snapshot of base
            // [CONTENTS]: normal reads and copy-like reads;
            // [FIXME] copy-like: just snapshot; need to mark if this is a copy-like
            // function
            // [FIXME] hashcode
            // a = s.get()/ s.set(a): just log witness; need to specify collection calls and
            // witnesses
            //
            List<String> boundaryNodes = CompileUtils.readFrom("/home/jingyuan/neo4j/cypher/boundary.csv");
            boundaryNodes.remove(0);
            for (String s : boundaryNodes) {
                String[] rawItems = s.split("\t");
                String methodAndInst = removeQuotes(rawItems[0]);
                String v = removeQuotes(rawItems[1]);
                String base = removeQuotes(rawItems[2]);
                String field = removeQuotes(rawItems[3]);

                // p(methodAndInst);
                String method = methodAndInst.substring(0, methodAndInst.indexOf("/"));
                String instId = methodAndInst.substring(methodAndInst.indexOf("/") + 1);
                String local = v.substring(v.indexOf("/") + 1);

                if (instId.contains("fresh-null-assign") || !method.contains("hadoop")) {
                    continue;
                }
                String stmt = translationMap.get(method).get(instId);
                if (stmt == null) {
                    p("!!" + methodAndInst);
                }
                if (field.equals("")) {
                    // local
                    LInst inst = new NondInst(LumosAgent.findMethod(method), stmt, -1, local, "BOUNDARY");
                    activate(inst);
                }
                // else if(!field.contains("[")){
                // // normal field

                // }
                else if (field.contains("*")) {
                    // snapshot
                    String baseLocal = base.substring(base.indexOf("/") + 1);
                    LInst inst = new NondInst(LumosAgent.findMethod(method), stmt, -1, baseLocal, "SNAPSHOT");
                    activate(inst);
                } else {
                    // collections or normal fields
                    if(witnessMap.containsKey(methodAndInst)){
                        for (String w : witnessMap.get(methodAndInst)) {
                            LInst inst = new NondInst(LumosAgent.findMethod(method), stmt, -1, w, "WITNESS");
                            activate(inst);
                        }
                    }
                }
            }
        }

        String ConcurrencyInstFile = System.getProperty("ConcurrencyInst");
        List<String> ConcurrencyInsts = CompileUtils.readFrom(ConcurrencyInstFile);
        for (String s : ConcurrencyInsts) {
            // FIXME: lambda currently unresolved
            if(s.contains("$lambda_")){
                continue;
            }

            String[] rawItems = s.split("\t");
            String methodAndInst = rawItems[0];

            if(!all && !inDepthInsts.contains(methodAndInst)){
                continue;
            }
            String v = rawItems[1];
            String nondType = rawItems[2];

            String method = methodAndInst.substring(0, methodAndInst.indexOf("/"));
            String instId = methodAndInst.substring(methodAndInst.indexOf("/") + 1);
            String local = v.substring(v.indexOf("/") + 1);

            if (instId.contains("fresh-null-assign") || !method.contains("hadoop")) {
                continue;
            }
            if (!LumosAgent.findMethod(method).hasActiveBody()) {
                continue;
            }
            String stmt = translationMap.get(method).get(instId);

            if (stmt == null) {
                p("!!" + methodAndInst);
            }
            LInst inst = new NondInst(LumosAgent.findMethod(method), stmt, -1, local, nondType);
            // if(nondType.contains("WRITE")){
            //     p(inst);
            // }
            activate(inst);
        }


        String ContentInstFile = System.getProperty("ContentInst");
        List<String> ContentInsts = CompileUtils.readFrom(ContentInstFile);
        for (String s : ContentInsts) {
            // FIXME: lambda currently unresolved
            if(s.contains("$lambda_")){
                continue;
            }

            String[] rawItems = s.split("\t");
            // String nondType = rawItems[0];
            String methodAndInst = rawItems[1];

            if(!all && !inDepthInsts.contains(methodAndInst)){
                continue;
            }
            String v = rawItems[2];
            String method = methodAndInst.substring(0, methodAndInst.indexOf("/"));
            String instId = methodAndInst.substring(methodAndInst.indexOf("/") + 1);
            String local = v.substring(v.indexOf("/") + 1);

            if (instId.contains("fresh-null-assign") || !method.contains("hadoop")) {
                continue;
            }

            Map<String, String> mm = translationMap.get(method);
            if(mm == null){
                p(method);
                continue;
            }
            
            String stmt = mm.get(instId);

            if (stmt == null) {
                p("!!" + methodAndInst);
            }
            LInst inst = new NondInst(LumosAgent.findMethod(method), stmt, -1, local, "CONTENT");
            activate(inst);
        }
        if (all) {
            String InputInstFile = System.getProperty("InputInst");
            List<String> InputInsts = CompileUtils.readFrom(InputInstFile);
            for (String s : InputInsts) {
                // FIXME: lambda currently unresolved
                if (s.contains("$lambda_")) {
                    continue;
                }

                String[] rawItems = s.split("\t");
                String methodAndInst = rawItems[0];

                if (!all && !inDepthInsts.contains(methodAndInst)) {
                    continue;
                }
                String v = rawItems[1];
                String method = methodAndInst.substring(0, methodAndInst.indexOf("/"));
                String instId = methodAndInst.substring(methodAndInst.indexOf("/") + 1);
                String local = v.substring(v.indexOf("/") + 1);

                if (instId.contains("fresh-null-assign") || !method.contains("hadoop")) {
                    continue;
                }
                if (!LumosAgent.findMethod(method).hasActiveBody()) {
                    continue;
                }
                String stmt = translationMap.get(method).get(instId);
                LInst inst = new NondInst(LumosAgent.findMethod(method), stmt, -1, local, "INPUT");
                activate(inst);
            }
        }
    }

    //public static void readInstsDoop() {
    //    p("reading inst files...");
    //    // [FIXME] 1. Read All Inst; 2. Select each nond
    //    String P1InstFile = System.getProperty("P1InstFile");
    //    String P2InstFile = System.getProperty("P2InstFile");
    //    List<String> P1Insts = CompileUtils.readFrom(P1InstFile);
    //    List<String> P2Insts = CompileUtils.readFrom(P2InstFile);
    //    Set<String> visitedStmt = new HashSet<>();
    //    for (String s : P2Insts) {
    //        // FIXME: lambda currently unresolved
    //        if(s.contains("$lambda_")){
    //            continue;
    //        }
    //        if(!s.contains("unary-read")){
    //            continue;
    //        }
    //        String[] rawItems = s.split("\t");
    //        String[] items = Arrays.copyOfRange(rawItems, 1, rawItems.length);
    //        String minst = items[0];
    //        if(visitedStmt.contains(minst)){
    //            continue;
    //        }
    //        visitedStmt.add(minst);
    //        //p("%% " + minst);
    //        LInst inst = fromDoopSummary(items, false);
    //        if(inst == null){
    //            continue;
    //        }
    //        activate(inst);
    //    }

    //    for (String s : P1Insts) {
    //        // FIXME: lambda currently unresolved
    //        if(s.contains("$lambda_")){
    //            continue;
    //        }

    //        String[] items = s.split("\t");
    //        String minst = items[0];
    //        if(visitedStmt.contains(minst)){
    //            continue;
    //        }
    //        visitedStmt.add(minst);
    //        LInst inst = fromDoopSummary(items, true);
    //        if(inst == null){
    //            continue;
    //        }
    //        activate(inst);
    //    }

    //}
    //public static LInst fromDoopSummary(String[] items, boolean isP1){
    //    String methodAndInst = items[0];
    //    String method = methodAndInst.substring(0, methodAndInst.indexOf("/"));
    //    String instId = methodAndInst.substring(methodAndInst.indexOf("/") + 1);
    //    String baseValue = items[1];
    //    String witness = items[3];
    //    baseValue = baseValue.substring(baseValue.indexOf("/") + 1);
    //    witness = witness.substring(witness.indexOf("/") + 1);
    //    String tp = items[4];
    //    if(instId.contains("fresh-null-assign") || !method.contains("hadoop")){
    //        return null;
    //    }
    //    if(!LumosAgent.findMethod(method).hasActiveBody()){
    //        return null;
    //    }
    //    String inst = translationMap.get(method).get(instId);
    //    // p("!! " + instId);
    //    // p(method);
    //    // if(inst == null){
    //    //     p("$$ " + inst);
    //    // 
    //    return new PhasedTracingInst(LumosAgent.findMethod(method),
    //            inst, -1, baseValue, witness, tp, methodAndInst, isP1);
    //        //ExperimentInst(LumosAgent.findMethod(method), inst, -1, null, recType);
    //    //return new ValueRecordingInst(LumosAgent.findMethod(method), inst, -1, "vread");
    //}

    //public static LInst fromSummary(String summary){
    //    String[] items = summary.split(LInst.SEPARATOR);
    //    String type = items[0];
    //    SootMethod sm = LumosAgent.findMethod(items[1]);
    //    sm.retrieveActiveBody();
    //    String stmt = items[2];
    //    int lineNum = Integer.valueOf(items[3]);
    //    if(type.equals("concurrency")){
    //        return new ConcurrencyInst(sm, stmt, lineNum, type);
    //    }
    //    else{
    //        return new ValueRecordingInst(sm, stmt, lineNum, type);
    //    }
    //}
    // public static void readInsts() {
    //     p("reading inst files...");
    //     String instFile = System.getProperty("P1InstFile");
    //     String instFile2 = System.getProperty("P2InstFile");
    //     List<String> allInsts = CompileUtils.readFrom(instFile);
    //     SootClass sc = Scene.v().getSootClass("java.time.temporal.TemporalQueries");
    //     SootMethod sm = sc.getMethodByName("<clinit>");

    //     for (String s : allInsts) {
    //         // if(!s.contains("$lambda")){
    //         //     continue;
    //         // }
    //         // FIXME: lambda currently unresolved
    //         if(s.contains("$lambda_")){
    //             continue;
    //         }
    //         LInst inst = fromSummary(s);
    //         activate(inst);
    //     }

    //     String baseInstFile = System.getProperty("baseInstFile");
    //     allInsts = CompileUtils.readFrom(baseInstFile);


    //     for(String s : allInsts){
    //         if(s.contains("$lambda_")){
    //             continue;
    //         }
    //         String[] items = s.split(LInst.SEPARATOR);
    //         SootMethod m = LumosAgent.findMethod(items[1]);
    //         SootClass c = m.getDeclaringClass();
    //         baseInstClasses.add(c);
    //     }
    // }

    public static void activate(LInst inst) {
        if (allInsts.contains(inst)) {
            return;
        }
        String v = ((NondInst)inst).value;
        if(v.contains("$") && v.contains("constant")){
            return;
        }
        if(v.contains("$") && v.contains("null")){
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
            SootClass sclass = sm.getDeclaringClass();
            // if(sclass.getPackageName().contains("java.")){
            //     continue;
            // }
            if (checkSkipped(sclass)) {
                continue;
            }
            // FIXME: this method is too large
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
                            LInst inst = targetInsts.get(i);
                            if (!mode.equals("off")) {
                                inst.instrument(b);
                            }
                        }

                        // if (smstr.contains("convert")) {
                        //     p("## " + smstr);
                        //     p(b + "");
                        // }
                        try {
                            b.validate();
                        } catch (Exception e) {
                            p("!! " + smstr+":");
                            p(b);
                            e.printStackTrace();
                        }
                        sm.setActiveBody(b);
                        end = System.nanoTime();
                        // p("Time=" + (end-start)/1e9+"s");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
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
            addTask(new Runnable() {
                @Override
                public void run() {
                    try{
                        byte[] bytecode = CompileUtils.compileClass(sclass);

                         // if (sclass.getName().contains("LinkedSetIterator")) {
                         //    compile(sclass.getName(), bytecode);
                         // }
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
        String dirname = "/home/jingyuan/debug";
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

    //public static Map<String, byte[]> instrumentOld() {
    //    Map<String, byte[]> cmap = new HashMap<>();
    //    Set<SootClass> scToCompile = new HashSet<>();
    //    for (String smstr : methodTPMap.keySet()) {
    //        SootMethod sm = findMethod(smstr);
    //        //Body b = findBody(sm.toString());
    //        Body b = (Body)getBody(sm).clone();
    //        SootClass sclass = findClass(sm.getDeclaringClass().getName());
    //        List<Stmt> targetstmts = new ArrayList<>();
    //        List<LumosInstrumentation> targetInsts = new ArrayList<>();
    //        PatchingChain<Unit> units = b.getUnits();

    //        // This two-step way is needed to avoid inserted stmts
    //        // from breaking the labeling
    //        for (LumosInstrumentation inst : methodTPMap.get(smstr)) {
    //            inst.setBody(b);
    //            Stmt stmt = inst.getActualStmt();
    //            targetstmts.add(stmt);
    //            targetInsts.add(inst);
    //        }

    //        for (int i = 0; i < targetstmts.size(); i++) {
    //            Stmt stmt = targetstmts.get(i);
    //            LumosInstrumentation inst = targetInsts.get(i);
    //            if (!(inst instanceof TimestampedInstrumentation)) {
    //                List<Stmt> inserts = inst.addInsts();
    //                if (inserts.size() > 0) {
    //                    CompileUtils.insertAt(units, inserts, stmt, inst.isBefore());
    //                }
    //            }
    //        }

    //        for (int i = 0; i < targetstmts.size(); i++) {
    //            Stmt stmt = targetstmts.get(i);
    //            LumosInstrumentation inst = targetInsts.get(i);
    //            if (inst instanceof TimestampedInstrumentation) {
    //                List<Stmt> inserts = inst.addInsts();
    //                if (inserts.size() > 0) {
    //                    CompileUtils.insertAt(units, inserts.get(0),stmt, true);
    //                    inserts.remove(0);
    //                    CompileUtils.insertAt(units, inserts, stmt, false);
    //                    for (Unit uu : units) {
    //                        p(uu + "");
    //                    }
    //                }

    //            }
    //        }
    //        sm.setActiveBody(b);
    //        scToCompile.add(sclass);
    //    }

    //    for (SootClass sclass : scToCompile) {
    //        byte[] bytecode = CompileUtils.compileClass(sclass);
    //        forTest = bytecode;
    //        // if(sclass.)
    //        cmap.put(sclass.toString(), bytecode);

    //    }
    //    return cmap;
    //}

    public static void readJars(String path, List<String> jars){
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].getName().endsWith(".jar")) {
                    jars.add(listOfFiles[i].getAbsolutePath());
                } 
            }
        }
    }
    public static void setupEnv(){
        // FIXME: fix basePath & tracing framework path
        String basePath = System.getenv("LUMOS_HADOOP_DIR");
        String commonPath = basePath + "/hadoop-common-project/hadoop-common/target/classes/";
        String hdfsPath = basePath + "/hadoop-hdfs-project/hadoop-hdfs/target/classes/";
        String commonJarPath = basePath + "/hadoop-dist/target/hadoop-2.7.2/share/hadoop/common/lib/";
        String hdfsJarPath = basePath + "/hadoop-dist/target/hadoop-2.7.2/share/hadoop/hdfs/lib/";
        // String httpfsJarPath = basePath + "/hadoop-dist/target/hadoop-2.7.2/share/hadoop/httpfs/tomcat/lib/";
        String btracePath = System.getenv("LUMOS_TRACING_FRAMEWORK_DIR") + "/tracingplane/client/target/classes/";
        //String testPath = "/home/jingyuan/testpa/my-app/target/classes/";
        List<String> cpaths = new ArrayList<String>();
        List<String> jpaths = new ArrayList<String>();
        List<String> apaths = new ArrayList<String>();
        readJars(commonJarPath, jpaths);
        readJars(hdfsJarPath, jpaths);
        // readJars(httpfsJarPath, jpaths);
        cpaths.add(commonPath);
        cpaths.add(hdfsPath);
        cpaths.add(btracePath);
        // cpaths.add(testPath);
        apaths.addAll(cpaths);
        apaths.add(LumosAgent.tracerJar);
        cpaths.addAll(jpaths);
        cpaths.add(LumosAgent.jrePath);
        LumosAgent.setupSoot(cpaths, apaths);
/*
        SootClass ecls = Scene.v().getSootClass("org.apache.hadoop.hdfs.server.datanode.DataNode");
        p(ecls.getMethods());
        SootMethod sm = ecls.getMethodByName("getStorage");
        sm.retrieveActiveBody();
        p(sm.getActiveBody());
*/
        //LumosAgent.setupClass("hdfs");
        analyzePath();
        readTranslation();
    }

    public static void readTranslation(){
        p("reading translation...");
        try {
            // FIXME: Tmp hack :(
            FileInputStream fis = new FileInputStream(System.getenv("TRANSLATION_MAP_PATH"));
            ObjectInputStream ois = new ObjectInputStream(fis);
            translationMap = (ConcurrentHashMap<String, Map<String, String>>) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("!! " + translationMap.size());
        invTranslationMap = new HashMap<>();
        
        for(String mname : translationMap.keySet()){
            Map<String,String> map = translationMap.get(mname);
            for(String stmtid : map.keySet()){
                String actual = map.get(stmtid);
                invTranslationMap.put(actual, mname+"/"+stmtid);
            }
        }
    }


    public static void main(String args[]) {
        System.out.println("main!!");
        setupEnv();

        SootMethod sm2 = Scene.v().getMethod("<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: void offerService()>");
        p(sm2.getActiveBody());
        SootMethod sm1 = Scene.v().getMethod("<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: void run()>");
        p(sm1.getActiveBody());
        // lplay();
        // Analysis.doAnalysis();
        // addEntryMethods();
        // addBoundaries();
        // instrument();
    }

}
