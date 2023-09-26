package tracing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import soot.Body;
import soot.SootClass;
import soot.SootField;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;

import java.util.Objects;

import com.agent.LumosAgent;
import com.agent.compile.CompileUtils;

public class DBInstrumentationPoint implements LumosInstrumentation {
    public String sm;
    public String stmt;
    // public List<Stmt> stmts;
    public Body body;

    public DBInstrumentationPoint(String sm, String stmt) {
        this.sm = sm;
        this.stmt = stmt;
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
        if (stmt.containsInvokeExpr()) {
            InvokeExpr iexpr = stmt.getInvokeExpr();
            if (iexpr instanceof InstanceInvokeExpr) {
                InstanceInvokeExpr inexpr = (InstanceInvokeExpr) iexpr;
                if (inexpr.getMethod().toString().contains("save")) {
                    Value order = inexpr.getArg(0);
                    
                    // if (sf != null) {
                    insts = CompileUtils.generateDBStmts(body, order);
                    // }

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

    public boolean isBefore() {
        return true;
    }

}