/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ibhh.xpShop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Simon
 */
public class Repair {

    private xpShop plugin;
    private HashMap<Player, Integer> executer = new HashMap<Player, Integer>();
    private HashMap<Player, Integer> repairdamage = new HashMap<Player, Integer>();
    private HashMap<Player, ItemStack> item = new HashMap<Player, ItemStack>();
    /**
     * The plugin configuration file
     */
    private final YamlConfiguration configuration;
    /**
     * The plugin configuration file
     */
    private final File configurationFile;
    /**
     * The file where guid and opt out is stored in
     */
    private static final String CONFIG_FILE = "plugins/xpShop/RepairCosts.yml";
    private boolean mode;
    private String mode1 = "XP";

    public Repair(xpShop pl) {
        plugin = pl;
        configurationFile = new File(CONFIG_FILE);
        if (!configurationFile.exists()) {
            try {
                configurationFile.createNewFile();
            } catch (IOException ex) {
                plugin.Logger("Couldnt create new config file!", "Error");
                Logger.getLogger(Repair.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        configuration = YamlConfiguration.loadConfiguration(configurationFile);
        configuration.addDefault("useXPtoRepair", true);
        configuration.addDefault("Repair.NORMAL", 1);
        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                mode1 = "XP";
            } else {
                mode1 = "Money";
            }
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.OXYGEN", 1);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.PROTECTION_ENVIRONMENTAL", 1.5);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.PROTECTION_EXPLOSIONS", 2);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.PROTECTION_FALL", 1.3);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.PROTECTION_FIRE", 1.4);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.PROTECTION_PROJECTILE", 1);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.SILK_TOUCH", 2);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.WATER_WORKER", 1.6);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.ARROW_DAMAGE", 1);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.ARROW_FIRE", 1.4);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.ARROW_INFINITE", 2.2);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.ARROW_KNOCKBACK", 2);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.DAMAGE_ALL", 2.5);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.DAMAGE_ARTHROPODS", 1.2);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.DAMAGE_UNDEAD", 1.2);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.DIG_SPEED", 1.5);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.DURABILITY", 1.5);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.FIRE_ASPECT", 1.8);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.KNOCKBACK", 1.3);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.LOOT_BONUS_BLOCKS", 2.5);
            configuration.addDefault("Repair.Enchantment." + mode1 + ".Factor.LOOT_BONUS_MOBS", 1.2);
        }
        try {
            configuration.save(configurationFile);
        } catch (IOException ex) {
            plugin.Logger("Couldnt save config of the repair section!", "Error");
            Logger.getLogger(Repair.class.getName()).log(Level.SEVERE, null, ex);
        }
        configuration.options().header("http://dev.bukkit.org/server-mods/xpShop").copyDefaults(true);
        try {
            configuration.save(configurationFile);
        } catch (IOException ex) {
            plugin.Logger("Couldnt save config of the repair section!", "Error");
            Logger.getLogger(Repair.class.getName()).log(Level.SEVERE, null, ex);
        }
        mode = configuration.getBoolean("useXPtoRepair");
        mode1 = "XP";
        if (!mode) {
            mode1 = "Money";
        }
    }

    public ItemStack getItem(Player player) {
        ItemStack i = player.getItemInHand();
        return i;
    }

    public int getDurability(ItemStack i) {
        int a = 0;
        try {
            a = i.getDurability();
        } catch (NullPointerException e) {
            plugin.Logger(plugin.getConfig().getString("Repair.notool." + plugin.config.language), "Error");
        }
        return a;
    }

    public int getDamage(ItemStack i) {
        return getDurability(i);
    }

    public void setDurability(ItemStack i, int durability) {
        try {
            i.setDurability((short) durability);
        } catch (Exception e) {
            plugin.Logger(plugin.getConfig().getString("Repair.notool." + plugin.config.language), "Error");
        }
    }

    public int getXP_Price(ItemStack i, int repairAmount) {
        plugin.Logger("repairAmount: " + repairAmount, "Debug");
        double ret = 0;
        for (Enchantment ench : Enchantment.values()) {
            if (i.containsEnchantment(ench)) {
                plugin.Logger("items contains enchantment: " + ench.getName() + " level " + i.getEnchantmentLevel(ench), "Debug");
                double b = configuration.getDouble("Repair.Enchantment." + mode1 + ".Factor." + ench.getName());
                plugin.Logger("extracost of " + ench.getName()  + ": " + b, "Debug");
                double a = (b + i.getEnchantmentLevel(ench)) * (repairAmount / i.getEnchantments().size());
                plugin.Logger("Extra costs: " + a, "Debug");
                ret = ret + a;
                plugin.Logger("new amount ret = " + ret, "Debug");
            }
        }
        ret = ret + repairAmount * configuration.getDouble("Repair.NORMAL");
        plugin.Logger("price: " + ret, "Debug");
        return (int) ret;
    }

    public int maxDurability(ItemStack i) {
        int id = i.getTypeId();
        Material mat = Material.getMaterial(id);
        int a = 0;
        try {
            a = mat.getMaxDurability();
        } catch (Exception e) {
            plugin.Logger(plugin.getConfig().getString("Repair.notool." + plugin.config.language), "Error");
        }
        return a;
    }

    public void registerRepair(final Player player, int damage) {
        ItemStack i = getItem(player);
        if (plugin.PermissionsHandler.checkpermissionssilent(player, "xpShop.repair.nocosts") && damage == -1) {
            if (i == null) {
                plugin.Logger("Error item == null", "Debug");
                plugin.PlayerLogger(player, plugin.getConfig().getString("Repair.notool." + plugin.config.language), "Error");
                return;
            }
            setDurability(i, 0);
            player.saveData();
            plugin.PlayerLogger(player, plugin.getConfig().getString("Repair.executed." + plugin.config.language), "");
        } else if (damage < -1) {
            plugin.PlayerLogger(player, plugin.getConfig().getString("Repair.WrongAmount." + plugin.config.language), "Error");
        } else if (!executer.containsKey(player)) {
            if (i == null) {
                plugin.Logger("Error item == null", "Debug");
                plugin.PlayerLogger(player, plugin.getConfig().getString("Repair.notool." + plugin.config.language), "Error");
                return;
            }
            int amount = 0;
            if (damage == 0) {
                amount = getDurability(i);
            } else {
                amount = damage;
            }
            if (amount != 0) {
                int cost = getXP_Price(i, amount);
                boolean ok = false;
                if (mode) {
                    if (player.getTotalExperience() >= cost) {
                        ok = true;
                    }
                } else {
                    if (plugin.MoneyHandler.getBalance(player) >= cost) {
                        ok = true;
                    }
                }
                if (ok) {
                    plugin.PlayerLogger(player, String.format(plugin.getConfig().getString("Repair.RegisterRepair1." + plugin.config.language), i.getData().getItemType().name(), cost, mode1), "Warning");
                    plugin.PlayerLogger(player, plugin.getConfig().getString("Repair.RegisterRepair2." + plugin.config.language), "Warning");
                    plugin.PlayerLogger(player, String.format(plugin.getConfig().getString("Repair.RegisterRepair3." + plugin.config.language), plugin.getConfig().getInt("Cooldownoftp")), "Warning");
                    executer.put(player, cost);
                    item.put(player, i);
                    repairdamage.put(player, amount);
                    plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

                        @Override
                        public void run() {
                            if (executer.containsKey(player)) {
                                plugin.Logger("Player: " + player.getName() + " repair found", "Debug");
                                executer.remove(player);
                                item.remove(player);
                                repairdamage.remove(player);
                                plugin.PlayerLogger(player, plugin.getConfig().getString("Repair.TimedOut." + plugin.config.language), "Warning");
                            }
                        }
                    }, plugin.getConfig().getInt("Cooldownoftp") * 20);
                } else {
                    plugin.PlayerLogger(player, String.format(plugin.getConfig().getString("Repair.NotEnoughXP." + plugin.config.language), cost, mode1), "Error");
                    if (executer.containsKey(player)) {
                        plugin.Logger("Player: " + player.getName() + " repair found, but not enough XP", "Debug");
                        executer.remove(player);
                        item.remove(player);
                        repairdamage.remove(player);
                    }
                }
            } else {
                plugin.PlayerLogger(player, String.format(plugin.getConfig().getString("Repair.damage." + plugin.config.language), amount, maxDurability(i)), "Error");
            }
        } else {
            plugin.PlayerLogger(player, plugin.getConfig().getString("Repair.alreadyCommandRegistered." + plugin.config.language), "Error");
        }
    }

    public void RepairConfirm(Player player) {
        if (executer.containsKey(player)) {
            plugin.PlayerLogger(player, plugin.getConfig().getString("Repair.CommandConfirmed." + plugin.config.language), "");
            boolean ok = false;
            if (mode) {
                if (player.getTotalExperience() >= executer.get(player)) {
                    ok = true;
                }
            } else {
                if (plugin.MoneyHandler.getBalance(player) >= executer.get(player)) {
                    ok = true;
                }
            }
            if (ok) {
                ItemStack i = item.get(player);
                int damage = getDurability(i);
                plugin.Logger("Tool: " + i.getType().name() + " damage: " + damage + " maxDurability: " + maxDurability(i) + " repairdamage: " + repairdamage.get(player), "Debug");
                if (damage - repairdamage.get(player) >= 0) {
                    setDurability(i, damage - repairdamage.get(player));
                    if (mode) {
                        plugin.UpdateXP(player, -executer.get(player), "Repair");
                    } else {
                        plugin.MoneyHandler.substract(executer.get(player), player);
                    }
                    player.saveData();
                    executer.remove(player);
                    item.remove(player);
                    repairdamage.remove(player);
                    plugin.PlayerLogger(player, plugin.getConfig().getString("Repair.executed." + plugin.config.language), "");
                } else {
                    plugin.PlayerLogger(player, plugin.getConfig().getString("Repair.WrongAmount." + plugin.config.language), "");
                    if (executer.containsKey(player)) {
                        plugin.Logger("Player: " + player.getName() + " repair found, but wrong amount", "Debug");
                        executer.remove(player);
                        item.remove(player);
                    }
                }
            } else {
                plugin.PlayerLogger(player, String.format(plugin.getConfig().getString("Repair.NotEnoughXP." + plugin.config.language), executer.get(player), mode1), "Error");
                if (executer.containsKey(player)) {
                    plugin.Logger("Player: " + player.getName() + " repair found, but not enough XP", "Debug");
                    executer.remove(player);
                    item.remove(player);
                    repairdamage.remove(player);
                }
            }
        } else {
            plugin.PlayerLogger(player, plugin.getConfig().getString("Repair.NoCommand." + plugin.config.language), "Error");
        }
    }

    public void RepairCancel(Player player) {
        if (executer.containsKey(player)) {
            plugin.PlayerLogger(player, plugin.getConfig().getString("Repair.CommandCanceled." + plugin.config.language), "");
            executer.remove(player);
            item.remove(player);
        } else {
            plugin.PlayerLogger(player, plugin.getConfig().getString("Repair.NoCommand." + plugin.config.language), "Error");
        }
    }
}
