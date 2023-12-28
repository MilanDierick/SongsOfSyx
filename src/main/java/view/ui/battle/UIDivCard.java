package view.ui.battle;

import init.config.Config;
import init.sprite.SPRITES;
import settlement.army.Div;
import settlement.army.DivMorale;
import settlement.army.DivisionBanners.DivisionBanner;
import settlement.army.formation.DivFormation;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.equip.EquipRange;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.DIMENSION;
import util.colors.GCOLOR;
import util.colors.GCOLOR_UI;
import util.gui.misc.GMeter;
import world.army.AD;

public final class UIDivCard implements DIMENSION{

	public final int width = 50;
	public final int height = 64+20;

	private final DivFormation forCurrent;
	private final DivFormation forDest;
	
	UIDivCard(){
		forCurrent = new DivFormation();
		forDest = new DivFormation();
	}
	
	@Override
	public int width() {
		return width;
	}

	@Override
	public int height() {
		return height;
	}
	
	private static COLOR cMoraleWorst = new ColorShifting(COLOR.WHITE10, new ColorImp(40, 0, 0)).setSpeed(2);
	private static COLOR cMoraleBad = new ColorShifting(COLOR.WHITE10, new ColorImp(40, 25, 0)).setSpeed(1.5);
	
	public void render(Div div, int x1, int y1, SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
			boolean isHovered) {
		
		DivisionBanner b = SETT.ARMIES().banners.get(div.info.symbolI());
		GCOLOR.UI().border().render(r, x1, y1, width, height, 0);
		{
			COLOR c = COLOR.WHITE20;
			if (isSelected)
				c = COLOR.WHITE85;
			else if (isHovered) {
				c = COLOR.WHITE65;
			}
			c.render(r, x1, y1, width, height, -1);
		}
		GCOLOR_UI.color(b.bg, true, isSelected, isHovered).render(r, x1, y1, width, height, -3);
		
		{
			double morale = div.morale.get();
			COLOR c = COLOR.WHITE10;
			if (morale <= 0.1) {
				c = cMoraleWorst;
			}else if(morale < 0.5) {
				c = cMoraleBad;
			}
			c.render(r, x1, y1, width, height, -3, -5);
		}
		

		{
			int cx = x1+width/2;
			div.info.race().appearance().icon.render(r, cx-12, y1+6);
			b.renderSymbol(r, cx-b.width()/2, y1+32);
		}
		
		{
			double d = AD.UTIL().power.get(div);
			SPRITES.armyCard().renderPower(x1+width-10, y1+18, r, d);
		}
		
		if (hasAmmo(div)){
			int y = y1+height-20;
//			double m = 0;
//			for (StatEquippableRange a : STATS.EQUIP().ammo()) {
//				m = Math.max(a.ammunition.div().getD(div), m);
//			}
			EquipRange ra = div.settings.ammo();
			double m = 0;
			if (ra != null)
				m = ra.ammunition.div().getD(div);
			ColorImp.TMP.set(COLOR.YELLOW100);
			ColorImp.TMP.shadeSelf(0.25);
			ColorImp.TMP.render(r, x1+4, (int) (x1+4+(width-8)), y, y+6);
			ColorImp.TMP.set(COLOR.YELLOW100);
			ColorImp.TMP.render(r, x1+4, (int) (x1+4+Math.ceil((width-8)*m)), y+1, y+6);
		}
		

		
		
		
		GMeter.renderDelta(r, (double)(div.menNrOf()-DivMorale.CASULTIES.getD(div)-DivMorale.DESERTION.getD(div))/Config.BATTLE.MEN_PER_DIVISION, (double)div.menNrOf()/Config.BATTLE.MEN_PER_DIVISION, x1+2, x1+width-2, y1+height-14, y1+height-4);
		OPACITY.unbind();
		
		
		
		if (!isActive || div.menNrOf() <= 0) {
			OPACITY.O50.bind();
			COLOR.BLACK.render(r, x1, y1, width, height, -1);
			OPACITY.unbind();
		}else {
			div.order().next.get(forCurrent);
			div.order().dest.get(forDest);
			if (forCurrent.deployed() > 0 && forDest.deployed() > 0 && !forCurrent.centrePixel().isSameAs(forDest.centrePixel())) {
				int i = 3;
				if (div.settings.running)
					i++;
				SPRITES.armyCard().renderThing(r, i, x1, y1+3);
				
			}
			
			if (div.settings.isFighting()) {
				SPRITES.armyCard().renderThing(r, 5, x1+14, y1+3);
				
			}
			
			
			if (div.settings.shouldFire()) {
				SPRITES.armyCard().renderThing(r, 6, x1+28, y1+3);
			}
		}
	}
	
	public boolean hasAmmo(Div div) {
		for (int k = 0; k < STATS.EQUIP().RANGED().size(); k++) {
			EquipRange a = STATS.EQUIP().RANGED().get(k);
			if (a.stat().div().get(div) > 0) {
				return true;
			}
		}
		return false;
	}
	
}
