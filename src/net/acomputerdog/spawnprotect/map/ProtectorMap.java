package net.acomputerdog.spawnprotect.map;

import net.acomputerdog.spawnprotect.PluginSpawnProtect;
import net.acomputerdog.spawnprotect.protect.NonProtector;
import net.acomputerdog.spawnprotect.protect.Protector;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class ProtectorMap {
    private final PluginSpawnProtect plugin;
    private final Map<World, Protector> worldMap;

    public ProtectorMap(PluginSpawnProtect plugin) {
        this.plugin = plugin;
        worldMap = new HashMap<>();
    }

    public void addWorld(World world, Protector protector) {
        if (protector == null) {
            protector = createDefaultProtector(world);
        }
        worldMap.put(world, protector);
    }

    public void removeWorld(World world) {
        worldMap.remove(world);
    }

    public void setProtector(World world, Protector protector) {
        worldMap.put(world, protector);
    }

    public Protector getProtector(World world) {
        return worldMap.get(world);
    }

    private Protector createDefaultProtector(World world) {
        //todo move to PluginSpawnProtect?
        return new NonProtector(plugin, world);
    }
}
