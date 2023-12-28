package init.race.appearence;

import java.io.IOException;

import init.race.ExpandInit;
import settlement.stats.Induvidual;
import snake2d.SPRITE_RENDERER;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

public final class RPortrait {

	public static final int P_WIDTH = RPortraitFrame.TILES_X*RPortraitFrame.TILE_SIZE;
	public static final int P_HEIGHT = RPortraitFrame.TILES_Y*RPortraitFrame.TILE_SIZE;
	private final PPP p;
	
	
	RPortrait(ExpandInit init, RColors colors, Json json) throws IOException{
		
		if (json.has("FACE")) {
			p = new New(init, colors, json);
			
		}else {
			p = new Old(init, colors, json);
		}
		
	}

	public void render(SPRITE_RENDERER r, int x1, int y1, Induvidual indu, int scale, boolean isDead) {
		p.render(r, x1, y1, indu, scale, isDead);
	}
	
	interface PPP {
		
		abstract void render(SPRITE_RENDERER r, int x1, int y1, Induvidual indu, int scale, boolean isDead);
		
	}
	
	private static class Old implements PPP {
		private RPortraitFrame[] frames;
		private final TILE_SHEET sheet;
		private final RaceFrameMaker fm;
		
		Old(ExpandInit init, RColors colors, Json json) throws IOException{
			fm = init.fm;
			if (!json.has("PORTRAIT")) {
				frames = new RPortraitFrame[0];
				sheet = null;
				return;
			}
			
			String sport = json.value("PORTRAIT_FILE");
			
			if (init.portraits.containsKey(sport)){
				sheet = init.portraits.get(sport);
			}else{
				sheet = new ITileSheet(init.sg.getFolder("portrait").get(sport), 416, 60) {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						int FRAMES = RPortraitFrame.FRAMES_X*c.getSource().height/60;
						FRAMES = CLAMP.i(FRAMES, 0, RPortraitFrame.FRAMES);
						s.full.init(0, 0, RPortraitFrame.FRAMES_X, FRAMES, RPortraitFrame.TILES_X, RPortraitFrame.TILES_Y, d.s8);
						for (int i = 0; i < FRAMES; i++)
							s.full.setVar(i).paste(true);
						return d.s8.saveGame();
						
						
					}
				}.get();
				init.portraits.put(sport, sheet);
			}
			
			Json[] js = json.jsons("PORTRAIT");
			frames = new RPortraitFrame[js.length];
			for (int i = 0; i < frames.length; i++) {
				frames[i] = new RPortraitFrame(colors, js[i], i);
			}
		}

		@Override
		public void render(SPRITE_RENDERER r, int x1, int y1, Induvidual indu, int scale, boolean isDead) {
			for (RPortraitFrame f : frames) {
				f.render(r, sheet, fm, x1, y1, indu, isDead, scale);
			}
		}
	}
	
	private static final class New implements PPP{

		private RPortraitFrameNew[] frames;
		
		New(ExpandInit init, RColors colors, Json json) throws IOException{
			
			Json[] js = json.jsons("FACE");
			frames = new RPortraitFrameNew[js.length];
			for (int i = 0; i < frames.length; i++) {
				frames[i] = new RPortraitFrameNew(init.fm, colors, js[i], i);
			}
		}

		@Override
		public void render(SPRITE_RENDERER r, int x1, int y1, Induvidual indu, int scale, boolean isDead) {
			for (RPortraitFrameNew f : frames) {
				f.render(r, x1, y1, indu, isDead, scale);
			}
		}
		
		
	}
	
	
}
