package example.biz; 

public class Member implements IMember 
{      
	
  String firstName = ""; String middleName = null; String lastName = ""; IActorType actorType; Boolean gender;
  public Member() {}
  public Member( String firstName, String middleName, String lastName, IActorType actorType, Boolean gender )
  {
    this.firstName = firstName;
    this.middleName = middleName;
    this.lastName = lastName;
    this.actorType = actorType;
    this.gender = gender;
  }   
  public String fullName() { return "" + firstName + 
		(middleName == null ? " " : " " + middleName + " ") + lastName; }
  public String name() { return fullName(); }                            
  public Boolean gender() { return this.gender; }       
	public String actorType() { if( actorType == null ) return null; return actorType.name(); }

}