package me.ibhh.xpShop.send.sql;

/**
 *
 */
import java.io.File;
import java.sql.*;
import java.util.ArrayList;

import me.ibhh.xpShop.xpShop;

/**
 * @author ibhh
 * 
 */
public class SendDatabase {

	private Connection	cn;
	private xpShop		plugin;

	public SendDatabase(xpShop plugin) {
		this.plugin = plugin;
		cn = null;
	}

	public boolean deleteDB() {
		Statement st = null;
		boolean temp = false;
		long time = 0;
		plugin.Logger("deleting table!", "Debug");
		time = System.nanoTime();
		try {
			String sql = "drop table if exists xpShop;";
			st = cn.createStatement();
			if (plugin.getConfig().getBoolean("SQL")) {
				st.executeUpdate(sql);
				plugin.Logger("Table deleted!", "Debug");
				temp = true;
			} else {
				st.executeUpdate("drop table if exists xpShop;");
				plugin.Logger("Table deleted!", "Debug");
				temp = true;
			}
			cn.commit();
			st.close();
		} catch (SQLException e) {
			plugin.Logger("Error while creating tables! - " + e.getMessage(), "Error");
			SQLErrorHandler(e);
		}
		time = (System.nanoTime() - time) / 1000000;
		plugin.Logger("DELETED in " + time + " ms!", "Debug");
		// UpdateDB();
		return temp;
	}

	public void PrepareDB() {
		Statement st = null;
		long time = 0;
		plugin.Logger("Creaeting table!", "Debug");
		time = System.nanoTime();
		try {
			String sql = "CREATE TABLE IF NOT EXISTS xpShopSend (ID INT PRIMARY KEY AUTOINCREMENT, Name VARCHAR(30), Sender VARCHAR(30), Message VARCHAR(100), XP INT, Status INT);";
			st = cn.createStatement();
			if (plugin.getConfig().getBoolean("SQL")) {
				st.executeUpdate(sql);
				plugin.Logger("Table created!", "Debug");
			} else {
				st.executeUpdate("CREATE TABLE IF NOT EXISTS xpShopSend (ID INT PRIMARY KEY AUTOINCREMENT, Name VARCHAR, Sender VARCHAR, Message VARCHAR, XP INT, Status INT);");
				plugin.Logger("Table created!", "Debug");
			}
			cn.commit();
			st.close();
		} catch (SQLException e) {
			plugin.Logger("Error while creating tables! - " + e.getMessage(), "Error");
			SQLErrorHandler(e);
		}
		time = (System.nanoTime() - time) / 1000000;
		plugin.Logger("Created in " + time + " ms!", "Debug");
		// UpdateDB();
	}

	public boolean InsertSend(XPSend send) {
		return InsertSend(send.getPlayer(), send.getSender(), send.getSendedXP(), send.getMessage(), send.getStatus());
	}

	public boolean InsertSend(String player, String sender, int sendedXP, String message, int status) {
		long time = 0;
		plugin.Logger("Insert new XPSend into table!", "Debug");
		time = System.nanoTime();
		try {
			PreparedStatement ps = cn.prepareStatement("INSERT INTO xpShopSend (Name, Sender, Message, XP, Status) VALUES (?,?,?,?,?)");
			ps.setString(1, player);
			ps.setString(2, sender);
			ps.setInt(3, sendedXP);
			ps.setString(4, message);
			ps.setInt(5, status);
			ps.execute();
			cn.commit();
			ps.close();
		} catch (SQLException e) {
			plugin.Logger("Error while inserting XP into DB! - " + e.getMessage(), "Error");
			SQLErrorHandler(e);
			return false;
		}
		time = (System.nanoTime() - time) / 1000000;
		plugin.Logger("Finished in " + time + " ms!", "Debug");
		return true;
	}

	public boolean setStatus(String player, int id, int status) {
		long time = 0;
		plugin.Logger("Updating XP in DB!", "Debug");
		time = System.nanoTime();
		try {
			PreparedStatement ps = cn.prepareStatement("UPDATE xpShopSend SET Status='" + status + "' WHERE Name='" + player + "' AND ID=" + id + ";");
			ps.executeUpdate();
			cn.commit();
			ps.close();

		} catch (SQLException e) {
			plugin.Logger("Error while inserting XP into DB! - " + e.getMessage(), "Error");
			SQLErrorHandler(e);
			return false;
		}
		time = (System.nanoTime() - time) / 1000000;
		plugin.Logger("Finished in " + time + " ms!", "Debug");
		return true;
	}

	public Connection createConnection() {
		if (plugin.config.useMySQL) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
				cn = DriverManager.getConnection("jdbc:mysql://" + plugin.config.dbPath, plugin.config.dbUser, plugin.config.dbPassword);
				cn.setAutoCommit(false);
				return cn;
			} catch (SQLException e) {
				plugin.Logger("could not be enabled: Exception occured while trying to connect to DB", "Error");
				SQLErrorHandler(e);
				if (cn != null) {
					plugin.Logger("Old Connection still activated", "Error");
					try {
						cn.close();
						plugin.Logger("Old connection that was still activated has been successfully closed", "Error");
					} catch (SQLException e2) {
						plugin.Logger("Failed to close old connection that was still activated", "Error");
						SQLErrorHandler(e2);
					}
				}
				return null;
			} catch (ClassNotFoundException e) {
				ErrorLogger(e.getMessage());
				return null;
			}
		} else if (!plugin.config.useMySQL) {
			try {
				try {
					Class.forName("org.sqlite.JDBC");
				} catch (ClassNotFoundException cs) {
					ErrorLogger(cs.getMessage());
				}
				cn = DriverManager.getConnection("jdbc:sqlite:plugins" + File.separator + "xpShop" + File.separator + "xpShop.sqlite");
				cn.setAutoCommit(false);
				return cn;
			} catch (SQLException e) {
				SQLErrorHandler(e);
			}
		}
		return null;
	}

	private void ErrorLogger(String Error) {
		plugin.Logger(Error, "Error");
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
			plugin.Logger("Failed to close connection to DB!", "Error");
			SQLErrorHandler(e);
			return false;
		}
	}

	public ArrayList<XPSend> getOpenTransactions(String name) throws SQLException {
		long time = 0;
		plugin.Logger("getting Transactions!", "Debug");
		time = System.nanoTime();
		Statement st = null;
		String sql;
		ResultSet result;
		try {
			st = cn.createStatement();
		} catch (SQLException e) {
			SQLErrorHandler(e);
		}
		sql = "SELECT * from xpShopSend WHERE Name='" + name + "' AND Status=0;";
		result = st.executeQuery(sql);
		ArrayList<XPSend> xpSends = new ArrayList<XPSend>();
		try {
			while (result.next() == true) {
				XPSend temp = new XPSend(result.getString("Name"), result.getString("Sender"), result.getInt("XP"), result.getString("Message"), result.getInt("Status"));
				temp.setId(result.getInt("ID"));
				xpSends.add(temp);
			}
			cn.commit();
			st.close();
			result.close();
		} catch (SQLException e2) {
			SQLErrorHandler(e2);
		}
		time = (System.nanoTime() - time) / 1000000;
		plugin.Logger("Finished in " + time + " ms!", "Debug");
		return xpSends;
	}
}
