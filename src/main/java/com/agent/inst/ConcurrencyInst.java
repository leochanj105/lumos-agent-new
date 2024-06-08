package com.agent.inst;

import com.agent.LumosAgent;

import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;

public class ConcurrencyInst extends LInst{

    @Override
    public void instrument(Body b) {
        SootClass sysc = LumosAgent.findClassExact("java.lang.System");

        SootMethod hashm = sysc.getMethod("int identityHashCode(java.lang.Object)");

        SootMethod timem = sysc.getMethod("long nanoTime()");
    }

    public ConcurrencyInst(SootMethod sm, Stmt stmt, int lineNum, Value mayRecord) {
        super(sm, stmt, lineNum, mayRecord);
    }


}
