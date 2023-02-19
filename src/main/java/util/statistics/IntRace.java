package util.statistics;

import java.io.IOException;
import java.util.Arrays;

import init.race.RACES;
import init.race.Race;
import snake2d.util.file.*;
import util.data.INT_O.INT_OE;

public class IntRace implements INT_OE<Race>, SAVABLE{

	private final int[] data = new int[RACES.all().size()+1];
	
	@Override
	public int min(Race t) {
		return 0;
	}
	
	@Override
	public final int get(Race t) {
		if (t == null)
			return data[RACES.all().size()];
		return data[t.index];
	}

	@Override
	public final void set(Race t, int i) {
		
		if (i != get(t)) {
			
			change(t, -data[t.index]);
			data[RACES.all().size()] -= data[t.index];
			data[t.index] = i;
			data[RACES.all().size()] += data[t.index];
			change(t, data[t.index]);
			
		}
	}

	@Override
	public final void save(FilePutter file) {
		file.is(data);
	}

	@Override
	public final void load(FileGetter file) throws IOException {
		file.is(data);
	}

	@Override
	public final void clear() {
		Arrays.fill(data, 0);
	}
	
	protected void change(Race t, int i) {
		
	}

	@Override
	public int max(Race t) {
		return Integer.MAX_VALUE;
	}

}
