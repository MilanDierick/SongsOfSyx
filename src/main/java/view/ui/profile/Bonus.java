package view.ui.profile;

import game.boosting.*;
import init.race.POP_CL;
import init.race.RACES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;

final class Bonus extends GuiSection {
	
	private final StringInputSprite in = new StringInputSprite(16, UI.FONT().M).placeHolder(DicMisc.¤¤Search);
	
	Bonus(int height){
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		addRelBody(0, DIR.S, new GInput(in));
		
		BoostableCat cat = null;
		
		Row rr = new Row(BOOSTING.ALL().get(0));
		
		for (Boostable b : BOOSTING.ALL()) {
			if (b.name == null || b.name.length() == 0)
				continue;
			if ( b.cat != cat) {
				cat = b.cat;
				rows.add(new RENDEROBJ.RenderImp(rr.body().width(),rr.body().height()) {
					GText h = new GText(UI.FONT().H2, b.cat.name).lablifySub();
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						h.renderCY(r, body().x1()+20, body().cY());
						GCOLOR.UI().border().render(r, body().x1(), body().x2(), body().y2()-1, body().y2());
					}
				});
			}
			rows.add(new Row(b));
			
		}
		
		
		
		addRelBody(8, DIR.S, new GScrollRows(rows, height-body().height()-16, 0) {
			
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
	
	private static class Row extends GuiSection {
		
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
					double min = bo.min(POP_CL.class);
					double max = bo.max(POP_CL.class);
					if (min == bo.baseValue && max == bo.baseValue)
						return;
					if (bo.baseValue == 0)
						GFORMAT.percInc(text, bo.get(RACES.clP(null, null)));
					else
						GFORMAT.percInc(text, bo.get(RACES.clP(null, null))/bo.baseValue-1.0);
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
			bo.hoverDetailed(text, RACES.clP(null, null), null, true);
		}
		
		private class Gauge extends SPRITE.Imp {
			
			Gauge(Boostable bo){
				super(400, 24);
			}

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				double min = bo.min(POP_CL.class);
				double max = bo.max(POP_CL.class);
				if (min == bo.baseValue && max == bo.baseValue)
					return;
				
				double d = bo.get(RACES.clP(null, null));
				
				GMeter.renderDelta(r, bo.baseValue/max, d/max, X1, X2, Y1, Y2, GMeter.C_GRAY);
				
			}
			

			
		}
		
	}
	

	
}
