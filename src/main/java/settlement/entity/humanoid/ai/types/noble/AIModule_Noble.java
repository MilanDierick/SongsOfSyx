package settlement.entity.humanoid.ai.types.noble;

import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;

public final class AIModule_Noble extends AIModule{
	
	private final AIData.AIDataBit sus = AI.bit();
	
	public AIModule_Noble(){
	
	}

	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {
		return beNoble.activate(a, d);
	}
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		if (newDay)
			sus.set(d, false);
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		if (STATS.WORK().WORK_TIME.indu().isMax(a.indu()))
			return 0;
		return 3;
	}
	
	private final AIPLAN beNoble = new AIPLAN.PLANRES() {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			if (!sus.is(d) && a.office().rooms().size() > 0) {
				int bI = RND.rInt(a.office().rooms().size());
				for (int i = 0; i < a.office().rooms().size(); i++) {
					RoomBlueprintIns<?> b = a.office().rooms().get((bI+i)%a.office().rooms().size());
					if (b.instancesSize() > 0) {
						int ii = RND.rInt(b.instancesSize());
						for (int k = 0; k < b.instancesSize(); k++) {
							RoomInstance ins = b.getInstance(((ii+k)%b.instancesSize()));
							if (ins.employees().employed() > 0) {
								AISubActivation s = AI.SUBS().walkTo.room(a, d, ins);
								if (s != null) {
									inspectRoom.set(a, d);
									return s;
								}
								
							}
						}
						
					}
					
				}
				sus.set(d, true);
			}
			
			
			
			
			return other.set(a, d);
		}
		
		private final Resumer other = new Resumer("Beeing Noble") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				inspect.set(a, d);
				if (SETT.PATH().finders.randomDistanceAway.find(a.tc().x(), a.tc().y(), d.path, 100)) {
					return AI.SUBS().walkTo.path(a, d);
				}
//				if (SETT.PATH().finders.getOutofWay.find(a.physics.tileC(), d.path, a)) {
//					return AI.SUBS().walkTo.path(a, d);
//				}
				
				return AI.SUBS().STAND.activate(a, d);
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
				// TODO Auto-generated method stub
				
			}
			
		};
		

		private final Resumer inspect = new Resumer("Inspecting") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = 8;
				return null;
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				d.planByte1--;
				if (d.planByte1 <= 0)
					return null;
				return AI.SUBS().STAND.activateRndDir(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};

		private final Resumer inspectRoom = new Resumer("Inspecting Room") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = 16;
				return null;
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				d.planByte1--;
				if (d.planByte1 <= 0)
					return null;
				if (RND.oneIn(4)) {
					RoomInstance r = SETT.ROOMS().map.instance.get(a.tc());
					if (r != null) {
						return AI.SUBS().walkTo.room(a, d, r);
					}
				}
				return AI.SUBS().STAND.activateRndDir(a, d);
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
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (e.type == HPoll.WORKING) {
				return 1.0;
			}
			return super.poll(a, d, e);
		}
		
	};

}
