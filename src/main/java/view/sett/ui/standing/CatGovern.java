package view.sett.ui.standing;

import init.D;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.STATS;
import settlement.stats.law.StatsLaw.StatLaw;
import settlement.stats.stat.STAT;
import settlement.stats.stat.StatCollection;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import view.main.VIEW;
import view.sett.ui.standing.Cats.Cat;
import view.sett.ui.standing.StatRow.Arrow;
import view.sett.ui.standing.StatRow.Meter;

final class CatGovern extends Cat {
	
	private static CharSequence ¤¤name = "¤Government";
	private static CharSequence ¤¤manageLaw = "¤Manage Law";

	static {
		D.ts(CatGovern.class);
	}
	
	CatGovern(HCLASS cl){
		super(new StatCollection[] { STATS.LAW(), STATS.GOVERN()});
		titleSet(¤¤name);
		
		LinkedList<RENDEROBJ> rens = new LinkedList<>();
		
		rens.add(new StatRow.Title(STATS.GOVERN().info));
		for (STAT s : STATS.GOVERN().all()) {
			GuiSection ss = new StatRow(s, cl);
			rens.add(ss);
		}
		
		rens.add(new StatRow.Title(STATS.LAW().info));
		
		for (STAT s : STATS.LAW().all()) {
			if (s instanceof StatLaw)
				continue;
			GuiSection ss = new StatRow(s, cl);
			rens.add(ss);
		}
		
		rens.add(new StatRow.Title(DicMisc.¤¤Punishment, ""));
		
		for (StatLaw s : STATS.LAW().punishments) {
			rens.add(pRow(s, cl));
		}
		
		rens.add(new GButt.ButtPanel(¤¤manageLaw) {
			@Override
			protected void clickA() {
				VIEW.s().panels.add(VIEW.s().ui.law, true);
			}
		});
		
		

		section.addDown(4, new GScrollRows(rens, HEIGHT, 0).view());
		
	}
	
	private RENDEROBJ pRow(StatLaw s, HCLASS cl) {
		GuiSection res = new GuiSection() {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(s.info().name);
				b.text(s.info().desc);
				b.NL();
				text.NL(8);
				StatRow.hoverStanding(text, s, cl);
			}
		};
		
		res.add(s.p.icon, 0, 0);
		res.addRightCAbs(40, new Arrow(s, cl));
		res.addRightC(2, new GText(UI.FONT().H2, s.info().name).lablify());
		res.addCentredY(new GStat() {
			
			@Override
			public void update(GText text) {
				StatRow.format(text, s, s.data(cl).getD(CitizenMain.current, 0), cl);
			}
		}, StatRow.StatX);
		res.addCentredY(new Meter(s, cl),StatRow.MeterX);
		res.pad(2, 4);
		
		res.add(new SPRITE.Imp(res.body().width(), 1) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				GCOLOR.UI().border().render(r, X1, X2, Y1, Y2);
			}
		}, 0, res.body().y2()-1);
		return res;
		
		
	}

}