package com.iKeirNez.PluginMessageApiPlus;

import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.entity.Player;

/**
 * Represents a BungeeCord or Bukkit player
 * Created by iKeirNez on 12/12/13.
 */
public class PacketPlayer {

    @Getter private ProxiedPlayer bungeePlayer = null;
    @Getter private Player bukkitPlayer = null;

    /**
     * Constructor used in the case of a BungeeCord player
     * @param bungeePlayer The BungeeCord player instance
     */
    public PacketPlayer(ProxiedPlayer bungeePlayer){
        this.bungeePlayer = bungeePlayer;
    }

    /**
     * Constructor used in the case of a Bukkit player
     * @param bukkitPlayer The Bukkit player instance
     */
    public PacketPlayer(Player bukkitPlayer){
        this.bukkitPlayer = bukkitPlayer;
    }

}