package com.agent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agent.compile.CompileUtils;
import com.agent.inst.LInst;
import com.agent.inst.NondInst;

public class InstLoader{

    public static String queryResPath = System.getenv("QUERY_RES_PATH");
    public static String nondFactPath = System.getenv("NONDFACT_PATH");
    public static String ConcurrencyInstFile = System.getenv("CONCURRENCY_NOND");
    public static String ContentInstFile = System.getenv("CONTENT_NOND");
    public static String InputInstFile =System.getenv("INPUT_NOND");
    static{
        if(LumosAgent.bench.equals("train")){
            queryResPath = "/app";
            nondFactPath = "/app";
            ConcurrencyInstFile = "/app/ConcurrencyNondRecord.csv";
            ContentInstFile = "/app/LNondContentReadInstruction.csv";
            InputInstFile = "/app/LNondInputVar.csv";
        }
        else{
            // String ConcurrencyInstFile = System.getenv("CONCURRENCY_NOND");
        }
    }
    public static String removeQuotes(String s){
        return s.substring(1, s.length()-1);
    }
    // Load all possible instrumentation
    // Local/snapshot
    // For each selected RNode/WNode for inDepth and boundary, match all
    // instrumentations
    public static void loadInstrumentation() {
        String allStr = System.getenv("AllInst");
        boolean all = allStr != null && allStr.equals("true");
        Set<String> inDepthInsts = new HashSet<>();
        System.out.println("all: " + all);
        if (!all) {
            String inDepthFile = queryResPath+"/indepth.csv";
            List<String> inDepthNodes = CompileUtils.readFrom(inDepthFile);
            inDepthNodes.remove(0);
            for (String s : inDepthNodes) {
                String[] rawItems = s.split("\t");
                String methodAndInst = removeQuotes(rawItems[1]);
                String instComp = removeQuotes(rawItems[5]);
                if (LumosAgent.component.equals(instComp)) {
                    inDepthInsts.add(methodAndInst);
                }
            }

            Map<String, Set<String>> witnessMap = new HashMap<>();
            String witnessFile = System.getenv("WITNESS");
                //System.getProperty("Witness");
            List<String> Witnesses = CompileUtils.readFrom(witnessFile);
            for (String s : Witnesses) {
                String[] rawItems = s.split("\t");
                String methodAndInst = rawItems[0];
                String v = rawItems[1];
                String local = v.substring(v.indexOf("/") + 1);
                witnessMap.computeIfAbsent(methodAndInst, e -> new HashSet<>()).add(local);
            }


            String cfFile = queryResPath+"/IndepthControlVar.csv";
            List<String> cfinsts = CompileUtils.readFrom(cfFile);
            for (String s : cfinsts) {
                String[] rawItems = s.split("\t");
                String methodAndInst = rawItems[0];
                String v = rawItems[1];
                String instComp = rawItems[2];
                if (!LumosAgent.component.equals(instComp)) {
                    continue;
                }

                String method = methodAndInst.substring(0, methodAndInst.indexOf("/"));
                if(method.contains("http.")){
                    continue;
                }
                String instId = methodAndInst.substring(methodAndInst.indexOf("/") + 1);
                String local = v.substring(v.indexOf("/") + 1);

                if (instId.contains("fresh-null-assign") || !method.contains("hadoop")) {
                    continue;
                }
                String stmt = LumosAgent.translationMap.get(method).get(instId);
                if (stmt != null) {
                    LInst inst = new NondInst(LumosAgent.findMethod(method), stmt, -1, local, "CONTROL");
                    LumosAgent.activate(inst);
                }
            }

            // boundaries
            // Locals: just log it
            // Normal fields: just log the left hand of assign
            // [*]: log snapshot of base
            // [CONTENTS]: normal reads and copy-like reads;
            // [FIXME] copy-like: just snapshot; need to mark if this is a copy-like
            // function
            // [FIXME] hashcode
            // a = s.get()/ s.set(a): just log witness; need to specify collection calls and
            // witnesses
            //
            List<String> boundaryNodes = CompileUtils.readFrom(queryResPath+"/boundary.csv");
            boundaryNodes.remove(0);
            for (String s : boundaryNodes) {
                String[] rawItems = s.split("\t");
                String methodAndInst = removeQuotes(rawItems[0]);
                String v = removeQuotes(rawItems[1]);
                String base = removeQuotes(rawItems[2]);
                String field = removeQuotes(rawItems[3]);

                String instComp = removeQuotes(rawItems[4]);
                if (!LumosAgent.component.equals(instComp)) {
                    continue;
                }
                // p(methodAndInst);
                String method = methodAndInst.substring(0, methodAndInst.indexOf("/"));
                String instId = methodAndInst.substring(methodAndInst.indexOf("/") + 1);
                String local = v.substring(v.indexOf("/") + 1);

                if (instId.contains("fresh-null-assign") || !method.contains("hadoop")) {
                    continue;
                }
                String stmt = LumosAgent.translationMap.get(method).get(instId);
                if (stmt == null) {
                    p("!!" + methodAndInst);
                }
                if (field.equals("")) {
                    // local
                    LInst inst = new NondInst(LumosAgent.findMethod(method), stmt, -1, local, "BOUNDARY");
                    LumosAgent.activate(inst);
                }
                // else if(!field.contains("[")){
                // // normal field

                // }
                else if (field.contains("*")) {
                    // snapshot
                    String baseLocal = base.substring(base.indexOf("/") + 1);
                    LInst inst = new NondInst(LumosAgent.findMethod(method), stmt, -1, baseLocal, "SNAPSHOT");
                    LumosAgent.activate(inst);
                } else {
                    // collections or normal fields
                    if(witnessMap.containsKey(methodAndInst)){
                        for (String w : witnessMap.get(methodAndInst)) {
                            LInst inst = new NondInst(LumosAgent.findMethod(method), stmt, -1, w, "BOUNDARY_WITNESS");
                            LumosAgent.activate(inst);
                        }
                    }
                }
            }
        }

        List<String> ConcurrencyInsts = CompileUtils.readFrom(ConcurrencyInstFile);
        for (String s : ConcurrencyInsts) {
            // FIXME: lambda currently unresolved
            if(s.contains("$lambda_")){
                continue;
            }

            String[] rawItems = s.split("\t");
            String methodAndInst = rawItems[0];

            if(!all && !inDepthInsts.contains(methodAndInst)){
                continue;
            }
            String v = rawItems[1];
            String nondType = rawItems[2];

            String method = methodAndInst.substring(0, methodAndInst.indexOf("/"));
            String instId = methodAndInst.substring(methodAndInst.indexOf("/") + 1);
            String local = v.substring(v.indexOf("/") + 1);

            if (instId.contains("fresh-null-assign") || !method.contains("hadoop")) {
                continue;
            }
            if (!LumosAgent.findMethod(method).hasActiveBody()) {
                continue;
            }
            String stmt = LumosAgent.translationMap.get(method).get(instId);

            if (stmt == null) {
                p("!!" + methodAndInst);
            }
            LInst inst = new NondInst(LumosAgent.findMethod(method), stmt, -1, local, nondType);
            LumosAgent.activate(inst);
        }


            //System.getProperty("ContentInst");
        List<String> ContentInsts = CompileUtils.readFrom(ContentInstFile);
        for (String s : ContentInsts) {
            // FIXME: lambda currently unresolved
            if(s.contains("$lambda_")){
                continue;
            }

            String[] rawItems = s.split("\t");
            // String nondType = rawItems[0];
            String methodAndInst = rawItems[1];

            if(!all && !inDepthInsts.contains(methodAndInst)){
                continue;
            }
            String v = rawItems[2];
            String method = methodAndInst.substring(0, methodAndInst.indexOf("/"));
            String instId = methodAndInst.substring(methodAndInst.indexOf("/") + 1);
            String local = v.substring(v.indexOf("/") + 1);

            if (instId.contains("fresh-null-assign") || !method.contains("hadoop")) {
                continue;
            }

            Map<String, String> mm = LumosAgent.translationMap.get(method);
            if(mm == null){
                p(method);
                continue;
            }
            String stmt = mm.get(instId);

            if (stmt == null) {
                p("!!" + methodAndInst);
            }
            LInst inst = new NondInst(LumosAgent.findMethod(method), stmt, -1, local, "CONTENT");
            LumosAgent.activate(inst);
        }
        if (all) {
            List<String> InputInsts = CompileUtils.readFrom(InputInstFile);
            for (String s : InputInsts) {
                // FIXME: lambda currently unresolved
                if (s.contains("$lambda_")) {
                    continue;
                }

                String[] rawItems = s.split("\t");
                String methodAndInst = rawItems[0];

                if (!all && !inDepthInsts.contains(methodAndInst)) {
                    continue;
                }
                String v = rawItems[1];
                String method = methodAndInst.substring(0, methodAndInst.indexOf("/"));
                String instId = methodAndInst.substring(methodAndInst.indexOf("/") + 1);
                String local = v.substring(v.indexOf("/") + 1);

                if (instId.contains("fresh-null-assign") || !method.contains("hadoop")) {
                    continue;
                }
                if (!LumosAgent.findMethod(method).hasActiveBody()) {
                    continue;
                }
                String stmt = LumosAgent.translationMap.get(method).get(instId);
                LInst inst = new NondInst(LumosAgent.findMethod(method), stmt, -1, local, "INPUT");
                LumosAgent.activate(inst);
            }
        }
        if(LumosAgent.bench.equals("hdfs")){
            addManualInst();
        }
    }

    public static void addManualInst(){
        if (LumosAgent.component.equals("dn")) {
            String methodAndInst = "<org.apache.hadoop.hdfs.server.datanode.DataNode: void transferBlock(org.apache.hadoop.hdfs.protocol.ExtendedBlock,org.apache.hadoop.hdfs.protocol.DatanodeInfo[],org.apache.hadoop.fs.StorageType[])>/if/0";
            String method = methodAndInst.substring(0, methodAndInst.indexOf("/"));
            String instId = methodAndInst.substring(methodAndInst.indexOf("/") + 1);

            String stmt = LumosAgent.translationMap.get(method).get(instId);
            // p("$$ " + stmt);
            LInst inst = new NondInst(LumosAgent.findMethod(method), stmt, -1, "replicaNotExist", "MANUAL");
            LumosAgent.activate(inst);

            methodAndInst = "<org.apache.hadoop.hdfs.server.datanode.fsdataset.impl.ReplicaMap: void addAll(org.apache.hadoop.hdfs.server.datanode.fsdataset.impl.ReplicaMap)>/invoke/0";
            method = methodAndInst.substring(0, methodAndInst.indexOf("/"));
            instId = methodAndInst.substring(methodAndInst.indexOf("/") + 1);

            stmt = LumosAgent.translationMap.get(method).get(instId);
            // p("$$ " + stmt);
            inst = new NondInst(LumosAgent.findMethod(method), stmt, -1, "$stack3", "MANUAL");
            LumosAgent.activate(inst);

        }
    }

    public static void addTrainInsts(){
        p("adding for train...");
    }

    public static void p(String s) {
        System.out.println(s);
    }

    public static void p(Object s) {
        p(s+"");
    }
}
