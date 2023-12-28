package game.events.tutorial;

import game.events.tutorial.Goal.GoalBeforeEnd;
import game.events.tutorial.Goal.GoalPrev;
import game.faction.FACTIONS;
import init.C;
import init.paths.PATHS;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.room.food.farm.ROOM_FARM;
import settlement.room.home.HOMET;
import settlement.room.main.throne.THRONE;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayListGrower;
import util.data.BOOLEAN;
import view.main.VIEW;

class Goals {
	
	final ArrayListGrower<Goal> all = new ArrayListGrower<>();


	Goals(){
		
		
		
		Json json = new Json(PATHS.SCRIPT().text.get("_TUTORIAL"));
		Goal g;
		
		g = new GoalBeforeEnd(all, json, "SPEED") {
			
			@Override
			protected boolean pIsActive() {
				return SETT.PATH().finders.job.hasAnyJobs(THRONE.coo().x(), THRONE.coo().y());
			}
			
			@Override
			protected boolean isAccomplished() {
				return true;
			}
		};
		g.hilight = new Rec(200, 65);
		g.hilight.moveCX(C.WIDTH()/2);
		g.hilight.moveY1(0);
		
		
		g = new GoalBeforeEnd(all, json, "OUT_OF_RES") {
			
			final RBITImp rr = new RBITImp();
			{
				rr.or(RESOURCES.WOOD());
				rr.or(RESOURCES.STONE());
			}

			
			private final BOOLEAN is = new BOOLEAN() {
				
				@Override
				public boolean is() {
					rr.clear();
					rr.or(RESOURCES.WOOD());
					if (!SETT.PATH().finders.resource.has(THRONE.coo().x(), THRONE.coo().y(), rr, rr, rr)) {
						return true;
						
					}
					rr.clear();
					rr.or(RESOURCES.STONE());
					return !SETT.PATH().finders.resource.has(THRONE.coo().x(), THRONE.coo().y(), rr, rr, rr);
				}
			};
						
			@Override
			protected boolean pIsActive() {
				return is.is();
			}
			
			final String key = "JOB_CLEAR_2";
			
			@Override
			protected void activateAction() {
				VIEW.s().misc.bottom.hilight(key, is);
			}
			
			@Override
			protected boolean isAccomplished() {
				
				return true;
			}
		};
		
		g = new GoalBeforeEnd(all, json, "GAME_OVER") {
						
			@Override
			protected boolean pIsActive() {
				return STATS.POP().POP.data().get(null) < 7;
			}
			
			@Override
			protected boolean isAccomplished() {
				return true;
			}
		};
		
		g = new GoalBeforeEnd(all, json, "IMMIGRANTS") {
			
			@Override
			protected boolean pIsActive() {
				return STATS.POP().POP.data(HCLASS.CITIZEN).get(null) > 0 && SETT.ENTRY().immi().wanted(FACTIONS.player().race()) > 1;
			}
			
			@Override
			protected boolean isAccomplished() {
				return true;
			}
		};
		
		g = new Goal(all, json, "WAREHOUSE_BUILD") {
			
			@Override
			protected boolean isActive() {
				return true;
			}
			
			@Override
			protected boolean isAccomplished() {
				return SETT.ROOMS().STOCKPILE.instancesSize() > 0;
			}
		};
		g.hilight = new Rec(44, 55);
		g.hilight.moveX2(C.WIDTH()-260);
		g.hilight.moveY1(0);
		
		new GoalPrev(all, json, "WAREHOUSE_ALLOCATE") {
			final RESOURCE[] rr = new RESOURCE[] {
				RESOURCES.WOOD(),
				RESOURCES.STONE(),
			};
			RESOURCE r = RESOURCES.map().tryGet("MEAT");
			
			@Override
			protected boolean isAccomplished() {
				for (RESOURCE r : rr) {
					if (SETT.ROOMS().STOCKPILE.tally().crateDesignations(r) < 1)
						return false;
					
					
				}
				if (SETT.ROOMS().STOCKPILE.tally().crateDesignations(r) < 3)
					return false;
				return true;
			}
		};
		
		new GoalPrev(all, json, "HUNTING") {

			RESOURCE meat = RESOURCES.map().tryGet("MEAT");
			
			@Override
			protected boolean isAccomplished() {
				return SETT.ROOMS().STOCKPILE.tally().amountTotal(meat) >= 100;
			}
		};
		
		new GoalPrev(all, json, "HEARTH") {

			@Override
			protected boolean isAccomplished() {
				return SETT.ROOMS().HEARTH.instancesSize() > 0;
			}
		};
		
		new GoalPrev(all, json, "WELL") {

			@Override
			protected boolean isAccomplished() {
				return SETT.ROOMS().WELLS.get(0).instancesSize() > 0;
			}
		};
		
		new GoalPrev(all, json, "HOUSING") {

			@Override
			protected boolean isAccomplished() {
				return SETT.ROOMS().HOMES.total(HOMET.get(HCLASS.CITIZEN, FACTIONS.player().race())) >= 10;
			}
		};
		
		new GoalPrev(all, json, "FARM") {
			RESOURCE r = RESOURCES.map().tryGet("VEGETABLE");
			ROOM_FARM f;
			{
				for (ROOM_FARM ff : SETT.ROOMS().FARMS)
					if (ff.industries().get(0).outs().get(0).resource == r)
						f = ff;
				if (f == null)
					throw new RuntimeException();
			}
			RBITImp rr = new RBITImp().or(r);
			GoalInfo i = new GoalInfo(json, "FARM_FAIL");
			
			@Override
			protected boolean isAccomplished() {
				if (f.totalArea() < 64) {
					if (!SETT.PATH().finders.resource.has(THRONE.coo().x(), THRONE.coo().y(), rr, rr, rr)) {
						new MessTut(i.name, i.desc,i.mission, null).send();
						activateAction();
					}
					
				}else
					return true;
				
				return f.totalArea() >= 64;
			}
			
			@Override
			protected void activateAction() {
				COORDINATE c = THRONE.coo();
				SETT.THINGS().resources.create(c, r, 32);
			}
		};
		
		new GoalPrev(all, json, "MAINTENANCE") {

			@Override
			protected boolean isAccomplished() {
				return SETT.ROOMS().JANITOR.employment().neededWorkers() > 0;
			}
		};
		
		new GoalPrev(all, json, "END") {

			@Override
			protected boolean isAccomplished() {
				return true;
			}
		};
		
	}
}
