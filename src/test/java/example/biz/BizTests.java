package example.biz;

import org.junit.Test;
import junit.framework.TestCase;
import static org.junit.Assert.*;

import example.exceptions.PDXception;

public class BizTests extends TestCase {

	public BizTests() {
	}

	/**
	 * Meds have 'rank out-of' eg. '4th out of 7 similar meds'. If you say '7
	 * out of 4' you should get an exception
	 */
	public void testMedWithInvalidRankingFails() throws Exception {
		Medication med = new Medication("Sertraline", "Useless", 4,
				new Long(7L));
		assertEquals("Should be 4", 4, med.rank());
		assertEquals("Should be 7", 7L, med.outOf());
		try {
			med = new Medication("Cymbalta", "Useless", 7, new Long(4L));
		} catch (PDXception p) {
			return;
		}
		throw new Exception(
				"This method should have thrown an exception before it got here");
	}

}