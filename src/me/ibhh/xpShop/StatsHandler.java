/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ibhh.xpShop;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;

/**
 *
 * @author Simon
 */
public class StatsHandler {

    private Connection cn;
    private xpShop auTrade;

    public StatsHandler(xpShop AuctTrade) {
        auTrade = AuctTrade;
        cn = null;
        auTrade.getServer().getScheduler().scheduleAsyncRepeatingTask(auTrade, new Runnable() {

            @Override
            public void run() {
                Update();
            }
        }, 200L, 72000L);
    }

    public void PrepareDB() {
        Statement st = null;
        long time = 0;
        if (auTrade.config.debug) {
            auTrade.Logger("Creaeting table!", "");
            time = System.nanoTime();
        }
        try {
            String sql = "CREATE TABLE IF NOT EXISTS xpshop (ID INT NOT NULL AUTO_INCREMENT, PRIMARY KEY(ID), ServerIP VARCHAR(30), ServerName VARCHAR(30), PlayerOnline INT, MaxPlayers INT, Hours INT, LastUpdate LONG)";
            st = cn.createStatement();

            st.executeUpdate(sql);
            if (auTrade.config.debug) {
                System.out.println("[xpShop] Table created!");
            }

            cn.commit();
            st.close();
        } catch (SQLException e) {
            System.out.println("[xpShop]: Error while creating tables! - " + e.getMessage());
            SQLErrorHandler(e);
        }
        if (auTrade.config.debug) {
            time = (System.nanoTime() - time) / 1000000;
            auTrade.Logger("Created in " + time + " ms!", "Debug");
        }
        //	    UpdateDB();
    }

    public boolean InsertAuction() {
        long time = 0;
        if (auTrade.config.debug) {
            auTrade.Logger("Insert player into table!", "");
            time = System.nanoTime();
        }
        try {
            PreparedStatement ps = cn.prepareStatement("INSERT INTO xpshop (ServerIP, ServerName, PlayerOnline, MaxPlayers, Hours, LastUpdate) VALUES (?,?,?,?,?,?)");
            ps.setString(1, auTrade.getServer().getIp());
            ps.setString(2, auTrade.getServer().getName());
            int Players = 0;
            for (Player player : auTrade.getServer().getOnlinePlayers()) {
                Players++;
            }
            ps.setInt(3, Players);
            ps.setInt(4, auTrade.getServer().getMaxPlayers());
            ps.setInt(5, 0);
            ps.setLong(6, System.currentTimeMillis());
            ps.execute();
            cn.commit();
            ps.close();
        } catch (SQLException e) {
            System.out.println("[xpShop] Error while inserting XP into DB! - " + e.getMessage());
            SQLErrorHandler(e);
            return false;
        }
        if (auTrade.config.debug) {
            time = (System.nanoTime() - time) / 1000000;
            auTrade.Logger("Finished in " + time + " ms!", "Debug");
        }
        return true;
    }

    public boolean Update() {
        long time = 0;
        try {
            if (isindb(auTrade.getServer().getIp())) {
                if (auTrade.config.debug) {
                    auTrade.Logger("Updating XP in DB!", "");
                    time = System.nanoTime();
                }
                try {
                    int Players = 0;
                    for (Player player : auTrade.getServer().getOnlinePlayers()) {
                        Players++;
                    }
                    String IP = "localhost";
                    if (auTrade.config.debug) {
                    auTrade.Logger("ServerIP: " + auTrade.getServer().getIp(), "");
                    }
                    if(auTrade.getServer().getIp() == null || auTrade.getServer().getIp().equalsIgnoreCase("'")){
                        IP = "localhost";
                    } else {
                        IP = auTrade.getServer().getIp();
                    }
                    PreparedStatement ps = cn.prepareStatement("UPDATE xpshop SET PlayerOnline='" + Players + ", MaxPlayers='" + auTrade.getServer().getMaxPlayers() + "' , Hours='" + (getHours() + 1) + "', LastUpdate='" + System.currentTimeMillis() + "' WHERE ServerIP='" + IP + "';");
                    ps.executeUpdate();
                    cn.commit();
                    ps.close();

                } catch (SQLException e) {
                    System.out.println("[xpShop] Error while updating Server into DB! - " + e.getMessage());
                    SQLErrorHandler(e);
                    return false;
                }
                if (auTrade.config.debug) {
                    time = (System.nanoTime() - time) / 1000000;
                    auTrade.Logger("Finished in " + time + " ms!", "Debug");
                }
            } else {
                InsertAuction();
            }
        } catch (SQLException ex) {
            Logger.getLogger(StatsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    public Connection createConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            cn = DriverManager.getConnection("jdbc:mysql://" + "SQL09.FREEMYSQL.NET/xpshop", "ibhhreal", "tester");
            cn.setAutoCommit(false);
            return cn;
        } catch (SQLException e) {
            System.out.println("[xpShop] could not be enabled: Exception occured while trying to connect to DB");
            SQLErrorHandler(e);
            if (cn != null) {
                System.out.println("[xpShop] Old Connection still activated");
                try {
                    cn.close();
                    System.out.println("[xpShop] Old connection that was still activated has been successfully closed");
                } catch (SQLException e2) {
                    System.out.println("[xpShop] Failed to close old connection that was still activated");
                    SQLErrorHandler(e2);
                }
            }
            return null;
        } catch (ClassNotFoundException e) {
            ErrorLogger(e.getMessage());
            return null;
        }
    }

    private void ErrorLogger(String Error) {
        System.err.println("[xpShop] Error:" + Error);
    }

    private void SQLErrorHandler(SQLException ex) {
        do {
            try {
                ErrorLogger("Exception Message: " + ex.getMessage());
                ErrorLogger("DBMS Code: " + ex.getErrorCode());
                ex.printStackTrace();
            } catch (Exception ne) {
                ErrorLogger(ne.getMessage());
            }
        } while ((ex = ex.getNextException()) != null);
    }

    public boolean CloseCon() {
        try {
            cn.close();
            return true;
        } catch (SQLException e) {
            System.out.println("[xpShop] Failed to close connection to DB!");
            SQLErrorHandler(e);
            return false;
        }
    }

    public boolean isindb(String name) throws SQLException {
        boolean a = false;
        long time = 0;
        if (auTrade.config.debug) {
            auTrade.Logger("Checking if in table!", "");
            time = System.nanoTime();
        }
        Statement st = null;
        String sql;
        ResultSet result;
        String Name = "";
        try {
            st = cn.createStatement();
        } catch (SQLException e) {
            SQLErrorHandler(e);
        }
        sql = "SELECT COUNT(ServerIP) from xpshop WHERE ServerIP='" + name + "';";
        try {
            result = st.executeQuery(sql);
        } catch (SQLException e1) {
            SQLErrorHandler(e1);
            return false;
        }
        try {
            result.next();
            int b = result.getInt(1);
            if (auTrade.config.debug) {
                auTrade.Logger("Lines: " + b, "Debug");
            }
            if (b > 0) {
                a = true;
            }

            cn.commit();
            result.close();
            st.close();
        } catch (SQLException e2) {
            SQLErrorHandler(e2);
        }
        if (auTrade.config.debug) {
            time = (System.nanoTime() - time) / 1000000;
            auTrade.Logger("Finished in " + time + " ms!", "Debug");
        }
        return a;
    }

    public void getStats(Player p) throws SQLException {
        boolean a = false;
        long time = 0;
        if (auTrade.config.debug) {
            auTrade.Logger("Checking if in table!", "");
            time = System.nanoTime();
        }
        Statement st = null;
        String sql;
        ResultSet result;
        String Name = "";
        try {
            st = cn.createStatement();
        } catch (SQLException e) {
            SQLErrorHandler(e);
        }
        sql = "SELECT COUNT(ServerIP) from xpshop WHERE LastUpdate>'" + (System.currentTimeMillis() - 3600000) + "';";
        try {
            result = st.executeQuery(sql);
        } catch (SQLException e1) {
            SQLErrorHandler(e1);
            return;
        }
        int Servers = 0;;
        try {
            while(result.next()){
                Servers++;
            }
            auTrade.PlayerLogger(p, "xpShop is running on " + Servers + " Servers", "");
            cn.commit();
            result.close();
            st.close();
        } catch (SQLException e2) {
            SQLErrorHandler(e2);
        }
        if (auTrade.config.debug) {
            time = (System.nanoTime() - time) / 1000000;
            auTrade.Logger("Finished in " + time + " ms!", "Debug");
        }
        return;
    }

    public int getHours() throws SQLException {
        long time = 0;
        if (auTrade.config.debug) {
            auTrade.Logger("getting Hours!", "");
            time = System.nanoTime();
        }
        Statement st = null;
        String sql;
        ResultSet result;
        try {
            st = cn.createStatement();
        } catch (SQLException e) {
            SQLErrorHandler(e);
        }
        sql = "SELECT Hours from xpshop WHERE ServerIP='" + auTrade.getServer().getIp() + "';";
        result = st.executeQuery(sql);
        int XP = 0;
        try {
            while (result.next() == true) {
                XP = result.getInt("Hours");
            }
            cn.commit();
            st.close();
            result.close();
        } catch (SQLException e2) {
            SQLErrorHandler(e2);
        }
        if (auTrade.config.debug) {
            time = (System.nanoTime() - time) / 1000000;
            auTrade.Logger("Finished in " + time + " ms!", "Debug");
        }
        return XP;

    }

    public long getlastUpdate() throws SQLException {
        long time = 0;
        if (auTrade.config.debug) {
            auTrade.Logger("getting lastUpdate!", "");
            time = System.nanoTime();
        }
        Statement st = null;
        String sql;
        ResultSet result;
        try {
            st = cn.createStatement();
        } catch (SQLException e) {
            SQLErrorHandler(e);
        }
        sql = "SELECT LastUpdate from xpshop WHERE ServerIP='" + auTrade.getServer().getIp() + "';";
        result = st.executeQuery(sql);
        long XP = 0;
        try {
            while (result.next() == true) {
                XP = result.getInt("LastUpdate");
            }
            cn.commit();
            st.close();
            result.close();
        } catch (SQLException e2) {
            SQLErrorHandler(e2);
        }
        if (auTrade.config.debug) {
            time = (System.nanoTime() - time) / 1000000;
            auTrade.Logger("Finished in " + time + " ms!", "Debug");
        }
        return XP;

    }
}
