package view.ui.tech;

import game.faction.FACTIONS;
import init.boostable.*;
import init.sprite.UI.UI;
import init.tech.TECH;
import init.tech.TECHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE.HoverableAbs;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;

final class InfoBonuses extends ISidePanel{

	static TECH hovered;
	
	public InfoBonuses() {
		
		titleSet("");
		
		LinkedList<RENDEROBJ> rens = new LinkedList<>();
		
		for (BoostableCollection col : BOOSTABLES.colls()) {
			rens.add(new GHeader(col.name));
			for (BOOSTABLE b : col.all()) {
				rens.add(new Boo(b));
			}
			
		}
		
		section = new GuiSection() {
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				
				super.render(r, ds);
				hovered = null;
			}
		};
		
		section.add(new GScrollRows(rens, HEIGHT).view());
		
	}
	
	private final GText t = new GText(UI.FONT().S, 20);
	
	private class Boo extends HoverableAbs{

		private final BOOSTABLE bo;
		
		public Boo(BOOSTABLE bo) {
			super(264, 18);
			this.bo = bo;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			bo.icon().render(r, body().x1(), body().y1());
			if (hovered != null) {
				for (BBoost b : hovered.boosts()) {
					if (b.boostable == bo)
						isHovered = true;
				}
			}
			
			if (!isHovered)
				OPACITY.O66.bind();
			GCOLOR.T().H2.bind();
			UI.FONT().S.render(r, bo.name, body().x1()+20, body().y1(), 0, bo.name.length() > 15 ? 15 : bo.name.length(), 1);
			COLOR.unbind();
			OPACITY.unbind();
			t.clear();
			GFORMAT.percInc(t, FACTIONS.player().tech.BOOSTER.mul(bo)*(1.0+FACTIONS.player().tech.BOOSTER.add(bo)));
			t.render(r, body.x1()+200, body.y1());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			text.title(bo.name);
			text.text(bo.desc);
			text.NL(8);
			
			GBox box = (GBox) text;
			for (TECH t : TECHS.ALL()) {
				for (BBoost b : t.boosts()) {
					if (b.boostable == bo) {
						SPRITE c = FACTIONS.player().tech.level(t) > 0 ? GCOLOR.T().IGOOD : GCOLOR.T().INACTIVE;
						box.add(c);
						b.hover(box);
					}
				}
				
			}
			
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				VIEW.UI().tech.tree.hoveredBoost = bo;
				return true;
			}
			return false;
		}
		
	}
	
}
