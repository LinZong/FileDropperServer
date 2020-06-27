package com.nemesiss.dev.filedropperserver.services.networking;

import com.nemesiss.dev.filedropperserver.models.discoveryservice.MachineInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.util.List;

public class MachineInfoEncoder extends MessageToMessageEncoder<MachineInfo> {

    private int port;
    private InetSocketAddress BROADCAST_ENDPOINT;

    public MachineInfoEncoder(int port) {
        this.port = port;
        BROADCAST_ENDPOINT = new InetSocketAddress("255.255.255.255", port);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, MachineInfo msg, List<Object> out) throws Exception {
        byte[] packetBytes = msg.toDiscoveryBytePacket();
        ByteBuf buffer = ctx.alloc().buffer(packetBytes.length);
        buffer.writeBytes(packetBytes);
        out.add(new DatagramPacket(buffer, BROADCAST_ENDPOINT));
        ReferenceCountUtil.release(msg);
    }
}
