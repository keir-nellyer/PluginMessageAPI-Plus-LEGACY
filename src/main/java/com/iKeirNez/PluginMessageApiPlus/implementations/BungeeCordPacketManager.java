package com.iKeirNez.PluginMessageApiPlus.implementations;

import com.iKeirNez.PluginMessageApiPlus.PacketManager;
import com.iKeirNez.PluginMessageApiPlus.PacketPlayer;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

/**
 * Created by iKeirNez on 24/12/13.
 */
public class BungeeCordPacketManager extends PacketManager implements Listener {

    public BungeeCordPacketManager(Plugin bungeePlugin, String channel){
        super(channel);
        bungeePlugin.getProxy().getPluginManager().registerListener(bungeePlugin, this);
        bungeePlugin.getProxy().registerChannel(channel);
    }

    protected void sendPluginMessage(PacketPlayer packetPlayer, String channel, byte[] bytes) {
        packetPlayer.getBungeePlayer().getServer().sendData(channel, bytes);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e){
        String channel = e.getTag();

        if (e.getSender() instanceof ServerConnection && channel.equals(getChannel())){ // prevents players faking a plugin message
            ProxiedPlayer proxiedPlayer = (ProxiedPlayer) e.getReceiver();
            dispatchIncomingPacket(new PacketPlayer(proxiedPlayer), e.getData());
        }
    }
}
