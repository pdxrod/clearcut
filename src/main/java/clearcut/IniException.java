package clearcut;                         

public class IniException extends Exception {    
	private Exception innerException = null;  
	public Exception innerException() { return this.innerException; }                                     
	public IniException() { this.innerException = this; }
	public IniException( Exception x ) { super( x ); this.innerException = x; }
	public IniException( String msg, Exception x ) { super( msg, x ); this.innerException  = x; }
	public IniException( String msg ) { super( msg ); this.innerException = this; }
}