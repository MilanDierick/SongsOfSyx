package init.race.appearence;

import java.io.IOException;
import java.util.Arrays;

import init.race.ExpandInit;
import init.race.appearence.RColors.ColorCollection;
import settlement.stats.STAT;
import settlement.stats.STATS;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

public final class RType {

	public final RTypeSpec spec;
	public final RPortrait portrait;
	public final RNames names;
	public final RaceSheet sheet;
	public final TILE_SHEET sheet_skelleton;
	
	public final LIST<RAddon> addonsBelow;
	public final LIST<RAddon> addonsAbove;

	RType(RColors colors, Json json, RExtras extra, ExpandInit init) throws IOException{
		{
			String sprite = json.value("SPRITE_FILE");
			if (init.map.containsKey(sprite)) {
				sheet = init.map.get(sprite);
			}else {
				sheet = new RaceSheet(init.sg.get(sprite));
				init.map.put(sprite, sheet);
			}
		}
		
		{
			String sprite = json.value("SPRITE_SKELLETON_FILE");
			if (!init.skelletons.containsKey(sprite)) {
				sheet_skelleton = new ITileSheet(init.sg.getFolder("skelleton").get(sprite), 316, 120) {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						s.singles.init(0, 0, 1, 1, 4, 3, d.s32);
						int a = 6;
						for (int i = 0; i < a; i++) {
							s.singles.setSkip(i * 2, 2).paste(3, true);
						}
						return d.s32.saveGame();
					}
				}.get();
				init.skelletons.put(sprite, sheet_skelleton);
			}else
				sheet_skelleton = init.skelletons.get(sprite);
		}
		
		
		
		spec = new RTypeSpec(colors, json);
		portrait = new RPortrait(init, colors, json);
		names = new RNames(json, init.names);
		
		LinkedList<RAddon> below = new LinkedList<>();
		LinkedList<RAddon> above = new LinkedList<>();
		
		if (json.has("ADDONS"))
			for (Json j : json.jsons("ADDONS")) {
				if (j.bool("BELOW_HEAD"))
					below.add(new RAddon(j, colors, init));
				else
					above.add(new RAddon(j, colors, init));
			}
		
		this.addonsAbove = new ArrayList<RAddon>(above);
		this.addonsBelow = new ArrayList<RAddon>(below);
	}
	
	public static class RTypeSpec {
		public final double occurrence;
		public final double occurrenceTop1;
		public final double occurrenceTop2;
		public final double[] extraOccurence = new double[STATS.APPEARANCE().extraBits.size()];
		
		
		public final ColorCollection skin;
		public final ColorCollection leg;
		
		
		RTypeSpec(RColors colors, Json json){
			
			Arrays.fill(extraOccurence, 0);
			if (json.has("SET_APPEARANCE_STATS")) {
				Json j = json.json("SET_APPEARANCE_STATS");
				for (int si = 0; si < STATS.APPEARANCE().extraBits.size(); si++) {
					STAT ss = STATS.APPEARANCE().extraBits.get(si);
					if (j.has(ss.key())) {
						extraOccurence[si] = j.d(ss.key(), 0, 1);
					}
				}
			}
			
			
			occurrence = json.has("OCCURRENCE") ? json.d("OCCURRENCE") : 0.5;
			occurrenceTop1 = json.has("OCCURRENCE_EXTRA1") ? json.d("OCCURRENCE_EXTRA1") : 0.5;
			occurrenceTop2 = json.has("OCCURRENCE_EXTRA2") ? json.d("OCCURRENCE_EXTRA2") : 0.5;
			skin = colors.collection.getByKey("COLOR_SKIN", json);
			leg = colors.collection.getByKey("COLOR_LEG", json);
		}
	}
	
}
