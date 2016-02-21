package net.acomputerdog.spawnprotect.protect;

import net.acomputerdog.spawnprotect.PluginSpawnProtect;
import org.bukkit.World;

public class GlobalProtector extends Protector {
    public GlobalProtector(PluginSpawnProtect plugin, World world, boolean allowPVP, boolean allowPVE, boolean allowDamage, boolean forceProtection) {
        super(plugin, world, allowPVP, allowPVE, allowDamage, forceProtection);
    }

    @Override
    protected boolean checkBounds(World world, int x, int y, int z) {
        return checkWorld(world);
    }
}
