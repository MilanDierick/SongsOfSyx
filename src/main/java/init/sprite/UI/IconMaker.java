package init.sprite.UI;

import java.io.IOException;

import game.GAME;
import init.paths.PATH;
import init.paths.PATHS;
import init.sprite.UI.Icon.IconSheet;
import snake2d.LOG;
import snake2d.util.color.COLOR;
import snake2d.util.file.Json;
import snake2d.util.file.SnakeImage;
import snake2d.util.sets.KeyMap;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerDests.Tile;
import util.spritecomposer.ComposerThings.ITileSheet;

public class IconMaker {

	public static final String split = "->";
	
	final PATH path;
	private KeyMap<TILE_SHEET> map = new KeyMap<>();
	public final int DIM;
	public final Icon DUMMY;
	private boolean hasComplained = false;
	
	IconMaker(String root, int dim){
		path = PATHS.SPRITE().getFolder("icon").getFolder(root);
		this.DIM = dim;
		DUMMY = new Icon(DIM, COLOR.ORANGE100);
	}
	
	public Icon get(String relPath, int nr) throws IOException {
		
		TILE_SHEET sheet = sheet(relPath, null);
		if (sheet == null)
			return DUMMY;
		
		if (nr >= sheet.tiles()) {
			GAME.Warn(relPath + " x or y is out of bounds!" + " " + nr);
			return DUMMY;
		}
		
		return new IconSheet(DIM, sheet, nr);
		
	}
	
	public Icon get(Json j, String key, String relPath) throws IOException {
		

		String[] ss = relPath.split(split);
		
		
		
		if (ss.length < 2) {
			GAME.Warn(j.errorGet("is badly formatted. Needs to contain a path with separation denoted by -> and the final entry being a number indicating which icon to pick of the sheet", key));
			return DUMMY;
		}
		
		try {
			int nr = Integer.parseInt(ss[ss.length-1]);
			String rPath = relPath.substring(0, relPath.length()-ss[ss.length-1].length());
			TILE_SHEET sheet = sheet(rPath, j);
			if (sheet == null) {
				return DUMMY;
			}
			if (nr >= sheet.tiles()) {
				GAME.Warn(file(rPath, j) + " does not have an icon at index: " + nr);
				return DUMMY;
			}
			return new IconSheet(DIM, sheet, nr);
		}catch(NumberFormatException e) {
			GAME.Warn(j.errorGet(relPath + " '" +ss[ss.length-1] + "' is badly formatted. Needs to end with  ->X where X is a number indicating which icon to pick of the sheet", key));
		}
		return DUMMY;
		
	}
	
	private void complain(PATH p, String file, Json json) {
		String err = p.get() + "/" + file + " does not contain the icon image file: " + file + System.lineSeparator();
		if (!hasComplained) {
			String available = "";
			for (String s : p.getFiles()) {
				available += s + "," + System.lineSeparator();
			}
			
			if (json != null)
				err += json.path() + System.lineSeparator();
			err += "Available: " + System.lineSeparator() + available;
			GAME.Warn(err);
			hasComplained = true;
		}else {
			if (json != null)
				err += json.path() + System.lineSeparator();
			LOG.ln(err);
		}
	}
	
	private String file(String relPath, Json json) {
		String[] pp = relPath.split(split);
		PATH p = path;
		for (int i = 0; i < pp.length-1; i++) {
			if (!p.existsFolder(pp[i])) {
				complain(p, pp[i], json);
				return null;
			}
			p = p.getFolder(pp[i]);
		}
		return pp[pp.length-1];
	}
	
	protected TILE_SHEET sheet(String relPath, Json json) throws IOException {
		
		if (!map.containsKey(relPath)) {
			String[] pp = relPath.split(split);
			PATH p = path;
			for (int i = 0; i < pp.length-1; i++) {
				if (!p.existsFolder(pp[i])) {
					complain(p, pp[i], json);
					return null;
				}
				p = p.getFolder(pp[i]);
			}
			String file = file(relPath, json);
			
			if (!p.exists(file)) {
				complain(p, file, json);
				
				return null;
			}
			
			
			
			SnakeImage im = new SnakeImage(p.get(file));
			final int iwidth = im.width/2;
			final int iheight = im.height;
			im.dispose();
			
			if ((iwidth-6)%(DIM+6) != 0 || (iheight-6)%(DIM+6) != 0) {
				GAME.Warn(p.get(file) + " does not have the right dimensions: Should be a multiple of " + DIM + " squares. Look at other file for reference.");
				return null;
			}
			
			int xs = (iwidth-6)/(DIM+6);
			int ys = (iheight-6)/(DIM+6);
			
			TILE_SHEET s = new ITileSheet(p.get(file), iwidth*2, iheight) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					Tile t = d.s16;
					if (DIM == 24)
						t = d.s24;
					if (DIM == 32)
						t = d.s32;
					s.singles.init(0, 0, 1, 1, xs, ys, t);
					s.singles.paste(true);
					return t.saveGame();
				}
			}.get();
			map.put(relPath, s);
			
		}
		return map.get(relPath);
	}

	

	
}
