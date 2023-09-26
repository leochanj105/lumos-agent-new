package tracing;

import java.util.List;

import soot.Body;
import soot.jimple.Stmt;

public interface LumosInstrumentation {

    public List<Stmt> addInsts();

    public String getStmt();

    public String getSm();

    public boolean isBefore();

    public void setBody(Body b);

    public Body getBody();

    public Stmt getActualStmt();
}
