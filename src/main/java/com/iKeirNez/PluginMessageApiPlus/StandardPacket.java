package com.iKeirNez.PluginMessageApiPlus;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by iKeirNez on 11/12/13.
 */
public abstract class StandardPacket {

    protected PacketPlayer sender;

    /**
     * Gets the player this packet was received from
     * PLEASE NOTE for forwarded packets this may not be the player that you send the packet via, it will just be
     * the first player found the could deliver that packet
     * @return The player this packet was received from
     */
    public PacketPlayer getSender(){
        return sender;
    }

    /**
     * When a packet is received, data is passed through here. The packet class must cache data ready for the instance to be passed t listeners
     * @param dataInputStream The data received
     * @throws java.io.IOException Thrown if there is an error handling the packet
     */
    protected abstract void handle(DataInputStream dataInputStream) throws IOException;

    /**
     * This function is called when a packet is being prepared to be sent, the function must write cached values to a PacketWriter and return it
     * @return The PacketWriter containing all data ready to send
     * @throws java.io.IOException Thrown if there is an error writing this packet
     */
    protected abstract PacketWriter write() throws IOException;

}
