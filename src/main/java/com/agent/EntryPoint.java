package com.agent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.agent.compile.CompileUtils;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;

public class EntryPoint{

    public static Set<SootMethod> entryMethods = new HashSet<>();
    public static Set<String> entryClasses = new HashSet<>();
    public static void addEntryMethods(){
        String component = LumosAgent.component;
        String specialName = System.getProperty("entry");
        // LumosAgent.entryMethods.add(Scene.v().getSootClass("com.mycompany.app.Work").getMethodByName("work"));
        if (LumosAgent.component.equals("nn")) {
            SootClass ecls = Scene.v().getSootClass("org.apache.hadoop.hdfs.server.namenode.NameNodeRpcServer");
            for (SootMethod sm : ecls.getMethods()) {
                String mname = sm.getName();
                if (mname.equals("<init>") ||
                        mname.equals("join") ||
                        mname.equals("start") ||
                        mname.equals("stop") ||
                        mname.equals("<clinit>") ||
                        mname.equals("checkNNStartup") ||
                        mname.equals("verifyRequest") ||
                        mname.equals("verifySoftwareVersion")||
                        mname.equals("verifyLayoutVersion")||
                        mname.equals("getClientMachine") ||
                        mname.equals("getServiceRpcAddress") ||
                        mname.equals("getRpcAddress") ||
                        mname.equals("getRemoteUser") ||
                        mname.equals("checkPathLength") ||
                        mname.equals("readOp") ||
                        (specialName != null && !specialName.equals("any") && !mname.equals(specialName))) {
                    continue;
                }
                entryMethods.add(sm);
            }
            if (specialName == null ||  specialName.equals("any")) {
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.blockmanagement.BlockManager")
                        .getMethodByName("computeDatanodeWork"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.blockmanagement.BlockManager")
                        .getMethodByName("processPendingReplications"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.blockmanagement.BlockManager")
                        .getMethodByName("rescanPostponedMisreplicatedBlocks"));

                entryMethods.add(Scene.v().getMethod("<org.apache.hadoop.hdfs.server.namenode.FSNamesystem$LazyPersistFileScrubber: void clearCorruptLazyPersistFiles()>"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.namenode.FSNamesystem")
                        .getMethodByName("checkAvailableResources"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.namenode.FSNamesystem")
                        .getMethodByName("nameNodeHasResourcesAvailable"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.blockmanagement.PendingReplicationBlocks$PendingReplicationMonitor")
                        .getMethodByName("pendingReplicationCheck"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.blockmanagement.DecommissionManager$Monitor")
                        .getMethodByName("check"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.blockmanagement.HeartbeatManager")
                        .getMethodByName("heartbeatCheck"));

                // entryMethods.add(Scene.v().getMethod("<org.apache.hadoop.hdfs.server.blockmanagement.CacheReplicationMonitor: void rescan()>"));
                entryMethods.add(Scene.v().getSootClass(
                        "org.apache.hadoop.hdfs.server.namenode.LeaseManager")
                        .getMethodByName("checkLeases"));
                // logSync
            }
        }
        else if(LumosAgent.component.equals("dn")){
            // SootClass ecls = Scene.v().getSootClass("org.apache.hadoop.hdfs.server.datanode.BPOfferService");
            // entryMethods.add(ecls.getMethodByName("processCommandFromActive"));
            SootClass recvCls = Scene.v().getSootClass("org.apache.hadoop.hdfs.server.datanode.DataXceiver");
            for (SootMethod sm : recvCls.getMethods()) {
                String mname = sm.getName();
                if (mname.equals("readBlock") ||
                        mname.equals("writeBlock") ||
                        mname.equals("replaceBlock") ||
                        mname.equals("copyBlock") ||
                        mname.equals("blockChecksum") ||
                        mname.equals("transferBlock") ||
                        mname.equals("requestShortCircuitFds") ||
                        mname.equals("releaseShortCircuitFds") ||
                        mname.equals("requestShortCircuitShm")) {
                    entryMethods.add(sm);
                }
            }
            entryMethods.add(
                    Scene.v().getMethod("<org.apache.hadoop.hdfs.server.datanode.DirectoryScanner: void reconcile()>"));
        }

        // SootMethod protoM = Scene.v().getMethod(
        //         "<org.apache.hadoop.hdfs.protocolPB.PBHelper: org.apache.hadoop.hdfs.protocol.proto.DatanodeProtocolProtos$DatanodeCommandProto convert(org.apache.hadoop.hdfs.server.protocol.DatanodeCommand)>");
        for (SootMethod toggleM : entryMethods) {
            p("adding to " + toggleM.getName());
            Body b = LumosAgent.getBody(toggleM);
            List<Stmt> stmts = CompileUtils.generateStartRecording(toggleM.toString());
            CompileUtils.insertAt(b.getUnits(), stmts, CompileUtils.firstStmt(b), true);
            // if(component.equals("dn") || !toggleM.toString().contains("sendHeartbeat")){
            if(LumosAgent.component.equals("nn") && toggleM.getDeclaringClass().getShortName().equals("NameNodeRpcServer")){
                SootMethod protoM;
                String name = toggleM.getName();
                SootClass nnProtoClass = Scene.v()
                        .getSootClass("org.apache.hadoop.hdfs.protocolPB.DatanodeProtocolServerSideTranslatorPB");
                protoM = nnProtoClass.getMethodByNameUnsafe(name);
                if (protoM == null) {
                    nnProtoClass = Scene.v()
                            .getSootClass("org.apache.hadoop.hdfs.protocolPB.ClientNamenodeProtocolTranslatorPB");
                    protoM = nnProtoClass.getMethodByNameUnsafe(name);
                }
                if (protoM == null) {
                    protoM = toggleM;
                }
                Body pb = LumosAgent.getBody(protoM);
                for (Stmt ret : CompileUtils.getReturnStmts(pb)) {
                    stmts = CompileUtils.generateEndRecording();
                    pb.getUnits().insertBefore(stmts, ret);
                }
                p("%%" + protoM);
                protoM.setActiveBody(pb);
            }
            else{
                for (Stmt ret : CompileUtils.getReturnStmts(b)) {
                    stmts = CompileUtils.generateEndRecording();
                    b.getUnits().insertBefore(stmts, ret);
                }
            }
            // } else {
            // }
            toggleM.setActiveBody(b);
        }
        if(component.equals("nn")){
            // entryMethods.add(protoM);
        }
        else{
            SootMethod beginM1 = Scene.v()
                    .getMethod("<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.protocol.HeartbeatResponse sendHeartBeat()>");
            Body b = beginM1.getActiveBody();
            CompileUtils.insertAt(b.getUnits(), CompileUtils.generateStartRecording("offerService"),
                    CompileUtils.firstStmt(b), false);
            beginM1.setActiveBody(b);

            SootMethod endM1 = Scene.v()
                    .getMethod("<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: void processQueueMessages()>");
            b = endM1.getActiveBody();
            for (Stmt ret : CompileUtils.getReturnStmts(b)) {
                CompileUtils.insertAt(b.getUnits(), CompileUtils.generateEndRecording(),
                        ret, true);
            }
            endM1.setActiveBody(b);

            SootMethod beginM2 = Scene.v()
                    .getMethod("<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.apache.hadoop.hdfs.protocol.ExtendedBlock popNextSuspectBlock()>");
            b = beginM2.getActiveBody();
            CompileUtils.insertAt(b.getUnits(), CompileUtils.generateStartRecording("VolumeScanner"),
                    CompileUtils.firstStmt(b), false);
            beginM2.setActiveBody(b);

            SootMethod endM2 = Scene.v()
                    .getMethod("<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: long runLoop(org.apache.hadoop.hdfs.protocol.ExtendedBlock)>");
            b = endM2.getActiveBody();
            for (Stmt ret : CompileUtils.getReturnStmts(b)) {
                CompileUtils.insertAt(b.getUnits(), CompileUtils.generateEndRecording(),
                        ret, true);
            }
            endM2.setActiveBody(b);
            entryMethods.add(beginM1);
            entryMethods.add(endM1);
            entryMethods.add(beginM2);
            entryMethods.add(endM2);
            // SootMethod sm2 = Scene.v().getMethod("<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: void run()>");
            // Body b2 = sm2.getActiveBody();
            // Stmt start2 = CompileUtils.searchStmt(b2, "l4 = this", -1);
            // CompileUtils.insertAt(b2.getUnits(), CompileUtils.generateStartRecording(sm2.getName()),
            //         start2, false);
            // Stmt end2 = CompileUtils.searchStmt(b2,
            //         "iter = specialinvoke this.<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: long runLoop(org.apache.hadoop.hdfs.protocol.ExtendedBlock)>(suspectBlock)",
            //         -1);
            // CompileUtils.insertAt(b2.getUnits(), CompileUtils.generateEndRecording(),
            //         end2, false);
            // sm2.setActiveBody(b2);

            // p(sm1.getActiveBody());
            // p(sm2.getActiveBody());
        }

        for (SootMethod sm : entryMethods) {
            entryClasses.add(sm.getDeclaringClass().getName());
        }
  
    }

    public static void p(String s) {
        System.out.println(s);
    }

    public static void p(Object s) {
        p(s+"");
    }
}
