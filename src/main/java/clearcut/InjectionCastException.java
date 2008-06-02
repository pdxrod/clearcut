package clearcut;                         

public class InjectionCastException extends InjectionException {    
	public InjectionCastException() { super(); }
	public InjectionCastException( Exception x ) { super( x );  }
	public InjectionCastException( String msg, Exception x ) { super( msg, x );  }
	public InjectionCastException( String msg ) { super( msg );  }
}