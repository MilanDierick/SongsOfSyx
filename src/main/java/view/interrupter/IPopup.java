package view.interrupter;

import init.C;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import util.gui.misc.*;
import util.gui.panel.GPanelS;

public class IPopup extends Interrupter{
	
	private final GButt trigger;
	protected final GuiSection section = new GuiSection();
	private int rights = 0;
	private final int maxRights;
	private GPanelS box = new GPanelS();
	private boolean dismissable = true;
	private boolean haltClose;
	private final InterManager m;
	
	public IPopup(InterManager manager, GButt trigger){
		
		this(manager, trigger, 4);
		box.setButtBg();
	}
	
	public IPopup(InterManager manager, GButt trigger, int row){
		this.m = manager;
		this.trigger = trigger;
		trigger.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				if (!isActivated())
					show();
				else
					hide();
			}
		});
		maxRights = row;
	}
	
	public RENDEROBJ addButt(RENDEROBJ b){
		return addButt(b, 0, 0);
	}
	
	public void setDismissable() {
		dismissable = true;
	}
	
	public RENDEROBJ addButt(RENDEROBJ b, int marginX, int marginY){
		rights ++;
		if (rights > maxRights) {
			rights = 1;
			b.body().moveX1(section.body().x1() + marginX);
			b.body().moveY1(section.body().y2() + marginY);
			section.add(b);
		}else {
			section.addRight(marginX, b);
		}
		
		return b;
		
	}
	
	public void NL() {
		rights = 1000000;
	}
	
	public void NL(int my) {
		rights = 1000000;
		section.body().incrW(my);
	}
	
	public void NL(int my, CharSequence title) {
		RENDEROBJ t = new GText(UI.FONT().H2, title).toCamel().r(DIR.C);
		if (section.body().width() == 0) {
			section.body().setWidth(t.body().width()+20);
			t.body().moveX1(section.body().x1()+20);
			t.body().moveY1(section.body().y2() + my);
			section.add(t);
		}else {
			rights = 1000000;
			section.body().incrH(my);
			addButt(t, 20, 0);
		}
		
		
		rights = 1000000;
	}
	
	public RENDEROBJ newLine(RENDEROBJ b, int marginX, int marginY){
		rights = 1;
		if (section.body().width() == 0 && section.body().height() == 0) {
			section.body().setWidth(b.body().width() + marginX);
			section.body().setHeight(b.body().height() + marginY);
			b.body().moveX1(section.body().x1() + marginX);
			b.body().moveY1(section.body().y1() + marginY);
			section.add(b);
		}else {
			b.body().moveX1(section.body().x1() + marginX);
			b.body().moveY1(section.body().y2() + marginY);
			section.add(b);
		}
		
		return b;
		
	}
	
	protected void show() {
		if (isActivated())
			return;
		
		section.body().moveC(trigger.body().cX(), 0);
		if (trigger.body().y1() > C.HEIGHT()/2)
			section.body().moveY2(trigger.body().y1() - C.SCALE);
		else
			section.body().moveY1(trigger.body().y2() + C.SCALE);
		box.inner().set(section);
		show(m);
	}
	
	public void deactivate() {
		hide();
	}
	
	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.RIGHT){
			hide();
		}else if(button == MButt.LEFT){
			section.click();
			if (haltClose) {
				haltClose = false;
				return;
			}
			if (dismissable)
				hide();
		}
	}
	
	@Override
	protected boolean otherClick(MButt butt) {
		
		
		if (butt == MButt.RIGHT) {
			hide();
			return true;
		}
		
		if (dismissable) {
			hide();
			return false;
		}

		
		return false;
	}
	
	public GButt getTrigger(){
		return trigger;
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		section.hover(mCoo);
		return mCoo.isWithinRec(section);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		box.render(r, ds);
		section.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {

		trigger.selectTmp();
		return true;
	}

	public void haltClose() {
		haltClose = true;
	}

	

}
