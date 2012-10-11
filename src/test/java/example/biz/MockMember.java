package example.biz; 

public class MockMember implements IMember 
{            	
  String firstName; String middleName; String lastName; IActorType actorType;  
  public MockMember() {}
  public MockMember( String firstName, String middleName, String lastName, IActorType actorType )
  {
    this.firstName = firstName;
    this.middleName = middleName;
    this.lastName = lastName;
    this.actorType = actorType;
  }   
  public String fullName() { return "Mock Member"; }                            
  public String name() { return fullName(); }  
  public String actorType() { return "Member"; }
  public Boolean gender() { return null; }

}