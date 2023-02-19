package view.ui.profile;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.player.PTitles.PTitle;
import game.statistics.G_REQ;
import init.D;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.gui.misc.*;
import util.gui.misc.GMeter.GGaugeColor;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;

final class Titles extends GuiSection{

	private static CharSequence ¤¤spent = "¤Chosen";
	private static CharSequence ¤¤NotAchiving = "¤Titles can not be unlocked";
	private static CharSequence ¤¤spentD = "¤Whenever you start a new game, you get to choose 5 unlocked titles.";
	
	private static CharSequence ¤¤Locked = "¤Title is currently unavailable";
	private static CharSequence ¤¤Active = "¤Title is currently unlocked and active";
	private static CharSequence ¤¤Unlocked = "¤Title is currently unlocked. You can select it when starting a new game.";
	static {
		D.ts(Titles.class);
	}
	
	Titles(){
		
		ArrayList<RENDEROBJ> rows = new ArrayList<>(FACTIONS.player().titles.all().size());
		
		
		for (PTitle t : FACTIONS.player().titles.all()) {
			rows.add(new Butt(t));
		}
		
		add(new GScrollRows(rows, 500).view());
		
		
		
		addRelBody(8, DIR.N, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, FACTIONS.player().titles.selected(), 5);
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.text(¤¤spentD);
			};
		}.hh(¤¤spent));
		
		addRightC(64, new GStat() {
			
			@Override
			public void update(GText text) {
				if (!GAME.achieving())
					text.errorify().add(¤¤NotAchiving);
			}
			
		});
		
	}
	
	private static final class Butt extends HOVERABLE.HoverableAbs {

		private final PTitle title;
		
		Butt(PTitle title){
			body.setDim(700, 48);
			this.title = title;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			GCOLOR.UI().border().render(r, body.x1(), body().x2(), body().y1(), body().y2()-2);
			GCOLOR.UI().bg(title.unlocked(), title.isNew(), title.selected()).render(r, body.x1()+1, body().x2()-1, body().y1()+1, body().y2()-3);
			Str.TMP.clear().add(title.name);
			if (title.selected())
				GCOLOR.T().H2.bind();
			else if (title.unlocked())
				GCOLOR.T().H1.bind();
			else if (title.isNew())
				GCOLOR.T().HOVERED.bind();
			else
				GCOLOR.T().INACTIVE.bind();
			
			UI.FONT().H2.render(r, Str.TMP, body().x1()+8, body().y1()+2);
			COLOR.unbind();
			
			double d = 0;
			if (title.unlocked()) {
				d = 1;
			}else {
				double am = 0;
				for (G_REQ q : title.reqs) {
					am += q.progress();
				}
				d = am/title.reqs.size();
			}
			
			GGaugeColor cc = GMeter.C_ORANGE;
			if (title.selected())
				cc = GMeter.C_BLUE;
			else if (title.unlocked())
				cc = GMeter.C_GREEN;
			
			GMeter.render(r, cc, d, body.x1(), body().x2(), body().y2()-10, body().y2()-2);
			
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			title.consumeNew();
			GBox b = (GBox) text;
			b.title(title.name);
			b.text(title.desc);
			b.NL(6);
			
			if (title.selected()) {
				b.text(¤¤Active);
			}else if(title.unlocked()) {
				b.text(¤¤Unlocked);
			}else {
				b.text(¤¤Locked);
			}
			
			b.NL(8);
			
			G_REQ.hover(title.reqs, b);

			b.NL(16);
			
			title.boost.hover(b);
			
			b.NL(16);
			
			if (title.unlock != null)
				title.unlock.hoverInfoGet(text);
			
			
		}
		
	}
	
}
