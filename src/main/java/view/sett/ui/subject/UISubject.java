package view.sett.ui.subject;

import init.D;
import init.sprite.SPRITES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.standing.STANDINGS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Str;
import util.dic.DicMisc;
import util.gui.misc.GButt;
import view.interrupter.ISidePanel;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.sett.ui.subject.UISubjects.Panel;

final class UISubject extends ISidePanel implements Panel{
	
	Humanoid a;
	private final Str title = new Str(24);
	int follow = 20;
	private final GuiSection s = new GuiSection() {
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);
			if (a != null) {
				SETT.OVERLAY().add(a);
				
				
				if (KEYS.anyDown())
					follow--;
				if (follow > 0)
					VIEW.s().getWindow().centerer.set(a.body().cX(), a.body().cY());
			}
		};
	};

	
	private final SInfo info = new SInfo(this, ISidePanel.HEIGHT-40);
	private final SProperties prop = new SProperties(this, ISidePanel.HEIGHT-40);
	private final SStats stats = new SStats(this, ISidePanel.HEIGHT-40);
	private Object current;
	private static CharSequence ¤¤race = "¤Read up about current race in the tome of knowledge.";
	private static CharSequence ¤¤favourite = "¤Mark as favourite";
	
	static {
		D.ts(UISubject.class);
	}
	
	UISubject() {

		s.addRightC(8, new GButt.ButtPanel(SPRITES.icons().m.heart) {
			
			@Override
			protected void clickA() {
				a.indu().favSet(!a.indu().favorite());
			}
			
			@Override
			protected void renAction() {
				selectedSet(a.indu().favorite());
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(¤¤favourite);
			}
		}.pad(8, 1));
		
		s.addRightC(8, new GButt.ButtPanel(DicMisc.¤¤Info) {
			@Override
			protected void clickA() {
				set(info, info.activate());
				super.clickA();
			}
			
			@Override
			protected void renAction() {
				selectedSet(current == info);
			}
		}.pad(4, 1));
		
		s.addRightC(8, new GButt.ButtPanel(DicMisc.¤¤Properites) {
			@Override
			protected void clickA() {
				set(prop, prop.activate());
				super.clickA();
			}
			
			@Override
			protected void renAction() {
				selectedSet(current == prop);
			}
		}.pad(4, 1));
		
		s.addRightC(8, new GButt.ButtPanel(STANDINGS.CITIZEN().fullfillment.info().name) {
			@Override
			protected void clickA() {
				set(stats, stats.activate());
				super.clickA();
			}
			
			@Override
			protected void renAction() {
				selectedSet(current == stats);
			}
		}.pad(4, 1));
		
		s.addRightC(8, new GButt.ButtPanel(SPRITES.icons().m.questionmark) {
			@Override
			protected void clickA() {
				VIEW.inters().wiki.showRace(a.race());

			}
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(¤¤race);
				super.hoverInfoGet(text);
			}
		}.pad(8, 1));
		
		int w = 0;
		w = Math.max(w, info.activate().body().width());
		w = Math.max(w, prop.activate().body().width());
		w = Math.max(w, stats.activate().body().width());
		
//		s.addDownC(C.SG*4, makeServiceAccess(s.body().width()));
//		int width = s.body().width()/2;
//		int height = ISidePanel.HEIGHT-s.body().height()-16;
//		s.add(makeProperties(height, width-24), s.body().x1(), s.body().y2()+16);
//		s.add(makeStats(height, width+24), s.getLastX2()+8, s.getLastY1());
		
		section().body().setDim(w, 1);
		
		set(info, info.activate());
		
		
		new SPortraitsDebug();

		
	}
	
	void set(Object o, GuiSection s) {
		current = o;
		int w = section().body().width();
		int x1 = section().body().x1();
		int y1 = section().body().y1();
		section().clear();
		section().body().setDim(w, 1);
		
		section().addRelBody(2, DIR.S, this.s);
		section().addDownC(8, s);
		section().body().moveX1Y1(x1, y1);
	}

	@Override
	public void activate(Humanoid a, ISidePanel p) {
		this.a = a;
		title.clear();
		title.add(a.race().info.namePosessive).add(' ').add(a.indu().hType().name);
		titleSet(title);
		//sname.text().clear().add(STATS.APPEARANCE().name(a.indu()));
		follow = 20;
		VIEW.s().panels.addDontRemove(p, this);
		VIEW.s().getWindow().centerer.set(a.body().cX(), a.body().cY());
	}

	@Override
	public Humanoid showing() {
		if (VIEW.s().panels.added(this))
			return a;
		return null;
	}
}

