package com.iKeirNez.PluginMessageApiPlus;

import com.iKeirNez.PluginMessageApiPlus.packets.bungeecord.PacketForward;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by iKeirNez on 12/12/13.
 */
public abstract class PacketManager {

    private final String channel, forwardChannel;

    private HashMap<Class<? extends StandardPacket>, List<PacketListener>> packetListeners = new HashMap<>();
    private List<StandardPacket> sendQueue = new ArrayList<>();

    public PacketManager(String channel){
        this.channel = channel;
        this.forwardChannel = channel + "-fwd";
    }

    /**
     * Gets the channel this instance is tuned into
     * @return The channel this instance is tuned into
     */
    public String getChannel(){
        return channel;
    }

    /**
     * Gets the channel that all forward operations will be executed on
     * @return The forward channel
     */
    public String getForwardChannel(){
        return forwardChannel;
    }

    /**
     * Registers a packet, this allows the packet to be sent or received
     * A {@link RawPacket} must have an empty constructor (no parameters)
     * in order to function
     * @param packet The packet to be registered
     * @deprecated Packet registering system removed
     */
    public void registerPacket(Class<? extends StandardPacket> packet){
    }

    /**
     * Un-registers a packet, this means the packet can no longer be sent or receieved
     * @param packet The packet to be un-registered
     * @deprecated Packet registering system removed
     */
    public void unregisterPacket(Class<? extends StandardPacket> packet){
    }

    /**
     * Checks if a a packet is registered
     * @param packet The packet to check if registered
     * @return That registered status of the packet
     * @deprecated Packet registering system removed
     */
    public boolean isPacketRegistered(StandardPacket packet){
        return true;
    }

    /**
     * Checks if a a packet is registered
     * @param packet The packet to check if registered
     * @return That registered status of the packet
     * @deprecated Packet registering system removed
     */
    public boolean isPacketRegistered(Class<? extends StandardPacket> packet){
        return true;
    }

    /**
     * Registers a new listener, this class will be able to receive from packets it listens to
     * @param packetListener The listener to be registered
     */
    public void registerListener(final PacketListener packetListener){
        for (Method method : packetListener.getClass().getMethods()){
            Class<?>[] parameters = method.getParameterTypes();

            if (parameters.length == 1 && StandardPacket.class.isAssignableFrom(parameters[0])){
                Class<? extends StandardPacket> parameter = (Class<? extends StandardPacket>) parameters[0];

                if (!packetListeners.containsKey(parameter)){
                    packetListeners.put(parameter, new ArrayList<PacketListener>(){
                        {
                            add(packetListener);
                        }
                    });
                } else {
                    List<PacketListener> existingList = packetListeners.get(parameter);
                    existingList.add(packetListener);
                    packetListeners.put(parameter, existingList);
                }
            }
        }
    }

    /**
     * Un-registers a listener, this class will no longer be able to receieve packets
     * @param packetListener The listener to be un-registered
     */
    public void unregisterListener(PacketListener packetListener){
        Iterator<Class<? extends StandardPacket>> iterator = packetListeners.keySet().iterator();
        while (iterator.hasNext()){
            Class<? extends StandardPacket> clazz = iterator.next();
            List<PacketListener> list = packetListeners.get(clazz);

            if (list.contains(packetListener)){
                list.remove(packetListener);
                packetListeners.put(clazz, list);
            }
        }
    }

    /**
     * Dispatches an incoming packet to listeners
     * @param packetPlayer The player whom this packet was received from
     * @param bytes The byte array containing data
     */
    public void dispatchIncomingPacket(PacketPlayer packetPlayer, byte[] bytes){
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bytes));
        doPacket(packetPlayer, dataInputStream);
    }

    /**
     * Dispatches an incoming forwarded packet to listeners
     * PLEASE NOTE the {@link com.iKeirNez.PluginMessageApiPlus.PacketPlayer} is not the "REAL" sender
     * as this packet has been forwarded from another server
     * @param packetPlayer The player whom this packet was received from
     * @param bytes The byte array containing data
     */
    public void dispatchIncomingForwardPacket(PacketPlayer packetPlayer, byte[] bytes){
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bytes));

        try {
            if (dataInputStream.readUTF().equals(getForwardChannel())){
                short length = dataInputStream.readShort();
                byte[] msgBytes = new byte[length];
                dataInputStream.readFully(msgBytes);

                DataInputStream realDataInputStream = new DataInputStream(new ByteArrayInputStream(msgBytes));
                doPacket(packetPlayer, realDataInputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doPacket(PacketPlayer packetPlayer, DataInputStream dataInputStream){
        Class<? extends StandardPacket> packetClazz = null;

        try {
            Class<?> clazz = Class.forName(dataInputStream.readUTF());

            if (StandardPacket.class.isAssignableFrom(clazz)){
                packetClazz = (Class<? extends StandardPacket>) clazz;

                StandardPacket packet = packetClazz.newInstance();
                packet.sender = packetPlayer;
                packet.handle(dataInputStream);

                List<PacketListener> listeners = packetListeners.get(packetClazz);
                if (listeners != null){
                    for (PacketListener packetListener : listeners){
                        for (Method method : packetListener.getClass().getMethods()){
                            if (method.isAnnotationPresent(PacketHandler.class)){
                                Class<?>[] parameters = method.getParameterTypes();

                                if (parameters.length == 1 && parameters[0].equals(packetClazz)){
                                    try {
                                        method.invoke(packetListener, packet);
                                    } catch (InvocationTargetException e) {
                                        System.out.println("Error whilst passing packet to listener " + packetListener.getClass() + "#" + method.getName());
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e){
        } catch (Throwable e){
            System.out.println("Error whilst receiving packet " + packetClazz != null ? packetClazz.getSimpleName() : "");
            e.printStackTrace();
        }
    }

    private String figureChannel(StandardPacket packet){
        String sendChannel = getChannel();

        if (packet instanceof RawPacket){
            sendChannel = ((RawPacket) packet).getChannel();

            if (sendChannel == null){
                sendChannel = getChannel();
            }
        }

        return sendChannel;
    }

    protected void playerJoined(PacketPlayer packetPlayer){
        for (StandardPacket packet : sendQueue){
            sendPacket(packetPlayer, packet);
        }

        sendQueue.clear();
    }

    private void addToQueue(StandardPacket packet){
        sendQueue.add(packet);
    }

    /**
     * Sends a packet via any available means
     * @param packet The packet to send
     */
    public void sendPacket(StandardPacket packet){
        sendPacket(null, packet, true);
    }

    /**
     * Sends a packet via a player
     * @param packetPlayer The player whom this should be sent to
     * @param packet The packet to be sent to the player
     */
    public void sendPacket(PacketPlayer packetPlayer, StandardPacket packet){
        sendPacket(packetPlayer, packet, false);
    }

    /**
     * Sends a packet via a player
     * @param packetPlayer The player whom this should be sent to
     * @param packet The packet to be sent to the player
     * @param random Send via a random player if packetPlayer is null
     */
    public void sendPacket(PacketPlayer packetPlayer, StandardPacket packet, boolean random){
        try {
            if (packet instanceof PacketForward){ // fix for forward packets
                PacketForward packetForward = (PacketForward) packet;

                if (packetForward.channel == null){
                    packetForward.channel = getForwardChannel();
                }
            }

            if (packetPlayer == null){
                if (getPlayerCount() > 0){
                    sendPluginMessage(getRandomPlayer(), figureChannel(packet), packet.write().toByteArray());
                } else if (random) {
                    addToQueue(packet);
                }
            } else {
                sendPluginMessage(packetPlayer, figureChannel(packet), packet.write().toByteArray());
            }
        } catch (IOException e) {
            System.out.println("Error whilst writing packet " + getClass().getSimpleName());
            e.printStackTrace();
        }
    }

    protected abstract void sendPluginMessage(PacketPlayer packetPlayer, String channel, byte[] bytes);
    protected abstract int getPlayerCount();
    protected abstract PacketPlayer getRandomPlayer();

}
