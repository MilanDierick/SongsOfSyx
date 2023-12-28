package world.map.pathing;

import init.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.misc.IntChecker;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import world.WORLD;
import world.overlay.WorldOverlays;

final class DebugOverlay extends WorldOverlays.OverlayTile{

	private WComp hovered;
	private IntChecker check = new IntChecker(1);
	
	DebugOverlay() {
		super(true, false);
	}
	

	Str str = new Str(16);
	
	@Override
	public void renderAbove(Renderer r, ShadowBatch s, RenderData data) {
		
		hovered= WORLD.PATH().COMPS.get(mouse(data));
		
		
		if (hovered == null)
			return;
		
		if (hovered != null) {
			if (check.size() < WORLD.PATH().COMPS.maxID())
				check = new IntChecker(WORLD.PATH().COMPS.maxID());
			check.init();
		}
		
		for (int i = 0; i < hovered.edges(); i++) {
			check.isSetAndSet(hovered.edge(i).id);
		}
		
		GBox b = VIEW.hoverBox();
		b.NL();
		b.add(b.text().add(hovered.id));
		b.NL();
		for (int i = 0; i < hovered.edges(); i++) {
			b.add(b.text().add(hovered.edge(i).id));
			b.tab(4);
			b.add(b.text().add(hovered.dist(i)));
			b.NL();
		}
		b.NL();
		super.renderAbove(r, s, data);
	}
	
	@Override
	public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
		int id = WORLD.PATH().COMPS.ids().get(it.tile());
		
		if (WORLD.PATH().route.is(it.tile())) {
			COLOR.ORANGE100.bind();
			SPRITES.cons().BIG.line.render(r, 0, it.x(), it.y());
			COLOR.unbind();
		}
		if (id > 0) {
			if (hovered != null && check.isSet(id)) {
				COLOR.WHITE65.render(r, it.x(), it.x()+C.TILE_SIZE, it.y(), it.y()+C.TILE_SIZE);
			}
			COLOR.UNIQUE.getC(id).bind();
			str.clear().add(id);
			UI.FONT().H1.render(r, str, it.x(), it.y());
			
			COLOR.unbind();
		}
		hovered = null;
		
	}
	
}