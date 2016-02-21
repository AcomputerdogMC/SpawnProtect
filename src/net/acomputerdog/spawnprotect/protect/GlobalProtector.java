package net.acomputerdog.spawnprotect.protect;

import net.acomputerdog.spawnprotect.PluginSpawnProtect;
import org.bukkit.World;

public class GlobalProtector extends Protector {
    public GlobalProtector(PluginSpawnProtect plugin, World world) {
        super(plugin, world);
    }

    @Override
    protected boolean checkBounds(World world, int x, int y, int z) {
        return checkWorld(world);
    }
}
