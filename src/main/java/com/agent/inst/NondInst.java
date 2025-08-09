
package com.agent.inst;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

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
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;

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
        }
    }

    @Override
    public List<Stmt> instrument(Body b) {
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
        // if(!(actualStmt instanceof AssignStmt)){
        //     System.out.println(actualStmt);
        // }
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
        // if (LumosAgent.TimeOn) {
        //     units.insertBefore(startStmt, actualStmt);
        //     followings.add(endStmt);
        // }
        if (CompileUtils.isParamIdentity(actualStmt)) {
            anchor = CompileUtils.firstStmt(b);
        } 
        Value baseV = null;
        // if(!base.equals("[NONE]")){
        baseV = CompileUtils.findLocal(b, value);
        if (baseV == null && value.contains("#")) {
            baseV = CompileUtils.findLocal(b, value.substring(0, value.indexOf("#")));
        }
        if (baseV == null) {
            System.out.println("Can't find " + value + " in " + sm);
        } else {
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
            if(LumosAgent.verbose.equals("debug")){
                followings.addAll(CompileUtils.generateLog(b, anchor, baseV, id));
            }
            else{
                followings.addAll(CompileUtils.generateLog(b, anchor, baseV, lid));
            }
        }
        // }
        // if (LumosAgent.TimeOn) {
        //     followings.addAll(CompileUtils.generatePrimitiveLog(b, endStmt, startLocal, id + "::start"));
        //     followings.addAll(CompileUtils.generatePrimitiveLog(b, endStmt, endLocal, id + "::end"));
        // }
        // Stmt firstStmt = null;
        // if (followings.size() > 0) {
        //     firstStmt = followings.get(0);
        // }
        if(type.equals("CONTROL")){
            // units.insertBeforeNoRedirect(followings, anchor);
            CompileUtils.insertBeforeRedirect(units, followings, anchor);
            // units.insertBefore(actualStmt, anchor);
            // CompileUtils.insertAt(units, followings, anchor, true);
            // if(value.contains("childrenList")){
            //     LumosAgent.p("@@ " + followings);
            //     LumosAgent.p(anchor);
            //     units.insertBefore(followings, anchor);
            //     LumosAgent.p(b);
            // }
        }
        else if(anchor instanceof ReturnStmt){
            CompileUtils.insertBeforeRedirect(units, followings, anchor);
            // units.insertBefore(followings, anchor);
        }
        else{
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
