package init.biomes;


import java.util.Set;

import init.D;
import init.boostable.BOOSTABLES;
import init.paths.PATHS;
import init.sprite.ICON;
import init.sprite.SPRITES;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import util.info.INFO;
import util.keymap.KEY_COLLECTION;

public final class CLIMATES {

	public final static String KEY = "CLIMATE";
	private static Data d;
	private static ClimateBonus bonus;

	
	private CLIMATES() {
		
	}
	
	public static void init() {
		new Data();
	}
	
	public static void initBonuses(BOOSTABLES bo, double[][] climates) {
		bonus = new ClimateBonus(bo, climates);
	}
	
	public static CLIMATE COLD() {
		return d.COLD;
	}
	
	public static CLIMATE TEMP() {
		return d.TEMPERATE;
	}
	
	public static CLIMATE HOT() {
		return d.HOT;
	}
	
	public static LIST<CLIMATE> ALL(){
		return d.all();
	}
	
	public static KEY_COLLECTION<CLIMATE> MAP(){
		return d;
	}
	
	public static INFO INFO() {
		return d.info;
	}
	
	public static ClimateBonus BONUS() {
		return bonus;
	}
	

	static ICON.BIG icon(CLIMATE c){
		switch(c.index()) {
		case 0: return SPRITES.icons().l.season_winter;
		case 1: return SPRITES.icons().l.season_spring;
		case 2: return SPRITES.icons().l.season_summer;
		}
		return null;
	}
	

	
	private static final class Data implements KEY_COLLECTION<CLIMATE>{

		private final CLIMATE COLD;
		private final CLIMATE TEMPERATE;
		private final CLIMATE HOT;
		private final ArrayList<CLIMATE> all = new ArrayList<>(3);
		private final KeyMap<CLIMATE> map = new KeyMap<CLIMATE>();
		private final INFO info;
		
		Data(){
			D.gInit(CLIMATES.class);
			d = this;
			Json j = new Json(PATHS.CONFIG().get(KEY));
			COLD = new CLIMATE(
					all, "COLD", 
					D.g("Cold"),
					D.g("cold_desc", "Very cold winters. Unique crops. Low disease rates."),
					j);
			TEMPERATE = new CLIMATE(
					all, "TEMPERATE", 
					D.g("Temperate"),
					D.g("temp_desc", "Varying temperature."),
					j);
			HOT = new CLIMATE(
					all, "HOT", 
					D.g("Warm"),
					D.g("warm_desc", "Hot summers."),
					j);
			info = new INFO(D.g("Climate"), D.g("desc", "Climate zones have a range of bonuses and drawbacks. They also have different base temperatures, which can lead to exposure and death for your subjects depending on their natural resilience to hot and cold."));
			for (CLIMATE c : all)
				map.put(c.key, c);
		}
		
		@Override
		public CLIMATE tryGet(String value) {
			return map.get(value);
		}

		@Override
		public String key() {
			return KEY;
		}

		@Override
		public LIST<CLIMATE> all() {
			return all;
		}
		
		@Override
		public Set<String> available() {
			return map.keys();
		}
		
	}
	
	
}
