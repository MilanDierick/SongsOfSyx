package init.sprite.UI;

import java.io.IOException;

import init.config.Config;
import init.paths.PATHS;
import settlement.army.Div;
import settlement.army.DivMorale;
import settlement.army.DivisionBanners.DivisionBanner;
import settlement.army.formation.DivFormation;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.DIMENSION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.TILE_SHEET;
import util.colors.GCOLOR;
import util.colors.GCOLOR_UI;
import util.gui.misc.GMeter;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

public final class UIArmyCard implements DIMENSION {

	private final int width = 44;
	private final int height = 64+18;
	
	private final TILE_SHEET decorations;
	
	
	private final COLOR cPower = new ColorImp(114, 84, 33).shade(0.7);
	private static DivFormation forCurrent;
	private static DivFormation forDest;
		
	public UIArmyCard() throws IOException{
		
		new ITileSheet(PATHS.SPRITE_UI().get("ArmyCard"), 296, 108) {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, 0, 1, 1, 1, 3, d.s32);
				s.full.setVar(0);
				s.full.paste(true);
				return d.s32.saveGui();
			}
		}.get();
		new ITileSheet() {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(s.full.body().x2(), s.full.body().y1(), 1, 1, 1, 3, d.s32);
				s.full.setVar(0);
				s.full.paste(true);
				return d.s32.saveGui();
			}
		}.get();
		decorations = new ITileSheet() {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(s.full.body().x2(), s.full.body().y1(), 1, 1, 3, 3, d.s16);
				s.full.setVar(0);
				s.full.paste(true);
				return d.s16.saveGui();
			}
		}.get();
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
		
		if (forCurrent == null) {
			forCurrent = new DivFormation();
			forDest = new DivFormation();
		}
		
		
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
		GCOLOR_UI.color(b.bg, true, isSelected, isHovered).render(r, x1, y1, width, height, -2);
		
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
			double d = STATS.BATTLE_BONUS().power(div)/STATS.BATTLE_BONUS().max();
			renderPower(x1+width-10, y1+18, r, d);
		}
		
		if (div.settings.ammo() != null){
			int y = y1+height-18;
//			double m = 0;
//			for (StatEquippableRange a : STATS.EQUIP().ammo()) {
//				m = Math.max(a.ammunition.div().getD(div), m);
//			}
			double m = div.settings.ammo().ammunition.div().getD(div);
			if (m > 0) {
				ColorImp.TMP.set(COLOR.YELLOW100);
				ColorImp.TMP.shadeSelf(0.5);
				ColorImp.TMP.render(r, x1+4, (int) (x1+4+(width-8)*m), y, y+4);
				ColorImp.TMP.set(COLOR.YELLOW100);
				ColorImp.TMP.render(r, x1+4, (int) (x1+4+(width-8)*m), y+1, y+3);
			}else {
				COLOR.WHITE15.render(r, x1+4, (int) (x1+4+(width-8)*m), y, y+4);
				COLOR.WHITE15WHITE50.render(r, x1+4, (int) (x1+4+(width-8)*m), y+1, y+3);
			}
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
				decorations.render(r, i, x1, y1+3);
				
			}
			
			if (div.settings.isFighting()) {
				decorations.render(r, 5, x1+14, y1+3);
				
			}
			
			
			if (div.settings.shouldFire()) {
				decorations.render(r, 6, x1+28, y1+3);
			}
		}
	}
	
	public void renderPower(int x1, int y1, SPRITE_RENDERER r, double l) {
		renderThing(x1, y1, r, l, 2);
	};
	
	
	private void renderThing(int x1, int y1, SPRITE_RENDERER r, double l, int v) {
		int i = CLAMP.i((int) (l*7), 0, 7);
		if (i == 0)
			return;
		
		cPower.bind();
		int am = i;
		for (int k = 0; k <= am; k++) {
			decorations.render(r, v, x1, y1+6*k);
		}
		COLOR.unbind();
	}


}