package com.agent.inst;

import java.util.ArrayList;
import java.util.List;

import com.agent.LumosAgent;
import com.agent.compile.CompileUtils;

import soot.Body;
import soot.IntType;
import soot.Local;
import soot.PatchingChain;
import soot.RefLikeType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JNewExpr;

public class ValueRecordingInst extends LInst{
    // public String type;
    // public String id;
    @Override
    public List<Stmt> instrument(Body b) {
        SootClass sysc = Scene.v().getSootClass("java.lang.System");
        PatchingChain<Unit> units = b.getUnits();
        List<Stmt> stmts = new ArrayList<>();
        Stmt actualStmt = getActualStmt(b);
        // CompileUtils.setUpRR(b);
        if (type.equals("vread")) {
            // System.out.println(this.id);
            Value v = null;

            boolean isAssignNew = false;
            if (actualStmt instanceof AssignStmt) {
                v = ((AssignStmt) actualStmt).getLeftOp();
                isAssignNew = ((AssignStmt)actualStmt).getRightOp() instanceof JNewExpr;
            }
            else if(actualStmt instanceof IdentityStmt){
                v = ((IdentityStmt) actualStmt).getLeftOp();
            }
            if (v == null) {
                System.out.println("&&" + actualStmt + "\n" + this.stmt);
                System.out.println(sm.getActiveBody());
            }
            String tag = this.id;
            if (verbose().equals("verbose")) {
                tag = "[####READ####]" + tag;
            }
            if(isAssignNew){
                for(Unit u:units){
                    Stmt stmt = (Stmt) u;
                    if(stmt.containsInvokeExpr() && stmt.getInvokeExpr() instanceof SpecialInvokeExpr){
                        SpecialInvokeExpr iexpr = (SpecialInvokeExpr) stmt.getInvokeExpr();
                        if(iexpr.getBase().equals(v) && iexpr.getMethod().getName().equals("<init>")){
                            actualStmt = stmt;
                        }
                    }
                }
            }
            
            List<Stmt> logStmt = CompileUtils.generateValueLog(b, actualStmt, v, LumosAgent.logger,tag);
            stmts.addAll(logStmt);
        } else if (type.equals("invoke")) {
            if(actualStmt == null){
                System.out.println(stmt +"\n"+b);
            }
            InvokeExpr iexpr = actualStmt.getInvokeExpr();
            if (iexpr != null) {
                if (iexpr instanceof InstanceInvokeExpr) {
                    // SootClass objc = Scene.v().getSootClass("java.lang.Object");
                    // SootMethod getcm = objc.getMethod("java.lang.Class getClass()");
                    // Local classLocal = CompileUtils.getLocal(b, "classLocal", getcm.getReturnType());
                    // Stmt astmt = CompileUtils.assign(classLocal,
                    //         CompileUtils.invokeV((Local) ((InstanceInvokeExpr) iexpr).getBase(), getcm));
                    // stmts.add(astmt);
                    
                    // List<Stmt> logStmt1 = CompileUtils.generateClassLog(b, actualStmt,
                    //         ((InstanceInvokeExpr) iexpr).getBase(), LumosAgent.logger, "[====CALL====]" + this.id);
                    // CompileUtils.insertAt(units, logStmt1, actualStmt, true);
                    String tag = this.id;
                    if (verbose().equals("verbose")) {
                        tag = "[====RETURN====]" + tag;
                    }
                    List<Stmt> logStmt = CompileUtils.generateClassLog(b, actualStmt,
                            ((InstanceInvokeExpr) iexpr).getBase(), LumosAgent.logger, tag);
                    stmts.addAll(logStmt);
                }

            }
        }
        if (CompileUtils.isParamIdentity(actualStmt)) {
            CompileUtils.insertAt(units, stmts, CompileUtils.firstStmt(b));
        } else {
            CompileUtils.insertAt(units, stmts, actualStmt);
        }
        return stmts;
    }

    public ValueRecordingInst(SootMethod sm, String stmt, int lineNum, String type) {
        super(sm, stmt, lineNum, null, type);
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

    @Override
    public String getType() {
        return type;
    }

}
