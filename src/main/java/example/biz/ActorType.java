package example.biz; 

public class ActorType implements IActorType 
{ 
  private String name;                                          
  public ActorType( String name ) { this.name = name; }   
  public String name() { return name; }  
}