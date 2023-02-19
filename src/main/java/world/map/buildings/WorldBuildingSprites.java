
package world.map.buildings;

import java.io.IOException;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

public class WorldBuildingSprites {

	private final PATH getter = PATHS.SPRITE().getFolder("world").getFolder("buildings");
	
	public final TILE_SHEET houses = new ITileSheet(getter.getFolder("houses").get("Normal"), 460, 62) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.singles.init(0, 0, 1, 1, 16, 4, d.s8);
			s.singles.paste(true);
			return d.s8.saveGame();
		}
	}.get();
	public final TILE_SHEET farms = new ITileSheet(getter.get("Farms"), 364, 100) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.singles.init(0, 0, 1, 1, 8, 4, d.s16);
			s.singles.paste(true);
			return d.s16.saveGame();
		}
	}.get();
	public final TILE_SHEET mines = new ITileSheet(getter.get("Mines"), 364, 100) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.singles.init(0, 0, 1, 1, 8, 1, d.s16);
			s.singles.paste(true);
			return d.s16.saveGame();
		}
	}.get();
	
	public final TILE_SHEET roads = new ITileSheet(getter.get("Roads"), 576, 140) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			int var = 4;
			int am = 3;
			s.house.init(0, 0, var, am, d.s16);
			s.house.paste(true);
			for (int i = 0; i < am*var; i++)
				s.house.setVar(i).paste(true);
			return d.s16.saveGame();
		}
	}.get();
	
	public final TILE_SHEET wallCity = new ITileSheet(getter.get("Wall"), 600, 120) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			final ComposerSources.Full f = s.full;
			f.init(0, 0, 5, 2, 3, 3, t);
			for (int i = 0; i < 10; i++)
				f.setVar(i).paste(true);
			return t.saveGame();
			
		}
	}.get();
	
	public final TILE_SHEET siege = new ITileSheet(getter.get("Siege"), 120, 60) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			final ComposerSources.Full f = s.full;
			f.init(0, 0, 1, 1, 3, 3, t);
			f.paste(true);
			return t.saveGame();
			
		}
	}.get();
	
	public final TILE_SHEET centre = new ITileSheet(getter.get("Keep"), 252, 36) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.singles.init(0, 0, 1, 1, 4, 1, d.s24);
			s.singles.paste(true);
			return d.s24.saveGame();
		}
	}.get();

	
	
	WorldBuildingSprites() throws IOException {
		
		
		
	}
	
	
	
}

