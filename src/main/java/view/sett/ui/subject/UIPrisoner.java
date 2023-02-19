package view.sett.ui.subject;

import init.C;
import init.D;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.types.prisoner.AIModule_Prisoner;
import settlement.main.SETT;
import settlement.stats.law.LAW;
import settlement.stats.law.Processing.Punishment;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.panel.GPanelS;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.sett.ui.subject.UISubjects.Panel;

final class UIPrisoner extends GuiSection implements Panel{

	private Humanoid a;
	
	private static CharSequence ¤¤title = "Set Punishment";
	static {
		D.ts(UIPrisoner.class);
	}
	
	UIPrisoner(){
		
		
		
		for (Punishment p : LAW.process().punishments) {
			GButt.ButtPanel b = new GButt.ButtPanel(p.icon) {
				
				@Override
				protected void clickA() {
					if(p != AIModule_Prisoner.DATA().punishment.get(a.ai())) {
						AIModule_Prisoner.DATA().punishment.set(a.ai(), p);
						a.interrupt();
					}
				}
				
				@Override
				protected void renAction() {
					selectedSet(p == AIModule_Prisoner.DATA().punishment.get(a.ai()));
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(p.action);
					b.text(p.desc);
					b.NL(4);
					b.textLL(DicMisc.¤¤Law);
					b.tab(6);
					b.add(GFORMAT.f0(b.text(), p.multiplier));
				}
				
			};
			
			b.setDim(40, 40);
			
			addRight(4, b);
		}
		
		addRelBody(8, DIR.N, new GHeader(¤¤title));
		addRelBody(8, DIR.S, new GButt.ButtPanel(DicMisc.¤¤cancel) {
			@Override
			protected void clickA() {
				VIEW.inters().section.deactivate();
			}
		});
		
		pad(8);
		
		GPanelS p = new GPanelS();
		p.inner().set(this);
		add(p);
		moveLastToBack();
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		if (a == null || a.indu().hType() != HTYPE.PRISONER) {
			VIEW.inters().section.deactivate();
			return;
		}
		body().centerX(VIEW.s().getWindow().viewWindow());
		body().moveY2(C.DIM().cY() - 64);
		SETT.OVERLAY().add(a);
		VIEW.s().getWindow().centerer.set(a.body().cX(), a.body().cY());
		super.render(r, ds);
	}


	@Override
	public Humanoid showing() {
		if (VIEW.inters().section.isActivated() && VIEW.inters().section.section() == this)
			return a;
		return null;
	}


	@Override
	public void activate(Humanoid a, ISidePanel list) {
		
		this.a = a;
		VIEW.inters().section.activate(this);
	}

}
