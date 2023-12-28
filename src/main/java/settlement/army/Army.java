package settlement.army;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.config.Config;
import settlement.main.SETT;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.DOUBLE_O;

public final class Army implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private final int index;
	private final LIST<Div> divisions;
	private final ArrayList<Div> ordered;
	private int men = 0;
	private final int menMax;
	
	private double upTimer = 0;
	private double morale = 1;
	public final int bit;
	final long[] moraleData;
	
	Army(ArrayList<Army> armies, ArrayList<Div> divisions, int menPerDevision){
		this.index = armies.add(this);
		
		ArrayList<Div> divs = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
		
		
		for (int i = 0; i < Config.BATTLE.DIVISIONS_PER_ARMY; i++)
			new Div(divisions, divs, this, menPerDevision);
		this.divisions = divs;
		ordered = new ArrayList<Div>(divs);
		
		menMax = menPerDevision*Config.BATTLE.DIVISIONS_PER_ARMY;
		bit = 1 << index;
		moraleData = new long[ArmyMorale.data.longCount()];
		ArmyMorale.SUPPLIES.setD(this, 1.0);
	}
	
	public int index() {
		return index;
	}
	
	public LIST<Div> divisions(){
		return divisions;
	}
	
	public LIST<Div> ordered(){
		return ordered;
	}
	
	public void setDivAtOrderedIndex(Div toBeReplaced, Div replacer) {
		if (ordered.remove(replacer) == null)
			throw new RuntimeException();
		int oi = ordered.indexOf(toBeReplaced);
		if (oi < 0)
			throw new RuntimeException();
		ordered.insert(oi, replacer);
	}
	
	public Div getNextEmptyOrdered() {
		for (int di = 0; di < ordered.size(); di++) {
			Div d = ordered.get(di);
			if (d.info.men() == 0 && d.menNrOf() == 0) {
				return d;
			}
		}
		return null;
	}
	
	public int men() {
		return men;
	}
	
	public int menMax() {
		return menMax;
	}
	
	public Army enemy() {
		return SETT.ARMIES().armies().get((index+1)%2);
	}
	
	public Faction faction() {
		if (this == SETT.ARMIES().player())
			return FACTIONS.player();
		return FACTIONS.otherFaction();
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			for (Div d : divisions)
				d.saver.save(file);
			file.i(men);
			file.d(morale);
			file.ls(moraleData);
			for (Div d : ordered) {
				file.i(d.indexArmy());
			}
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			for (Div d : divisions)
				d.saver.load(file);
			men = file.i();
			morale = file.d();
			file.ls(moraleData);
			if (men() == 0) {
				for (Div d : divisions)
					d.morale.init();
				Arrays.fill(moraleData, 0);
				ArmyMorale.SUPPLIES.setD(Army.this, 1.0);
			}
			
			ordered.clearSloppy();
			for (int i = 0; i < divisions.size(); i++) {
				ordered.add(divisions.get(file.i()));
			}
			
			update(3);
		}
		
		@Override
		public void clear() {
			for (Div d : divisions)
				d.saver.clear();
			men = 0;
			upTimer = 0;
			morale = 1;
			Arrays.fill(moraleData, 0);
			ArmyMorale.SUPPLIES.setD(Army.this, 1.0);
			ordered.clearSloppy();
			ordered.add(divisions);
		}
	};

	public double morale() {
		return morale;
	}
	
	void update(double ds) {
		upTimer -= ds;
		if (upTimer < 0 && SETT.ARMIES().enemy().men() == 0) {
			if (this == SETT.ARMIES().enemy())
				resetMorale();
			else {
				ArmyMorale.CASULTIES.inc(this, CLAMP.i(ArmyMorale.CASULTIES.get(this)/10, 10, 1000));
				ArmyMorale.DESERTION.inc(this, CLAMP.i(ArmyMorale.DESERTION.get(this)/10, 10, 1000));
			}
			
		}
	}
	
	public void resetMorale() {
		
		ArmyMorale.CASULTIES.set(this, 0);
		ArmyMorale.DESERTION.set(this, 0);
		ArmyMorale.SUPPLIES.setD(this, 1.0);
		for (DOUBLE_O<Army> d : ArmyMorale.factors) {
			morale *= d.getD(this);
		}
		morale = 1;
		morale = CLAMP.d(morale, 0, 1.5);
	}

	void menInc(int i) {
		men += i;
	}
	
	public boolean defender() {
		return true;
	}
	
}
