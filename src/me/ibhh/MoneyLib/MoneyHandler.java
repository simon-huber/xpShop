package me.ibhh.MoneyLib;

import com.iCo6.system.Accounts;
import com.iConomy.iConomy;
import com.nijikokun.register.payment.Methods;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class MoneyHandler {

    private static int iConomyversion = 0;
    private com.iConomy.system.Holdings balance5;
    private Double balance;
    private Plugin plugin;
    public static Economy economy = null;

    public MoneyHandler(Plugin pl) {
        plugin = pl;
        if (setupEconomy() == true) {
            iConomyversion = 2;
            plugin.getLogger().info("hooked into Vault");
        }
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
        	plugin.getLogger().fine("checking MoneyPlugin!");
                iConomyversion();
            }
        });
    }

    private static boolean packageExists(String[] packages) {
        try {
            String[] arrayOfString = packages;
            int j = packages.length;
            for (int i = 0; i < j; i++) {
                String pkg = arrayOfString[i];
                Class.forName(pkg);
            }
            return true;
        } catch (Exception localException) {
        }
        return false;
    }

    private Boolean setupEconomy() {
        try {
            RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
            }
        } catch (NoClassDefFoundError e) {
            return false;
        }
        return (economy != null);
    }

    public int iConomyversion() {
        if (iConomyversion == 0) {
            try {
                if (packageExists(new String[]{"net.milkbowl.vault.economy.Economy"})) {
                    iConomyversion = 2;
                    plugin.getLogger().info("hooked into Vault");
                } else if (packageExists(new String[]{"com.nijikokun.register.payment.Methods"})) {
                    iConomyversion = 1;
                    plugin.getLogger().info("hooked into Register");
                } else if (packageExists(new String[]{"com.iConomy.iConomy", "com.iConomy.system.Account", "com.iConomy.system.Holdings"})) {
                    iConomyversion = 5;
                    plugin.getLogger().info("hooked into iConomy5");
                } else if (packageExists(new String[]{"com.iCo6.system.Accounts"})) {
                    iConomyversion = 6;
                    plugin.getLogger().info("hooked into iConomy6");
                } else {
                    plugin.getLogger().severe("cant hook into iConomy5, iConomy6, Vault or Register. Downloading Vault!");
                    plugin.getLogger().severe(" ************ Please download and configure Vault!!!!! **********");
                }
            } catch (Exception E) {
                E.printStackTrace();
                iConomyversion = 0;
            }
            return iConomyversion;
        } else {
            return 2;
        }
    }

    public double getBalance(Player player) throws NoiConomyPluginFound {
        String name = player.getName();
        return getBalance(name);
    }

    public double getBalance(String name) throws NoiConomyPluginFound {
        if (iConomyversion == 5) {
            try {
                this.balance5 = getAccount5(name).getHoldings();
            } catch (Exception E) {
        	plugin.getLogger().severe("No Account! Please report it to an admin!");
                E.printStackTrace();
                this.balance5 = null;
                return this.balance;
            }
            try {
                this.balance = Double.valueOf(this.balance5.balance());
            } catch (Exception E) {
        	plugin.getLogger().severe("No Account! Please report it to an admin!");
                E.printStackTrace();
                this.balance5 = null;
                return this.balance;
            }
            balance = balance5.balance();
            return this.balance;
        }
        if (iConomyversion == 6) {
            try {
                this.balance = new Accounts().get(name).getHoldings().getBalance();
            } catch (Exception e) {
        	plugin.getLogger().severe("No Account! Please report it to an admin!");
                e.printStackTrace();
                balance = null;
                return this.balance;
            }
        }
        if (iConomyversion == 1) {
            try {
                this.balance =
                        Double.valueOf(Methods.getMethod().getAccount(name).balance());
            } catch (Exception e) {
        	plugin.getLogger().severe("No Account! Please report it to an admin!");
                e.printStackTrace();
                this.balance = null;
                return this.balance;
            }
        }
        if (iConomyversion == 2) {
            this.balance = economy.getBalance(name);
            return balance;
        }

        throw new NoiConomyPluginFound("No iConomy plugin found! Install a supported plugin and restart the server, please.");
    }

    private com.iConomy.system.Account getAccount5(String name) {
        return iConomy.getAccount(name);
    }

    public void substract(double amountsubstract, String name) {
        if (iConomyversion == 5) {
            try {
                getAccount5(name).getHoldings().subtract(amountsubstract);
            } catch (Exception e) {
        	errorSubstract();
                e.printStackTrace();
            }
        } else if (iConomyversion == 6) {
            try {
                com.iCo6.system.Account account = new Accounts().get(name);
                account.getHoldings().subtract(amountsubstract);
            } catch (Exception e) {
        	errorSubstract();
                e.printStackTrace();
            }
        } else if (iConomyversion == 1) {
            try {
                Methods.getMethod().getAccount(name).subtract(amountsubstract);
            } catch (Exception e) {
        	errorSubstract();
                e.printStackTrace();
            }
        } else if (iConomyversion == 2) {
            try {
                economy.withdrawPlayer(name, amountsubstract);
            } catch (Exception e) {
        	errorSubstract();
                e.printStackTrace();
            }
        }
    }

    public void substract(double amountsubstract, Player player) {
        String name = player.getName();
        substract(amountsubstract, name);
    }

    public void addmoney(double amountadd, String name) {
        if (iConomyversion == 5) {
            try {
                getAccount5(name).getHoldings().add(amountadd);
            } catch (Exception e) {
        	errorSubstract();
                e.printStackTrace();
            }
        } else if (iConomyversion == 6) {
            try {
                com.iCo6.system.Account account = new Accounts().get(name);
                account.getHoldings().add(amountadd);
            } catch (Exception e) {
        	errorSubstract();
                e.printStackTrace();
            }
        } else if (iConomyversion == 1) {
            try {
                Methods.getMethod().getAccount(name).add(amountadd);
            } catch (Exception e) {
        	errorSubstract();
        	
                e.printStackTrace();
            }
        } else if (iConomyversion == 2) {
            try {
                economy.depositPlayer(name, amountadd);
            } catch (Exception e) {
        	errorSubstract();
                e.printStackTrace();
            }
        }
    }
    
    private void errorSubstract() {
	plugin.getLogger().severe("Cant substract money! Does account exist?");
    }

    public void addmoney(double amountadd, Player player) {
        String name = player.getName();
        addmoney(amountadd, name);
    }
}