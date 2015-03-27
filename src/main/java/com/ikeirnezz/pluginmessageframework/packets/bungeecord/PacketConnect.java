package com.ikeirnezz.pluginmessageframework.packets.bungeecord;

import com.ikeirnezz.pluginmessageframework.PacketWriter;
import com.ikeirnezz.pluginmessageframework.RawPacket;

import java.io.IOException;

/**
 * Created by iKeirNez on 01/01/14.
 */
public class PacketConnect extends RawPacket {

    public String server;

    public PacketConnect(String server){
        super("BungeeCord");
        this.server = server;
    }

    @Override
    public PacketWriter write() throws IOException {
        PacketWriter packetWriter = new PacketWriter(this);
        packetWriter.writeUTF("Connect");
        packetWriter.writeUTF(server);
        return packetWriter;
    }
}
