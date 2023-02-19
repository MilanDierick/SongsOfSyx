package view.sett.ui.standing.decree;

import init.D;
import init.race.Race;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.STATS;
import settlement.stats.StatsMultipliers.StatMultiplier;
import settlement.stats.StatsMultipliers.StatMultiplierAction;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;

public final class UIDecreeButt extends GButt.ButtPanel{
	
	static CharSequence ¤¤title = "¤Decrees";
	static CharSequence ¤¤desc = "¤Options for a ruler to increase fulfillment.";
	static {
		D.ts(UIDecreeButt.class);
	}
	
	private final GETTER<Race> racegetter;
	private final ISidePanel pp;
	private final HCLASS cl;
	
	public UIDecreeButt(HCLASS cl, GETTER<Race> race) {
		super(new SPRITE.Imp(180, UI.FONT().H2.height()) {
			private final GText t = new GText(UI.FONT().S, 8);
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				GCOLOR.T().H1.bind();
				UI.FONT().H2.render(r, ¤¤title, X1, Y1);
				t.clear();
				double mul = 1;
				for (StatMultiplier m : STATS.MULTIPLIERS().get(cl)) {
					if (m instanceof StatMultiplierAction)
						mul *= m.multiplier(cl, race.get(), 0);
				}
				GFORMAT.f1(t, mul);
				t.adjustWidth();
				t.renderCY(r, X2-t.width()-16, Y1 + (Y2-Y1)/2);
				COLOR.unbind();
			}
		});
		this.racegetter = race;
		this.cl = cl;
		body.incrW(16);
		pp = new DPanel(cl, race);
	}
	
	@Override
	protected void renAction() {
		activeSet(cl != HCLASS.CITIZEN || race() != null);
		selectedSet(VIEW.s().panels.added(pp));
	}
	
	@Override
	protected void clickA() {
		VIEW.s().panels.addDontRemove(VIEW.s().ui.standing, VIEW.s().ui.slaves, pp);
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		text.title(¤¤title);
		text.text(¤¤desc);
		text.NL(16);
		for (StatMultiplier m : STATS.MULTIPLIERS().get(cl)) {
			if (m.key != null)
				hoverP(m, text, cl, race());
		}
	}
	
	protected Race race() {
		return racegetter.get();
	}

	public static void hover(GUI_BOX box, HCLASS cl, Race race) {
		GBox b = (GBox) box;
		b.NL(8);
		b.textLL(DicMisc.¤¤Multipliers);
		b.NL(4);
		for (StatMultiplier m : STATS.MULTIPLIERS().get(cl)) {
			hoverP(m, b, cl, race);
		}
	}
	
	public static void hoverP(StatMultiplier m, GUI_BOX box, HCLASS cl, Race race) {
		GBox b = (GBox) box;
		b.textL(m.name);
		b.tab(6);
		b.add(GFORMAT.f1(b.text(), m.multiplier(cl, race, 0)));
		b.tab(8);
		{
			GText t = b.text();
			t.add('(').s();
			t.add(m.min(cl, race), 2).s().add('-').s();
			t.add(m.max(cl, race), 2);
			t.s().add(')');
			b.add(t);
		}
		
		b.NL();
		b.text(m.desc);
		b.NL(8);
	}
	
	
	
}
