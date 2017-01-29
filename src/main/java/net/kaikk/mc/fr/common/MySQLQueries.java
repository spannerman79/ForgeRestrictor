package net.kaikk.mc.fr.common;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLQueries {
	public PreparedStatement query;
	
	public MySQLQueries(MySQLConnection conn) throws SQLException {
		this.query = conn.prepareStatement("");
	}
}
