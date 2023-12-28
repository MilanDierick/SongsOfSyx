package init.race.appearence;

import java.io.IOException;

import game.values.GVALUES;
import game.values.Lockable;
import init.race.appearence.RColors.ColorCollection;
import settlement.stats.Induvidual;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

public final class RAddon {

	public final ColorCollection col;
	public final TILE_SHEET sheetStand;
	public final TILE_SHEET sheetLay;
	public final Lockable<Induvidual> cons = GVALUES.INDU.LOCK.push();
	
	RAddon(Json json, RColors colors, RAddon[] done) throws IOException{
		
		cons.push("CONDITIONS", json);
		if (json.has("COLOR"))
			col = colors.collection.getByKey("COLOR", json);
		else
			col = ColorCollection.DUMMY;
		int ii = json.i("ADDON_INDEX", 0, 8);
		
		if (done[ii] != null) {
			this.sheetStand = done[ii].sheetStand;
			this.sheetLay = done[ii].sheetLay;
			return;
		}
		
		final int y1 = 194 + 44*ii;
		final int x1 = 66;
		
		sheetStand = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(x1, y1, 1, 1, 2, 1, d.s24);
				s.singles.setSkip(0, 2).paste(3, true);
				return d.s24.saveGame();
			}
		}.get();
		
		sheetLay = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(s.singles.body().x2(), y1, 1, 1, 2, 1, d.s32);
				s.singles.setSkip(0, 2).paste(3, true);
				return d.s32.saveGame();
			}
		}.get();
		
		
	}

	public void renderStanding(Renderer r, int dir, int x, int y, Induvidual in2, boolean dead) {
		render(sheetStand, r, dir, x, y, in2, dead);
	}
	
	public void renderLaying(Renderer r, int dir, int x, int y, Induvidual in2, boolean dead) {
		render(sheetLay, r, dir, x, y, in2, dead);
	}
	
	public void renderLayingTextured(TILE_SHEET stencil, int si, Renderer r, int dir, int x, int y, Induvidual in2, boolean dead) {
		
		if (!cons.passes(in2))
			return;
		
		col.get(in2, dead).bind();
		
		stencil.renderTextured(sheetLay.getTexture(dir), si, x, y);
		COLOR.unbind();
	}
	
	private void render(TILE_SHEET s, Renderer r, int dir, int x, int y, Induvidual in2, boolean dead) {
		
		if (!cons.passes(in2))
			return;
		
		col.get(in2, dead).bind();
		s.render(r, dir, x, y);
		COLOR.unbind();
	}
	
	public void renderLaying(Renderer r, int dir, int x, int y, Induvidual in2, boolean dead, COLOR cDecay, double decay) {

		if (!cons.passes(in2))
			return;
		
		ColorImp.TMP.interpolate(col.get(in2, dead), cDecay, decay);
		ColorImp.TMP.bind();
		sheetLay.render(r, dir, x, y);
		COLOR.unbind();
		
		render(sheetLay, r, dir, x, y, in2, dead);
	}
	
}
