package settlement.stats;

import init.D;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.room.spirit.grave.GraveData;
import settlement.stats.Init.Updatable2;
import settlement.stats.STAT.StatInfo;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public class StatsBurial extends StatCollection{

	private final LIST<STAT> all;
	private final LIST<StatGrave> graves;
	public STAT DESECRATION;
	
	private final LIST<STAT> others;
	
	private static CharSequence ¤¤more = "¤We want better prospects of being buried in a {0}.";
	private static CharSequence ¤¤less = "¤We do not wish to be buried in a {0}.";
	
	static {
		D.ts(StatsBurial.class);
	}
	
	StatsBurial(Init init) {
		super(init, "BURIAL");
		
		ArrayList<StatGrave> graves = new ArrayList<StatGrave>(SETT.ROOMS().GRAVES.size());
		for (GraveData.GRAVE_DATA_HOLDER h : SETT.ROOMS().GRAVES) {
			StatInfo in = new StatInfo(h.graveData().blueprint().info.names, h.graveData().blueprint().info.desc);
			in.setOpinion(¤¤more, ¤¤less);
			
			graves.add(new StatGrave(h.graveData(), init, in));			
		}
		this.graves = graves;
		
		
		
		
		DESECRATION = new STAT.STATImp("DESECRATION", init) {

			@Override
			int getDD(HCLASS s, Race r) {
				double am = 0;
				for (StatGrave g : graves)
					am += g.grave().disturbance.getD();
				return (int) (CLAMP.d(am, 0, 1)*pdivider(s, r, 0));
			}
		};
		
		all = makeStats(init);
		
		ArrayList<STAT> others = new ArrayList<>(all.size());
		for (STAT s : all) {
			if (s instanceof StatGrave)
				continue;
			others.add(s);
		}
		this.others = new ArrayList<>(others);
		
		
		init.upers.add(new Updatable2() {
			
			GraveUpdater g = new GraveUpdater();
			
			@Override
			public void update(double ds) {
				g.update(ds);
			}
		});
	}
	
	@Override
	public LIST<STAT> all() {
		return all;
	}
	
	public LIST<STAT> others(){
		return others;
	}
	
	public LIST<StatGrave> graves(){
		return graves;
	}
	
	private class GraveUpdater {
		
		int[] available = new int[graves.size()];
		int[] needed = new int[graves.size()];
		
		public void update(double ds) {
			
			for (StatGrave gr : graves) {
				available[gr.gIndex()] = gr.grave().available.get(null)*100;
			}
			
			for (HCLASS c : HCLASS.ALL) {
				if (!c.player)
					continue;
				
				for (StatGrave gr : graves) {
					needed[gr.gIndex()] = 0;
				}
				
				for (Race r : RACES.all()) {
					for (StatGrave gr : r.service().GRAVES.get(c.index())) {
						if (gr.grave().permission().get(c, r)) {
							needed[gr.gIndex()] += STATS.POP().POP.data(c).get(r);
						}
					}
				}
				
			
				
				for (Race r : RACES.all()) {
					for (StatGrave gr : r.service().GRAVES.get(c.index())) {
						if (gr.grave().permission().get(c, r)) {
							int av = available[gr.gIndex()];
							int needed = this.needed[gr.gIndex()];
							if (av == 0) {
								gr.access[c.index()][r.index] = 0;
							}else {
								int used = Math.min(av, STATS.POP().POP.data(c).get(r));
								double d = av/(double)(needed+1);
								available[gr.gIndex()] -= used;
								gr.access[c.index()][r.index] = CLAMP.d(d, 0, 1);
							}
							
						}else {
							gr.access[c.index()][r.index] = 0;
						}
					}
				}
				
			}
			
		}
	}
	
	public static class StatGrave extends STAT.STATImp {

		private GraveData h;
		private double[][] access = new double[HCLASS.ALL.size()][RACES.all().size()];
		
		StatGrave(GraveData h, Init init, StatInfo info) {
			super(h.blueprint().key, init, info, h.standingDef());
			this.h = h;
			
		}

		@Override
		int getDD(HCLASS s, Race r) {
			
			return (int) (access[s.index()][r.index] * h.get(s).value.getD(r)*pdivider(s, r, 0));
			
			//return h.get(s).value.getD(r);
		}
		
		public GraveData grave() {
			return h;
		}
		
		int gIndex() {
			return index()-STATS.BURIAL().graves.get(0).index();
		}
		
		public double coverage(HCLASS s, Race r) {
			if (r == null) {
				double am = 0;
				for (Race rr : RACES.all()) {
					am += coverage(s, rr);
				}
				return am/STATS.POP().POP.data(s).get(null);
			}
			return access[s.index()][r.index];
		}
		
		
	}
	
}
