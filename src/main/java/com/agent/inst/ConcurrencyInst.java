package com.agent.inst;

import java.util.ArrayList;
import java.util.List;

import com.agent.LumosAgent;
import com.agent.compile.CompileUtils;

import soot.Body;
import soot.IntType;
import soot.Local;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.ConcreteRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;

public class ConcurrencyInst extends LInst{

    @Override
    public List<Stmt> instrument(Body b) {
        SootClass sysc = LumosAgent.findClassExact("java.lang.System");

        SootMethod hashm = sysc.getMethod("int identityHashCode(java.lang.Object)");
        SootMethod timem = sysc.getMethod("long nanoTime()");
        AssignStmt assignStmt = (AssignStmt) getActualStmt(b);
        Local startLocal = CompileUtils.getLocal(b, "startLocal", soot.LongType.v());
        Local endLocal = CompileUtils.getLocal(b, "endLocal", soot.LongType.v());
        // Local durationLocal = CompileUtils.getLocal(b, "durationLocal", soot.LongType.v());
        Stmt startStmt = CompileUtils.assign(startLocal,CompileUtils.invoke(timem));
        Stmt endStmt = CompileUtils.assign(endLocal,CompileUtils.invoke(timem));
        // Stmt negStmt = CompileUtils.assign(startLocal, Jimple.v().newNegExpr(startLocal));
        // Stmt diffStmt = CompileUtils.assign(endLocal, CompileUtils.ADD(startLocal, endLocal));
        PatchingChain<Unit> units = b.getUnits();
        if(assignStmt == null){
            System.out.println(assignStmt +"\n"+ stmt+"\n"+b);
            // System.out.println(b);
        }
        units.insertBefore(startStmt, assignStmt);
        List<Stmt> followings = new ArrayList<>();
        followings.add(endStmt);
        // followings.addAll(CompileUtils.generateValueLog(b, endStmt, startLocal, LumosAgent.logger, "s"));
        // followings.addAll(CompileUtils.generateValueLog(b, endStmt, endLocal, LumosAgent.logger, "e"));

        // /*
        Value rop = assignStmt.getRightOp();
        Value lop = assignStmt.getLeftOp();
        Value target = rop;
        String rid = this.id;
        if(lop instanceof ConcreteRef){
            target = lop;
            // rid+=":W";
        }
        else if(rop instanceof ConcreteRef){
            target = rop;
            // rid+=":R";
        }
        else{
            System.out.println(stmt);
            System.out.println(assignStmt);
            throw new RuntimeException("at least one side must be a ConcreteRef!");
        }
        Value toRec = null, index = null, intLocal = null;
        if(target instanceof StaticFieldRef){
            // No need to log base for static references
        }
        else if(target instanceof InstanceFieldRef){
            toRec = ((InstanceFieldRef)target).getBase();
        }
        else if(target instanceof ArrayRef){
            // Need to also log index for arrayref
            toRec = ((ArrayRef)target).getBase();
            index = ((ArrayRef)target).getIndex();
        }
        // if (toRec != null) {
        //     intLocal = CompileUtils.getLocal(b, "intLocal", IntType.v());
        //     Stmt astmt = CompileUtils.assign(intLocal, CompileUtils.invoke(hashm, toRec));
        //     followings.add(astmt);
        //     followings.addAll(CompileUtils.generateValueLog(b, endStmt, intLocal, LumosAgent.logger, rid));
        // }
        // */
        followings.addAll(
                CompileUtils.generateValueLog(b, endStmt, toRec, LumosAgent.logger, rid,
                        startLocal, endLocal, index));
        units.insertAfter(followings, assignStmt);
        return null;
    }

    public ConcurrencyInst(SootMethod sm, String stmt, int lineNum, String type) {
        super(sm, stmt, lineNum, null, type);
    }

    @Override
    public String getType() {
        return "concurrency";
    }


}
