package settlement.tilemap;

import java.io.IOException;

import game.GAME;
import init.C;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.settings.S;
import init.sound.SOUND;
import init.sound.SoundSettlement;
import init.sprite.SPRITES;
import settlement.main.RenderData;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.tilemap.Terrain.TerrainTile;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import snake2d.util.sprite.TileTexture.TileTextureScroller;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;

public final class TBush extends TerrainTile{
	

	private final TILE_SHEET sheet;
	private final static int SET = 16;
	
	private final TileTextureScroller dis1 = SPRITES.textures().dis_low.scroller(12*6, -12*5.5);
	
	private final TerrainClearing clearing = new TerrainClearing() {
		
		@Override
		public RESOURCE resource() {
			return RND.oneIn(3) ? RESOURCES.WOOD() : null;
		}
		
		@Override
		public boolean clear1(int tx, int ty) {
			shared.NADA.placeFixed(tx, ty);
			return false;
		}
		
		@Override
		public boolean can() {
			return true;
		}

		@Override
		public int clearAll(int tx, int ty) {
			shared.NADA.placeFixed(tx, ty);
			return 0;
		}
		
		@Override
		public SoundSettlement.Sound sound() {
			return SOUND.sett().action.dig;
		}
		
		@Override
		public boolean isEasilyCleared() {
			return true;
		};
		
	};
	
	TBush(Terrain t) throws IOException {
		super(t,"bush", SPRITES.icons().m.cancel, null);
		sheet = new ComposerThings.ITileSheet(PATHS.SPRITE_SETTLEMENT_MAP().get("Bush"), 716, 94) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(0, 0, 1, 1, 16, 4, d.s16);
				s.singles.paste(true);
				return d.s16.saveGame();
			}
		}.get();
		
	}

	@Override
	public TerrainClearing clearing() {
		return clearing;
	}
	
	@Override
	protected boolean place(int tx, int ty) {
		double v = 0;
		for (int i = 0; i < DIR.ALL.size(); i++) {
			DIR d = DIR.ALL.get(i);
			if (is(tx, ty, d) || shared.TREES.isTree(tx+d.x(), ty+d.y()))
				v+= 0.5;
		}
		int d = (int) (v-0.1);
		super.placeRaw(tx, ty);
		shared.data.set(tx, ty, d*SET);
		return false;
	}
	
	@Override
	protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		
		return false;
	}
	
	@Override
	protected final boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		
		render(i, r, s, i.x(), i.y(), i.ran(), data);
		i.countVegetation();
		return false;

	}
	
	void update(float ds) {
		double w = Math.pow(SETT.WEATHER().wind.getD(), 1.5);
		if (w > 0.1)
			dis1.update(ds*w);
	}

	
	private void render(RenderData.RenderIterator i, SPRITE_RENDERER r, ShadowBatch s, int x, int y, int ran, int data) {
		
//		{
//			int am = ran&7;
//			ran = ran >> 3;
//			x -= SETT.WEATHER().wind.getD()*am;
//			y += SETT.WEATHER().wind.getD()*am;
//		}
		
		
		
		int d = data/SET;
		data = CLAMP.i((int) ((d+1)*SETT.WEATHER().moisture.getD() + (i.ran()&0x03)), 0, d)*SET;
		
		int t = ran & 0x0F;
		t+= data;
		SETT.TERRAIN().colors.tree.get(ran >> 4).bind();
		if (t < 0 || t > SET*4)
			GAME.Notify("nono");
		sheet.render(r, t, x, y);
		
		if (S.get().graphics.get() > 0) {
			OPACITY.O50.bind();
			TextureCoords ti = SPRITES.textures().dots.get(i.tx(), i.ty(), 0, 0);
			CORE.renderer().renderDisplaced(x, x+C.TILE_SIZE, y, y+C.TILE_SIZE, dis1.get(i.tx(), i.ty()), ti);
			OPACITY.unbind();
		}
		
		
		s.setDistance2Ground(0).setHeight(2);
		sheet.render(s,  t, x, y);
		COLOR.unbind();
	}
	
	void render(RenderData.RenderIterator i, SPRITE_RENDERER r, ShadowBatch s, int x, int y, int ran) {
		int ss = (ran&3)*SET;
		ran = ran >> 2;
		render(i, r, s, x, y, ran, ss);
	}

	@Override
	public AVAILABILITY getAvailability(int x, int y) {
		return null;
	}
	
	@Override
	public boolean isPlacable(int tx, int ty) {
		return true;
	}
	

}
