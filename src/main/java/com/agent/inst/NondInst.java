
package com.agent.inst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.agent.LumosAgent;
import com.agent.compile.CompileUtils;

import polyglot.ast.Assign;
import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.LengthExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.jimple.TableSwitchStmt;

public class NondInst extends LInst {
    public String value;
    public String nondType;
    public static AtomicLong currId = new AtomicLong(0);
    public long lid;

    public NondInst(SootMethod sm, String stmt, int lineNum, String value, String nondType) {
        super(sm, stmt, lineNum, null, nondType);
        this.value = value;
        if(LumosAgent.verbose.equals("debug")){
            this.id = sm + "::" + stmt + "::" + value + "::" + nondType;
        }else{
            lid = currId.addAndGet(1);
            this.id = lid + "";
        }
    }

    @Override
    public List<Stmt> instrument(Body b) {
        // fucking weird bug, don't know how to fix....
        // basically, in computeReplicationWorkForBlocks, there is a very strange issue
        // where numReplicas is clearly defined but reported as not
        if(value.equals("numReplicas")){
            return null;
        }
        // SootClass sysc = LumosAgent.findClassExact("java.lang.System");
        // SootMethod hashm = sysc.getMethod("int identityHashCode(java.lang.Object)");
        // SootMethod timem = sysc.getMethod("long nanoTime()");
        // Local startLocal = CompileUtils.getLocal(b, "startLocal", soot.LongType.v());
        // Local endLocal = CompileUtils.getLocal(b, "endLocal", soot.LongType.v());
        // Stmt startStmt = CompileUtils.assign(startLocal,CompileUtils.invoke(timem));
        // Stmt endStmt = CompileUtils.assign(endLocal,CompileUtils.invoke(timem));
        PatchingChain<Unit> units = b.getUnits();
        List<Stmt> followings = new ArrayList<>();
        Stmt actualStmt = getActualStmt(b);
        if(actualStmt == null){
            // super hack!
            String hacked = stmt;
            if(stmt.contains("m.")){
                hacked = stmt.replace("m.", "$r0.");
            }
            else if(stmt.contains("s.")){
                hacked = stmt.replace("s.","$r0.");
            }
            else if(stmt.contains("map.")){
                hacked = stmt.replace("map.","$r0.");
            }
            Stmt hackStmt = CompileUtils.searchStmt(b, hacked, -1);
            if (hackStmt == null) {
                System.out.println("!! Null stmt " + stmt + "\n" + b);
                return null;
            }
            actualStmt = hackStmt;
        }
        Stmt anchor = actualStmt;
        if (CompileUtils.isParamIdentity(actualStmt)) {
            anchor = CompileUtils.firstStmt(b);
        } 
        Value baseV = null;
        // if(!base.equals("[NONE]")){
    
        // for now, manually wire lengthof expression
        if (anchor instanceof AssignStmt && ((AssignStmt) anchor).getRightOp() instanceof LengthExpr) {
            baseV = ((AssignStmt) anchor).getLeftOp();
        } else {
            String local = value;
            // System.out.println(type + ",," + local);
            if (type.contains("TRAIN_STORE")) {
                // add basev
                local = value.substring(0, value.indexOf("->"));
                // System.out.println(local);
            }
            // hack
            // if(!anchor.toString().contains(local)){
            //     if (anchor instanceof AssignStmt) {
            //         baseV = ((AssignStmt) anchor).getLeftOp();
            //     } else {
            //         System.out.println("Can't find " + local + " in " + sm);
            //         return null;
            //     }
            // } else {
                baseV = CompileUtils.findLocal(b, local);
                if (baseV == null && local.contains("#")) {
                    baseV = CompileUtils.findLocal(b, local.substring(0, local.indexOf("#")));
                }
                if (baseV == null) {
                    System.out.println("Can't find " + local + " in " + sm);
                    return null;
                }
            // }

        }
        boolean needReplace = false;
        if (!sm.isStatic() && baseV.equals(b.getThisLocal()) && sm.getName().equals("<init>")) {
            needReplace = true;
        }
        if (needReplace) {
            for (Unit u : units) {
                Stmt stmt = (Stmt) u;
                if (stmt.containsInvokeExpr() && stmt.getInvokeExpr() instanceof SpecialInvokeExpr) {
                    SpecialInvokeExpr iexpr = (SpecialInvokeExpr) stmt.getInvokeExpr();
                    if (iexpr.getBase().equals(baseV) && iexpr.getMethod().getName().equals("<init>")) {
                        anchor = stmt;
                    }
                }
            }
        }
        if (LumosAgent.bench.equals("hdfs")) {
            if (LumosAgent.verbose.equals("debug")) {
                followings.addAll(CompileUtils.generateLog(b, anchor, baseV, id));
            } else {
                followings.addAll(CompileUtils.generateLog(b, anchor, baseV, lid));
            }
        } else {
            List<String> refseq = new ArrayList<>();
            // add fields
            if(type.equals("TRAIN_STORE")){
                String[] refs = value.trim().split("->");
                for(int i = 1; i < refs.length; i++){
                    refseq.add(refs[i]);
                }
            }
            if(baseV.toString().contains("tpLocal")){
                System.out.println("^^^ " + anchor+",  " + id);
            }
            followings.addAll(
                    CompileUtils.generateTPStmtsOld(b, baseV, refseq, false, anchor, id));
        }

        if (type.equals("CONTROL") ||
                (anchor instanceof IfStmt) || (anchor instanceof SwitchStmt)) {
            CompileUtils.insertBeforeRedirect(units, followings, anchor);
        } else if (anchor instanceof ReturnStmt) {
            CompileUtils.insertBeforeRedirect(units, followings, anchor);
        } else {
            units.insertAfter(followings, anchor);
        }
        return null;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        NondInst other = (NondInst) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }


}
