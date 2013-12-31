package com.iKeirNez.PluginMessageApiPlus.implementations;

import com.iKeirNez.PluginMessageApiPlus.PacketManager;
import com.iKeirNez.PluginMessageApiPlus.PacketPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

/**
 * Created by iKeirNez on 24/12/13.
 */
public class BukkitPacketManager extends PacketManager implements PluginMessageListener {

    private JavaPlugin bukkitPlugin;

    public BukkitPacketManager(JavaPlugin bukkitPlugin, String channel){
        super(channel);
        this.bukkitPlugin = bukkitPlugin;

        Messenger messenger = Bukkit.getMessenger();

        messenger.registerOutgoingPluginChannel(bukkitPlugin, channel);
        messenger.registerIncomingPluginChannel(bukkitPlugin, channel, this);
        messenger.registerIncomingPluginChannel(bukkitPlugin, "BungeeCord", this);
        messenger.registerOutgoingPluginChannel(bukkitPlugin, "BungeeCord");
    }

    protected void sendPluginMessage(PacketPlayer packetPlayer, String channel, byte[] bytes) {
        packetPlayer.getBukkitPlayer().sendPluginMessage(bukkitPlugin, channel, bytes);
    }

    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (channel.equals(getChannel())){
            dispatchIncomingPacket(new PacketPlayer(player), bytes);
        } else if (channel.equals("BungeeCord")){
            dispatchIncomingForwardPacket(new PacketPlayer(player), bytes);
        }
    }
}
