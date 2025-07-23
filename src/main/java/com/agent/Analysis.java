package com.agent;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.UnitGraph;

public class Analysis {
    public static void doAnalysis() {

        Map<Stmt, Set<Stmt>> cfdeps = new HashMap<>();
        Map<Stmt, String> mmap = new HashMap<>();
        for (String mname : LumosAgent.methodMap.keySet()) {
            // p(mname);
            SootMethod sm = LumosAgent.methodMap.get(mname);
            UnitGraph cfg = new BriefUnitGraph(sm.getActiveBody());
            MHGPostDominatorsFinder<Unit> pdf = new MHGPostDominatorsFinder<>(cfg);
            // Map<Stmt, Set<Stmt>> cfDependency;
            Map<Stmt, Set<Stmt>> postDominators = new HashMap<>();
            Set<Stmt> worklist = new HashSet<>();
            // stmt -> cfstmt
            for (Unit u : sm.getActiveBody().getUnits()) {
                Stmt stmt = (Stmt) u;
                mmap.put(stmt, mname);
                worklist.add(stmt);
                cfdeps.put(stmt, new HashSet<>());
            }
            for (Unit u : sm.getActiveBody().getUnits()) {
                Stmt stmt = (Stmt) u;
                postDominators.put(stmt, new HashSet<Stmt>());
                for (Unit du : pdf.getDominators(stmt)) {
                    postDominators.get(stmt).add((Stmt) du);
                }
            }

            for (Unit u : sm.getActiveBody().getUnits()) {
                Stmt stmt = (Stmt) u;
                Set<Stmt> pdsuccs = new HashSet<>();
                for (Unit succ : cfg.getSuccsOf(stmt)) {
                    Stmt succstmt = (Stmt) succ;
                    pdsuccs.addAll(postDominators.get(succstmt));
                }
                for (Stmt dee : pdsuccs) {
                    if (!postDominators.get(stmt).contains(dee)) {
                        cfdeps.get(dee).add(stmt);
                    }
                }
            }

            // cfDependency = cfdeps;
        }
        p(cfdeps.keySet().size()+"");

        // File outputDir = new File("/home/jingyuan/doopstuff/doop/lfacts/deps/nn/");
        // if (!outputDir.exists()) {
        //     outputDir.mkdir();
        // }
        
        File file2 = new File("/home/jingyuan/doopstuff/doop/lfacts/deps/nn/Translation");
        File file = new File("/home/jingyuan/doopstuff/doop/lfacts/deps/nn/ControlDeps");
        PrintWriter  writerfile, writerfile2;
        try {
            writerfile2 = new PrintWriter(file2);
            for(String actual: LumosAgent.invTranslationMap.keySet()){
                String id = LumosAgent.invTranslationMap.get(actual);
                writerfile2.println(actual + '\t' + id);
            }
            writerfile2.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            writerfile = new PrintWriter(file);
            for(Stmt stmt : cfdeps.keySet()){
                for(Stmt cfstmt:cfdeps.get(stmt)){
                    // String stmtStr = LumosAgent.translationMap.get(mmap.get(stmt)).get(stmt.toString());
                    // String cfStmtStr = LumosAgent.translationMap.get(mmap.get(stmt)).get(cfstmt.toString());
                    String stmtStr = LumosAgent.invTranslationMap.get(stmt.toString());
                    String cfstmtStr = LumosAgent.invTranslationMap.get(cfstmt.toString());
                    // p(stmt + "\t" + cfstmt);
                    writerfile.println(stmtStr + '\t' + cfstmtStr);
                }
            }
            writerfile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void p(Object s){
        p(s+"");
    }

    public static void p(String s){
        System.out.println(s);
    }

}
