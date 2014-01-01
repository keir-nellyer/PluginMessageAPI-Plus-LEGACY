package com.iKeirNez.PluginMessageApiPlus.packets.bungeecord;

import com.iKeirNez.PluginMessageApiPlus.PacketWriter;
import com.iKeirNez.PluginMessageApiPlus.RawPacket;

import java.io.IOException;

/**
 * Created by iKeirNez on 01/01/14.
 */
public class PacketMessage extends RawPacket {

    public String name;
    public String message;

    public PacketMessage(String name, String message){
        super("BungeeCord");
        this.name = name;
        this.message = message;
    }

    @Override
    protected PacketWriter write() throws IOException {
        PacketWriter packetWriter = new PacketWriter(this);
        packetWriter.writeUTF("Message");
        packetWriter.writeUTF(name);
        packetWriter.writeUTF(message);
        return packetWriter;
    }
}
