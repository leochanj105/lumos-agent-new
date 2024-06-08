package tracing;

import java.util.List;

import soot.Body;
import soot.jimple.Stmt;

public class LValRecorder implements LumosInstrumentation{
    public String sm;
    public String stmt;
    public String val;
    public int line;
    public String uid;

    public Body body;
    @Override
    public List<Stmt> addInsts() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addInsts'");
    }

    @Override
    public String getStmt() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStmt'");
    }

    @Override
    public String getSm() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSm'");
    }

    @Override
    public boolean isBefore() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBefore'");
    }

    @Override
    public void setBody(Body b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setBody'");
    }

    @Override
    public Body getBody() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBody'");
    }

    @Override
    public Stmt getActualStmt() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getActualStmt'");
    }

    public LValRecorder(String sm, String stmt, String val, String uid, int line) {
        this.sm = sm;
        this.stmt = stmt;
        this.val = val;
        this.uid = uid;
        this.line = line;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sm == null) ? 0 : sm.hashCode());
        result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
        result = prime * result + ((val == null) ? 0 : val.hashCode());
        result = prime * result + line;
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
        LValRecorder other = (LValRecorder) obj;
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
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        return true;
    }

}
