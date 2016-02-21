package net.acomputerdog.spawnprotect.protect;

import net.acomputerdog.spawnprotect.PluginSpawnProtect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.*;

public abstract class Protector {
    protected final PluginSpawnProtect plugin;

    protected final World world;
    protected final boolean allowPVP, allowPVE, allowDamage, forceProtection;;

    public Protector(PluginSpawnProtect plugin, World world, boolean allowPVP, boolean allowPVE, boolean allowDamage, boolean forceProtection) {
        this.plugin = plugin;
        this.world = world;
        this.allowPVP = allowPVP;
        this.allowPVE = allowPVE;
        this.allowDamage = allowDamage;
        this.forceProtection = forceProtection;
    }

    public PluginSpawnProtect getPlugin() {
        return plugin;
    }

    public void onBlockBreak(BlockExpEvent e) {
        if (e instanceof BlockBreakEvent) {
            filterAction(((BlockBreakEvent) e), ((BlockBreakEvent) e).getPlayer());
        }
    }

    public void onBlockPlace(BlockPlaceEvent e) {
        filterAction(e, e.getPlayer());
    }

    public void onPiston(BlockPistonEvent e) {
        filterAction(e, e.getBlock());
    }

    public void onSpread(BlockSpreadEvent e) {
        filterAction(e, e.getBlock());
    }

    public void onFlow(BlockFromToEvent e) {
        filterAction(e, e.getToBlock());
    }

    public void onInteract(PlayerInteractEvent e) {
        filterAction(e, e.getPlayer());
    }

    public void onDamage(EntityDamageEvent e) {
        if (checkWorld(e.getEntity().getWorld())) { //don't bother with complicated logic if it isn't even the same world
            if (e.getEntityType() == EntityType.DROPPED_ITEM) {
                //make sure items can be destroyed
                return;
            }
            if (e instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent e2 = (EntityDamageByEntityEvent) e;
                if (e2.getDamager().getType() == EntityType.PLAYER) {
                    if (e2.getEntity().getType() == EntityType.PLAYER) {
                        if (!allowPVP) {
                            //PVP
                            filterAction(e2, (Player) e2.getDamager());
                        }
                    } else if (!allowPVE) {
                        //PvE
                        filterAction(e2, (Player) e2.getDamager());
                    }
                } else if (!allowDamage) {
                    //player taking damage from entity
                    filterAction(e2, e2.getEntity());
                }
            } else if (!allowDamage && e.getEntityType() == EntityType.PLAYER) {
                //player taking damage from environment
                filterAction(e, e.getEntity());
            }
        }
    }

    public void onGrief(EntityChangeBlockEvent e) {
        filterAction(e, e.getBlock());
    }

    public void onExplode(ExplosionPrimeEvent e) {
        filterAction(e, e.getEntity());
        if (e.isCancelled() && e.getEntityType() == EntityType.CREEPER) {
            //kill creepers when they don't explode
            e.getEntity().remove();
        }
    }

    public void onFire(BlockIgniteEvent e) {
        Player p = e.getPlayer();
        if (p != null) {
            filterAction(e, p);
        } else {
            filterAction(e, e.getBlock());
        }
    }

    public void onArmorStand(PlayerArmorStandManipulateEvent e) {
        filterAction(e, e.getPlayer());
    }

    public void onEntityInteract(PlayerInteractEntityEvent e) {
        filterAction(e, e.getPlayer());
    }

    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        filterAction(e, e.getPlayer());
    }

    public void onBucketFill(PlayerBucketFillEvent e) {
        filterAction(e, e.getPlayer());
    }

    /*
    Returns TRUE if in bounds
     */
    protected abstract boolean checkBounds(World world, int x, int y, int z);

    /*
    Returns TRUE if player can bypass
     */
    protected boolean checkPerms(Player p) {
        return !forceProtection && p.hasPermission("spawnprotect.bypass");
    }

    /*
    Returns FALSE if player is breaking protection
     */
    protected boolean checkBoundsAndPerms(Player p) {
        Location l = p.getLocation();
        return !(checkBounds(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ()) && !checkPerms(p));
    }

    protected void filterAction(Cancellable c, Player p) {
        if (!checkBoundsAndPerms(p)) {
            c.setCancelled(true);
        }
    }

    protected void filterAction(Cancellable c, World world, int x, int y, int z) {
        if (checkBounds(world, x, y, z)) {
            c.setCancelled(true);
        }
    }

    protected void filterAction(Cancellable c, Block b) {
        filterAction(c, b.getWorld(), b.getX(), b.getY(), b.getZ());
    }

    protected void filterAction(Cancellable c, Location l) {
        filterAction(c, l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    protected void filterAction(Cancellable c, Entity e) {
        filterAction(c, e.getLocation());
    }

    /*
    Return TRUE if world matches
     */
    protected boolean checkWorld(World world) {
        return world == this.world;
    }
}
