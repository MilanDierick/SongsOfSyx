package view.interrupter;

import init.C;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.gui.misc.GBox;
import util.gui.panel.GPanelS;

public final class IPopup2{
	
	private final GuiSection s = new GuiSection();
	private final Inter inter = new Inter(s);
	private final InterManager m;
	private CLICKABLE trigger;
	
	
	public IPopup2(InterManager manager){
		this.m = manager;
	}

	
	public void show(RENDEROBJ s, CLICKABLE trigger) {
		this.s.clear();
		this.s.add(s);
		show(trigger);
		
	}
	
	public GuiSection section() {
		return s;
	}
	
	public void show(CLICKABLE trigger) {
		this.trigger = trigger;
		showP(trigger.body().cX(), trigger.body().cY());
	}
	
	public void close() {
		inter.hide();
	}
	
	public void show(int x, int y) {
		this.trigger = null;
		showP(x, y);
	}
	
	public boolean showing() {
		return inter.isActivated();
	}
	
	protected void showP(int x, int y) {
		
		int M = C.SG*32;
		
		
		s.body().moveCX(x);
		if (y > C.HEIGHT()/2){
			s.body().moveY2(y-M);
		}else {
			s.body().moveY1(y+M);
		}
		
		if (s.body().x2()+M >= C.WIDTH()) {
			s.body().moveX2(C.WIDTH()-M);
		}
		
		if (s.body().x1() - M < 0) {
			s.body().moveX1(x+M);
		}
		
		if (s.body().y2()+M >= C.HEIGHT()) {
			s.body().moveY2(C.HEIGHT()-M);
		}
		
		if (s.body().y1() - M < 0) {
			s.body().moveY1(M);
		}
		
		if (!inter.isActivated()) {
			m.add(inter);
		}
	}

	private class Inter extends Interrupter {
		
		private final GPanelS box;
		
		Inter(GuiSection s){
			box = new GPanelS();
			box.setButtBg();
		}
		
		@Override
		protected void hoverTimer(GBox text) {
			s.hoverInfoGet(text);
		}

		@Override
		protected void mouseClick(MButt button) {
			if (button == MButt.RIGHT){
				hide();
			}else if(button == MButt.LEFT){
				s.click();
			}
		}
		
		@Override
		public void hide() {
			// TODO Auto-generated method stub
			super.hide();
		}
		
		@Override
		protected boolean otherClick(MButt butt) {
			hide();
			if (butt == MButt.RIGHT)
				return true;
			return false;
		}
		
		@Override
		protected void otherAdd(Interrupter other) {
			hide();
		}

		@Override
		protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
			return s.hover(mCoo);
		}

		@Override
		protected boolean render(Renderer r, float ds) {
			box.inner().set(s);
			box.render(r, ds);
			s.render(r, ds);
			if (trigger != null) {
				trigger.selectTmp();
			}
			return true;
		}

		@Override
		protected boolean update(float ds) {
//			if (KEY.anyPressed()) {
//				hide();
//			}
			return true;
		}
		
		
		
	}

}
