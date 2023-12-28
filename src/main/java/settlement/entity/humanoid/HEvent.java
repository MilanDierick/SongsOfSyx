package settlement.entity.humanoid;

import game.boosting.BOOSTABLES;
import init.config.Config;
import settlement.entity.ENTITY;
import settlement.entity.ENTITY.ECollision;
import settlement.entity.EPHYSICS;
import settlement.entity.animal.Animal;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import settlement.stats.util.CAUSE_LEAVE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public enum HEvent {
	
	MEET_HARMLESS,
	MEET_ENEMY,
	COLLISION_HARD,
	/**
	 * return true if your speed should be altered
	 */
	COLLISION_SOFT,
	/**
	 * return true if your speed should be altered
	 */
	COLLISION_TILE,
	/**
	 * Called once in awhile. Lets you check the surroundings
	 */
	CHECK_MORALE,
	EXHAUST,
	NOTIFY_CRIME,
	MAKE_PRISONER,
	ROOM_REMOVED,
	PRISON_EXECUTE,
	COLLISION_UNREACHABLE,
	INTERRACT,
	FISHINGTRIP_OVER,
	;
	
	public static final LIST<HEvent> all = new ArrayList<HEvent>(values());
	
	private HEvent() {
		
	}
	
	private static HEventData event = new HEventData();
	public static final class HEventData {

		
		public HEvent event;
		/**
		 * collisions
		 */
		public double norX, norY, momentum;
		public ENTITY other;
		public RoomInstance room;
		public double facingDot;
		public boolean broken;
		public int tx; 
		public int ty;
		public boolean speedHasChanged;
		
		private HEventData() {
			
		}
	}
	
	public static class Handler {
		
		private Handler() {
			
		}
		
		static boolean  debug = false;
		
		static void collide(Humanoid a, AIManager ai, ECollision coll) {
			
			boolean hostile = coll.damageStrength > 0 || (coll.other != null && coll.other instanceof Humanoid && HPoll.Handler.isEnemy(a, (Humanoid)coll.other));
			
			
			double mom = coll.momentum*a.physics.getMassI()*EPHYSICS.MOM_TRESHOLDI;
			
			CAUSE_LEAVE l = coll.leave;
			if (l == null)
				l = coll.other instanceof Animal ? CAUSE_LEAVE.ANIMAL : CAUSE_LEAVE.SLAYED;
			
			
			
			if (mom > 4) {
				a.inflictDamage(2, l);
				if (a.isRemoved()) {
					if (coll.other instanceof Animal) {
						Animal an = (Animal) coll.other;
						SETT.ANIMALS().spawn.reportKillRevenge(an.species());
					}
				};
				return;
			}
			
			if (coll.damageStrength > 0) {
				
				double dam = coll.damageStrength;
				
				for (int i = 0; i < BOOSTABLES.BATTLE().DAMAGES.size(); i++) {
					double d = coll.damageStrength*coll.damage[i]/(1.0 + BOOSTABLES.BATTLE().DAMAGES.get(i).defence.get(a.indu()));
					dam += d;
				}
				
				dam *= RND.rFloat()*Config.BATTLE.DAMAGE;
				dam /= 5.0*(1 + BOOSTABLES.BATTLE().BLUNT_DEFENCE.get(a.indu()));
				//dam -= RND.rFloat()*4.0;
				
				
				
				if (dam > 0 && !a.inflictDamage(dam, l)) {
					if (a.isRemoved()) {
						if (coll.other instanceof Animal) {
							Animal an = (Animal) coll.other;
							SETT.ANIMALS().spawn.reportKillRevenge(an.species());
						}
					};
					return;
				}				
			}
			
			
			
			event.momentum = mom;
			event.norX = coll.norX;
			event.norY = coll.norY;
			event.speedHasChanged = coll.speedHasChanged;
			
			if (mom > 1.0) {
				event.event = COLLISION_HARD;
				ai.event(a, event);
				return;
			}
			event.facingDot = coll.dirDot;
			event.other = coll.other;
			
			if (coll.other instanceof Humanoid) {
				
				if (hostile) {
					event.event = MEET_ENEMY;
				}else {
					if (mom > 0)
						event.event = COLLISION_SOFT;
					else
						event.event = MEET_HARMLESS;
				}
				ai.event(a, event);
			}else {
				if (mom > 0 || coll.other == null)
					event.event = COLLISION_SOFT;
				else
					event.event = MEET_HARMLESS;
				ai.event(a, event);
			}
			
			
		}
		
		static void meet(Humanoid a, AIManager ai, ENTITY other) {
			event.event = MEET_HARMLESS;
			event.other = other;
			if (other instanceof Animal && (STATS.RAN().get(a.indu(), 0) & 0x01FF) == 0)
				STATS.POP().FRIEND.set(a.indu(), other);
			ai.event(a, event);
		}

		
		public static void notifyCrime(Humanoid a, ENTITY criminal) {
			event.event = NOTIFY_CRIME;
			event.other = criminal;
			a.ai.event(a, event);
		}
		
		public static void exhaust(Humanoid a) {
			event.event = EXHAUST;
			a.ai.event(a, event);
		}
		
		public static void checkMorale(Humanoid a) {
			event.event = CHECK_MORALE;
			a.ai.event(a, event);
		}
		
		public static void removeRoom(Humanoid a, RoomInstance room) {
			AI.modules().evictFromRoom(a, a.ai, room);
			event.event = ROOM_REMOVED;
			event.room = room;
			a.ai.event(a, event);
		}
		
		public static void executePrisoner(Humanoid a) {
			event.event = PRISON_EXECUTE;
			a.ai.event(a, event);
		}
		
		public static void collisionUnreachable(Humanoid a) {
			event.event = COLLISION_UNREACHABLE;
			a.ai.event(a, event);
		}
		
		public static void fishingTripOver(Humanoid a, double time) {
			event.event = FISHINGTRIP_OVER;
			event.momentum = time;
			a.ai.event(a, event);
		}

		public static boolean interract(Humanoid a, Humanoid friend) {
			event.event = INTERRACT;
			event.other = friend;
			return a.ai.event(a, event);
		}
		
		static boolean collideTile(Humanoid a, AIManager ai, double norX, double norY, double momentum, boolean broken, int tx, int ty) {
			 momentum*=EPHYSICS.MOM_TRESHOLDI;
			 event.momentum = momentum;
			event.norX = norX;
			event.norY = norY;
			event.speedHasChanged = true;
			if (momentum >= 1) {
				event.event = COLLISION_HARD;
				ai.event(a, event);
				return true;
			}else {
				event.event = COLLISION_TILE;
				event.tx = tx;
				event.ty = ty;
				event.broken = broken;
				return ai.event(a, event);
			}
		}
		
		public static boolean makePrisoner(Humanoid a) {
			event.event = HEvent.MAKE_PRISONER;
			return a.ai.event(a, event);
		}
		
	}
	
}
