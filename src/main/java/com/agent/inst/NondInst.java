
package com.agent.inst;

import java.util.ArrayList;
import java.util.List;


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
import soot.jimple.Stmt;

public class NondInst extends LInst {
    public String value;
    public String nondType;
    public NondInst(SootMethod sm, String stmt, int lineNum, String value, String nondType) {
        super(sm, stmt, lineNum, null, nondType);
        this.value = value;
        this.id = sm + "::" + stmt + "::" + value + "::" + nondType;
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
        if(!(actualStmt instanceof AssignStmt)){
            System.out.println(actualStmt);
        }
        if(actualStmt == null){
            System.out.println("!! Null stmt " + stmt + "\n" + b);
            return null;
        }
        Stmt anchor = actualStmt;
        // if (LumosAgent.TimeOn) {
        //     units.insertBefore(startStmt, actualStmt);
        //     followings.add(endStmt);
        // }
        Value baseV = null;
        // if(!base.equals("[NONE]")){
        baseV = CompileUtils.findLocal(b, value);
        if (baseV == null && value.contains("#")) {
            baseV = CompileUtils.findLocal(b, value.substring(0, value.indexOf("#")));
        }
        if (baseV == null) {
            System.out.println("Can't find " + value + " in " + sm);
        } else {
            followings.addAll(CompileUtils.generatePrimitiveLog(b, anchor, baseV, id));
        }
        // }
        // if (LumosAgent.TimeOn) {
        //     followings.addAll(CompileUtils.generatePrimitiveLog(b, endStmt, startLocal, id + "::start"));
        //     followings.addAll(CompileUtils.generatePrimitiveLog(b, endStmt, endLocal, id + "::end"));
        // }
        units.insertAfter(followings, anchor);
        return null;
    }

    @Override
    public String getType() {
        return type;
    }

}
