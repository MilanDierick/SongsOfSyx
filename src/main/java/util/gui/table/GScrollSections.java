package util.gui.table;

import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GButt;

public final class GScrollSections{

	private final CLICKABLE up,down,drag;
	private final static String descUp = "up";
	private final static String descDown = "down";
	private final static String descDrag = "drag";
	private final GuiSection scrollstuff = new GuiSection();
	private final GuiSection section = new GuiSection() {
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			double s = MButt.clearWheelSpin();
			if (hoveredIs() && s != 0) {
				if (s < 0) {
					move(currentI +1);
				}else {
					move(currentI -1);
				}
				MButt.clearWheelSpin();
			}
			super.render(r, ds);
		};
	};
	private final ArrayListResize<Entry> sections = new ArrayListResize<>(10, 100);
	private int currentI = -1;
	
	private final ACTION actionUp = new ACTION() {
		@Override
		public void exe() {
			move(currentI -1);
		}
	};
	private final ACTION actionDown = new ACTION() {
		@Override
		public void exe() {
			move(currentI +1);
		}
	};
	
	public GScrollSections(int width, int height) {

		
		this( 
				new GButt.Glow(UI.decor().slider.makeSprite(4)).repetativeSet(true), 
				new GButt.Glow(UI.decor().slider.makeSprite(5)).repetativeSet(true), 
				UI.decor().slider.makeSprite(7),
				width, height
				);
	}
	
	public GScrollSections(CLICKABLE up, CLICKABLE down, SPRITE c, int width, int height) {
		
		this.up = up;
		this.down = down;
		up.clickActionSet(actionUp).hoverInfoSet(descUp);
		down.clickActionSet(actionDown).hoverInfoSet(descDown);
		final int h = height - up.body().height()*2;
		drag = new CLICKABLE.ClickableAbs(up.body().width(), h) {
			

			private boolean mouseDown;
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
				if (sections.size() == 0)
					return;
				
				int y1 = up.body().y2() + currentI*(h-c.height())/(sections.size()-1);
				c.render(r, body().x1(), y1);
			}
			
			@Override
			public boolean hover(COORDINATE mCoo) {
				
				if (super.hover(mCoo) || mouseDown) {
					mouseDown = MButt.LEFT.isDown();
					if (mouseDown && sections.size() > 0) {
						int dy = mCoo.y()-body().y1();
						int i = dy/(h/sections.size());
						move(i);
					}
					return true;
				}
				return false;
			}
			
			@Override
			protected void clickA() {
				mouseDown = true;
			}

		}.hoverInfoSet(descDrag);
		
		scrollstuff.add(up);
		scrollstuff.addDownC(0, drag);
		scrollstuff.addDownC(0, down);
		section.body().setWidth(width);
		section.body().setHeight(height);
		move(0);
		currentI = -1;
		
		
	}
	
	private void move(int first) {
		
		if (first >= sections.size())
			first = sections.size()-1;
		if (first < 0)
			first = 0;
		up.activeSet(currentI > 0);
		down.activeSet(currentI < sections.size()-1);
		
		if (first == currentI)
			return;
		currentI = first;
		up.activeSet(currentI > 0);
		down.activeSet(currentI < sections.size()-1);
		
		int x1 = section.body().x1();
		int y1 = section.body().y1();
		int x2 = section.body().x2();
		int y2 = section.body().y2();
		section.clear();
		section.body().setWidth(x2-x1);
		section.body().setHeight(y2-y1);
		section.body().moveX1(x1).moveY1(y1);
		scrollstuff.body().moveX2(x2);
		scrollstuff.body().moveY1(y1);
		section.add(scrollstuff);
		
		if (sections.size() > 0) {
			Entry e = sections.get(currentI);
			e.s.body().moveX1(x1+e.x1);
			e.s.body().moveY1(y1+e.y1);
			section.add(e.s);
		}
		
		
	}
	
	public final CLICKABLE getView() {
		return section;
	}
	
	public void add(GuiSection section) {
		Entry e = new Entry(section);
		sections.add(e);
		if (sections.size() == 1)
			move(0);
	}

	private final class Entry {
		
		private final GuiSection s;
		private final int x1;
		private final int y1;
		
		Entry(GuiSection section){
			this.s = section;
			x1 = s.body().x1()-section.body().x1();
			y1 = s.body().y1()-section.body().y1();
		}
		
	}
	
}
