package settlement.stats.colls;

import game.time.TIME;
import init.D;
import init.disease.DISEASE;
import init.disease.DISEASES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.main.SETT;
import settlement.stats.*;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.health.HEALTH;
import settlement.stats.law.LAW;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATData;
import settlement.stats.util.CAUSE_LEAVE;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import view.sett.IDebugPanelSett;
import view.tool.PlacableSimple;

public class StatsNeedsDisease implements StatUpdatableI{

	private final STATData infected;
	private final STATData incubating;
	private final INT_OE<Induvidual> index;
	private final INT_OE<Induvidual> count;
	private final INT_OE<Induvidual> status;
	

	private final int NONE = 0;
	private final int INCUBATING = 1;
	private final int ISICK = 2;
	private final int IIMMUNE = 3;
	
	private static CharSequence ¤¤disease = "¤Disease";
	private static CharSequence ¤¤diseaseD = "¤The current disease this subject is suffering from.";
	
	static {
		D.ts(StatsNeedsDisease.class);
	}
	
	StatsNeedsDisease(StatsInit init) {
		infected = new STATData("INFECTED", init, new INT_OE<Induvidual>() {

			@Override
			public int get(Induvidual t) {
				return getter.get(t) != null ? 1 : 0;
			}

			@Override
			public int min(Induvidual t) {
				return 0;
			}

			@Override
			public int max(Induvidual t) {
				return 1;
			}

			@Override
			public void set(Induvidual t, int i) {

			}
		
		});
		
		incubating = new STATData(null, init, new INT_OE<Induvidual>() {

			@Override
			public int get(Induvidual t) {
				return getter.get(t) != null || status.get(t) == INCUBATING ? 1 : 0;
			}

			@Override
			public int min(Induvidual t) {
				return 0;
			}

			@Override
			public int max(Induvidual t) {
				return 1;
			}

			@Override
			public void set(Induvidual t, int i) {

			}
		
		}, infected.info());
		incubating.info().setMatters(false, true);
		
		index = init.count.new DataByte();
		count = init.count.new DataNibble();
		status = init.count.new DataCrumb() {
			@Override
			public void set(Induvidual t, int s) {
				infected.removeH(t);
				incubating.removeH(t);
				super.set(t, s);
				infected.addH(t);
				incubating.addH(t);
			};
		};
		
		IDebugPanelSett.add(new PlacableSimple("Infect") {
			
			@Override
			public void place(int x, int y) {
				for (ENTITY e : SETT.ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						infect(a, DISEASES.randomRegular(SETT.ENV().climate()));
					}
						
				}
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				return SETT.ENTITIES().getAtPoint(x, y) != null ? null : E;
			}
		});
		
		
		
		
	}
	
	@Override
	public void update16(Humanoid h, int updateR, boolean day, int updateI) {
		
		Induvidual i = h.indu();
		int st = status.get(i);
		
		if (st == INCUBATING) {
			if ((((STATS.RAN().get(i, 11, 16)+TIME.seasons().bitsSinceStart()) >>7)&0b11) == 0 && LAW.curfew().is()) {
				status.set(i, NONE);
				count.set(i, 0);
			}else if ((updateI & 0b01) == 0) {
				if (count.get(i) <=  0) {
					status.set(i, ISICK);
					count.set(i, 0);
				}else {
					count.inc(i, -1);
				}
			}
		}else if (day) {
			if (st == ISICK) {
				if (count.get(i) >= getter.get(i).length) {
					if (willDie(i, 0)) {
						AIManager.dead = CAUSE_LEAVE.DISEASE;
						AIManager.deadGore = false;
					}else {
						status.set(i, IIMMUNE);
						count.set(i, 0);
					}
					
				}else {
					count.inc(i, 1);
				}
			}else if (HEALTH.shouldGetSickDay(h.indu())) {
				DISEASE d = DISEASES.randomRegular(SETT.ENV().climate());
				if (d == null || (st == IIMMUNE && DISEASES.all().get(index.get(h.indu())) == d)) {
					
				}else {
					infect(h, d);
				}
			}else if (st == IIMMUNE) {
				if (count.get(i) >= 4) {
					status.set(i, NONE);
				}else {
					count.inc(i, 1);
				}
			}
				
			
		}
		
		
	}
	
	public boolean willDie(Induvidual i, double treatment) {
		double deathRate = DISEASES.all().get(index.get(i)).fatalityRate;
		deathRate *= 1.0-treatment;
		deathRate = CLAMP.d(deathRate, 0, 1);
		int rnd = STATS.RAN().get(i, 13, 16);
		int ch = (int) (deathRate*0x0FFFF);
		if (ch > rnd)
			return true;
		return false;
		
	}
	
	public STAT infected() {
		return infected;
	}
	
	public STAT total() {
		return incubating;
	}
	
	public GETTER_TRANSE<Induvidual, DISEASE> getter = new GETTER_TRANSE<Induvidual, DISEASE>(){

		private final INFO info = new INFO(¤¤disease, ¤¤diseaseD);
		
		@Override
		public DISEASE get(Induvidual t) {
			if (status.get(t) == ISICK)
				return DISEASES.all().get(index.get(t));
			return null;
		}

		@Override
		public void set(Induvidual f, DISEASE d) {
			if (d != null) {
				if (d.index() == index.get(f) && status.get(f) != NONE) {
					return;
				}
				index.set(f, d.index());
				status.set(f, INCUBATING);
				count.set(f, 0);
			}else {
				status.set(f, NONE);
			}
		}
		
		@Override
		public INFO info() {
			return info;
		};
		
	};
	
	private final ColorImp color = new ColorImp();
	
	public COLOR colorAdd(COLOR a, Induvidual h) {
		int i = status.get(h);
		if ((i == ISICK)) {
			COLOR b = DISEASES.all().get(index.get(h)).color;
			color.set((((int)a.red()&0x0FF)+((int)b.red()&0x0FF)), (((int)a.green()&0x0FF)+((int)b.green()&0x0FF)), (((int)a.blue()&0x0FF)+((int)b.blue()&0x0FF)));
			return color;
		}
		return a;
	}
	
	public void infect(Humanoid a, DISEASE d) {
		getter.set(a.indu(), d);
		status.set(a.indu(), ISICK);
		count.set(a.indu(), 0);
	}
	
	public void incubate(Humanoid a, DISEASE d) {
		getter.set(a.indu(), d);
		status.set(a.indu(), INCUBATING);
		count.set(a.indu(), 1 + RND.rInt(14));
	}
	
	public void cure(Humanoid a) {
		status.set(a.indu(), IIMMUNE);
		count.set(a.indu(), 0);
	}

	
}
