package me.legit.rtp;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Random;

public final class RTP extends JavaPlugin {

    private String msgcolor(String text){
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    String prefix = msgcolor(this.getConfig().getString("prefix"));
    private final HashMap<Player, Long> cooldowns = new HashMap<>();
    private int cooldownDuration;

    @Override
    public void onEnable() {
        cooldownDuration = getConfig().getInt("cooldownDuration");
        Bukkit.getConsoleSender().sendMessage(msgcolor(prefix+"&c------------------------------"));
        Bukkit.getConsoleSender().sendMessage(msgcolor(prefix+" "));
        Bukkit.getConsoleSender().sendMessage(msgcolor(prefix+"&eRTP Plugin Started !!"));
        Bukkit.getConsoleSender().sendMessage(msgcolor(prefix+"&ePlugin Version: &c1.0"));
        Bukkit.getConsoleSender().sendMessage(msgcolor(prefix+"&ePlugin Made By: &cLegit Killer"));
        Bukkit.getConsoleSender().sendMessage(msgcolor(prefix+" "));
        Bukkit.getConsoleSender().sendMessage(msgcolor(prefix+"&c------------------------------"));
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(msgcolor(prefix+"&c------------------------------"));
        Bukkit.getConsoleSender().sendMessage(msgcolor(prefix+" "));
        Bukkit.getConsoleSender().sendMessage(msgcolor(prefix+"&eRTP Plugin Stopped !!"));
        Bukkit.getConsoleSender().sendMessage(msgcolor(prefix+" "));
        Bukkit.getConsoleSender().sendMessage(msgcolor(prefix+"&c------------------------------"));
        saveConfig();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("rtp")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (player.hasPermission("rtp.use")) {
                    if (checkCooldown(player)) {
                        player.sendMessage(msgcolor(prefix + "You must wait before using this command again."));
                    } else {
                        player.sendMessage(msgcolor(prefix + "Teleporting in 5 sec!"));
                        setCooldown(player);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                teleportRandomly(player);
                            }
                        }.runTaskLater(this, 100);
                    }
                } else {
                    player.sendMessage(msgcolor(prefix + "You don't have permission to use this command."));
                }
                return true;
            } else {
                sender.sendMessage(msgcolor(prefix + "Only players can use this command."));
                return true;
            }
        }
        return false;
    }

    private boolean checkCooldown(Player player) {
        if (cooldowns.containsKey(player)) {
            long lastTime = cooldowns.get(player);
            long currentTime = System.currentTimeMillis() / 1000;
            return currentTime - lastTime < cooldownDuration;
        }
        return false;
    }

    private void setCooldown(Player player) {
        cooldowns.put(player, System.currentTimeMillis() / 1000);
    }

    private void teleportRandomly(Player player) {
        World world = player.getWorld();
        Location randomLocation = getRandomLocation(world);

        if (randomLocation != null) {
            player.teleport(randomLocation);
            player.sendMessage(msgcolor(prefix+"You have been teleported to "+ChatColor.GOLD+randomLocation.getX()+", "+randomLocation.getY()+", "+randomLocation.getZ()));
        } else {
            player.sendMessage(msgcolor(prefix+"Failed to find a safe random location."));
        }
    }

    private Location getRandomLocation(World world) {
        Random random = new Random();
        int maxAttempts = this.getConfig().getInt("maxattempt");
        int maxTeleportRadius = this.getConfig().getInt("maxteleportradius");

        for (int i = 0; i < maxAttempts; i++) {
            int x = random.nextInt(maxTeleportRadius * 2) - maxTeleportRadius;
            int z = random.nextInt(maxTeleportRadius * 2) - maxTeleportRadius;
            int y = world.getHighestBlockYAt(x, z);

            Location location = new Location(world, x, y, z);

            if (isSafeLocation(location)) {
                return location;
            }
        }
        return null;
    }

    private boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        if (world.getBlockAt(x, y, z).isLiquid()) {
            return false;
        }

        Material blockType = world.getBlockAt(x, y - 1, z).getType();
        if (!blockType.isSolid() && !blockType.equals(Material.GRASS) && !blockType.equals(Material.DIRT)) {
            return false;
        }

        for (Entity entity : world.getNearbyEntities(location, 1.0, 1.0, 1.0)) {
            if (entity instanceof Player || entity.getType() == EntityType.PLAYER) {
                return false;
            }
        }

        return true;
    }
}