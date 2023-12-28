package game.events.tutorial;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import script.ScriptStates;
import script.ScriptStates.ScriptState;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.room.food.farm.ROOM_FARM;
import settlement.room.main.throne.THRONE;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import view.interrupter.IDebugPanel;
import view.ui.message.MessageText;

public class Tutorial2  {

	private final ScriptStates states = new ScriptStates();
	private static Json json;
	
	private ROOM_FARM f = null;
	

	public Tutorial2() {

		
		IDebugPanel.add("Skip tut", new ACTION() {
			@Override
			public void exe() {
				
			}
		});
		
		RESOURCE r = RESOURCES.map().tryGet("VEGETABLE");
		if (r == null)
			throw new RuntimeException();
		for (ROOM_FARM ff : SETT.ROOMS().FARMS)
			if (ff.industries().get(0).outs().get(0).resource == r)
				f = ff;
		if (f == null)
			throw new RuntimeException();
		
		//json = new Json(PATHS.SCRIPT().text.get("_TUTORIAL"));
		
		states.add(new ScriptState() {

			
			@Override
			public boolean condition() {
				return true;
			}
			
			@Override
			public void action() {
				
				Mess("HEARTH").send();
			}

		});
		
		states.add(new ScriptState() {
			
			int tx,ty;

			@Override
			public boolean condition() {
				int d = Math.min(ROOMS().HEARTH.constructor().groups().get(0).item(0, 0).width(), ROOMS().HEARTH.constructor().groups().get(0).item(0, 0).height());
				
				for (int i = 0; i < 200; i++) {
					if (SETT.ROOMS().construction.isser.is(tx, ty) && SETT.ROOMS().map.get(tx, ty).constructor() == SETT.ROOMS().HEARTH.constructor())
						return true;
					tx += d;
					if (tx >= SETT.TWIDTH) {
						ty+= d;
						tx = 0;
						if (ty >= SETT.THEIGHT)
							ty = 0;
					}
				}
				return false;
			}
			
			@Override
			public void action() {
				COORDINATE c = THRONE.coo();
				SETT.THINGS().resources.create(c, RESOURCES.WOOD(), 100);
				SETT.THINGS().resources.create(c, RESOURCES.STONE(), 100);
				Mess("HEARTH_PLACED").send();
				resources = false;
			}
		});
		
		states.add(new ScriptState() {
			
			@Override
			public boolean condition() {
				return (ROOMS().HEARTH.instancesSize() > 0);
			}
			
			@Override
			public void action() {
				Mess("HUNTER").send();
			}
		});
		
		
		states.add(new ScriptState() {
			
			@Override
			public boolean condition() {
				return (ROOMS().HUNTERS.get(0).instancesSize() > 0);
			}
			
			@Override
			public void action() {

				COORDINATE c = THRONE.coo();
				SETT.THINGS().resources.create(c, f.industries().get(0).outs().get(0).resource, 32);
				Mess("FARM").send();
			}
		});
		
		states.add(new ScriptState() {

			@Override
			public boolean condition() {
				return f.totalArea() >= 64;
			}
			
			@Override
			public void action() {
				Mess("MAINTENANCE").send();
			}
		});
		
		states.add(new ScriptState() {
			
			@Override
			public boolean condition() {
				return SETT.ROOMS().JANITOR.employment().neededWorkers() > 0;
			}
			
			@Override
			public void action() {
				Mess("STOCKPILE").send();
			}
		});
		
		states.add(new ScriptState() {
			
			@Override
			public boolean condition() {
				return SETT.ROOMS().STOCKPILE.instancesSize() > 0;
			}
			
			@Override
			public void action() {
				Mess("STOCKPILE_ALLOCATE").send();
			}
		});
		
		states.add(new ScriptState() {
			
			final RESOURCE[] rr = new RESOURCE[] {
				RESOURCES.WOOD(),
				RESOURCES.STONE(),
			};
			
			{
				for (RESOURCE r : rr)
					if (r == null)
						throw new RuntimeException();
			}
			
			@Override
			public boolean condition() {
				for (RESOURCE r : rr) {
					if (SETT.ROOMS().STOCKPILE.tally().crateDesignations(r) < 1)
						return false;
				}
				return true;
			}
			
			@Override
			public void action() {
				Mess("STOCKPILE_WORKER").send();
			}
		});
		
		states.add(new ScriptState() {
			
			@Override
			public boolean condition() {
				return SETT.ROOMS().STOCKPILE.employment().neededWorkers() > 0;
			}
			
			@Override
			public void action() {
				Mess("SUCCESS").send();
			}
		});
		
	}
	
	private boolean resources = true;
	private boolean immigration = false;
	public boolean enabled = false;
	
	
//	@Override
//	public void save(FilePutter file) {
//		states.save(file);
//		file.bool(resources);
//		file.bool(immigration);
//		file.bool(enabled);
//	}
//
//	@Override
	public void load(FileGetter file) throws IOException {
		states.load(file);
		resources = file.bool();
		immigration = file.bool();
		enabled = file.bool();
	}


	public void update(double ds) {
		if (!enabled)
			return;
		states.update(ds);
		
		if (ds > 0 && STATS.POP().POP.data(HCLASS.CITIZEN).get(null) > 0) {
			if (!resources && (!SETT.PATH().finders.resource.normal.has(RESOURCES.WOOD()) || !SETT.PATH().finders.resource.normal.has(RESOURCES.STONE()))){
				Mess("OUT_OF_RESOURCES").send();
				COORDINATE c = THRONE.coo();
				SETT.THINGS().resources.create(c, RESOURCES.WOOD(), 100);
				SETT.THINGS().resources.create(c, RESOURCES.STONE(), 100);
				resources = true;
			}
			
			if (!immigration && TIME.currentSecond() > TIME.secondsPerDay*4) {
				
				for (Race r : RACES.all()) {
					if (STATS.POP().POP.data(HCLASS.CITIZEN).get(null) > 0 && SETT.ENTRY().immi().wanted(r) > 1) {
						Mess("IMMIGRATION").send();
						immigration = true;
						
						break;
					}
				}
				
			}
		}
		
		

	}

	private static MessageText Mess(String key) {
		return new MessageText(json.json(key));
	}

}
