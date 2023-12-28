package settlement.entity.humanoid.ai.home;

import static settlement.main.SETT.*;

import game.time.TIME;
import init.D;
import settlement.entity.humanoid.*;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIData.AIDataBit;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.util.AIUtilMoveH;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.room.home.HOME;
import settlement.room.home.chamber.ChamberInstance;
import settlement.room.home.house.HomeHouse;
import settlement.room.home.house.HomeHouse.DirCoo;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import snake2d.LOG;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;

public final class AIModule_Home extends AIModule{

	private final AIDataBit hasHoused;
	
	private static CharSequence ¤¤name = "relaxing";
	private static CharSequence ¤¤curfew = "Staying off the streets";
	
	static{
		D.ts(AIModule_Home.class);
	}
	
	public static final int CURFEW_PRIO = 5;
	
	public AIModule_Home(){
		hasHoused = AI.data().new AIDataBit();
		
		
		
	}

	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		hasHoused.set(d, true);
		STATS.HOME().GETTER.hasSearched.indu().set(a.indu(), 0);
		
		if (STATS.HOME().GETTER.has(a)) {
			HOME h = STATS.HOME().GETTER.get(a, this);
			if (h.is(a.tc())) {
				h.done();
				if (h instanceof HomeHouse)
					return sleep_home.activate(a, d);
				else
					return sleep_noble.activate(a, d);
			}
			h.done();
		}
		
		if (SETT.PATH().finders().home.find(a, d.path)) {
			HOME h = STATS.HOME().GETTER.get(a, this);
			h.done();
			if (h instanceof HomeHouse)
				return sleep_home.activate(a, d);
			else if (h instanceof ChamberInstance)
				return sleep_noble.activate(a, d);
			throw new RuntimeException(""+h);
		}
		
		if (STATS.HOME().GETTER.has(a)) {
			HOME h = STATS.HOME().GETTER.get(a, this);
			int sx = h.service().x();
			int sy = h.service().y();
			if (!SETT.PATH().reachability.is(sx, sy))
				LOG.ln(sx + " " + sy);
			h.done();
			STATS.HOME().GETTER.set(a, null);
		}
		
		STATS.HOME().GETTER.hasSearched.indu().set(a.indu(), 1);
		
		return sleep_groud.activate(a, d);
		
	}
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		
		if (newDay || ((upI&3 )== 0 &&  !STATS.HOME().GETTER.has(a) && 	STATS.HOME().GETTER.hasSearched.indu().get(a.indu()) == 1)) {
			hasHoused.set(d, false);
		}
		
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		if (STATS.HOME().GETTER.has(a)) {
			if (SETT.PATH().finders.otherHumanoid.enemiesAreNear(a))
				return 7;
			if (LAW.curfew().is())
				return 5;
		}
		
		
		if (!hasHoused.is(d))
			return 3;
		
		if (STATS.HOME().GETTER.has(a)) {
			if ((AIModules.current(d) == this)){
				if (TIME.light().nightIs())
					return 3;
				return (int) ((STATS.RAN().get(a.indu(), 62, 1)) + TIME.days().bitsSinceStart() & 0b01) +1;
			}
			return (int) ((STATS.RAN().get(a.indu(), 62, 1)) + TIME.days().bitsSinceStart() & 0b01);
		}
		
		if (STATS.WORK().EMPLOYED.get(a) == null) {
			if (SETT.ROOMS().HOMES.HOME.odd.has(a))
				return 3;
		}
		
		
		return 0;
		
	}
	
	private final AIPLAN sleep_noble = new AIPLAN.PLANRES() {
		
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			HOME h = STATS.HOME().GETTER.get(a, this);
			if (h.is(a.tc())) {
				h.done();
				if (RND.oneIn(6))
					return walk.set(a, d);
				return bed.set(a, d);
			}
			h.done();
			return first.set(a, d);
		}
		
		private final Resumer first = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().walkTo.pathFull(a, d);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (moduleCanContinue(a, d) && isHome(a, d)) {
					if (RND.oneIn(6))
						return walk.set(a, d);
					return bed.set(a, d);
				}
				
				return null;
				
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
			
		};
		
		private final Resumer walk = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				
				if (moduleCanContinue(a, d) && isHome(a, d)) {
					return AI.SUBS().walkTo.room(a, d, get(a));
				}
				return null;
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (moduleCanContinue(a, d) && isHome(a, d)) {
					if (RND.oneIn(8)) {
						if (RND.oneIn(3))
							return bed.set(a, d);
						return AI.SUBS().walkTo.room(a, d, get(a));
					}else {
						return AI.SUBS().STAND.activateRndDir(a, d, 4);
					}
				}
				
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
			
		};
		
		private final Resumer bed = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = 8;
				HOME h = get(a);
				int sx = h.service().x();
				int sy = h.service().y();
				h.done();
				return AI.SUBS().walkTo.cooFull(a, d, sx, sy);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (moduleCanContinue(a, d) && isHome(a, d)) {
					d.planByte1--;
					if (d.planByte1 <= 0)
						return walk.set(a, d);
					return sleepBed(a, d);
				}
				
				return null;
				
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
			
		};
		
		private AISubActivation sleepBed(Humanoid a, AIManager d) {
			d.planTile.set(d.path.destX(), d.path.destY());
			int tx = d.planTile.x();
			int ty = d.planTile.y();
			int cx = SETT.ROOMS().HOMES.CHAMBER.getSleepPixelX(tx, ty);
			int cy = SETT.ROOMS().HOMES.CHAMBER.getSleepPixelY(tx, ty);
			a.physics.body().moveC(cx, cy);
			a.speed.setRaw(SETT.ROOMS().HOMES.CHAMBER.getSleepDir(tx, ty), 0);
			return AI.SUBS().subSleep.activate(a, d);
		}

		
		private boolean isHome(Humanoid a, AIManager d) {
			ChamberInstance h = get(a);
			return h != null && h.is(a.tc());
		}
		
		private ChamberInstance get(Humanoid a) {
			HOME h = STATS.HOME().GETTER.get(a, this);
			if (h != null) {
				h.done();
				if (h instanceof ChamberInstance)
					return (ChamberInstance) h;
			}
			return null;
		}
		
		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			if (LAW.curfew().is())
				string.add(¤¤curfew);
			else
				string.add(¤¤name);
		};
		
	};
	
	private final AIPLAN sleep_home = new AIPLAN.PLANRES() {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			d.planByte2 = 4;
			HOME h = STATS.HOME().GETTER.get(a, this);
			if (h.is(a.tc())) {
				h.done();
				return use.set(a, d);
			}
			h.done();
			return walk.set(a, d);
		}
		
		private final Resumer walk = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().walkTo.pathFull(a, d);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return use.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};
		
		private final Resumer use = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte2 = 5;
				HomeHouse h = get(a);
				if (h == null)
					return null;
				if (!h.is(a.tc())) {
					int sx = h.service().x();
					int sy = h.service().y();
					h.done();
					return AI.SUBS().walkTo.cooFull(a, d, sx, sy);
				}
				h.use();
				h.done();
				return AI.SUBS().STAND.activateRndDir(a, d);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				
				if (shouldRes(a, d)) {
					AISubActivation s = null;
					switch(RND.rInt(4)) {
					case 0: 
						s = bed.set(a, d);
						break;
					case 1:
						s = move.set(a, d);
						break;
					default: 
					}
					if (s != null)
						return s;
					
					if (!SETT.ENTITIES().hasAtTileHigher(a, a.tc().x(), a.tc().y()))
						return AI.SUBS().STAND.activateRndDir(a, d);
					
					HomeHouse h = get(a);
					for (DIR dir : DIR.ORTHO) {
						if (h.is(a.tc(), dir) && !SETT.PATH().solidity.is(a.tc(), dir)) {
							h.done();
							return AI.SUBS().walkTo.cooFull(a, d, a.tc().x()+dir.x(), a.tc().y()+dir.y());
						}
					}
					h.done();
					return AI.SUBS().STAND.activateRndDir(a, d);
				}
				can(a, d);
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {

			}
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				if (e.type == HPoll.CAN_INTERRACT)
					return 1.0;
				return super.poll(a, d, e);
			};
		};
		
		private final Resumer bed = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				HomeHouse h = get(a);
				DirCoo c = h.findService(a);
				h.done();
				
				if (c == null)
					return AI.SUBS().STAND.activateRndDir(a, d);
				d.planTile.set(c);
				d.planByte1 = (byte) (8+RND.rInt(2));
				AISubActivation s = AI.SUBS().walkTo.coo(a, d, c);
				if (s != null)
					return s;
				
				
				return AI.SUBS().STAND.activateRndDir(a, d);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (shouldRes(a, d)) {
					
					
					if (d.planByte1-- <= 0) {
						moveOK(a, d);
						return use.set(a, d);
					}
					HomeHouse h = get(a);
					DirCoo c = h.getService(d.planTile.x(), d.planTile.y());
					h.done();
					if (c == null) {
						can(a, d);
						return null;
					}
					
					AIUtilMoveH.moveToTile(a, c.x(), c.y(), c.isLay ? c.dir : DIR.C);
					
					a.speed.setDirCurrent(c.dir);
					
					if (c.isLay && a.race().physics.sleeps) {
						return AI.SUBS().subSleep.activate(a, d);
					}else
						return AI.SUBS().STAND.activateTime(a, d, 8);
				}
				can(a, d);
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				moveOK(a, d);
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				if (e.event == HEvent.COLLISION_UNREACHABLE)
					return false;
				return super.event(a, d, e);
			};
		};
		
		private final Resumer move = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				HomeHouse h = get(a);
				for (DIR dir : DIR.ORTHO) {
					if (h.is(a.tc(), dir) && !SETT.PATH().solidity.is(a.tc(), dir) && !SETT.ENTITIES().hasAtTile(a.tc().x()+dir.x(), a.tc().y()+dir.y())) {
						h.done();
						return AI.SUBS().walkTo.cooFull(a, d, a.tc().x()+dir.x(), a.tc().y()+dir.y());
					}
				}
				h.done();
				return null;
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (shouldRes(a, d)) {
					if (RND.oneIn(8)) {
						return use.set(a, d);
					}
					return AI.SUBS().STAND.activateRndDir(a, d);
				}
				can(a, d);
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			

			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
			
			
		};
		
		private boolean shouldRes(Humanoid a, AIManager d) {
			if (!isHome(a, d))
				return false;
			if (!moduleCanContinue(a, d)) {
				if (AIModules.nextPrio(d) < 5) {
					d.planByte2--;
					return d.planByte2 <= 0;
				}
				return false;
			}
			return true;
				
		}

		
		private void moveOK(Humanoid a, AIManager d) {
			if (SETT.PATH().solidity.is(a.tc())) {
				AIUtilMoveH.unfuck(a);
			}
		}
		
		private boolean isHome(Humanoid a, AIManager d) {
			HomeHouse h = get(a);
			if (h != null) {
				boolean ret = h.is(a.tc());
				h.done();
				return ret;
			}
			return false;
		}
		
		private HomeHouse get(Humanoid a) {
			HOME h = STATS.HOME().GETTER.get(a, this);
			if (h != null && h instanceof HomeHouse)
				return (HomeHouse) h;
			return null;
		}
		
		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			if (LAW.curfew().is())
				string.add(¤¤curfew);
			else
				string.add(¤¤name);
		};
		
	};
	
	
	private final AIPLAN sleep_groud = new AIPLAN.PLANRES() {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			STATS.HOME().dump(a);
			return findBed.set(a, d);
		}
		
		private final Resumer findBed = new Resumer("going to bed") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				FINDABLE f = PATH().finders.indoor.getReservable(a.tc().x(), a.tc().y());
				if (f != null) {
					d.planTile.set(a.tc().x(), a.tc().y());
					f.findableReserve();
					return exit.set(a, d);
				}
				int dist = LAW.curfew().is() ? 256 : 64;
				AISubActivation s = AI.SUBS().walkTo.serviceInclude(a, d, PATH().finders.indoor, dist);
				
				if (s == null && a.inWater) {
					PATH().finders.water.findLand(a.physics.tileC(), d.path, 16);
					if (d.path.isSuccessful()) {
						d.planTile.set(-1, -1);
						s = AI.SUBS().walkTo.path(a, d);
					}
				}
				
				if (s != null) {
					d.planTile.set(d.path.destX(), d.path.destY());
					return s;
				}
				return exit.set(a, d);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				return exit.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};
		
		private final Resumer exit = new Resumer("sleeping") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = (byte) (5 + RND.rInt(5));
				if (LAW.curfew().is())
					return AI.SUBS().STAND.activateRndDir(a, d);
				return AI.SUBS().subSleep.activate(a, d);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (moduleCanContinue(a, d) && d.planByte1-- > 0) {
					if (d.plansub() == AI.SUBS().subSleep) {
						if (RND.oneIn(8))
							return AI.SUBS().STAND.activateRndDir(a, d);
						return AI.SUBS().subSleep.activate(a, d);
					}
					
					if (!RND.oneIn(7) && !LAW.curfew().is()) {
						return AI.SUBS().subSleep.activate(a, d);
					}else {
						return AI.SUBS().STAND.activateRndDir(a, d);
					}
					
				}
				can(a, d);
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				FINDABLE f = PATH().finders.indoor.getReserved(d.planTile.x(), d.planTile.y());
				if (f != null)
					f.findableReserveCancel();
			}
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				if (e.type == HPoll.CAN_INTERRACT)
					return d.plansub() == AI.SUBS().STAND ? 1.0 : 0;
				return super.poll(a, d, e);
			};
		};

		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			if (LAW.curfew().is())
				string.add(¤¤curfew);
			else
				string.add(¤¤name);
		};
		
	};
	

}
