package view.sett.ui.room.construction;

import init.C;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import view.main.VIEW;
import view.tool.ToolConfig;

final class Config implements ToolConfig {
	
	private final State s;
	boolean build = true;
	private GuiSection section = new GuiSection();
	private final SShape sshape;
	private final SMaterial sMaterial;
	private final SFrame frame;
	private final SItems items;
	private final SStats stats;
	private final SCollection coll;
	private final Separator sep1 = new Separator();
	private final Separator sep2 = new Separator();
	Config(State s){
		this.s = s;
		sshape = new SShape(s);
		frame = new SFrame(s);
		items = new SItems(s);
		stats = new SStats(s);
		sMaterial = new SMaterial(s);
		coll = new SCollection(s);
	}
	
	@Override
	public void addUI(LISTE<RENDEROBJ> uis){
		s.b.constructor().renderExtra();
		section.clear();
		
		if (s.collection != null) {
			VIEW.s().tools.placer.stealButtons(section);
			section.addRelBody(12, DIR.N, coll.get());
		}else if (s.b.constructor().usesArea()) {
			section.add(sshape.get());
			
			if (s.b.constructor().mustBeIndoors()) {
				section.addDownC(6, sMaterial.get());
			}
			
			section.addRelBody(12, DIR.E, sep1.get(section.body().height()));
			
			if (s.b.constructor().groups().size() > 0) {
				section.addRelBody(12, DIR.E, items.get());
				section.addRelBody(12, DIR.E, sep2.get(section.body().height()));
			}
			
			section.addRelBody(12, DIR.E, stats.get());
			
		}else {

			
			if (s.b.constructor().mustBeIndoors()) {
				section.addDownC(6, sMaterial.get());
				section.addRelBody(12, DIR.E, sep1.get(section.body().height()));
				if (s.b.constructor().groups().size() > 1) {
					section.addRelBody(12, DIR.E, items.getFlat());
				}else {
					section.addRelBody(12, DIR.E, items.getSingle());
				}
			}else if (s.b.constructor().groups().size() > 1) {
				
				section.addRelBody(12, DIR.E, items.getFlat());
			}else {
				VIEW.s().tools.placer.addStandardButtons(uis, false);
				return;
			}
			

			
		}
		
		GuiSection s = frame.get(section);
		
		
		s.body().moveCX(C.WIDTH()/2);
		s.body().moveY1(C.SG*80);
		if (VIEW.s().getWindow().tiles().y1() == 0) {
			s.body().moveY2(C.HEIGHT()-C.SG*80);
		}
		if (VIEW.s().getWindow().tiles().x2() == SETT.TWIDTH) {
			s.body().moveX1(C.SG*80);
		}
		uis.add(s);
		

	}
	
	
	@Override
	public boolean back() {
		if (SETT.ROOMS().placement.placer.popHistory())
			return false;
		
		if (!s.refurnishing && SETT.ROOMS().placement.placer.removeAllItems())
			return false;
		if (!s.refurnishing && SETT.ROOMS().placement.placer.removeArea())
			return false;
		return true;
	}
	
	@Override
	public void update(boolean UIHovered) {
		if (VIEW.renderSecond() > s.problemTimer) {
			s.problemGroup = null;
			s.problemneedArea = false;
			s.problemneedDoor = false;
			
		}
	}
	
	@Override
	public void activateAction() {

	}
	
	@Override
	public void deactivateAction() {
		if (s.refurnishing && build && SETT.ROOMS().placement.placer.createProblem() == null)
			SETT.ROOMS().placement.placer.create();
		SETT.ROOMS().placement.placer.init(null, 0);
		build = true;
	}
	
	private class Separator extends SPRITE.Imp{

		
		SPRITE get(int h) {
			this.height = h;
			this.width = 2;
			return this;
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			GCOLOR.UI().border(r, X1, X2, Y1, Y2);
			
		}
		
	}
	
};

