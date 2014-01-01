package com.iKeirNez.PluginMessageApiPlus;

import lombok.Delegate;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by iKeirNez on 12/12/13.
 */
public class PacketWriter {

    private final StandardPacket packet;

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    @Delegate private DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

    /**
     * Creates a new instance of this class
     * @param packet The packet that is writing to this instance
     */
    public PacketWriter(StandardPacket packet){
        this.packet = packet;

        try {
            dataOutputStream.writeUTF(packet.getClass().getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] toByteArray(){
        return byteArrayOutputStream.toByteArray();
    }

}
