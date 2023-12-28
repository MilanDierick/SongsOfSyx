package settlement.stats.stat;

import java.io.IOException;
import java.util.Arrays;

import init.config.Config;
import init.race.Race;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.*;
import snake2d.util.file.*;
import util.data.INT_O;
import util.info.INFO;
import util.statistics.HISTORY_INT.HISTORY_INT_OBJECT;

public interface SETT_STATISTICS {
	
	abstract public INFO info();

	
	abstract public HISTORY_INT_OBJECT<Race> data();
	
	abstract public HISTORY_INT_OBJECT<Race> data(HCLASS c);

	abstract public INT_O<Div> div();
	

	
	public default int dataDivider() {
		return 1;
	}
	
	public static class SettStatistics implements SETT_STATISTICS{

		private final DataStat[] datas = new DataStat[HCLASS.ALL().size()];
		private final DataStat total;
		private int[] divData = new int[Config.BATTLE.DIVISIONS_PER_ARMY*2];
		private final INFO info;
		
		public SettStatistics(StatsInit init, CharSequence name, CharSequence desc){
			this(init, new StatInfo(name, desc));
		}
		
		public SettStatistics(StatsInit init, INFO info){
			for (int c = 0; c < datas.length; c++) {
				final int k = c;
				datas[c] = new DataStat(init) {
					
					@Override
					public double getD(Race t, int fromZero) {
						double d = (double)(popDivider(HCLASS.ALL.get(k), t, fromZero)*dataDivider());
						if (d == 0)
							return 0;
						return get(t, fromZero)/(double)(d);
					}
					
					@Override
					public int min(Race t) {
						return 0;
					}
					
					@Override
					public int max(Race t) {
						return (int)(popDivider(HCLASS.ALL.get(k), t, 0)*dataDivider());
					}
				};
			}
			
			this.info = info;
			
			total = new DataStat(init) {

				@Override
				public double getD(Race t, int fromZero) {
					double d = (double)(popDivider(null, t, fromZero)*dataDivider());
					if (d == 0)
						return 0;
					return get(t, fromZero)/(double)(d);
				}
				
				@Override
				public int min(Race t) {
					return 0;
				}
				
				@Override
				public int max(Race t) {
					return (int)(popDivider(null, t, 0)*dataDivider());
				}
			};
			
			init.savables.add(new SAVABLE() {
				
				@Override
				public void save(FilePutter file) {
					file.is(divData);
				}
				
				@Override
				public void load(FileGetter file) throws IOException {
					file.is(divData);
				}
				
				@Override
				public void clear() {
					Arrays.fill(divData, 0);
				}
			});
		}
		
		private final INT_O<Div> div = new INT_O<Div>() {

			@Override
			public int get(Div t) {
				return divData[t.index()];
			}

			@Override
			public int min(Div t) {
				return 0;
			}
			
			@Override
			public double getD(Div t) {
				double p = STATS.POP().POP.div().get(t);
				if (p == 0)
					return 0;
				return get(t)/(dataDivider() *p);
			}

			@Override
			public int max(Div t) {
				return Integer.MAX_VALUE;
			}
			
		};

		@Override
		public INFO info() {
			return info;
		}

		@Override
		public HISTORY_INT_OBJECT<Race> data(){
			return data(null);
		}
		
		@Override
		public HISTORY_INT_OBJECT<Race> data(HCLASS c) {
			if (c == null)
				return total;
			return datas[c.index()];
		}

		@Override
		public INT_O<Div> div() {
			return div;
		}
		
		public void inc(HCLASS c, Race r, int am, int divi) {
			datas[c.index()].set(r, datas[c.index()].get(r)+am);
			
			if (c.player) {
				datas[c.index()].set(null, datas[c.index()].get(null)+am);
				total.set(r, total.get(r)+am);
				total.set(null, total.get(null)+am);
			}
			if (divi != -1) {
				divData[divi] += am;
			}
		}
		
		public void inc(Induvidual i, int d) {
			if (i.added()) {
				Div div = STATS.BATTLE().DIV.get(i);
				inc(i.clas(), i.race(), d, div == null ? -1 : div.index());
			}
				
			
		}
		
		protected int popDivider(HCLASS c, Race r, int daysback) {
			return STATS.POP().POP.data(c).get(r, daysback);
		}
		
		@Override
		public int dataDivider() {
			return 1;
		}


		
	}
}
