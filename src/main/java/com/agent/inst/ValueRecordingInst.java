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
import soot.jimple.Stmt;

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
            if (actualStmt instanceof AssignStmt) {
                v = ((AssignStmt) actualStmt).getLeftOp();
            }
            else if(actualStmt instanceof IdentityStmt){
                v = ((IdentityStmt) actualStmt).getLeftOp();
            }
            if(v==null){
                System.out.println("&&"+actualStmt+"\n"+ this.stmt);
                System.out.println(sm.getActiveBody());
            }
            Type t = v.getType();
            Value toRec = null;
            if (CompileUtils.isPrimitive(t) || !(t instanceof RefLikeType)) {
                toRec = v;
            } else {
                SootMethod hashm = sysc.getMethod("int identityHashCode(java.lang.Object)");
                Local intLocal = CompileUtils.getLocal(b, "intLocal", IntType.v());
                Stmt astmt = CompileUtils.assign(intLocal, CompileUtils.invoke(hashm, v));
                stmts.add(astmt);
                toRec = intLocal;
            }
            List<Stmt> logStmt = CompileUtils.generateLog(b, actualStmt, toRec, LumosAgent.logger,
                    this.id + ":" + v);
            stmts.addAll(logStmt);
        } else if (type.equals("invoke")) {
            if(actualStmt == null){
                System.out.println(stmt +"\n"+b);
            }
            InvokeExpr iexpr = actualStmt.getInvokeExpr();
            if (iexpr != null) {
                if (iexpr instanceof InstanceInvokeExpr) {
                    SootClass objc = Scene.v().getSootClass("java.lang.Object");
                    SootMethod getcm = objc.getMethod("java.lang.Class getClass()");
                    Local classLocal = CompileUtils.getLocal(b, "classLocal", getcm.getReturnType());
                    Stmt astmt = CompileUtils.assign(classLocal,
                            CompileUtils.invokeV((Local) ((InstanceInvokeExpr) iexpr).getBase(), getcm));
                    // Stmt astmt = CompileUtils.assign(classLocal, NullConstant.v());
                    stmts.add(astmt);
                    List<Stmt> logStmt = CompileUtils.generateLog(b, actualStmt, classLocal, LumosAgent.logger,
                            this.id + ":CLASS");
                    stmts.addAll(logStmt);
                }

            }
        }
        if (CompileUtils.isParamIdentity(actualStmt)) {
            // if(body == null){System.out.println(sm+"\n"+stmt.hashCode());}
            CompileUtils.insertAt(units, stmts, CompileUtils.firstStmt(b));
        } else {
            CompileUtils.insertAt(units, stmts, actualStmt);
        }
        return stmts;
    }

    public ValueRecordingInst(SootMethod sm, String stmt, int lineNum, String type) {
        super(sm, stmt, lineNum, null, type);
        if(sm.getDeclaringClass().getName().equals("java.lang.Object")){
            System.out.println("!!! " +toSummary());
        }
        // this.type = type;
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
