package view.sett.ui.subject;

import init.D;
import init.race.appearence.RPortrait;
import init.settings.S;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.types.prisoner.AIModule_Prisoner;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.law.Processing.Punishment;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import util.dic.DicTime;
import util.gui.misc.*;

class UIHoverer {
	
	private Humanoid h;
	static CharSequence ¤¤Free = "leisure time";
	static CharSequence ¤¤Sentenced = "Sentenced to be:";
	static CharSequence ¤¤ClickToChange = "Click to change punishment.";
	static CharSequence ¤¤JudgedNo = "Pleads innocence. Wants to try case in court.";
	static CharSequence ¤¤Judged = "Has been found guilty in a court.";
	
	static {
		D.ts(UIHoverer.class);
	}
	
	private GuiSection s = new GuiSection();
	
	public UIHoverer() {
		
		s.addRightC(8, new GStat() {
			
			@Override
			public void update(GText text) {
				text.lablify();
				text.clear().add(STATS.APPEARANCE().name(h.indu()));
				text.setMaxWidth(300);
				text.setMultipleLines(false);
			}
		}.increase());
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				text.add(h.race().info.namePosessive);
				text.s().add(h.title());
				
				CharSequence extra = null;
				if (h.indu().hType() == HTYPE.SLAVE)
					extra = h.indu().clas().name;
				else if (h.indu().hType() == HTYPE.PRISONER)
					extra = STATS.LAW().prisonerType.get(h.indu()).title;
				
				if (extra != null)
					text.s().add('(').add(extra).add(')');
				
			}
		}, 0, s.body().y2()+2);
		
		s.addDown(2, new GStat() {
			
			@Override
			public void update(GText text) {
				h.ai().getOccupation(h, text);
				
			}
		});
		
		s.addRelBody(8, DIR.W, new SPRITE() {
			
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				if (h.indu().hType() != HTYPE.CHILD)
					STATS.APPEARANCE().portraitRender(r, h.indu(), X1, Y1, 2);
			}
			
			@Override
			public int width() {
				return RPortrait.P_WIDTH*2;
			}
			
			@Override
			public int height() {
				return RPortrait.P_HEIGHT*2;
			}
		});
		
		s.body().setWidth(550);
	}
	
	void hover(Humanoid h, GBox text) {
		this.h = h;
		
		if (h == null)
			return;
		

		
		
		if (h.indu().hostile() && !S.get().developer) {
			text.error(HTYPE.ENEMY.name);
			return;
		}
		
		text.add(s);
		text.NL();
		
		if (SProblem.problem(h) != null) {
			text.add(text.text().errorify().add(SProblem.problem(h)));
			text.NL();
		}else if (SProblem.warning(h) != null) {
			text.add(text.text().warnify().add(SProblem.warning(h)));
			text.NL();
		}
		
		if (h.indu().hType() == HTYPE.PRISONER) {
			text.text(¤¤Sentenced);
			Punishment p = AIModule_Prisoner.DATA().punishment.get(h.ai());
			text.textLL(p.name);
			if (p == LAW.process().prison) {
				GText t = text.text();
				t.add('(');
				DicTime.setDays(t, AIModule_Prisoner.DATA().prisonTimeLeft.get(h.ai()));
				t.add(')');
				text.add(t);
			}
			
			text.NL(4);
			
			if (AIModule_Prisoner.DATA().noJudge.get(h.ai()) == 0) {
				if (AIModule_Prisoner.DATA().judged.get(h.ai()) == 0)
					text.error(¤¤JudgedNo);
				else
					text.text(¤¤Judged);
				text.NL(4);
			}
			
			text.textL(¤¤ClickToChange);
			
		}
		
		h.ai().hoverInfoSet(h, text);
		
		text.NL(8);
		
//		RoomInstance ins = STATS.WORK().EMPLOYED.get(h);
//		if (ins != null) {
//			text.add(GFORMAT.f(text.text(), h.race().bonus().get(ins.blueprintI().bonuses())));
//		}

	}
}
