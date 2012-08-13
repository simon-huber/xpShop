package me.ibhh.xpShop;

import com.iCo6.system.Accounts;
import com.iConomy.iConomy;
import com.nijikokun.register.payment.Methods;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class iConomyHandler {

    public static int iConomyversion = 0;
    private com.iConomy.system.Holdings balance5;
    private Double balance;
    private xpShop plugin;
    public static Economy economy = null;

    public iConomyHandler(xpShop pl) {
        plugin = pl;
        plugin.aktuelleVersion();
        if (setupEconomy() == true) {
            iConomyversion = 2;
            plugin.Logger("hooked into Vault", "Debug");
        }
        plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

            @Override
            public void run() {
                plugin.Logger("checking MoneyPlugin!", "Debug");
                iConomyversion();
            }
        }, 0);
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
                    plugin.Logger("hooked into Vault", "Debug");
                } else if (packageExists(new String[]{"com.nijikokun.register.payment.Methods"})) {
                    iConomyversion = 1;
                    plugin.Logger("hooked into Register", "Debug");
                } else if (packageExists(new String[]{"com.iConomy.iConomy", "com.iConomy.system.Account", "com.iConomy.system.Holdings"})) {
                    iConomyversion = 5;
                    plugin.Logger("hooked into iConomy5", "Debug");
                } else if (packageExists(new String[]{"com.iCo6.system.Accounts"})) {
                    iConomyversion = 6;
                    plugin.Logger("hooked into iConomy6", "Debug");
                } else {
                    plugin.Logger("cant hook into iConomy5, iConomy6, Vault or Register.!", "");
                    plugin.Logger(" ************ Please download and configure Vault!!!!! **********", "Warning");
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

    public double getBalance(Player player) {
        String name = player.getName();
        return getBalance(name);
    }

    public double getBalance(String name) {
        if (iConomyversion == 5) {
            try {
                this.balance5 = getAccount5(name).getHoldings();
            } catch (Exception E) {
                plugin.Logger("No Account! Please report it to an admin!", "Error");
                E.printStackTrace();
                this.balance5 = null;
                return this.balance;
            }
            try {
                this.balance = Double.valueOf(this.balance5.balance());
            } catch (Exception E) {
                plugin.Logger("No Account! Please report it to an admin!", "Error");
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
                plugin.Logger("No Account! Please report it to an admin!", "Error");
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
                plugin.Logger("No Account! Please report it to an admin!", "Error");
                e.printStackTrace();
                this.balance = null;
                return this.balance;
            }
        }
        if (iConomyversion == 2) {
            this.balance = economy.getBalance(name);
            return balance;
        }

        return this.balance;
    }

    private com.iConomy.system.Account getAccount5(String name) {
        return iConomy.getAccount(name);
    }

    public void substract(double amountsubstract, String name) {
        if (iConomyversion == 5) {
            try {
                getAccount5(name).getHoldings().subtract(amountsubstract);
            } catch (Exception e) {
                plugin.Logger("Cant substract money! Does account exist?", "Error");
                e.printStackTrace();
            }
        } else if (iConomyversion == 6) {
            try {
                com.iCo6.system.Account account = new Accounts().get(name);
                account.getHoldings().subtract(amountsubstract);
            } catch (Exception e) {
                plugin.Logger("Cant substract money! Does account exist?", "Error");
                e.printStackTrace();
            }
        } else if (iConomyversion == 1) {
            try {
                Methods.getMethod().getAccount(name).subtract(amountsubstract);
            } catch (Exception e) {
                plugin.Logger("Cant substract money! Does account exist?", "Error");
                e.printStackTrace();
            }
        } else if (iConomyversion == 2) {
            try {
                economy.withdrawPlayer(name, amountsubstract);
            } catch (Exception e) {
                plugin.Logger("Cant substract money! Does account exist?", "Error");
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
                plugin.Logger("Cant substract money! Does account exist?", "Error");
                e.printStackTrace();
            }
        } else if (iConomyversion == 6) {
            try {
                com.iCo6.system.Account account = new Accounts().get(name);
                account.getHoldings().add(amountadd);
            } catch (Exception e) {
                plugin.Logger("Cant substract money! Does account exist?", "Error");
                e.printStackTrace();
            }
        } else if (iConomyversion == 1) {
            try {
                Methods.getMethod().getAccount(name).add(amountadd);
            } catch (Exception e) {
                plugin.Logger("Cant substract money! Does account exist?", "Error");
                e.printStackTrace();
            }
        } else if (iConomyversion == 2) {
            try {
                economy.depositPlayer(name, amountadd);
            } catch (Exception e) {
                plugin.Logger("Cant substract money! Does account exist?", "Error");
                e.printStackTrace();
            }
        }
    }

    public void addmoney(double amountadd, Player player) {
        String name = player.getName();
        addmoney(amountadd, name);
    }
}