package net.acomputerdog.spawnprotect;

import net.acomputerdog.spawnprotect.map.ProtectorMap;
import net.acomputerdog.spawnprotect.protect.AreaProtector;
import net.acomputerdog.spawnprotect.protect.GlobalProtector;
import net.acomputerdog.spawnprotect.protect.NonProtector;
import net.acomputerdog.spawnprotect.protect.Protector;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class PluginSpawnProtect extends JavaPlugin implements Listener {

    private static final int CONFIG_VERSION = 6;

    //don't reset in onDisable()
    private boolean loaded = false;

    private File configFile;
    private boolean protectionEnabled;
    private boolean forceProtection;
    private ProtectorMap protectorMap;

    @Override
    public void onEnable() {
        try {
            if (loaded) {
                getLogger().warning("Plugin is already loaded, was the server hot-reloaded with /reload?");
                getLogger().warning("Attempting to unload and restart, no guarantees of success!");
                getLogger().warning("Keep in mind that even if this works protection settings will not change!");
                onDisable();
            }
            protectorMap = new ProtectorMap(this);
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
        protectorMap = null;
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
        forceProtection = c.getBoolean("force_protection", false);
        /*
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
        */
    }

    public void registerEvents() {
        PluginManager m = getServer().getPluginManager();
        //FileConfiguration c = getConfig();
        //manually registered so that un-used events can be skipped.  Also uses lambdas instead of reflection for performance.
        if (protectionEnabled) {
            m.registerEvent(WorldLoadEvent.class, this, EventPriority.MONITOR, (l, event) -> {
                WorldLoadEvent e = (WorldLoadEvent) event;
                //getLogger().info("Adding world: " + e.getWorld().getName());
                protectorMap.addWorld(e.getWorld(), createProtector(e.getWorld()));
            }, this, true);
            //Warning: block of text
            m.registerEvent(BlockExpEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((BlockExpEvent) e).onBlockBreak((BlockExpEvent) e), this, true);
            m.registerEvent(BlockPlaceEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((BlockPlaceEvent) e).onBlockPlace((BlockPlaceEvent) e), this, true);
            m.registerEvent(BlockPistonExtendEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((BlockPistonExtendEvent) e).onPiston((BlockPistonExtendEvent) e), this, true);
            m.registerEvent(BlockPistonRetractEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((BlockPistonRetractEvent) e).onPiston((BlockPistonRetractEvent) e), this, true);
            m.registerEvent(BlockSpreadEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((BlockSpreadEvent) e).onSpread((BlockSpreadEvent) e), this, true);
            m.registerEvent(BlockFromToEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((BlockFromToEvent) e).onFlow((BlockFromToEvent) e), this, true);
            m.registerEvent(PlayerInteractEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((PlayerInteractEvent) e).onInteract((PlayerInteractEvent) e), this, true);
            m.registerEvent(EntityDamageEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((EntityDamageEvent) e).onDamage((EntityDamageEvent) e), this, true);
            m.registerEvent(EntityChangeBlockEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((EntityChangeBlockEvent) e).onGrief((EntityChangeBlockEvent) e), this, true);
            m.registerEvent(ExplosionPrimeEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((ExplosionPrimeEvent) e).onExplode((ExplosionPrimeEvent) e), this, true);
            m.registerEvent(BlockIgniteEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((BlockIgniteEvent) e).onFire((BlockIgniteEvent) e), this, true);
            m.registerEvent(PlayerArmorStandManipulateEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((PlayerArmorStandManipulateEvent) e).onArmorStand((PlayerArmorStandManipulateEvent) e), this, true);
            m.registerEvent(PlayerInteractEntityEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((PlayerInteractEntityEvent) e).onEntityInteract((PlayerInteractEntityEvent) e), this, true);
            m.registerEvent(PlayerBucketEmptyEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((PlayerBucketEmptyEvent) e).onBucketEmpty((PlayerBucketEmptyEvent) e), this, true);
            m.registerEvent(PlayerBucketFillEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((PlayerBucketFillEvent) e).onBucketFill((PlayerBucketFillEvent) e), this, true);

            /*
            if (forceProtection || !c.getBoolean("allow_place", false)) {
                m.registerEvent(BlockExpEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((BlockExpEvent)e).onBlockBreak((BlockExpEvent)e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_break", false)) {
                m.registerEvent(BlockPlaceEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((BlockPlaceEvent) e).onBlockPlace((BlockPlaceEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_pistons", false)) {
                m.registerEvent(BlockPistonExtendEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((BlockPistonExtendEvent) e).onPiston((BlockPistonExtendEvent) e), this, true);
                m.registerEvent(BlockPistonRetractEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((BlockPistonRetractEvent) e).onPiston((BlockPistonRetractEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_spread", false)) {
                m.registerEvent(BlockSpreadEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((BlockSpreadEvent) e).onSpread((BlockSpreadEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_flow", false)) {
                m.registerEvent(BlockFromToEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((BlockFromToEvent) e).onFlow((BlockFromToEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_interact", true)) {
                m.registerEvent(PlayerInteractEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((PlayerInteractEvent) e).onInteract((PlayerInteractEvent) e), this, true);
            }
            if (forceProtection || !allowPVE || !allowPVP || !allowDamage) {
                m.registerEvent(EntityDamageEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((EntityDamageEvent) e).onDamage((EntityDamageEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_mob_greifing", false)) {
                m.registerEvent(EntityChangeBlockEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((EntityChangeBlockEvent) e).onGrief((EntityChangeBlockEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_explosions", false)) {
                m.registerEvent(ExplosionPrimeEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((ExplosionPrimeEvent) e).onExplode((ExplosionPrimeEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_fire", false)) {
                m.registerEvent(BlockIgniteEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((BlockIgniteEvent) e).onFire((BlockIgniteEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_armor_stands", false)) {
                m.registerEvent(PlayerArmorStandManipulateEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((PlayerArmorStandManipulateEvent) e).onArmorStand((PlayerArmorStandManipulateEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_entity_interact", false)) {
                m.registerEvent(PlayerInteractEntityEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((PlayerInteractEntityEvent) e).onEntityInteract((PlayerInteractEntityEvent) e), this, true);
            }
            if (forceProtection || !c.getBoolean("allow_buckets", false)) {
                m.registerEvent(PlayerBucketEmptyEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((PlayerBucketEmptyEvent) e).onBucketEmpty((PlayerBucketEmptyEvent) e), this, true);
                m.registerEvent(PlayerBucketFillEvent.class, this, EventPriority.NORMAL, (l, e) -> getProtector((PlayerBucketFillEvent) e).onBucketFill((PlayerBucketFillEvent) e), this, true);
            }*/
        }
    }

    private Protector getProtector(BlockEvent e) {
        return protectorMap.getProtector(e.getBlock().getWorld());
    }

    private Protector getProtector(EntityEvent e) {
        return protectorMap.getProtector(e.getEntity().getWorld());
    }

    private Protector getProtector(PlayerEvent e) {
        return protectorMap.getProtector(e.getPlayer().getWorld());
    }

    private Protector createProtector(World world) {
        ConfigurationSection section = getConfig().getConfigurationSection("world." + world.getName());
        if (section != null) {
            return readProtector(section, world);
        } else {
            section = getConfig().getConfigurationSection("world.*");
            if (section != null) {
                return readProtector(section, world);
            } else {
                //getLogger().info("Did not find a matching section for: " + world.getName());
                return createDefaultProtector(world);
            }
        }
    }

    private Protector readProtector(ConfigurationSection s, World world) {
        try {
            String mode = s.getString("protection_mode");
            getLogger().info("Enabling " + mode + " protection for " + world.getName());
            switch (mode.toLowerCase()) {
                case "none":
                    //getLogger().info("Using protection mode none for: " + world.getName());
                    return new NonProtector(this, world);
                case "global":
                    //getLogger().info("Using protection mode global for: " + world.getName());
                    return populateSettings(s, new GlobalProtector(this, world));
                case "area":
                    //getLogger().info("Using protection mode area for: " + world.getName());
                    AreaProtector protector = new AreaProtector(this, world);
                    protector.setX1(s.getInt("spawn_x1"));
                    protector.setY1(s.getInt("spawn_y1"));
                    protector.setZ1(s.getInt("spawn_z1"));
                    protector.setX2(s.getInt("spawn_x2"));
                    protector.setY2(s.getInt("spawn_y2"));
                    protector.setZ2(s.getInt("spawn_z2"));
                    protector.sortXYZ();
                    return populateSettings(s, protector);

                default:
                    logConfigError("Invalid protection mode for \"" + world.getName() + "\" (\"" + mode + "\" was not recognized)!\nProtection will be DISABLED for this world!");
                    return createDefaultProtector(world);
            }
        } catch (Exception e) {
            logConfigError("Exception reading configuration!\nProtection will be DISABLED in world \"" + world.getName() + "\"!", e);
            return new NonProtector(this, world);
        }
    }

    private Protector populateSettings(ConfigurationSection s, Protector p) {
        p.setForceProtection(forceProtection);

        p.setAllowBreak(s.getBoolean("allow_break", p.isAllowBreak()));
        p.setAllowPlace(s.getBoolean("allow_place", p.isAllowPlace()));
        p.setAllowInteract(s.getBoolean("allow_interact", p.isAllowInteract()));
        p.setAllowPVP(s.getBoolean("allow_pvp", p.isAllowPVP()));
        p.setAllowPVE(s.getBoolean("allow_pve", p.isAllowPVE()));
        p.setAllowDamage(s.getBoolean("allow_damage", p.isAllowDamage()));
        p.setAllowMobGreifing(s.getBoolean("allow_mob_greifing", p.isAllowMobGreifing()));
        p.setAllowSpread(s.getBoolean("allow_spread", p.isAllowSpread()));
        p.setAllowFlow(s.getBoolean("allow_flow", p.isAllowFlow()));
        p.setAllowPistons(s.getBoolean("allow_pistons", p.isAllowPistons()));
        p.setAllowExplosions(s.getBoolean("allow_explosions", p.isAllowExplosions()));
        p.setAllowFire(s.getBoolean("allow_fire", p.isAllowFire()));
        p.setAllowArmorStands(s.getBoolean("allow_armor_stands", p.isAllowArmorStands()));
        p.setAllowEntityInteract(s.getBoolean("allow_entity_interact", p.isAllowEntityInteract()));
        p.setAllowBuckets(s.getBoolean("allow_buckets", p.isAllowBuckets()));
        return p;
    }

    public Protector createDefaultProtector(World world) {
        return new NonProtector(this, world);
    }

    private void logConfigError(String message, Throwable t) {
        getLogger().warning("---|Configuration Error|---------------");
        getLogger().warning(message);
        if (t != null) {
            t.printStackTrace();
        }
        getLogger().warning("---------------------------------------");
    }

    private void logConfigError(String message) {
        logConfigError(message, null);
    }

}
