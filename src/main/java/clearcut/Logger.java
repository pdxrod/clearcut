package clearcut;
    
import java.io.File;      
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;  
import java.io.IOException;     
import java.io.FilenameFilter;
import java.util.Properties;      
import java.util.List;
import java.util.Map;
import java.util.HashMap; 
import java.util.Date;
import java.text.SimpleDateFormat;
import static clearcut.Injector.INJECTOR;
         
/** Java i/o */
public class Logger 
{ 
	public enum Logging { NONE, FILE, CONSOLE, BOTH }
	      
  private static String DEFAULT_LOG_FILE = "logger.log";    
  private static int MAX_LINES = 333;
  private static int MIN_FILES = 5;    
  private static int MAX_FILES = 10000;     
  private static String LOG_FORMAT = "%0" +(""+MAX_FILES).length()+"d";
  private static String LONG_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";
  private static String SHORT_FORMAT = "HH:mm:ss.SS";
	private static Map < String, Logger > loggers;     
  private int rotation = MIN_FILES;      
  private int rotator = 0;             
  private int numLines = 0;
  private Logging loggin = Logging.CONSOLE;
  private int maxLines = MAX_LINES ;     
  private Class caller;
  private String logPath;
      	
  public static Logger LOGGER( Object caller ) {                                  
	  if( Logger.loggers == null ) 
			Logger.loggers = new HashMap< String, Logger > ();
	  String packageName = caller.getClass().getPackage().getName();
	  if( Logger.loggers.get( packageName ) == null )
	    Logger.loggers.put( packageName, new Logger(caller) );
	  return Logger.loggers.get( packageName );
	}    	
  private Logger() { 
		super(); 
		if( Logger.loggers == null ) Logger.loggers = new HashMap< String, Logger > (); 
	}                     
  private Logger (Object caller)  {  	 	  
	  this(); 
	  load( caller );
	}
	
	private synchronized void load( Object caller ) {
	  OutputStream outStream = null; 
		try {			
      setCaller( caller );
	    setRotation( INJECTOR.property( "logging",  "files" ) );     	      
	    setLogPath ( INJECTOR.iniPath() + File.separator + "log" + File.separator );
		  if( getLogging() == Logging.FILE || getLogging() == Logging.BOTH ) 
		    outStream = Logger.test( getLogPath() );	  
      File file = new File  ( getLogPath() );
		  File [] files = file.listFiles( new LogFilter() ) ;
		  for( File logFile : files ) 
			  if( logFile.getName().startsWith( getCaller().getPackage().getName() ) ) 
					incRotator( );                                                        
	  } catch( IOException x )  	{ 
		  System.out.println( x.getMessage() );
		  this.loggin = Logging.CONSOLE;   
		} catch( InjectionException j )   { 
		  j.printStackTrace();                                                      
		} finally {
		  try { outStream.close(); } catch ( Exception e ) { } 
		}
  }                                                              
  
	private static synchronized OutputStream test(String logPath) throws IOException {
	 	 File file = new File( logPath );
	   if( ! file.exists() ) 
			   if( ! file.mkdirs() ) 
					   throw new IOException("Unable to create "+logPath);
		 String fileName = logPath + DEFAULT_LOG_FILE;
		 file = new File( fileName );
	 	 if( file.exists() ) 
				  if( ! file.delete() ) 
	            throw new IOException("Unable to delete "+fileName);
	   OutputStream outStream = new FileOutputStream( file, true );	
		 byte [] timedate = new byte[ 32 ]; 
		 String datetime = new SimpleDateFormat( LONG_FORMAT ).format( new Date() );   				 
		 for( int i = 0; i < timedate.length && i < datetime.length(); i ++ )
		   timedate[ i ] = (byte) datetime.charAt( i );  
		 timedate[ 31 ] = (byte) '\n';    
	   outStream.write( timedate );         
	   return outStream;
	}          
	
	/** Makes a file name from package name of calling class and a rotating number.
      Creates new file if max no. of lines written in old file. Deletes old files
      if their numbers have been used and has various other side-effects. */
  private String rotate( ) throws IOException {
	  int num = getLineNum() + 1;  		
		if( num == 1 || num > MAX_LINES ) {
			this.numLines = 1;                            
		  incRotator();  
		  int rotated = getRotator();
		  String fileName = fileName( rotated ++ );
		  if( rotated > getRotation() ) rotated = 1;
		  String nextFile = fileName( rotated );
		  File oldFile = new File( fileName );
		  File newFile = new File( nextFile );
		  if( oldFile.exists() && (oldFile.length() > MAX_LINES * 50L || newFile.exists()) ) {          
				if( ! oldFile.delete() ) {
					setLogging( Logging.CONSOLE ); 				  
					throw new IOException("Unable to delete old log file "+fileName); 
				}
			}
		} else this.numLines = num;
	  return this.fileName( getRotator() );
	}

  public void log( String msg ) {       
   	try {           		
			Logging logging = this.getLogging();      
			if( logging == Logging.CONSOLE || logging == Logging.BOTH ) 
			   System.out.println( new SimpleDateFormat( SHORT_FORMAT ).format( new Date() ) + " " + msg );       
			if( logging == Logging.FILE || logging == Logging.BOTH ) 
		     log( msg, getCaller().getPackage().getName() );
		} catch( InjectionException x ) {
			System.out.println( "Logger error" );
			x.printStackTrace();
		}
  }
         
  private void log( String msg, String packageName ) {
  	OutputStream outStream = null;  
    String logFile = "log file";     
		try {
 	  	 logFile = rotate( );                                  
			 outStream = this.open( logFile );
			 this.write( outStream, msg );
	  } catch( IOException o )  	{ 
			 System.out.println( "Unable to write "+logFile );
			 setLogging( Logging.CONSOLE );
		} finally {
		   try { outStream.close(); } catch ( Exception e ) { } 
		}
	}     
            
  private OutputStream open( String logFile ) throws IOException {     
		File file = new File( logFile ); 	
		if( file.exists() ) 
			return new FileOutputStream( file, true );
 		
		OutputStream outStream = new FileOutputStream( new File( logFile ), false );	
		byte [] buffer = new byte[ 32 ];    
		String datetime = new SimpleDateFormat( LONG_FORMAT ).format( new Date() );   				 
	  for( int i = 0; i < buffer.length && i < datetime.length(); i ++ )
	     buffer[ i ] = (byte) datetime.charAt( i );  
	  buffer[ 31 ] = (byte) '\n';    
    outStream.write( buffer );   
    return outStream;
	}
	
	private void write( OutputStream stream, String message ) throws IOException {  		                          
     byte [] timedate = new byte[ 11 ];     
		 String datetime = new SimpleDateFormat( SHORT_FORMAT ).format( new Date() );				 
		 for( int i = 0; i < timedate.length && i < datetime.length(); i ++ )
		   timedate[ i ] = (byte) datetime.charAt( i );
		 stream.write( timedate );
		 timedate = new byte[1]; 
		 timedate[0] = (byte) ' ';
		 stream.write( timedate );
		 timedate = (getCaller().getSimpleName() + " " + message).getBytes();
		 stream.write( timedate );
		 timedate = new byte[2];   
		 timedate[0] = (byte) '\r'; // M$ 
		 timedate[1] = (byte) '\n';
		 stream.write( timedate );
	}

  private void setLogging( Logging logging ) { this.loggin = logging; }
  private Logging getLogging()  throws InjectionException {                    
    String level = INJECTOR.property( "logging", "level" );                                                             
    if( level != null )
      for (Logging l : Logging.values())
        if( l.toString().equals( level )) this.loggin = l;  
    return this.loggin;       
  }
  private void setRotation( String numFiles ) {   
 	  if( numFiles == null || numFiles.length() < 1 ) 
			this.rotation = MIN_FILES; 
		else {
			int numFiles2Rotate = Integer.parseInt( numFiles );
			if( numFiles2Rotate < 1 ) this.rotation = 1;
			else if( numFiles2Rotate > MAX_FILES ) this.rotation = MAX_FILES; 
			else this.rotation = numFiles2Rotate;  
    }
  }                         
  private int getRotation() { return this.rotation; }         
  private void setLogPath( String logPath ) { this.logPath = logPath; }
  public String getLogPath() { return this.logPath; }   
  public Logger.LogFilter getLogFilter() { LogFilter filter = new LogFilter(); return filter; }
  private int getRotator() { return this.rotator; }  
	private void incRotator() {      
		int rotNum =1+ this.getRotator();
	  if( rotNum > getRotation() ) 
			this.rotator = 1; 
		else
			this.rotator = rotNum;
	}                                	
  private int getLineNum() { return this.numLines; }      
  private Class getCaller() { return this.caller; } 
  private void setCaller( Object caller ) { this.caller = caller.getClass(); }

  private String fileName( int rotNum ) { 
	  return 
	  	getLogPath() + 
				getCaller().getPackage().getName() + "." + 
				  String.format( LOG_FORMAT, rotNum ) + ".log"; 
	}
 
	public class LogFilter implements FilenameFilter {
	    public boolean accept(File dir, String name) {
	        return (name.endsWith(".log"));
	    }
	}
}
