package com.agent.guard;


import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.agent.LumosAgent;
import com.agent.compile.CompileUtils;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.IntConstant;
import soot.jimple.Stmt;

public abstract class GuardOp {
    public static String verbose = System.getProperty("verbose");
    public static String SEPARATOR = ",,";
    public static AtomicInteger currId = new AtomicInteger(0);
    public String type;
    public SootMethod sm;
    public String stmt;
    public int lineNum;
    public String id;
    @Override
    public String toString(){
        return id;
    }

    public String verbose(){
        return verbose == null ? "" : verbose;
    }

    public List<Stmt> instrument(Body b){
        PatchingChain<Unit> units = b.getUnits();
        Stmt actualStmt = getActualStmt(b);
        SootMethod checkm = Scene.v().getSootClass(LumosAgent.rrClass).getMethodByName("check");

        Local checkResult = CompileUtils.getLocal(b, "checkResult", soot.IntType.v());
        Stmt checkStmt = CompileUtils.assign(checkResult, CompileUtils.invoke(checkm));
        List<Stmt> guardStmts = CompileUtils.generateRRtoggle(b, LumosAgent.getRRField(), false);
        Stmt ifStmt = CompileUtils.IF(CompileUtils.EQ(checkResult, IntConstant.v(1)), actualStmt);
        guardStmts.add(0, ifStmt);
        guardStmts.add(0, checkStmt);
        units.insertBefore(guardStmts, actualStmt);
        return null;
    }

    public Stmt getActualStmt(Body b){
        return CompileUtils.searchStmt(b, stmt, lineNum);
    }
    //public abstract List<Stmt> instrument(Body b);
    public abstract String getType();
}
