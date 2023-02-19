package init.sprite;

import java.io.IOException;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.sprite.TileTexture;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileTexture;

public final class Textures {

	public final TileTexture dis_big;
	public final TileTexture dis_weird;
	public final TileTexture dis_small;
	public final TileTexture dis_tiny;
	public final TileTexture fire;
	public final TileTexture leafs;
	public final TileTexture water;
	public final TileTexture water2;
	public final TileTexture water3;
	public final TileTexture dots;
	public final TileTexture dis_low;
	
	
	Textures() throws IOException{
		
		PATH path = PATHS.SPRITE().getFolder("misc");
		
		dis_big = new ITileTexture(8,8, path.get("Displacement"), 560, 188) {
			
			@Override
			protected
			SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d, ComposerTexturer t) {
				return t.paste(0, 0, 8, 8);
			}
		}.get();
		
		dis_weird = new ITileTexture(8, 8) {
			
			@Override
			protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d, ComposerTexturer t) {
				return t.paste(t.body().x2(), t.body().y1(), 8, 8);
			}
		}.get();
		
		dis_small = new ITileTexture(8, 8) {
			
			@Override
			protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d, ComposerTexturer t) {
				return t.paste(0, t.body().y2(), 8, 8);
			}
		}.get();
		
		dis_tiny = new ITileTexture(8, 8) {
			
			@Override
			protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d, ComposerTexturer t) {
				return t.paste(t.body().x2(), t.body().y1(), 8, 8);
			}
		}.get();
		
		fire = new ITileTexture(8,8, path.get("Textures"), 560, 188) {
			
			@Override
			protected
			SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d, ComposerTexturer t) {
				return t.paste(0, 0, 8, 8);
			}
		}.get();
		
		leafs = new ITileTexture(8, 8) {
			
			@Override
			protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d, ComposerTexturer t) {
				return t.paste(t.body().x2(), 0, 8, 8);
			}
		}.get();
		
		water = new ITileTexture(8, 8) {
			
			@Override
			protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d, ComposerTexturer t) {
				return t.paste(0, t.body().y2(), 8, 8);
			}
		}.get();
		
		water2 = new ITileTexture(8, 8) {
			
			@Override
			protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d, ComposerTexturer t) {
				return t.paste(t.body().x2(), t.body().y1(), 8, 8);
			}
		}.get();
		
		path = PATHS.SPRITE().getFolder("textures");
		
		dots = new ITileTexture(8,8, path.get("Dots"), 280, 140) {
			
			@Override
			protected
			SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d, ComposerTexturer t) {
				return t.paste(0, 0, 8, 8);
			}
		}.get();
		
		dis_low = new ITileTexture(8,8, path.get("Displacement_low"), 280, 140) {
			
			@Override
			protected
			SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d, ComposerTexturer t) {
				return t.paste(0, 0, 8, 8);
			}
		}.get();
		
		water3 = new ITileTexture(8,8, path.get("Water"), 280, 140) {
			
			@Override
			protected
			SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d, ComposerTexturer t) {
				return t.paste(0, 0, 8, 8);
			}
		}.get();
		
	}
	
}
