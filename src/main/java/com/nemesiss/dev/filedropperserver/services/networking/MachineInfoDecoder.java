package com.nemesiss.dev.filedropperserver.services.networking;

import com.nemesiss.dev.filedropperserver.models.discoveryservice.MachineInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class MachineInfoDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        ByteBuf buffer = msg.content();
        byte[] packet;
        if (buffer.hasArray()) {
            packet = buffer.array();

        } else {
            int length = buffer.readableBytes();
            packet = new byte[length];
            buffer.readBytes(packet);
        }
        MachineInfo machineInfo = MachineInfo.convertBytePacketToMachineInfo(packet);
        if (machineInfo != null) {
            out.add(machineInfo);
        }
    }
}
