package clearcut.data;
                          
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;         
import java.util.Map;    
import java.sql.SQLException;     
import java.sql.Connection;   
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.PreparedStatement;
                   
import clearcut.Logger;

/** Encapsulates a resultset from a database query as a list of string arrays, regardless of the types of the database columns */
public class Select extends Results {
	                                
	private Logger logger = Logger.LOGGER(this);
	
	private Select() { super(); }
	
	public Select( Connection con, String tableName, String [] columnNames, String whereClause ) throws DataException
	{                                          
	 this();                     
                                         
	 if( columnNames == null || columnNames.length < 1 ) throw new DataException( "Must provide column names" );
	 PreparedStatement statement = null;
	 try {                                 
		String sql = "select ";
	    for( String col : columnNames ) 
		  sql += col + ", ";                    
		sql = sql.substring( 0, sql.length() - 2 ); // Remove trailing comma  
		sql += " from " + tableName;
	    sql += Dataset.where( whereClause );     
	
    logger.log( sql );

		statement = con.prepareStatement( sql );
	    ResultSet resultset = statement.executeQuery();
        
		ResultSetMetaData metaData = resultset.getMetaData();
		int columnCount = metaData.getColumnCount();  
		String [] names = new String [ columnCount ];
	    for( int col = 0; col < columnCount; col ++ ) // java.sql uses 1-based indices 
	      names[ col ] = metaData.getColumnName( col + 1 );
	    this.init( names );                           
	
	    while (resultset.next()) {
		  String [] row = new String [columnCount ]; 
		  for( int col = 0; col < columnCount; col ++ )
		    row[col ] = resultset.getString( col + 1 );		           
	      this.add( row ); // See Results.java
		}	
	    
	  } catch( SQLException x ) { throw new DataException( x ); }
	    finally { 
	 		try{ statement.close(); } catch( Exception e ) { } 
	    } 
	
	}   
    					 
    
}