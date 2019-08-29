package cn.dawnland.packdownload.netty.packet.response;

import cn.dawnland.packdownload.netty.packet.Command;
import cn.dawnland.packdownload.netty.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DownloadResponsePacket extends Packet {

    private String url;

    @Override
    public Byte getCommand() {
        return Command.DOWNLOAD_RESPONSE;
    }
}
