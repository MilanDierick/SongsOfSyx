package settlement.entity.humanoid.ai.work;
import static settlement.main.SETT.*;

import game.GAME;
import init.C;
import init.D;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.ENTITY;
import settlement.entity.animal.Animal;
import settlement.entity.humanoid.*;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.room.food.hunter.HunterInstance;
import settlement.room.food.hunter.ROOM_HUNTER;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.stats.STATS;
import settlement.thing.THINGS.Thing;
import settlement.thing.ThingsCadavers.Cadaver;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;

final class WorkHunter extends PlanBlueprint {

	private final ROOM_HUNTER b;
	
	protected WorkHunter(AIModule_Work module, PlanBlueprint[] map) {
		
		super(module, ROOMS().HUNTER, map);
		b = SETT.ROOMS().HUNTER;
	}
	
	@Override
	public AISubActivation init(Humanoid a, AIManager d) {
		HunterInstance in = (HunterInstance) work(a);
		
		SETT_JOB j = in.getWork();
		
		
		if (j == null) {
			GAME.Notify("Weird " + in.mX() + " " + in.mY());
			return null;
		}
		
		d.planTile.set(j.jobCoo());
		
		for (Thing t : SETT.THINGS().get(j.jobCoo().x(), j.jobCoo().y())) {
			if (t instanceof Cadaver) {
				Cadaver c = (Cadaver) t;
				if (c.resHas()) {
					d.planObject = c.index();
					in.getWork(d.planTile).jobReserve(null);
					
					return butcher_walk.set(a, d);
				}
			}
		}
		
		if (!in.searching())
			return null;
		
		Animal prey = SETT.PATH().finders.prey.findAndReserve(a.physics.tileC(), d.path, in.radius());
		
		if (prey == null) {
			in.stopSearching();
			return null;
		}
		j.jobReserve(null);
		d.planObject = prey.id();
		
		return stalk.set(a,d);
		
	}
	
	{
		D.gInit(this);
	}
	
	
	final Resumer stalk = new Resumer(D.g("Stalking", "Stalking prey")) {
		

		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (e.type == HPoll.SCARE_ANIMAL_NOT)
				return 1;
			return super.poll(a, d, e);
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_SOFT && (work(a) instanceof HunterInstance)) {
				
				if (e.other instanceof Animal) {
					Animal prey = getPrey(a, d);
					Animal an = ((Animal)e.other);
					if (prey != an) {
						if (an.reservable()) {
							if (prey != null)
								prey.reserveCancel();;
							prey = an;
							prey.reserve();
							d.planObject = prey.id();
						}
					}
					
					if (prey == an) {
						a.speed.magnitudeInit(0);
						if (((HunterInstance)work(a)).countHunt()) {
							AISubActivation s = drag_back.trySet(a, d);
							if (s != null) {
								d.overwrite(a, s);
								return false;
							}	
						}else {
							AISubActivation s = drag_back_failed.trySet(a, d);
							if (s != null) {
								prey.reserveCancel();
								d.overwrite(a, s);
								prey.scare(a, true);
								return false;
							}
						}
					}
						
						
					
				}
			}
			return super.event(a, d, e);
		}
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation ac = AI.SUBS().walkTo.path(a, d);
			if (ac != null)
				return ac;
			can(a, d);
			return null;
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			
			Animal prey = getPrey(a, d);
			int dx = prey.physics.tileC().x() - a.physics.tileC().x();
			int dy = prey.physics.tileC().y() - a.physics.tileC().y();
			
			if (Math.abs(dx) + Math.abs(dy) == 1) {
				if (((HunterInstance)work(a)).countHunt()) {
					return drag_back.set(a, d);
				}else {
					prey.reserveCancel();
					prey.scare(a, true);
					return drag_back_failed.set(a, d);
				}
				
			}else {
				
				AISubActivation ac = AI.SUBS().walkTo.coo(a, d, prey.physics.tileC());
				if (ac != null)
					return ac;
				can(a, d);
				return null;
			}
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return getPrey(a, d) != null && work(a) != null && ((HunterInstance)work(a)).getWork(d.planTile).jobReservedIs(null);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			Animal prey  = getPrey(a, d);
			if (prey != null)
				prey.reserveCancel();
			butcher_walk.can(a, d);
		}

	};
	
	final Resumer butcher = new Resumer(D.g("Butchering")) {
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			Cadaver prey = getCadaver(a, d);
			if (prey == null) {
				can(a,d);
				return null;
			}
			if (prey.resHas() && work(a) != null) {
				RESOURCE r = prey.resRemove();
				produce(r, a, d);
			}
			
			if (prey.resHas()) {
				init.RES.sound().settlement.action.squish.rnd(a.body());
				return AI.SUBS().WORK_HANDS.activate(a, d, 5);
			}else {
				if (RND.oneIn(6))
					produce(RESOURCES.LIVESTOCK(), a, d);
				can(a, d);
				return null;
			}
			
		}
		
		private void produce(RESOURCE res, Humanoid a,  AIManager d) {
			HunterInstance in = (HunterInstance) work(a);
			

			
			int kk = 0;
			for (IndustryResource r : b.industries().get(0).outs()) {
				if (r.resource == res) {
					r.inc(in, 1);
					break;
				}
				kk++;
			}
			in.gore(d.planTile);
			
			DIR dd = a.speed.dir().next(kk == 0 ? -1 : 1);
			THINGS().resources.create(a.tc().x()+dd.x(), a.tc().y()+dd.y(), res,1);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return butcher_walk.con(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			butcher_walk.can(a, d);
		}

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			HunterInstance in = (HunterInstance) work(a);
			in.resetGore(d.planTile);
			return AI.SUBS().WORK_HANDS.activate(a, d, 12);
		}
	};
	
	final Resumer butcher_walk = new Resumer(D.g("Buthering2", "butchering prey")) {
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			AISubActivation s = butcher.set(a, d);
			if (s == null)
				can(a, d);
			return s;
		}
		

		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			HunterInstance in = (HunterInstance) work(a);
			if (in == null)
				return false;
			if (in.getWork(d.planTile) == null || !in.getWork(d.planTile).jobReservedIs(null))
				return false;
			return getCadaver(a, d) != null;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			HunterInstance in = (HunterInstance) work(a);
			if (in != null) {
				SETT_JOB j = in.getWork(d.planTile);
				if (j != null)
					j.jobReserveCancel(null);
			}
		}

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.coo(a, d, d.planTile);
			if (s == null) {
				can(a, d);
				return null;
			}
			return s;
		}
	};
	
	final Resumer drag_back = new Resumer(D.g("Back", "dragging back")) {
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (e.type == HPoll.SCARE_ANIMAL_NOT)
				return 1;
			return super.poll(a, d, e);
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			Cadaver old = THINGS().cadavers.tGet.get(d.planTile);
			if (old != null)
				old.remove();
			Cadaver c = getCadaver(a, d);
			c.drag(DIR.ORTHO.get(ROOMS().fData.item.get(d.planTile).rotation), (d.planTile.x()<<C.T_SCROLL)+C.TILE_SIZEH, (d.planTile.y()<<C.T_SCROLL)+C.TILE_SIZEH, 0);
			return butcher.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return getCadaver(a, d) != null && work(a) != null && ((HunterInstance)work(a)).getWork(d.planTile).jobReservedIs(null);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			butcher_walk.can(a, d);
		}

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			Cadaver c = getPrey(a, d).slaugher();
			if (c != null) {
				AISubActivation ac = AI.SUBS().walkTo.drag(a, d, THINGS().cadavers.draggable, c.index(), d.planTile);
				if (ac != null)
					return ac;
			}
			can(a, d);
			return null;
		}
	};
	
	final Resumer drag_back_failed = new Resumer(D.g("failed", "failed to catch prey")) {
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (e.type == HPoll.SCARE_ANIMAL_NOT)
				return 1;
			return super.poll(a, d, e);
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			if (d.planByte1 == 0) {
				d.planByte1++;
				return AI.SUBS().STAND.activateRndDir(a, d, 15+RND.rInt(15));
			}
			
			
			double w = STATS.WORK().WORK_TIME.indu().getD(a.indu());
			
				
			if (w > 0.6) {
				if (w == 1) {
					can(a, d);
					return null;
				}
				return AI.SUBS().STAND.activateRndDir(a, d, 10+RND.rInt(5));
			}
			
			can(a, d);
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return work(a) != null && ((HunterInstance)work(a)).getWork(d.planTile).jobReservedIs(null);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			butcher_walk.can(a, d);
		}

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = 0;
			AISubActivation ac = AI.SUBS().walkTo.coo(a, d, d.planTile);
			if (ac != null)
				return ac;
			can(a, d);
			return null;
		}
	};
	
	
	double progress = 0;
	


	
	private Animal getPrey(Humanoid a, AIManager d) {
		if (d.planObject == -1)
			return null;
		ENTITY e = ENTITIES().getByID(d.planObject);
		
		if (e == null || !(e instanceof Animal) || !((Animal) e).reserved()) {
			d.planObject = -1;
			return null;
		}
		return (Animal) e;
	}
	
	private Cadaver getCadaver(Humanoid a, AIManager d) {
		if (d.planObject == -1)
			return null;
		Cadaver e = THINGS().cadavers.getByIndex(d.planObject);
		
		if (e == null || e.isRemoved() || !e.resHas()) {
			d.planObject = -1;
			return null;
		}
		return e;
	}

	@Override
	public double poll(Humanoid a, AIManager d, HPollData e) {
		if (e.type == HPoll.SCARE_ANIMAL_NOT)
			return 1;
		return super.poll(a, d, e);
	}
	
	@Override
	protected void name(Humanoid a, AIManager d, Str string) {
		string.add(b.employment().verb);
	}


}