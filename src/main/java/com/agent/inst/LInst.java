package com.agent.inst;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.agent.LumosAgent;
import com.agent.compile.CompileUtils;

import soot.Body;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;

public abstract class LInst{
    public static String SEPARATOR = ",,";
    public static AtomicInteger currId = new AtomicInteger(0);
    public String type;
    public SootMethod sm;
    public String stmt;
    public int lineNum;
    public Value mayRecord;
    // public Body body; 
    // public String id;
    public String id;
    // public List<Stmt> instrument(){
    //     return this.instrument(this.body);
    // }
    public abstract List<Stmt> instrument(Body b);
    // public abstract boolean isBefore();
    
    public LInst(SootMethod sm, String stmt, int lineNum, Value mayRecord, String type) {
        this.sm = sm;
        this.stmt = stmt;
        this.lineNum = lineNum;
        this.mayRecord = mayRecord;
        this.type = type;
        // this.id = sm.getDeclaringClass().getShortName() + ":" + sm.getName() + ":" + lineNum;
        this.id = currId.getAndIncrement()+"";
    }
    public abstract String getType();
    public String toSummary(){
        return getType() + SEPARATOR + sm.getSignature() + SEPARATOR + stmt + SEPARATOR + lineNum;
    }

    public Stmt getActualStmt(Body b){
        return CompileUtils.searchStmt(b, stmt, lineNum);
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sm == null) ? 0 : sm.hashCode());
        result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
        result = prime * result + lineNum;
        result = prime * result + ((mayRecord == null) ? 0 : mayRecord.hashCode());
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
        LInst other = (LInst) obj;
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
        if (lineNum != other.lineNum)
            return false;
        if (mayRecord == null) {
            if (other.mayRecord != null)
                return false;
        } else if (!mayRecord.equals(other.mayRecord))
            return false;
        return true;
    }
}
