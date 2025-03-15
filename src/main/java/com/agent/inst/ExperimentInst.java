package com.agent.inst;

import java.util.ArrayList;
import java.util.List;

import com.agent.compile.CompileUtils;

import soot.Body;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;

public class ExperimentInst extends LInst{
    public String recType;
    public ExperimentInst(SootMethod sm, String stmt, int lineNum, Value mayRecord, String recType) {
        super(sm, stmt, lineNum, mayRecord, "Experiment");
        this.recType = recType;
    }

    @Override
    public List<Stmt> instrument(Body b) {
        PatchingChain<Unit> units = b.getUnits();
        List<Stmt> stmts = new ArrayList<>();
        Stmt actualStmt = getActualStmt(b);
        List<Stmt> logStmt = CompileUtils.generateUpdateStat(b, actualStmt, recType, this.sm+": " + this.stmt);
        stmts.addAll(logStmt);
        if (CompileUtils.isParamIdentity(actualStmt)) {
            CompileUtils.insertAt(units, stmts, CompileUtils.firstStmt(b));
        } else {
            CompileUtils.insertAt(units, stmts, actualStmt);
        }
        return stmts;
    }

    @Override
    public String getType() {
        return "Experiment";
    }
}

