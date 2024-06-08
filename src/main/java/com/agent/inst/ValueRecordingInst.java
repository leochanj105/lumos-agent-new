package com.agent.inst;

import com.agent.LumosAgent;
import com.agent.compile.CompileUtils;

import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;

public class ValueRecordingInst extends LInst{
    public String type;
    @Override
    public void instrument(Body b) {
        SootClass sysc = LumosAgent.findClassExact("java.lang.System");
        if(stmt instanceof AssignStmt){
            Value v = ((AssignStmt) stmt).getLeftOp();
            Type t = v.getType();
            if(CompileUtils.isPrimitive(t)){

            }
            else{
                SootMethod hashm = sysc.getMethod("int identityHashCode(java.lang.Object)");
            }
        }
        InvokeExpr iexpr = stmt.getInvokeExpr();
        if(iexpr != null){
            if(iexpr instanceof VirtualInvokeExpr ||
                    iexpr instanceof InterfaceInvokeExpr ||
                    iexpr instanceof SpecialInvokeExpr){
                // SootMethod timer = sysc.getMethod("long nanoTime()");
                SootClass objc = LumosAgent.findClassExact("java.lang.Object");
                SootMethod getcm = objc.getMethod("java.lang.Class getClass()");
            }
        }
    }


    public ValueRecordingInst(SootMethod sm, Stmt stmt, int lineNum, Value mayRecord, String type) {
        super(sm, stmt, lineNum, mayRecord);
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ValueRecordingInst other = (ValueRecordingInst) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
