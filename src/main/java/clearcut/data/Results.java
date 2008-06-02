package clearcut.data;
 
import java.util.Map;    
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
				
import clearcut.Logger;
                               
/** @see Select.java
    @see Procedure.java 
rows() returns the */
public class Results implements IResults {
    private String [] names;
    private List < String [] > rows;      
	private List <Map< String, String >> table;
	private Logger logger = Logger.LOGGER(this);
		  
	protected Results() { 	 
		this.names = new String [0];  
	    this.rows = new ArrayList < String [] > ();
	    this.table = new ArrayList <Map< String, String >> ();
	}    
	            
	public void init( String [] names ) { this.names = names; }
	public String [] names() { return this.names; }
	public void add( String[] row ) { rows.add( row ); }       
    public List <String [] > rows() { return this.rows; }
                                
	/** Returns rows as a list of string-to-string maps, which can be indexed by column name */
	public List <Map< String, String >> table() throws DataException {      
	 // Initialize list of string-indexed data if it isn't already and there is some data  
    	if( this.table.size() == 0 && this.rows().size() > 0 ) { 
	      if( this.rows().get( 0 ).length != this.names().length ) 
			throw new DataException( "Column names has not been initialized correcly using the init() method: " +
			          "columns should be "+this.rows().get( 0 ).length+" in width but are "+this.names().length); 
		  for( String[] row : this.rows() ) {
		      Map< String, String > map = new HashMap< String, String> ( row.length );   
		      for( int col = 0; col < row.length; col ++ )
			    map.put( this.names[ col ], row[ col ] );
		      this.table.add( map );                                  
		    
		  }                                                      
	    }
		return this.table;                      		
	}                        
}