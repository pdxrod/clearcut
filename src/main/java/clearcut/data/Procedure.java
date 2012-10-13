package clearcut.data;

import java.util.ArrayList;
import java.util.List;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import clearcut.Logger;

public class Procedure extends Results {

	private Logger logger = Logger.LOGGER(this);

	private Procedure() {
		super();
	}

	public Procedure(Connection con, String procedureName, String[] inputs)
			throws DataException, SQLException {
		this();
		String questionMarks = "( ";
		if (inputs != null)
			for (int i = 0; i < inputs.length; i++)
				questionMarks += "?, ";

		if (questionMarks.endsWith("?, "))
			questionMarks = questionMarks.substring(0,
					questionMarks.length() - 2); // Remove trailing comma
		questionMarks += " )";
		String procCall = "{call " + procedureName + questionMarks + "}";
		// {call change_clinic_name( ?, ? )}
		logger.log(procCall);
		CallableStatement cs = con.prepareCall(procCall);

		if (inputs != null)
			for (int i = 0; i < inputs.length; i++)
				cs.setString(i + 1, inputs[i]);

		ResultSet resultset = null;
		try {
			resultset = cs.executeQuery();
			ResultSetMetaData metaData = resultset.getMetaData();
			int columnCount = metaData.getColumnCount();
			String[] names = new String[columnCount];
			for (int col = 0; col < columnCount; col++)
				// java.sql uses 1-based indices
				names[col] = metaData.getColumnName(col + 1);
			this.init(names);
			// The procedure might be update-only. In MySQL, the only way to
			// avoid an exception is to look at the column count.
			if (columnCount > 0)
				while (resultset.next()) {
					String[] row = new String[columnCount];
					for (int col = 0; col < columnCount; col++)
						row[col] = resultset.getString(col + 1);
					this.add(row); // See Results.java
				}
		} catch (SQLException x) {
			// Some drivers will complain if there is no result set - the only
			// way to find out is to look at the message
			String msg = x.getMessage().toLowerCase();
			if (msg.indexOf("did not return") < 0)
				throw x;
		}
	}

}