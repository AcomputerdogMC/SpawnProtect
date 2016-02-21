package net.acomputerdog.spawnprotect;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class PluginSpawnProtect extends JavaPlugin implements Listener {

    private static final int CONFIG_VERSION = 5;

    //don't reset in onDisable()
    private boolean loaded = false;
    private File configFile;

    private boolean protectionEnabled;
    private World protectedWorld;
    private int x1, y1, z1, x2, y2, z2;
    private boolean forceProtection;
    private boolean allowPVP, allowPVE, allowDamage;

    @Override
    public void onEnable() {
        try {
            if (loaded) {
                getLogger().warning("Plugin is already loaded, was the server hot-reloaded with /reload?");
                getLogger().warning("Attempting to unload and restart, no guarantees of success!");
                getLogger().warning("Keep in mind that even if this works protection settings will not change!");
                onDisable();
            }
            loadConfig(); //must load config before registerEvents()
            if (!loaded) { //don't register for the same events twice
                registerEvents();
            }
            loaded = true;
        } catch (Exception e) {
            getLogger().severe("Exception during startup!  SpawnProtect will not start!");
            super.setEnabled(false);
            throw new RuntimeException("Exception during startup!", e);
        }
    }

    @Override
    public void onDisable() {
        configFile = null;
    }

    private void loadConfig() {
        if (!getDataFolder().isDirectory() && !getDataFolder().mkdir()) {
            getLogger().warning("Unable to create data directory!");
        }
        configFile = new File(getDataFolder(), "SpawnProtect.cfg");
        if (!configFile.isFile()) {
            getLogger().warning("Configuration file not found, a new one will be created.");
            InputStream in = null;
            OutputStream out = null;
            try {
                in = getClass().getResourceAsStream("/default.yml");
                out = new FileOutputStream(configFile);
                while (in.available() > 0) {
                    out.write(in.read());
                }
            } catch (Exception e) {
                throw new RuntimeException("Exception creating new configuration file!", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ignored) {}
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ignored) {}
                }
            }
        }
        try {
            getConfig().load(configFile);
            readConfig();
        } catch (IOException e) {
            throw new RuntimeException("Exception loading configuration file!", e);
        } catch (Exception e) {
            getLogger().severe("*****************************************************************");
            getLogger().severe("*Errors found in your configuration, SpawnProtect will NOT load!*");
            getLogger().severe("*****************************************************************");
            throw new RuntimeException("Errors found in configuration file", e);
        }
    }

    private void readConfig() throws InvalidConfigurationException {
        FileConfiguration c = getConfig();
        if (c.getInt("config_version", -1) != CONFIG_VERSION) {
            getLogger().warning("Configuration file is out of date, you should regenerate it.");
            getLogger().warning("To regenerate the file: rename it, run SpawnProtect, then copy your changes to the generated file.");
        }
        protectionEnabled = c.getBoolean("protection_enabled", false);
        String worldName = c.getString("world_name");
        if ("".equals(worldName)) {
            protectedWorld = getServer().getWorlds().get(0);
        } else {
            protectedWorld = getServer().getWorld(worldName);
        }
        if (protectedWorld == null) {
            throw new InvalidConfigurationException("The specified world could not be found!");
        }
        x1 = c.getInt("spawn_x1", 0);
        y1 = c.getInt("spawn_y1", 0);
        z1 = c.getInt("spawn_z1", 0);
        x2 = c.getInt("spawn_x2", 0);
        y2 = c.getInt("spawn_y2", 0);
        z2 = c.getInt("spawn_z2", 0);
        if (x1 > x2) {
            int t = x1;
            x1 = x2;
            x2 = t;
        }
        if (y1 > y2) {
            int t = y1;
            y1 = y2;
            y2 = t;
        }
        if (z1 > z2) {
            int t = z1;
            z1 = z2;
            z2 = t;
        }
        forceProtection = c.getBoolean("force_protection", false);
        allowPVP = c.getBoolean("allow_pvp", true);
        allowPVE = c.getBoolean("allow_pve", true);
        allowDamage = c.getBoolean("allow_damage", true);
    }

    public void registerEvents() {
        PluginManager m = getServer().getPluginManager();
        FileConfiguration c = getConfig();
        //manually registered so that un-used events can be skipped.  Also uses lambdas instead of reflection for performance.
        if (protectionEnabled) {
            if (forceProtection || !c.getBoolean("allow_place", false)) {
                m.registerEvent(BlockExpEvent.class, this, EventPriority.HIGH, (l, e) -> onBlockBreak((BlockExpEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_break", false)) {
                m.registerEvent(BlockPlaceEvent.class, this, EventPriority.HIGH, (l, e) -> onBlockPlace((BlockPlaceEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_pistons", false)) {
                m.registerEvent(BlockPistonExtendEvent.class, this, EventPriority.HIGH, (l, e) -> onPiston((BlockPistonExtendEvent) e), this, true);
                m.registerEvent(BlockPistonRetractEvent.class, this, EventPriority.HIGH, (l, e) -> onPiston((BlockPistonRetractEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_spread", false)) {
                m.registerEvent(BlockSpreadEvent.class, this, EventPriority.HIGH, (l, e) -> onSpread((BlockSpreadEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_flow", false)) {
                m.registerEvent(BlockFromToEvent.class, this, EventPriority.HIGH, (l, e) -> onFlow((BlockFromToEvent) e), this, true);
            }
            if (!c.getBoolean("allow_interact", true)) {
                m.registerEvent(PlayerInteractEvent.class, this, EventPriority.HIGH, (l, e) -> onInteract((PlayerInteractEvent) e), this, true);
            }
            if (!allowPVE || !allowPVP || !allowDamage) {
                m.registerEvent(EntityDamageEvent.class, this, EventPriority.HIGH, (l, e) -> onDamage((EntityDamageEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_mob_greifing", false)) {
                m.registerEvent(EntityChangeBlockEvent.class, this, EventPriority.HIGH, (l, e) -> onGrief((EntityChangeBlockEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_explosions", false)) {
                m.registerEvent(ExplosionPrimeEvent.class, this, EventPriority.HIGH, (l, e) -> onExplode((ExplosionPrimeEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_fire", false)) {
                m.registerEvent(BlockIgniteEvent.class, this, EventPriority.HIGH, (l, e) -> onFire((BlockIgniteEvent) e), this, true);
            }
            if (!c.getBoolean("allow_armor_stands", false)) {
                m.registerEvent(PlayerArmorStandManipulateEvent.class, this, EventPriority.HIGH, (l, e) -> onArmorStand((PlayerArmorStandManipulateEvent) e), this, true);
            }
            if (!c.getBoolean("allow_entity_interact", false)) {
                m.registerEvent(PlayerInteractEntityEvent.class, this, EventPriority.HIGH, (l, e) -> onEntityInteract((PlayerInteractEntityEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_buckets", false)) {
                m.registerEvent(PlayerBucketEmptyEvent.class, this, EventPriority.HIGH, (l, e) -> onBucketEmpty((PlayerBucketEmptyEvent) e), this, true);
                m.registerEvent(PlayerBucketFillEvent.class, this, EventPriority.HIGH, (l, e) -> onBucketFill((PlayerBucketFillEvent) e), this, true);
            }
        }
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
        if (e.getEntity().getWorld() == protectedWorld) { //don't bother with complicated logic if it isn't even the same world
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
    private boolean checkBounds(World world, int x, int y, int z) {
        return world == protectedWorld && x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
    }

    /*
    Returns TRUE if player can bypass
     */
    private boolean checkPerms(Player p) {
        return !forceProtection && p.hasPermission("spawnprotect.bypass");
    }

    /*
    Returns FALSE if player is breaking protection
     */
    private boolean checkBoundsAndPerms(Player p) {
        Location l = p.getLocation();
        return !(checkBounds(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ()) && !checkPerms(p));
    }

    private void filterAction(Cancellable c, Player p) {
        if (!checkBoundsAndPerms(p)) {
            c.setCancelled(true);
        }
    }

    private void filterAction(Cancellable c, World world, int x, int y, int z) {
        if (checkBounds(world, x, y, z)) {
            c.setCancelled(true);
        }
    }

    private void filterAction(Cancellable c, Block b) {
        filterAction(c, b.getWorld(), b.getX(), b.getY(), b.getZ());
    }

    private void filterAction(Cancellable c, Location l) {
        filterAction(c, l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    private void filterAction(Cancellable c, Entity e) {
        filterAction(c, e.getLocation());
    }
}
