package com.nemesiss.dev.filedropperserver.models.discoveryservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.nemesiss.dev.filedropperserver.models.JsonSerializableObject;
import com.nemesiss.dev.filedropperserver.models.command.SupportByteMark;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachineInfo implements JsonSerializableObject {

    private String machineName;
    private InetAddress machineIp;
    private Integer managePort;
    private Integer transferTcpPort;


    @JSONField(serialize = false)
    private static final int FIXED_PART_LENGTH = 1 + 4 + 4;

    @Override
    public String toJsonString() {
        return JSON.toJSONString(this);
    }

    public byte[] toDiscoveryBytePacket() {
        // 发现标志 + IP地址 + 2字节管理端口 + 2字节TCP传输端口 + 剩余字节为机器名(UTF-8)
        byte[] machineNameBytes = machineName.getBytes();
        byte[] ipAddr = machineIp.getAddress();
        byte[] fixedLength = {
                SupportByteMark.DISCOVERY_PACKET,
                ipAddr[0], ipAddr[1], ipAddr[2], ipAddr[3],
                (byte) ((managePort >> 8) & 0xff),
                (byte) (managePort & 0xff),
                (byte) ((transferTcpPort >> 8) & 0xff),
                (byte) (transferTcpPort & 0xff)
        };
        byte[] packet = new byte[fixedLength.length + machineNameBytes.length];

        // 拼装要发送的数据包
        System.arraycopy(fixedLength, 0, packet, 0, fixedLength.length);
        System.arraycopy(machineNameBytes, 0, packet, fixedLength.length, machineNameBytes.length);
        return packet;
    }

    public static MachineInfo convertBytePacketToMachineInfo(byte[] packet) {
        if (ArrayUtils.isEmpty(packet)) {
            return null;
        }
        if (packet[0] != SupportByteMark.DISCOVERY_PACKET) {
            return null;
        }
        // 看看定长部分是否够长
        if (packet.length < FIXED_PART_LENGTH) {
            return null;
        }
        byte[] ipAddrPacket = {packet[1], packet[2], packet[3], packet[4]};
        try {
            InetAddress ipAddrObj = Inet4Address.getByAddress(ipAddrPacket);
            int mgmrPort = (packet[5] << 8) & 0xff00;
            mgmrPort |= (packet[6] & 0xfff);

            int tsfTcpPort = (packet[7] << 8) & 0xff00;
            tsfTcpPort |= packet[8] & 0xff;

            int remain = packet.length - FIXED_PART_LENGTH;
            String remoteHostName = new String(packet, FIXED_PART_LENGTH, remain, StandardCharsets.UTF_8);

            return new MachineInfo(remoteHostName, ipAddrObj, mgmrPort, tsfTcpPort);
        } catch (UnknownHostException e) {
            // ignored. never cause.
            return null;
        }
    }
}