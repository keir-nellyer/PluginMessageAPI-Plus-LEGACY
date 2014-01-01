package com.iKeirNez.PluginMessageApiPlus.packets.bungeecord;

import com.iKeirNez.PluginMessageApiPlus.PacketWriter;
import com.iKeirNez.PluginMessageApiPlus.RawPacket;

import java.io.IOException;

/**
 * Created by iKeirNez on 01/01/14.
 */
public class PacketPlayerCount extends RawPacket {

    public String server;

    public PacketPlayerCount(String server){
        super("BungeeCord");
        this.server = server;
    }

    @Override
    protected PacketWriter write() throws IOException {
        PacketWriter packetWriter = new PacketWriter(this);
        packetWriter.writeUTF("PlayerCount");
        packetWriter.writeUTF(server);
        return packetWriter;
    }
}
