package tracing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.agent.LumosAgent;
import com.agent.compile.CompileUtils;

import polyglot.ast.Assign;
import soot.Body;
import soot.Local;
import soot.LongType;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.jimple.Stmt;

public class TimestampedInstrumentation implements LumosInstrumentation {
    public String sm;
    public String stmt;
    public Body body;
    public String uid;

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
        TimestampedInstrumentation other = (TimestampedInstrumentation) obj;
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

    public TimestampedInstrumentation(String uid, String sm, String stmt) {
        this.sm = sm;
        this.stmt = stmt;
        this.uid = uid;
    }

    @Override
    public List<Stmt> addInsts() {
        List<Stmt> insts = new ArrayList<>();
        // Stmt stmt = getActualStmt();
        if (!LumosAgent.SOInjectOn) {
            return insts;
        }

        Local tp1 = CompileUtils.getLocal(body, "tpt1", LongType.v());
        Local tp2 = CompileUtils.getLocal(body, "tpt2", LongType.v());
        SootMethod timeMethod = CompileUtils.getMethod("java.lang.System",
                "long nanoTime()");
        AssignStmt stmt1 = Jimple.v().newAssignStmt(tp1, Jimple.v().newStaticInvokeExpr(timeMethod.makeRef()));
        insts.add(stmt1);
        AssignStmt stmt2 = Jimple.v().newAssignStmt(tp2, Jimple.v().newStaticInvokeExpr(timeMethod.makeRef()));
        insts.add(stmt2);
        List<Stmt> traceStmts1 = CompileUtils.generateTPStmtsOld(body, tp1, Collections.emptyList(),
                false, null, uid + "_0");
        insts.addAll(traceStmts1);
        List<Stmt> traceStmts2 = CompileUtils.generateTPStmtsOld(body, tp2, Collections.emptyList(),
                false, null, uid + "_1");
        insts.addAll(traceStmts2);
        System.out.println("---------");
        insts.forEach(s -> {
            System.out.println(s);
        });
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

    public boolean isBefore() {
        return true;
    }
}
