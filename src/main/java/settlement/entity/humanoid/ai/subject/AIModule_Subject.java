package settlement.entity.humanoid.ai.subject;

import init.D;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.main.throne.THRONE;
import settlement.room.spirit.dump.ROOM_DUMP;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.RND;
import util.data.INT_O;
import util.data.INT_O.INT_OE;

public final class AIModule_Subject extends AIModule{

	
	private final INT_OE<AIManager> kissed = new INT_O.INTWRAP<AIManager>(0b0000_0001, AIModules.data().byte3);
	private final INT_OE<AIManager> activity = new INT_O.INTWRAP<AIManager>(0b0000_0010, AIModules.data().byte3);
	private final PlanJoinArmy army = new PlanJoinArmy();
	private final PlanEmmigrate emmi = new PlanEmmigrate(); 

	private static CharSequence ¤¤swearing = "Swearing fealty";
	
	static {
		D.ts(AIModule_Subject.class);
	}
	
	static abstract class Activity extends AIPLAN.PLANRES {
		
	}
	
	public int debug(AIManager aiManager) {
		return kissed.get(aiManager);
	}
	
	private final AIPLAN immigrate = new AIPLAN.PLANRES() {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			
			COORDINATE c = SETT.PATH().finders.rndCoo.find(THRONE.coo().x(), THRONE.coo().y(), 8);
			if (c != null) {
				d.planTile.set(c);
				AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, d.planTile);
				start.set(a, d);
				return s;
			}
			
			kissed.set(d, 1);
			return null;
		}
		
		private final Resumer start = new Resumer(¤¤swearing) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return null;
			}
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				a.speed.turn2(THRONE.coo().x() - a.tc().x(), THRONE.coo().y() - a.tc().y());
				return swear.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};
		
		private final Resumer swear = new Resumer(¤¤swearing) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().LAY.activateTime(a, d, 4 + RND.rInt(10));
			}
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				kissed.set(d, 1);
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
	
	private final AIPLAN burryCorpse = new AIPLAN.PLANRES() {
		
		private ROOM_DUMP dump = SETT.ROOMS().DUMP;
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return start.set(a, d);
		}
		
		private final Resumer start = new Resumer(dump.service().verb) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				COORDINATE coo = dump.service().finder.reserve(a.tc(), Integer.MAX_VALUE);
				if (coo != null) {
					d.planTile.set(coo);
					if (SETT.PATH().finders.corpses.reserve(a.tc(), d.path, Integer.MAX_VALUE)) {
						d.planObject = SETT.PATH().finders.corpses.getResult().index();
						return AI.SUBS().walkTo.path(a, d);
					}
					dump.service().finder.getReserved(d.planTile.x(), d.planTile.y()).findableReserveCancel();
				}
				return null;
			}
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				
				return ret.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return SETT.THINGS().corpses.getByIndex((short) d.planObject) != null && SETT.THINGS().corpses.getByIndex((short) d.planObject).canBeDragged(); 
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				FSERVICE s = dump.service().service(d.planTile.x(), d.planTile.y());
				if (s != null)
					s.findableReserveCancel();
				Corpse c =  SETT.THINGS().corpses.getByIndex((short) d.planObject);
				if (c != null)
					c.findableReserveCancel();
			}
		};
		
		private final Resumer ret = new Resumer(dump.service().verb) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				Corpse c =  SETT.THINGS().corpses.getByIndex((short) d.planObject);
				return AI.SUBS().walkTo.drag(a, d, SETT.THINGS().corpses.draggable, c.index(), d.planTile);
			}
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				Corpse c = SETT.THINGS().corpses.getByIndex((short) d.planObject);
				if (c != null) {
					dump.burry(c, d.planTile.x(), d.planTile.y());
					c.remove();
				}else {
					can(a, d);
				}
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return dump.service().service(d.planTile.x(), d.planTile.y()) != null;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				FSERVICE s = dump.service().service(d.planTile.x(), d.planTile.y());
				if (s != null)
					s.findableReserveCancel();
				Corpse c = SETT.THINGS().corpses.getByIndex((short) d.planObject);
				if (c != null)
					c.findableReserveCancel();
			}
		};
	};
	
	private final AIPLAN[] activities = new AIPLAN[] {
		new ActivityMourn(),new ActivityExecute(),new ActivityCourt(),new ActivityStock()
	};
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {

		if (emmi.shouldEmmigrate(a)) {
			return emmi.activate(a, d);
		}
		
		if (army.getPriority(a) != 0) {
			AiPlanActivation s = army.activate(a, d);
			if (s != null)
				return s;
		}
		
		if (kissed.get(d) == 0 && a.indu().hType().player)
			return immigrate.activate(a, d);
		
		if (SETT.ROOMS().DUMP.service().finder.has(a.tc()) && SETT.ARMIES().enemy().men() == 0) {
			if (SETT.PATH().finders.corpses.has(a.tc())) {
				AiPlanActivation p = burryCorpse.activate(a, d);
				if (p != null)
					return p;
			}
		}
		
		
		if (activity.isMax(d)) {
			activity.set(d, 0);
			int i = RND.rInt(activities.length);
			for (int k = 0; k < activities.length; k++) {
				AiPlanActivation p = activities[(k+i)%activities.length].activate(a, d);
				if (p != null)
					return p;
			}
		}
		
		
		return null;
	}


	
	@Override
	protected void init(Humanoid a, AIManager d) {
		AIModules.data().byte2.set(d, 0);
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		if (newDay)
			activity.set(d, 1);
	}
	
	@Override
	public int getPriority(Humanoid a, AIManager d) {
		if (army.getPriority(a) != 0)
			return 10;
		
		if (kissed.get(d) == 0 && a.indu().hType() == HTYPE.SUBJECT)
			return 5;
		if (SETT.ROOMS().DUMP.service().finder.has(a.tc()) && SETT.ARMIES().enemy().men() == 0) {
			if (SETT.PATH().finders.corpses.has(a.tc())) {
				return 5;
			}
		}
		if (activity.isMax(d))
			return 2;
		if (AIModules.current(d) == this && d.plan() instanceof Activity)
			return 2;
		return 0;
	}

}
