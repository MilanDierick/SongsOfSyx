package settlement.entity.humanoid.ai.needs;

import static settlement.main.SETT.*;

import game.faction.FACTIONS;
import init.resources.Edible;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIData.AIDataSuspender;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.path.finder.SFinderMisc.FinderMiscWithoutDest;
import settlement.room.food.farm.FarmInstance;
import settlement.room.main.Room;
import settlement.room.service.food.canteen.ROOM_CANTEEN;
import settlement.room.service.food.eatery.ROOM_EATERY;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.stats.STATS;
import settlement.thing.THINGS.Thing;
import settlement.thing.ThingsCorpses.Corpse;
import settlement.tilemap.TGrowable;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;

public final class AIModule_Hunger extends AIModule{

	private final AIDataSuspender suspender = AI.suspender();
	private final AIDataSuspender suspenderStarvation = AI.suspender();
	
	private final ArrayList<Eatery> eateries;
	private final ArrayList<Canteen> canteens;
	private static AIModule_Hunger self;
	
	public AIModule_Hunger() {
		eateries = new ArrayList<Eatery>(ROOMS().EATERIES.size());
		for (ROOM_EATERY a : ROOMS().EATERIES) {
			eateries.add(new Eatery(a));
		}
		
		canteens = new ArrayList<Canteen>(ROOMS().CANTEENS.size());
		for (ROOM_CANTEEN a : ROOMS().CANTEENS) {
			canteens.add(new Canteen(a));
		}
		self = this;
	}
	

	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		for (ROOM_SERVICE_ACCESS_HASER s : a.race().service().EAT.get(a.indu().clas().index())) {
			s.service().clearAccess(a);
		}
		
		if (!suspender.is(d)) {
			for (ROOM_SERVICE_ACCESS_HASER s : a.race().service().EAT.get(a.indu().clas().index())) {
				if (s.service().room() instanceof ROOM_EATERY) {
					if (s.service().accessRequest(a)) {
						AiPlanActivation p = eateries.get(s.service().room().typeIndex()).activate(a, d);
						if (p != null)
							return p;
					}
				}else if (s.service().room() instanceof ROOM_CANTEEN) {
					if (s.service().accessRequest(a)) {
						AiPlanActivation p = canteens.get(s.service().room().typeIndex()).activate(a, d);
						if (p != null)
							return p;
					}
				}
				
				else {
					//throw new RuntimeException();
				}
			}
			
			AiPlanActivation p = eat.activate(a, d);
			if (p != null)
				return p;
			
			suspender.suspend(d);
		}
		
		if (STATS.FOOD().STARVATION.indu().get(a.indu()) > 0) {
			return starve.activate(a, d);
		}

		return null;
	}
	
	public static AiPlanActivation getFood(Humanoid a, AIManager d) {
		return self.eat.activate(a, d);
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateI) {
		suspender.update(d);
		suspenderStarvation.update(d);
		
	}
	
	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		if (STATS.FOOD().STARVATION.indu().getD(a.indu()) > 0)
			return 10;
		
		if (suspender.is(d))
			return 0;
		
		return (int) Math.ceil(STATS.NEEDS().HUNGER.getPrio(a.indu())*8);
		
	
	}
	
	private final AIPLAN eat = new AIPLAN.PLANRES(){

		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return fetchRaw.set(a, d);
		}
		
		private final Resumer fetchRaw = new Resumer("Finding Food") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				long m = RESOURCES.EDI().mask;
				if (STATS.FOOD().STARVATION.indu().get(a.indu()) <= 0) {
					m &= STATS.FOOD().fetchMask(a);
				}
				return AI.SUBS().walkTo.resource(a, d, m, Integer.MAX_VALUE);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				STATS.FOOD().eat(a, 0, 0);
				return eat.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				d.resourceCarriedSet(null);
			}
		};
		
		private final Resumer eat = new Resumer("Eating") {
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				if (d.resourceCarried() != null && RESOURCES.EDI().is(d.resourceCarried())) {
					FACTIONS.player().res().outConsumed.inc(d.resourceCarried(), 1);
				}
				d.resourceCarriedSet(null);
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				d.resourceCarriedSet(null);
			}

			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return subEat.activate(a, d);
			}
		};
		


		
		
	};
	
	private final AIPLAN starve = new AIPLAN.PLANRES(){
		
		

		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			if (!suspenderStarvation.is(d)) {
				if (edible.find(a.physics.tileC(), d.path)) {
					return goEatTerrain.set(a, d);
				}
				if (corpses.find(a.physics.tileC(), d.path)) {
					return goEatCorpse.set(a, d);
				}
				suspenderStarvation.suspend(d);
			}
			
			
			
			
			//misery
			return actCrazy.set(a, d);
		}
		
		private final Resumer goEatCorpse = new Resumer("Eating") {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().walkTo.pathRun(a, d);
			};
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				return eatCorpse.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return corpse(d.path.destX(), d.path.destY()) != null;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {

			}
		};
		
		private final Resumer eatCorpse = new Resumer("Cannibalising") {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return subEat.activate(a, d);
			};
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				Corpse c = corpse(d.path.destX(), d.path.destY());
				if (c != null) {
					SETT.ROOMS().CANNIBAL.reportCannibal();
					c.removeMeat();
					STATS.FOOD().eat(a, 0, 0);
					return null;
				}
				//kill other here
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
		
		private final Resumer goEatTerrain = new Resumer("Finding Food") {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().walkTo.pathRun(a, d);
			};
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				return eatTerrain.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return edible.isTile(d.path.destX(), d.path.destY());
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {

			}
		};
		
		private final Resumer eatTerrain = new Resumer("Gourging") {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				
				return subEat.activate(a, d);
			};
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				if (edible.isTile(d.path.destX(), d.path.destY())) {
					STATS.FOOD().eat(a, 0, 0);
					TERRAIN().get(d.path.destX(), d.path.destY()).clearing().clear1(d.path.destX(), d.path.destY());
					Room r = SETT.ROOMS().map.get(d.path.destX(), d.path.destY());
					if (r != null && r.destroyTileCan(d.path.destX(), d.path.destY()))
						r.destroyTile(d.path.destX(), d.path.destY());
					
					return null;
				}
				//kill other here
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
		
		private final Resumer actCrazy = new Resumer("Starving") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().desperate.activate(a, d);
				
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
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
		


		
		
	};

	private final AISUB subEat = new AISUB.Simple("ravaging food") {

		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			
			d.subByte ++;
			switch(d.subByte) {
			case 1 : return AI.STATES().STAND.activate(a, d, 1.5f+RND.rFloat(4));
			case 2 : return AI.STATES().anima.box.activate(a, d, 2.5+RND.rFloat(2));
			case 3 : return AI.STATES().STAND.activate(a, d, 1.5f+RND.rFloat(4));
			case 4 : return AI.STATES().anima.box.activate(a, d, 2.5+RND.rFloat(2));
			}
			return null;
		}

		
	};
	
	public final FinderMiscWithoutDest edible = new FinderMiscWithoutDest(32) {
		
		@Override
		protected boolean has() {
			return SETT.WEATHER().growthRipe.cropsAreRipe();
		};
		
		@Override
		public boolean isTile(int tx, int ty) {
			Room r = SETT.ROOMS().map.get(tx, ty);
			if (r != null && r instanceof FarmInstance) {
				return r.destroyTileCan(tx, ty);
			}
			return (TERRAIN().get(tx, ty) instanceof TGrowable) && ((TGrowable)TERRAIN().get(tx, ty)).isEdible(tx, ty) && ((TGrowable)TERRAIN().get(tx, ty)).size.get(tx, ty) > 0;
		}
	};
	
	private Corpse corpse(int tx, int ty) {
		for (Thing t : SETT.THINGS().get(tx, ty))
			if (t instanceof Corpse) {
				Corpse c = (Corpse) t;
				if (c.hasMeat())
					return c;
			}
		return null;
	}
	
	public final FinderMiscWithoutDest corpses = new FinderMiscWithoutDest(32) {
		
		@Override
		protected boolean has() {
			return true;
		};
		
		@Override
		public boolean isTile(int tx, int ty) {
			return corpse(tx, ty) != null;
		}
	};
	
	private final class Eatery extends AIPLAN.PLANRES {

		private final ROOM_EATERY e;
		
		private final Resumer walk;
		private final Resumer eat;
		
		Eatery(ROOM_EATERY e){
			this.e = e;
			
			walk = new Resumer(e.service().verb) {
				
				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					AISubActivation s = AI.SUBS().walkTo.service(a, d, e.service());
					if (s != null) {
						d.planTile.set(d.path.destX(), d.path.destY());
						e.service().reportDistance(a);
						e.service().reportAccess(a, d.path.destX(), d.path().destY());
						return s;
					}
					return null;
				}
				
				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					return eat.set(a, d);
				}
				
				@Override
				public boolean con(Humanoid a, AIManager d) {
					return true;
				}
				
				@Override
				public void can(Humanoid a, AIManager d) {
					
				}
			};
			
			eat = new Resumer(e.service().verb) {
				
				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					return subEat.activate(a, d);
				}
				
				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					
					Edible edi = a.race().pref().food.rnd();
					short da = e.eat(edi, STATS.FOOD().RATIONS.decree().get(a), d.planTile.x(), d.planTile.y());
					STATS.FOOD().eat(a, edi, da);
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
		}
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walk.set(a, d);
		}
		
		@Override
		protected boolean shouldContinue(Humanoid a, AIManager d) {
			FSERVICE s = e.service().finder.get(d.planTile.x(), d.planTile.y());
			return (s != null && s.findableReservedIs()) && super.shouldContinue(a, d);
		}
		
		@Override
		protected void cancel(Humanoid a, AIManager d) {
			FSERVICE s = e.service().finder.get(d.planTile.x(), d.planTile.y());
			if (s != null && s.findableReservedIs())
				s.findableReserveCancel();
			e.service().clearAccess(a);
			super.cancel(a, d);
		}
		
		
		
	}
	
	private final class Canteen extends AIPLAN.PLANRES {
		
		private final Resumer walk;
		private final Resumer eat;
		private final Resumer walkTable;
		private final Resumer walkLast;
		private final Resumer eatTable;

		
		Canteen(ROOM_CANTEEN e){
			
			walk = new Resumer(e.service().verb) {
				
				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					AISubActivation s = AI.SUBS().walkTo.service(a, d, e.service());
					if (s != null) {
						d.planTile.set(d.path.destX(), d.path.destY());
						e.service().reportDistance(a);
						e.service().reportAccess(a, d.path.destX(), d.path().destY());
						return s;
					}
					return null;
				}
				
				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					return eat.set(a, d);
				}
				
				@Override
				public boolean con(Humanoid a, AIManager d) {
					return true;
				}
				
				@Override
				public void can(Humanoid a, AIManager d) {
					
				}
			};
			
			eat = new Resumer(e.service().verb) {
				
				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					Edible edi = a.race().pref().food.rnd();
					short da = e.grab(edi, STATS.FOOD().RATIONS.decree().get(a), d.planTile.x(), d.planTile.y());
					STATS.FOOD().eat(a, edi, da);
					COORDINATE c = e.getChair(d.planTile.x(), d.planTile.y());
					if (c != null) {
						AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, c);
						if (s != null) {
							d.planTile.set(c);
							d.planObject = da;
							walkTable.set(a, d);
							return s;
						}
					}
					return subEat.activate(a, d);
				}
				
				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
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
			
			walkTable = new Resumer(e.service().verb) {
				
				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					return null;
				}
				
				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					return walkLast.set(a, d);
				}
				
				@Override
				public boolean con(Humanoid a, AIManager d) {
					return e.is(d.planTile);
				}
				
				@Override
				public void can(Humanoid a, AIManager d) {
					e.returnChair(d.planTile.x(), d.planTile.y());
				}
			};
			
			walkLast = new Resumer(e.service().verb) {
				
				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					DIR dir = e.setChair(d.planTile.x(), d.planTile.y(), (short)d.planObject);
					if (dir != null) {
						return AI.SUBS().single.activate(a, d, AI.STATES().WALK2.moveToEdge(a, d, dir));
					}
					return null;
				}
				
				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					a.speed.magnitudeInit(0);
					return eatTable.set(a, d);
				}
				
				@Override
				public boolean con(Humanoid a, AIManager d) {
					return walkTable.con(a, d);
				}
				
				@Override
				public void can(Humanoid a, AIManager d) {
					walkTable.can(a, d);
				}
			};
			
			eatTable = new Resumer(e.service().verb) {
				

				@Override
				public AISubActivation setAction(Humanoid a, AIManager d) {
					d.planByte1 = (byte) (4 + RND.rInt(10));
					return subEat.activate(a, d);
				};
				
				@Override
				public AISubActivation res(Humanoid a, AIManager d) {
					d.planByte1 --;
					if (d.planByte1 < 0) {
						can(a, d);
						return null;
					}else {
						return subEat.activate(a, d);
					}
				}
				
				@Override
				public boolean con(Humanoid a, AIManager d) {
					return walkTable.con(a, d);
				}
				
				@Override
				public void can(Humanoid a, AIManager d) {
					walkTable.can(a, d);
				}
			};
		}
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walk.set(a, d);
		}
		
		@Override
		protected AISubActivation resume(Humanoid a, AIManager d) {
			
			return super.resume(a, d);
		}
		
		
		
	}
	
}
