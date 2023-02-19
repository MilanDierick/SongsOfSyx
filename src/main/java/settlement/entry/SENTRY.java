package settlement.entry;

import java.io.IOException;

import game.faction.FACTIONS;
import init.race.Race;
import settlement.entity.humanoid.HTYPE;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.main.SETT.SettResource;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import view.main.MessageText;
import view.main.VIEW;
import world.World;
import world.entity.WEntity;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;
import world.map.regions.Region;

public final class SENTRY extends SettResource{

	private static CharSequence ¤¤open = "Capitol Open!";
	private static CharSequence ¤¤openD = "Your capitol is now open for immigrants and trade.";
	private static CharSequence ¤¤closed = "Capitol Closed!";
	private static CharSequence ¤¤closedD = "Your capitol is unreachable. There is no clear path from the edge of your city map to your Throne. As a consequence, no immigration or trade can happen. Enemies can't attack you either, but will keep besieging you indefinitely. If not done deliberately, clear a path to an edge of the map as soon as possible.";
	
	
	private double checkTimer = 0;
	private boolean isClosed = false;
	private final PeopleSpawner spawn = new PeopleSpawner();
	private final Immigration im = new Immigration();
	private boolean besieged;
//	private final Invador invador = new Invador();
	
	public void add(Race race, HTYPE type, int amount) {
		if (amount <= 0)
			return;
		spawn.add(race, type, amount);
	}
	
	public int onTheirWay(Race race, HTYPE type) {
		return spawn.onTheirWay(race, type);
	}
	
	@Override
	protected void update(float ds) {
		
		if (VIEW.b().isActive())
			return;
		
		if (!SETT.INVADOR().invading()) {
			
			
			checkTimer += ds;
			if (checkTimer > 5) {
				checkTimer -= 5;
				Region c = FACTIONS.player().capitolRegion();
				
				besieged = false;
				
				if (SETT.INVADOR().invading()) {
					besieged = true;
				}else {
					for (WEntity e : World.ENTITIES().fillTiles(c.cx()-3, c.cx()+3, c.cy()-3, c.cy()+3)) {
						if (e instanceof WArmy) {
							WArmy a = (WArmy) e;
							if (FACTIONS.rel().enemy(a.faction(), FACTIONS.player()) && a.state() == WArmyState.besieging) {
								besieged = true;
								break;
							}
						}
					}
				}
				
				
				
				
				
				if (besieged)
					isClosed = true;
				else if (isClosed && SETT.PATH().entryPoints.hasAny()){
					isClosed = false;
					if (!VIEW.b().isActive())
						new MessageText(¤¤open, ¤¤openD).send();
				}else if (!isClosed && !SETT.PATH().entryPoints.hasAny()) {
					if (!VIEW.b().isActive())
						new MessageText(¤¤closed, ¤¤closedD).send();
					isClosed = true;
				}
				
			}
			
			

		}
		
		
		
		if (isClosed) {
			spawn.update(0);
			im.update(0);
		}else {
			spawn.update(ds);
			im.update(ds);
		}
		
		super.update(ds);
	}
	
	@Override
	protected void save(FilePutter file) {
		spawn.save(file);
		im.saver.save(file);
		file.bool(isClosed);
		file.d(checkTimer);
		file.bool(besieged);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		spawn.load(file);
		im.saver.load(file);
		isClosed = file.bool();
		checkTimer = file.d();
		besieged = file.bool();
	}
	
	@Override
	protected void clearBeforeGeneration(CapitolArea area) {
		spawn.clear();
		im.saver.clear();
		isClosed = false;
		checkTimer = 0;
		besieged = false;
	}
	
	public Immigration immi() {
		return im;
	}
	
	public boolean isClosed() {
		return isClosed || SETT.INVADOR().invading();
	}
	
	public boolean beseiged() {
		return besieged;
	}
	
}
