package settlement.entity.humanoid.ai.danger;

import static settlement.main.SETT.*;

import init.need.NEED;
import init.need.NEEDS;
import init.resources.RBIT.RBITImp;
import settlement.entity.animal.ANIMAL_ROOM_RUINER;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIData.AIDataSuspender;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.path.finder.SFinderMisc.FinderMiscWithoutDest;
import settlement.room.main.Room;
import settlement.stats.STATS;
import settlement.stats.util.CAUSE_LEAVE;
import settlement.thing.THINGS.Thing;
import settlement.thing.ThingsCorpses.Corpse;
import settlement.tilemap.terrain.TGrowable;
import snake2d.util.rnd.RND;

class AIModule_Starvation extends AIModule{

	private final AIDataSuspender suspenderStarvation = AI.suspender();
	final RBITImp bits = new RBITImp();
	private final NEED need = NEEDS.TYPES().HUNGER;
	
	public AIModule_Starvation() {
		
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		AiPlanActivation p = AI.modules().needs.get(a, d, need, Integer.MAX_VALUE);
		if (p != null)
			return p;
		
		if (STATS.FOOD().STARVATION.indu().getD(a.indu()) > 0) {
			return starve.activate(a, d);
		}
		
		return null;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateI) {
		suspenderStarvation.update(d);
	}
	
	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		if (STATS.FOOD().STARVATION.indu().getD(a.indu()) > 0)
			return 10;
		return 0;
		
	}
	
	public final AISUB eat = new AISUB.Simple("eating") {

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

	private final AIPLAN starve = new AIPLAN.PLANRES(){
		
		public final FinderMiscWithoutDest edible = new FinderMiscWithoutDest(32) {
			
			@Override
			protected boolean has() {
				return SETT.WEATHER().growthRipe.cropsAreRipe();
			};
			
			@Override
			public boolean isTile(int tx, int ty) {
				Room r = SETT.ROOMS().map.get(tx, ty);
				if (r != null && r instanceof ANIMAL_ROOM_RUINER) {
					return ((ANIMAL_ROOM_RUINER)r).canBeGraced(tx, ty);
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

		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			if (need.stat().stat().indu().isMax(a.indu()))
				AIManager.dead = CAUSE_LEAVE.STARVED;
			
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
				return eat.activate(a, d);
			};
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				Corpse c = corpse(d.path.destX(), d.path.destY());
				if (c != null) {
					SETT.ROOMS().CANNIBAL.reportCannibal();
					c.removeMeat();
					STATS.FOOD().eat(a, 0, 0);
					need.stat().fix(a.indu());
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
				
				return eat.activate(a, d);
			};
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				if (edible.isTile(d.path.destX(), d.path.destY())) {
					STATS.FOOD().eat(a, 0, 0);
					need.stat().fix(a.indu());
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

}
