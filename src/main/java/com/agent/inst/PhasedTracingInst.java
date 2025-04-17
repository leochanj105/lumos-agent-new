package com.agent.inst;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;

import com.agent.LumosAgent;
import com.agent.compile.CompileUtils;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;

public class PhasedTracingInst extends LInst{
    public String witness;
    public String base;
    public boolean isP1;
    public PhasedTracingInst(SootMethod sm, String stmt, int lineNum, String base, String witness, String type, String oid, boolean isP1) {
        super(sm, stmt, lineNum, null, type);
        this.witness = witness;
        this.base = base;
        this.isP1 = isP1;
        this.id = oid + "::" + stmt + "::" + type;
    }

    public PhasedTracingInst(SootMethod sm, String stmt, int lineNum, String base, String witness, String type,
            String oid) {
        this(sm, stmt, lineNum, base, witness, type, oid, true);
    }

    @Override
    public List<Stmt> instrument(Body b) {
        SootClass sysc = LumosAgent.findClassExact("java.lang.System");
        SootMethod hashm = sysc.getMethod("int identityHashCode(java.lang.Object)");
        SootMethod timem = sysc.getMethod("long nanoTime()");
        Local startLocal = CompileUtils.getLocal(b, "startLocal", soot.LongType.v());
        Local endLocal = CompileUtils.getLocal(b, "endLocal", soot.LongType.v());
        Stmt startStmt = CompileUtils.assign(startLocal,CompileUtils.invoke(timem));
        Stmt endStmt = CompileUtils.assign(endLocal,CompileUtils.invoke(timem));
        PatchingChain<Unit> units = b.getUnits();
        List<Stmt> followings = new ArrayList<>();
        Stmt actualStmt = getActualStmt(b);
        if(!(actualStmt instanceof AssignStmt)){
            System.out.println(actualStmt);
        }
        if(actualStmt == null){
            System.out.println("!! Null stmt " + stmt + "\n" + b);
            return null;
        }
        Stmt anchor = actualStmt;

        boolean needReplace = false;
        /*
        if (!sm.isStatic() && base.equals("this") && sm.getName().equals("<init>")) {
            needReplace = true;
        }
        if (needReplace) {
            for (Unit u : units) {
                Stmt nstmt = (Stmt) u;
                if (nstmt.containsInvokeExpr() && nstmt.getInvokeExpr() instanceof SpecialInvokeExpr) {
                    SpecialInvokeExpr iexpr = (SpecialInvokeExpr) nstmt.getInvokeExpr();
                    if (iexpr.getBase().toString().equals("this") && iexpr.getMethod().getName().equals("<init>")) {
                        if(nstmt.getJavaSourceStartLineNumber() > anchor.getJavaSourceStartLineNumber()){
                            anchor = nstmt;
                        }
                    }
                }
            }
        }
        */
        units.insertBefore(startStmt, actualStmt);
        //units.insertAfter(endStmt, actualStmt);
        // if(anchor.equals(actualStmt)){
        //     anchor = endStmt;
        // }
        followings.add(endStmt);
        Value baseV = null, witnessV = null;
        if(!base.equals("[NONE]")){
            baseV = CompileUtils.findLocal(b, base);
            if (baseV == null && base.contains("#")) {
                baseV = CompileUtils.findLocal(b, base.substring(0, base.indexOf("#")));
            }
            if (baseV == null) {
                System.out.println("Can't find " + base + " in " + sm);
            } else {
                followings.addAll(CompileUtils.generatePrimitiveLog(b, endStmt, baseV, id + "::base"));
            }
        }
        if (!witness.equals("[NONE]") || (!isP1)) {
            if(!isP1){
                if (actualStmt instanceof AssignStmt) {
                    witnessV = ((AssignStmt) actualStmt).getLeftOp();
                    //needReplace = ((AssignStmt) actualStmt).getRightOp() instanceof JNewExpr;
                }
                else{
                    System.out.println("P2 left hand variable can't be found: " + actualStmt);
                }
            }
            else {
                witnessV = CompileUtils.findLocal(b, witness);
            }
            if (witnessV == null && witness.contains("#")) {
                // FIXME: temporary hack for unstable variable names containing "#"
                witnessV = CompileUtils.findLocal(b, witness.substring(0, witness.indexOf("#")));
            }
            if (witnessV == null) {
                System.out.println("Can't find " + witness + " in " + sm);
            } else {
                followings.addAll(CompileUtils.generatePrimitiveLog(b, endStmt, witnessV, id + "::witness"));
            }
        }
        followings.addAll(CompileUtils.generatePrimitiveLog(b, endStmt, startLocal, id+"::start"));
        followings.addAll(CompileUtils.generatePrimitiveLog(b, endStmt, endLocal, id+"::end"));
        units.insertAfter(followings, anchor);
        return null;
    }

    @Override
    public String getType() {
        return type;
    }

}
