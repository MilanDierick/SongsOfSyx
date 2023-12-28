package settlement.stats.stat;

import java.io.IOException;
import java.util.Arrays;

import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.StatsInit;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import util.data.INT_O.INT_OE;

public class StatDecree extends StatInfo {

	private byte[] levels = new byte[HCLASS.ALL().size()*RACES.all().size()]; 
	private final ArrayList<INT_OE<Race>> tars;
	
	public StatDecree (StatsInit i, int min, int max, CharSequence name, int def){
		super(name, "");
		i.savables.add(saver);
		Arrays.fill(levels, (byte)def);
		tars = new ArrayList<>(HCLASS.ALL().size());
		
		for (HCLASS c : HCLASS.ALL()) {
			tars.add(new INT_OE<Race>() {

				@Override
				public int min(Race t) {
					return min;
				}
				
				@Override
				public int max(Race t) {
					return max;
				}
				
				@Override
				public int get(Race t) {
					if (t == null) {
						int m = 0;
						for (int ri = 0; ri < RACES.all().size(); ri++) {
							Race r = RACES.all().get(ri);
							m = Math.max(m, get(r));
						}
						return m;
					}
					return levels[c.index()*RACES.all().size() + t.index];
				}

				@Override
				public void set(Race t, int i) {
					if (t == null) {
						for (int ri = 0; ri < RACES.all().size(); ri++) {
							Race r = RACES.all().get(ri);
							set(r, i);
						}
						return;
					}
					levels[c.index()*RACES.all().size() + t.index] = (byte) CLAMP.i(i, min, max);
				}
			
			
			});
		}
	}
	
	public INT_OE<Race> get(HCLASS c){
		return tars.get(c.index());
	}
	
	public int get(Humanoid h) {
		return tars.get(h.indu().clas().index()).get(h.indu().race());
	}
	
	private final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.bsE(levels);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			file.bsE(levels);
		}
		
		@Override
		public void clear() {
			Arrays.fill(levels, (byte)1);
		}
	};
}