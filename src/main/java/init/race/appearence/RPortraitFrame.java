package init.race.appearence;

import game.GAME;
import game.time.TIME;
import init.boostable.BOOSTABLES;
import init.race.appearence.RColors.ColorCollection;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.KeyMap;
import snake2d.util.sprite.TILE_SHEET;

final class RPortraitFrame {

	final static int TILE_SIZE = 8;
	final static int FRAMES_X = 4;
	final static int FRAMES_Y = 8;
	final static int FRAMES = FRAMES_X*FRAMES_Y;
	final static int TILES_X = 5;
	final static int TILES_Y = 6;
	final static int TILES = TILES_X*TILES_Y;
	
	private final int frameStart;
	private final int frameVariations;
	private final int random;
	final int occurence;
	
	private int dx,dy,dxr,dyr;
	private final int tile_start;
	private final int rows;
	
	private final ColorCollection color;
	private final int opacity;
	private static final OpacityImp op = new OpacityImp(0);

	public boolean stains;
//	public boolean onlyWhenOld;
//	public boolean onlyWhenOldNot;
//	public boolean onlyWhenExtra1;
//	public boolean onlyWhenExtra1Not;
//	public boolean onlyWhenExtra2;
//	public boolean onlyWhenExtra2Not;
//	public boolean onlyWhenDead;
//	public boolean onlyWhenDeadNot;
	
	public final RCondition[] cons;
	
	
	private static KeyMap<String> keepClean = new KeyMap<>();
	{
		String[] keep = new String[] {
			"FRAME_START",
			"FRAME_VARIATIONS",
			"FRAME_RANDOM",
			"FRAME_OCCURENCE",
			"OFF_X",
			"OFF_Y",
			"OFF_X_RANDOM",
			"OFF_Y_RANDOM",
			"CONDITIONS",
			"SPLITS",
			"SPLIT",
			"COLOR",
			"OPACITY",
			"STAINS",
		};
		
		for (String s : keep) {
			keepClean.put(s, s);
		}
	}
	
	
	RPortraitFrame(RColors colors, Json json, int i){
		
		for (String s : json.keys()) {
			if (!keepClean.containsKey(s)) {
				GAME.Warn(json.errorGet(s + " is not a valid modifier, available:  " + keepClean.keysString(), s));
			}
		}
		
		frameStart = json.i("FRAME_START", 0, FRAMES-1);
		frameVariations = json.i("FRAME_VARIATIONS", 1, FRAMES-frameStart);
		random = json.has("FRAME_RANDOM") ? json.i("FRAME_RANDOM", 0, 8) : i%8;
		occurence = (int) (0x010*(json.has("FRAME_OCCURRENCE") ? json.d("FRAME_OCCURRENCE", 0, 1) : 1.0));
		dx = json.has("OFF_X") ? json.i("OFF_X", -40, 40) : 0;
		dy = json.has("OFF_Y") ? json.i("OFF_Y", -48, 48) : 0;
		dxr = json.has("OFF_X_RANDOM") ? json.i("OFF_X_RANDOM", 0, 40) : 0;
		dyr = json.has("OFF_Y_RANDOM") ? json.i("OFF_Y_RANDOM", 0, 48) : 0;
		
		if (json.has("CONDITIONS")) {
			Json[] jj = json.jsons("CONDITIONS");
			cons = new RCondition[jj.length];
			for (int ii = 0; ii < jj.length; ii++)
				cons[ii] = new RCondition(jj[ii]);
		}else {
			cons = new RCondition[0];
		}
		
		
		int splits = json.has("SPLITS") ? json.i("SPLITS", 1, 3) : 1;
		rows = TILES_Y/splits;
		int split = json.has("SPLIT") ? json.i("SPLIT", 0, splits) : 0;
		
		tile_start = frameStart*TILES + split*rows*TILES_X;
		dy += split*rows*TILE_SIZE;

		color = json.has("COLOR") ? colors.collection.get(json.value("COLOR"), json) : RColors.dummy;
		opacity = json.has("OPACITY") ? json.i("OPACITY", 0, 256) : 255;

//		onlyWhenOld = json.bool("ONLY_IF_OLD", false);
//		onlyWhenOldNot = json.bool("ONLY_IF_OLD_NOT", false);
//		onlyWhenExtra1 = json.bool("ONLY_IF_EXTRA1", false);
//		onlyWhenExtra1Not = json.bool("ONLY_IF_EXTRA1_NOT", false);
//		onlyWhenExtra2 = json.bool("ONLY_IF_EXTRA2", false);
//		onlyWhenExtra2Not = json.bool("ONLY_IF_EXTRA2_NOT", false);
//		onlyWhenDead = json.bool("ONLY_IF_DEAD", false);
//		onlyWhenDeadNot = json.bool("ONLY_IF_DEAD_NOT", false);
		stains = json.has("STAINS") ? json.bool("STAINS") : true;
		
	}
	
	public void render(SPRITE_RENDERER r, TILE_SHEET sheet, TILE_SHEET sFilth, TILE_SHEET sBlood, int x1, int y1, Induvidual indu, boolean isDead, int scale) {
		
		double age = STATS.POP().AGE.indu().get(indu)/(TIME.years().bitConversion(TIME.days()));
		
		double death = BOOSTABLES.PHYSICS().DEATH_AGE.get(indu);
		double grayAt = age/death;
		
		for (RCondition c : cons) {
			if (!c.comp.passes(c.stat.getD(indu), c.compI))
				return; 
		}
		int ran = (int) ((indu.randomness() >> (random*8)) & 0x0FF);
		if (occurence <= (ran&0x0F))
			return;
//		if (onlyWhenOld && grayAt < 0.7)
//			return;
//		if (onlyWhenOldNot && grayAt >= 0.7)
//			return;
//		if (onlyWhenExtra1 && STATS.APPEARANCE().hasTop1.get(indu) == 0)
//			return;
//		if (onlyWhenExtra1Not && STATS.APPEARANCE().hasTop1.get(indu) == 1)
//			return;
//		if (onlyWhenExtra2 && STATS.APPEARANCE().hasTop2.get(indu) == 0)
//			return;
//		if (onlyWhenExtra2Not && STATS.APPEARANCE().hasTop2.get(indu) == 1)
//			return;
//		if (onlyWhenDead && !isDead)
//			return;
//		if (onlyWhenDeadNot && isDead)
//			return;
		
		COLOR col = color.get((int) ((indu.randomness2() >> (color.ran*4)) & 0x0F));
		
		if (color.turnsGrayWhenOld && grayAt > 0.7) {
			double d = grayAt-0.7;
			d /= 0.2;
			d = CLAMP.d(d, 0, 1);
			col = ColorImp.TMP.interpolate(col, RColors.grey, d);
		}
		
		if (color.turnsWhiteWhenDead && isDead) {
			col = ColorImp.TMP.set(col).saturateSelf(0.7);
		}else if (color.addsSickColor) {
			col = STATS.NEEDS().disease.colorAdd(col, indu);
		}
		
		col = ColorImp.TMP.set(col).shadeSelf(1.2);
		
		col.bind();
		op.set(opacity);
		op.bind();
		
		x1 = (int) (x1 + (dx + (ran/15.0)*dxr)*scale);
		y1 = (int) (y1 + (dy + ((ran>>4)/15.0)*dyr)*scale);
		int var = ran % frameVariations;
		
		int i = tile_start +  TILES*var;
		for (int iy = 0; iy < rows; iy++) {
			for (int ix = 0; ix < TILES_X; ix++) {
				sheet.render(r, i++, x1+ix*TILE_SIZE*scale, x1+(ix+1)*TILE_SIZE*scale,  y1+iy*TILE_SIZE*scale, y1+(iy+1)*TILE_SIZE*scale);
			}
		}
		COLOR.unbind();
		OPACITY.O99.bind();
		if (stains) {
			int am = (int) (Math.ceil(STATS.NEEDS().DIRTINESS.stat().indu().getD(indu)*4)-1);
			if (am >= 0) {
				int ti = tile_start%TILES;
				ti += am*TILES;
				i = tile_start + TILES*var;
				for (int iy = 0; iy < rows; iy++) {
					for (int ix = 0; ix < TILES_X; ix++) {
						sheet.renderTextured(sFilth.getTexture(ti++), i++, x1+ix*TILE_SIZE*scale, y1+iy*TILE_SIZE*scale, scale);
					}
				}
			}
			if (STATS.NEEDS().INJURIES.count.getD(indu) > 0) {
				indu.race().appearance().colors.blood.bind();
				int ti = tile_start%TILES;
				ti += (int)((STATS.NEEDS().INJURIES.count.getD(indu)-0.1)*4)*TILES;
				i = tile_start + TILES*var;
				for (int iy = 0; iy < rows; iy++) {
					for (int ix = 0; ix < TILES_X; ix++) {
						sheet.renderTextured(sBlood.getTexture(ti++), i++, x1+ix*TILE_SIZE*scale, y1+iy*TILE_SIZE*scale, scale);
					}
				}
				COLOR.unbind();
			}
		}
		

		
		OPACITY.unbind();
		
	}
	
}
