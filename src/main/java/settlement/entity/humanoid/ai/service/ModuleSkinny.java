package settlement.entity.humanoid.ai.service;

import static settlement.main.SETT.*;

import init.D;
import init.need.NEEDS;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.room.service.module.RoomServiceNeed;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

class ModuleSkinny extends Module{

	private static CharSequence ¤¤skinnydipping = "¤Skinny dipping";
	
	static {
		D.ts(ModuleSkinny.class);
	}
	
	
	public ModuleSkinny(MPlans plans) {
		super(NEEDS.TYPES().SKINNYDIP, plans);
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		need.stat().fix(a.indu());
		AiPlanActivation p = super.getPlan(a, d);
		
		if (p == null) {
			return skinnydip.activate(a, d);
		}
		
		
		
		return null;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateI) {
		
		
	}
	
	@Override
	public int getPriority(Humanoid a, AIManager d) {

		int pp = need.stat().iPrio(a.indu());
		
		if (pp > 0) {
			for (RoomServiceNeed b : a.race().service().services(a.indu().clas(), need)) {
//				if (b.stats().mulAction() != null && b.stats().mulAction().markIs(a)) {
//					return pp;
//				}
				if (b.accessRequest(a) && b.finder.has(a.tc())) {
					return pp;
				}
			}
			if (SETT.WEATHER().ice.canBatheOutside() && PATH().finders.water.has(a.tc())) {
				return pp;
			}
			

		}
		
		return 0;
		
	}
	
	public final AIPLAN skinnydip = new AIPLAN.PLANRES() {

		private final AISUB sub = new AISUB.Simple("") {

			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {

				d.subByte++;

				if (!a.speed.isZero())
					a.speed.magnitudeInit(0);

				if (d.subByte > 1) {
					need.stat().fix(a.indu());
					STATS.NEEDS().EXPOSURE.count.set(a.indu(), 0);
					STATS.NEEDS().DIRTINESS.set(a.indu(), 0);
				}

				if (d.subByte > 20)
					return null;

				if (RND.oneIn(5)) {

					DIR dir = DIR.ALL.get(RND.rInt(DIR.ALL.size()));

					for (int i = 0; i < 8; i++) {
						int x = a.physics.tileC().x() + dir.x();
						int y = a.physics.tileC().y() + dir.y();
						if (SETT.PATH().coster.player.getCost(a.tc().x(), a.tc().y(), x, y) > 0 && PATH().finders.water.get(x, y) != null) {
							return AI.STATES().WALK2.dirTile(a, d, dir);
						}
						dir = dir.next(1);
					}

				}

				if (RND.oneIn(3))
					return AI.STATES().STAND.aDirRND(a, d, 1 + RND.rFloat(2));
				return AI.STATES().LAY.activate(a, d, 1 + RND.rFloat(5));
			}
		};

		private final Resumer walkToWater = new Resumer(¤¤skinnydipping) {

			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				AISubActivation ss = AI.SUBS().walkTo.serviceInclude(a, d, PATH().finders.water, 100);
				if (ss != null) {
					STATS.ENV().SKINNY_DIP.indu().set(a.indu(), 1);
					return ss;
				}
				STATS.ENV().SKINNY_DIP.indu().set(a.indu(), 0);
				return null;
			}

			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return bathe.set(a, d);
			}

			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}

			@Override
			public void can(Humanoid a, AIManager d) {

			}
		};

		private final Resumer bathe = new Resumer(¤¤skinnydipping) {

			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				STATS.POP().NAKED.set(a.indu(), 1);
				STATS.NEEDS().EXPOSURE.count.set(a.indu(), 0);
				d.planByte1 = (byte) (5 + RND.rInt(10));
				return sub.activate(a, d);
			}

			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				need.stat().fix(a.indu());
				STATS.NEEDS().EXPOSURE.count.set(a.indu(), 0);
				STATS.NEEDS().DIRTINESS.set(a.indu(), 0);
				if (d.planByte1-- > 0 &&AIModules.current(d) != null && AIModules.current(d).moduleCanContinue(a, d) && SETT.WEATHER().ice.canBatheOutside()) {
					return sub.activate(a, d);
				}
				can(a, d);
				return null;
			}

			@Override
			public boolean con(Humanoid a, AIManager d) {
				FINDABLE s = PATH().finders.water.getReserved(d.path.destX(), d.path.destY());
				return s != null && s.findableReservedIs();
			}

			@Override
			public void can(Humanoid a, AIManager d) {
				FINDABLE s = PATH().finders.water.getReserved(d.path.destX(), d.path.destY());
				if (s != null)
					s.findableReserveCancel();
				STATS.POP().NAKED.set(a.indu(), 0);
			}
		};

		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walkToWater.set(a, d);
		}
	};

}
