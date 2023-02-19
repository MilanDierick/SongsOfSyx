package settlement.entity.humanoid.ai.types.prisoner;

import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.main.ROOMA;
import settlement.room.main.RoomInstance;
import settlement.stats.CAUSE_LEAVE;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.law.PRISONER_TYPE;
import settlement.stats.law.Processing.Punishment;
import snake2d.util.rnd.RND;
import util.dic.DicMisc;

public final class AIModule_Prisoner extends AIModule{
	

	public static PrisonerData DATA() {
		return PrisonerData.self;
	}
	
	private final Executed executed = new Executed();
	private final Prison prison = new Prison();
	private final Judged judged = new Judged();
	private final Enslaved slave = new Enslaved();
	private final Temple temple = new Temple();
	private final Arena arena = new Arena();
	private final Stocked stocked = new Stocked();
	public static final byte PRISON_DAYS = (byte) (4*TIME.years().bitConversion(TIME.days()));
	
	public AIModule_Prisoner() {
		new PrisonerData();
		for (Punishment p : LAW.process().punishments)
			plan(p, RACES.all().get(0));
	}
	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {

		AiPlanActivation s = judge(a, d);
		if (s != null)
			return s;
		s = temple.activate(a, d);
		if (s != null)
			return s;
		s = arena.activate(a, d);
		if (s != null)
			return s;
		Punishment p = DATA().punishment.get(d);
		AIPLAN plan = plan(p, a.race());
		s = plan.activate(a, d);
		if (s != null)
			return s;
		if (plan != prison) {
			s = prison.activate(a, d);
			if (s != null)
				return s;
		}
		return exile.activate(a, d);
		
	}
	
	private AIPLAN plan(Punishment p, Race race) {
		if(p == LAW.process().execution)
			return executed;
		if (p == LAW.process().prison) {
			if (TIME.light().dayIs() && TIME.light().partOf() < 0.5 && LAW.process().stocks.allowed.is(race))
				return stocked;
			return prison;
		}
		if (p == LAW.process().enslaved)
			return slave;
		if (p == LAW.process().pardoned) {
			LAW.process().pardoned.inc(race);
			return free;
		}
		if (p == LAW.process().exile)
			return exile;
		throw new RuntimeException(p + " " + p.name);
	}
	
	private final AiPlanActivation judge(Humanoid a, AIManager d) {
		if (DATA().judged.get(d) == 0 && DATA().noJudge.get(d) == 0) {
			AiPlanActivation s = judged.activate(a, d);
			if (s != null)
				return s;
			if (DATA().hasWaitedJudge.isMax(d)) {
				return null;
			}
			return prison.activate(a, d);
		}
		return null;
	}
	
	@Override
	protected void init(Humanoid a, AIManager d) {
		DATA().init(a, d);
	}
	
	public static boolean isPrisoner(Humanoid a, RoomInstance room) {
		AIManager d = (AIManager) a.ai();
		return a.indu().hType() == HTYPE.PRISONER && room.is(AI.modules().coo(d));
	}

	@Override
	public void evictFromRoom(Humanoid a, AIManager d, ROOMA r) {
		if (r.is(AI.modules().coo(d))) {
			cancel(a, d);
			AI.modules().coo(d).set(-1, -1);
		}
		super.evictFromRoom(a, d, r);
	}
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {

		SETT.ROOMS().PRISON.unregisterPrisoner(AI.modules().coo(d));
		AI.modules().coo(d).set(-1, -1);
		if ((DATA().judged.get(d) == 0 && DATA().noJudge.get(d) == 0)) {
			LAW.process().judgement.inc(a.race(), false);
		}
		if (d.plan() != arena && d.plan() != temple) {

			if (DATA().reportedPunish.get(d) == 0) {
				LAW.process().exile.inc(a.race());
			}
		}
		super.cancel(a, d);
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		if (newDay) {
			DATA().prisonTimeLeft.inc(d, -1);
			DATA().hasWaitedJudge.inc(d, 1);
		}
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		return 10;
	}
	
	public void makePrisoner(Humanoid h, AIManager m) {
		
		
		if (h.indu().hType() == HTYPE.PRISONER)
			return;
		if (h.indu().hType().hostile) {
			h.kill(false, CAUSE_LEAVE.EXECUTED);
			return;
		}
		
		if (h.indu().hType() == HTYPE.SLAVE) {
			h.kill(false, CAUSE_LEAVE.EXECUTED);
			return;
		}
		boolean court = h.indu().hType().player;
		h.HTypeSet(HTYPE.PRISONER, CAUSE_LEAVE.PUNISHED, null);
		if (court) {
			DATA().noJudge.set(m, 0);
		}
		m.overwrite(h, plan);
		
		
		
	}
	
	private final AIPLAN plan = new AIPLAN.PLANRES() {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return start.set(a, d);
		}
		
		Resumer start = new Resumer("unconsious") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().LAY.activateTime(a, d, 10+RND.rInt(10));
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				// TODO Auto-generated method stub
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				return false;
			};
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				return 0;
			};
		};
	};
	
	private final AIPLAN exile = new AIPLAN.PLANRES() {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			
			return start.set(a, d);
		}
		
		Resumer start = new Resumer(LAW.process().exile.verb) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				if (SETT.PATH().finders.entryPoints.find(a.tc().x(), a.tc().y(), d.path, Integer.MAX_VALUE)){
					return AI.SUBS().walkTo.pathFull(a, d);
				}
				return finish(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return finish(a, d);
			}
			
			private AISubActivation finish(Humanoid a, AIManager d) {
				if (STATS.LAW().prisonerType.get(a.indu()) != PRISONER_TYPE.WAR)
					LAW.process().exile.inc(a.race());
				if (DATA().reportedPunish.get(d) == 1) {
					DATA().punishment.get(d).dec(a.race());
				}
				DATA().reportedPunish.set(d,1);
				AIManager.dead = CAUSE_LEAVE.EXILED;
				return AI.SUBS().LAY.activateTime(a, d, 10);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				return super.event(a, d, e);
			};
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				return super.poll(a, d, e);
			};
		};
		
		@Override
		protected void cancel(Humanoid a, AIManager d) {
			super.cancel(a, d);
		}
	};
	
	private final AIPLAN free = new AIPLAN.PLANRES() {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return start.set(a, d);
		}
		
		Resumer start = new Resumer(DicMisc.¤¤Free) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().STAND.activate(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return free.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				// TODO Auto-generated method stub
				
			}
		};
		
		final Resumer free = ResFree.make(this);
	};

}
