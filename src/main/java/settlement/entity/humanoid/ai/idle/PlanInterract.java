package settlement.entity.humanoid.ai.idle;

import game.GAME;
import game.time.TIME;
import init.D;
import init.boostable.BOOSTABLES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISTATES.Animation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.path.components.SComponent;
import settlement.room.home.HOME;
import settlement.stats.CAUSE_LEAVE;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;

class PlanInterract {

	private static CharSequence ¤¤name = "¤Hanging out with {0}";
	private static CharSequence ¤¤nameMeet = "¤Meeting up with {0}";
	private static CharSequence ¤¤fighting = "¤Fighting {0}";
	private static CharSequence ¤¤knockedOut = "¤Knocked out";
	
	static {
		D.ts(PlanInterract.class);
	}
	
	final AIPLAN lookForFriend = new AIPLAN.PLANRES() {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return first.set(a, d);
		}
		
		private final Resumer first = new Resumer("") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.otherEntitySet(null);
				Humanoid o = findAndSet(a);
				if (o == null)
					return null;
				if (!d.path.request(a.tc(), o.tc().x(), o.tc().y()))
					return null;

				d.otherEntitySet(o);
				return AI.SUBS().walkTo.path(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (friend(a, d) != null && a.tc().tileDistanceTo(friend(a, d).tc()) < 3)
					return d.resumeOtherPlan(a, interract);
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
			protected void name(Humanoid a, AIManager d, Str string) {
				Humanoid o = d.otherEntity();
				if (o == null)
					string.add(AIModule_Idle.¤¤name);
				else
					string.add(¤¤nameMeet).insert(0, STATS.APPEARANCE().name(o.indu()));
			}
		};
		
		private Humanoid findAndSet(Humanoid a) {
			SComponent c = SETT.PATH().finders().otherHumanoid.findComp(a, 20);
			if (c == null)
				return null;
			
			int dim = c.level().size()+2;
			int x1 = (c.centreX()&~(c.level().size()-1))-1;
			int y1 = (c.centreY()&~(c.level().size()-1))-1;
			int x2 = x1+dim;
			int y2 = y1+dim;
			
			int rx = x1 + RND.rInt(dim);
			int ry = y1 + RND.rInt(dim);
			Humanoid backup = null;
			
			
			
			for (int y = 0; y < dim; y++) {
				for (int x = 0; x < dim; x++) {

					for (ENTITY e : SETT.ENTITIES().getAtTile(rx, ry)) {
						if (e != a && e instanceof Humanoid) {
							Humanoid o = (Humanoid) e;
							
							if (HEvent.Handler.interract(o, a)) {
								if (o.race() == a.race()) {
									return o;
								}else if (backup == null){
									backup = o;
								}
							}
						}
					}
					rx++;
					if (rx >= x2) {
						rx = x1;
						ry++;
						if (ry >= y2) {
							ry = y1;
						}
					}

				}
			}
			
			if (backup != null) {
				if (shouldFight(a, backup)) {
					if (GAME.events().raceWars.isAtOdds(a.race(), backup.race()))
						return backup;
					HOME h = STATS.HOME().GETTER.get(a, this);
					if (h == null) {
						return null;
					}
					if (a.tc().tileDistanceTo(h.service()) > 16) {
						h.done();
						return null;
					}
					h.done();
					h = STATS.HOME().GETTER.get(backup, this);
					if (h == null) {
						return null;
					}
					if (backup.tc().tileDistanceTo(h.service()) > 16) {
						h.done();
						return null;
					}
					h.done();
				}
				
			}
			
			return backup;
		}
		
	}; 
	
	final AIPLAN interract = new AIPLAN.PLANRES() {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return first.set(a, d);
		}
		
		private final Resumer first = new Resumer("") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = 0;
				return AI.SUBS().STAND.activateRndDir(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				if (!conn(a, d))
					return null;
				
				Humanoid o = friend(a, d);

				if (((AIManager)o.ai()).plan() == lookForFriend) {
					d.planByte1 ++;
					if (d.planByte1 > 100)
						return null;
					return AI.SUBS().STAND.activateRndDir(a, d);
				}
				
				if (shouldFight(a, o))
					return fight.set(a, d);
				return social.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};
		
		private final Resumer fight = new Resumer("") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = (byte) (15+RND.rInt(30));
				STATS.POP().FRIEND.set(a.indu(), friend(a, d));
				
				return res(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				d.planByte1 --;
				if (d.planByte1 < 0)
					return null;
				if (!conn(a, d))
					return null;
				if (STATS.NEEDS().INJURIES.count.getD(a.indu()) > 0.25) {
					return d.resumeOtherPlan(a, out);
				}
				Humanoid o = friend(a, d);
				if (RND.rBoolean()) {
					a.speed.turn2(a.body(), o.body());
					return AI.SUBS().STAND.activateTime(a, d, 1 +RND.rInt(2));
				}
				double dam = RND.rFloat()*0.25*BOOSTABLES.BATTLE().BLUNT_DAMAGE.get(a)/BOOSTABLES.PHYSICS().MASS.get(o);
				if (dam > 0.4)
					dam = 0.4;
				o.inflictDamage(dam, 0, CAUSE_LEAVE.BRAWL);
				return AI.SUBS().DUMMY.activate(a, d, AI.STATES().anima.box.activate(a, d, 1));
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};
		
		private final Resumer social = new Resumer("") {
			
			private final Animation[] ani = new Animation[] {
				AI.STATES().anima.carry,
				AI.STATES().anima.fist,
				AI.STATES().anima.grab,
				AI.STATES().anima.fistRight,
				AI.STATES().anima.fistRight,
				AI.STATES().anima.fistRight,
			};
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = (byte) (15+RND.rInt(30));
				if (STATS.POP().FRIEND.get(a.indu()) == null || RND.oneIn(10)) {
					STATS.POP().FRIEND.set(a.indu(), friend(a, d));
				}
				return res(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				d.planByte1 --;
				if (d.planByte1 < 0)
					return null;
				if (!conn(a, d))
					return null;
				Humanoid o = friend(a, d);
				if (RND.rBoolean()) {
					a.speed.turn2(a.body(), o.body());
					return AI.SUBS().STAND.activateRndDir(a, d, RND.rInt(5));
				}
				return AI.SUBS().DUMMY.activate(a, d, ani[RND.rInt(ani.length)].activate(a, d, RND.rFloat(3)));
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};
		
		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			Humanoid o = friend(a, d);
			if (o == null)
				string.add(AIModule_Idle.¤¤name);
			else if (((AIManager)o.ai()).plan() == lookForFriend)
				string.add(¤¤nameMeet).insert(0, STATS.APPEARANCE().name(o.indu()));
			else if (shouldFight(a, o))
				string.add(¤¤fighting).insert(0, STATS.APPEARANCE().name(o.indu()));
			else
				string.add(¤¤name).insert(0, STATS.APPEARANCE().name(o.indu()));
		}
		
	}; 
	
	private final AIPLAN out = new AIPLAN.PLANRES() {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return next.set(a, d);
		}
		
		private final Resumer next = new Resumer(¤¤knockedOut) {
			
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
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};
	};

	private Humanoid friend(Humanoid a, AIManager d) {
		Humanoid h = d.otherEntity();
		if (h != null && (((AIManager)h.ai()).plan() == interract || ((AIManager)h.ai()).plan() == lookForFriend))
			return h;
		return null;
	}
	
	private static double CHA = 1.0/0x0FFFFFF;
	
	public boolean shouldFight(Humanoid a, Humanoid b) {
		
		double c = 1.0-a.race().pref().other(b.indu().race());
		
		long ran = a.indu().randomness() + b.indu().randomness() + TIME.days().bitsSinceStart();
		double d = CHA*(ran&0x0FFFFFF);
		return c > d;
	}
	
	public boolean conn(Humanoid a, AIManager d) {
		return friend(a, d) != null && AIModules.nextPrio(d) < 5;
	}
	

	
	
	
}
