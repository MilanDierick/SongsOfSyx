package world.army;

import java.io.IOException;

import game.Profiler;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.updating.IUpdater;
import world.WORLD;
import world.WORLD.WorldResource;
import world.army.ai.WArmyAI;
import world.entity.army.WArmy;

public class WARMIES extends WorldResource{
	
	private final AD RD = new AD(this);
	private final ArrayList<FactionArmies> fArmies = new ArrayList<>(FACTIONS.MAX);
	private final FactionArmies rebels = new FactionArmies(-1);
	private final Updater updateDay = new Updater();
	private final WDivStoredAll cityDivs = new WDivStoredAll();
	private final WDivMercenaries mercenaries = new WDivMercenaries();
	private final WDivRegionalAll regional = new WDivRegionalAll();
	private final WDivsCapitol city = new WDivsCapitol();
	private final WArmyUpdater updater = new WArmyUpdater();
	public final WArmyAI AI = new WArmyAI();
	
	public WARMIES() {;
		for (int i = 0; i < FACTIONS.MAX; i++)
			fArmies.add(new FactionArmies(i));
		
		new Faction.FactionActivityListener() {
			
			@Override
			public void remove(Faction f) {
				while (army(f.index()).all().size() > 0) {
					army(f.index()).all().get(0).disband();
				}
			}

			@Override
			public void add(Faction f) {
				// TODO Auto-generated method stub
				
			}
		};
		

		
	}
	
	@Override
	protected void save(FilePutter file) {
		
		rebels.saver.save(file);
		cityDivs.save(file);
		mercenaries.save(file);
		city.save(file);
		regional.save(file);
		updateDay.save(file);
		AI.saver.save(file);
		for (FactionArmies a : fArmies)
			a.saver.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		
		rebels.saver.load(file);
		cityDivs.load(file);
		mercenaries.load(file);
		city.load(file);
		regional.load(file);
		updateDay.load(file);
		AI.saver.load(file);
		for (FactionArmies a : fArmies)
			a.saver.load(file);
	}
	
	@Override
	protected void update(float ds, Profiler prof) {
		prof.logStart(this);
		updateDay.update(ds);
		cityDivs.update(ds);
		mercenaries.update(ds);
		AI.update(ds);
		prof.logEnd(this);
	}
	
	@Override
	protected void clear() {
		for (FactionArmies a : fArmies)
			a.saver.clear();	

		city.clear();
		updateDay.clear();
		regional.clear();
		AI.saver.clear();
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
			int i = WORLD.ENTITIES().armies.create(tx, ty, null);
			return  WORLD.ENTITIES().armies.get(i);
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
			RD.update(f, timeSinceLast);
			
			
			FactionArmies as = fArmies.get(i);
			
			for (int ai = 0; ai < as.all().size(); ai++) {
				WArmy a = as.all().get(ai);
				if (!updater.update(a))
					ai--;
			}
			
			
		}
		
		
	}


	
	public void init() {
		
		for (Faction f : FACTIONS.active()) {
			RD.init(f);
			AI.init(f);
		}
		
		WORLD.ARMIES().mercenaries().randmoize();
		
	}
	

}
