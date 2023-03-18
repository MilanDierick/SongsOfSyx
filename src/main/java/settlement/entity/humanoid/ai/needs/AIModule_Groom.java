package settlement.entity.humanoid.ai.needs;

import init.resources.RES_AMOUNT;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.misc.util.FSERVICE;
import settlement.room.service.barber.ROOM_BARBER;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;

public final class AIModule_Groom extends AIModule{

	public AIModule_Groom() {

		
	}
	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		for (ROOM_SERVICE_ACCESS_HASER s : a.race().service().BARBER.get(a.indu().clas().index())) {
			s.service().clearAccess(a);
		}
		
		for (ROOM_BARBER s : a.race().service().BARBER.get(a.indu().clas().index())) {
			if (s.service().accessRequest(a)) {
				d.planByte1 = (byte) s.typeIndex();
				AiPlanActivation p = plan.activate(a, d);
				if (p != null)
					return p;
			}
		}
		
		STATS.NEEDS().GROOMING.fixMax(a.indu());

		return null;
		
	}
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		
		double p = STATS.NEEDS().GROOMING.getPrio(a.indu()); 
		return (int) Math.ceil(p*8);
		
	}
	
	private final AIPLAN plan = new AIPLAN.PLANRES() {
		
		
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walk.set(a, d);
		}
		
		final Resumer walk = new Resumer() {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				AISubActivation s = AI.SUBS().walkTo.serviceInclude(a, d, b(a, d).service());
				if (s == null)
					s = null;
				d.planTile.set(d.path.destX(), d.path.destY());
				b(a, d).service().reportDistance(a);
				b(a, d).service().reportAccess(a, d.planTile);
				
				return s;
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return use.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				FINDABLE s = get(a, d);
				return s != null && s.findableReservedIs();
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};
		
		final Resumer use = new Resumer("") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte2 = (byte) (5 + RND.rInt(15));
				get(a, d).startUsing();
				STATS.NEEDS().GROOMING.fix(a.indu());
				return res(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (d.planByte2-- < 0) {
					get(a, d).consume();
					for (int di = 0; di < DIR.ORTHO.size(); di++) {
						if (!SETT.PATH().solidity.is(d.planTile, DIR.ORTHO.get(di))) {
							for (RES_AMOUNT ra : a.race().resourcesGroom()) {
								SETT.THINGS().resources.create(d.planTile.x(), d.planTile.y(), ra.resource(), ra.amount());
							}
						}
					}
					return null;
				}
				
				DIR dir = b(a,d).dir(d.planTile.x(), d.planTile.y());
				
				if (b(a, d).service().usageSound != null && RND.oneIn(5))
					b(a, d).service().usageSound.rnd(a.physics.body(), 0.5);
				
				if (RND.rBoolean()) {
					a.speed.setDirCurrent(dir);
					if (RND.rBoolean()) {
						return AI.SUBS().STAND.activateRndDir(a, d, 6);
					}else {
						return AI.SUBS().STAND.activate(a, d, AI.STATES().anima.box.activate(a, d, 3));
					}
				}else {
					dir = dir.next((int) RND.rSign());
					return AI.SUBS().STAND.activateRndDir(a, d, 6);
				}
				
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				FINDABLE s = get(a, d);
				return s != null && s.findableReservedIs();
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				FINDABLE s = get(a, d);
				if (s != null)
					s.findableReserveCancel();
			}
		};
		
		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			string.add(b(a, d).service().verb);
		};
		
		private FSERVICE get(Humanoid a, AIManager d) {
			return b(a, d).service().finder.get(d.planTile.x(), d.planTile.y());
		}
		
		private ROOM_BARBER b(Humanoid a, AIManager d) {
			return SETT.ROOMS().BARBERS.get(d.planByte1);
		}
		
		
	};
	


}
