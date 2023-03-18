package view.ui.profile;

import java.util.Arrays;
import java.util.Comparator;

import init.boostable.*;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import util.colors.GCOLOR;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;

final class Bonus extends GuiSection {
	
	public static int hW = 200;
	private final static int ww = 90;

	
	Bonus(int height){
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		GuiSection h = new GuiSection();
		h.body().setWidth(150);
		
		for (BoostableCollection c : BOOSTABLES.colls()) {
			cat(rows, c);
		}
		
		addDown(8, new GScrollRows(rows, height-h.body().height()-8, 0).view());
		
		
		
	}
	
	private void cat(LISTE<RENDEROBJ> res, BoostableCollection cat) {
		
		res.add(new RENDEROBJ.RenderImp(100,24) {
			GText h = new GText(UI.FONT().H2, cat.name).lablifySub();
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				h.render(r, body().x1()+20, body().y1());
			}
		});
		
		BOOSTABLE[] indecies = new BOOSTABLE[cat.all().size()];
		for (int i = 0; i < indecies.length; i++)
			indecies[i] = cat.all().get(i);
		
		Arrays.sort(indecies, new Comparator<BOOSTABLE>() {

			@Override
			public int compare(BOOSTABLE o1, BOOSTABLE o2) {
				String s1 = "" + o1.name;
				String s2 = "" + o2.name;
				return s1.compareToIgnoreCase(s2);
			}
		
		});
		
		
		
		for (int i = 0; i < indecies.length; i++) {
			res.add(new Row(indecies[i]));
		}
		
		res.add(new RENDEROBJ.RenderImp(600,16) {
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				GCOLOR.UI().border().render(r, body().x1(), body().x2(), body().y1()+4, body().y1()+5);
			}
		});
		
	}
	
	
	
	private static class Row extends GuiSection {
		
		private final BOOSTABLE bo;
		Row(BOOSTABLE bo){
			this.bo = bo;
			add(bo.icon(), 0,0);
			GText l = new GText(UI.FONT().M, bo.name);
			l.setMaxChars(20);
			addRight(4, l);
			
		
			
			add(new Gauge(bo), 300, 0);
			
			addRightC(8, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.percInc(text, BOOSTABLES.player().get(bo, null, null)/bo.defAdd-1.0);
				}
			});
			
			
			
			pad(0, 2);
			
			body().incrW(ww);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			if (hoveredIs()) {
				COLOR.WHITE15.render(r, body());
			}
			super.render(r, ds);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			text.title(bo.name);
			text.text(bo.desc);
			text.NL(8);
			BoostHoverer.hover(text, bo, null, null);
			
		}
		
	}
	
	private static class Gauge extends RenderImp {
		
		private final BOOSTABLE bo;
		
		Gauge(BOOSTABLE bo){
			super(300, 16);
			this.bo = bo;
		}

		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			
			double min = BOOSTABLES.player().min(bo);
			double max = BOOSTABLES.player().max(bo);
			if (min == bo.defAdd && max == bo.defAdd)
				return;
			
			double d = BOOSTABLES.player().get(bo, null, null);
			GMeter.renderDelta(r, bo.defAdd/max, d/max, body);
			
		}
		

		
	}

	

	
}
