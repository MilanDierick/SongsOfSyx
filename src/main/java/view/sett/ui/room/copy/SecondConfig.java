package view.sett.ui.room.copy;

import init.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.ON_TOP_RENDERABLE;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import util.dic.DicMisc;
import util.gui.misc.GButt;
import util.gui.panel.GPanelS;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import view.tool.ToolConfig;

final class SecondConfig implements ToolConfig{

	private final GuiSection section = new GuiSection();
	private final ON_TOP_RENDERABLE top;
	private final GPanelS p = new GPanelS();
	private final GuiSection butts = new GuiSection();
	private final CLICKABLE butt;
	private final First first;
	private final FirstConfig fConfig;
	CLICKABLE exit = new GButt.Glow(SPRITES.icons().s.cancel, UI.PANEL().panelM.get(0)) {
		@Override
		protected void clickA() {
			VIEW.s().tools.placer.deactivate();
		};
	};
	
	SecondConfig(Source source, First first, FirstConfig fConfig){
		this.first = first;
		this.fConfig = fConfig;
		butt = new GButt.Panel(SPRITES.icons().m.arrow_left) {
			@Override
			protected void clickA() {
				VIEW.s().tools.place(first, fConfig);
			}
			
			@Override
			protected void renAction() {
				activeSet(false);
				for (COORDINATE c : source.area()) {
					if (source.is(c)) {
						activeSet(true);
						return;
					}
				}
			}
		};
		
		butts.add(UI.PANEL().panelL.get(DIR.E), 0, 0);
		butts.addRight(0, UI.PANEL().panelL.get(DIR.E, DIR.W));
		butts.addRight(0, UI.PANEL().panelL.get(DIR.E, DIR.W));
		butts.addRight(0, UI.PANEL().panelL.get(DIR.W));
		
		top = new ON_TOP_RENDERABLE() {
			
			@Override
			public void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
				RenderIterator it = data.onScreenTiles();
				while(it.has()) {
					if (source.is(it.tile())) {
						int m = 0;
						for (DIR d : DIR.ORTHO) {
							if (source.is(it.tx(),  it.ty(), d))
								m |= d.mask();
						}
						SPRITES.cons().BIG.dashed.render(r, m, it.x(), it.y());
					}
					it.next();
				}
				top.remove();
				
			}
		};
	}
	
	@Override
	public void addUI(LISTE<RENDEROBJ> uis){
		section.clear();
		
		VIEW.s().tools.placer.stealButtons(section);
		
		section.pad(20, 0);
		
		section.addRelBody(4, DIR.S, butts);
		butt.body().centerIn(butts);
		section.add(butt);
		
		section.body().centerX(C.DIM());
		
		
		
		p.setButtBg();
		p.inner().set(section);
		
		
		
		p.moveExit(exit);
		section.add(exit);
		p.setTitle(DicMisc.¤¤Copy);
		section.add(p);
		section.moveLastToBack();
		section.body().moveY1(75);
		section.body().centerX(C.DIM());

		uis.add(section);
	}
	
	@Override
	public void update(boolean UIHovered) {
		top.add();
	}
	
	@Override
	public boolean back() {
		VIEW.s().tools.place(first, fConfig);
		return false;
	}
	
}
