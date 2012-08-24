/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ibhh.xpShop;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author Simon
 */
public class MetricsHandler implements Serializable {

    private xpShop plugin;
    private Metrics metrics;
    public static HashMap<MTLocation, String> Shop = new HashMap<MTLocation, String>();
    public static HashMap<MTLocation, String> Safe = new HashMap<MTLocation, String>();
    public int buy,
            sell,
            buylevel,
            selllevel,
            info,
            infoxp,
            infolevel,
            send,
            grand,
            bottle,
            toolinfo,
            repair,
            tpto,
            tpme,
            xpShopSignBuy,
            xpShopSignSell,
            xpShopSafeGet,
            xpShopSafeStore = 0;

    public MetricsHandler(xpShop pl) {
        plugin = pl;
    }

    public void onStart() {
        try {
            metrics = new Metrics(plugin);
        } catch (IOException ex) {
            plugin.Logger("There was an error while submitting statistics.", "Error");
        }
        initializeGraphs();
        startStatistics();
    }

    public void saveStatsFiles() {
        try {
            ObjectManager.save(Shop, plugin.getDataFolder() + File.separator + "metrics" + File.separator + "Shop.statistics");
            plugin.Logger("Shops stats file contains " + calculateShopQuantity() + " values!", "Debug");
        } catch (Exception e) {
            plugin.Logger("Cannot save Shop statistics!", "Error");
            if (plugin.config.debug) {
                e.printStackTrace();
            }
        }
        try {
            ObjectManager.save(Safe, plugin.getDataFolder() + File.separator + "metrics" + File.separator + "Safe.statistics");
            plugin.Logger("Safe stats file contains " + calculateSafeQuantity() + " values!", "Debug");
        } catch (Exception e) {
            plugin.Logger("Cannot save Shop statistics!", "Error");
            if (plugin.config.debug) {
                e.printStackTrace();
            }
        }
    }

    public void loadStatsFiles() {
        try {
            Shop = ObjectManager.load(plugin.getDataFolder() + File.separator + "metrics" + File.separator + "Shop.statistics");
            plugin.Logger("Shops stats file contains " + calculateShopQuantity() + " values!", "Debug");
            plugin.Logger("Stats loaded!", "Debug");
        } catch (Exception e) {
            plugin.Logger("Cannot load Shop statistics!", "Error");
            if (plugin.config.debug) {
                e.printStackTrace();
            }
        }
        try {
            Safe = ObjectManager.load(plugin.getDataFolder() + File.separator + "metrics" + File.separator + "Safe.statistics");
            plugin.Logger("Safe stats file contains " + calculateSafeQuantity() + " values!", "Debug");
            plugin.Logger("Stats loaded!", "Debug");
        } catch (Exception e) {
            plugin.Logger("Cannot load Shop statistics!", "Error");
            if (plugin.config.debug) {
                e.printStackTrace();
            }
        }
    }

    private void startStatistics() {
        try {
            metrics.start();
        } catch (Exception ex) {
            plugin.Logger("There was an error while submitting statistics.", "Error");
        }
    }

    private void initializeGraphs() {
        initializeOthers();
        initializeDependenciesGraph();
        initializeCommandGraph();
    }

    public void initializeOthers() {
        Metrics.Graph ShopCountGraph = metrics.createGraph("Signs");
        ShopCountGraph.addPlotter(new Metrics.Plotter("xpShopSigns") {

            @Override
            public int getValue() {
                return calculateShopQuantity();
            }
        });
        ShopCountGraph.addPlotter(new Metrics.Plotter("xpShopSafes") {

            @Override
            public int getValue() {
                return calculateSafeQuantity();
            }
        });
        Metrics.Graph GMGraph = metrics.createGraph("DefaultGameMode");
        GMGraph.addPlotter(new Metrics.Plotter(plugin.getServer().getDefaultGameMode().name()) {

            @Override
            public int getValue() {
                return 1;
            }
        });
    }

    private void initializeCommandGraph() {
        Metrics.Graph CMDUses = metrics.createGraph("CommandUses");
        CMDUses.addPlotter(new Metrics.Plotter("Buy") {

            @Override
            public int getValue() {
                return buy;
            }

            @Override
            public void reset() {
                buy = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("Sell") {

            @Override
            public int getValue() {
                return sell;
            }

            @Override
            public void reset() {
                sell = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("Buylevel") {

            @Override
            public int getValue() {
                return buylevel;
            }

            @Override
            public void reset() {
                buylevel = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("Selllevel") {

            @Override
            public int getValue() {
                return selllevel;
            }

            @Override
            public void reset() {
                selllevel = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("Send") {

            @Override
            public int getValue() {
                return send;
            }

            @Override
            public void reset() {
                send = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("Grand") {

            @Override
            public int getValue() {
                return grand;
            }

            @Override
            public void reset() {
                grand = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("Infoxp") {

            @Override
            public int getValue() {
                return infoxp;
            }

            @Override
            public void reset() {
                infoxp = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("Infolevel") {

            @Override
            public int getValue() {
                return infolevel;
            }

            @Override
            public void reset() {
                infolevel = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("Info") {

            @Override
            public int getValue() {
                return info;
            }

            @Override
            public void reset() {
                info = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("Bottle") {

            @Override
            public int getValue() {
                return bottle;
            }

            @Override
            public void reset() {
                bottle = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("Toolinfo") {

            @Override
            public int getValue() {
                return toolinfo;
            }

            @Override
            public void reset() {
                toolinfo = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("Repair") {

            @Override
            public int getValue() {
                return repair;
            }

            @Override
            public void reset() {
                repair = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("TPto") {

            @Override
            public int getValue() {
                return tpto;
            }

            @Override
            public void reset() {
                tpto = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("TPme") {

            @Override
            public int getValue() {
                return tpme;
            }

            @Override
            public void reset() {
                tpme = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("xpShopSignBuy") {

            @Override
            public int getValue() {
                return xpShopSignBuy;
            }

            @Override
            public void reset() {
                xpShopSignBuy = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("xpShopSignSell") {

            @Override
            public int getValue() {
                return xpShopSignSell;
            }

            @Override
            public void reset() {
                xpShopSignSell = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("xpShopSafeGet") {

            @Override
            public int getValue() {
                return xpShopSafeGet;
            }

            @Override
            public void reset() {
                xpShopSafeGet = 0;
            }
        });
        CMDUses.addPlotter(new Metrics.Plotter("xpShopSafeStore") {

            @Override
            public int getValue() {
                return xpShopSafeStore;
            }

            @Override
            public void reset() {
                xpShopSafeStore = 0;
            }
        });
    }

    public void initializeDependenciesGraph() {
        Metrics.Graph depGraph = metrics.createGraph("EconomyDependencies");
        String iConomyName = "None";
        if (plugin.MoneyHandler.iConomyversion() != 0) {
            if (plugin.MoneyHandler.iConomyversion() == 1) {
                iConomyName = "Register";
            } else if (plugin.MoneyHandler.iConomyversion() == 2) {
                iConomyName = "Vault";
            } else if (plugin.MoneyHandler.iConomyversion() == 5) {
                iConomyName = "iConomy5";
            } else if (plugin.MoneyHandler.iConomyversion() == 6) {
                iConomyName = "iConomy6";
            }
        }
        depGraph.addPlotter(new Metrics.Plotter(iConomyName) {

            @Override
            public int getValue() {
                return 1;
            }
        });
        Metrics.Graph Permgraph = metrics.createGraph("PermissionDependencies");
        String PermName = "None";
        if (plugin.PermissionsHandler.PermPlugin != 0) {
            if (plugin.PermissionsHandler.PermPlugin == 1) {
                PermName = "BukkitPermissions";
            } else if (plugin.PermissionsHandler.PermPlugin == 2) {
                PermName = "PermissionsEX";
            } else if (plugin.PermissionsHandler.PermPlugin == 3) {
                PermName = "GroupManager";
            } else if (plugin.PermissionsHandler.PermPlugin == 4) {
                PermName = "bPermissions";
            }
        }
        Permgraph.addPlotter(new Metrics.Plotter(PermName) {

            @Override
            public int getValue() {
                return 1;
            }
        });
    }

    public int calculateShopQuantity() {
        int a = 0;
        for (String i : Shop.values()) {
            a++;
        }
        return a;
    }

    public int calculateSafeQuantity() {
        int a = 0;
        for (String i : Safe.values()) {
            a++;
        }
        return a;
    }
}
