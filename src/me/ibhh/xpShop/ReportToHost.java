/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ibhh.xpShop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author Simon
 */
public class ReportToHost {

    private xpShop plugin;

    public ReportToHost(xpShop pl) {
        plugin = pl;
    }

    public String report(int line, String other, String message, String classfile, Throwable stack) {
        if (plugin.getConfig().getBoolean("senderrorreport")) {
            String ret = "Error";
            other = other.replace(" ", "%20");
            message = message.replace(" ", "%20");
            String stacktrace = StackTraceUtil.getCustomStackTrace(stack);
            stacktrace = stacktrace.replace(" ", "%20");
            String url = "http://ibhh.de/report/index.php?"
                    + "plugin=" + plugin.getName()
                    + "&version=" + plugin.getDescription().getVersion()
                    + "&line=" + line
                    + "&gameversion=" + plugin.getServer().getBukkitVersion()
                    + "&message=" + message
                    + "&class=" + classfile
                    + "&stacktrace=" + stacktrace
                    + "&other=" + other;
            try {
                System.out.print("[" + plugin.getName() + "] Sending issue report to ibhh.de!");
                System.out.print("[" + plugin.getName() + "] -------------------------");
                System.out.print("[" + plugin.getName() + "] Version: " + plugin.getDescription().getVersion());
                System.out.print("[" + plugin.getName() + "] ErrorID: " + line);
                System.out.print("[" + plugin.getName() + "] Gameversion: " + plugin.getServer().getBukkitVersion());
                System.out.print("[" + plugin.getName() + "] Other: " + other);
                System.out.print("[" + plugin.getName() + "] Message: " + message);
                System.out.print("[" + plugin.getName() + "] Class: " + classfile);
                System.out.print("[" + plugin.getName() + "] -------------------------");
                ret = readAll(url);
                System.out.print("[" + plugin.getName() + "] Message of Server: " + ret);
                System.out.print("[" + plugin.getName() + "] -------------------------");
            } catch (Exception ex) {
                System.out.print("[" + plugin.getName() + "] Couldnt send error report to ibhh.de!");
                if(plugin.getConfig().getBoolean("debug")){
                    ex.printStackTrace();
                }
            }
            return ret;
        } else {
            return "internet not enabled in the config.yml";
        }
    }

    public String readAll(String url) {
        String zeile;
        try {
            URL myConnection = new URL(url);
            URLConnection connectMe = myConnection.openConnection();
            InputStreamReader lineReader = new InputStreamReader(connectMe.getInputStream());
            BufferedReader br = new BufferedReader(new BufferedReader(lineReader));
            zeile = br.readLine();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            plugin.Logger("Exception: IOException! Exception on reading message!", "Error");
            return "Exception on reading message!";
        } catch (Exception e) {
            e.printStackTrace();
            plugin.Logger("Exception: Exception! Exception on reading message!", "");
            return "Exception on reading message!";
        }
        return zeile;
    }
}
