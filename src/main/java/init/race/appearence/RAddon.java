package init.race.appearence;

import java.io.IOException;

import init.race.ExpandInit;
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
	public final RCondition[] cons;
	
	RAddon(Json json, RColors colors, ExpandInit init) throws IOException{
		
		if (json.has("CONDITIONS")) {
			Json[] jj = json.jsons("CONDITIONS");
			cons = new RCondition[jj.length];
			for (int i = 0; i < jj.length; i++)
				cons[i] = new RCondition(jj[i]);
		}else {
			cons = new RCondition[0];
		}
		
		String k = json.value("SPRITE_FILE");
		
		if (!init.frames.containsKey(k)) {
			sheetStand = new ITileSheet(init.sg.getFolder("addon").get(k), 296, 44) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, 0, 1, 1, 2, 1, d.s24);
					s.singles.setSkip(0, 2).paste(3, true);
					return d.s24.saveGame();
				}
			}.get();
			
			sheetLay = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(s.singles.body().x2(), 0, 1, 1, 2, 1, d.s32);
					s.singles.setSkip(0, 2).paste(3, true);
					return d.s32.saveGame();
				}
			}.get();
			
			init.frames.put(k, this);
			
		}else {
			sheetStand = init.frames.get(k).sheetStand;
			sheetLay = init.frames.get(k).sheetLay;
		}
		
		if (json.has("COLOR"))
			col = colors.collection.getByKey("COLOR", json);
		else
			col = ColorCollection.DUMMY;
		
	}

	public void renderStanding(Renderer r, int dir, int x, int y, Induvidual in2, boolean dead) {
		render(sheetStand, r, dir, x, y, in2, dead);
	}
	
	public void renderLaying(Renderer r, int dir, int x, int y, Induvidual in2, boolean dead) {
		render(sheetLay, r, dir, x, y, in2, dead);
	}
	
	public void renderLayingTextured(TILE_SHEET stencil, int si, Renderer r, int dir, int x, int y, Induvidual in2, boolean dead) {
		
		for (RCondition c : cons) {
			if (!c.comp.passes(c.stat.getD(in2), c.compI))
				return;
		}
		col.get(in2, dead).bind();
		
		stencil.renderTextured(sheetLay.getTexture(dir), si, x, y);
		COLOR.unbind();
	}
	
	private void render(TILE_SHEET s, Renderer r, int dir, int x, int y, Induvidual in2, boolean dead) {
		
		for (RCondition c : cons) {
			if (!c.comp.passes(c.stat.getD(in2), c.compI))
				return;
		}
		
		col.get(in2, dead).bind();
		s.render(r, dir, x, y);
		COLOR.unbind();
	}
	
	public void renderLaying(Renderer r, int dir, int x, int y, Induvidual in2, boolean dead, COLOR cDecay, double decay) {

		for (RCondition c : cons) {
			if (!c.comp.passes(c.stat.getD(in2), c.compI))
				return;
		}
		
		ColorImp.TMP.interpolate(col.get(in2, dead), cDecay, decay);
		ColorImp.TMP.bind();
		sheetLay.render(r, dir, x, y);
		COLOR.unbind();
		
		render(sheetLay, r, dir, x, y, in2, dead);
	}
	
}
