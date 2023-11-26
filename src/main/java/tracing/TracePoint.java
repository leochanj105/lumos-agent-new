package tracing;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.agent.LumosAgent;
import com.agent.compile.CompileUtils;

import soot.Body;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;

public class TracePoint implements LumosInstrumentation {
    public String sm;
    public String stmt;
    public String val;
    public int line;
    public List<String> suffix;
    public String uid;

    public Body body;

    public TracePoint(String uid, String sm, String stmt, int line, String val, List<String> suffix) {
        this.uid = uid;
        this.sm = sm;
        this.stmt = stmt;
        this.line = line;
        this.val = val;
        this.suffix = suffix;
    }

    public TracePoint(String uid, String sm, String stmt, int line, String val) {
        this(uid, sm, stmt, line, val, Collections.emptyList());
    }

    @Override
    public String toString() {
        return this.sm + ": " + this.stmt + " ==> " + this.val + "." + this.suffix;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sm == null) ? 0 : sm.hashCode());
        result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
        result = prime * result + ((val == null) ? 0 : val.hashCode());
        result = prime * result + line;
        result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
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
        TracePoint other = (TracePoint) obj;
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
        if (val == null) {
            if (other.val != null)
                return false;
        } else if (!val.equals(other.val))
            return false;
        if (line != other.line)
            return false;
        if (suffix == null) {
            if (other.suffix != null)
                return false;
        } else if (!suffix.equals(other.suffix))
            return false;
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        return true;
    }

    public String getSm() {
        return sm;
    }

    public void setSm(String sm) {
        this.sm = sm;
    }

    public String getStmt() {
        return stmt;
    }

    public void setStmt(String stmt) {
        this.stmt = stmt;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public List<String> getSuffix() {
        return suffix;
    }

    public void setSuffix(List<String> suffix) {
        this.suffix = suffix;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public Body getBody() {
        return this.body;
    }

    @Override
    public boolean isBefore() {
        Stmt stmt = getActualStmt();
        return stmt instanceof JIfStmt || stmt instanceof JReturnStmt
                || stmt instanceof JReturnVoidStmt ||
                stmt instanceof JGotoStmt;
    }

    @Override
    public void setBody(Body b) {
        this.body = b;
    }

    @Override
    public List<Stmt> addInsts() {
        if (!LumosAgent.TPInstOn) {
            List<Stmt> empty = new ArrayList<>();
            return empty;
        }
        Body b = getBody();
        Stmt stmt = getActualStmt();
        Value base = CompileUtils.findLocal(stmt, getVal());
        List<String> refs = getSuffix().stream().filter(x -> !x.isEmpty()).collect(Collectors.toList());
        List<Stmt> inserts = CompileUtils.generateTPStmts(b, base, refs, false, stmt, getUid());
        return inserts;
    }

    @Override
    public Stmt getActualStmt() {
        return CompileUtils.searchStmt(getBody(), getStmt(), -1);
    }

}
