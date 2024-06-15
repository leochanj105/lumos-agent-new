package com.agent.inst;

import java.util.List;

import com.agent.LumosAgent;

import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;

public class ConcurrencyInst extends LInst{

    @Override
    public List<Stmt> instrument(Body b) {
        SootClass sysc = LumosAgent.findClassExact("java.lang.System");

        SootMethod hashm = sysc.getMethod("int identityHashCode(java.lang.Object)");

        SootMethod timem = sysc.getMethod("long nanoTime()");
        return null;
    }

    public ConcurrencyInst(SootMethod sm, String stmt, int lineNum) {
        super(sm, stmt, lineNum, null);
    }

    @Override
    public String getType() {
        return "concurrency";
    }


}
