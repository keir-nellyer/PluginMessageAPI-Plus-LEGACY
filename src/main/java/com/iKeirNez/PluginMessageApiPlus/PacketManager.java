package com.iKeirNez.PluginMessageApiPlus;

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

    private List<Class<? extends StandardPacket>> registeredPackets = new ArrayList<>();
    private HashMap<Class<? extends StandardPacket>, List<PacketListener>> packetListeners = new HashMap<>();

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
     */
    public void registerPacket(Class<? extends StandardPacket> packet){
        try {
            packet.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Packet " + packet.getName() + " does not have an empty constructor (no parameters)");
        }

        registeredPackets.add(packet);
    }

    /**
     * Un-registers a packet, this means the packet can no longer be sent or receieved
     * @param packet The packet to be un-registered
     */
    public void unregisterPacket(Class<? extends StandardPacket> packet){
        registeredPackets.remove(packet);
    }

    /**
     * Checks if a a packet is registered
     * @param packet The packet to check if registered
     * @return That registered status of the packet
     */
    public boolean isPacketRegistered(StandardPacket packet){
        return isPacketRegistered(packet.getClass());
    }

    /**
     * Checks if a a packet is registered
     * @param packet The packet to check if registered
     * @return That registered status of the packet
     */
    public boolean isPacketRegistered(Class<? extends StandardPacket> packet){
        return registeredPackets.contains(packet) || packet.getName().startsWith("com.iKeirNez.PluginMessageApiPlus.packets.");
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

                if (registeredPackets.contains(parameter)){
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
                if (!isPacketRegistered(packetClazz)){
                    throw new RuntimeException("Cannot receive unregistered packet " + packetClazz.getName());
                }

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

    private String figureChannel(StandardPacket packet, boolean forward){
        String sendChannel = getChannel();

        if (forward){
            sendChannel = getForwardChannel();
        } else if (packet instanceof RawPacket){
            sendChannel = ((RawPacket) packet).getChannel();

            if (sendChannel == null){
                sendChannel = getChannel();
            }
        }

        return sendChannel;
    }

    /**
     * Sends a packet via a player
     * @param packetPlayer The player whom this should be sent to
     * @param packet The packet to be sent to the player
     */
    public void sendPacket(PacketPlayer packetPlayer, StandardPacket packet){
        if (!isPacketRegistered(packet)){
            throw new RuntimeException("Tried to send unregistered packet " + packet.getClass().getName());
        }

        try {
            sendPluginMessage(packetPlayer, figureChannel(packet, false), packet.write().toByteArray());
        } catch (IOException e) {
            System.out.println("Error whilst writing packet " + getClass().getSimpleName());
            e.printStackTrace();
        }
    }

    protected abstract void sendPluginMessage(PacketPlayer packetPlayer, String channel, byte[] bytes);

    /**
     * Sends a packet in a forward method to the specified server
     * PLEASE NOTE: There will be issues when using this with a packet that is not intended to be forwarded
     * IE the sender/receiver of the packet is not the REAL sender/receiver, it is just the first player BungeeCord could send the packet via
     * @param packetPlayer The player whom this packet should be sent via
     * @param packet The packet to be sent to the specified server
     * @param server The server the packet should be sent to
     */
    public void sendForwardPacket(PacketPlayer packetPlayer, StandardPacket packet, String server){
        if (!isPacketRegistered(packet)){
            throw new RuntimeException("Tried to send unregistered packet " + packet.getClass().getName());
        }

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeUTF("Forward");
            dataOutputStream.writeUTF(server);
            dataOutputStream.writeUTF(figureChannel(packet, true));

            byte[] msgBytes = packet.write().toByteArray();

            dataOutputStream.writeShort(msgBytes.length);
            dataOutputStream.write(msgBytes);

            sendPluginMessage(packetPlayer, "BungeeCord", byteArrayOutputStream.toByteArray());
        } catch (IOException e){
            System.out.println("Error whilst writing forward packet " + getClass().getSimpleName());
            e.printStackTrace();
        }
    }

    /**
     * Sends a packet in a forward method to all servers linked to the BungeeCord server
     * PLEASE NOTE: There will be issues when using this with a packet that is not intended to be forwarded
     * IE the sender/receiver of the packet is not the REAL sender/receiver, it is just the first player BungeeCord could send the packet via
     * @param packetPlayer The player whom this packet should be sent via
     * @param packet The packet to be sent to the specified server
     */
    public void sendForwardAllPacket(PacketPlayer packetPlayer, StandardPacket packet){
        sendForwardPacket(packetPlayer, packet, "ALL");
    }

}
