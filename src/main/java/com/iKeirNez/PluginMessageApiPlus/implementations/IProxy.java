package com.iKeirNez.PluginMessageApiPlus.implementations;

/**
 * Created by iKeirNez on 18/02/14.
 */
public interface IProxy {

    public void sendPluginMessage(String server, String channel, byte[] bytes);

}
