package world.overlay;

import static world.WORLD.*;

import init.C;
import init.RES;
import init.sprite.SPRITES;
import snake2d.PathTile;
import snake2d.Renderer;
import snake2d.util.MATH;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import world.WORLD;
import world.regions.Region;

public final class ERegion{

	private Region hovered;
	private final COLOR cNone = new ColorImp(100, 100, 100);
	private final ColorImp col = new ColorImp();
	private double shade;
	public void add(Region r) {
		if (r != null) {
			hovered = r;
		}
	}

	public void renderAbove(Renderer r, ShadowBatch s, RenderData data) {
		if (hovered == null)
			return;
		renderAbove(hovered, r, s, data);
		renderPath(hovered, r, s, data);
		hovered = null;
	}
	
	public void renderAbove(Region hovered, Renderer r, ShadowBatch s, RenderData data) {
		s.setHeightUI(6);
		s.setDistance2GroundUI(10);
		s.setHard();
		shade = VIEW.renderSecond();
		shade = MATH.mod(shade, 2);
		shade = MATH.distanceC(shade, 1, 2);
		if (hovered.realm() == null)
			col.set(cNone);
		else
			col.set(hovered.faction().banner().colorBG());
		col.shadeSelf(0.5 + shade);
		col.bind();
		for (COORDINATE c : hovered.info.bounds()) {
			if (hovered.is(c)) {
				int m = 0;
				for (DIR d : DIR.ORTHO) {
					if (hovered.is(c, d) || !IN_BOUNDS(c, d)) {
						m |= d.mask();
					}
				}
				if (m != 0x0F) {
					int x = data.transformGX(c.x()*C.TILE_SIZE);
					int y = data.transformGY(c.y()*C.TILE_SIZE);
					SPRITES.cons().BIG.outline_dashed.render(r, m, x, y);
					SPRITES.cons().BIG.outline_dashed.render(s, m, x, y);
				}
			}
		}
		s.setPrev();
	}
	
	public void renderPath(Region reg, Renderer r, ShadowBatch s, RenderData data) {
		RES.coos().set(0);
		RES.flooder().init(this);
		RES.flooder().pushSloppy(reg.cx(), reg.cy(), 0);
		RES.flooder().setValue2(reg.cx(), reg.cy(), 0);
		COLOR.ORANGE100.bind();
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			Region now = WORLD.REGIONS().map.get(t);
			boolean edge = false;
			int md = WORLD.PATH().dirMap().get(t);
			for (DIR d : DIR.ALL) {
				if ((md & d.bit) != 0) {
					Region to = WORLD.REGIONS().map.get(t, d);
					if (now == to) {
						if (RES.flooder().pushSmaller(t, d, t.getValue()+ d.tileDistance(), t) != null)
							RES.flooder().setValue2(t, d, 0);
					}else
						edge = true;
				}
			}
			if (edge) {
				render(r, s, data, t);
				RES.coos().get().set(t);
				RES.coos().inc();
			}
		}
		RES.flooder().done();
		while(RES.coos().getI() > 0) {
			RES.coos().dec();
			render(RES.coos().get(), r, s, data);
		}
		COLOR.unbind();

	}
	
	private void render(COORDINATE start, Renderer r, ShadowBatch s, RenderData data) {
		RES.flooder().init(this);
		RES.flooder().pushSloppy(start, 0);
		Region reg = WORLD.REGIONS().map.get(start);
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			Region now = WORLD.REGIONS().map.get(t);
			if (now != null && t.isSameAs(now.cx(), now.cy())) {
				render(r, s, data, t);
				break;
			}
			int md = WORLD.PATH().dirMap().get(t);
			for (DIR d : DIR.ALL) {
				if ((md & d.bit) != 0) {
					Region to = WORLD.REGIONS().map.get(t, d);
					if (to == reg)
						continue;
					if (now == null || now == to || now == reg) {
						RES.flooder().pushSmaller(t, d, t.getValue()+ d.tileDistance(), t);
						RES.flooder().setValue2(t, d, 0);
					}
				}
			}
		}
		RES.flooder().done();

	}
	
	private void render(Renderer r, ShadowBatch s, RenderData data, PathTile t) {
		while(t.getParent() != null) {
			if (t.getValue2() == 1)
				break;
			int x = data.transformGX(t.x()*C.TILE_SIZE);
			int y = data.transformGY(t.y()*C.TILE_SIZE);
			DIR d = DIR.get(t.getParent(), t);
			SPRITES.cons().ICO.arrows2.get(d.id()).render(r, x, y);
			t.setValue2(1);
			t = t.getParent();
		}
	}

}
