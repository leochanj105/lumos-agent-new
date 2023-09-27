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
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
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

    public boolean isInject;

    public DBInstrumentationPoint(String sm, String stmt, boolean isInject) {
        this.sm = sm;
        this.stmt = stmt;
        this.isInject = isInject;
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
        String field = "TEST";

        if (stmt.containsInvokeExpr()) {
            InvokeExpr iexpr = stmt.getInvokeExpr();
            if (iexpr instanceof InstanceInvokeExpr) {
                InstanceInvokeExpr inexpr = (InstanceInvokeExpr) iexpr;
                if(isInject()){
                    if (inexpr.getMethod().toString().contains("save")) {
                        Value order = inexpr.getArg(0);
                        
                        // if (sf != null) {
                            insts = CompileUtils.generateDBInjectStmts(body, order, field);
                        // }

                    }
                }
                else {
                    if(stmt instanceof AssignStmt){
                        AssignStmt findStmt = (AssignStmt) stmt;
                        
                        Local objList = (Local)(findStmt).getLeftOp();
                        
                        Local limit = CompileUtils.getLocal(body, "loopLimit", IntType.v());
                        SootMethod sizeMethod = CompileUtils.getMethod("java.util.ArrayList", "int size()");
                        AssignStmt astmt = Jimple.v().newAssignStmt(limit, Jimple.v().newVirtualInvokeExpr(objList, sizeMethod.makeRef()));
                        insts.add(astmt);

                        Local loopVar = CompileUtils.getLocal(body, "loopVar", IntType.v());
                        
                        List<Stmt> loopStmts = new ArrayList<>();
                        Local objLocal = CompileUtils.getLocal(body, "objLocal", RefType.v("java.lang.Object"));
                        Local orderLocal = CompileUtils.getLocal(body, "orderLocal", RefType.v("order.domain.Order"));

                        SootMethod getMethod = CompileUtils.getMethod("java.util.ArrayList", "java.lang.Object get(int)");                        
                        AssignStmt astmt2 = Jimple.v().newAssignStmt(objLocal, Jimple.v().newVirtualInvokeExpr(objList, getMethod.makeRef(), loopVar));
                        loopStmts.add(astmt2);

                        AssignStmt astmt3 = Jimple.v().newAssignStmt(orderLocal, Jimple.v().newCastExpr(objLocal, orderLocal.getType()));
                        loopStmts.add(astmt3);
                        

                        Local tmpMap = CompileUtils.getLocal(body, "tmpMap", RefType.v("java.util.HashMap"));
                        SootField sf = CompileUtils.findField(((RefType)orderLocal.getType()).getSootClass(), "LumosContext");
                        
                        AssignStmt astmt4 = Jimple.v().newAssignStmt(tmpMap, Jimple.v().newInstanceFieldRef(orderLocal, sf.makeRef()));
                        loopStmts.add(astmt4);

                        SootMethod getODMethod = CompileUtils.getMethod("java.util.HashMap", "java.lang.Object getOrDefault(java.lang.Object,java.lang.Object)");
                        AssignStmt astmt5 = Jimple.v().newAssignStmt(objLocal, 
                            Jimple.v().newVirtualInvokeExpr(tmpMap, getODMethod.makeRef(), StringConstant.v(field), StringConstant.v("UNKNOWN")));
                        loopStmts.add(astmt5);
                        
                        List<Stmt> traceStmts = CompileUtils.generateTPStmts(body, objLocal, Collections.emptyList(), false, null, "WRITECONTEXT_"+field);
                        loopStmts.addAll(traceStmts);

                        List<Stmt> actualLoop = CompileUtils.generateLoop(body, loopVar, limit, (Stmt)body.getUnits().getSuccOf(findStmt), loopStmts);
                        insts.addAll(actualLoop);
                        System.out.println("---------");
                        insts.forEach(s ->{
                            System.out.println(s);
                        });

                        
                    }
                }
            }
        }
        return insts;
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

    public boolean isInject(){
        return isInject;
    }

    public boolean isBefore() {
        return isInject();
    }

}