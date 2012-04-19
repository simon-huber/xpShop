package me.ibhh.xpShop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 *
 * @author Simon
 */
public class Logger {
    private xpShop plugin;
    /**
     * Konstruktor of Logger class
     * @param pl 
     */
    public Logger(xpShop pl){
        plugin = pl;
    }
    
    /**
     * Logs string into file.
     * @param in 
     */
    public void log(String in){
        Date now = new Date();
        String Stream = now.toString();
        String path = plugin.getDataFolder().toString() + File.separator;
        File file = new File(path + "debug" + ".txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
        try {
            // Create file
            FileWriter fstream = new FileWriter(file, true);
            PrintWriter out = new PrintWriter(fstream);
            out.println("[" + Stream + "] " + in);
            //Close the output stream
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.out.println("Error: " + e.getMessage());
        }
    }
}
