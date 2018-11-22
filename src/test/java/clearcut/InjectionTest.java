package clearcut;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;
import java.lang.reflect.Constructor;

import org.junit.Test;
import junit.framework.TestCase;
import static org.junit.Assert.*;

import example.biz.IMember;
import example.biz.IMedication;
import static clearcut.Injector.INJECTOR;

/**
 * To run this, app.ini should contain <code>

[injection]
realMember=example.biz.Member
mockMember=example.biz.MockMember
member=mockMember
doubleIndirection=member
noMember=thisDoesNotExist
noParamsMember=example.biz.Member  ( )
emptyMember=

a_ctor=example.biz.ActorType("Patient") # Test spacing, quotes, booleans, objects...
a_member=example.biz.Member('Rod',"David" , "O'Reilly", a_ctor, true)

a_drug=example.biz.Medication( "Prozac", 'Useless', 0, 42) # Numbers and primitives
number_cast_error = example.biz.Medication( "Zoloft", 'Useless', 2.0, -42 )

should_fail = example.biz.Medication( "Pseudoephedrine", 'Wrong, 1, 42 ) ; Missing quote
should_also_fail=example.biz.Medication("wrong","number","and""type" "of","parameters" )
should_fail_too= example.biz.Member( "No closing", 'parenthesis)', a_ctor, true

no_commas=example.biz.Member( "Who"'needs' 'commas?',     dr_who false   ) ) )
dr_who=example.biz.ActorType("Doctor") # Commas, spaces and extra brackets are optional

dr_666=example.biz.ActorType("Doctor")
num_in_name=example.biz.Member( 'Jim' null 'Catch22' dr_666 true )

too_many_minuses=java.lang.Integer(-42-1 )
too_many_dots=java.lang.Integer( 0.1.2 )
dot_at_end_err=java.lang.Long(42.)
ok_number=java.lang.Integer( -42)
overflower=java.lang.Byte(42000000) # 2147483648 is MaxInt + 1 - works OK in a long parameter
long_higher_than_int=example.biz.Medication( "Paxil" , 'Useless' , 42 , 2147483648 )

</code>
 */
public class InjectionTest extends TestCase {
	private Logger logger = Logger.LOGGER(this);

	public InjectionTest() {
	}

	public void testAppIni() throws Exception {
		String properties = INJECTOR.fessUp();
		assertTrue("Should contain NO SECTION section",
				properties.contains(Ini.NO_SECTION));
		assertTrue("Should have some properties", properties.contains("="));
	}

	public void testCreatingMockMemberFromInjection() throws Exception {
		IMember member = (IMember) INJECTOR.implement("mockMember");
		assertEquals("Mock member should have 'Mock Member' as its name",
				"Mock Member", member.name());
		assertNotNull("Mock member should have a mock actor type",
				member.actorType());
	}

	public void testCreatingRealMemberFromInjection() throws Exception {
		IMember member = (IMember) INJECTOR.implement("realMember");
		assertNotSame("A real member should not be called 'Mock Member'",
				"Mock Member", member.name());
		assertNull(
				"Real member should have a null actor type 'cos it did not get set yet",
				member.actorType());
	}

	public void testEmptyMemberCausesException() throws Exception {
		try {
			INJECTOR.implement("emptyMember");
		} catch (InjectionException e) {
			return;
		}
		throw new Exception("We should not have reached here");
	}

	public void testNonExistentMemberCausesException() throws Exception {
		try {
			IMember member = (IMember) INJECTOR.implement("not.there");
		} catch (InjectionException e) { /* OK */
			return;
		}
		throw new Exception("We should not be here");
	}

	public void testConstructorInjectionWithObjectParameters() throws Exception {
		IMember member = (IMember) INJECTOR.implement("a_member");
		String name = member.name();
		assertEquals("Name should start with 'Rod'", "Rod",
				name.substring(0, 3));
		assertFalse("Name should not be 'Johnson'",
				name.indexOf("Johnson") > -1);
		String actorType = member.actorType();
		assertEquals("Should be 'Patient'", "Patient", actorType);
		Boolean gender = member.gender();
		assertTrue("Should be male", gender.booleanValue());
	}

	public void testConstructorWithParenthesesButNoParameters()
			throws Exception {
		IMember member = (IMember) INJECTOR.implement("noParamsMember");
		String name = member.name(); // Uninitialized name might be null,
										// spaces, the word null...
		assertTrue("Name is " + name, name == null || name.trim().equals("")
				|| name.indexOf("null") > -1);
	}

	public void testInjectionIndirection() throws Exception {
		IMember member = (IMember) INJECTOR.implement("doubleIndirection");
		String name = member.name(); // doubleIndirection points to member in
										// app.ini, and member can point to
										// realMember or mockMember
		assertTrue("Name is " + name, name == null || name.trim().equals("")
				|| name.indexOf("null") > -1 || name.indexOf("Mock") > -1);
	}

	public void testIndirectionFailure() throws Exception {
		try {
			IMember member = (IMember) INJECTOR.implement("noMember");
		} catch (InjectionException e) {
			return;
		}
		throw new Exception("Reference to non-reference failed to throw");
	}

	public void testConstructingNumbers() throws Exception {
		Number n = (Number) INJECTOR.implement("ok_number");
		assertEquals("Should be minus forty-two", -42, n.intValue());
	}

	// Found a bug by running on Windows: added IllegalArgumentException check
	// in Injector.implement()
	// Class is java.lang.Integer, which has two 1-param constructors; parameter
	// is an Integer, -42
	public void testIllegalArgument() throws Exception {
		Class[] classes = new Class[1];
		Class claS$ = Class.forName("java.lang.Integer");
		Constructor[] constructors = claS$.getConstructors();
		boolean found = false;
		for (Constructor con : constructors) {
			try {
				classes = con.getParameterTypes();
				if (classes.length == 1) {
					Object[] parameters = new Object[1];
					parameters[0] = new Integer(-42);
					con.newInstance(parameters);
					found = true;
				}
			} catch (IllegalArgumentException u) {
				// Try another constructor
			}
		}
		assertTrue("Didn't succeed in constructing an Integer with -42", found);
	}

	public void testLongParameter() throws Exception {
		IMedication med = (IMedication) INJECTOR.implement("long_higher_than_int");
		long outOf = med.outOf();
		assertTrue("" + outOf + " should be higher than " + Integer.MAX_VALUE,
				((long) Integer.MAX_VALUE) < outOf);
	}

	public void testTooManyMinusesInNumberFails() throws Exception {
		Number n = null;
		try {
			n = (Number) INJECTOR.implement("too_many_minuses");
		} catch (InjectionException e) {
			return;
		}
		throw new Exception(
				"Too many minuses in number didn't fail like one might expect");
	}

	public void testTooManyDotsInNumberFails() throws Exception {
		Number n = null;
		try {
			n = (Number) INJECTOR.implement("too_many_dots");
		} catch (InjectionException i) {
			return; // OK
		}
		throw new Exception(
				"Too many dots in number, 0.1.2, failed to fail like it should have");
	}

	public void testDotAtEndOfNumberIsNoGood() throws Exception {
		try {
			Long n = (Long) INJECTOR.implement("dot_at_end_err");
		} catch (InjectionException e) {
			return;
		}
		throw new Exception(
				"Number ending in a dot didn't throw exception - it should have");
	}

	public void testOverflow() throws Exception {
		try {
			Byte n = (Byte) INJECTOR.implement("overflower");
		} catch (InjectionException e) {
			return;
		}
		throw new Exception("Big number didn't throw exception but should have done so");
	}

	public void testCommasAreOptionalAndForwardReferencesWork()
			throws Exception {
		IMember member = (IMember) INJECTOR.implement("no_commas");
		String name = member.name();
		assertEquals("Who?", "Who", name.substring(0, 3));
	}

	public void testNumberInNameAndReference() throws Exception {
		IMember member = (IMember) INJECTOR.implement("num_in_name");
		String name = member.name();
		assertTrue("'Catch22' should be in name, but name is " + name,
				name.indexOf("Catch22") > -1);
		String actorType = member.actorType();
		assertEquals("Should be 'Doctor'", "Doctor", actorType);
	}

	public void testConstructorInjectionWithNumberParameters() throws Exception {
		IMedication med = (IMedication) INJECTOR.implement("a_drug");
		String name = med.name();
		assertEquals("Should be called Prozac", "Prozac", name);
		name = med.cla$$();
		assertEquals("Should be called Useless", "Useless", name);
		int num = med.rank();
		assertEquals("Rank should be zero", 0, num);
		long outOf = med.outOf();
		assertEquals("Out-of number should be 42", 42L, outOf);
	}

	public void testConstructorInjectionWithDoubleToLongCastFails()
			throws Exception {
		IMedication med = null;
		try {
			med = (IMedication) INJECTOR.implement("number_cast_error");
		} catch (InjectionException e) {
			return;
		}
		throw new Exception("This exception should not happen");
	}

	public void testConstructorInjectionWithFaultyParameterFails()
			throws Exception {
		IMedication med = null;
		try {
			med = (IMedication) INJECTOR.implement("should_fail");
		} catch (InjectionException e) {
			return;
		}
		throw new Exception("We should not be here");
	}

	public void testConstructorInjectionWithWrongNumberOfParametersFails()
			throws Exception {
		IMedication med = null;
		try {
			med = (IMedication) INJECTOR.implement("should_also_fail");
		} catch (InjectionException e) {
			return;
		}
		throw new Exception("We should not be here");
	}

	public void testConstructorInjectionWithNoClosingParenthesisFails()
			throws Exception {
		IMember aPerson = null;
		try {
			aPerson = (IMember) INJECTOR.implement("should_fail_too");
		} catch (InjectionException e) {
			return;
		}
		throw new Exception("Something has gone wrong");
	}

	// Please keep
	public void testClassCastingWithNumbers() throws Exception {
		Integer i = new Integer(Integer.MAX_VALUE);
		Long l = new Long(Long.MAX_VALUE);
		Byte b = new Byte(Byte.MAX_VALUE);
		Float f = new Float(Float.MAX_VALUE);
		Short s = new Short(Short.MAX_VALUE); // Has anyone EVER used a Short in
												// Java before?
		Double d = new Double(Double.MAX_VALUE);
		List<Number> list = new ArrayList<Number>();
		list.add(i);
		list.add(l);
		list.add(b);
		list.add(f);
		list.add(s);
		list.add(d);
		Object[] arr = list.toArray();
		String c = "";

		for (Number n : list) {
			for (Object o : arr) {
				c = "" + o.getClass().getName() + " into "
						+ n.getClass().getName();
				try {
					n = (Number) (o.getClass().cast(o));
				} catch (ClassCastException e) {
					System.out.println("Cast exception " + c); // continue
				}
			}
		}

	}

	// Please keep
	public void testParsingNumbers() throws Exception {
		Long.parseLong("42");
		Long.parseLong("-42");
		String n = "42.0";
		try {
			Long.parseLong(n);
		} catch (NumberFormatException x) {
			Double d = Double.parseDouble(n);
			assertEquals(42.0, d.doubleValue());
		}
		try {
			Long.parseLong("-4-2");
		} catch (NumberFormatException e) {
			return;
		}

		throw new Exception(
				"Long.parseLong() with invalid number didn't throw exception");
	}
}
