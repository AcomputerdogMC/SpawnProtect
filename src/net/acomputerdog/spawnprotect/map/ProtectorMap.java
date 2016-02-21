package net.acomputerdog.spawnprotect.map;

import net.acomputerdog.spawnprotect.PluginSpawnProtect;
import net.acomputerdog.spawnprotect.protect.Protector;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class ProtectorMap {
    private final PluginSpawnProtect plugin;
    private final Map<World, Protector> worldMap;
    private final Map<String, Protector> uninitializedWorldMap;

    public ProtectorMap(PluginSpawnProtect plugin) {
        this.plugin = plugin;
        worldMap = new HashMap<>();
        uninitializedWorldMap = new HashMap<>();
    }

    public void addUninitializedWorld(String world, Protector protector) {
        uninitializedWorldMap.put(world, protector);
    }

    public void addWorld(World world, Protector protector) {
        worldMap.put(world, protector);
        uninitializedWorldMap.remove(world.getName());
    }

    public void removeWorld(World world) {
        worldMap.remove(world);
        uninitializedWorldMap.remove(world.getName());
    }

    public Protector getProtector(World world) {
        Protector protector = worldMap.get(world);
        if (protector == null) {
            protector = uninitializedWorldMap.get(world.getName());
            if (protector != null) {
                protector.setWorld(world);
            } else {
                protector = plugin.createDefaultProtector(world);
            }
            addWorld(world, protector);
        }
        return protector;
    }
}
