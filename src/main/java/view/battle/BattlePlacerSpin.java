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
import snake2d.util.datatypes.VectorImp;
import util.gui.misc.GBox;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.battle.BattlePlacer.Action;
import view.battle.BattlePlacer.Mode;
import view.main.VIEW;
import view.subview.GameWindow;

final class BattlePlacerSpin extends Mode{

	
	
	private final GameWindow w;
	private final DivSelection s;
	private final DivFormation form = new DivFormation();
	private final Action a;
	
	public BattlePlacerSpin(GameWindow w, DivSelection s, Action a) {
		this.w = w;
		this.s = s;
		this.a = a;
	}
	

	
	private double cx,cy;
	private final VectorImp vec = new VectorImp();
	
	@Override
	void update(boolean hovered) {
		
		cx = 0;
		cy = 0;
		for (Div d : s.selection()) {
			d.order().dest.get(form);
			cx += form.start().x();
			cy += form.start().y();
		}
		cx /= s.selection().size();
		cy /= s.selection().size();
		
		if (!hovered)
			return;
		

		if (a.clickReleased) {

			for (Div d : s.selection()) {
				DivFormation f = getFor(d);
				if (f != null) {
					d.order().dest.set(f);
					DivTDataTask.TMP.move();
					d.order().task.set(DivTDataTask.TMP);
				}
				
				
				
			}
			if (VIEW.b().state() != null && VIEW.b().state().deploying()) {
				
				for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						a.teleportAndInitInDiv();
					}
				}
			}
			
		}
	}
	
	private DivFormation getFor(Div d) {
		d.order().dest.get(form);
		
		double newAngle = 0;
		{
			double destDX = w.pixel().x()-a.start.x();
			destDX/= (100<<w.zoomout());
			destDX %= Math.PI*2;
			newAngle = destDX;
		}
		
		double dist = vec.set(cx, cy, form.start().x(), form.start().y());
		
		vec.rotateRad(newAngle);
		
		double x1 = a.start.x() + vec.nX()*dist;
		double y1 = a.start.y() + vec.nY()*dist;

		vec.set(form.dx(), form.dy());
		vec.rotateRad(newAngle);

		return ARMIES().placer.deployer.deploy(d.menNrOf(), d.settings.formation, (int)x1, (int)y1, vec.nX(), vec.nY(), form.width(), d.army());
	
	}

	@Override
	void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
		
		VIEW.mouse().setReplacement(SPRITES.icons().m.rotate);
		
		
		if (a.clicked) {
			int x = w.pixel().x();
			int y = w.pixel().y();
			{
				if (ARMIES().placer.isBlocked(x, y, C.TILE_SIZE))
					SPRITES.cons().color.blocked.bind();
				else
					SPRITES.cons().color.ok.bind();
				SPRITES.cons().BIG.dots.renderCentered(r, 0, x -data.offX1(), y- data.offY1());
			}
			
			
			
			
			COLOR.unbind();
			for (Div d : s.selection())
				ARMIES().placer.render(r, getFor(d), data);
			return;
		}
		
		
		
	
	}

	@Override
	void hoverTimer(GBox text) {
		
	}


	
}
