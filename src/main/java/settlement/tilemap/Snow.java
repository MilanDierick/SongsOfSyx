package settlement.tilemap;

import static settlement.main.SETT.*;

import java.io.IOException;

import init.C;
import init.paths.PATHS;
import init.settings.S;
import settlement.main.RenderData;
import settlement.main.SETT;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.HeightMap;
import snake2d.util.sets.Bitsmap1D;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

final class Snow extends TileMap.Resource {

	private Bitsmap1D amount = new Bitsmap1D(0, 2, SETT.TAREA);
	private final TILE_SHEET sheet;
	
	Snow() throws IOException{ 
		new ComposerThings.IInit(PATHS.SPRITE_SETTLEMENT_MAP().get("Snow"), 972, 126);
		
		sheet = (new ITileSheet() {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				ComposerDests.Tile t = d.s24;
				final ComposerSources.Singles f = s.singles;
				f.init(0, 0, 1, 1, 16, 4, t);
				f.setVar(0).paste(true);
				return t.saveGame();

			}
		}).get();
		final HeightMap height = new HeightMap(TWIDTH, THEIGHT, 8, 8);
		for (int i = 0; i < TAREA; i++) {
			amount.set(i, (int) Math.round(height.get(i)*3));
		}
		
	}
	
	@Override
	protected void save(FilePutter saveFile) {
		amount.save(saveFile);
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		amount.load(saveFile);
	}

	@Override
	protected void clearAll() {

	}
	
	
	
	void render(Renderer r, RenderData data) {
		

		
		RenderData.RenderIterator i = data.onScreenTiles(1,1,1,1);
		
		double snow = SETT.WEATHER().snow.getD()*7.0;
		double ri = 1.0/0x0FFFF;
		if (S.get().graphics.get() == 0)
			snow*= 0.25;
		
		if (snow == 0)
			return;
		
		
		while(i.has()) {
			
			long ran = i.ran();
			
			double rr = (ran & 0x0FFFF)*ri;
			double s = snow;
			
			s -= amount.get(i.tile());
			s -= rr;
			if (s < 1)
				s -= rr;
			
			
			s-= SETT.PATH().huristics.getter.get(i.tile())*16*3;
		
			
			
			
			
		
			
			if (s >= 0 && !TERRAIN().get(i.tile()).roofIs() && !TERRAIN().get(i.tile()).isMassiveWall() && !SETT.ROOMS().placement.embryo.is(i.tile())) {
				int c = (int) (s);
				int m = SETT.ROOMS().map.is(i.tile()) || SETT.FLOOR().getter.get(i.tile()) != null || SETT.MINERALS().amountInt.get(i.tile()) > 0  ? 1 : 3;
				
				c = (int) CLAMP.d(c, 0, m);
				ran = ran >> 8;
				
				
				
				int d = (int) (((ran & 0x07) - 7) * C.SCALE);
				ran = ran >> 3;
				int x = i.x() + d;
				d = (int) (((ran & 0x07) - 7) * C.SCALE);
				ran = ran >> 3;
				int y = i.y() + d;
				{
					
					sheet.render(r, (int) (ran&0x0F)+c*16, x, y);
				
				}
			}
			i.next();
		}
		COLOR.unbind();
		
	}

}
