package clearcut.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import clearcut.Logger;

public class Update {

	private Logger logger = Logger.LOGGER(this);

	private Update() {
	}

	public Update(Connection con, String tableName, Map<String, String> values,
			String whereClause) throws DataException {
		this();
		if (values == null || values.size() < 1)
			throw new DataException(
					"Need to give at least one column name/column value pair");
		PreparedStatement statement = null;
		try {
			String sql = "update " + tableName + " set ";
			for (String key : values.keySet()) {
				String value = values.get(key);
				if (value == null)
					value = "null";
				else
					value = Dataset.quote(value);
				sql += key + " = " + value + ", ";
			}
			sql = sql.substring(0, sql.length() - 2); // Remove trailing comma
			sql += Dataset.where(whereClause);
			logger.log(sql);
			statement = con.prepareStatement(sql);
			statement.executeUpdate();
		} catch (SQLException x) {
			throw new DataException(x);
		} finally {
			try {
				statement.close();
			} catch (Exception e) {
			}
		}
	}

}