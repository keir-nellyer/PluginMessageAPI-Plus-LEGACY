PluginMessageAPI+
=================

An extension of the Plugin Message API for Bukkit, BungeeCord

Links
-----

* [Bukkit Post](http://forums.bukkit.org/threads/api-pluginmessageapi.213649/)

Upcoming Features
-----------------

* Forward packet for BungeeCord

Example Usage
-------------

A lot of these examples are written for Bukkit but they aren't too hard to translate to BungeeCord


Initializing Example (Bukkit)

```java
public PacketManager packetManager;

public void onEnable(){
    packetManager = new BukkitPacketManager(this, "MyChannelName");
    packetManager.registerPacket(PacketPlayerUpdatePoints.class);
    packetManager.registerListener(new IncomingPacketHandler());
}
```


Initializing Example (BungeeCord)

```java
public PacketManager packetManager;

public void onEnable(){
    packetManager = new BungeeCordPacketManager(this, "MyChannelName");
    packetManager.registerPacket(PacketPlayerUpdatePoints.class);
    packetManager.registerListener(new IncomingPacketHandler());
}
```


Packet Class Example

```java
public class PacketPlayerUpdatePoints extends Packet {

    public int points;

    public PacketPlayerUpdatePoints(){}

    public PacketPlayerUpdatePoints(int point){
        this.change = change;
    }

    protected void handle(DataInputStream dataInputStream) throws IOException {
        this.point = dataInputStream.readInt();
    }

    protected PacketWriter write() throws IOException {
        PacketWriter packetWriter = new PacketWriter(this);
        packetWriter.writeInt(points);
        return packetWriter;
    }
}
```


Sending Example (Bukkit)

```java
packetManager.sendPacket(new PacketPlayer(Bukkit.getOnlinePlayers()[0], new PacketPlayerUpdatePoints(50)));
```


Listener Example (Bukkit)

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