package com.agent;

import java.lang.instrument.Instrumentation;
import java.lang.Thread;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import tracing.TracePoint;

import com.agent.compile.CompileUtils;

import java.lang.reflect.Field;
import java.lang.ClassLoader;
import java.util.Vector;
import java.net.URI;
import java.sql.Time;

import com.google.common.collect.Lists;
// import edu.brown.cs.systems.dynamicinstrumentation.JVMAgent;
import edu.brown.cs.systems.dynamicinstrumentation.*;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.ConstPool;
import org.json.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import tracing.DBInstrumentationPoint;
import tracing.LumosInstrumentation;
import tracing.TimestampedInstrumentation;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.Value;
import soot.Body;
import soot.ValueBox;
import soot.IntType;
import soot.Local;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;

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

    public void handleJSON(String jstr) {
        boolean changed = false;
        if (jstr.equals("SharedOn")) {
            setORMInjectOn(true);
            return;
        } else if (jstr.equals("SharedOff")) {
            setORMInjectOn(false);
            return;
        } else if (jstr.equals("TPOn")) {
            setTPInstOn(true);
            return;
        } else if (jstr.equals("TPOff")) {
            setTPInstOn(false);
            return;
        }
        JSONObject obj = new JSONObject(jstr);
        String x = obj.getString("type");
        if (x.equals("add")) {
            JSONArray arr = obj.getJSONArray("tps");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject tp = arr.getJSONObject(i);
                String id = tp.getString("id");
                String tptype = tp.getString("tptype");
                String method = tp.getString("method");
                String stmt = tp.getString("stmt");
                int line = tp.getInt("line");
                LumosInstrumentation toadd = null;
                if (tptype.equals("code")) {
                    String value = tp.getString("value");
                    List<String> suffix = new ArrayList<>();
                    JSONArray suffixarray = tp.getJSONArray("suffix");
                    for (int j = 0; j < suffixarray.length(); j++) {
                        suffix.add(suffixarray.getString(j));
                    }
                    toadd = new TracePoint(id, method, stmt, line, value, suffix);
                } else if (tptype.equals("soread")) {
                    String sotype = tp.getString("sotype");
                    String soclass = tp.getString("soclass");
                    if (sotype.contains("Repository")) {
                        toadd = new DBInstrumentationPoint(id, method, stmt, false, soclass);
                    } else if (sotype.contains("ValueOperations") || sotype.contains("ClassShared")
                            || sotype.contains("Atomic")) {
                        toadd = new TimestampedInstrumentation(id, method, stmt);
                    } else {
                        LumosAgent.p("[WARN] SO type not supported for read: " + sotype);
                    }
                } else if (tptype.equals("sowrite")) {
                    String sotype = tp.getString("sotype");
                    if (sotype.contains("Repository")) {
                        toadd = new DBInstrumentationPoint(id, method, stmt, true, "");
                    } else if (sotype.contains("ValueOperations") || sotype.contains("ClassShared")
                            || sotype.contains("Atomic")) {
                        toadd = new TimestampedInstrumentation(id, method, stmt);
                    } else {
                        LumosAgent.p("[WARN] SO type not supported for write: " + sotype);
                    }

                } else {
                    LumosAgent.p("[WARN] TP type not supported!!!!");
                }
                if (toadd != null) {
                    boolean result = LumosAgent.addTP(toadd);
                    if (result) {
                        changed = true;
                    }
                }
            }

        } else if (x.equals("remove")) {
            // System.out.println("removing temporarily not implemented");
            JSONArray arr = obj.getJSONArray("tps");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject tp = arr.getJSONObject(i);
                String id = tp.getString("id");

            }
        } else if (x.equals("field")) {
            String classname = obj.getString("classname");
            String type = obj.getString("type");
            String fieldname = obj.getString("fieldname");
            addField(classname, type, fieldname);
            // changed = true;

        } else {
            System.out.println("Not implemented!");
        }
        if (changed) {
            refreshTPs();
        }
    }

    public void addField(String classname, String type, String fieldname) {
        Map<String, byte[]> cmap = LumosAgent.addField(classname, type, fieldname);
        System.out.println(cmap.keySet());
        reload(cmap);
    }

    public void refreshTPs() {
        System.out.println("[LUMOS] Instrumenting...");
        Map<String, byte[]> cmap = LumosAgent.instrument();
        System.out.println(cmap.keySet());
        // cmap.put(LumosAgent.testclass, LumosAgent.forTest);
        // LumosAgent.p(LumosAgent.forTest.length + "");
        if (cmap.keySet().size() > 0) {
            reload(cmap);
        }

    }

    public void setORMInjectOn(boolean b) {
        LumosAgent.p("ORM: " + b);
        LumosAgent.SOInjectOn = b;
        refreshTPs();
    }

    public void setTPInstOn(boolean b) {
        LumosAgent.p("TP: " + b);
        LumosAgent.TPInstOn = b;
        refreshTPs();
    }

    public void reload(Map<String, byte[]> cmap) {
        try {
            this.agent.reload(cmap);
        } catch (UnmodifiableClassException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LumosAgent.p("Instrumented");
    }

    public void handleMessage(String message) {
        // System.out.println("Handling " + message);
        // if(message.equals("keepalive"))
        // return;
        try {
            handleJSON(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
         * String[] traceArgs = message.split(",", 0);
         * System.out.println(traceArgs);
         * String classname = traceArgs[0];
         * String methodname = traceArgs[1];
         * String code = traceArgs[2];
         * int ln = Integer.parseInt(traceArgs[3]);
         * //DynamicModification mod = new AnnotationModification(classname, methodname,
         * WITHSPAN);
         * DynamicModification mod = new InstructionModification(classname, methodname,
         * code, ln);
         * try{
         * this.manager.add(mod);
         * this.manager.install();
         * }
         * catch(Exception e){
         * e.printStackTrace();
         * }
         * System.out.println("Instrumented!");
         */
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

    public void playground(int seconds) {
        // int seconds = 30;
        countDown(seconds);
        System.out.println("[LUMOS] Instrumenting...");
        SootClass sclass = LumosAgent.findClassExact("order.service.OrderServiceImpl");
        List<TracePoint> tps = new ArrayList<>();
        SootMethod sm = LumosAgent.findMethod("create", "order.service.OrderServiceImpl");
        System.out.println(sm);
        Body b = LumosAgent.findBody(sm.toString());
        List<Stmt> saveStmts = new ArrayList<>();
        List<Stmt> savedObjs = new ArrayList<>();
        for (Unit u : b.getUnits()) {
            Stmt stmt = (Stmt) u;
            if (stmt.containsInvokeExpr()) {
                if (stmt.getInvokeExpr().getMethod().toString().contains("save")) {
                    Value objSaved = stmt.getInvokeExpr().getArgs().get(0);
                    System.out.println(stmt.getJavaSourceStartLineNumber() + ":" + stmt + ", " + objSaved);
                    DBInstrumentationPoint dbinst = new DBInstrumentationPoint("7", sm.toString(), stmt.toString(),
                            true,
                            "order.domain.Order");
                    LumosAgent.addTP(dbinst);
                }
            }
        }

        SootMethod sm2 = LumosAgent.findMethod("queryOrders", "order.service.OrderServiceImpl");
        Body b2 = sm2.getActiveBody();
        Stmt findStmt = null;
        for (Unit u : b2.getUnits()) {
            Stmt stmt = (Stmt) u;
            if (stmt.containsInvokeExpr()) {
                if (stmt.getInvokeExpr().getMethod().toString().contains("findByAccountId")) {
                    // findStmt = stmt;
                    // break;
                    DBInstrumentationPoint dbinst = new DBInstrumentationPoint("1", sm2.toString(), stmt.toString(),
                            false, "order.domain.Order");
                    LumosAgent.addTP(dbinst);
                    TimestampedInstrumentation tinst = new TimestampedInstrumentation("9", sm2.toString(),
                            stmt.toString());
                    LumosAgent.addTP(tinst);
                }
            }
        }
        refreshTPs();
    }

    @Override
    public void run() {
        // Wait Until we hooked the Spring classloader
        while (LumosAgent.cloader == null || !LumosAgent.analyzeReady)
            ;

        // int seconds = 10;
        // for (int i = 0; i < seconds; i++) {
        // try {
        // Thread.sleep(1000);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // System.out.println("[LUMOS] Count " + (seconds - i));
        // }

        // Connect to websocket controller server
        connect("ws://lumos:8765");

        // while(!LumosAgent.playGroundFlag);
        // A test of adding a tracepoint, then remove it...
        if (sname.contains("ts-order-service")) {
            // playground(5);
        }
        /*
         * 
         * 
         * //DynamicModification modifyMethod = new InstructionModification(CLASSNAME,
         * METHODNAME, TESTTP, TESTLINE);
         * DynamicModification modifyMethod = new AnnotationModification(CLASSNAME,
         * METHODNAME, WITHSPAN);
         * 
         * try{
         * this.manager.add(modifyMethod);
         * this.manager.install();
         * System.out.println("[LUMOS] Instrumented!");
         * //this.client.close();
         * }
         * catch(Exception e){
         * e.printStackTrace();
         * }
         */
        /**
         * seconds = 60;
         * for(int i = 0; i < seconds; i++){
         * try{
         * Thread.sleep(1000);
         * }
         * catch(Exception e){
         * }
         * System.out.println("[LUMOS] Count " + (seconds - i));
         * }
         * 
         * System.out.println("[LUMOS] Uninstrumenting...");
         * try{
         * this.manager.remove(modifyMethod);
         * this.manager.install();
         * System.out.println("[LUMOS] Uninstrumented!");
         * 
         * 
         * }
         * catch(Exception e){
         * e.printStackTrace();
         * }
         **/
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
        }
    }
}
