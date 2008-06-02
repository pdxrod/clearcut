package clearcut.data;                         

public class DataException extends Exception {    
	private Exception innerException = null;  
	public Exception innerException() { return this.innerException; }                                     
	public DataException() { this.innerException = this; }
	public DataException( Exception x ) { super( x ); this.innerException = x; }
	public DataException( String msg, Exception x ) { super( msg, x ); this.innerException  = x; }
	public DataException( String msg ) { super( msg ); this.innerException = this; }
}