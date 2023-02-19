package game;

import util.data.INT.INTE;

public final class GameConSpec implements INTE{

	protected CharSequence[] options;
	protected int index;
	private final CharSequence label;
	private double[] values;
	
	public GameConSpec(CharSequence[] options, double[] values, int defaultI, CharSequence label){
		
		this.options = new String[options.length];
		if (options == null || options.length <= 0)
			throw new RuntimeException();
		
		for (int i = 0; i < options.length; i++){
			this.options[i] = options[i].toString();
		}
		this.options = options;
		this.label = label;
		this.index = defaultI;
		this.values = values;
		if (options.length != this.values.length)
			throw new RuntimeException();
	}
	
	GameConSpec(String label){
		this.label = label;
	}
	
	public CharSequence[] getOptions(){
		return options;
	}

	public CharSequence getLabel(){
		return label;
	}
	
	public double getValue() {
		return values[index];
	}

	@Override
	public int min() {
		return 0;
	}

	@Override
	public int max() {
		return values.length-1;
	}

	@Override
	public int get() {
		return index;
	}

	@Override
	public void set(int t) {
		index = t;
	}
	
}
