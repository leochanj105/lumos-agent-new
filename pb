main!!
reading translation...
!! 78788
    private void offerService() throws java.lang.Exception
    {
        boolean $stack56, sleepTime, $stack101, $stack105, $stack119, $stack135, $stack62, $stack78, $stack221, $stack173, $stack190, $stack197, $stack179, $stack198, $stack159, $stack57;
        byte $stack122, $stack94, $stack75;
        edu.brown.cs.systems.xtrace.logging.Log4jProxy $stack54, $stack133, $stack219;
        edu.brown.cs.systems.xtrace.logging.LoggerWrappers $stack153, $stack136, $stack222, $stack180, $stack202, $stack161;
        edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper $stack155, $stack138, $stack224, $stack182, $stack204, $stack163;
        int $stack129, $stack64;
        java.lang.AssertionError $stack152;
        java.lang.Class $stack171, $stack188, $stack195;
        java.lang.InterruptedException $stack213, $stack167;
        java.lang.Object[] $stack66;
        java.lang.String l9, l11, l13, reClass, $stack172, $stack189, $stack196, l16, l19, l22;
        java.lang.StringBuilder $stack24, $stack27, $stack28, $stack29, $stack32, $stack33, $stack34, $stack37, $stack38, $stack39, $stack42, $stack43, $stack44, $stack47, $stack48, $stack49, $stack52, $stack123, $stack126, $stack127, $stack130, $stack131, $stack214, $stack216, $stack217, $stack174, $stack176, $stack177;
        java.lang.Thread $stack168;
        java.lang.Throwable $stack211, $stack158, startTime#49, l21;
        java.net.InetSocketAddress $stack26;
        java.util.List resp#17;
        java.util.Map $stack74, l8, $stack79;
        long $stack31, $stack36, $stack41, $stack46, $stack51, startTime, $stack109, $stack110, startProcessCommands, endProcessCommands, $stack121, $stack125, $stack90, $stack93, $stack92, waitTime, $stack200, sleepTime#48;
        org.apache.commons.logging.Log $stack25, l10, $stack124, l12, $stack215, l14, $stack175, l17, l20, l23;
        org.apache.hadoop.ha.HAServiceProtocol$HAServiceState $stack114, $stack116, $stack115;
        org.apache.hadoop.hdfs.server.datanode.BPOfferService $stack111;
        org.apache.hadoop.hdfs.server.datanode.BPServiceActor this;
        org.apache.hadoop.hdfs.server.datanode.BPServiceActor$Scheduler $stack58, $stack60, $stack108, $stack72;
        org.apache.hadoop.hdfs.server.datanode.DNConf $stack30, $stack35, $stack40, $stack45, $stack50, $stack91, $stack199;
        org.apache.hadoop.hdfs.server.datanode.DataNode $stack100, $stack106;
        org.apache.hadoop.hdfs.server.datanode.metrics.DataNodeMetrics $stack107;
        org.apache.hadoop.hdfs.server.protocol.DatanodeCommand startProcessCommands#21;
        org.apache.hadoop.hdfs.server.protocol.DatanodeCommand[] $stack118, $stack128, $stack67, $stack65, $stack70;
        org.apache.hadoop.hdfs.server.protocol.HeartbeatResponse resp;
        org.apache.hadoop.hdfs.server.protocol.NNHAStatusHeartbeat $stack112, $stack113;
        org.apache.hadoop.ipc.RemoteException $stack169, startTime#26, l15, l18;
        org.aspectj.lang.JoinPoint$StaticPart $stack55, $stack154, $stack134, $stack137, $stack220, $stack223, $stack181, $stack203, $stack162;

        this := @this: org.apache.hadoop.hdfs.server.datanode.BPServiceActor;

        $stack25 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.commons.logging.Log LOG>;
/*629*/

        $stack24 = new java.lang.StringBuilder;
/*629*/

        specialinvoke $stack24.<java.lang.StringBuilder: void <init>(java.lang.String)>("For namenode ");
/*629*/

        $stack26 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: java.net.InetSocketAddress nnAddr>;
/*629*/

        $stack27 = virtualinvoke $stack24.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.Object)>($stack26);
/*629*/

        $stack28 = virtualinvoke $stack27.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(" using");
/*629*/

        $stack29 = virtualinvoke $stack28.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(" DELETEREPORT_INTERVAL of ");
/*630*/

        $stack30 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.datanode.DNConf dnConf>;
/*630*/

        $stack31 = $stack30.<org.apache.hadoop.hdfs.server.datanode.DNConf: long deleteReportInterval>;
/*630*/

        $stack32 = virtualinvoke $stack29.<java.lang.StringBuilder: java.lang.StringBuilder append(long)>($stack31);
/*630*/

        $stack33 = virtualinvoke $stack32.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(" msec ");
/*630*/

        $stack34 = virtualinvoke $stack33.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(" BLOCKREPORT_INTERVAL of ");
/*631*/

        $stack35 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.datanode.DNConf dnConf>;
/*631*/

        $stack36 = $stack35.<org.apache.hadoop.hdfs.server.datanode.DNConf: long blockReportInterval>;
/*631*/

        $stack37 = virtualinvoke $stack34.<java.lang.StringBuilder: java.lang.StringBuilder append(long)>($stack36);
/*631*/

        $stack38 = virtualinvoke $stack37.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("msec");
/*631*/

        $stack39 = virtualinvoke $stack38.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(" CACHEREPORT_INTERVAL of ");
/*632*/

        $stack40 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.datanode.DNConf dnConf>;
/*632*/

        $stack41 = $stack40.<org.apache.hadoop.hdfs.server.datanode.DNConf: long cacheReportInterval>;
/*632*/

        $stack42 = virtualinvoke $stack39.<java.lang.StringBuilder: java.lang.StringBuilder append(long)>($stack41);
/*632*/

        $stack43 = virtualinvoke $stack42.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("msec");
/*632*/

        $stack44 = virtualinvoke $stack43.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(" Initial delay: ");
/*633*/

        $stack45 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.datanode.DNConf dnConf>;
/*633*/

        $stack46 = $stack45.<org.apache.hadoop.hdfs.server.datanode.DNConf: long initialBlockReportDelay>;
/*633*/

        $stack47 = virtualinvoke $stack44.<java.lang.StringBuilder: java.lang.StringBuilder append(long)>($stack46);
/*633*/

        $stack48 = virtualinvoke $stack47.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("msec");
/*633*/

        $stack49 = virtualinvoke $stack48.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("; heartBeatInterval=");
/*634*/

        $stack50 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.datanode.DNConf dnConf>;
/*634*/

        $stack51 = $stack50.<org.apache.hadoop.hdfs.server.datanode.DNConf: long heartBeatInterval>;
/*634*/

        $stack52 = virtualinvoke $stack49.<java.lang.StringBuilder: java.lang.StringBuilder append(long)>($stack51);
/*634*/

        l9 = virtualinvoke $stack52.<java.lang.StringBuilder: java.lang.String toString()>();
/*634*/
/*634*/

        l10 = $stack25;
/*634*/

        $stack54 = staticinvoke <edu.brown.cs.systems.xtrace.logging.Log4jProxy: edu.brown.cs.systems.xtrace.logging.Log4jProxy aspectOf()>();
/*634*/

        $stack55 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_13>;
/*634*/

        virtualinvoke $stack54.<edu.brown.cs.systems.xtrace.logging.Log4jProxy: void ajc$before$edu_brown_cs_systems_xtrace_logging_Log4jProxy$1$5905ebc8(java.lang.Object,java.lang.Object,org.aspectj.lang.JoinPoint$StaticPart)>(l10, l9, $stack55);
/*634*/

        $stack56 = l10 instanceof edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper;
/*634*/

        if $stack56 == 0 goto label01;
/*634*/

        $stack153 = staticinvoke <edu.brown.cs.systems.xtrace.logging.LoggerWrappers: edu.brown.cs.systems.xtrace.logging.LoggerWrappers aspectOf()>();
/*634*/

        $stack155 = (edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper) l10;
/*634*/

        $stack154 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_13>;
/*634*/

        virtualinvoke $stack153.<edu.brown.cs.systems.xtrace.logging.LoggerWrappers: void ajc$before$edu_brown_cs_systems_xtrace_logging_LoggerWrappers$5$96495bd6(edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper,org.aspectj.lang.JoinPoint$StaticPart)>($stack155, $stack154);
/*634*/

     label01:
        interfaceinvoke l10.<org.apache.commons.logging.Log: void info(java.lang.Object)>(l9);
/*629*/

        goto label33;
/*638*/

     label02:
        $stack58 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.datanode.BPServiceActor$Scheduler scheduler>;
/*640*/

        startTime = virtualinvoke $stack58.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor$Scheduler: long monotonicNow()>();
/*640*/
/*640*/

        $stack60 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.datanode.BPServiceActor$Scheduler scheduler>;
/*644*/

        sleepTime = virtualinvoke $stack60.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor$Scheduler: boolean isHeartbeatDue(long)>(startTime);
/*644*/
/*644*/

        if sleepTime == 0 goto label08;
/*645*/

        $stack100 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.datanode.DataNode dn>;
/*653*/

        $stack101 = virtualinvoke $stack100.<org.apache.hadoop.hdfs.server.datanode.DataNode: boolean areHeartbeatsDisabledForTests()>();
/*653*/

        if $stack101 != 0 goto label08;
/*653*/

        staticinvoke <java.lang.System: long nanoTime()>();
/*657*/

        resp = virtualinvoke this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.protocol.HeartbeatResponse sendHeartBeat()>();
/*660*/
/*660*/

        $stack105 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: boolean $assertionsDisabled>;
/*661*/

        if $stack105 != 0 goto label03;
/*661*/

        if resp != null goto label03;
/*665*/

        $stack152 = new java.lang.AssertionError;
/*665*/

        specialinvoke $stack152.<java.lang.AssertionError: void <init>()>();
/*665*/

        throw $stack152;
/*665*/

     label03:
        $stack106 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.datanode.DataNode dn>;
/*666*/

        $stack107 = virtualinvoke $stack106.<org.apache.hadoop.hdfs.server.datanode.DataNode: org.apache.hadoop.hdfs.server.datanode.metrics.DataNodeMetrics getMetrics()>();
/*666*/

        $stack108 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.datanode.BPServiceActor$Scheduler scheduler>;
/*666*/

        $stack109 = virtualinvoke $stack108.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor$Scheduler: long monotonicNow()>();
/*666*/

        $stack110 = $stack109 - startTime;
/*666*/

        virtualinvoke $stack107.<org.apache.hadoop.hdfs.server.datanode.metrics.DataNodeMetrics: void addHeartbeat(long)>($stack110);
/*666*/

        $stack111 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.datanode.BPOfferService bpos>;
/*675*/

        $stack112 = virtualinvoke resp.<org.apache.hadoop.hdfs.server.protocol.HeartbeatResponse: org.apache.hadoop.hdfs.server.protocol.NNHAStatusHeartbeat getNameNodeHaState()>();
/*675*/

        virtualinvoke $stack111.<org.apache.hadoop.hdfs.server.datanode.BPOfferService: void updateActorStatesFromHeartbeat(org.apache.hadoop.hdfs.server.datanode.BPServiceActor,org.apache.hadoop.hdfs.server.protocol.NNHAStatusHeartbeat)>(this, $stack112);
/*674*/

        $stack113 = virtualinvoke resp.<org.apache.hadoop.hdfs.server.protocol.HeartbeatResponse: org.apache.hadoop.hdfs.server.protocol.NNHAStatusHeartbeat getNameNodeHaState()>();
/*676*/

        $stack114 = virtualinvoke $stack113.<org.apache.hadoop.hdfs.server.protocol.NNHAStatusHeartbeat: org.apache.hadoop.ha.HAServiceProtocol$HAServiceState getState()>();
/*676*/

        this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.ha.HAServiceProtocol$HAServiceState state> = $stack114;
/*676*/

        $stack116 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.ha.HAServiceProtocol$HAServiceState state>;
/*678*/

        $stack115 = <org.apache.hadoop.ha.HAServiceProtocol$HAServiceState: org.apache.hadoop.ha.HAServiceProtocol$HAServiceState ACTIVE>;
/*678*/

        if $stack116 != $stack115 goto label04;
/*678*/

        specialinvoke this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: void handleRollingUpgradeStatus(org.apache.hadoop.hdfs.server.protocol.HeartbeatResponse)>(resp);
/*679*/

     label04:
        startProcessCommands = staticinvoke <org.apache.hadoop.util.Time: long monotonicNow()>();
/*681*/
/*681*/

        $stack118 = virtualinvoke resp.<org.apache.hadoop.hdfs.server.protocol.HeartbeatResponse: org.apache.hadoop.hdfs.server.protocol.DatanodeCommand[] getCommands()>();
/*682*/

        $stack119 = virtualinvoke this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: boolean processCommand(org.apache.hadoop.hdfs.server.protocol.DatanodeCommand[])>($stack118);
/*682*/

        if $stack119 != 0 goto label06;
/*682*/

     label05:
        goto label33;
/*683*/

     label06:
        endProcessCommands = staticinvoke <org.apache.hadoop.util.Time: long monotonicNow()>();
/*684*/
/*684*/

        $stack121 = endProcessCommands - startProcessCommands;
/*685*/

        $stack122 = $stack121 cmp 2000L;
/*685*/

        if $stack122 <= 0 goto label08;
/*685*/

        $stack124 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.commons.logging.Log LOG>;
/*686*/

        $stack123 = new java.lang.StringBuilder;
/*686*/

        specialinvoke $stack123.<java.lang.StringBuilder: void <init>(java.lang.String)>("Took ");
/*686*/

        $stack125 = endProcessCommands - startProcessCommands;
/*686*/

        $stack126 = virtualinvoke $stack123.<java.lang.StringBuilder: java.lang.StringBuilder append(long)>($stack125);
/*686*/

        $stack127 = virtualinvoke $stack126.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("ms to process ");
/*687*/

        $stack128 = virtualinvoke resp.<org.apache.hadoop.hdfs.server.protocol.HeartbeatResponse: org.apache.hadoop.hdfs.server.protocol.DatanodeCommand[] getCommands()>();
/*687*/

        $stack129 = lengthof $stack128;
/*687*/

        $stack130 = virtualinvoke $stack127.<java.lang.StringBuilder: java.lang.StringBuilder append(int)>($stack129);
/*687*/

        $stack131 = virtualinvoke $stack130.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(" commands from NN");
/*688*/

        l11 = virtualinvoke $stack131.<java.lang.StringBuilder: java.lang.String toString()>();
/*688*/
/*688*/

        l12 = $stack124;
/*688*/

        $stack133 = staticinvoke <edu.brown.cs.systems.xtrace.logging.Log4jProxy: edu.brown.cs.systems.xtrace.logging.Log4jProxy aspectOf()>();
/*688*/

        $stack134 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_14>;
/*688*/

        virtualinvoke $stack133.<edu.brown.cs.systems.xtrace.logging.Log4jProxy: void ajc$before$edu_brown_cs_systems_xtrace_logging_Log4jProxy$1$5905ebc8(java.lang.Object,java.lang.Object,org.aspectj.lang.JoinPoint$StaticPart)>(l12, l11, $stack134);
/*688*/

        $stack135 = l12 instanceof edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper;
/*688*/

        if $stack135 == 0 goto label07;
/*688*/

        $stack136 = staticinvoke <edu.brown.cs.systems.xtrace.logging.LoggerWrappers: edu.brown.cs.systems.xtrace.logging.LoggerWrappers aspectOf()>();
/*688*/

        $stack138 = (edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper) l12;
/*688*/

        $stack137 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_14>;
/*688*/

        virtualinvoke $stack136.<edu.brown.cs.systems.xtrace.logging.LoggerWrappers: void ajc$before$edu_brown_cs_systems_xtrace_logging_LoggerWrappers$5$96495bd6(edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper,org.aspectj.lang.JoinPoint$StaticPart)>($stack138, $stack137);
/*688*/

     label07:
        interfaceinvoke l12.<org.apache.commons.logging.Log: void info(java.lang.Object)>(l11);
/*686*/

     label08:
        $stack62 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: boolean sendImmediateIBR>;
/*692*/

        if $stack62 != 0 goto label09;
/*692*/

        $stack90 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: long lastDeletedReport>;
/*693*/

        $stack93 = startTime - $stack90;
/*693*/

        $stack91 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.datanode.DNConf dnConf>;
/*693*/

        $stack92 = $stack91.<org.apache.hadoop.hdfs.server.datanode.DNConf: long deleteReportInterval>;
/*693*/

        $stack94 = $stack93 cmp $stack92;
/*693*/

        if $stack94 <= 0 goto label10;
/*693*/

     label09:
        specialinvoke this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: void reportReceivedDeletedBlocks()>();
/*694*/

        this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: long lastDeletedReport> = startTime;
/*695*/

     label10:
        resp#17 = virtualinvoke this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: java.util.List blockReport()>();
/*698*/
/*698*/

        if resp#17 != null goto label11;
/*699*/

        $stack67 = null;
/*699*/

        goto label12;
/*699*/

     label11:
        $stack64 = interfaceinvoke resp#17.<java.util.List: int size()>();
/*699*/

        $stack65 = newarray (org.apache.hadoop.hdfs.server.protocol.DatanodeCommand)[$stack64];
/*699*/

        $stack66 = interfaceinvoke resp#17.<java.util.List: java.lang.Object[] toArray(java.lang.Object[])>($stack65);
/*699*/

        $stack67 = (org.apache.hadoop.hdfs.server.protocol.DatanodeCommand[]) $stack66;
/*699*/

     label12:
        virtualinvoke this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: boolean processCommand(org.apache.hadoop.hdfs.server.protocol.DatanodeCommand[])>($stack67);
/*699*/

        startProcessCommands#21 = virtualinvoke this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.protocol.DatanodeCommand cacheReport()>();
/*701*/
/*701*/

        $stack70 = newarray (org.apache.hadoop.hdfs.server.protocol.DatanodeCommand)[1];
/*702*/

        $stack70[0] = startProcessCommands#21;
/*702*/

        virtualinvoke this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: boolean processCommand(org.apache.hadoop.hdfs.server.protocol.DatanodeCommand[])>($stack70);
/*702*/

        $stack72 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.datanode.BPServiceActor$Scheduler scheduler>;
/*708*/

        waitTime = virtualinvoke $stack72.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor$Scheduler: long getHeartbeatWaitTime()>();
/*708*/
/*708*/

        $stack74 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: java.util.Map pendingIncrementalBRperStorage>;
/*709*/

        l8 = $stack74;
/*709*/

        entermonitor $stack74;
/*709*/

     label13:
        $stack75 = waitTime cmp 0L;
/*710*/

        if $stack75 <= 0 goto label18;
/*710*/

        $stack78 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: boolean sendImmediateIBR>;
/*710*/

        if $stack78 != 0 goto label18;
/*710*/

     label14:
        $stack79 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: java.util.Map pendingIncrementalBRperStorage>;
/*712*/

        virtualinvoke $stack79.<java.lang.Object: void wait(long)>(waitTime);
/*712*/

     label15:
        goto label18;
/*713*/

     label16:
        $stack213 := @caughtexception;
/*723*/

        $stack215 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.commons.logging.Log LOG>;
/*714*/

        $stack214 = new java.lang.StringBuilder;
/*714*/

        specialinvoke $stack214.<java.lang.StringBuilder: void <init>(java.lang.String)>("BPOfferService for ");
/*714*/

        $stack216 = virtualinvoke $stack214.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.Object)>(this);
/*714*/

        $stack217 = virtualinvoke $stack216.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(" interrupted");
/*714*/

        l13 = virtualinvoke $stack217.<java.lang.StringBuilder: java.lang.String toString()>();
/*714*/
/*714*/

        l14 = $stack215;
/*714*/

        $stack219 = staticinvoke <edu.brown.cs.systems.xtrace.logging.Log4jProxy: edu.brown.cs.systems.xtrace.logging.Log4jProxy aspectOf()>();
/*714*/

        $stack220 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_15>;
/*714*/

        virtualinvoke $stack219.<edu.brown.cs.systems.xtrace.logging.Log4jProxy: void ajc$before$edu_brown_cs_systems_xtrace_logging_Log4jProxy$1$5905ebc8(java.lang.Object,java.lang.Object,org.aspectj.lang.JoinPoint$StaticPart)>(l14, l13, $stack220);
/*714*/

        $stack221 = l14 instanceof edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper;
/*714*/

        if $stack221 == 0 goto label17;
/*714*/

        $stack222 = staticinvoke <edu.brown.cs.systems.xtrace.logging.LoggerWrappers: edu.brown.cs.systems.xtrace.logging.LoggerWrappers aspectOf()>();
/*714*/

        $stack224 = (edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper) l14;
/*714*/

        $stack223 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_15>;
/*714*/

        virtualinvoke $stack222.<edu.brown.cs.systems.xtrace.logging.LoggerWrappers: void ajc$before$edu_brown_cs_systems_xtrace_logging_LoggerWrappers$5$96495bd6(edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper,org.aspectj.lang.JoinPoint$StaticPart)>($stack224, $stack223);
/*714*/

     label17:
        interfaceinvoke l14.<org.apache.commons.logging.Log: void warn(java.lang.Object)>(l13);
/*714*/

     label18:
        exitmonitor l8;
/*709*/

     label19:
        goto label32;
/*709*/

     label20:
        $stack211 := @caughtexception;
/*723*/

        exitmonitor l8;
/*723*/

     label21:
        throw $stack211;
/*723*/

     label22:
        $stack169 := @caughtexception;
/*638*/

        startTime#26 = $stack169;
/*718*/

        reClass = virtualinvoke startTime#26.<org.apache.hadoop.ipc.RemoteException: java.lang.String getClassName()>();
/*719*/
/*719*/

        $stack171 = class "Lorg/apache/hadoop/hdfs/protocol/UnregisteredNodeException;";
/*720*/

        $stack172 = virtualinvoke $stack171.<java.lang.Class: java.lang.String getName()>();
/*720*/

        $stack173 = virtualinvoke $stack172.<java.lang.String: boolean equals(java.lang.Object)>(reClass);
/*720*/

        if $stack173 != 0 goto label23;
/*720*/

        $stack188 = class "Lorg/apache/hadoop/hdfs/server/protocol/DisallowedDatanodeException;";
/*721*/

        $stack189 = virtualinvoke $stack188.<java.lang.Class: java.lang.String getName()>();
/*721*/

        $stack190 = virtualinvoke $stack189.<java.lang.String: boolean equals(java.lang.Object)>(reClass);
/*721*/

        if $stack190 != 0 goto label23;
/*721*/

        $stack195 = class "Lorg/apache/hadoop/hdfs/server/common/IncorrectVersionException;";
/*722*/

        $stack196 = virtualinvoke $stack195.<java.lang.Class: java.lang.String getName()>();
/*722*/

        $stack197 = virtualinvoke $stack196.<java.lang.String: boolean equals(java.lang.Object)>(reClass);
/*722*/

        if $stack197 == 0 goto label25;
/*722*/

     label23:
        $stack175 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.commons.logging.Log LOG>;
/*723*/

        $stack174 = new java.lang.StringBuilder;
/*723*/

        specialinvoke $stack174.<java.lang.StringBuilder: void <init>()>();
/*723*/

        $stack176 = virtualinvoke $stack174.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.Object)>(this);
/*723*/

        $stack177 = virtualinvoke $stack176.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(" is shutting down");
/*723*/

        l15 = startTime#26;
/*723*/
/*723*/

        l16 = virtualinvoke $stack177.<java.lang.StringBuilder: java.lang.String toString()>();
/*723*/
/*723*/

        l17 = $stack175;
/*723*/

        $stack179 = l17 instanceof edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper;
/*723*/

        if $stack179 == 0 goto label24;
/*723*/

        $stack180 = staticinvoke <edu.brown.cs.systems.xtrace.logging.LoggerWrappers: edu.brown.cs.systems.xtrace.logging.LoggerWrappers aspectOf()>();
/*723*/

        $stack182 = (edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper) l17;
/*723*/

        $stack181 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_16>;
/*723*/

        virtualinvoke $stack180.<edu.brown.cs.systems.xtrace.logging.LoggerWrappers: void ajc$before$edu_brown_cs_systems_xtrace_logging_LoggerWrappers$5$96495bd6(edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper,org.aspectj.lang.JoinPoint$StaticPart)>($stack182, $stack181);
/*723*/

     label24:
        interfaceinvoke l17.<org.apache.commons.logging.Log: void warn(java.lang.Object,java.lang.Throwable)>(l16, l15);
/*723*/

        this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: boolean shouldServiceRun> = 0;
/*724*/

        return;
/*725*/

     label25:
        l18 = startTime#26;
/*727*/

        l19 = "RemoteException in offerService";
/*727*/

        l20 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.commons.logging.Log LOG>;
/*727*/

        $stack198 = l20 instanceof edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper;
/*727*/

        if $stack198 == 0 goto label26;
/*727*/

        $stack202 = staticinvoke <edu.brown.cs.systems.xtrace.logging.LoggerWrappers: edu.brown.cs.systems.xtrace.logging.LoggerWrappers aspectOf()>();
/*727*/

        $stack204 = (edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper) l20;
/*727*/

        $stack203 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_17>;
/*727*/

        virtualinvoke $stack202.<edu.brown.cs.systems.xtrace.logging.LoggerWrappers: void ajc$before$edu_brown_cs_systems_xtrace_logging_LoggerWrappers$5$96495bd6(edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper,org.aspectj.lang.JoinPoint$StaticPart)>($stack204, $stack203);
/*727*/

     label26:
        interfaceinvoke l20.<org.apache.commons.logging.Log: void warn(java.lang.Object,java.lang.Throwable)>(l19, l18);
/*727*/

     label27:
        $stack199 = this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.hadoop.hdfs.server.datanode.DNConf dnConf>;
/*729*/

        $stack200 = $stack199.<org.apache.hadoop.hdfs.server.datanode.DNConf: long heartBeatInterval>;
/*729*/

        sleepTime#48 = staticinvoke <java.lang.Math: long min(long,long)>(1000L, $stack200);
/*729*/
/*729*/

        staticinvoke <java.lang.Thread: void sleep(long)>(sleepTime#48);
/*730*/

     label28:
        goto label32;
/*731*/

     label29:
        $stack167 := @caughtexception;
/*638*/

        $stack168 = staticinvoke <java.lang.Thread: java.lang.Thread currentThread()>();
/*732*/

        virtualinvoke $stack168.<java.lang.Thread: void interrupt()>();
/*732*/

        goto label32;
/*732*/

     label30:
        $stack158 := @caughtexception;
/*638*/

        startTime#49 = $stack158;
/*734*/

        l21 = startTime#49;
/*735*/

        l22 = "IOException in offerService";
/*735*/

        l23 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.apache.commons.logging.Log LOG>;
/*735*/

        $stack159 = l23 instanceof edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper;
/*735*/

        if $stack159 == 0 goto label31;
/*735*/

        $stack161 = staticinvoke <edu.brown.cs.systems.xtrace.logging.LoggerWrappers: edu.brown.cs.systems.xtrace.logging.LoggerWrappers aspectOf()>();
/*735*/

        $stack163 = (edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper) l23;
/*735*/

        $stack162 = <org.apache.hadoop.hdfs.server.datanode.BPServiceActor: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_18>;
/*735*/

        virtualinvoke $stack161.<edu.brown.cs.systems.xtrace.logging.LoggerWrappers: void ajc$before$edu_brown_cs_systems_xtrace_logging_LoggerWrappers$5$96495bd6(edu.brown.cs.systems.xtrace.wrappers.CommonsLogWrapper,org.aspectj.lang.JoinPoint$StaticPart)>($stack163, $stack162);
/*735*/

     label31:
        interfaceinvoke l23.<org.apache.commons.logging.Log: void warn(java.lang.Object,java.lang.Throwable)>(l22, l21);
/*735*/

     label32:
        specialinvoke this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: void processQueueMessages()>();
/*737*/

     label33:
        $stack57 = specialinvoke this.<org.apache.hadoop.hdfs.server.datanode.BPServiceActor: boolean shouldRun()>();
/*638*/

        if $stack57 != 0 goto label02;
/*638*/

        return;
/*739*/

        catch java.lang.InterruptedException from label14 to label15 with label16;
        catch java.lang.Throwable from label13 to label19 with label20;
        catch java.lang.Throwable from label20 to label21 with label20;
        catch org.apache.hadoop.ipc.RemoteException from label02 to label05 with label22;
        catch org.apache.hadoop.ipc.RemoteException from label06 to label22 with label22;
        catch java.lang.InterruptedException from label27 to label28 with label29;
        catch java.io.IOException from label02 to label05 with label30;
        catch java.io.IOException from label06 to label22 with label30;
    }

    public void run()
    {
        boolean $stack27, $stack30, $stack37, $stack88, $stack79, $stack40, $stack43;
        byte $stack31;
        edu.brown.cs.systems.tracing.aspects.RunnablesCallablesThreads $stack20, $stack73, $stack49;
        edu.brown.cs.systems.xtrace.logging.LoggerWrappers $stack63, $stack89, $stack80, $stack51;
        edu.brown.cs.systems.xtrace.wrappers.Slf4jLoggerWrapper $stack65, $stack91, $stack82, $stack53;
        java.io.Closeable[] $r0, $r1, $r2;
        java.lang.InterruptedException $stack87;
        java.lang.Object $stack44;
        java.lang.Object[] $stack45, $stack76, $stack47;
        java.lang.Throwable $stack95, $stack78, iter#13, l12, $stack75, l5, $stack72, l19;
        java.util.Iterator l2;
        java.util.List $stack41;
        java.util.concurrent.TimeUnit $stack22, $stack24;
        long $stack23, $stack25, $stack26, iter;
        org.apache.hadoop.hdfs.protocol.ExtendedBlock suspectBlock;
        org.apache.hadoop.hdfs.server.datanode.VolumeScanner this, l6, l4, l9, l13, l16;
        org.apache.hadoop.hdfs.server.datanode.VolumeScanner$ScanResultHandler $stack28;
        org.apache.hadoop.hdfs.server.datanode.fsdataset.FsVolumeReference $stack77, $stack48;
        org.apache.hadoop.hdfs.server.datanode.fsdataset.FsVolumeSpi$BlockIterator iter#34;
        org.aspectj.lang.JoinPoint$StaticPart $stack21, $stack64, $stack90, $stack81, $stack52, $stack74, $stack50;
        org.slf4j.Logger l8, l11, l15, l18;

        this := @this: org.apache.hadoop.hdfs.server.datanode.VolumeScanner;

     label01:
        $stack20 = staticinvoke <edu.brown.cs.systems.tracing.aspects.RunnablesCallablesThreads: edu.brown.cs.systems.tracing.aspects.RunnablesCallablesThreads aspectOf()>();
/*597*/

        $stack21 = <org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_20>;
/*597*/

        virtualinvoke $stack20.<edu.brown.cs.systems.tracing.aspects.RunnablesCallablesThreads: void ajc$before$edu_brown_cs_systems_tracing_aspects_RunnablesCallablesThreads$8$d5fbf97f(edu.brown.cs.systems.tracing.aspects.RunnablesCallablesThreads$InstrumentedExecution,org.aspectj.lang.JoinPoint$StaticPart)>(this, $stack21);
/*597*/

        $stack22 = <java.util.concurrent.TimeUnit: java.util.concurrent.TimeUnit MINUTES>;
/*598*/

        $stack23 = staticinvoke <org.apache.hadoop.util.Time: long monotonicNow()>();
/*598*/

        $stack24 = <java.util.concurrent.TimeUnit: java.util.concurrent.TimeUnit MILLISECONDS>;
/*598*/

        $stack25 = virtualinvoke $stack22.<java.util.concurrent.TimeUnit: long convert(long,java.util.concurrent.TimeUnit)>($stack23, $stack24);
/*598*/

        this.<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: long startMinute> = $stack25;
/*597*/

        $stack26 = this.<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: long startMinute>;
/*599*/

        this.<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: long curMinute> = $stack26;
/*599*/

     label02:
        l6 = this;
/*601*/

        l8 = <org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.slf4j.Logger LOG>;
/*601*/

        $stack27 = l8 instanceof edu.brown.cs.systems.xtrace.wrappers.Slf4jLoggerWrapper;
/*601*/

        if $stack27 == 0 goto label03;
/*601*/

        $stack63 = staticinvoke <edu.brown.cs.systems.xtrace.logging.LoggerWrappers: edu.brown.cs.systems.xtrace.logging.LoggerWrappers aspectOf()>();
/*601*/

        $stack65 = (edu.brown.cs.systems.xtrace.wrappers.Slf4jLoggerWrapper) l8;
/*601*/

        $stack64 = <org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_16>;
/*601*/

        virtualinvoke $stack63.<edu.brown.cs.systems.xtrace.logging.LoggerWrappers: void ajc$before$edu_brown_cs_systems_xtrace_logging_LoggerWrappers$6$f79db3cf(edu.brown.cs.systems.xtrace.wrappers.Slf4jLoggerWrapper,org.aspectj.lang.JoinPoint$StaticPart)>($stack65, $stack64);
/*601*/

     label03:
        interfaceinvoke l8.<org.slf4j.Logger: void trace(java.lang.String,java.lang.Object)>("{}: thread starting.", l6);
/*601*/

        $stack28 = this.<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.apache.hadoop.hdfs.server.datanode.VolumeScanner$ScanResultHandler resultHandler>;
/*602*/

        virtualinvoke $stack28.<org.apache.hadoop.hdfs.server.datanode.VolumeScanner$ScanResultHandler: void setup(org.apache.hadoop.hdfs.server.datanode.VolumeScanner)>(this);
/*602*/

     label04:
        iter = 0L;
/*604*/
/*604*/

     label05:
        l4 = this;
/*609*/

        entermonitor this;
/*609*/

     label06:
        $stack30 = this.<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: boolean stopping>;
/*610*/

        if $stack30 == 0 goto label08;
/*610*/

        exitmonitor l4;
/*611*/

     label07:
        goto label19;
/*611*/

     label08:
        $stack31 = iter cmp 0L;
/*613*/

        if $stack31 <= 0 goto label10;
/*613*/

        virtualinvoke this.<java.lang.Object: void wait(long)>(iter);
/*614*/

        $stack37 = this.<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: boolean stopping>;
/*615*/

        if $stack37 == 0 goto label10;
/*615*/

        exitmonitor l4;
/*616*/

     label09:
        goto label19;
/*616*/

     label10:
        suspectBlock = specialinvoke this.<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.apache.hadoop.hdfs.protocol.ExtendedBlock popNextSuspectBlock()>();
/*619*/
/*619*/

        exitmonitor l4;
/*609*/

     label11:
        goto label14;
/*609*/

     label12:
        $stack95 := @caughtexception;
/*626*/

        exitmonitor l4;
/*626*/

     label13:
        throw $stack95;
/*626*/

     label14:
        iter = specialinvoke this.<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: long runLoop(org.apache.hadoop.hdfs.protocol.ExtendedBlock)>(suspectBlock);
/*621*/
/*621*/

        goto label05;
/*605*/

     label15:
        $stack87 := @caughtexception;
/*630*/

        l9 = this;
/*626*/

        l11 = <org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.slf4j.Logger LOG>;
/*626*/

        $stack88 = l11 instanceof edu.brown.cs.systems.xtrace.wrappers.Slf4jLoggerWrapper;
/*626*/

        if $stack88 == 0 goto label16;
/*626*/

        $stack89 = staticinvoke <edu.brown.cs.systems.xtrace.logging.LoggerWrappers: edu.brown.cs.systems.xtrace.logging.LoggerWrappers aspectOf()>();
/*626*/

        $stack91 = (edu.brown.cs.systems.xtrace.wrappers.Slf4jLoggerWrapper) l11;
/*626*/

        $stack90 = <org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_17>;
/*626*/

        virtualinvoke $stack89.<edu.brown.cs.systems.xtrace.logging.LoggerWrappers: void ajc$before$edu_brown_cs_systems_xtrace_logging_LoggerWrappers$6$f79db3cf(edu.brown.cs.systems.xtrace.wrappers.Slf4jLoggerWrapper,org.aspectj.lang.JoinPoint$StaticPart)>($stack91, $stack90);
/*626*/

     label16:
        interfaceinvoke l11.<org.slf4j.Logger: void trace(java.lang.String,java.lang.Object)>("{} exiting because of InterruptedException.", l9);
/*626*/

        goto label19;
/*626*/

     label17:
        $stack78 := @caughtexception;
/*640*/

        iter#13 = $stack78;
/*627*/

        l12 = iter#13;
/*628*/

        l13 = this;
/*628*/

        l15 = <org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.slf4j.Logger LOG>;
/*628*/

        $stack79 = l15 instanceof edu.brown.cs.systems.xtrace.wrappers.Slf4jLoggerWrapper;
/*628*/

        if $stack79 == 0 goto label18;
/*628*/

        $stack80 = staticinvoke <edu.brown.cs.systems.xtrace.logging.LoggerWrappers: edu.brown.cs.systems.xtrace.logging.LoggerWrappers aspectOf()>();
/*628*/

        $stack82 = (edu.brown.cs.systems.xtrace.wrappers.Slf4jLoggerWrapper) l15;
/*628*/

        $stack81 = <org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_18>;
/*628*/

        virtualinvoke $stack80.<edu.brown.cs.systems.xtrace.logging.LoggerWrappers: void ajc$before$edu_brown_cs_systems_xtrace_logging_LoggerWrappers$6$f79db3cf(edu.brown.cs.systems.xtrace.wrappers.Slf4jLoggerWrapper,org.aspectj.lang.JoinPoint$StaticPart)>($stack82, $stack81);
/*628*/

     label18:
        interfaceinvoke l15.<org.slf4j.Logger: void error(java.lang.String,java.lang.Object,java.lang.Object)>("{} exiting because of exception ", l13, l12);
/*628*/

     label19:
        l16 = this;
/*630*/
/*630*/

        l18 = <org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.slf4j.Logger LOG>;
/*630*/
/*630*/

        $stack40 = l18 instanceof edu.brown.cs.systems.xtrace.wrappers.Slf4jLoggerWrapper;
/*630*/

        if $stack40 == 0 goto label20;
/*630*/

        $stack51 = staticinvoke <edu.brown.cs.systems.xtrace.logging.LoggerWrappers: edu.brown.cs.systems.xtrace.logging.LoggerWrappers aspectOf()>();
/*630*/

        $stack53 = (edu.brown.cs.systems.xtrace.wrappers.Slf4jLoggerWrapper) l18;
/*630*/

        $stack52 = <org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_19>;
/*630*/

        virtualinvoke $stack51.<edu.brown.cs.systems.xtrace.logging.LoggerWrappers: void ajc$before$edu_brown_cs_systems_xtrace_logging_LoggerWrappers$6$f79db3cf(edu.brown.cs.systems.xtrace.wrappers.Slf4jLoggerWrapper,org.aspectj.lang.JoinPoint$StaticPart)>($stack53, $stack52);
/*630*/

     label20:
        interfaceinvoke l18.<org.slf4j.Logger: void info(java.lang.String,java.lang.Object)>("{} exiting.", l16);
/*630*/

        $stack41 = this.<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: java.util.List blockIters>;
/*632*/

        l2 = interfaceinvoke $stack41.<java.util.List: java.util.Iterator iterator()>();
/*632*/
/*632*/

        goto label22;
/*632*/

     label21:
        $stack44 = interfaceinvoke l2.<java.util.Iterator: java.lang.Object next()>();
/*632*/

        iter#34 = (org.apache.hadoop.hdfs.server.datanode.fsdataset.FsVolumeSpi$BlockIterator) $stack44;
/*632*/

        specialinvoke this.<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: void saveBlockIterator(org.apache.hadoop.hdfs.server.datanode.fsdataset.FsVolumeSpi$BlockIterator)>(iter#34);
/*633*/

        $stack45 = newarray (java.io.Closeable)[1];
/*634*/

        $stack45[0] = iter#34;
/*634*/

        $r0 = (java.io.Closeable[]) $stack45;

        staticinvoke <org.apache.hadoop.io.IOUtils: void cleanup(org.apache.commons.logging.Log,java.io.Closeable[])>(null, $r0);
/*634*/

     label22:
        $stack43 = interfaceinvoke l2.<java.util.Iterator: boolean hasNext()>();
/*632*/

        if $stack43 != 0 goto label21;
/*632*/

        goto label24;
/*636*/

     label23:
        $stack75 := @caughtexception;
/*610*/

        l5 = $stack75;
/*610*/

        $stack76 = newarray (java.io.Closeable)[1];
/*639*/

        $stack77 = this.<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.apache.hadoop.hdfs.server.datanode.fsdataset.FsVolumeReference ref>;
/*639*/

        $stack76[0] = $stack77;
/*639*/

        $r1 = (java.io.Closeable[]) $stack76;

        staticinvoke <org.apache.hadoop.io.IOUtils: void cleanup(org.apache.commons.logging.Log,java.io.Closeable[])>(null, $r1);
/*639*/

        throw l5;
/*640*/

     label24:
        $stack47 = newarray (java.io.Closeable)[1];
/*639*/

        $stack48 = this.<org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.apache.hadoop.hdfs.server.datanode.fsdataset.FsVolumeReference ref>;
/*639*/

        $stack47[0] = $stack48;
/*639*/

        $r2 = (java.io.Closeable[]) $stack47;

        staticinvoke <org.apache.hadoop.io.IOUtils: void cleanup(org.apache.commons.logging.Log,java.io.Closeable[])>(null, $r2);
/*639*/

        goto label26;
/*641*/

     label25:
        $stack72 := @caughtexception;
/*610*/

        l19 = $stack72;
/*610*/

        $stack73 = staticinvoke <edu.brown.cs.systems.tracing.aspects.RunnablesCallablesThreads: edu.brown.cs.systems.tracing.aspects.RunnablesCallablesThreads aspectOf()>();
/*610*/

        $stack74 = <org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_20>;
/*610*/

        virtualinvoke $stack73.<edu.brown.cs.systems.tracing.aspects.RunnablesCallablesThreads: void ajc$after$edu_brown_cs_systems_tracing_aspects_RunnablesCallablesThreads$9$d5fbf97f(edu.brown.cs.systems.tracing.aspects.RunnablesCallablesThreads$InstrumentedExecution,org.aspectj.lang.JoinPoint$StaticPart)>(this, $stack74);
/*610*/

        throw l19;
/*610*/

     label26:
        $stack49 = staticinvoke <edu.brown.cs.systems.tracing.aspects.RunnablesCallablesThreads: edu.brown.cs.systems.tracing.aspects.RunnablesCallablesThreads aspectOf()>();
/*641*/

        $stack50 = <org.apache.hadoop.hdfs.server.datanode.VolumeScanner: org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_20>;
/*641*/

        virtualinvoke $stack49.<edu.brown.cs.systems.tracing.aspects.RunnablesCallablesThreads: void ajc$after$edu_brown_cs_systems_tracing_aspects_RunnablesCallablesThreads$9$d5fbf97f(edu.brown.cs.systems.tracing.aspects.RunnablesCallablesThreads$InstrumentedExecution,org.aspectj.lang.JoinPoint$StaticPart)>(this, $stack50);
/*641*/

        return;
/*641*/

        catch java.lang.Throwable from label06 to label07 with label12;
        catch java.lang.Throwable from label08 to label09 with label12;
        catch java.lang.Throwable from label10 to label11 with label12;
        catch java.lang.Throwable from label12 to label13 with label12;
        catch java.lang.InterruptedException from label04 to label15 with label15;
        catch java.lang.Throwable from label04 to label15 with label17;
        catch java.lang.Throwable from label02 to label23 with label23;
        catch java.lang.Throwable from label01 to label25 with label25;
    }

