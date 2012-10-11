package clearcut;                         

public class InjectionException extends Exception {    
	private Exception innerException = null;  
	public Exception innerException() { return this.innerException; }                                     
	public InjectionException() { this.innerException = this; }
	public InjectionException( Exception x ) { super( x ); this.innerException = x; }
	public InjectionException( String msg, Exception x ) { super( msg, x ); this.innerException  = x; }
	public InjectionException( String msg ) { super( msg ); this.innerException = this; }
}