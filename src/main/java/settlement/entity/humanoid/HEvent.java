package settlement.entity.humanoid;

import init.RES;
import init.boostable.BOOSTABLES;
import settlement.army.DivMorale;
import settlement.entity.ENTITY;
import settlement.entity.ENTITY.ECollision;
import settlement.entity.EPHYSICS;
import settlement.entity.animal.Animal;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.room.main.RoomInstance;
import settlement.stats.CAUSE_LEAVE;
import settlement.stats.STATS;
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
		
		static void collide(Humanoid a, AIManager ai, ECollision coll) {
			
			boolean hostile = coll.pierceDamage > 0 || (coll.other != null && coll.other instanceof Humanoid && HPoll.Handler.isEnemy(a, (Humanoid)coll.other));
			double mom = coll.momentum*a.physics.getMassI()*EPHYSICS.MOM_TRESHOLDI;
			
//			{
//				LOG.ln(Thread.currentThread().getStackTrace()[3]);
//				LOG.ln(STATS.APPEARANCE().name(a.indu()));
//				LOG.ln("other: " + (coll.other instanceof Humanoid ? STATS.APPEARANCE().name(((Humanoid)coll.other).indu()) : coll.other));
//				LOG.ln("damage: " + coll.pierceDamage + " " + coll.momentum);
//				LOG.ln("dot: " + coll.dirDot);
//				LOG.ln("dotO: " + coll.dirDotOther);
//				LOG.ln(coll.norX + " " + coll.norY);
//				LOG.ln(mom + " " + coll.speedHasChanged);
//				LOG.ln();;
//			}
			
			CAUSE_LEAVE l = coll.leave;
			if (l == null)
				l = coll.other instanceof Animal ? CAUSE_LEAVE.ANIMAL : CAUSE_LEAVE.SLAYED;
			
			if (mom > 4) {
				a.inflictDamage(2, 2, l);
				return;
			}
			
			if (hostile) {
				
				if (coll.other == null && a.division() != null) {
					DivMorale.PROJECTILES.incD(a.division(), 1);
				}
				
				if (mom > RND.rFloat()*2) {
					STATS.NEEDS().EXHASTION.indu().inc(a.indu(), 1);
				}
				
//				double pdamage = coll.pierceDamage*RND.rFloat() - RND.rFloat()*BOOSTABLES.BATTLE().ARMOUR.get(a);
//				pdamage = Math.max(RND.rFloat()*pdamage, 0);
				
				double damage = coll.pierceDamage*RND.rFloat() - BOOSTABLES.BATTLE().ARMOUR.get(a);
				damage = mom*10*Math.max(damage, 0)*RES.config().BATTLE.DAMAGE;
				
				mom = Math.pow(mom, 2 - STATS.NEEDS().EXHASTION.indu().getD(a.indu()));
				double momDamage = Math.max(mom-RND.rFloat(), 0);
				damage = momDamage + damage;
				
				
				if (damage > 0 && !a.inflictDamage(damage, momDamage, l)) {
					return;
				}
				
			}
			
			
			
			event.momentum = mom;
			event.norX = coll.norX;
			event.norY = coll.norY;
			event.speedHasChanged = coll.speedHasChanged;
			
			if (mom >= 1) {
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
			if (other instanceof Animal && (a.indu().randomness() & 0x01FF) == 0)
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
