package settlement.stats;

import init.D;
import init.disease.DISEASE;
import init.disease.DISEASES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.main.SETT;
import settlement.stats.Init.Updatable;
import settlement.stats.STAT.StatInfo;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.rnd.RND;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import view.sett.IDebugPanelSett;
import view.tool.PlacableSimple;

public class StatsDisease {

	private final STAT.STATData infected;
	private final STAT.STATData incubating;
	private final INT_OE<Induvidual> index;
	private final INT_OE<Induvidual> count;
	private final INT_OE<Induvidual> status;
	

	private final int NONE = 0;
	private final int INCUBATING = 1;
	private final int ISICK = 2;
	private final int IIMMUNE = 3;
	
	private static CharSequence ¤¤infected = "¤Infected";
	private static CharSequence ¤¤infectedD = "¤Amount of subjects Infected with a disease";
	private static CharSequence ¤¤disease = "¤Disease";
	private static CharSequence ¤¤diseaseD = "¤The current disease this subject is suffering from.";
	
	static {
		D.ts(StatsDisease.class);
	}
	
	StatsDisease(Init init) {
		infected = new STAT.STATData(null, init, new INT_OE<Induvidual>() {

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
		
		}, new StatInfo(¤¤infected, ¤¤infectedD), null);
		
		incubating = new STAT.STATData(null, init, new INT_OE<Induvidual>() {

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
		
		}, new StatInfo(¤¤infected, ¤¤infectedD), null);
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
		
		init.updatable.add(updater);
		
		IDebugPanelSett.add(new PlacableSimple("Infect") {
			
			@Override
			public void place(int x, int y) {
				for (ENTITY e : SETT.ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						infect(a, DISEASES.random());
					}
						
				}
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				return SETT.ENTITIES().getAtPoint(x, y) != null ? null : E;
			}
		});
		
	}
	
	private final Updatable updater = new Updatable() {
		
		@Override
		public void update16(Humanoid h, int updateR, boolean day, int updateI) {
			
			Induvidual i = h.indu();
			int st = status.get(i);
			
			if (st == INCUBATING) {
				if ((updateI & 0b01) == 0) {
					if (count.get(i) <=  0) {
						status.set(i, ISICK);
						count.set(i, 0);
					}else {
						count.inc(i, -1);
					}
				}
				
				
			}else if (day) {
				
				if (st == ISICK) {
					if (count.get(i) >= 2) {
						if (RND.rFloat() <= DISEASES.all().get(index.get(i)).fatalityRate) {
							AIManager.dead = CAUSE_LEAVE.DISEASE;
							AIManager.deadGore = false;
						}else {
							status.set(i, IIMMUNE);
							count.set(i, 0);
						}
						
					}else {
						count.inc(i, 1);
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
	};
	
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
	
//	public void transmit(Humanoid from, ENTITY to) {
//		
//		if (to instanceof Humanoid) {
//			Humanoid bb = (Humanoid) to;
//			Induvidual a = from.indu();
//			
//			int st = status.get(a);
//			if (st == ISICK || st == INCUBATING) {
//				if (!GAME.events().disease.shouldInfect(DISEASES.all().get(index.get(a))))
//					return;
//				
//				Induvidual b = bb.indu();
//				
//				
//				switch(status.get(b)) {
//					case NONE: {
//						index.set(b, index.get(a));
//						status.set(b, INCUBATING);
//						count.set(b, 0);
//						return;
//					}
//					case INCUBATING: return;
//					case ISICK: return;
//					case IIMMUNE: {
//						if (index.get(a) != index.get(b)) {
//							index.set(b, index.get(a));
//							status.set(b, INCUBATING);
//							count.set(b, 0);
//						}
//						return;
//					}
//				}
//				
//			}
//			
//		}
//	}
	
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
	
//	public boolean tryInfect(Humanoid a, DISEASE d) {
//		Induvidual i = a.indu();
//		if (status.get(i) == IIMMUNE && index.get(i) == d.index())
//			return false;
//		double c = d.spread/(0.1+BOOSTABLES.PHYSICS().HEALTH.get(i));
//		if (RND.rFloat() < c) {
//			infect(a, d);
//			return true;
//		}
//		return false;
//	}
	

	

	
}
