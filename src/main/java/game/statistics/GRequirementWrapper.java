package game.statistics;

public abstract class GRequirementWrapper extends G_REQ {
	
	private final G_REQ o;
	
	public GRequirementWrapper(G_REQ o) {
		super();
		this.o = o;
	}

	@Override
	public boolean isInt() {
		return o.isInt();
	}

	@Override
	public CharSequence name() {
		return o.name();
	}

	@Override
	public int value() {
		return o.value();
	}
	
	@Override
	protected String key() {
		return o.key();
	}


	@Override
	public boolean isAnti() {
		return o.isAnti();
	}
	
}