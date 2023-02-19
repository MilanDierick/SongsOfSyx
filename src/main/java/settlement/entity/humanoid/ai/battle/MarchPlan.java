package settlement.entity.humanoid.ai.battle;

import init.C;
import init.D;
import init.resources.RESOURCE;
import settlement.army.Div;
import settlement.entity.humanoid.*;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISTATES.STOP;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.spirte.HSprites;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE;
import settlement.stats.StatsEquippables.StatEquippableRange;
import settlement.thing.projectiles.SProjectiles;
import settlement.thing.projectiles.Trajectory;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

final class MarchPlan extends AIPLAN.PLANRES{

	private static CharSequence ¤¤Reforming = "¤Reforming";
	private static CharSequence ¤¤Waiting = "¤Waiting for orders";
	private static CharSequence ¤¤Breaking = "¤Breaking Formation";
	private static CharSequence ¤¤Firing = "¤Firing";
	private final int cutDistance = 32;
	
	static {
		D.ts(MarchPlan.class);
	}

	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		
		
		if (a.indu().player() && d.plan() != this) {
			AISubActivation s = fetchGear.set(a, d);
			if (s != null)
				return s;
		}
		
		return retry(a, d);
		
	}
	
	private AISubActivation retry(Humanoid a, AIManager d) {

		Div div = a.division();
		if (div == null || div.deployed() == 0 || !div.settings.mustering())
			return null;
		
		div.reporter.reportReachable(a.divSpot(), true);
		
		if (!AI.modules().battle.moduleCanContinue(a, d)) {
			return null;
		}
		
		AISubActivation s = retry2(a, d);
		
		if (s == null) {
			div.reporter.reportReachable(a.divSpot(), false);
		}
		return s;
	}
	
	private AISubActivation retry2(Humanoid a, AIManager d) {

		Div div = a.division();
		COORDINATE c = a.physics.tileC();
		COORDINATE de = div.reporter.getPixel(a.divSpot());
		
		if (de == null) {
			return pathToDestination.set(a, d);
		}
		
		if (isInPosition(de, a, d)) {
			return beBraced.set(a, d);
		}
		
		de = div.reporter.getTile(a.divSpot());
		
		if (COORDINATE.tileDistance(c, de) < cutDistance) {
			return cutToPosition.set(a, d);
		}
		
		if (c != null && SETT.PATH().isInTheNeighbourhood(de.x(), de.y(), a.physics.tileC().x(), a.physics.tileC().y())) {
			return pathToPosition.set(a, d);
		}
		
		return pathToDestination.set(a, d);
	}
	
	private boolean conn(Humanoid a, AIManager d) {
		Div div = a.division();
		return div != null && div.settings.mustering() && div.deployed() > 0;
	}
	
	private boolean isInPosition(COORDINATE dest, Humanoid a, AIManager d) {
		return dest.isSameAs(a.physics.body().cX(), a.physics.body().cY());
	}
	
	private final Resumer cutToPosition = new Resumer(¤¤Reforming) {
		
		private final AISUB sub = new MarchSubCutTo();
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			if (a.indu().hType().player && a.division() != null) {
				DIR dir = DIR.get(a.tc(), a.division().reporter.getTile(a.divSpot()));
				if (dir.x() != 0 || dir.y() != 0) {
					if (SETT.PATH().coster.player.getCost(a.tc().x(), a.tc().y(), a.tc().x()+dir.x(), a.tc().y()+dir.y()) < 0)
						return pathToDestination.set(a, d);
				}
			}
			
			
			return sub.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			Div div = a.division();
			COORDINATE dest = div.reporter.getPixel(a.divSpot());
			if (dest == null)
				return pathToDestination.set(a, d);
			if (Util.isInPosition(dest, a, d)) {
				return arriveInFormation.set(a, d);
			}
			return sub.activate(a, d);
		}
		
		@Override
		public AISubActivation resFailed(Humanoid a, AIManager d, HEvent event) {
			if (event == HEvent.COLLISION_TILE) {
				return pathToPosition.set(a, d);
			}
			return null;
		};
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return conn(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
		
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.pollReady(a, d, e);
		}
	};
	
	private final Resumer pathToDestination = new Resumer(¤¤Reforming) {
		
		private final AISUB sub = new AISUB.Simple() {
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte++;
				if (d.subByte == 1) {
					Div div = a.division();
					if (div.settings.running)
						return AI.STATES().RUN2.path(a, d);
					else
						return AI.STATES().WALK2.path(a, d);
				}

				return null;
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				return InterBattle.listener.event(a, d, e);
			}
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				return InterBattle.listener.poll(a, d, e);
			}

		};
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			Div div = a.division();
			if (div.deployed() == 0)
				return waitForSpot.set(a, d);
			COORDINATE c = div.reporter.getDestTile();
			int sx = c.x();
			int sy = c.y();
			COORDINATE dest = SETT.PATH().finders.arround.find(sx, sy, 0, 15);
			if (dest == null)
				return null;
			d.planByte1 = 0;
			d.path.request(a.physics.tileC(), dest.x(), dest.y());
			if (!d.path.isSuccessful())
				return null;
			
			return res(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (!d.path.isSuccessful())
				return null;
			if (d.path.isDest()) {
				return waitInDestination.set(a, d);
			}
			d.path.setNext();
			Div div = a.division();
			COORDINATE c = div.reporter.getDestTile();
			int tx = c.x();
			int ty = c.y();
			if (COORDINATE.tileDistance(tx, ty, d.path.destX(), d.path.destY()) > 15) {
				return retry(a, d);
			}
			d.planByte1 ++;
			if (d.planByte1 == 5) {
				c = div.reporter.getTile(a.divSpot());
				if (c != null && SETT.PATH().isInTheNeighbourhood(c.x(), c.y(), a.physics.tileC().x(), a.physics.tileC().y())) {
					
					a.speed.magnitudeInit(0);
					return pathToPosition.set(a, d);
				}
				d.planByte1 = 0;
			}
			
			return sub.activate(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return conn(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
		
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.listener.poll(a, d, e);
		}
	};
	
	private final Resumer pathToPosition = new Resumer(¤¤Reforming) {
		
		private final AISUB sub = new AISUB.Simple() {
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte++;
				if (d.subByte == 1)
					return AI.STATES().RUN2.path(a, d);
				return null;
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				return InterBattle.listener.event(a, d, e);
			}
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				return InterBattle.listener.poll(a, d, e);
			}
		};
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			Div div = a.division();
			COORDINATE c = div.reporter.getTile(a.divSpot());
			if (c == null)
				return pathToDestination.set(a, d);
			d.planByte1 = div.state();
			d.planByte2 = 0;
			d.path.request(a.physics.tileC(), c.x(), c.y());
			if (!d.path.isSuccessful())
				return waitForSpot.set(a, d);
			return sub.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (!d.path.isSuccessful())
				return null;
			Div div = a.division();
			if (d.path.isDest()) {
				//fix here
				a.speed.magnitudeInit(0);
				if (d.planByte2 == 5 && d.planByte1 != div.state()) {
					a.speed.magnitudeInit(0);
					return set(a, d);
				}
				COORDINATE c = div.reporter.getTile(a.divSpot());
				if (c == null)
					return waitForSpot.set(a, d);
				if (div.reporter.getPixel(a.divSpot()) != null)
					return cutToPosition.set(a, d);
			}
			d.path.setNext();
			d.planByte2++;
			
			if (d.planByte2 == 5 && d.planByte1 != div.state()) {
				d.planByte2 = 0;
				a.speed.magnitudeInit(0);
				return set(a, d);
			}
			
			return sub.activate(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return conn(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
		
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.listener.poll(a, d, e);
		}
	};
	
	private final Resumer arriveInFormation = new Resumer(¤¤Reforming) {
		
		private final AISUB sub = new AISUB.Simple() {
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte == 1)
					return AI.STATES().STAND.activate(a, d, 0.1);
				return null;
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				return InterBattle.listener.event(a, d, e);
			}
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				return InterBattle.listener.poll(a, d, e);
			}
		};
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = (byte) (5+RND.rInt(5));
			return sub.activate(a, d);
		}

		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			Div div = a.division();
			COORDINATE dest = div.reporter.getPixel(a.divSpot());
			if (dest == null)
				return null;
			if (!isInPosition(dest, a, d)) {
				return retry(a, d);
			}
			
			if (d.planByte1 > 0) {
				d.planByte1--;
			}else if (d.planByte1 == 0) {
				return beBraced.set(a, d);
			}
			return sub.activate(a, d);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return conn(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
		
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.listener.poll(a, d, e);
		}
	};
	

	
	private final Resumer beBraced = new Resumer(¤¤Waiting) {
		
		private final STOP stand = new AISTATES.STOP(HSprites.SWORD_STAND_SWAY);
		

		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			d.subByte = 0;
			return AI.SUBS().single.activate(a, d, stand.activate(a, d, 0.5));
		}

		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			
			if (!conn(a, d)) {
				return null;
			}
			
			byte ss = d.subByte++;
			AISubActivation s = tryMopup(a, d);
			if (s != null)
				return s;
			
			if (!a.physics.isWithinTile()) {
				if (AI.modules().battle.tile.shouldattackTile(d, a, a.physics.tx1(),a.physics.ty1())) {
					return d.resumeOtherPlan(a, AI.modules().battle.tile.init(d, a, a.physics.tx1(),a.physics.ty1()));
				}
				if (AI.modules().battle.tile.shouldattackTile(d, a, a.physics.tx2(),a.physics.ty1())) {
					return d.resumeOtherPlan(a, AI.modules().battle.tile.init(d, a, a.physics.tx2(),a.physics.ty1()));
				}
				if (AI.modules().battle.tile.shouldattackTile(d, a, a.physics.tx1(),a.physics.ty2())) {
					return d.resumeOtherPlan(a, AI.modules().battle.tile.init(d, a, a.physics.tx1(),a.physics.ty2()));
				}
				if (AI.modules().battle.tile.shouldattackTile(d, a, a.physics.tx2(),a.physics.ty2())) {
					return d.resumeOtherPlan(a, AI.modules().battle.tile.init(d, a, a.physics.tx2(),a.physics.ty2()));
				}
			}
			
			if (shouldFire(a, d))
				return fire.set(a, d);
			
			d.subByte = ss;
			
			if (d.subByte < 50) {
				
				if (!con(a, d)) {
					return retry(a, d);
				}
				Div div = a.division();

				COORDINATE dest = div.reporter.getPixel(a.divSpot());
				if (dest == null || !isInPosition(dest, a, d)) {
					return retry(a, d);
				}
				
				DIR dir = div.position().dir(a.divSpot());
				if (dir == null || !div.settings.threatAt(dir)) {
					dir = a.division().dir();
				}
				
				if (RND.oneIn(30)) {
					a.speed.turn2(dir.next(RND.rInt0(1)));
					return AI.SUBS().single.activate(a, d, stand.activate(a, d, 1.0 + RND.rFloat(0.5)));
				}else {
					a.speed.turn2(dir);
				}
				
				
				return AI.SUBS().single.activate(a, d, stand.activate(a, d, 0.5));
			}
			return retry(a, d);
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
			return InterBattle.listener.event(a, d, e);
		}

		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (e.type == HPoll.DEFENCE)
				return super.poll(a, d, e)*1.5;
			return InterBattle.pollReady(a, d, e);
		}
	};
	
	private boolean shouldFire(Humanoid a, AIManager d) {
		return conn(a, d) && a.division().settings.shouldFire() && a.division().trajectory.get(a.divSpot()) != null && a.division().settings.ammo().ammunition.indu().get(a.indu()) > 0;
	}
	
	private final Resumer fire = new Resumer(¤¤Firing) {
		
		private final AISUB fire = new AISUB.Simple() {
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				if (!shouldFire(a, d))
					return null;
				Trajectory j = a.division().trajectory.get(a.divSpot());
				a.speed.setDirCurrent(DIR.get(j.vx(), j.vy()));
				
				if (drawInter(a, d) >= 0.75) {
					int x = a.body().cX()+a.speed.dir().x()*C.TILE_SIZEH;
					int y = a.body().cY()+a.speed.dir().y()*C.TILE_SIZEH;
					int h = SProjectiles.releaseHeight(a.tc().x(), a.tc().y());
					StatEquippableRange rr = a.division().settings.ammo();
					double ran = rr.accuracyRND(a.indu());
				
					SETT.PROJS().launch(x, y, 
							h, j, a.division().settings.ammo().projectile, ran, rr.stat().indu().get(a.indu()));
					if (STATS.NEEDS().EXHASTION.indu().getD(a.indu()) > 0.25)
						STATS.NEEDS().EXHASTION.indu().inc(a.indu(), -1);
					a.division().settings.ammo().use(a.indu());
					return null;
				}
				return AI.STATES().anima.archer1.activate(a, d, 0.1+RND.rFloat()*0.1);
			}
		};
		
		private final AISUB aim = new AISUB.Simple() {
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				
				if (d.subByte == 0) {
					d.subByte = 1;
					Trajectory j = a.division().trajectory.get(a.divSpot());
					if (j != null)
						a.speed.setDirCurrent(DIR.get(j.vx(), j.vy()));
					return AI.STATES().anima.archer2.activate(a, d, 0.1+RND.rFloat()*0.1);
					
				}
				return null;
			}
		};
		

		
		private double drawInter(Humanoid a, AIManager d) {
			return a.division().settings.ammo().drawInter(a.division());
		}

		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			return res(a, d);
		}

		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			
			if (!shouldFire(a, d))
				return retry(a, d);
			
			AISubActivation s = tryMopup(a, d);
			if (s != null)
				return s;
			
			Div div = a.division();
			COORDINATE dest = div.reporter.getPixel(a.divSpot());
			if (dest == null || !isInPosition(dest, a, d)) {
				return retry(a, d);
			}
			
			if (drawInter(a, d) < 0.25) {
				return fire.activate(a, d);
			}else
				return aim.activate(a, d);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return conn(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.listener.poll(a, d, e);
		}
	};
	
	private final Resumer waitForSpot = new Resumer(¤¤Waiting) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().STAND.activateTime(a, d, 1);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			AISubActivation s = tryMopup(a, d);
			if (s != null)
				return s;
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
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.listener.poll(a, d, e);
		}
	};
	
	private final Resumer waitInDestination = new Resumer(¤¤Waiting) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (RND.oneIn(5))
				a.speed.turnRandom();
			return AI.SUBS().STAND.activateTime(a, d, 1);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {

			AISubActivation s = tryMopup(a, d);
			if (s != null)
				return s;
			
			Div div = a.division();
			COORDINATE c = div.reporter.getDestTile();
			int tx = c.x();
			int ty = c.y();
			if (COORDINATE.tileDistance(tx, ty, d.path.destX(), d.path.destY()) > 15) {
				return null;
			}
			
			if (RND.oneIn(5)) {
				c = div.reporter.getTile(a.divSpot());
				if (c != null && SETT.PATH().isInTheNeighbourhood(c.x(), c.y(), a.physics.tileC().x(), a.physics.tileC().y())) {
					return pathToPosition.set(a, d);
				}
			}
			
			return setAction(a, d);
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.listener.poll(a, d, e);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return conn(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	

	
	private final Resumer fetchGear = new Resumer("Getting Battlegear") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			long bit = 0;
			Div div = STATS.BATTLE().DIV.get(a);
			if (div == null)
				return null;
			
			for (EQUIPPABLE e : STATS.EQUIP().military_all()) {
				if (e.stat().indu().get(a.indu()) < e.target(a)) {
					bit |= e.resource().bit;
				}
			}
			
			if (bit == 0)
				return null;
			
			return AI.SUBS().walkTo.resource(a, d, bit, Integer.MAX_VALUE);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			RESOURCE r = d.resourceCarried();
			for (EQUIPPABLE e : STATS.EQUIP().military_all()) {
				if (e.stat().indu().get(a.indu()) < e.target(a) && e.resource() == r) {
					e.inc(a.indu(), 1);
					d.resourceCarriedSet(null);
					break;
				}
			}
			AISubActivation s = set(a, d);
			return s;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return STATS.BATTLE().DIV.get(a) != null;
		}
		
		@Override
		public 
		void can(Humanoid a, AIManager d) {
			
		}
	};
	
	private AISubActivation tryMopup(Humanoid a, AIManager d) {
		
		if (!a.division().settings.moppingUp())
			return null;
		if (STATS.POP().pop(HTYPE.ENEMY) == 0 && STATS.POP().pop(HTYPE.RIOTER) == 0)
			return null;
		
		Div div = STATS.BATTLE().DIV.get(a);
		Humanoid h = div.targets.getNextTarget();
		if (h != null) {
			AISubActivation s = AI.SUBS().walkTo.follow(a, d, h, true, (byte)10);
			
			h.target(2);
			if (s != null) {
				mopup.set(a, d);
				return s;
			}
		}
		return null;
	}
	
	private final Resumer mopup = new Resumer(¤¤Breaking) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return tryMopup(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			Div div = a.division();
			return div != null && div.settings.mustering() && div.settings.moppingUp();
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_SOFT)
				return super.event(a, d, e);
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.listener.poll(a, d, e);
		}
	};
	
}
