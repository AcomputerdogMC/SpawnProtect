package net.acomputerdog.spawnprotect.protect;

import net.acomputerdog.spawnprotect.PluginSpawnProtect;
import org.bukkit.World;

/**
 * A protector type that protects a region
 */
public class AreaProtector extends Protector {
    private int x1, y1, z1, x2, y2, z2; //defaults to 0

    public AreaProtector(PluginSpawnProtect plugin, World world) {
        super(plugin, world);
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

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public void setZ1(int z1) {
        this.z1 = z1;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public void setZ2(int z2) {
        this.z2 = z2;
    }


    public void sortXYZ() {
        if (x2 < x1) {
            int t = x2;
            x2 = x1;
            x1 = t;
        }
        if (y2 < y1) {
            int t = y2;
            y2 = y1;
            y1 = t;
        }
        if (z2 < z1) {
            int t = z2;
            z2 = z1;
            z1 = t;
        }
    }

    @Override
    protected boolean checkBounds(World world, int x, int y, int z) {
        return checkWorld(world) && x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
    }
}
