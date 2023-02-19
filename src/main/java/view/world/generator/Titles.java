package view.world.generator;

import game.faction.FACTIONS;
import game.faction.player.PTitles.PTitle;
import init.D;
import init.boostable.BBoost;
import init.boostable.BOOSTABLES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import util.colors.GCOLOR;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;

final class Titles extends GuiSection{

	private static CharSequence ¤¤spent = "¤Pick 5 unlocked titles to boost your name.";
	
	static {
		D.ts(Titles.class);
	}
	
	Titles(){
		
		ArrayList<RENDEROBJ> rows = new ArrayList<>(FACTIONS.player().titles.all().size());
		
		
		for (PTitle t : FACTIONS.player().titles.all()) {
			rows.add(new Butt(t));
		}
		
		add(new GScrollRows(rows, 400).view());
		
		
		addRelBody(8, DIR.N, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofkInv(text, FACTIONS.player().titles.selected(), 5);
			}
			
		}.r(DIR.N));
		addRelBody(4, DIR.N, new GHeader(¤¤spent));
		
	}
	
	private static final class Butt extends CLICKABLE.ClickableAbs {

		private final PTitle title;
		
		Butt(PTitle title){
			body.setDim(700, 32);
			this.title = title;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			ColorImp c = ColorImp.TMP;
			if (title.selected())
				c.set(GCOLOR.T().IGREAT);
			else if(!title.unlocked())
				c.set(GCOLOR.T().ERROR);
			else if(FACTIONS.player().titles.selected() >= 5)
				c.set(GCOLOR.T().INACTIVE);
			else {
				c.set(isHovered ? GCOLOR.T().HOVERED : GCOLOR.T().HOVERABLE);
			}
			
			c.bind();
			UI.FONT().M.renderCX(r, body().cX(), body().y1(), title.name);
			
			COLOR.unbind();
			
		}
		
		@Override
		protected void renAction() {
			activeSet(title.unlocked());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(title.name);
			b.text(title.desc);
			b.NL(6);
			
			b.textLL(BOOSTABLES.INFO().names);
			b.NL();
			for (BBoost bo : title.boost.boosts()) {
				bo.hover(text);
				b.NL();
			}
			if (title.unlock != null)
				title.unlock.hoverInfoGet(text);
		}
		
		@Override
		protected void clickA() {
			if (title.selected())
				title.select(!title.selected());
			else if(!title.unlocked())
				;
			else if(FACTIONS.player().titles.selected() >= 5)
				;
			else {
				title.select(!title.selected());
			}
			
		}
		
	}
	
}
