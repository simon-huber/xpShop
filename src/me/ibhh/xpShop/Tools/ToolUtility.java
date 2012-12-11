package me.ibhh.xpShop.Tools;

import me.ibhh.xpShop.xpShop;


/**
 *
 * @author ibhh
 */
public class ToolUtility {
    public static Tools getTools(){
        if(xpShop.getRawBukkitVersion().equalsIgnoreCase("1.4.5-R0.3")){
            return new Tools145();
        } else {
            return new Tools132();
        }
    }
}
