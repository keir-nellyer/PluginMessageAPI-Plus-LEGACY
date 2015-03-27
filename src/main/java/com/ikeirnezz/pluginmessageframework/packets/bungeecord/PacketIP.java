package com.ikeirnezz.pluginmessageframework.packets.bungeecord;

import com.ikeirnezz.pluginmessageframework.PacketWriter;
import com.ikeirnezz.pluginmessageframework.RawPacket;

import java.io.IOException;

/**
 * Created by iKeirNez on 01/01/14.
 */
public class PacketIP extends RawPacket {

    public PacketIP(){
        super("BungeeCord");
    }

    @Override
    public PacketWriter write() throws IOException {
        PacketWriter packetWriter = new PacketWriter(this);
        packetWriter.writeUTF("IP");
        return packetWriter;
    }
}
