package com.iKeirNez.PluginMessageApiPlus.packets.bungeecord;

import com.iKeirNez.PluginMessageApiPlus.PacketWriter;
import com.iKeirNez.PluginMessageApiPlus.RawPacket;

import java.io.IOException;

/**
 * Created by iKeirNez on 01/01/14.
 */
public class PacketUUIDOther extends RawPacket {

    public String name;

    public PacketUUIDOther(String name){
        super("BungeeCord");
        this.name = name;
    }

    @Override
    protected PacketWriter write() throws IOException {
        PacketWriter packetWriter = new PacketWriter(this);
        packetWriter.writeUTF("UUIDOther");
        packetWriter.writeUTF(name);
        return packetWriter;
    }
}
