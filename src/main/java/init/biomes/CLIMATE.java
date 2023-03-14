package init.biomes;

import game.time.TIME;
import init.boostable.BBoost;
import init.boostable.BOOST_HOLDER;
import init.sprite.ICON;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import util.info.INFO;

public final class CLIMATE extends INFO implements INDEXED, BOOST_HOLDER{

	public final String key;
	private final int index;
	public final double seasonChange;
	public final double tempCold;
	public final double tempWarm;
	public final COLOR color;
	public final COLOR colorGroundDry;
	public final COLOR colorGroundWet;
	ClimateBonus.Boost booster;
	
	CLIMATE(LISTE<CLIMATE> all, String key, CharSequence name, CharSequence desc,  Json json){
		super(name, desc);
		index = all.add(this);
		this.key = key;
		json = json.json(key);
		this.seasonChange = json.d("SEASONAL_CHANGE", 0, 1);
		double t = json.d("TEMP_COLD",-1,1);
		t = t < 0 ? 1.0 + t : 1 + t;
		tempCold = t/2.0;
		t = json.d("TEMP_WARM",-1,1);
		t = t < 0 ? 1.0 + t : 1 + t;
		tempWarm = t/2.0;
		color = new ColorImp(json);
		json = json.json("GROUND");
		colorGroundDry = new ColorImp(json, "DRY");
		colorGroundWet = new ColorImp(json, "WET");
		
		
	}
	
	@Override
	public int index() {
		return index;
	}
	
	public double getPartOfYear() {
		int pow = (int) (seasonChange*2);
		if (pow == 0)
			return 0.5;
		double d = (TIME.years().bitPartOf()+0.125)%1.0;
		if (d < 0.5) {
			d *= 2;
			d = Math.pow(d, pow);
			return d*0.5;
		}else {
			d -= 0.5;
			d *= 2;
			d = 1.0-d;
			d = Math.pow(d, pow);
			d = 1.0-d;
			return 0.5+d*0.5;
		}
	}
	
	public ICON.BIG icon(){
		return CLIMATES.icon(this);
	}
	
	@Override
	public CharSequence boosterName() {
		return name;
	}

	@Override
	public LIST<BBoost> boosts() {
		return booster.boosts;
	}
	
}
