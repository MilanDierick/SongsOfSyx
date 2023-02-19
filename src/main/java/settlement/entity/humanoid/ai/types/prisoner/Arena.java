package settlement.entity.humanoid.ai.types.prisoner;

import game.time.TIME;
import init.D;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.*;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.ai.battle.SubFight;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.service.arena.ROOM_ARENA;
import settlement.stats.STATS;
import snake2d.util.datatypes.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;

class Arena extends AIPLAN.PLANRES{

	private static CharSequence ¤¤name = "¤Fighting in the Arena";
	private static double inj = 0.4;
	static {
		D.ts(Arena.class);
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		if (STATS.NEEDS().INJURIES.count.getD(a.indu()) >= inj)
			return null;
		for (ROOM_ARENA arena : SETT.ROOMS().ARENAS) {
			COORDINATE c = arena.gladiatorGetSpot();
			if (c == null)
				continue;
			d.planByte1 = (byte) arena.typeIndex();
			d.planByte3 = (byte) (TIME.hours().bitsSinceStart() & 0b01111111);
			d.planTile.set(c);
			return walk.set(a, d);
			
		}
		return null;
	}
	
	private boolean outOfTime(AIManager d) {
		byte b = (byte) (TIME.hours().bitsSinceStart() & 0b01111111);
		if (b > d.planByte3) {
			b -= d.planByte3;
		}else {
			b = (byte) (0b01111111 - d.planByte3 + b);
		}
		return b > TIME.hoursPerDay;
	}
	
	private final SubFight fightSub = new SubFight() {
		
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			if (!shouldFight(a, d))
				return null;
			if (!isFighter(d.otherEntity()))
				return null;
			return super.resume(a, d);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			
			if (e.type == HPoll.IS_ENEMY) {
				return isFighter(e.other) ? 1 :0;
			}
			if (e.type == HPoll.COLLIDING)
				return 1;
			
			return super.poll(a, d, e);
		}
		
	};
	
	private boolean shouldFight(Humanoid a, AIManager d) {
		if (!arena(a, d).gladiatorInArena(a.tc().x(), a.tc().y()))
			return false;
		if (STATS.NEEDS().INJURIES.count.getD(a.indu()) > inj)
			return false;
		if (STATS.NEEDS().EXHASTION.indu().getD(a.indu()) > 0.75)
			return false;
		
		return true;
	}
	
	private final Resumer walk = new Resumer(¤¤name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			STATS.NEEDS().HUNGER.fixMax(a.indu());
			STATS.NEEDS().INJURIES.count.set(a.indu(), 0);;
			AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, d.planTile);
			if (s != null)
				return s;
			cancel(a, d);
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return taunt.set(a, d);
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
	
	private final Resumer taunt = new Resumer(¤¤name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			a.speed.impulseBreak(1.0);
			d.planByte2 = 4;
			a.speed.setDirCurrent(DIR.ALL.rnd());
			arena(a, d).gladiatorDrawMakeSheer(d.planTile);
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.armsOut, 3+RND.rInt(3));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			d.planByte2 -= 1;
			if (d.planByte2 <= 0) {
				return ready.set(a, d);
			}
			a.speed.setDirCurrent(DIR.ALL.rnd());
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.armsOut, 3+RND.rInt(3));
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
	
	private final Resumer ready = new ResFigher() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.sword, 4+RND.rInt(4));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (!shouldFight(a, d) || !arena(a, d).gladiatorShouldFight(d.planTile) || outOfTime(d)) {
				
				cancel(a, d);
				return null;
			}
			
			Humanoid other = other(a, d);
			if (other != null) {
				
				d.otherEntitySet(other);
				arena(a, d).gladiatorDrawMakeSheer(d.planTile);
				return fight.set(a, d);
			}
			a.speed.setDirCurrent(DIR.ALL.rnd());
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.sword, 4+RND.rInt(4));
		}
		
		private Humanoid other(Humanoid a, AIManager d) {
			RECTANGLE rec = arena(a, d).gladiatorArea(d.planTile);
			LIST<ENTITY> ents = SETT.ENTITIES().fillTiles(rec);
			if (ents.size() < 1)
				return null;
			int k = RND.rInt(ents.size());
			for (int i = 0; i < ents.size(); i++) {
				ENTITY e = ents.getC(i+k);
				if (e == a)
					continue;
				if (e instanceof Humanoid) {
					Humanoid a2 = (Humanoid) e;
					AIManager d2 = (AIManager) a2.ai();
					if (d2.plan() == Arena.this) {
						if (Arena.this.getResumer(d2) == ready && arena(a, d).gladiatorInArena(a2.tc().x(), a2.tc().y())) {
							d2.otherEntitySet(a);
							if (!shouldFight(a2, d2))
								continue;
							if (!isFighter(d2.otherEntity()))
								continue;
							d2.overwrite(a2, fight.set(a2, d2));
							return a2;
						}
						if (Arena.this.getResumer(d2) == fight) {
							return a2;
						}
					}
				}
				
			}
			return null;
		}
		

	};
	
	private final Resumer fight = new ResFigher() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = fightSub.activate(a, d);
			if (s == null) {
				cancel(a, d);
			}
			return s;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (STATS.NEEDS().INJURIES.count.getD(a.indu()) >= inj)
				return yield.set(a, d);
			if (!isFighter(d.otherEntity()))
				return taunt2.set(a, d);
			if (arena(a, d).gladiatorShouldFight(d.planTile) && outOfTime(d)) {
				return walk.set(a, d);
			}
			cancel(a, d);
			return null;
		}
		
	};
	
	private final Resumer yield = new Resumer(¤¤name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().LAY.activateTime(a, d, 8+RND.rInt(14));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			cancel(a, d);
			return null;
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
	
	
	
	private final Resumer taunt2 = new Resumer(¤¤name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			a.speed.impulseBreak(1.0);
			arena(a, d).gladiatorDrawMakeSheer(d.planTile);
			d.planByte2 = 4;
			a.speed.setDirCurrent(DIR.ALL.rnd());
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.armsOut, 3+RND.rInt(3));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			d.planByte2 -= 1;
			if (d.planByte2 <= 0) {
				if (arena(a, d).gladiatorInArena(a.tc().x(), a.tc().y()))
					return ready.set(a, d);
				return walk.set(a, d);
			}
			a.speed.setDirCurrent(DIR.ALL.rnd());
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.armsOut, 3+RND.rInt(3));
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
	
	private boolean isFighter(ENTITY e) {
		if (e instanceof Humanoid && !e.isRemoved()) {
			Humanoid a2 = (Humanoid) e;
			AIManager d2 = (AIManager) a2.ai();
			if (d2.plan() == Arena.this) {
				return Arena.this.getResumer(d2) instanceof ResFigher;
			}
		}
		
		return false;
		
	}
	

	

	

	
	private final Resumer removed = new Resumer(¤¤name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().STAND.activate(a, d);
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
			// TODO Auto-generated method stub
			
		}
	};
	
	private ROOM_ARENA arena(Humanoid a, AIManager d) {
		return SETT.ROOMS().ARENAS.get(d.planByte1);
	}
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		//GAME.Notify("here" + " " + d.planTile + " " + a.id() + " " + a.tc());
		arena(a, d).gladiatorReturnSpot(d.planTile);
		d.planTile.set(-1, -1);
		super.cancel(a, d);
	}
	
	@Override
	public boolean event(Humanoid a, AIManager d, HEventData e) {
		if (e.event == HEvent.ROOM_REMOVED && SETT.ROOMS().map.get(d.planTile) == e.room) {
			d.planTile.set(-1,-1);
			d.overwrite(a, removed.set(a, d));
			return true;
		}
		if (e.event == HEvent.MEET_ENEMY) {
			if (isFighter(e.other)) {
				
				if (d.plansub() == fightSub) {
					fightSub.event(a, d, e);
				}else if (shouldFight(a, d)) {
					d.otherEntitySet((Humanoid) e.other);
					if (d.otherEntity() != null) {
						d.overwrite(a, fight.set(a, d));	
					}
				}
				return true;
			}
		}
		if (e.event == HEvent.CHECK_MORALE)
			return true;
		return super.event(a, d, e);
	}
	
	private abstract class ResFigher extends Resumer {

		protected ResFigher() {
			super(¤¤name);
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
			
			if (e.type == HPoll.IS_ENEMY) {
				return isFighter(e.other) ? 1 :0;
			}
			return super.poll(a, d, e);
		}

			

	}

}
