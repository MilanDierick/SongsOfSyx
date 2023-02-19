package view.world.ui.army;

import init.RES;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.Rec;
import util.colors.GCOLOR;
import util.colors.GCOLOR_UI;
import util.gui.misc.GMeter;
import util.gui.misc.GText;
import world.army.WDIV;
import world.entity.army.WArmy;

public final class DivCard {

	private final GText tmp = new GText(UI.FONT().S, 5);
	private final Rec body = new Rec();
	public static final int WIDTH = 44;
	public static final int HEIGHT = 64+18;
	private static final COLOR[] colors = new COLOR[] {
		COLOR.ORANGE100.makeSaturated(0.5).shade(0.75),
		COLOR.BLUE100.makeSaturated(0.5).shade(0.75),
		COLOR.WHITE20,
		COLOR.WHITE20,
	};


	public void render(SPRITE_RENDERER r, int x1, int y1, WDIV d, boolean isActive, boolean isSelected, boolean isHovered) {
		
		
		body.set(x1,x1+WIDTH, y1, y1+HEIGHT);
		GCOLOR.UI().border().render(r, body);
		
		GCOLOR_UI.color(colors[d.type()], isActive, isSelected, isHovered).render(r, body, -1);
		GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body, -3);
		
		if (d.men() == 0 || !isActive) {
			OPACITY.O50.bind();
		}
		
		int cx = body.cX();
		d.race().appearance().icon.render(r, cx-12, body.y1()+4);
		d.banner().render(r, cx-d.banner().width()/2, body.y1()+30);
		//SPRITES.armyCard().renderTraining(body.x1()+6, body.cY()-4, r, d.training());
		//SPRITES.armyCard().renderGear(body.x1()+6+10, body.cY()-4, r, d.gear());
		SPRITES.armyCard().renderPower(body.x1()+32, body.cY()-4, r, d.provess()/(STATS.BATTLE_BONUS().max()*d.men()));
		GMeter.renderDelta(r, (double)d.men()/RES.config().BATTLE.MEN_PER_DIVISION, (double)d.menTarget()/RES.config().BATTLE.MEN_PER_DIVISION, body.x1()+4, body.x2()-4, body.cY()+20, body.cY()+28);
		OPACITY.unbind();
		
	}
	
	public void renderArmy(SPRITE_RENDERER r, int x1, int y1, WDIV d, WArmy a, boolean isActive, boolean isSelected, boolean isHovered) {
		
		render(r, x1, y1, d, isActive, isSelected, isHovered);
		
		if (d.men() != d.menTarget() && d.daysUntilMenArrives() > 0) {
			SPRITES.icons().s.time.renderC(r, body.x2()-6, body.y1()+8);
			tmp.clear();
			if ((a != null && !a.acceptsSupplies()) || d.menTarget() == 0)
				tmp.errorify().add('-');
			else
				tmp.normalify().add(d.daysUntilMenArrives());
			tmp.adjustWidth();
			tmp.render(r, body.x2()-2-tmp.width(), body.y1()+16);
			
		}
		
	}
	
}
