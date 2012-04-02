package me.ibhh.xpShop;

/**
 *
 */
import java.io.File;
import java.sql.*;

/**
 * @author Simon
 *
 */
public class SQLConnectionHandler {

    private Connection cn;
    private xpShop auTrade;

    public SQLConnectionHandler(xpShop AuctTrade) {
        auTrade = AuctTrade;
        cn = null;
    }
    
    public boolean deleteDB(){
        Statement st = null;
        boolean temp = false;
        long time = 0;
        auTrade.Logger("deleting table!", "Debug");
        time = System.nanoTime();
        try {
            String sql = "drop table if exists xpShop;";
            st = cn.createStatement();
            if (auTrade.getConfig().getBoolean("SQL")) {
                st.executeUpdate(sql);
                auTrade.Logger("[xpShop] Table deleted!", "Debug");
                temp = true;
            } else {
                st.executeUpdate("drop table if exists xpShop;");
                auTrade.Logger("[xpShop] Table deleted!", "Debug");
                temp = true;
            }
            cn.commit();
            st.close();
        } catch (SQLException e) {
            System.out.println("[xpShop]: Error while creating tables! - " + e.getMessage());
            SQLErrorHandler(e);
        }
        time = (System.nanoTime() - time) / 1000000;
        auTrade.Logger("DELETED in " + time + " ms!", "Debug");
        //	    UpdateDB();
        return temp;
    }

    public void PrepareDB() {
        Statement st = null;
        long time = 0;
        auTrade.Logger("Creaeting table!", "Debug");
        time = System.nanoTime();
        try {
            String sql = "CREATE TABLE IF NOT EXISTS xpShop (Name VARCHAR(30) PRIMARY KEY , XP INT);";
            st = cn.createStatement();
            if (auTrade.getConfig().getBoolean("SQL")) {
                st.executeUpdate(sql);
                auTrade.Logger("[xpShop] Table created!", "Debug");
            } else {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS xpShop (Name VARCHAR PRIMARY KEY NOT NULL, XP INT)");
                auTrade.Logger("[xpShop] Table created!", "Debug");
            }
            cn.commit();
            st.close();
        } catch (SQLException e) {
            System.out.println("[xpShop]: Error while creating tables! - " + e.getMessage());
            SQLErrorHandler(e);
        }
        time = (System.nanoTime() - time) / 1000000;
        auTrade.Logger("Created in " + time + " ms!", "Debug");
        //	    UpdateDB();
    }

    public boolean InsertAuction(String name, int xp) {
        long time = 0;
        auTrade.Logger("Insert player into table!", "Debug");
        time = System.nanoTime();
        try {
            PreparedStatement ps = cn.prepareStatement("INSERT INTO xpShop (Name, XP) VALUES (?,?)");
            ps.setString(1, name);
            ps.setInt(2, xp);
            ps.execute();
            cn.commit();
            ps.close();
        } catch (SQLException e) {
            System.out.println("[xpShop] Error while inserting XP into DB! - " + e.getMessage());
            SQLErrorHandler(e);
            return false;
        }
        time = (System.nanoTime() - time) / 1000000;
        auTrade.Logger("Finished in " + time + " ms!", "Debug");
        return true;
    }

    public boolean UpdateXP(String name, int xp) {
        long time = 0;
        auTrade.Logger("Updating XP in DB!", "Debug");
        time = System.nanoTime();
        try {
            PreparedStatement ps = cn.prepareStatement("UPDATE xpShop SET XP='" + xp + "' WHERE Name='" + name + "';");
            ps.executeUpdate();
            cn.commit();
            ps.close();

        } catch (SQLException e) {
            System.out.println("[xpShop] Error while inserting XP into DB! - " + e.getMessage());
            SQLErrorHandler(e);
            return false;
        }
        time = (System.nanoTime() - time) / 1000000;
        auTrade.Logger("Finished in " + time + " ms!", "Debug");
        return true;
    }

    public Connection createConnection() {
        if (auTrade.config.useMySQL) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                cn = DriverManager.getConnection("jdbc:mysql://" + auTrade.config.dbPath, auTrade.config.dbUser, auTrade.config.dbPassword);
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
        } else if (!auTrade.config.useMySQL) {
            try {
                try {
                    Class.forName("org.sqlite.JDBC");
                } catch (ClassNotFoundException cs) {
                    ErrorLogger(cs.getMessage());
                }
                cn = DriverManager.getConnection("jdbc:sqlite:plugins"
                        + File.separator + "xpShop" + File.separator
                        + "xpShop.sqlite");
                cn.setAutoCommit(false);
                return cn;
            } catch (SQLException e) {
                SQLErrorHandler(e);
            }
        }
        return null;
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
        auTrade.Logger("Checking if in table!", "Debug");
        time = System.nanoTime();
        Statement st = null;
        String sql;
        ResultSet result;
        String Name = "";
        try {
            st = cn.createStatement();
        } catch (SQLException e) {
            SQLErrorHandler(e);
        }
        sql = "SELECT COUNT(Name) from xpShop WHERE Name='" + name + "';";
        try {
            result = st.executeQuery(sql);
        } catch (SQLException e1) {
            SQLErrorHandler(e1);
            return false;
        }
        try {
            result.next();
            int b = result.getInt(1);
            auTrade.Logger("Lines: " + b, "Debug");
            if (b > 0) {
                a = true;
            }

            cn.commit();
            result.close();
            st.close();
        } catch (SQLException e2) {
            SQLErrorHandler(e2);
        }
        time = (System.nanoTime() - time) / 1000000;
        auTrade.Logger("Finished in " + time + " ms!", "Debug");
        return a;
    }

    public int getXP(String name) throws SQLException {
        long time = 0;
        auTrade.Logger("getting XP!", "Debug");
        time = System.nanoTime();
        Statement st = null;
        String sql;
        ResultSet result;
        try {
            st = cn.createStatement();
        } catch (SQLException e) {
            SQLErrorHandler(e);
        }
        sql = "SELECT XP from xpShop WHERE Name='" + name + "';";
        result = st.executeQuery(sql);
        int XP = 0;
        try {
            while (result.next() == true) {
                XP = result.getInt("XP");
            }
            cn.commit();
            st.close();
            result.close();
        } catch (SQLException e2) {
            SQLErrorHandler(e2);
        }
        time = (System.nanoTime() - time) / 1000000;
        auTrade.Logger("Finished in " + time + " ms!", "Debug");
        return XP;
    }
}
