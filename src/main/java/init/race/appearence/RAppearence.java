package init.race.appearence;

import java.io.IOException;

import init.paths.PATH;
import init.race.Race;
import init.race.appearence.RType.RTypeSpec;
import init.sprite.ICON;
import settlement.entity.humanoid.HTYPE;
import settlement.stats.Induvidual;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.*;

public final class RAppearence {

	public final RColors colors;
	public final ICON.MEDIUM icon;
	public final ICON.BIG iconBig;
	public final SPRITE repSprite;
	private final RaceSheet sheet;
	private final RaceSheet sheet_child;
	private final TILE_SHEET sheet_skelleton;
	private final TILE_SHEET sheet_child_skelleton;
	public final TILE_SHEET sPortrait;
	public final ExtraSprite extra;
	public final int off;
	

	
	
	public final RTypeSpec child;
	public final LIST<RType> types;
	public double tMax;
	
	
	public RAppearence(Race race, PATH sg, Json data, KeyMap<TILE_SHEET> skelleton, KeyMap<RAppearence> others, KeyMap<RaceSheet> children, KeyMap<ExtraSprite> extras,  KeyMap<String[]> names, int hitboxSize) throws IOException{
		
		data = data.json("APPEARANCE");
		colors = new RColors(data);
		
		String s = data.value("SPRITE_EXTRA_FILE");
		if (extras.containsKey(s)) {
			this.extra = extras.get(s);
		}else {
			this.extra = new ExtraSprite(sg.getFolder("extra").get(s));
			extras.put(s, this.extra);
		}
		
		String sprite = data.value("SPRITE_FILE");
		if (others.containsKey(sprite)) {
			RAppearence o = others.get(sprite);
			icon = o.icon;
			iconBig = o.iconBig;
			repSprite = o.repSprite;
			sheet = o.sheet;
			sPortrait = o.sPortrait;
		}else {
			
			icon = IIcon.MEDIUM.get(new ISpriteData(sg.get(sprite), 864, 636) {

				@Override
				protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					
					s.singles.init(0, 0, 1, 1, 1, 1, d.s24);
					s.singles.paste(true);
					return d.s24.saveSprite();
				}
				
			}.get());
			
			repSprite = ISprite.game(new ISpriteData() {

				@Override
				protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(s.singles.body().x2(), 0, 1, 1, 1, 1, d.s24);
					s.singles.paste(true);
					return d.s24.saveSprite();
				}
				
			}.get());
			
			iconBig = IIcon.LARGE.get(new ISpriteData() {

				@Override
				protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					
					s.singles.init(s.singles.body().x2(), 0, 1, 1, 1, 1, d.s32);
					s.singles.paste(true);
					return d.s32.saveSprite();
				}
				
			}.get());
			
			int am = 0;
			{
				for (Json t : data.jsons("TYPES")) {
					for (Json p : t.jsons("PORTRAIT")) {
						am = Math.max(am, p.i("FRAME_START") + p.i("FRAME_VARIATIONS"));
					}
				}
				am = CLAMP.i(am, 0, PortraitFrame.FRAMES);
			}
			final int FRAMES = am;
			sPortrait = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, s.singles.body().y2(), PortraitFrame.FRAMES_X, PortraitFrame.FRAMES_Y, PortraitFrame.TILES_X, PortraitFrame.TILES_Y, d.s8);
					for (int i = 0; i < FRAMES; i++)
						s.full.setVar(i).paste(true);
					return d.s8.saveGame();
				}
			}.get();
			
			sheet = new RaceSheet(208);
			
			others.put(sprite, this);
		}
		
		{

			String key = data.value("SPRITE_CHILD_FILE");
			if (children.containsKey(key)) {
				this.sheet_child = children.get(key);
				
			}else {
				new ComposerThings.IInit(sg.getFolder("child").get(key), 448, 666);
				this.sheet_child = new RaceSheet(0);
			}
			Json child = data.json("CHILD");
			this.child = new RTypeSpec(colors, child);
		}
		
		{
			Json[] jjs = data.jsons("TYPES", 1, 4);
			
			ArrayList<RType> types = new ArrayList<>(jjs.length);
			
			for (Json j : jjs) {
				types.add(new RType(colors, j, sPortrait, extra, names));
			}
			
			this.types = types;
		}
		
		double bb = 0;
		for (RType t : types) {
			bb += t.spec.occurrence;
		}
		tMax = bb;
		
		off = (sheet.sheet.size() - hitboxSize)/2;
		
		{
			sheet_child_skelleton = skelleton("SPRITE_CHILD_SKELLETON_FILE", sg, data, skelleton);
			sheet_skelleton = skelleton("SPRITE_SKELLETON_FILE", sg, data, skelleton);
		}
		
		
	}
	
	private TILE_SHEET skelleton(String k, PATH sg, Json data, KeyMap<TILE_SHEET> skelleton) throws IOException {
		String key = data.value(k);
		if (!skelleton.containsKey(key)) {
			TILE_SHEET sh = new ITileSheet(sg.getFolder("skelleton").get(key), 316, 120) {
				
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
			skelleton.put(key, sh);
		}
		return skelleton.get(key);
	}


	public RaceSheet adult() {
		return sheet;
	}
	
	public RaceSheet child() {
		return sheet_child;
	}
	
	public RaceSheet sheet(Induvidual indu) {
		return indu.hType() == HTYPE.CHILD ? sheet_child : sheet;
	}
	
	public TILE_SHEET skelleton(Induvidual indu) {
		return indu.hType() == HTYPE.CHILD ? sheet_child_skelleton : sheet_skelleton;
	}
	
	public TILE_SHEET skelleton(boolean adult) {
		return !adult ? sheet_child_skelleton : sheet_skelleton;
	}
	
}
