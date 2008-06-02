package clearcut;
                 
import java.io.File;
            
import org.junit.Test;
import junit.framework.TestCase;
import static org.junit.Assert.assertTrue;

import clearcut.Logger;   
import clearcut.data.Temp;   
import static clearcut.Injector.INJECTOR;  
			
public class LoggerTest extends TestCase {                                      
	                       
	private Logger logger = null;
	public void setUp() throws Exception {        
	  if( this.logger == null ) this.logger = Logger.LOGGER(this);
	}    
	
  public void testTemp() throws Exception { 
    Temp temp = Temp.TEMP; // Should do some logging
	}
	    
	public void testLogging() throws Exception {  
	  String msg = "Logger Test ";
	  for( int i = 1; i < 1001; i ++ ) 
	    this.logger.log( msg + i );
		File file = new File  ( this.logger.getLogPath() );
	  File [] files = file.listFiles( this.logger.getLogFilter() ) ;
	  int bigOnes = 1; 
	  int smallOnes = 0;
	  for( File logFile : files ) 
			 if( logFile.length() > 512 )
				 bigOnes ++;	    
			 else
			   smallOnes ++;
				
		assertTrue( "Size matters - there are " + smallOnes + " small ones and "
		 + bigOnes + " big ones", bigOnes > smallOnes * 2 ); 
    
	}
}