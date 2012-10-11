package clearcut.data;
                          
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;         
import java.util.Map;
import java.sql.SQLException;     
import java.sql.Connection;
import java.sql.PreparedStatement;

import clearcut.Logger;

public class Delete {
  private Logger logger = Logger.LOGGER(this);
	
  private Delete() { }
  
  public Delete( Connection con, String tableName ) throws Exception {
    throw new Exception( "To do unqualified delete, call Delete() constructor with "+
     "three parameters, the last one null. This will delete ALL ROWS in table " + tableName + "." );  
  }
                                                                  
  /** whereClause should be null or something like "ID < 4 and NAME like 'JIM%'" */
  public Delete( Connection con, String tableName, String whereClause ) throws DataException        
  {
    this();
  	PreparedStatement statement = null;
	try {                             
		String sql = "delete from " + tableName + Dataset.where( whereClause );
		logger.log( sql );
		statement = con.prepareStatement( sql );
	    statement.executeUpdate();
		} catch( SQLException x ) { throw new DataException( x ); }
		finally { 
		try{ statement.close(); } catch( Exception e ) { } 
	}
  }   
	
	
}