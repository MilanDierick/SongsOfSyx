package init.race.appearence;

import init.race.appearence.RColors.ColorCollection;
import snake2d.util.file.Json;
import snake2d.util.sets.KeyMap;
import snake2d.util.sprite.TILE_SHEET;

public final class RType {

	public final RTypeSpec spec;
	public final RPortrait portrait;
	public final RNames names;

	RType(RColors colors, Json json, TILE_SHEET psheet, ExtraSprite extra, KeyMap<String[]> namemap){
		
		spec = new RTypeSpec(colors, json);
		portrait = new RPortrait(colors, json, psheet, extra);
		names = new RNames(json, namemap);
	}
	
	public static class RTypeSpec {
		public final double occurrence;
		public final double occurrenceTop1;
		public final double occurrenceTop2;
		
		public final ColorCollection skin;
		public final ColorCollection leg;
		
		public final ColorCollection top1;
		public final ColorCollection top2;
		
		RTypeSpec(RColors colors, Json json){
			occurrence = json.has("OCCURRENCE") ? json.d("OCCURRENCE") : 0.5;
			occurrenceTop1 = json.has("OCCURRENCE_EXTRA1") ? json.d("OCCURRENCE_EXTRA1") : 0.5;
			occurrenceTop2 = json.has("OCCURRENCE_EXTRA2") ? json.d("OCCURRENCE_EXTRA2") : 0.5;
			skin = colors.collection.getByKey("COLOR_SKIN", json);
			leg = colors.collection.getByKey("COLOR_LEG", json);
			top1 = colors.collection.getByKey("COLOR_EXTRA1", json);
			top2 = colors.collection.getByKey("COLOR_EXTRA2", json);
		}
	}
	
}
