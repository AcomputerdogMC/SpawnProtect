package net.acomputerdog.spawnprotect.protect;

import net.acomputerdog.spawnprotect.PluginSpawnProtect;
import org.bukkit.World;

public class AreaProtector extends Protector {
    private final int x1, y1, z1, x2, y2, z2;

    public AreaProtector(PluginSpawnProtect plugin, World world, boolean allowPVP, boolean allowPVE, boolean allowDamage, boolean forceProtection, int x1, int y1, int z1, int x2, int y2, int z2) {
        super(plugin, world, allowPVP, allowPVE, allowDamage, forceProtection);
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getZ1() {
        return z1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    public int getZ2() {
        return z2;
    }

    @Override
    protected boolean checkBounds(World world, int x, int y, int z) {
        return checkWorld(world) && x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
    }
}
