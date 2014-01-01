package com.iKeirNez.PluginMessageApiPlus.packets.bungeecord;

import com.iKeirNez.PluginMessageApiPlus.PacketWriter;
import com.iKeirNez.PluginMessageApiPlus.RawPacket;

import java.io.IOException;

/**
 * Created by iKeirNez on 01/01/14.
 */
public class PacketIP extends RawPacket {

    public PacketIP(){
        super("BungeeCord");
    }

    @Override
    protected PacketWriter write() throws IOException {
        PacketWriter packetWriter = new PacketWriter(this);
        packetWriter.writeUTF("IP");
        return packetWriter;
    }
}
