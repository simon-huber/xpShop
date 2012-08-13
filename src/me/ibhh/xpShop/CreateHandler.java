/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ibhh.xpShop;

import org.bukkit.Location;
import org.bukkit.event.block.SignChangeEvent;

/**
 *
 * @author Simon
 */
public class CreateHandler {

    //define variable
    private xpShop plugin;
    private SafeHandler safeHandler;
    private SignHandler signHandler;
    private SignConSafeHandler signConSafeHandler;

    /**
     * Konstruktor
     *
     * @param pl
     */
    public CreateHandler(xpShop pl) {
        plugin = pl;
        safeHandler = new SafeHandler(pl);
        signHandler = new SignHandler(pl);
        signConSafeHandler = new SignConSafeHandler(pl);
    }

    /**
     * Manages the creation of shops
     *
     * @param event
     */
    public void CreatexpShop(SignChangeEvent event) {
        if (plugin.config.optionalconnectionofSafetoShop) {
            plugin.Logger("Creating xpShop with opt con!", "Debug");
            signConSafeHandler.CreatexpShop(event);
        } else {
            plugin.Logger("Creating xpShop!", "Debug");
            signHandler.Create(event);
        }
    }

    public void CreateSafe(SignChangeEvent event) {
        safeHandler.Create(event);
    }
}
