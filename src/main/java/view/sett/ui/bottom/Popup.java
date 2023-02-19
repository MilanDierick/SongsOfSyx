package view.sett.ui.bottom;

import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.BODY_HOLDER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.clickable.CLICKABLE.Holder;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GButt;
import util.gui.panel.GPanelS;
import util.gui.table.GScrollRows;
import view.main.VIEW;

class Popup {

	private GuiSection hackContent;
	
	GuiSection main = new GuiSection() {

		@Override
		public boolean hoveredIs() {
			return super.hoveredIs() || (exp != null && exp.hoveredIs());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
		
			super.hoverInfoGet(text);
			if ((exp != null))
				exp.hoverInfoGet(text);
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (exp != null && !exp.hover(mCoo))
				exp = null;
			return super.hover(mCoo);
		};
		
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);
			if (exp != null && !VIEW.inters().popup.showing())
				exp = null;
		}
	};
	
	private GuiSection exp = null;
	static final int width = 280;
	static final int bh = 40;
	
	Popup(){
		
		CLICKABLE.Holder expansion = new Holder() {
			
			@Override
			protected CLICKABLE get() {
				return exp;
			}
		};
		
		
		main.add(expansion);
	}
	
	void add(CLICKABLE c) {
		addB(c);
	}
	
	private void addB(CLICKABLE c) {
		if (main.elements().size() == 1)
			c.body().moveY2(main.body().cY());
		else
			c.body().moveY2(main.getLastY1());
		c.body().moveX1(0);
		main.add(c);
	}
	
	void add(SPRITE icon, CharSequence name, Expansion e) {
		
		CLICKABLE tt;
		
		GuiSection ex = new GuiSection();
		GPanelS p = new GPanelS();
		p.setButtBg();
		p.inner().setDim(width, bh*8+16);
		
		CLICKABLE c = new GButt.ButtPanel(name) {
			final GuiSection m = new GuiSection();
			
			@Override
			public boolean hover(COORDINATE mCoo) {
				if (super.hover(mCoo)) {
					m.clear();
					m.add(p);
					m.body().moveX1(this.body.x2());
					m.body().moveY2(main.body().y2());
					
					ex.body().moveCY(this.body().cY());
					if (ex.body().y2() > p.inner().y2())
						ex.body().moveY2(p.inner().y2());
					else if (ex.body().y1() < p.inner().y1())
						ex.body().moveY1(p.inner().y1());
					ex.body().moveX1(p.inner().x1());
					m.add(ex);
					
					exp = m;
//					exp.body().moveX1(this.body().x2());
//					
//					
//					
//					set(ex, this);
					return true;
				}
				return false;
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
				isHovered |= exp == m;
				super.render(r, ds, isActive, isSelected, isHovered);
				SPRITES.icons().s.arrow_right.renderC(r, body().x2()-16, body().cY());
			};
			
		}.setDim(width, bh).icon(icon);
		addB(c);
		
		if (e.rows.size() > 8) {
			tt = new GScrollRows(e.rows, bh*8).view();
		}else {
			GuiSection s = new GuiSection();
			for (CLICKABLE cl : e.rows) {
				s.addDownC(0, cl);
			}
			tt = s;
		}
		
		ex.add(tt);
		p.inner().setWidth(tt.body().width());
		
//		tt.body().moveCY(c.body().cY());
//		tt.body().moveX1(ex.body().x1());
//		if (tt.body().y2() > ex.body().y2())
//			tt.body().moveY2(ex.body().y2());
//		if (tt.body().y1() < ex.body().y1())
//			tt.body().moveY1(ex.body().y1());
		
//		GPanelS p = new GPanelS();
//		p.setButtBg();
//		p.inner().set(tt);
//		ex.add(p);	
//		
//		ex.add(tt);
		
		
		
	}
	
	void add(SPRITE icon, CharSequence name, Popup p) {
		
		
		
		CLICKABLE c = new GButt.ButtPanel(name) {
			
			@Override
			public boolean hover(COORDINATE mCoo) {
				if (super.hover(mCoo)) {
					set(p, this);
					return true;
				}
				return false;
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
				isHovered |= exp == p.main;
				super.render(r, ds, isActive, isSelected, isHovered);
				SPRITES.icons().s.arrow_right.renderC(r, body().x2()-16, body().cY());
			};
			
		}.setDim(width, bh).icon(icon);
		addB(c);
		
		p.hackContent = new GuiSection();
		for (RENDEROBJ o : p.main.elements())
			p.hackContent.addDown(0, o);
		p.main.clear();
		
		GPanelS pa = new GPanelS();
		pa.inner().setDim(width, bh*8+16);
		pa.setButtBg();
		pa.inner().moveY2(p.main.body().y2());
		pa.inner().moveX1(p.main.body().x1());
		p.main.add(pa);
		p.main.moveLastToBack();
		p.hackContent.body().centerIn(p.main);
		p.main.add(p.hackContent);
		
		
	}
	
	GuiSection get() {
		exp = null;
		return main;
	}
	
	private void set(Popup p, BODY_HOLDER b) {
		exp = p.main;
		exp.body().moveX1(b.body().x2());
		exp.body().moveCY(b.body().cY());
		
		if (exp.body().y2() > main.body().y2())
			exp.body().moveY2(main.body().y2());
		
		p.hackContent.body().moveCY(b.body().cY());
		if (p.hackContent.body().y2() > exp.body().y2()-8)
			p.hackContent.body().moveY2(exp.body().y2()-8);
		
	}
	

	
	static class Expansion {
		
		private final LinkedList<CLICKABLE> rows = new LinkedList<>();
		
		void add(CLICKABLE c) {
			rows.add(c);
		}
		
	}
	
}
