package view.sett.ui.subject;

import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.*;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.sett.ui.standing.StatRow;

final class SStats {
	
	private final UISubject a;
	private final GuiSection section = new GuiSection();
	
	SStats(UISubject a, int height) {
		this.a = a;
		section.addRelBody(8, DIR.S, makeStats(height-16));
	}
	
	private RENDEROBJ makeStats(int height) {
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		GText work = new GText(UI.FONT().S, 32);

		
		for (StatCollection h : STATS.COLLECTIONS()) {
			LinkedList<STAT> stats = new LinkedList<>();
			for (STAT s : h.all()) {
				if (s.key() == null)
					continue;
				if (s.standing() == null)
					continue;
				outer:
				for (Race r : RACES.all()) {
					for (HCLASS c : HCLASS.ALL)
					if (s.standing().max(c, r) != 0) {
						stats.add(s);
						break outer;
					}
				}
			}
			
			if (stats.size() == 0)
				continue;
		
			rows.add(new GHeader(h.info.name).hoverInfoSet(h.info.desc));
			for (STAT s : stats) {
			
				CLICKABLE c = new Row(s, work); 				
				rows.add(c);
			}
		}
	
		GuiSection s = new GuiSection();
		GScrollRows sc = new GScrollRows(rows, height-s.body().height()-4, 0);
		s.addDownC(4, sc.view());
		
		return s;
	}
	

	
	private class Row extends CLICKABLE.ClickableAbs {
		
		private final GText work;
		private final STAT s;
		Row(STAT stat, GText text){
			this.work = text;
			this.s = stat;
			body.setDim(480, 24);
		}
		
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			if (isHovered) {
				COLOR.BLUEDARK.render(r, body());
			}
			
			work.setFont(UI.FONT().S);
			work.clear();
			work.add(s.info().name);
			work.lablifySub();
			work.render(r, body().x1(), body().y1());
			
			work.setFont(UI.FONT().S);
			work.clear();
			
			if (s.indu().max(a.a.indu()) == 1 && s.info().isInt()) {
				GFORMAT.bool(work, s.indu().get(a.a.indu()) == 1);
			}else if (s.info().isInt()) {
				
				GFORMAT.i(work, s.indu().get(a.a.indu()));
			}else {
				GFORMAT.perc(work, s.indu().getD(a.a.indu()));
			}
			work.normalify();
			work.render(r, body().x1()+180, body().y1());
			
			double now = s.standing().get(a.a.indu());
			double max = s.standing().max(a.a.indu().clas(), a.a.race());
			int w = (int) (200*s.standing().normalized(a.a.indu().clas(), a.a.race()));
			if (w > 0) {
				if (w < 20)
					w = 20;
				GMeter.render(r, GMeter.C_REDGREEN, now/max, body().x1()+230, body().x1()+230+w, body().y1()+3, body().y2()-3);
			}
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			text.title(s.info().name);
			text.text(s.info().desc);
			text.NL();
			GBox b = (GBox) text;
			if (s.indu().max(a.a.indu()) == 1) {
				b.add(GFORMAT.bool(b.text(), s.indu().get(a.a.indu()) == 1));
			}else if (s.info().isInt()) {
				b.add(GFORMAT.i(b.text(), s.indu().get(a.a.indu())));
			}else {
				double d = s.indu().getD(a.a.indu());
				b.add(GFORMAT.perc(b.text(), d));
			}
			
			text.NL(8);
			StatRow.hoverStanding(text, s, a.a.indu());
			
			
		}
		
		@Override
		protected void clickA() {
			if (s.indu() != null)
				SDebugInput.activate(s.indu(), a.a);
		}
		
	}
	
	GuiSection activate() {
		return section;
	}

}
