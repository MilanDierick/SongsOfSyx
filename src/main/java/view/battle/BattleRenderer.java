package view.battle;

import static settlement.main.SETT.*;

import init.C;
import init.settings.S;
import init.sprite.SPRITES;
import settlement.army.Div;
import settlement.army.ai.divs.PathDiv;
import settlement.army.ai.util.DivTDataStatus;
import settlement.army.formation.DivFormation;
import settlement.army.formation.DivRenderer;
import settlement.army.order.DivTDataTask;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.ON_TOP_RENDERABLE;
import settlement.main.SETT;
import settlement.room.military.artillery.ArtilleryInstance;
import snake2d.*;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.SPRITE;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.keyboard.KEYS;

public final class BattleRenderer extends ON_TOP_RENDERABLE{

	private final DivSelection s;
	private final COLOR cHover = new ColorImp(0, 127, 0);
	private final COLOR cHoverEnemy = new ColorImp(127, 0, 0);
	private final DivTDataTask task = new DivTDataTask();
	private final DivTDataStatus status = new DivTDataStatus();
	
	public BattleRenderer(DivSelection s) {
		this.s = s;
	}
	
	@Override
	public void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
		remove();
		
//		{
//			RenderIterator it = data.onScreenTiles();
//			while(it.has()) {
//				if (ArmyAIUtil.space().cost.get(it.tile()) > 0)
//					SPRITES.cons().ICO.warning.render(r, it.x(), it.y());
//				it.next();
//			}
//		}
		
		//arrows
		if (r.getZoomout() < 3) {
			for (ENTITY e : SETT.ENTITIES().fill(data.gBounds())){
				if (e instanceof Humanoid) {
					Div d = ((Humanoid) e).division();
					if (d != null && (s.hovered(d) || s.selected(d))) {
						if (d.army() == SETT.ARMIES().enemy())
							cHoverEnemy.bind();
						else
							cHover.bind();
						int rx = e.body().cX() - data.offX1();
						int ry = e.body().cY() - data.offY1();
						DIR dir = d.position().dir(((Humanoid) e).divSpot());
						if (dir == null)
							dir = e.speed.dir();
						SPRITES.cons().ICO.arrows2.get(dir.id()).renderC(r, rx, ry);
					}
				}
			}
		}

		
		
		//target
		for (Div d : ARMIES().divisions()) {
			if (s.hovered(d) || s.selected(d)) {
				d.order().task.get(task);
				Div t = task.targetDiv();
				
				if (t != null && t.reporter.body().touches(data.gBounds())) {
					t.order().status.get(status);
					int x = status.currentPixelCX();
					int y = status.currentPixelCY();
					if (data.gBounds().holdsPoint(x, y)) {
						x -= data.offX1();
						y -= data.offY1();
						COLOR.RED2RED.bind();
						SPRITES.cons().ICO.crosshair.renderScaled(r, x-C.TILE_SIZE, y-C.TILE_SIZE, 2);
					}
				}
				
				COORDINATE c = task.targetTile();
				if (c != null && data.tBounds().touches(c.x(), c.y())) {
					int x = c.x()*C.TILE_SIZE;
					int y = c.y()*C.TILE_SIZE;
					x -= data.offX1();
					y -= data.offY1();
					COLOR.RED2RED.bind();
					SPRITES.cons().BIG.dots.render(r, 0, x, y);
				}
			}
		}
		
		for (ArtilleryInstance ins : s.artillery.all()) {
			
			
			
			if (ins.hovered) {
				if (ins.army() != SETT.ARMIES().player()) {
					cHoverEnemy.bind();
				}else {
					cHover.bind();
				}
			}else if (ins.selected) {
				cHover.bind();
			}else {
				continue;
			}
			if (data.tBounds().touches(ins.body())) {
				int x1 = ins.body().x1()*C.TILE_SIZE - data.offX1();
				int y1 = ins.body().y1()*C.TILE_SIZE - data.offY1();
				int w = ins.body().width()*C.TILE_SIZE;
				int h = ins.body().height()*C.TILE_SIZE;
				SPRITES.cons().BIG.outline.renderBox(r, x1, y1, w, h);
			}
			Div t = ins.targetDivGet();
			if (t != null && t.reporter.body().touches(data.gBounds())) {
				t.order().status.get(status);
				int x = status.currentPixelCX();
				int y = status.currentPixelCY();
				if (data.gBounds().holdsPoint(x, y)) {
					x -= data.offX1();
					y -= data.offY1();
					COLOR.RED2RED.bind();
					SPRITES.cons().ICO.crosshair.renderScaled(r, x-C.TILE_SIZE, y-C.TILE_SIZE, 2);
				}
			}
			COORDINATE coo = ins.targetCooGet();
			if (coo != null) {
				int x = coo.x();
				int y = coo.y();
				if (data.gBounds().holdsPoint(x, y)) {
					x -= data.offX1();
					y -= data.offY1();
					COLOR.RED2RED.bind();
					SPRITES.cons().ICO.crosshair.renderScaled(r, x-C.TILE_SIZE, y-C.TILE_SIZE, 2);
				}
			}
			
			
			
			
		}
		
		
		
		COLOR.unbind();
	}
	
	private final DivFormation tmp = new DivFormation();
	private final PathDiv pathDiv = new PathDiv();
	
	public void renderBelow(SPRITE_RENDERER ren, RenderData data) {
		//OPACITY.O50.bind();
	
		
		SPRITE s = CORE.renderer().getZoomout() >= 3 ? SPRITES.cons().TINY.flat.get(0) : SPRITES.icons().s.circle;
		for (Div d : SETT.ARMIES().divisions()) {
			COLOR.GREEN40.bind();
			if ((d.army() == SETT.ARMIES().player() || S.get().developer) && (this.s.hovered(d) || this.s.selected(d) || KEYS.BATTLE().SHOW_DIVISIONS.isPressed())) {
				
				if ( S.get().developer)
					DivRenderer.render(ren, d.position(), data);
				
				if (this.s.hovered(d)) {
					COLOR.WHITE100.bind();
				}else
					COLOR.WHITE50.bind();
				d.order().task.get(task);
				if (task.task().showDest || S.get().developer) {
					d.order().dest.get(tmp);
					DivRenderer.render(ren, tmp, data);
				}
				if (task.task().showPath || S.get().developer) {
					d.order().path.get(pathDiv);
					if (pathDiv.length() > 0) {
						COLOR.ORANGE100.bind();
						int curr = pathDiv.currentI();
						int k = curr > 0 ? curr-1 : curr;
						for(int i = k; i < pathDiv.length(); i++) {
							pathDiv.setCurrentI(i);
							int rx = pathDiv.x()-s.width()/2;
							int ry = pathDiv.y()-s.width()/2;
							rx -= data.offX1();
							ry -= data.offY1();
							
							s.render(ren, rx, ry);									
						}
						pathDiv.setCurrentI(curr);
						
					}
				}
				
				COLOR.unbind();
				
				
			}
		}
		COLOR.unbind();
	}

}
