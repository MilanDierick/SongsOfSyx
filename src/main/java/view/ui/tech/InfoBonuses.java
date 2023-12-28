package view.ui.tech;

import game.boosting.*;
import game.faction.FACTIONS;
import init.sprite.UI.UI;
import init.tech.TECH;
import init.tech.TECHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LinkedList;
import util.colors.GCOLOR;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.main.VIEW;

final class InfoBonuses extends GuiSection{

	static TECH hovered;
	
	public InfoBonuses(int height) {
		
		
		
		
		LinkedList<RENDEROBJ> rens = new LinkedList<>();
		
		BoostableCat cat = null;
		for (Boostable b : BOOSTING.ALL()) {
			if (b.name != null && b.name.length() > 0) {
				if (b.cat != cat) {
					cat = b.cat;
					rens.add(new GHeader(cat.name));
				}
				rens.add(new Boo(b));
			}
			
			
		}
		
		add(new GScrollRows(rens, height-8).view());
		
		addRelBody(8, DIR.W, new RENDEROBJ.RenderImp(2, height) {

			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				GCOLOR.UI().border().render(r, body);
			}
		});
		
	}
	
	private final GText t = new GText(UI.FONT().S, 20);
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
		super.render(r, ds);
		hovered = null;
	}
	
	private class Boo extends HoverableAbs{

		private final Boostable bo;
		private ArrayListGrower<TECH> techs = new ArrayListGrower<>();
		
		public Boo(Boostable bo) {
			super(300, 18);
			this.bo = bo;
			for (TECH t : TECHS.ALL()) {
				for (BoostSpec b : t.boosters.all()) {
					if (b.boostable == bo) {
						techs.add(t);
					}
				}
			}
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			if (techs.size() == 0)
				OPACITY.O50.bind();
			else if (!isHovered)
				OPACITY.O85.bind();
			
			bo.icon.render(r, body().x1(), body().y1());
			if (hovered != null) {
				for (BoostSpec b : hovered.boosters.all()) {
					if (b.boostable == bo)
						isHovered = true;
				}
			}
			
			if (techs.size() > 0)
				GCOLOR.T().H1.bind();
			UI.FONT().S.render(r, bo.name, body().x1()+20, body().y1(), 0, bo.name.length() > 15 ? 15 : bo.name.length(), 1);
			COLOR.unbind();
			OPACITY.unbind();
			t.clear();
			double add = 0;
			double mul = 1;
			for (TECH t : techs) {
				for (BoostSpec b : t.boosters.all()) {
					if (b.boostable == bo) {
						if (b.booster.isMul)
							mul *= FACTIONS.player().tech.level(t)*(b.booster.to()-1) + 1;
						else
							add += FACTIONS.player().tech.level(t)*(b.booster.to());
						
					}
				}
			}
			
			GFORMAT.percInc(t, (add+1)*mul-1);
			t.render(r, body.x1()+220, body.y1());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			text.title(bo.name);
			text.text(bo.desc);
			text.NL(8);
			
			GBox box = (GBox) text;
			for (TECH t : techs) {
				for (BoostSpec b : t.boosters.all()) {
					if (b.boostable == bo) {
						if (b.booster.isMul)
							b.booster.hover(box, FACTIONS.player().tech.level(t)*(b.booster.to()-1) + 1);
						else
							b.booster.hover(box, FACTIONS.player().tech.level(t)*(b.booster.to()));
						
					}
				}
				box.NL();
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
