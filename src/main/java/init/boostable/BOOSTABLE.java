package init.boostable;

import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.biomes.CLIMATES;
import init.race.RACES;
import init.race.Race;
import init.sprite.ICON;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.INDEXED;
import util.info.INFO;

public class BOOSTABLE extends INFO implements INDEXED{
	
	public final int index;
	public final String key;
	final ICON.SMALL icon;
	public final double defValue;
	double[] climates = new double[CLIMATES.ALL().size()];

	BOOSTABLE(String key, double def, CharSequence name, CharSequence desc, ICON.SMALL icon){
		super(name, desc);
		index = BOOSTABLES.self.all.add(this);
		this.icon = icon;
		this.key = key;
		this.defValue = def;
		Arrays.fill(climates, 1);
	}

	@Override
	public int index() {
		return index;
	}
	
	public ICON.SMALL icon(){
		return icon;
	}
	
	@Override
	public String toString() {
		return "B:" + key; 
	}
	
	public double get(Humanoid h) {
		return get(h.indu());
	}
	
	public double get(Induvidual v) {
		
		double add = defValue;
		double mul = 1;
		Race r = v.race();
		add += r.bonus().add(this);
		mul *= r.bonus().mul(this);
		add += v.faction().bonus().add(this);
		mul *= v.faction().bonus().mul(this);
		add += STATS.BOOST().add(this, v);
		mul *= STATS.BOOST().mul(this, v);
		
		double d = add*mul;
		return CLAMP.d(d, 0, Math.abs(d));
	}
	
	public double max(Induvidual v) {
		
		Faction f = v.faction();
		double add = defValue;
		double mul = 1;
		
		add += RACES.bonus().add(this, v.race());
		mul *= RACES.bonus().mul(this, v.race());
		
		add += f.bonus().maxAdd(this);
		mul *= f.bonus().maxMul(this);
		
		add += STATS.BOOST().maxAdd(this);
		mul *= STATS.BOOST().maxMul(this);
		
		double d = add*mul;
		return CLAMP.d(d, 0, Math.abs(d));
	}
	
	public double get(Div v) {
		
		Faction f = v.army() == SETT.ARMIES().enemy() ? FACTIONS.other() : FACTIONS.player();
		Race r = v.info.race();
		double add = defValue;
		double mul = 1;
		
		add += r.bonus().add(this);
		mul *= r.bonus().mul(this);
		
		add += f.bonus().add(this);
		mul *= f.bonus().mul(this);
		add += STATS.BOOST().add(this, v);
		mul *= STATS.BOOST().mul(this, v);
		
		double d = add*mul;
		return CLAMP.d(d, 0, Math.abs(d));
		
	}

	public double max(Faction f) {
		
		double add = defValue;
		double mul = 1;
		
		add += RACES.bonus().maxAdd(this);
		mul *= RACES.bonus().maxMul(this);
		
		add += f.bonus().maxAdd(this);
		mul *= f.bonus().maxMul(this);
		
		add += STATS.BOOST().maxAdd(this);
		mul *= STATS.BOOST().maxMul(this);
		
		double d = add*mul;
		return CLAMP.d(d, 0, Math.abs(d));
	}

	public double get(HCLASS c, Race r, double addd) {
		
		double add = defValue+addd;
		double mul = 1;
		
		if (r == null) {
			double rAdd = 0;
			double rMul = 0;
			double popTot = 0;
			for (Race rr : RACES.all()) {
				double pop = STATS.POP().POP.data(c).get(rr);
				popTot += pop;
				rAdd += rr.bonus().add(this)*pop;
				rMul += rr.bonus().mul(this)*pop;
			}
			if (popTot > 0) {
				rAdd /= popTot;
				rMul /= popTot;
			}else {
				rAdd = 0;
				rMul = 1;
			}
			add += rAdd;
			mul *= rMul;
			
		}else {
			add += r.bonus().add(this);
			mul *= r.bonus().mul(this);
		}
		
		
		
		add += FACTIONS.player().bonus().add(this);
		mul *= FACTIONS.player().bonus().mul(this);
		add += STATS.BOOST().add(this, c, r);
		mul *= STATS.BOOST().mul(this, c, r);
		double d = add*mul;
		return CLAMP.d(d, 0, Math.abs(d));
	}
	
	public double get(HCLASS c, Race r) {
		
		return get(c, r, 0);
	}
	
	public double race(Race race) {
		return RACES.bonus().mul(this, race)*(defValue+RACES.bonus().add(this, race));
	}
	
	public double raceMax() {
		return RACES.bonus().maxMul(this)*(defValue+RACES.bonus().maxAdd(this));
	}
	
//
//
//	
//	public double race(HCLASS c, Race r) {
//		if (r == null) {
//			double mul = 0;
//			for (Race rr : RACES.all()) {
//				mul += STATS.POP().POP.data(c).get(rr)*rr.bonus().getD(this);
//			}
//			if (mul == 0)
//				mul = defValue;
//			else
//				mul /= STATS.POP().POP.data(c).get(null);
//			return mul;
//		}
//		return r.bonus().getD(this);
//	}
	
}