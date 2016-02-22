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

    protected World world;
    protected boolean forceProtection = false;

    protected boolean allowPVP = true;
    protected boolean allowPVE = true;
    protected boolean allowDamage = true;

    protected boolean allowPlace = true;
    protected boolean allowBreak = true;
    protected boolean allowInteract = true;

    protected boolean allowMobGreifing = true;
    protected boolean allowSpread = true;
    protected boolean allowFlow = true;
    protected boolean allowFire = true;

    protected boolean allowExplosions = true;
    protected boolean allowPistons = true;

    protected boolean allowArmorStands = true;
    protected boolean allowEntityInteract = true;
    protected boolean allowBuckets = true;

    public Protector(PluginSpawnProtect plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }

    public boolean isAllowPVP() {
        return allowPVP;
    }

    public void setAllowPVP(boolean allowPVP) {
        this.allowPVP = allowPVP;
    }

    public boolean isAllowPVE() {
        return allowPVE;
    }

    public void setAllowPVE(boolean allowPVE) {
        this.allowPVE = allowPVE;
    }

    public boolean isAllowDamage() {
        return allowDamage;
    }

    public void setAllowDamage(boolean allowDamage) {
        this.allowDamage = allowDamage;
    }

    public boolean isForceProtection() {
        return forceProtection;
    }

    public void setForceProtection(boolean forceProtection) {
        this.forceProtection = forceProtection;
    }

    public boolean isAllowPlace() {
        return allowPlace;
    }

    public void setAllowPlace(boolean allowPlace) {
        this.allowPlace = allowPlace;
    }

    public boolean isAllowBreak() {
        return allowBreak;
    }

    public void setAllowBreak(boolean allowBreak) {
        this.allowBreak = allowBreak;
    }

    public boolean isAllowInteract() {
        return allowInteract;
    }

    public void setAllowInteract(boolean allowInteract) {
        this.allowInteract = allowInteract;
    }

    public boolean isAllowMobGreifing() {
        return allowMobGreifing;
    }

    public void setAllowMobGreifing(boolean allowMobGreifing) {
        this.allowMobGreifing = allowMobGreifing;
    }

    public boolean isAllowSpread() {
        return allowSpread;
    }

    public void setAllowSpread(boolean allowSpread) {
        this.allowSpread = allowSpread;
    }

    public boolean isAllowFlow() {
        return allowFlow;
    }

    public void setAllowFlow(boolean allowFlow) {
        this.allowFlow = allowFlow;
    }

    public boolean isAllowFire() {
        return allowFire;
    }

    public void setAllowFire(boolean allowFire) {
        this.allowFire = allowFire;
    }

    public boolean isAllowExplosions() {
        return allowExplosions;
    }

    public void setAllowExplosions(boolean allowExplosions) {
        this.allowExplosions = allowExplosions;
    }

    public boolean isAllowPistons() {
        return allowPistons;
    }

    public void setAllowPistons(boolean allowPistons) {
        this.allowPistons = allowPistons;
    }

    public boolean isAllowArmorStands() {
        return allowArmorStands;
    }

    public void setAllowArmorStands(boolean allowArmorStands) {
        this.allowArmorStands = allowArmorStands;
    }

    public boolean isAllowEntityInteract() {
        return allowEntityInteract;
    }

    public void setAllowEntityInteract(boolean allowEntityInteract) {
        this.allowEntityInteract = allowEntityInteract;
    }

    public boolean isAllowBuckets() {
        return allowBuckets;
    }

    public void setAllowBuckets(boolean allowBuckets) {
        this.allowBuckets = allowBuckets;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public PluginSpawnProtect getPlugin() {
        return plugin;
    }

    public void onBlockBreak(BlockExpEvent e) {
        if (e instanceof BlockBreakEvent && !allowBreak) {
            filterAction(((BlockBreakEvent) e), ((BlockBreakEvent) e).getPlayer());
        }
    }

    public void onBlockPlace(BlockPlaceEvent e) {
        if (!allowPlace) {
            filterAction(e, e.getPlayer());
        }
    }

    public void onPiston(BlockPistonEvent e) {
        if (!allowPistons) {
            filterAction(e, e.getBlock());
        }
    }

    public void onSpread(BlockSpreadEvent e) {
        if (!allowSpread) {
            filterAction(e, e.getBlock());
        }
    }

    public void onFlow(BlockFromToEvent e) {
        if (!allowFlow) {
            filterAction(e, e.getToBlock());
        }
    }

    public void onInteract(PlayerInteractEvent e) {
        if (!allowInteract) {
            filterAction(e, e.getPlayer());
        }
    }

    public void onDamage(EntityDamageEvent e) {
        if (!allowDamage && checkWorld(e.getEntity().getWorld())) { //don't bother with complicated logic if it isn't even the same world
            if (e.getEntityType() == EntityType.DROPPED_ITEM) {
                //make sure items can be destroyed
                return;
            }
            if (e.getEntityType() == EntityType.ITEM_FRAME || e.getEntityType() == EntityType.ARMOR_STAND) {
                if (!allowArmorStands) {
                    filterAction(e, e.getEntity());
                }
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
        if (!allowMobGreifing) {
            filterAction(e, e.getBlock());
        }
    }

    public void onExplode(ExplosionPrimeEvent e) {
        if (!allowExplosions) {
            filterAction(e, e.getEntity());
            if (e.isCancelled() && e.getEntityType() == EntityType.CREEPER) {
                //kill creepers when they don't explode
                e.getEntity().remove();
            }
        }
    }

    public void onFire(BlockIgniteEvent e) {
        if (!allowFire) {
            Player p = e.getPlayer();
            if (p != null) {
                filterAction(e, p);
            } else {
                filterAction(e, e.getBlock());
            }
        }
    }

    public void onArmorStand(PlayerArmorStandManipulateEvent e) {
        if (!allowArmorStands) {
            filterAction(e, e.getPlayer());
        }
    }

    public void onEntityInteract(PlayerInteractEntityEvent e) {
        if (!allowEntityInteract) {
            filterAction(e, e.getPlayer());
        }
    }

    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (!allowBuckets) {
            filterAction(e, e.getPlayer());
        }
    }

    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!allowBuckets) {
            filterAction(e, e.getPlayer());
        }
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
