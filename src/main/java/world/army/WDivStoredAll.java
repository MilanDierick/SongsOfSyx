package world.army;

import java.io.IOException;
import java.util.Arrays;

import init.RES;
import init.race.RACES;
import init.race.Race;
import settlement.army.Div;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayCooShort;
import world.entity.army.WArmy;

public final class WDivStoredAll {

	
	private final WDivStored[] divs = new WDivStored[RES.config().BATTLE.DIVISIONS_PER_ARMY];
	int amount;
	final int[] ramounts = new int [RACES.all().size()];
	private double upD;
	private int upDI;
	private final ArrayCooShort returnPoints = new ArrayCooShort(32);
	
	
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

	void clear() {
		for (WDivStored d : divs) {
			d.clear();;
		}
		amount = 0;
		Arrays.fill(ramounts, 0);
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
		divs[div.index()].armySet(a);
	}
	
	public void add(Humanoid i, Div div) {
		divs[div.index()].add(i);
		for (EQUIPPABLE_MILITARY e : STATS.EQUIP().military_all()) {
			WARMYD.supplies().get(e).current().inc(attachedArmy(div), e.stat().indu().get(i.indu()));
		}
	}
	
	public int total() {
		return amount;
	}
	
	
	public int total(Race race) {
		return ramounts[race.index];
	}
	
	void update(double ds) {
		upD += ds;
		if (upD < 0.5)
			return;
		
		upD -= 0.5;
		if (!SETT.PATH().entryPoints.hasAny() || SETT.ENTRY().isClosed())
			return;
		if (returnPoints.size() == 0 || RND.oneIn(100)) {
			SETT.PATH().entryPoints.rnd(returnPoints);
		}
		if (returnPoints.size() == 0) {
			return;
		}
		
		returnPoints.set(RND.rInt(returnPoints.size()));
		
		if (!SETT.PATH().entryPoints.validate(returnPoints.get())) {
			SETT.PATH().entryPoints.rnd(returnPoints);
			return;
		}
		
		for (int i = 0; i < divs.length; i++) {
			
			
			WDivStored d = divs[upDI];
			if (d.army() == null && d.men() > 0) {
				Humanoid h = d.popSoldier(returnPoints.get().x(), returnPoints.get().y(), HTYPE.RECRUIT);
				if (h != null) {
					Div dd = SETT.ARMIES().player().divisions().get(d.index());
					if (dd.menNrOf() < RES.config().BATTLE.MEN_PER_DIVISION)
						h.setDivision(dd);
				}
				return;
			}
			upDI++;
			upDI %= divs.length;
			
		}
		
		
	}
	
	
	






	

	
}
