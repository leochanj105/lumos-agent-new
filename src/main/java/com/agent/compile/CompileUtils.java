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
import soot.baf.BafASMBackend;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.InvokeStmt;
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

    public static List<Stmt> generateTPStmts(Body body, Value v, List<String> suffix, boolean isPrint, Stmt stori) {
        Local tpLocal = getLocal(body, "tpLocal");
        PatchingChain<Unit> units = body.getUnits();
        if (tpLocal == null) {
            // tpLocal = Jimple.v().newLocal("tpLocal", RefType.v("java.io.PrintStream"));
            if (isPrint) {
                tpLocal = Jimple.v().newLocal("tpLocal", RefType.v("java.io.PrintStream"));
                body.getLocals().add(tpLocal);
                units.insertBefore(Jimple.v().newAssignStmt(
                        tpLocal, Jimple.v().newStaticFieldRef(
                                Scene.v().getField("<java.lang.System: java.io.PrintStreamout>").makeRef())),
                        ((JimpleBody) body).getFirstNonIdentityStmt());
            } else {
                tpLocal = Jimple.v().newLocal("tpLocal",
                        RefType.v("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span"));
                body.getLocals().add(tpLocal);
                SootMethod currMethod = Scene.v()
                        .getSootClass("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span")
                        .getMethod("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span current()");
                units.insertBefore(
                        Jimple.v().newAssignStmt(tpLocal, Jimple.v().newStaticInvokeExpr(currMethod.makeRef())),
                        ((JimpleBody) body).getFirstNonIdentityStmt());
            }
        }

        Local tmpString1 = getLocal(body, "tmpString1");
        if (tmpString1 == null) {
            tmpString1 = Jimple.v().newLocal("tmpString1", RefType.v("java.lang.String"));
            body.getLocals().add(tmpString1);
            // body.validate();
        }
        Local tmpString2 = getLocal(body, "tmpString2");
        if (tmpString2 == null) {
            tmpString2 = Jimple.v().newLocal("tmpString2", RefType.v("java.lang.String"));
            body.getLocals().add(tmpString2);
            // body.validate();
        }
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
                val = getLocal(body, baseval);
            }
        } else {
            // App.p(baseval);
            Value curr = getLocal(body, baseval);
            List<Local> locallist = new ArrayList<>();
            
            for (String ref : suffix) {
                SootClass sc = LumosAgent.classMap.get(curr.getType().toString());
                if (ref.isEmpty())
                    continue;
                String actual = ref.trim();
                SootField sf = null;
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
                Stmt orinext = (Stmt) (new BriefUnitGraph(body)).getSuccsOf(stori).get(0);
                Stmt branchnull = Jimple.v().newIfStmt(Jimple.v().newEqExpr(actualBase, NullConstant.v()), orinext);
                stlist.add(branchnull);

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

        AssignStmt stmt = Jimple.v().newAssignStmt(tmpString1, StringConstant.v(combine(v, suffix) + "="));
        stlist.add(stmt);
        // Value actualVal = null;
        // if(cv.g)
        // cv.getValue();

        SootMethod toStringMethod = getValueOfMethod(val);
        stmt = Jimple.v().newAssignStmt(tmpString2, Jimple.v().newStaticInvokeExpr(toStringMethod.makeRef(), val));
        stlist.add(stmt);
        SootMethod concatMethod = Scene.v().getSootClass("java.lang.String")
                .getMethod("java.lang.String concat(java.lang.String)");
        stmt = Jimple.v().newAssignStmt(tmpString1, Jimple.v().newVirtualInvokeExpr(tmpString1,
                concatMethod.makeRef(), tmpString2));
        stlist.add(stmt);

        if (isPrint) {
            SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream")
                    .getMethod("void println(java.lang.String)");
            InvokeStmt printStmt = Jimple.v()
                    .newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tpLocal, toCall.makeRef(),
                            tmpString1));
            stlist.add(printStmt);
        } else {
            SootMethod toCall = Scene.v()
                    .getSootClass("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span")
                    .getMethod(
                            "io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span addEvent(java.lang.String)");
            InvokeStmt eventStmt = Jimple.v()
                    .newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(tpLocal, toCall.makeRef(), tmpString1));
            stlist.add(eventStmt);
        }
        return stlist;
    }

    public static Local getLocal(Body b, String vname) {
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

    // public static Stmt findStmt(Body b, String str) {
    // for (Unit u : b.getUnits()) {
    // Stmt stmt = (Stmt) u;
    // if (stmt.toString().equals(str)) {
    // return stmt;
    // }
    // }
    // return null;
    // }

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
        return null;
    }

    public static String combine(Value base, List<String> suf) {
        String res = base.toString();
        for (String str : suf) {
            res += "." + str;
        }
        return res;

    }

    public static Local getLocal(Body b, Value v) {
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
