package settlement.army;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.RES;
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
	private int men = 0;
	private final int menMax;
	
	private double upTimer = 0;
	private double morale = 1;
	public final int bit;
	final long[] moraleData;
	
	Army(ArrayList<Army> armies, ArrayList<Div> divisions, int menPerDevision){
		this.index = armies.add(this);
		ArrayList<Div> divs = new ArrayList<>(RES.config().BATTLE.DIVISIONS_PER_ARMY);
		for (int i = 0; i < RES.config().BATTLE.DIVISIONS_PER_ARMY; i++)
			new Div(divisions, divs, this, menPerDevision);
		this.divisions = divs;
		menMax = menPerDevision*RES.config().BATTLE.DIVISIONS_PER_ARMY;
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
	
	public int men() {
		return men;
	}
	
	public int menMax() {
		return menMax;
	}
	
	public Army enemy() {
		return SETT.ARMIES().armies().get((index+1)%2);
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			for (Div d : divisions)
				d.saver.save(file);
			file.i(men);
			file.d(morale);
			file.ls(moraleData);
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
		}
	};

	public double morale() {
		return morale;
	}
	
	void update(double ds) {
		upTimer -= ds;
		if (upTimer < 0) {
			initMorale();
			
		}
	}
	
	public void initMorale() {
		morale = 1;
		for (DOUBLE_O<Army> d : ArmyMorale.factors)
			morale *= d.getD(this);
		upTimer += 2;
		morale = CLAMP.d(morale, 0, 1.5);
	}

	void menInc(int i) {
		men += i;
	}
	
	public boolean defender() {
		return true;
	}
	
	public Faction faction() {
		return this == SETT.ARMIES().player() ? FACTIONS.player() : FACTIONS.other();
	}
	
}
