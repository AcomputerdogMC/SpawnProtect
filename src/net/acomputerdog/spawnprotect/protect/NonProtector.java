package net.acomputerdog.spawnprotect.protect;

import net.acomputerdog.spawnprotect.PluginSpawnProtect;
import org.bukkit.World;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.*;

public class NonProtector extends Protector {
    public NonProtector(PluginSpawnProtect plugin, World world) {
        super(plugin, world, true, true, true, false);
    }
    @Override
    public void onBlockBreak(BlockExpEvent e) {}

    @Override
    public void onBlockPlace(BlockPlaceEvent e) {}

    @Override
    public void onPiston(BlockPistonEvent e) {}

    @Override
    public void onSpread(BlockSpreadEvent e) {}

    @Override
    public void onFlow(BlockFromToEvent e) {}

    @Override
    public void onInteract(PlayerInteractEvent e) {}

    @Override
    public void onDamage(EntityDamageEvent e) {}

    @Override
    public void onGrief(EntityChangeBlockEvent e) {}

    @Override
    public void onExplode(ExplosionPrimeEvent e) {}

    @Override
    public void onFire(BlockIgniteEvent e) {}

    @Override
    public void onArmorStand(PlayerArmorStandManipulateEvent e) {}

    @Override
    public void onEntityInteract(PlayerInteractEntityEvent e) {}

    @Override
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {}

    @Override
    public void onBucketFill(PlayerBucketFillEvent e) {}

    @Override
    protected boolean checkBounds(World world, int x, int y, int z) {
        return false;
    }
}
