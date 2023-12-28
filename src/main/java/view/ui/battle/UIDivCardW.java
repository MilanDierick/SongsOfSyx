package view.ui.battle;

import init.config.Config;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.misc.CLAMP;
import util.colors.GCOLOR;
import util.colors.GCOLOR_UI;
import util.gui.misc.GMeter;
import util.gui.misc.GMeter.GMeterCol;
import util.gui.misc.GText;
import world.army.*;
import world.entity.army.WArmy;

public final class UIDivCardW {

	private final GText tmp = new GText(UI.FONT().S, 5);
	private final Rec body = new Rec();
	public static final int WIDTH = 44;
	public static final int HEIGHT = 64+10;
	private static final COLOR[] colors = new COLOR[] {
		COLOR.ORANGE100.makeSaturated(0.5).shade(0.75),
		COLOR.BLUE100.makeSaturated(0.5).shade(0.75),
		COLOR.WHITE20,
		COLOR.WHITE20,
	};

	UIDivCardW() {
		// TODO Auto-generated constructor stub
	}
	

	public void render(SPRITE_RENDERER r, int x1, int y1, WDIV d, boolean isActive, boolean isSelected, boolean isHovered) {
		
		
		body.set(x1,x1+WIDTH, y1, y1+HEIGHT);
		GCOLOR.UI().border().render(r, body);
		
		if (d == null)
			throw new RuntimeException();
		
		GCOLOR_UI.color(colors[0], isActive, isSelected, isHovered).render(r, body, -1);
		GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body, -3);
		
		if (d.men() == 0 || !isActive) {
			OPACITY.O50.bind();
		}
		
		int cx = body.cX();
		d.race().appearance().icon.render(r, cx-12, body.y1()+6);
		d.banner().render(r, cx-d.banner().width()/2, body.y1()+30);
		//SPRITES.armyCard().renderTraining(body.x1()+6, body.cY()-4, r, d.training());
		//SPRITES.armyCard().renderGear(body.x1()+6+10, body.cY()-4, r, d.gear());
		SPRITES.armyCard().renderPower(body.x1()+32, body.cY()-4, r, d.provess()/(d.men()+1));
		
		
		int w = (int) ((body.width()-8)*CLAMP.d((double)(d.menTarget()+Config.BATTLE.MEN_PER_DIVISION/5)/Config.BATTLE.MEN_PER_DIVISION, 0, 1));
		
		GMeterCol col = GMeter.C_REDBLUE;
		
		
		GMeter.render(r, col, (double)d.men()/d.menTarget(), body.x1()+4, body.x1()+4+w, body.y2()-14, body.y2()-6);
		OPACITY.unbind();
		
		
	}
	
	public void render(SPRITE_RENDERER r, int x1, int y1, ADDiv d, boolean isActive, boolean isSelected, boolean isHovered) {
		
		
		body.set(x1,x1+WIDTH, y1, y1+HEIGHT);
		GCOLOR.UI().border().render(r, body);
		
		if (d == null)
			throw new RuntimeException();
		
		GCOLOR_UI.color(colors[d.type()], isActive, isSelected, isHovered).render(r, body, -1);
		GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body, -3);
		
		if (d.men() == 0 || !isActive) {
			OPACITY.O50.bind();
		}
		
		int cx = body.cX();
		d.race().appearance().icon.render(r, cx-12, body.y1()+6);
		d.banner().render(r, cx-d.banner().width()/2, body.y1()+30);
		//SPRITES.armyCard().renderTraining(body.x1()+6, body.cY()-4, r, d.training());
		//SPRITES.armyCard().renderGear(body.x1()+6+10, body.cY()-4, r, d.gear());
		SPRITES.armyCard().renderPower(body.x1()+32, body.cY()-4, r, d.provess()/(d.men()+1));
		
		
		int w = (int) ((body.width()-8)*CLAMP.d((double)(d.menTarget()+Config.BATTLE.MEN_PER_DIVISION/5)/Config.BATTLE.MEN_PER_DIVISION, 0, 1));
		
		GMeterCol col = GMeter.C_BLUE;
		
		WArmy a = d.army();

		if (d.needConscripts()) {
			if (d.army().acceptsSupplies()) {
				if (d.men() < d.menTarget()) {
					if (!AD.conscripts().canTrain(d.race(), d.faction())) {
						col = GMeter.C_REDBLUE;
					} else {
						SPRITES.icons().s.time.renderC(r, body.x2()-6, body.y1()+8);
						tmp.clear();
						if ((a != null && !a.acceptsSupplies()) || d.menTarget() == 0)
							tmp.errorify().add('-');
						else
							tmp.normalify().add(d.daysUntilMenArrives());
						tmp.adjustWidth();
						tmp.render(r, body.x2()-2-tmp.width(), body.y1()+16);
					}
				} else {
					int tt = UIDivHoverer.trainingTime(d);
					if (tt > 0) {
						
						SPRITES.icons().s.chevron(DIR.N).renderC(r, body.x2()-6, body.y1()+8);

					}

				}
			} else if (d.men() < d.menTarget() || UIDivHoverer.trainingTime(d) > 0) {
				COLOR.REDISH.bind();
				SPRITES.icons().s.chevron(DIR.N).renderC(r, body.x2()-6, body.y1()+8);
				COLOR.unbind();
			}

		}
		
		GMeter.render(r, col, (double)d.men()/d.menTarget(), body.x1()+4, body.x1()+4+w, body.y2()-14, body.y2()-6);
		OPACITY.unbind();
		
		
	}

	
}
