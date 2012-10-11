package example.exceptions;                         

public class PDXception extends Exception {    
	private Exception innerException = null;  
	public Exception innerException() { return this.innerException; }                                     
	public PDXception() { this.innerException = this; }
	public PDXception( Exception x ) { super( x ); this.innerException = x; }
	public PDXception( String msg, Exception x ) { super( msg, x ); this.innerException  = x; }
	public PDXception( String msg ) { super( msg ); this.innerException = this; }
}