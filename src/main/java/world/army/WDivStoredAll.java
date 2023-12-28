package world.army;

import java.io.IOException;

import init.config.Config;
import init.race.RACES;
import init.race.Race;
import settlement.army.Div;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import world.entity.army.WArmy;

public final class WDivStoredAll {

	
	private final WDivStored[] divs = new WDivStored[Config.BATTLE.DIVISIONS_PER_ARMY];
	int amount;
	final int[] ramounts = new int [RACES.all().size()];
	private double upD;
	private int upDI;
	
	WDivStoredAll(){
		for (int i = 0; i < divs.length; i++)
			divs[i] = new WDivStored(i);
	}
	
	void save(FilePutter file) {
		for (WDivStored d : divs) {
			d.save(file);
		}
		file.i(amount);
		file.is(ramounts);
	}

	void load(FileGetter file) throws IOException {
		for (WDivStored d : divs) {
			d.load(file);
		}
		amount = file.i();
		file.is(ramounts);
	}
	
	WDivStored get(long data) {
		return divs[(int) (data& 0x0FFFF)];
	}
	
	public WArmy attachedArmy(Div div) {
		if (div.army() == SETT.ARMIES().enemy())
			return null;
		return divs[div.index()].army();
	}
	
	public WDIV get(Div div) {
		return divs[div.index()];
	}
	
	public void attach(WArmy a, Div div) {
		divs[div.index()].reassign(a);
	}
	
	public void add(Humanoid i, Div div) {
		divs[div.index()].add(i);
		for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
			AD.supplies().get(e).current().inc(attachedArmy(div), e.stat().indu().get(i.indu()));
		}
	}
	
	public int total() {
		return amount;
	}
	
	
	public int total(Race race) {
		return ramounts[race.index];
	}
	
	void update(double ds) {
		upD += ds*32;
		
		while(upD > 1) {
			upD -= 1;
			if (!SETT.ENTRY().points.hasAny() || SETT.ENTRY().isClosed())
				return;
			
			WDivStored d = divs[upDI];
			upDI++;
			upDI %= divs.length;
			
			COORDINATE cret = SETT.ENTRY().points.randomReachable(upDI);
			if (cret == null) {
				upD -= (int) upD;
				continue;
			}
			
			if (d.men() == 0 || d.army() != null) {
				continue;
			}
			
			Humanoid h = d.popSoldier(cret.x(), cret.y(), HTYPE.SUBJECT);
			if (h != null) {
				
				Div dd = SETT.ARMIES().player().divisions().get(d.index());
				
				
					
				
				if (dd.menNrOf() < Config.BATTLE.MEN_PER_DIVISION)
					h.setDivision(dd);
			}
			
				
			
		}

		
		
	}
	







	

	
}
