package example.biz;

import example.exceptions.PDXception;

public class Medication implements IMedication {
	private String name;
	private String classification;
	private int rank;
	private Long outOf;

	public Medication(String name) {
		this.name = name;
	}

	public Medication(String name, String classification) {
		this(name);
		this.classification = classification;
	}

	public Medication(String name, int rank, Long outOf) {
		this(name);
		this.rank = rank;
		this.outOf = outOf;
	}

	public Medication(String name, String classification, int rank, Long outOf)
			throws PDXception {
		this(name, classification);
		if (outOf != null && rank > outOf.longValue()) // eg. '5 out of 3'
			throw new PDXception("" + rank + " out of " + outOf.longValue()
					+ " is impossible");
		this.rank = rank;
		this.outOf = outOf;
	}

	public String cla$$() {
		return classification;
	}

	public String name() {
		return name;
	}

	public int rank() {
		return rank;
	}

	public long outOf() {
		return outOf.longValue();
	}
}