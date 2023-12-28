package settlement.entity.humanoid.ai.service;

import init.need.NEED;
import init.settings.S;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.service.module.RoomService;
import settlement.room.service.module.RoomService.ROOM_SERVICE_HASER;
import settlement.room.service.module.RoomServiceNeed;
import settlement.room.service.module.RoomServiceNeed.ROOM_SERVICE_NEED_HASER;
import snake2d.LOG;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;

final class MPlans {

	private final AIPLAN[] plans = new AIPLAN[SETT.ROOMS().SERVICE.all().size()];
	
	MPlans(EPlans eplans){
		
		add(new PlanBarber());
		add(new PlanBath());
		add(new PlanCanteen());
		add(new PlanEatery());
		add(new PlanHearth());
		add(new PlanLavatory());
		add(new PlanPhysician());
		add(new PlanTavern());
		add(new PlanWell());
		add(new PlanMarket(eplans));
		add(new PlanSpectator(SETT.ROOMS().SPEAKERS), SETT.ROOMS().SPEAKERS);
		add(new PlanSpectator(SETT.ROOMS().STAGES), SETT.ROOMS().STAGES);
		add(new PlanSpectator(SETT.ROOMS().ARENAS), SETT.ROOMS().ARENAS);
		add(new PlanSpectator(SETT.ROOMS().GARENAS), SETT.ROOMS().GARENAS);
		
		for (RoomServiceNeed n : SETT.ROOMS().SERVICE.needs()) {
			if (plans[n.index()] == null) {
				LOG.err("MISSING AI: " + n.room());
			}
		}
		
	}
	
	private void add(MPlan<?> p) {
		
		add(p, p.services);
	}
	
	private void add(AIPLAN p, LIST<? extends ROOM_SERVICE_HASER> ser) {
		
		for (ROOM_SERVICE_HASER s : ser) {
			if (plans[s.service().index()] != null)
				throw new RuntimeException();
			plans[s.service().index()] = p;
		}
		
	}
	
	public AiPlanActivation get(Humanoid a, AIManager d, RoomService need) {
		return get(a, d, need, need.radius);
	}
	
	public AiPlanActivation get(Humanoid a, AIManager d, RoomService need, int dist) {
		MPlan.dist = dist;
		d.planByte3 = (byte) need.room().typeIndex();
		return plans[need.index()].activate(a, d);
	}
	
	public static abstract class MPlan<T extends ROOM_SERVICE_NEED_HASER> extends AIPLAN.PLANRES{

		private static int dist;
		public final LIST<T> services;
		private final boolean include;
		public MPlan(LIST<T> services, boolean include) {

			this.services = services;
			this.include = include;
		}
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walk.set(a, d);
		}
		
		private final Resumer walk = new Resumer("Walk") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				AISubActivation s = null;
				
				if (include)
					s = AI.SUBS().walkTo.serviceInclude(a, d, blue(d).service(), dist);
				else
					s = AI.SUBS().walkTo.service(a, d, blue(d).service().finder, dist);
				if (s == null)
					return null;
				d.planTile.set(d.path.destX(), d.path.destY());
				blue(d).service().reportDistance(a);
				blue(d).service().reportAccess(a, d.planTile);
				return s;
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return arrive(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};
		
		protected void succeed(Humanoid a, AIManager d) {
			blue(d).service().reportAccess(a, d.planTile);
			blue(d).service().need.stat().fix(a.indu());
		}
		
		protected abstract AISubActivation arrive(Humanoid a, AIManager d);
		
		protected T blue(AIManager d) {
			return services.get(d.planByte3);
		}
		
		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			
			string.add(blue(d).service().verb);
			if (S.get().debug) {
				string.s().add('(');
				super.name(a, d, string);
				string.add(')');
			}
		}

		@Override
		protected void cancel(Humanoid a, AIManager d) {
			//blue(d).service().clearAccess(a);
			super.cancel(a, d);
		}

		protected FSERVICE get(Humanoid a, AIManager d) {
			return blue(d).service().service(d.planTile.x(), d.planTile.y());
		}

		public NEED need(AIManager d) {
			return blue(d).service().need;
		}
		
	}
	
	
}
