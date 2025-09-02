package com.agent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

// import edu.brown.cs.systems.dynamicinstrumentation.JVMAgent;
import edu.brown.cs.systems.dynamicinstrumentation.DynamicManager;
import edu.brown.cs.systems.dynamicinstrumentation.DynamicModification;
import edu.brown.cs.systems.dynamicinstrumentation.JVMAgent;

public class AgentThread implements Runnable, MessageHandler {
    public Instrumentation inst;
    public JVMAgent agent;
    public DynamicManager manager;
    public WebSocketClient client;
    public String sname = System.getProperty("sname");

    public Map<String, DynamicModification> tpmap;
    // public ClassLoader loader;
    // public static final String CLASSNAME = "com.test.App";
    // public static final String METHODNAME = "yell";
    // public static final String TESTTP = "System.out.println(\"Ha!\");";
    public static final String WITHSPAN = "io.opentelemetry.instrumentation.annotations.WithSpan";
    public static final String CLASSNAME = "travel.service.TravelServiceImpl";
    public static final String METHODNAME = "query";
    public static final String TESTTP = "io.opentelemetry.api.trace.Span.current().addEvent(\"[LUMOS] HELLO!!!!!!!!\");";
    public static final int TESTLINE = 158;
    public static LumosClassLoader lambdaLoader = new LumosClassLoader();
    public AgentThread(Instrumentation inst) {
        this.inst = inst;
        this.agent = new JVMAgent(inst);
        this.manager = new DynamicManager(this.agent);
        tpmap = new HashMap<>();
    }

    public void connect(String controllerAddr) {
        try {
            this.client = new WebSocketClient(new URI(controllerAddr), this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.client.send(sname);
    }

    public void refreshInsts() {
        System.out.println("[LUMOS] Instrumenting...");
        long start = System.currentTimeMillis();
        Map<String, byte[]> cmap = LumosAgent.instrument();
        // System.out.println(cmap.keySet());
        if (cmap.keySet().size() > 0) {
            reload(cmap);
        }
        long instrumentDuration = System.currentTimeMillis() - start;
        System.out.println("Instrumentation time: " + instrumentDuration);

    }
    public void setORMInjectOn(boolean b) {
        LumosAgent.p("ORM: " + b);
        LumosAgent.SOInjectOn = b;
        // refreshTPs();
    }

    public void setTPInstOn(boolean b) {
        LumosAgent.p("TP: " + b);
        LumosAgent.TPInstOn = b;
        // refreshTPs();
    }

    public void reload(Map<String, byte[]> cmap) {
        System.out.println("reloading");
        for(String s:cmap.keySet()){
            Map<String, byte[]> nmap = new HashMap<>();
            nmap.put(s,cmap.get(s));
            // if(s.equals("org.apache.hadoop.hdfs.util.CyclicIteration")){
            //     continue;
            // }
            ClassLoader actualLoader = LumosAgent.cloader;
            if(s.contains("$lambda_")){
                actualLoader = lambdaLoader;
                lambdaLoader.setByteCode(s, cmap.get(s));
            }
            LumosAgent.p("reloading " + s);
            try {
                // this.agent.reload(cmap);
                this.agent.reload(nmap);
            } 
            catch (UnmodifiableClassException e) {
                System.out.println("?! " + s);
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("&& " + s);
                e.printStackTrace();
            }
            catch (Error e) {
                System.out.println("## " + s);
                e.printStackTrace();
            }
            LumosAgent.p(s + " reloaded");
        }
        // System.out.println("reloaded");
    }

    public void handleMessage(String message) {
        try {
            // handleJSON(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void countDown(int seconds) {
        for (int i = 0; i < seconds; i++) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("[LUMOS] Count " + (seconds - i));
        }
    }

    // public void playground(int seconds) {
    //     // int seconds = 30;
    //     countDown(seconds);
    //     System.out.println("[LUMOS] Instrumenting...");
    //     SootClass sclass = LumosAgent.findClassExact("order.service.OrderServiceImpl");
    //     List<TracePoint> tps = new ArrayList<>();
    //     SootMethod sm = LumosAgent.findMethod("create", "order.service.OrderServiceImpl");
    //     System.out.println(sm);
    //     Body b = (Body)LumosAgent.getBody(sm).clone();
    //     List<Stmt> saveStmts = new ArrayList<>();
    //     List<Stmt> savedObjs = new ArrayList<>();
    //     for (Unit u : b.getUnits()) {
    //         Stmt stmt = (Stmt) u;
    //         if (stmt.containsInvokeExpr()) {
    //             if (stmt.getInvokeExpr().getMethod().toString().contains("save")) {
    //                 Value objSaved = stmt.getInvokeExpr().getArgs().get(0);
    //                 System.out.println(stmt.getJavaSourceStartLineNumber() + ":" + stmt + ", " + objSaved);
    //                 DBInstrumentationPoint dbinst = new DBInstrumentationPoint("7", sm.toString(), stmt.toString(),
    //                         true,
    //                         "order.domain.Order");
    //                 LumosAgent.addTP(dbinst);
    //             }
    //         }
    //     }

        // SootMethod sm2 = LumosAgent.findMethod("queryOrders", "order.service.OrderServiceImpl");
        // Body b2 = sm2.getActiveBody();
        // Stmt findStmt = null;
        // for (Unit u : b2.getUnits()) {
        //     Stmt stmt = (Stmt) u;
        //     if (stmt.containsInvokeExpr()) {
        //         if (stmt.getInvokeExpr().getMethod().toString().contains("findByAccountId")) {
        //             // findStmt = stmt;
        //             // break;
        //             DBInstrumentationPoint dbinst = new DBInstrumentationPoint("1", sm2.toString(), stmt.toString(),
        //                     false, "order.domain.Order");
        //             LumosAgent.addTP(dbinst);
        //             TimestampedInstrumentation tinst = new TimestampedInstrumentation("9", sm2.toString(),
        //                     stmt.toString());
        //             LumosAgent.addTP(tinst);
        //         }
        //     }
        // }
        // refreshTPs();
    // }

    @Override
    public void run() {
        System.out.println("Agent thread started");
        // Wait Until we hooked the Spring classloader
        while (LumosAgent.cloader == null) {
            sleep(500);
        }

        System.out.println("Agent ready");
        System.out.println(System.getProperty("java.version"));

        // register appClassLoader loaded tracer with LumosGlobal
        ClassLoader appLoader = LumosAgent.cloader;
        Class<?> tracerClass;
        if (LumosAgent.bench.equals("hdfs")) {
            try {
                tracerClass = Class.forName("com.lumos.tracer.LumosRegister", true, appLoader);
                System.out.println(tracerClass);
                Method rm = tracerClass.getMethod("registerTracer");
                rm.invoke(null);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        // if(LumosAgent.cloader!=null) return;
        LumosAgent.setupEnv();
        LumosAgent.lplay();
        refreshInsts();

        // Connect to websocket controller server
        // connect("ws://lumos:8765");

        // while(!LumosAgent.playGroundFlag);
        // A test of adding a tracepoint, then remove it...
        loop(1000000);
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    public static void loop(int ms) {
        while (true) {
            sleep(ms);
        }
    }
}
