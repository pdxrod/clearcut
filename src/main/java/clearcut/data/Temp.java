package clearcut.data;
                  
import clearcut.Logger;

public class Temp {    
	
  private Logger logger = Logger.LOGGER(this);                  
  public static final Temp TEMP = new Temp();
	
	/** Tests logging etc. */
	private Temp() 
	{   
	  super();
	  String msg = "Temp ";
	  for( int i = 1; i < 1001; i ++ ) 
	     this.logger.log( msg + i ); 	 
	}                                
   
}