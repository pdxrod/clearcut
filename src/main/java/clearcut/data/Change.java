package clearcut.data;
                          
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;         
import java.util.Map;            
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;   

import clearcut.Logger;
                             
public class Change {
	 
  private Logger logger = Logger.LOGGER(this);
  	
  private Change() { }
                                     
  public static String [] STAR = { "*" };

  /** If you don't know whether the row already is in the table, call Change() with a where clause saying eg.
      'ID = 42'. If there is a row with ID 42, it will try to update, otherwise, it will try to insert. */
  public Change( Connection con, String tableName,  

	 Map <String, String> insertOrUpdateValues, String whereClause ) throws DataException      
  {
    this();
    if( whereClause == null || whereClause.trim().length() < 1 ) throw new DataException( "Must have a where clause in case this method does an update" );   
    if( insertOrUpdateValues == null || insertOrUpdateValues.size() < 1 ) throw new DataException( "Need to give at least one column name/column value pair for values to insert or update" );
    
    Select selection = new Select( con, tableName, STAR, whereClause );
    if( selection.rows().size() > 0 ) 
       new Update( con, tableName, insertOrUpdateValues, whereClause );
    else
	   new Insert( con, tableName, insertOrUpdateValues );
    
  }   
	
	
}