package clearcut.data;
                          
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;         
import java.util.Map;     
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;       

import clearcut.Logger;
                   
public class Dataset {    
	
  private Logger logger = Logger.LOGGER(this);

	private String url;
	private String login;
	private String password;
               
	private Dataset() {    }         
	                                                        
	public Dataset( String fullURL, String loginName, String password ) {    
	  this();            
	  this.url = fullURL;   
	  this.login = loginName;
	  this.password = password;    
	
    }      
	                                                        
	public Dataset( String jdbcURL, String database, String loginName, String password ) {
		this( jdbcURL + "/" + database, loginName, password );	
	}
									
  public void delete(String tableName, String whereClause) throws DataException, SQLException 
	{
	  Connection con = null;
	  try {
	   	con = DriverManager.getConnection( this.url, this.login, this.password );
		new Delete( con, tableName, whereClause );
	  } finally { // If you can't close it, it's null, isn't open, or you can't do anything about it
	  	try{ con.close(); } catch( Exception e ) { } 
	  }   
	}                                       

   public void insert(String tableName, Map<String, String> values) throws DataException, SQLException
	{
	  Connection con = null;
	  try {
	  	con = DriverManager.getConnection( this.url, this.login, this.password );
		new Insert( con, tableName, values );
	  } finally { 
	  	try{ con.close(); } catch( Exception e ) { } 
	   }   
	}

   public void update(String tableName, Map<String, String> values, String whereClause) throws DataException, SQLException 
	{
	  Connection con = null;
	  try {
	  	con = DriverManager.getConnection( this.url, this.login, this.password );
		new Update( con, tableName, values, whereClause );
	  } finally { 
	  	try{ con.close(); } catch( Exception e ) { } 
	   }   
	}
         
   public void change(String tableName,  
	 
		 Map <String, String> insertOrUpdateValues, String whereClause) throws DataException, SQLException 
	{
	  Connection con = null;
	  try {
	  	con = DriverManager.getConnection( this.url, this.login, this.password );
		new Change( con, tableName, insertOrUpdateValues, whereClause );
	  } finally { 
	  	try{ con.close(); } catch( Exception e ) { } 
	   }   
	}
          
    public List< String [] > procedure(  String procedureName, String [] inputs ) 
				throws DataException, SQLException
    {     
	    Connection con = null;
		try {
	  	  con = DriverManager.getConnection( this.url, this.login, this.password );
		  return new Procedure( con, procedureName, inputs ).rows();
		} finally { 
	 	  try{ con.close(); } catch( Exception e ) { } 
	    }
	}          
	
	public List< Map <String, String>  > method(  String procedureName, String [] inputs ) 
				throws DataException, SQLException
    {     
	    Connection con = null;
		try {
	  	  con = DriverManager.getConnection( this.url, this.login, this.password );
		  return new Procedure( con, procedureName, inputs ).table();
		} finally { 
	 	  try{ con.close(); } catch( Exception e ) { } 
	    }
	}

	public Select select(String tableName, String [] columns, String whereClause) throws DataException, SQLException 
	{   
		Connection con = null;
		try {
	  	  con = DriverManager.getConnection( this.url, this.login, this.password );
		  return new Select( con, tableName, columns, whereClause );
		} finally { 
	 	  try{ con.close(); } catch( Exception e ) { } 
	    }   
    }      
      
  public List <String [] > rows( String tableName, String [] columnNames, String whereClause ) throws DataException, SQLException {
	  return this.select( tableName, columnNames, whereClause ).rows();
  }

  public List <Map< String, String >> table( String tableName, String [] columnNames, String whereClause ) throws DataException, SQLException {
	  return this.select( tableName, columnNames, whereClause ).table();
  }
	         
	public static String where( String whereClause ) 
	{        
	  if( whereClause == null || whereClause.trim().equals( "" )) return " ";  
	  if( whereClause.toLowerCase().trim().indexOf( "where" ) == 0 ) return " " + whereClause;
	  return " where " + whereClause;  
	}                                 
	                                   
	public static String Quote = "'";
	public static String DoubleQuote = "\"";
	public static char QUOTE = '\'';
	public static char DOUBLEQUOTE = '"';
	public static String quote( String quotable ) 
	{ 		
	  if( quotable.indexOf( QUOTE ) < 0 ) return Quote + quotable + Quote; 
	  return Quote + quotable.replaceAll( Quote, DoubleQuote ) + Quote; // MySQL doesn't care but M$$QL does  
	}
	                           
   
	   
}
