package world.army;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.updating.IUpdater;
import world.World;
import world.World.WorldResource;
import world.entity.army.WArmy;

public class WArmies extends WorldResource{
	
	private final ArrayList<FactionArmies> fArmies = new ArrayList<>(FACTIONS.MAX);
	private final FactionArmies rebels = new FactionArmies(-1);
	private final Updater updateDay = new Updater();
	private final double[] tmp = new double[RACES.all().size()];
	private final WDivStoredAll cityDivs = new WDivStoredAll();
	private final WDivMercenaries mercenaries = new WDivMercenaries();
	private final WDivRegionalAll regional = new WDivRegionalAll();
	private final WDivsCapitol city = new WDivsCapitol();
	private final WArmyUpdater updater = new WArmyUpdater();
	
	public WArmies() {;
		for (int i = 0; i < FACTIONS.MAX; i++)
			fArmies.add(new FactionArmies(i));
		
	}
	
	@Override
	protected void save(FilePutter file) {
		for (FactionArmies a : fArmies)
			a.saver.save(file);
		rebels.saver.save(file);
		cityDivs.save(file);
		mercenaries.save(file);
		city.save(file);
		regional.save(file);
		updateDay.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		for (FactionArmies a : fArmies)
			a.saver.load(file);
		rebels.saver.load(file);
		cityDivs.load(file);
		mercenaries.load(file);
		city.load(file);
		regional.load(file);
		updateDay.load(file);
	}
	
	@Override
	protected void update(float ds) {
		updateDay.update(ds);
		cityDivs.update(ds);
		mercenaries.update(ds);
		super.update(ds);
	}
	
	@Override
	protected void clear() {
		for (FactionArmies a : fArmies)
			a.saver.clear();	
		cityDivs.clear();
		mercenaries.clear();
		city.clear();
		updateDay.clear();
		regional.clear();
	}
	
	public LIST<FactionArmies> armies(){
		return fArmies;
	}
	
	public FactionArmies army(int i) {
		return fArmies.get(i);
	}
	
	public FactionArmies army(Faction f) {
		return fArmies.get(f.index());
	}
	
	public WDivStoredAll cityDivs() {
		return cityDivs;
	}
	
	public WDivRegionalAll regional() {
		return regional;
	}
	
	public WDivMercenaries mercenaries() {
		return mercenaries;
	}
	
	public LIST<WDIV> playerGarrison(){
		return city;
	}
	
	public void extractLostEquipment(int[] amounts) {
		city.extractLostEquipment(amounts);
	}
	
	public FactionArmies rebels() {
		return rebels;
	}
	
	public WArmy createRebel(int tx, int ty) {
		if (rebels.canCreate()) {
			int i = World.ENTITIES().armies.create(tx, ty, null);
			return  World.ENTITIES().armies.get(i);
		}
		return null;
	}
	
	private class Updater extends IUpdater {

		public Updater() {
			super(FACTIONS.MAX+1, TIME.secondsPerDay);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void update(int i, double timeSinceLast) {
			
			if (i == FACTIONS.MAX) {
				FactionArmies as = rebels;
				for (int ai = 0; ai < as.all().size(); ai++) {
					WArmy a = as.all().get(ai);
					updater.update(a);
				}
				return;
			}
			
			Faction f = FACTIONS.getByIndex(i);
			for (Race r : RACES.all()) {
				int n = WARMYD.conscriptable(r).get(f);
				int t = targetConscripts(r, f);
				
				double d = t-n;
				
				if (d > 0) {
					d = f.kingdom().realm().population().get(r)*0.00125;
					n += (int) d;
					if (RND.rFloat() < (d - (int) d))
						n++;
				}else if (d < 0) {
					n = t;
				}
				
				n = CLAMP.i(n, 0, t);
				
				WARMYD.conscriptable(r).set(f, n);
				
				tmp[r.index] = WARMYD.penalty(f, r);
			}
			
			FactionArmies as = fArmies.get(i);
			
			for (int ai = 0; ai < as.all().size(); ai++) {
				WArmy a = as.all().get(ai);
				if (!updater.update(a))
					ai--;
			}
			
			
		}
		
		
	}

	private int targetConscripts(Race r, Faction f) {
		double p = 1 + f.kingdom().realm().regions().size()*0.1;
		int t = (int) (f.kingdom().realm().population().get(r)*0.15/p);
		return t;
	}
	
	public void init(Faction f) {
		for (Race r : RACES.all()) {
			WARMYD.conscriptable(r).set(f, targetConscripts(r, f));
		}
		
	}
	
	
	private final Hoverer hoverer = new Hoverer();
	
	public Hoverer hoverer() {
		return hoverer;
	}

}
