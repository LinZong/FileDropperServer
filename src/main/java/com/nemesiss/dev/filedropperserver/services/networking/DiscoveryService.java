package com.nemesiss.dev.filedropperserver.services.networking;

import com.nemesiss.dev.filedropperserver.models.command.CommandReply;
import com.nemesiss.dev.filedropperserver.models.command.CommandRequest;
import com.nemesiss.dev.filedropperserver.models.command.SupportCommand;
import com.nemesiss.dev.filedropperserver.models.configuration.ServerConfiguration;
import com.nemesiss.dev.filedropperserver.models.discoveryservice.DiscoveryStatus;
import com.nemesiss.dev.filedropperserver.models.discoveryservice.MachineInfo;
import com.nemesiss.dev.filedropperserver.models.discoveryservice.NetworkInterfaceCandidate;
import com.nemesiss.dev.filedropperserver.services.FileDropperCore;
import com.nemesiss.dev.filedropperserver.utils.NetworkUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DiscoveryService {

    private final ServerConfiguration serverConfiguration;

    private final FileDropperCore fileDropperCore;

    private final LinkedHashMap<MachineInfo, Integer> DISCOVERY_RES_MACHINE = new LinkedHashMap<>();

    private MachineInfo selfMachineInfo = null;

    private List<NetworkInterfaceCandidate> bindNicCandidates = NetworkUtils.resolveNicCandidates();

    private EventLoopGroup discoveryEventLoopGroup = null;

    private ScheduledFuture<?> sendDiscoveryPacketTask = null;

    private static final int MACHINE_DIED_THRESHOLD = 5;


    public DiscoveryService(ServerConfiguration serverConfiguration, FileDropperCore fileDropperCore) {
        this.serverConfiguration = serverConfiguration;
        this.fileDropperCore = fileDropperCore;

        fileDropperCore.registerHandler(SupportCommand.BEGIN_DISCOVERY, this::handleDiscoveryBegin);
        fileDropperCore.registerHandler(SupportCommand.STOP_DISCOVERY, this::handleDiscoveryStop);
        fileDropperCore.registerHandler(SupportCommand.DISCOVERY_RESULT, this::handleGetDiscoveryResult);
    }

    private void maintainAliveMachine() {
        synchronized (DISCOVERY_RES_MACHINE) {
            List<MachineInfo> diedMachine = new ArrayList<>();
            for (Map.Entry<MachineInfo, Integer> entry : DISCOVERY_RES_MACHINE.entrySet()) {
                int age = entry.getValue() + 1;
                if (age == MACHINE_DIED_THRESHOLD) {
                    logNewMachineDied(entry.getKey());
                    diedMachine.add(entry.getKey());
                } else {
                    DISCOVERY_RES_MACHINE.put(entry.getKey(), age);
                }
            }
            diedMachine.forEach(DISCOVERY_RES_MACHINE::remove);
        }
    }

    /**
     * 通知Netty开始网络发现
     */
    private boolean startNettyBroadcastLoop() {

        stopNettyBroadcastLoop();

        log.info("Starting down broadcast discovery...");

        discoveryEventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(discoveryEventLoopGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        // Invoker -> MachineInfoDecoder -> MachineInfoAdder
                        // MachineInfoEncoder
                        pipeline.addFirst(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                Channel channel = ctx.channel();
                                log.info("Broadcast discovery started.");
                                sendDiscoveryPacketTask =
                                        channel.eventLoop()
                                                .scheduleAtFixedRate(() -> {
                                                            maintainAliveMachine();
                                                            channel.writeAndFlush(selfMachineInfo);
                                                        },
                                                        0,
                                                        serverConfiguration.getDiscoveryTimePeriod(),
                                                        TimeUnit.MILLISECONDS);
                            }
                        });
                        pipeline.addLast(new MachineInfoDecoder());
                        pipeline.addLast(new SimpleChannelInboundHandler<MachineInfo>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, MachineInfo machineInfo) throws Exception {
                                if (machineInfo != null && !machineInfo.equals(selfMachineInfo)) {
                                    synchronized (DISCOVERY_RES_MACHINE) {
                                        if (!DISCOVERY_RES_MACHINE.containsKey(machineInfo)) {
                                            logNewMachineFound(machineInfo);
                                        }
                                        DISCOVERY_RES_MACHINE.put(machineInfo, 0);
                                    }
                                }
                            }
                        });
                        pipeline.addLast(new MachineInfoEncoder(serverConfiguration.getDiscoveryPort()));
                    }
                });

        try {
            bootstrap.bind(selfMachineInfo.getMachineIp(), serverConfiguration.getDiscoveryPort()).sync();
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isDiscovering() {
        return discoveryEventLoopGroup != null &&
                !discoveryEventLoopGroup.isTerminated() &&
                !discoveryEventLoopGroup.isShutdown() &&
                !discoveryEventLoopGroup.isShuttingDown();
    }

    private void stopNettyBroadcastLoop() {
        // Stop netty broadcasting.
        if (isDiscovering()) {
            try {
                log.info("Shutting down broadcast discovery...");
                if (sendDiscoveryPacketTask != null) {
                    // 给50次自旋机会让它取消任务,否则强行中断.
                    boolean canceled = false;
                    for (int i = 0; i < 50; ++i) {
                        if (sendDiscoveryPacketTask.cancel(false)) {
                            canceled = true;
                            break;
                        }
                    }
                    // 强行停止发送任务
                    if (!canceled) {
                        log.info("Force shutting down discovery packet sender...");
                        sendDiscoveryPacketTask.cancel(true);
                    }
                    discoveryEventLoopGroup.shutdownGracefully().await();
                }
                log.info("Broadcast discovery stop successfully.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public CommandReply handleGetDiscoveryResult(CommandRequest command) {
        synchronized (DISCOVERY_RES_MACHINE) {
            MachineInfo[] machineInfos = DISCOVERY_RES_MACHINE.keySet().toArray(new MachineInfo[0]);
            return new CommandReply(command.getCommand(), true, new DiscoveryStatus(isDiscovering(), machineInfos), null);
        }
    }

    public CommandReply handleDiscoveryBegin(CommandRequest command) {

        // Begin broadcast package in discovery port and listening outer discovery package come.
        // 如果此时尚未选出Candidate
        //      如果没有Candidate，报错
        //      否则，挑选第一个Candidate开始广播

        if (selfMachineInfo == null) {
            if (bindNicCandidates.isEmpty()) {
                log.error("DiscoveryService: Cannot find any NIC to send discovery packet.");
                return new CommandReply(command.getCommand(), false, "No NIC can be used to send discovery packet.", null);
            }
            NetworkInterfaceCandidate firstCandidate = bindNicCandidates.get(0);
            log.warn("Use the first candidate NIC to send discovery packet. {}", firstCandidate);
            selfMachineInfo = new MachineInfo(
                    serverConfiguration.getMachineName(),
                    firstCandidate.getNicIpAddr(),
                    serverConfiguration.getManagePort(),
                    serverConfiguration.getTransferTcpPort());
        }

        if (startNettyBroadcastLoop()) {
            return new CommandReply(command.getCommand(), true, CommandReply.SUCCESS_REPLY, null);
        }
        return new CommandReply(command.getCommand(), false, "Cannot launch discovery service", null);
    }

    public CommandReply handleDiscoveryStop(CommandRequest command) {
        stopNettyBroadcastLoop();
        DISCOVERY_RES_MACHINE.clear();
        return new CommandReply(command.getCommand(), true, CommandReply.SUCCESS_REPLY, null);
    }

    public static void logNewMachineFound(MachineInfo machineInfo) {
        log.info("New Machine Found: {}", machineInfo);
    }

    public static void logNewMachineDied(MachineInfo machineInfo) {
        log.info("Machine Died: {}", machineInfo);
    }
}
