package view.battle;

import static settlement.main.SETT.*;

import init.C;
import init.sprite.SPRITES;
import settlement.army.Div;
import settlement.army.formation.DivFormation;
import settlement.army.order.DivTDataTask;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import util.gui.misc.GBox;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.battle.BattlePlacer.Action;
import view.battle.BattlePlacer.Mode;
import view.main.VIEW;
import view.subview.GameWindow;

public final class BattlePlacerPlace extends Mode{

	private final GameWindow w;
	final DivSelection s;
	private final Action a;
	private final DivFormation ff = new DivFormation();
	public BattlePlacerPlace(GameWindow w, DivSelection s, Action a) {
		this.w = w;
		this.s = s;
		this.a = a;
	}
	


	
	@Override
	void update(boolean hovered) {
		if (!hovered)
			return;

		
		
		if (a.clickReleased) {
			
			
			ARMIES().placer.deploy(s.selection(), a.start.x(), w.pixel().x(), a.start.y(), w.pixel().y());
			if (VIEW.b().state() != null && VIEW.b().state().deploying()) {
			
				for (Div d : s.selection()) {
					d.order().dest.get(ff);
					d.position().copy(ff);
					DivTDataTask.TMP.move();
					d.order().task.set(DivTDataTask.TMP);
					d.initPosition();
				}
				
				for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						a.teleportAndInitInDiv();
					}
				}
			}
			
		}
	}

	@Override
	void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
		
		if (!a.clicked) {
			int x = w.pixel().x();
			int y = w.pixel().y();
			if (ARMIES().placer.isBlocked(x, y, C.TILE_SIZE))
				SPRITES.cons().color.blocked.bind();
			else
				SPRITES.cons().color.ok.bind();
			SPRITES.cons().BIG.dots.renderCentered(r, 0, x -data.offX1(), y- data.offY1());
			VIEW.mouse().setReplacement(SPRITES.icons().m.place_line);
			
			COLOR.unbind();
			return;
		}else {
			ARMIES().placer.render(r, s.selection(), a.start.x(), w.pixel().x(), a.start.y(), w.pixel().y(), data);
		}
		
		
		
		
		
	
	}

	@Override
	void hoverTimer(GBox text) {
		
	}



	
}
