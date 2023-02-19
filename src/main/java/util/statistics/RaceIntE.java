package util.statistics;

import java.io.IOException;
import java.util.Arrays;

import init.race.RACES;
import init.race.Race;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import util.data.INT.INTE;
import util.info.INFO;

public class RaceIntE implements SAVABLE{

	private int[] values = new int[RACES.all().size()];
	private INTE [] ints = new INTE[RACES.all().size()+1];
	private final INFO info;
	
	public RaceIntE(){
		this("","");
	}
	
	public RaceIntE(CharSequence name, CharSequence desc){
		this(name, desc, 0, 100);
	}
	
	public RaceIntE(CharSequence name, CharSequence desc, int min, int max){
		this.info = new INFO(name, desc);
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			final int k = ri;
			ints[ri] = new INTE() {
				
				@Override
				public int min() {
					return min;
				}
				
				@Override
				public int max() {
					return max;
				}
				
				@Override
				public int get() {
					return CLAMP.i(values[k], min, max);
				}
				
				@Override
				public void set(int t) {
					values[k] = CLAMP.i(t, min, max);
					setP(RACES.all().get(k), values[k]);
				}
				
				@Override
				public INFO info() {
					return RaceIntE.this.info();
				}
			};
		}
		
		ints[RACES.all().size()] = new INTE() {
			
			@Override
			public int min() {
				return min;
			}
			
			@Override
			public int max() {
				return max;
			}
			
			@Override
			public int get() {
				int m = -Integer.MAX_VALUE;
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					m = Math.max(m, values[ri]);
				}
				return m;
			}
			
			@Override
			public void set(int t) {
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					ints[ri].set(CLAMP.i(t, min, max));
				}
			}
			
			@Override
			public INFO info() {
				return RaceIntE.this.info();
			}
		};
	}
	
	protected void setP(Race r, int i) {
		
	}

	public INTE get(Race race) {
		if (race == null)
			return ints[ints.length-1];
		return ints[race.index];
	}
	
	@Override
	public void save(FilePutter file) {
		file.isE(values);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.isE(values);
	}

	@Override
	public void clear() {
		Arrays.fill(values, 0);
	}
	
	public void fill(int n) {
		Arrays.fill(values, n);
	}
	
	public INFO info() {
		return info;
	}
	
}
