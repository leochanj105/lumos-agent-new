package com.agent.compile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import com.agent.LumosAgent;

// import com.lumos.App;
// import com.lumos.forward.ContextSensitiveValue;
// import com.lumos.forward.UniqueName;

// import jas.StringCP;
import soot.Body;
import soot.G;
import soot.Local;
import soot.PackManager;
import soot.PatchingChain;
import soot.Printer;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.Type;
import soot.IntType;

import soot.baf.BafASMBackend;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.IfStmt;
import soot.jimple.GotoStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.jimple.NullConstant;

public class CompileUtils {

    // public static Map<String, Body> bodyMap = new HashMap<>();
    public static int id = 0;

    public static void insertAt(PatchingChain<Unit> units, Stmt target, Stmt toinsert, boolean before) {
        if (before) {
            units.insertBefore(toinsert, target);
        } else {
            units.insertAfter(toinsert, target);
        }
        // body.validate();
    }

    public static void insertAt(PatchingChain<Unit> units, Stmt target, List<Stmt> toinsert, boolean before) {
        if (before) {
            units.insertBefore(toinsert, target);
        } else {
            units.insertAfter(toinsert, target);
        }
        // body.validate();
    }

    // public static List<Stmt> assign
    public static SootMethod getMethod(String className, String methodName) {
        return Scene.v()
                .getSootClass(className)
                .getMethod(methodName);
    }

    public static List<Stmt> generateLoop(Body body, Local loopVar, Value limit, Stmt breakStmt, List<Stmt> loopStmts) {
        List<Stmt> insts = new ArrayList<>();
        AssignStmt initStmt = Jimple.v().newAssignStmt(loopVar, IntConstant.v(0));
        insts.add(initStmt);
        IfStmt condStmt = Jimple.v().newIfStmt(Jimple.v().newGeExpr(loopVar, limit), breakStmt);
        insts.add(condStmt);
        insts.addAll(loopStmts);
        AssignStmt incrStmt = Jimple.v().newAssignStmt(loopVar, Jimple.v().newAddExpr(loopVar, IntConstant.v(1)));
        insts.add(incrStmt);
        GotoStmt gotoStmt = Jimple.v().newGotoStmt(condStmt);
        insts.add(gotoStmt);
        return insts;
    }

    public static List<Stmt> generateInit(Body body, String member) {
        List<Stmt> insts = new ArrayList<>();
        SootClass sclass = body.getMethod().getDeclaringClass();
        SootField sf = null;
        for (SootField f : sclass.getFields()) {
            if (f.getName().equals(member)) {
                sf = f;
                break;
            }
        }
        if (sf == null) {
            System.out.println("Field not found!!");
            return insts;
        }
        Local tmpMember = getLocal(body, "tmpMember_" + member, RefType.v(sf.getType().toString()));

        AssignStmt astmt1 = Jimple.v().newAssignStmt(tmpMember,
                Jimple.v().newNewExpr(RefType.v(sf.getType().toString())));
        insts.add(astmt1);

        // This is now hardcoded!
        SootMethod mapInit = getMethod("java.util.HashMap", "void <init>()");
        InvokeStmt istmt = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(tmpMember, mapInit.makeRef()));
        insts.add(istmt);

        AssignStmt astmt2 = Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(body.getThisLocal(), sf.makeRef()),
                tmpMember);
        insts.add(astmt2);

        // List<Stmt> printStmts = generateTPStmts(body, tmpMember,
        // Collections.emptyList(), true, null, "INIT");
        // insts.addAll(printStmts);
        return insts;
    }

    public static List<Stmt> generateDBExtractStmts(Body body, Value obj) {
        return generateDBExtractStmts(body, obj, null);
    }

    public static List<Stmt> generateDBExtractStmts(Body body, Value obj, String field) {
        List<Stmt> insts = new ArrayList<>();
        // Local tmpMap = getLocal(body, "tmpMap", RefType.v("java.util.HashMap"));
        Local tmpMap = getLocal(body, "tmpMap", RefType.v("java.lang.String"));

        SootClass sclass = LumosAgent.classMap.get(obj.getType().toString());
        SootField sf = null;
        for (SootField f : sclass.getFields()) {
            if (f.getName().contains("LumosContext")) {
                sf = f;
                break;
            }
        }
        if (sf != null) {
            AssignStmt astmt1 = Jimple.v().newAssignStmt(tmpMap, Jimple.v().newInstanceFieldRef(obj, sf.makeRef()));
            insts.add(astmt1);

            // Local tmpContextObj = getLocal(body, "tmpContextObj_" + field,
            // RefType.v("java.lang.Object"));
            // SootMethod getMethod = getMethod("java.util.HashMap", "java.lang.Object
            // get(java.lang.Object)");
            // AssignStmt astmt2 = Jimple.v().newAssignStmt(tmpContextObj,
            // Jimple.v().newVirtualInvokeExpr(tmpMap,
            // getMethod.makeRef(), StringConstant.v(field)));
            // insts.add(astmt2);
        }
        return insts;
    }

    public static List<Stmt> generateDBInjectStmts(Body body, Value obj, String id) {
        return generateDBInjectStmts(body, obj, id, null);
    }

    public static List<Stmt> generateDBInjectStmts(Body body, Value obj, String id, String field) {
        List<Stmt> insts = new ArrayList<>();
        Local spanLocal = findLocal(body, "spanLocal");
        PatchingChain<Unit> units = body.getUnits();
        if (spanLocal == null) {
            spanLocal = getLocal(body, "spanLocal",
                    RefType.v("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span"));
            SootMethod currMethod = Scene.v()
                    .getSootClass("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span")
                    .getMethod("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span current()");
            units.insertBefore(
                    Jimple.v().newAssignStmt(spanLocal, Jimple.v().newStaticInvokeExpr(currMethod.makeRef())),
                    ((JimpleBody) body).getFirstNonIdentityStmt());

        }

        Local tmpString1 = getLocal(body, "tmpString1", RefType.v("java.lang.String"));
        Local tmpString2 = getLocal(body, "tmpString2", RefType.v("java.lang.String"));

        Local tmpSpctx = getLocal(body, "tmpSpctx",
                RefType.v("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.SpanContext"));

        // Local tmpMap = CompileUtils.getLocal(body, "tmpMap",
        // RefType.v("java.util.HashMap"));
        // Local tmpMap = CompileUtils.getLocal(body, "tmpMap",
        // RefType.v("java.lang.String"));

        SootMethod getSpanMethod = getMethod("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span",
                "io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.SpanContext getSpanContext()");
        AssignStmt astmt1 = Jimple.v().newAssignStmt(tmpSpctx,
                Jimple.v().newInterfaceInvokeExpr(spanLocal, getSpanMethod.makeRef()));
        insts.add(astmt1);

        SootMethod getTidMethod = getMethod("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.SpanContext",
                "java.lang.String getTraceId()");
        AssignStmt astmt2 = Jimple.v().newAssignStmt(tmpString1,
                Jimple.v().newInterfaceInvokeExpr(tmpSpctx, getTidMethod.makeRef()));
        insts.add(astmt2);

        // String id = "000";

        AssignStmt stmt3 = Jimple.v().newAssignStmt(tmpString2,
                StringConstant.v("_" + id + "_000"));
        insts.add(stmt3);
        SootMethod concatMethod = getMethod("java.lang.String", "java.lang.String concat(java.lang.String)");
        AssignStmt stmt4 = Jimple.v().newAssignStmt(tmpString1, Jimple.v().newVirtualInvokeExpr(tmpString1,
                concatMethod.makeRef(), tmpString2));
        insts.add(stmt4);

        SootClass sclass = LumosAgent.classMap.get(obj.getType().toString());
        SootField sf = null;
        for (SootField f : sclass.getFields()) {
            if (f.getName().contains("LumosContext")) {
                sf = f;
                break;
            }
        }
        if (sf != null) {
            // AssignStmt astmt3 = Jimple.v().newAssignStmt(tmpMap,
            // Jimple.v().newInstanceFieldRef(obj, sf.makeRef()));
            // insts.add(astmt3);
            // SootMethod mapSet = getMethod("java.util.HashMap",
            // "java.lang.Object put(java.lang.Object,java.lang.Object)");
            // InvokeStmt setStmt =
            // Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tmpMap,
            // mapSet.makeRef(),
            // StringConstant.v(field), tmpString1));
            AssignStmt setStmt = Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(obj, sf.makeRef()),
                    tmpString1);
            insts.add(setStmt);

        } else {
            System.out.println("[Lumos] Failed: LumosContext not found");
        }
        for (Stmt stmt : insts) {
            System.out.println(stmt);
        }
        return insts;
    }

    public static List<Stmt> generateTPStmts(Body body, Value v, List<String> suffix, boolean isPrint, Stmt stori,
            String name) {
        Local tpLocal = findLocal(body, "tpLocal");
        PatchingChain<Unit> units = body.getUnits();
        if (tpLocal == null) {
            // tpLocal = Jimple.v().newLocal("tpLocal", RefType.v("java.io.PrintStream"));
            if (isPrint) {
                tpLocal = getLocal(body, "tpLocal", RefType.v("java.io.PrintStream"));
                units.insertBefore(Jimple.v().newAssignStmt(
                        tpLocal, Jimple.v().newStaticFieldRef(
                                Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),
                        ((JimpleBody) body).getFirstNonIdentityStmt());
            } else {
                tpLocal = getLocal(body, "tpLocal",
                        RefType.v("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span"));
                SootMethod currMethod = getMethod("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span",
                        "io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span current()");
                units.insertBefore(
                        Jimple.v().newAssignStmt(tpLocal, Jimple.v().newStaticInvokeExpr(currMethod.makeRef())),
                        ((JimpleBody) body).getFirstNonIdentityStmt());
            }
        }

        Local tmpString1 = getLocal(body, "tmpString1", RefType.v("java.lang.String"));
        Local tmpString2 = getLocal(body, "tmpString2", RefType.v("java.lang.String"));

        List<Stmt> stlist = new ArrayList<>();

        Value val = null;
        Value baseval = v;
        if (suffix.isEmpty()) {
            if (baseval instanceof Constant) {
                val = baseval;
            } else if (baseval instanceof JInstanceFieldRef) {
                JInstanceFieldRef bref = (JInstanceFieldRef) baseval;
                Local tmp = Jimple.v().newLocal("tpfield" + (id++), RefType.v(bref.getField().getType().toString()));
                body.getLocals().add(tmp);
                // locallist.add(tmp);
                Stmt st = Jimple.v().newAssignStmt(tmp, bref);
                stlist.add(st);
                // App.p(st);
                // curr = tmp;
                val = tmp;
            } else {
                // App.p(baseval instanceof Constant);
                val = findLocal(body, baseval);
            }
        } else {
            // App.p(baseval);
            Value curr = findLocal(body, baseval);
            List<Local> locallist = new ArrayList<>();

            for (String ref : suffix) {
                SootClass sc = LumosAgent.classMap.get(curr.getType().toString());
                if (ref.isEmpty())
                    continue;
                String actual = ref.trim();
                SootField sf = null;
                // System.out.println("!!! " + curr +", " + (curr == null ? "#" :
                // curr.getType().toString()));
                for (SootField f : sc.getFields()) {
                    if (f.getName().contains(actual)) {
                        sf = f;
                        break;
                    }
                }
                SootMethod getter = null;
                if (sf.isPrivate() && !curr.toString().equals("this")) {
                    for (SootMethod method : sc.getMethods()) {
                        String fname = sf.getName();
                        String prefix = sf.getType().toString().equals("boolean") ? "is" : "get";
                        String cand = prefix + fname.substring(0, 1).toUpperCase() + fname.substring(1);
                        if (method.getName().equals(cand)) {
                            getter = method;
                            break;
                        }
                    }
                    // App.p(getter);
                }

                Local actualBase = null;

                if (!(curr instanceof Local)) {
                    Local tmp = Jimple.v().newLocal("tpfield" + (id++), curr.getType());
                    body.getLocals().add(tmp);
                    Stmt st = Jimple.v().newAssignStmt(tmp, curr);
                    stlist.add(st);

                    actualBase = tmp;
                } else {
                    actualBase = (Local) curr;
                }

                stlist.add(generateNullCheckStmt(body, stori, baseval));

                Local tmp = Jimple.v().newLocal("tpfield" + (id++), sf.getType());
                body.getLocals().add(tmp);
                if (sf.isPrivate() && !curr.toString().equals("this")) {
                    Stmt st = Jimple.v().newAssignStmt(tmp,
                            Jimple.v().newVirtualInvokeExpr(actualBase, getter.makeRef()));
                    stlist.add(st);
                } else {
                    Stmt st = Jimple.v().newAssignStmt(tmp, Jimple.v().newInstanceFieldRef(actualBase, sf.makeRef()));
                    stlist.add(st);
                }
                curr = tmp;
            }
            val = curr;
            // for(int i = 0; i < un.getSuffix())
        }

        AssignStmt stmt = Jimple.v().newAssignStmt(tmpString1,
                StringConstant.v("[" + name + "] " + combine(v, suffix) + "="));
        stlist.add(stmt);
        // Value actualVal = null;
        // if(cv.g)
        // cv.getValue();

        SootMethod toStringMethod = getValueOfMethod(val);
        stmt = Jimple.v().newAssignStmt(tmpString2, Jimple.v().newStaticInvokeExpr(toStringMethod.makeRef(), val));
        stlist.add(stmt);
        SootMethod concatMethod = getMethod("java.lang.String", "java.lang.String concat(java.lang.String)");
        stmt = Jimple.v().newAssignStmt(tmpString1, Jimple.v().newVirtualInvokeExpr(tmpString1,
                concatMethod.makeRef(), tmpString2));
        stlist.add(stmt);

        if (isPrint) {
            SootMethod toCall = getMethod("java.io.PrintStream", "void println(java.lang.String)");
            InvokeStmt printStmt = Jimple.v()
                    .newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tpLocal, toCall.makeRef(),
                            tmpString1));
            stlist.add(printStmt);
        } else {
            SootMethod toCall = getMethod("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span",
                    "io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span addEvent(java.lang.String)");
            InvokeStmt eventStmt = Jimple.v()
                    .newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(tpLocal, toCall.makeRef(), tmpString1));
            stlist.add(eventStmt);
        }
        return stlist;
    }

    public static Stmt generateNullCheckStmt(Body body, Stmt stori, Value obj) {
        Stmt orinext = (Stmt) (new BriefUnitGraph(body)).getSuccsOf(stori).get(0);
        Stmt branchnull = Jimple.v().newIfStmt(Jimple.v().newEqExpr(obj, NullConstant.v()), orinext);
        return branchnull;
    }

    public static SootField findField(SootClass sclass, String fieldName) {
        SootField sf = null;
        for (SootField f : sclass.getFields()) {
            if (f.getName().contains(fieldName)) {
                sf = f;
                break;
            }
        }
        return sf;
    }

    public static Local findLocal(Body b, String vname) {
        for (Local l : b.getLocals()) {
            if (l.getName().equals(vname)) {
                return l;
            }
        }
        return null;
    }

    public static Value findLocal(Stmt stmt, String local) {
        for (ValueBox vb : stmt.getUseAndDefBoxes()) {
            Value v = vb.getValue();
            if (v.toString().equals(local)) {
                return v;
            }
        }
        return null;
    }

    public static Local getLocal(Body body, String name, Type type) {
        Local local = findLocal(body, name);
        if (local == null) {
            local = Jimple.v().newLocal(name, type);
            body.getLocals().add(local);
        }
        return local;
    }

    public static Stmt searchStmt(Body b, String stmtStr, int linenum) {

        for (Unit unit : b.getUnits()) {
            Stmt stmt = (Stmt) unit;
            boolean stmtMatched = stmt.toString().equals(stmtStr);
            boolean lineMatch = linenum == -1 || (linenum == stmt.getJavaSourceStartLineNumber());
            // }
            if (stmtMatched && lineMatch) {
                return stmt;
            }
        }
        if (stmtStr.contains("if") && stmtStr.contains("(branch)")) {
            String alternative = stmtStr.substring(0, stmtStr.indexOf("(branch)"));
            for (Unit unit : b.getUnits()) {
                Stmt stmt = (Stmt) unit;
                boolean stmtMatched = stmt.toString().contains(alternative);
                boolean lineMatch = linenum == -1 || (linenum == stmt.getJavaSourceStartLineNumber());
                if (stmtMatched && lineMatch) {
                    return stmt;
                }
            }
        }
        return null;
    }

    public static String combine(Value base, List<String> suf) {
        String res = base.toString();
        for (String str : suf) {
            res += "." + str;
        }
        return res;

    }

    public static Local findLocal(Body b, Value v) {
        for (Local l : b.getLocals()) {
            if (l.getName().equals(((Local) v).getName())) {
                return l;
            }
        }
        return null;
    }

    private static SootMethod getValueOfMethod(Value value) {
        SootMethod toCall;
        if (value.getType().toString().equals("int")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(int)");
        } else if (value.getType().toString().equals("byte")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(int)");
        } else if (value.getType().toString().equals("float")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(float)");
        } else if (value.getType().toString().equals("double")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(double)");
        } else if (value.getType().toString().equals("boolean")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(boolean)");
        } else if (value.getType().toString().equals("char")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(char)");
        } else if (value.getType().toString().equals("char[]")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(char[])");
        } else if (value.getType().toString().equals("long")) {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(long)");
        } else {
            toCall = Scene.v().getSootClass("java.lang.String").getMethod("java.lang.String valueOf(java.lang.Object)");
        }
        return toCall;
    }

    public static byte[] compileClass(SootClass cl) {
        System.out.println("compiling " + cl);
        ByteArrayOutputStream bstream = new ByteArrayOutputStream(4096);
        try {
            BafASMBackend backend = new BafASMBackend(cl, 52);
            backend.generateClassFile(bstream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bstream.toByteArray();
    }

    public static void outputJimple(SootClass cl, String analysisPath) {

        System.out.println("compiling " + cl);

        File outputDir = new File(analysisPath);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        File file = new File(outputDir + File.separator + cl.getName() + ".jimple");
        PrintWriter writer, writerfile;
        try {
            writerfile = new PrintWriter(file);
            ByteArrayOutputStream bstream = new ByteArrayOutputStream(8192);
            writer = new PrintWriter(bstream, true);
            Printer.v().printTo(cl, writerfile);
            writerfile.close();

            BafASMBackend backend = new BafASMBackend(cl, 52);
            File file2 = new File(outputDir + File.separator + cl.getName() + ".class");
            FileOutputStream classout = new FileOutputStream(file2);
            backend.generateClassFile(classout);
            classout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
