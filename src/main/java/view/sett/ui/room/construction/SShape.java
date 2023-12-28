package view.sett.ui.room.construction;

import init.D;
import init.sprite.SPRITES;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.main.VIEW;

final class SShape{

	private final GuiSection ss = new GuiSection() {
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);
			boolean b = VIEW.s().tools.placer.getCurrent() == s.placement.placer.area() || VIEW.s().tools.placer.getCurrent() == s.placement.placer.area().getUndo();
			if (b)
			SETT.ROOMS().placement.placer.renderExpense();
		};
	};
	private final State s;
	private final GuiSection pButts = new GuiSection();
	private final GuiSection butts = new GuiSection();
	{
		D.gInit(this);
	}
	private final GHeader title = new GHeader(DicMisc.¤¤Shape).subify();
	

	
	private final CLICKABLE buttExpand = new GButt.ButtPanel(SPRITES.icons().m.expand) {
		
		@Override
		protected void clickA() {
			s.placement.placer.buildOnWalls.set(false);
			VIEW.s().tools.place(s.placement.placer.area(), s.config);
		}
		
		@Override
		protected void renAction() {
			selectedSet(VIEW.s().tools.placer.getCurrent() == s.placement.placer.area() && !s.placement.placer.buildOnWalls.is());
		}
	}.hoverInfoSet(D.g("Expand", "Expand Room. Items can only be placed on the designated room area."));
	
	final CLICKABLE buttExpandWalls = new GButt.ButtPanel(new SPRITE.Twin(SPRITES.icons().m.expand, SPRITES.icons().m.plus)) {
		
		@Override
		protected void clickA() {
			s.placement.placer.buildOnWalls.set(true);
			VIEW.s().tools.place(s.placement.placer.area(), s.config);
		}
		
		@Override
		protected void renAction() {
			selectedSet(VIEW.s().tools.placer.getCurrent() == s.placement.placer.area() && s.placement.placer.buildOnWalls.is());
		}
	}.hoverInfoSet(D.g("ExpandOver", "Expand Room Over Structures"));
	
	private final CLICKABLE buttShrink = new GButt.ButtPanel(SPRITES.icons().m.shrink) {
		
		@Override
		protected void clickA() {
			VIEW.s().tools.place(s.placement.placer.area().getUndo(), s.config);
		}
		
		@Override
		protected void renAction() {
			bg(GCOLOR.UI().BAD.normal);
			selectedSet(VIEW.s().tools.placer.getCurrent() == s.placement.placer.area().getUndo());
		}
		
	}.hoverInfoSet(D.g("Shrink", "Shrink Room"));
	
	private final HOVERABLE expensiveness = new GStat() {
		@Override
		public void update(GText text) {
			GFORMAT.percInv(text, s.placement.placer.extraExpense());
			
		}
	}.hh(SPRITES.icons().m.raw_materials).hoverInfoSet(D.g("extraDesc", "When rooms are built too deep or wide, extra materials must be used to build and maintain it. Yellow tiles represents this. Shape the room thinner, or remove room tiles in the center to allow for more support to avoid these costs."));
	
	SShape(State s){
		this.s = s;
	}
	
	GuiSection get(){
		ss.clear();
		
		ss.add(title);
		
		butts.clear();
		butts.add(buttExpand, 0, 0);
		butts.addRightC(2, buttExpandWalls);
		butts.addRightC(2, buttShrink);
		
		if (s.b.constructor().mustBeIndoors())
			butts.addRightC(8, expensiveness);
		else
			butts.body().incrW(buttExpand.body().width()+10);
		
		ss.addRelBody(4, DIR.S, butts);
		
		
		pButts.clear();
		pButts.body().setDim(1, 32);
		if (VIEW.s().tools.placer.getCurrent() == s.placement.placer.area() || VIEW.s().tools.placer.getCurrent() == s.placement.placer.area().getUndo())
			VIEW.s().tools.placer.stealButtons(pButts, true);
		ss.addRelBody(0, DIR.S, pButts);
		
		
		return ss;
	}
	
	
	
}
