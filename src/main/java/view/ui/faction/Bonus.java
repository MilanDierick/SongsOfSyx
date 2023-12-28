package view.ui.faction;

import java.util.LinkedList;

import game.boosting.*;
import game.faction.npc.FactionNPC;
import game.faction.npc.NPCBonus;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;

final class Bonus extends GuiSection{

	final GETTER<FactionNPC> f;
	
	Bonus(GETTER<FactionNPC> f, int height){
		this.f = f;
		
		
		final StringInputSprite in = new StringInputSprite(16, UI.FONT().M).placeHolder(DicMisc.¤¤Search);
		addRelBody(0, DIR.S, new GInput(in));
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		LIST<BoostableCat> cats = new ArrayList<>(BOOSTABLES.BATTLE(), BOOSTABLES.ROOMS());
		for (BoostableCat cat : cats) {
			
			rows.add(new GHeader(cat.name));
			
			for (Boostable bo : cat.all()) {
				rows.add(new Row(bo));
			}
		}
		
		addRelBody(8, DIR.S, new GScrollRows(rows, height-body().height()){
			
			@Override
			protected boolean passesFilter(int i, RENDEROBJ o) {
				if (in.text().length() == 0)
					return true;
				if (o instanceof Row) {
					Row r = (Row) o;
					if (Str.containsText(r.bo.name, in.text()) || Str.containsText(r.bo.desc, in.text()))
						return true;
					return false;
				}else
					return false;
			};
			
			
		}.view());
		
		
		

	}
	
	private class Row extends GuiSection {
		
		private final Boostable bo;
		
		Row(Boostable bo){
			this.bo = bo;
			add(bo.icon, 0,0);
			GText l = new GText(UI.FONT().M, bo.name);
			l.setMaxChars(20);
			addRightC(4, l);
			
		
			
			addRightCAbs(300, new Gauge(bo));
			
			addRightC(8, new GStat() {
				
				@Override
				public void update(GText text) {
					double min = bo.min(NPCBonus.class);
					double max = bo.max(NPCBonus.class);
					if (min == bo.baseValue && max == bo.baseValue)
						return;
					if (bo.baseValue == 0)
						GFORMAT.percInc(text, bo.get(f.get().bonus));
					else
						GFORMAT.percInc(text, bo.get(f.get().bonus)/bo.baseValue-1.0);
				}
			});
			
			
			
			pad(0, 4);
			
			body().incrW(130);
			body().incrH(4);
		}
		
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			if (hoveredIs()) {
				COLOR.WHITE15.render(r, body());
			}
			GCOLOR.UI().border().render(r, body().x1(), body().x2(), body().y2()-1, body().y2());
			super.render(r, ds);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			text.title(bo.name);
			text.text(bo.desc);
			text.NL(8);
			bo.hoverDetailed(text, f.get().bonus, null, true);
			
		}
		
		private class Gauge extends SPRITE.Imp {
			
			Gauge(Boostable bo){
				super(400, 24);
			}

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				double min = bo.min(NPCBonus.class);
				double max = bo.max(NPCBonus.class);
				if (min == bo.baseValue && max == bo.baseValue)
					return;
				
				double d = bo.get(f.get().bonus);
				
				GMeter.renderDelta(r, bo.baseValue/max, d/max, X1, X2, Y1, Y2, GMeter.C_GRAY);
				
			}
			

			
		}
	}
	
	


}
