package settlement.entity.humanoid.ai.types.prisoner;

import static settlement.main.SETT.*;

import game.time.TIME;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

class Prison extends AIPLAN.PLANRES{

	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		if (setCell(a, d)) {
			if (PrisonerData.self.punishment.get(d) == LAW.process().prison && PrisonerData.self.reportedPunish.get(d) == 0) {
				LAW.process().prison.inc(a.race());
				PrisonerData.self.reportedPunish.set(d, 1);
				
			}
			STATS.NEEDS().EXPOSURE.count.set(a.indu(), 0);
			return init.set(a, d);
			
		}
		return null;
	}

	private boolean setCell(Humanoid a, AIManager d) {
		
		if (SETT.ROOMS().PRISON.isreserved(AI.modules().coo(d))) {
			if (SETT.PATH().comps.superComp.get(a.tc()) != SETT.PATH().comps.superComp.get(AI.modules().coo(d))){
				return false;
			}
			
			return true;
		}
		
		AI.modules().coo(d).set(-1, -1);
		
		COORDINATE c = SETT.ROOMS().PRISON.registerPrisoner();
		if (c == null)
			return false;
		AI.modules().coo(d).set(c);
		
		if (SETT.PATH().comps.superComp.get(a.tc()) != SETT.PATH().comps.superComp.get(AI.modules().coo(d))){
			return false;
		}
		return true;
	}
	
	private final Resumer init = new Resumer(LAW.process().prison.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = 8;
			return AI.SUBS().STAND.activateRndDir(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			if (PrisonerData.self.prisonTimeLeft.get(d) == 0) {
				return unfuck(freed, a, d);
			}
			
			
			if (d.planByte1 <= 0) {
				if (!SETT.PATH().connectivity.is(a.tc())) {
					return unfuck.set(a, d);
				}
				return null;
			}
			d.planByte1 --;
			
			if (!SETT.ROOMS().PRISON.isWithinCell(a.tc().x(), a.tc().y(), AI.modules().coo(d))) {
				
				return walkToDoor.set(a, d);
			}
			
			
			
			if (STATS.NEEDS().CONSTIPATION.getPrio(a.indu()) > 0) {
				AISubActivation s = unfuck(poop, a, d);
				if (s != null)
					return s;
			}
			
			if (STATS.NEEDS().HUNGER.getPrio(a.indu()) > 0) {
				AISubActivation s = unfuck(eat, a, d);
				if (s != null)
					return s;
			}
			
			if (TIME.light().nightIs()) {
				
				AISubActivation s = sleep.set(a, d);
				if (s != null)
					return s;
			}
			
			//hunger, popo
			if (RND.oneIn(5)) {
				AISubActivation s = changeSpot.set(a, d);
				if (s != null)
					return s;
			}
			return AI.SUBS().STAND.activateRndDir(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return SETT.ROOMS().PRISON.isreserved(AI.modules().coo(d));
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			
		}
		
		private AISubActivation unfuck(Resumer res, Humanoid a, AIManager d) {
			if (!SETT.PATH().connectivity.is(a.tc())) {
				return unfuck.set(a, d);
			}
			return res.set(a, d);
		}
	};
	
	private final Resumer walkToDoor = new Resumer(LAW.process().prison.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().walkTo.cooFull(a, d, AI.modules().coo(d));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return SETT.ROOMS().PRISON.isreserved(AI.modules().coo(d));
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			
		}
	};
	

	

	
	private final Resumer unfuck = new Resumer(LAW.process().prison.verb) {
		
		final AISUB untrapp = new AISUB.Simple("trapped") {

			@Override
			public AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte != 1)
					return null;
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR dir = DIR.ALL.get(di);
					if (PATH().connectivity.is(a.tc(), dir) 
							&& SETT.ROOMS().PRISON.isWithinCell(a.tc().x()+dir.x(), a.tc().y()+dir.y(), AI.modules().coo(d)) ) 
					{
						return AI.STATES().WALK2.dirTile(a, d, dir);
					}
				}
				return AI.STATES().STAND.activate(a, d, 1);
			}
			
		};
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return untrapp.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return SETT.ROOMS().PRISON.isreserved(AI.modules().coo(d));
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			
		}
	};
	
	private final Resumer sleep = new Resumer(LAW.process().prison.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().subSleep.activate(a, d);
			
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return SETT.ROOMS().PRISON.isreserved(AI.modules().coo(d));
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			
		}
	};
	
	private final Resumer poop = new Resumer(LAW.process().prison.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().STAND.activateRndDir(a, d);
			
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			FSERVICE c = SETT.ROOMS().PRISON.getLatrine(AI.modules().coo(d));
			if (c == null || !c.findableReservedCanBe())
				return null;
			c.findableReserve();
			c.consume();
			STATS.NEEDS().CONSTIPATION.fix(a.indu());
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return SETT.ROOMS().PRISON.isreserved(AI.modules().coo(d));
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			
		}
	};
	
	private final Resumer eat = new Resumer(LAW.process().prison.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			FSERVICE c = SETT.ROOMS().PRISON.getFood(AI.modules().coo(d));
			if (c == null || !c.findableReservedCanBe())
				return null;
			c.findableReserve();
			return AI.SUBS().walkTo.coo(a, d, c);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			FSERVICE c = SETT.ROOMS().PRISON.getFood(AI.modules().coo(d));
			if (c != null)
				c.consume();
			return eat2.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return SETT.ROOMS().PRISON.isreserved(AI.modules().coo(d));
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			FSERVICE c = SETT.ROOMS().PRISON.getFood(AI.modules().coo(d));
			if (c != null)
				c.consume();

		}
	};
	
	private final Resumer eat2 = new Resumer(LAW.process().prison.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.grab, 3);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			STATS.FOOD().eat(a, 0, 0);
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return SETT.ROOMS().PRISON.isreserved(AI.modules().coo(d));
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			
		}
	};
	
	private final Resumer changeSpot = new Resumer(LAW.process().prison.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			DIR dir = DIR.ORTHO.rnd();
			int dx = a.tc().x() + dir.x();
			int dy = a.tc().y() + dir.y();
			if (SETT.ROOMS().PRISON.isWithinCell(dx, dy, AI.modules().coo(d))) {
				if (!SETT.ENTITIES().hasAtTile(dx, dy))
					return AI.SUBS().walkTo.cooFull(a, d, dx, dy);
			}
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return SETT.ROOMS().PRISON.isreserved(AI.modules().coo(d));
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			
		}
	};
	
	private final Resumer freed = ResFree.make(this);
	
//	@Override
//	public boolean event(Humanoid a, AIManager d, HEventData e) {
//		if (e.event == HEvent.COLLISION_UNREACHABLE) {
//			return false;
//		}
//		return super.event(a, d, e);
//	}
//	
	

	@Override
	protected boolean shouldContinue(Humanoid a, AIManager d) {
		return super.shouldContinue(a, d);
	}
	
	@Override
	protected AISubActivation resumeFailed(Humanoid a, AIManager d, HEvent event) {
		if (shouldContinue(a, d))
			return init.set(a, d);
		return null;
	}

}
