package clearcut.data;
 
import java.util.Map;
import java.util.List;
                        
public interface IResults {
    public List <String [] > rows();                                
    public List <Map< String, String >> table() throws DataException;    
	public String [] names(); 
}