package init.race.appearence;

import settlement.entity.humanoid.HTYPE;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import util.data.DOUBLE_O;
import util.keymap.RCollection;

public final class RColors {

	public static final int BLOOD_MASK = 64-1;
	public final COLOR blood;
	
	
	static final ColorCollection dummy = new ColorCollection(COLOR.WHITE100);
//	public final ColorCollection dead;
	private final COLOR[][] clothes;
//	private final COLOR[][] armour;
	private final ArrayList<ColorCollection> all;
	public static final COLOR grey = new ColorImp(170,170,170);
	final RCollection<ColorCollection> collection;
	
	RColors(Json data){
		
		clothes = clothes("COLOR_CLOTHES", data, STATS.EQUIP().CLOTHES.stat().indu().max(null)+1, 16);
//		armour = armour("COLOR_ARMOUR_LEVELS", data, STATS.EQUIP().BATTLEGEAR.stat().indu().max(null)+1, 16);
		blood = new ColorImp(data.json("COLOR_BLOOD"));
		
		data = data.json("COLORS");
		all = new ArrayList<>(data.keys().size());
		
		KeyMap<ColorCollection> map = new KeyMap<>();
		
		for (String k : data.keys()) {
			map.put(k, new ColorCollection(all, data, k));
		}
		
//		skin = map.get("SKIN");
//		hair = map.get("HAIR");
//		leg = map.get("LEG");
//		{
//			COLOR[] dead = new COLOR[16];
//			for (int i = 0; i < dead.length; i++)
//				dead[i] = new ColorImp().interpolate(skin.get(i), COLOR.WHITE100, 0.3);
//			this.dead = new ColorCollection(dead);
//		}
		
		collection = new RCollection<ColorCollection>("COLORS", map) {

			@Override
			public ColorCollection getAt(int index) {
				return all.get(index);
			}

			@Override
			public LIST<ColorCollection> all() {
				return all;
			}
		
		};
	}
	
	public COLOR clothes(int level, int var) {
		return clothes[var&0x0F][level%clothes[0].length];
	}
	
//	public COLOR armour(int level, int var) {
//		return armour[var&0x0F][level%armour[0].length];
//	}
	
//	public COLOR skin(Induvidual indu) {
//		return skin.get((int) (indu.randomness2()>>((skin.ran)*8)));
//	}
//	
//	public COLOR hair(Induvidual indu) {
//		if (turngray && indu.hType() == HTYPE.RETIREE)
//			return grey;
//		return hair.get((int) (indu.randomness2()>>((hair.ran)*8)));
//	}
//	
//	public COLOR leg(Induvidual indu) {
//		return leg.get((int) (indu.randomness2()>>((leg.ran)*8)));
//	}
	
	public static class ColorCollection implements INDEXED{
		
		public static ColorCollection DUMMY = new ColorCollection(COLOR.WHITE100);
		
		final String key;
		protected final COLOR[] colors;
		private final int index;
		public final int ran;
		public boolean turnsGrayWhenOld;
		public boolean turnsWhiteWhenDead;
		public boolean addsSickColor;
		public final DOUBLE_O<Induvidual> statDerive;
		
		private ColorCollection(ArrayList<ColorCollection> all, Json json, String key) {
			this.key = key;
			json = json.json(key);
			this.index = all.add(this);
			turnsGrayWhenOld = json.bool("TURNS_GRAY_WHEN_OLD", false);
			turnsWhiteWhenDead = json.bool("TURNS_WHITE_WHEN_DEAD", false);
			addsSickColor = json.bool("TURNS_SICKLY", false);
			ran = index&15;
			
			if (json.has("PICK_BY_STAT")) {
				DOUBLE_O<Induvidual> statDerive = null;
				STAT s = STATS.STAT(json.value("PICK_BY_STAT"));
				if (s != null) {
					statDerive = s.indu();
				}
				this.statDerive = statDerive;
			}else {
				this.statDerive = null;
			}
			
			LIST<ColorImp> lcols = ColorImp.cols(json, "VALUES");
			COLOR[] cols = new COLOR[16];
			
			int k = 0;
			for (ColorImp c : lcols) {
				cols[k++] = c;
			}
			
			if (json.has("GENERATE_RANDOMIZE")) {
				double d = json.d("GENERATE_RANDOMIZE");
				for (int i = k; i < 16; i++) {
					cols[i] = new ColorImp(cols[i%lcols.size()]).shade(1.0-d*(i/16.0));
				}
			}else {
				COLOR[] nn = new COLOR[16];
				double d = lcols.size()/16.0;
				for (int i = 0; i < 16; i++) {
					nn[i] = cols[(int) (i*d)];
				}
				cols = nn;
			}
			
			
			
			this.colors = cols;
			
		}
		
		private ColorCollection(COLOR color) {
			this.key = "";
			index = -1;
			this.colors = new COLOR[16];
			for (int i = 0; i < 16; i++) {
				this.colors[i] = color;
			}
			ran = 0;
			this.statDerive = null;
		}
		
		ColorCollection(COLOR[] color) {
			this.key = "";
			index = -1;
			this.colors = color;
			ran = 0;
			this.statDerive = null;
		}

		@Override
		public int index() {
			return index;
		}
		
		public COLOR get(int i) {
			return colors[i&0x0F];
		}
		
		public COLOR get(Induvidual in, boolean dead) {
			if (turnsGrayWhenOld && in.hType() == HTYPE.RETIREE)
				return grey;
			
			COLOR col = null;
			
			if (statDerive != null) {
				col = get((int)(statDerive.getD(in)*15));	
			}else {
				col = get((int) (STATS.RAN().get(in, 64) >> (ran*4)));
			}
			if (turnsWhiteWhenDead && dead)
				return col = ColorImp.TMP.interpolate(col, COLOR.WHITE100, 0.3);
			if (addsSickColor) {
				col = STATS.NEEDS().disease.colorAdd(col, in);
			}
			return col;
			
		}

	}
	
	private COLOR[][] clothes(String key, Json json, int levels, int vars) {
		Json[] is = json.jsons(key, 1, vars);
		COLOR[][] cols = new COLOR[vars][levels];
		for (int i = 0; i < cols.length; i++) {
			if (i >= is.length) {
				cols[i][levels-1] = new ColorImp(is[i%(is.length)]);
			}else {
				cols[i][levels-1] = new ColorImp(is[i]);
			}
		}
		
		for (int v = 0; v < vars; v++) {
			for (int s = levels-2; s >= 0; s--) {
				double d = (s+1.0)/(levels-1);
				cols[v][s] = cols[v][levels-1].makeSaturated(d);
			}
			
		}
		
		return cols;
	}
	
}
