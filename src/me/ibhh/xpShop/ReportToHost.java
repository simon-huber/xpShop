package me.ibhh.xpShop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class ReportToHost {
    
    private xpShop plugin;
    private FileSend filesend;
    private StackTraceUtil util;
    
    public ReportToHost(xpShop pl) {
        this.plugin = pl;
        util = new StackTraceUtil();
        this.filesend = new FileSend(this.plugin);
    }
    
    public String report(int line, String other, String message, String classfile, Exception stack) {
        if (this.plugin.getConfig().getBoolean("senderrorreport")) {
            if (other == null) {
                other = "none";
            }
            if (message == null) {
                message = "none";
            }
            String stacktrace;
            if (stack != null) {
                stacktrace = util.getStackTrace(stack);
            } else {
                stacktrace = "none";
            }
            
            return send(line + "", message, classfile, stacktrace, other);
        }
        return "internet not enabled in the config.yml";
    }
    
    public String report(int line, String other, String message, String classfile, String stacktrace) {
        if (this.plugin.getConfig().getBoolean("senderrorreport")) {
            if (other == null) {
                other = "none";
            }
            if (message == null) {
                message = "none";
            }
            if (stacktrace == null) {
                stacktrace = "none";
            }
            return send(line + "", message, classfile, stacktrace, other);
        }
        return "internet not enabled in the config.yml";
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
            this.plugin.Logger("Exception: IOException! Exception on reading message!", "Error");
            return "Exception on reading message!";
        } catch (Exception e) {
            e.printStackTrace();
            this.plugin.Logger("Exception: Exception! Exception on reading message!", "");
            return "Exception on reading message!";
        }
        return zeile;
    }
    
    public String send(String line, String message, String classfile, String stacktrace, String other) {
        String ret = "Error";
        String url1 = "http://report.ibhh.de/index.php?programm=" + this.plugin.getName();
        try {
            URL url = new URL(url1);
            // Construct data
            String data = URLEncoder.encode("version", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(this.plugin.getDescription().getVersion()), "UTF-8");
            data += "&" + URLEncoder.encode("line", "UTF-8") + "=" + URLEncoder.encode(line, "UTF-8");
            data += "&" + URLEncoder.encode("gameversion", "UTF-8") + "=" + URLEncoder.encode(this.plugin.getServer().getBukkitVersion(), "UTF-8");
            data += "&" + URLEncoder.encode("message", "UTF-8") + "=" + URLEncoder.encode(message, "UTF-8");
            data += "&" + URLEncoder.encode("class", "UTF-8") + "=" + URLEncoder.encode(classfile, "UTF-8");
            data += "&" + URLEncoder.encode("stacktrace", "UTF-8") + "=" + URLEncoder.encode(stacktrace, "UTF-8");
            data += "&" + URLEncoder.encode("other", "UTF-8") + "=" + URLEncoder.encode(other, "UTF-8");

            // Send data
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            ret = rd.readLine();
            wr.close();
            rd.close();
        } catch (Exception e) {
        }
        try {
            String temp = "[" + this.plugin.getName() + "] Sending issue report to ibhh.de!";
            System.out.print(temp);
            this.plugin.Loggerclass.log(temp);
            temp = "[" + this.plugin.getName() + "] -------------------------";
            System.out.print(temp);
            this.plugin.Loggerclass.log(temp);
            temp = "[" + this.plugin.getName() + "] Version: " + this.plugin.getDescription().getVersion();
            System.out.print(temp);
            this.plugin.Loggerclass.log(temp);
            temp = "[" + this.plugin.getName() + "] ErrorID: " + line;
            System.out.print(temp);
            this.plugin.Loggerclass.log(temp);
            temp = "[" + this.plugin.getName() + "] Gameversion: " + this.plugin.getServer().getBukkitVersion();
            System.out.print(temp);
            this.plugin.Loggerclass.log(temp);
            temp = "[" + this.plugin.getName() + "] Other: " + other;
            System.out.print(temp);
            this.plugin.Loggerclass.log(temp);
            temp = "[" + this.plugin.getName() + "] Message: " + message;
            System.out.print(temp);
            this.plugin.Loggerclass.log(temp);
            temp = "[" + this.plugin.getName() + "] Stacktrace: " + stacktrace;
            if (this.plugin.getConfig().getBoolean("debug")) {
                System.out.print(temp);
            }
            this.plugin.Loggerclass.log(temp);
            temp = "[" + this.plugin.getName() + "] Class: " + classfile;
            System.out.print(temp);
            this.plugin.Loggerclass.log(temp);
            temp = "[" + this.plugin.getName() + "] -------------------------";
            System.out.print(temp);
            this.plugin.Loggerclass.log(temp);
            temp = "[" + this.plugin.getName() + "] Message of Server: " + ret;
            System.out.print(temp);
            this.plugin.Loggerclass.log(temp);
            temp = "[" + this.plugin.getName() + "] -------------------------";
            System.out.print(temp);
            this.plugin.Loggerclass.log(temp);
        } catch (Exception ex) {
            String temp = "[" + this.plugin.getName() + "] Couldnt send error report to ibhh.de!";
            System.out.print(temp);
            this.plugin.Loggerclass.log(temp);
            if (this.plugin.getConfig().getBoolean("debug")) {
                ex.printStackTrace();
            }
        }
        if (this.plugin.getConfig().getBoolean("senddebugfile")) {
            this.plugin.Logger("config == true", "Debug");
            if (ret != null) {
                this.plugin.Logger("ret != null", "Debug");
                try {
                    String[] id_text = ret.split(":");
                    String id = id_text[1];
                    this.plugin.Logger("ID: " + id, "Debug");
                    try {
                        if (id != null) {
                            this.filesend.sendDebugFile(id);
                            this.plugin.Logger("debugfile successfully sent, thanks!", "");
                        }
                    } catch (Exception e1) {
                        this.plugin.Logger("Could not send debugfile!", "Error");
                        if (this.plugin.config.debug) {
                            e1.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        return ret;
    }
}
