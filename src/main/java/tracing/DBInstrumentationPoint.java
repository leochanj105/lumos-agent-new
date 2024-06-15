package tracing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.SootField;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.toolkits.graph.BriefUnitGraph;
import soot.Local;
import soot.RefType;
import soot.IntType;

import java.util.Objects;

import com.agent.LumosAgent;
import com.agent.compile.CompileUtils;

public class DBInstrumentationPoint implements LumosInstrumentation {
    public String sm;
    public String stmt;
    // public List<Stmt> stmts;
    public Body body;
    public String uid;

    public boolean isInject;
    public String objClassName;

    public DBInstrumentationPoint(String uid, String sm, String stmt, boolean isInject, String objClassName) {
        this.sm = sm;
        this.stmt = stmt;
        this.isInject = isInject;
        this.uid = uid;
        this.objClassName = objClassName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sm == null) ? 0 : sm.hashCode());
        result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DBInstrumentationPoint other = (DBInstrumentationPoint) obj;
        if (sm == null) {
            if (other.sm != null)
                return false;
        } else if (!sm.equals(other.sm))
            return false;
        if (stmt == null) {
            if (other.stmt != null)
                return false;
        } else if (!stmt.equals(other.stmt))
            return false;
        return true;
    }

    @Override
    public List<Stmt> addInsts() {
        List<Stmt> insts = new ArrayList<>();
        Stmt stmt = getActualStmt();
        // String field = "TEST";
        if (!LumosAgent.SOInjectOn) {
            return insts;
        }

        if (stmt.containsInvokeExpr()) {
            InvokeExpr iexpr = stmt.getInvokeExpr();
            if (iexpr instanceof InstanceInvokeExpr) {
                InstanceInvokeExpr inexpr = (InstanceInvokeExpr) iexpr;
                if (isInject()) {
                    if (inexpr.getMethod().toString().contains("save")) {
                        Value obj = inexpr.getArg(0);

                        // if (sf != null) {
                        // insts = CompileUtils.generateDBInjectStmts(body, order, field);
                        insts = CompileUtils.generateDBInjectStmts(body, obj, uid + "");
                        // }

                    }
                } else {
                    if (stmt instanceof AssignStmt) {
                        AssignStmt findStmt = (AssignStmt) stmt;

                        Local obj = (Local) (findStmt).getLeftOp();
                        insts.add(CompileUtils.generateNullCheckStmt(body, stmt, obj));
			
                        Local targetLocal = CompileUtils.getLocal(body, "TL_" + this.objClassName.replace(".","_"), RefType.v(this.objClassName));

                        String resType = findStmt.getLeftOp().getType().toString();
                        if (resType.contains("List")) {

                            Local limit = CompileUtils.getLocal(body, "loopLimit", IntType.v());
                            SootMethod sizeMethod = CompileUtils.getMethod("java.util.List", "int size()");
                            AssignStmt astmt = Jimple.v().newAssignStmt(limit,
                                    Jimple.v().newInterfaceInvokeExpr(obj, sizeMethod.makeRef()));
                            insts.add(astmt);

                            Local loopVar = CompileUtils.getLocal(body, "loopVar", IntType.v());

                            List<Stmt> loopStmts = new ArrayList<>();
                            Local objLocal = CompileUtils.getLocal(body, "objLocal", RefType.v("java.lang.Object"));

                            SootMethod getMethod = CompileUtils.getMethod("java.util.List",
                                    "java.lang.Object get(int)");
                            AssignStmt astmt2 = Jimple.v().newAssignStmt(objLocal,
                                    Jimple.v().newInterfaceInvokeExpr(obj, getMethod.makeRef(), loopVar));
                            loopStmts.add(astmt2);

                            AssignStmt astmt3 = Jimple.v().newAssignStmt(targetLocal,
                                    Jimple.v().newCastExpr(objLocal, targetLocal.getType()));
                            loopStmts.add(astmt3);

                            loopStmts.addAll(getExtractStmts(targetLocal));

                            List<Stmt> actualLoop = CompileUtils.generateLoop(body, loopVar, limit,
                                    (Stmt) body.getUnits().getSuccOf(findStmt), loopStmts);
                            insts.addAll(actualLoop);
                        } else {
                            insts.addAll(getExtractStmts(obj));
                        }

                        System.out.println("---------");
                        insts.forEach(s -> {
                            System.out.println(s);
                        });

                    }
                }
            }
        }
        return insts;
    }

    public List<Stmt> getExtractStmts(Local objLocal) {

        List<Stmt> extracStmts = new ArrayList<>();
        Stmt stmt = getActualStmt();
        // Local tmpMap = CompileUtils.getLocal(body, "tmpMap",
        // RefType.v("java.util.HashMap"));

        // extracStmts.add(CompileUtils.generateNullCheckStmt(body, stmt, objLocal));

        Local tmpMap = CompileUtils.getLocal(body, "ctx", RefType.v("java.lang.String"));
        SootField sf = CompileUtils.findField(((RefType) objLocal.getType()).getSootClass(),
                "LumosContext");

        AssignStmt astmt4 = Jimple.v().newAssignStmt(tmpMap,
                Jimple.v().newInstanceFieldRef(objLocal, sf.makeRef()));
        extracStmts.add(astmt4);

        // SootMethod getODMethod = CompileUtils.getMethod("java.util.HashMap",
        // "java.lang.Object getOrDefault(java.lang.Object,java.lang.Object)");
        // AssignStmt astmt5 = Jimple.v().newAssignStmt(objLocal,
        // Jimple.v().newVirtualInvokeExpr(tmpMap, getODMethod.makeRef(),
        // StringConstant.v(field),
        // StringConstant.v("UNKNOWN")));
        // loopStmts.add(astmt5);

        // List<Stmt> traceStmts = CompileUtils.generateTPStmts(body, objLocal,
        // Collections.emptyList(),
        // false, null, "WRITECONTEXT_" + field);
        List<Stmt> traceStmts = CompileUtils.generateTPStmtsOld(body, tmpMap, Collections.emptyList(),
                false, stmt, uid);
        extracStmts.addAll(traceStmts);
        return extracStmts;
    }

    @Override
    public Stmt getActualStmt() {
        return CompileUtils.searchStmt(getBody(), getStmt(), -1);
    }

    @Override
    public Body getBody() {
        return this.body;
    }

    @Override
    public void setBody(Body b) {
        this.body = b;

    }

    public String getSm() {
        return this.sm;
    }

    public void setSm(String sm) {
        this.sm = sm;
    }

    public String getStmt() {
        return this.stmt;
    }

    public void setStmt(String stmt) {
        this.stmt = stmt;
    }

    public boolean isInject() {
        return isInject;
    }

    public boolean isBefore() {
        return isInject();
    }

}
