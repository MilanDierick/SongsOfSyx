package settlement.entity.humanoid.ai.service;

import init.need.NEEDS;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.room.main.Room;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import settlement.stats.STATS;
import snake2d.util.datatypes.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;

class ModuleTemple extends ModuleSimple<ROOM_TEMPLE>{

	public ModuleTemple() {
		super(NEEDS.TYPES().TEMPLE);
	}
	

	@Override
	public LIST<ROOM_TEMPLE> services(Humanoid a, AIManager d) {
		return SETT.ROOMS().TEMPLES.temples(STATS.RELIGION().getter.get(a.indu()).religion);
	}

	@Override
	public AiPlanActivation plan(Humanoid a, AIManager d, ROOM_TEMPLE t) {
		return plan.activate(a, d);
	}

	@Override
	protected void clear(Humanoid a, AIManager d, ROOM_TEMPLE t) {
		STATS.RELIGION().clearAccess(a);
	}
	

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		// TODO Auto-generated method stub
		
	}

	
	private final AIPLAN plan = new AIPLAN.PLANRES(){

		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walk.set(a, d);
		}
		
		private final Resumer walk = new Resumer(null) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte4 = 0;
				for (ROOM_TEMPLE t : services(a, d)) {
					AISubActivation s = AI.SUBS().walkTo.serviceInclude(a, d, t.service().finder, 750);
					if (s != null) {
						d.planByte4 = (byte) t.typeIndex();
						return s;
					}
					
				}
				return null;
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				STATS.RELIGION().setAccess(a);
				NEEDS.TYPES().TEMPLE.stat().fix(a.indu());
				return pray.set(a, d);
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
				string.add(temp(d).service().verb);
			};
		};
		
		private final Resumer pray = new Resumer(null) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planTile.set(d.path.destX(), d.path.destY());
				d.planByte1 = (byte) (5 + RND.rInt(10));
				
				FurnisherItem it = SETT.ROOMS().fData.item.get(a.tc());
				if (it != null) {
					COORDINATE c = SETT.ROOMS().fData.itemX1Y1(a.tc(), Coo.TMP);
					if (c != null) {
						
						int dx = c.x()+it.width()/2;
						int dy = c.y()+it.height()/2;
						
						DIR dir = DIR.get(a.tc().x(), a.tc().y(), dx, dy);
						a.speed.setDirCurrent(dir);
					}
				}

				return res(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				d.planByte1 --;
				if (d.planByte1 <= 0) { 
					FINDABLE s = temp(d).service().finder.getReserved(d.planTile.x(), d.planTile.y());
					if (s != null)
						s.findableReserveCancel();
					return next.set(a, d);
				}
				if (RND.rBoolean()) {
					return AI.SUBS().single.activate(a, d, AI.STATES().anima.lay, 4 + RND.rInt(4));
				}else {
					if (RND.rBoolean())
						return AI.SUBS().single.activate(a, d, AI.STATES().anima.carry, 4 + RND.rInt(4));
					return AI.SUBS().single.activate(a, d, AI.STATES().anima.stand, 4 + RND.rInt(4));
				}
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				FINDABLE s = temp(d).service().finder.getReserved(d.planTile.x(), d.planTile.y());
				if (s != null)
					s.findableReserveCancel();
			}
			
			@Override
			protected void name(Humanoid a, AIManager d, Str string) {
				string.add(temp(d).service().verb);
			};
		};
		
		private final Resumer next = new Resumer(null) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				Room r = SETT.ROOMS().map.get(a.tc());
				d.planByte1 = (byte) (5 + RND.rInt(10));
				if (r != null)
					return AI.SUBS().walkTo.room(a, d, a.tc().x(), a.tc().y());
				return null;
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				d.planByte1 --;
				if (d.planByte1 <= 0) {
					return null;
				}
				if (RND.rBoolean()) {
					return AI.SUBS().single.activate(a, d, AI.STATES().anima.lay, 4 + RND.rInt(4));
				}else {
					if (RND.rBoolean())
						return AI.SUBS().single.activate(a, d, AI.STATES().anima.carry, 4 + RND.rInt(4));
					return AI.SUBS().single.activate(a, d, AI.STATES().anima.stand, 4 + RND.rInt(4));
				}
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
				string.add(temp(d).service().verb);
			};
		};
		
		private ROOM_TEMPLE temp(AIManager d) {
			return SETT.ROOMS().TEMPLES.ALL.get(d.planByte4);
		}

		
	};


}
