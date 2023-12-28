package settlement.stats.stat;

import java.io.IOException;
import java.util.Arrays;

import game.time.TIME;
import game.time.TIMECYCLE;
import init.race.RACES;
import init.race.Race;
import settlement.stats.*;
import settlement.stats.StatsInit.Pushable;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import util.statistics.HISTORY_INT.HISTORY_INT_OBJECT;

public abstract class DataStat implements HISTORY_INT_OBJECT<Race>, SAVABLE, Pushable{

	final int stride = RACES.all().size()+1;
	private final int[] history = new int[STATS.DAYS_SAVED*stride];
	protected final int[] current = new int[stride];
	
	public DataStat(StatsInit init){
		init.savables.add(this);
		init.pushable.add(this);
	}
	
	public DataStat(){

	}
	
	void set(Race r, int a) {
		if (r == null)
			current[stride-1] = a;
		else
			current[r.index] = a;
	}
	
	void incr(Induvidual i, int d) {
		current[i.race().index] += d;
		if (i.hType().player)
			current[stride-1] += d;
	}
	
	public void incrFull(Induvidual i, int d) {
		current[i.race().index] += d;
		current[stride-1] += d;
	}
	
	@Override
	public void pushday() {
		for (int i = history.length-1; i >= stride; i--)
			history[i] = history[i-stride];
		for (int i = 0; i < stride; i++)
			history[i] = current[i];
	}
	
	@Override
	public void save(FilePutter file) {
		file.isE(history);
		file.isE(current);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		file.isE(history);
		file.isE(current);
	}
	
	@Override
	public void clear() {
		Arrays.fill(history, 0);
		Arrays.fill(current, 0);
	}

	@Override
	public int get(Race group, int daysBack) {
		if (daysBack == 0) {
			if (group == null)
				return current[stride-1];
			return current[group.index];
		}
		if (group == null)
			return history[stride*daysBack+stride-1];
		return history[stride*(daysBack-1) + group.index];
	}

	@Override
	public TIMECYCLE time() {
		return TIME.days();
	}

	@Override
	public int historyRecords() {
		return STATS.DAYS_SAVED;
	}
	
	static abstract class DataRaceI extends DataStat {

		private final INT_OE<Induvidual> data;
		
		DataRaceI(StatsInit init, INT_OE<Induvidual> data){
			super(init);
			this.data = data;
		}
		
		@Override
		public int min(Race t) {
			return 0;
		}

		@Override
		public int max(Race t) {
			return data.max(null);
		}
		
		abstract double divider(Race r, int fromZero);

		@Override
		public double getD(Race r, int fromZero) {
			double p = divider(r, fromZero)*max(r);
			if (p == 0)
				return CLAMP.d(get(r, fromZero), 0, 1);
			return CLAMP.d(get(r, fromZero)/p, 0, 1);
		}
		
	}
	
	
}
