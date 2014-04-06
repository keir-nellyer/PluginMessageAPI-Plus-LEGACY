PluginMessageAPI+
=================

An extension of the Plugin Message API for Bukkit, BungeeCord

Links
-----

* [Bukkit Post](http://forums.bukkit.org/threads/api-pluginmessageapi.213649/)
* [Spigot Post](http://www.spigotmc.org/resources/pluginmessageapi.294/)

Example Usage
-------------

Initializing Example (Bukkit)

```java
public PacketManager packetManager;

public void onEnable(){
    packetManager = new BukkitPacketManager(this, "MyChannelName");
    packetManager.registerListener(new IncomingPacketHandler());
}
```


Initializing Example (BungeeCord)

```java
public PacketManager packetManager;

public void onEnable(){
    packetManager = new BungeeCordPacketManager(this, "MyChannelName");
    packetManager.registerListener(new IncomingPacketHandler());
}
```


Packet Class Example

```java
public class PacketPlayerUpdatePoints extends StandardPacket {

    public int points;

    public PacketPlayerUpdatePoints(){}

    public PacketPlayerUpdatePoints(int point){
        this.change = change;
    }

    public void handle(DataInputStream dataInputStream) throws IOException {
        this.point = dataInputStream.readInt();
    }

    public PacketWriter write() throws IOException {
        PacketWriter packetWriter = new PacketWriter(this);
        packetWriter.writeInt(points);
        return packetWriter;
    }
}
```


Sending Example (Bukkit, similar for BungeeCord)

```java
packetManager.sendPacket(new PacketPlayer(Bukkit.getOnlinePlayers()[0], new PacketPlayerUpdatePoints(50)));
```


Listener Example (Bukkit, same for BungeeCord)

```java
public class IncomingPacketHandler implements PacketListener {
    @PacketHandler
    public void onPointsUpdate(PacketPlayerUpdatePoints packet){
        Player player = packet.getSender().getBukkitPlayer();
        int points = packet.points;

        // do something to update points
    }
}
```

Plugins/Mods/Addons/Servers using this API
----------------------------------

* [CloudChat](http://www.spigotmc.org/resources/cloudchat.266/)