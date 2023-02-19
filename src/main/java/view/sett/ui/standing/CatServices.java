package view.sett.ui.standing;

import init.D;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.*;
import settlement.stats.StatsService.StatService;
import settlement.stats.StatsService.StatServiceGroup;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import util.colors.GCOLOR;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.sett.ui.standing.Cats.Cat;

final class CatServices extends Cat {
	
	private static CharSequence ¤¤Other = "¤Other Services";
	private static CharSequence ¤¤AllowRace = "¤Allow/deny access for Species";
	private static CharSequence ¤¤Allow = "¤Allow/deny access for whole class";
	
	static {
		D.ts(CatServices.class);
	}
	
	CatServices(HCLASS cl){
		super(new StatCollection[] {STATS.SERVICE()});
		StatsService s = STATS.SERVICE();
		
		titleSet(s.info.name);
		ArrayList<RENDEROBJ> rens = new ArrayList<>(s.all().size() + 1);
		
		for (StatServiceGroup g : s.groups()) {
			rens.add(new StatRowService(g, cl, false));
		}

		{
			GuiSection sec = new GuiSection();
			sec.add(new GHeader(¤¤Other));
			for (StatService ss : s.others()) {
				boolean has = false;
				for (Race r : RACES.all()) {
					if (ss.total().standing().definition(r).get(cl).max > 0) {
						has = true;
						break;
					}
				}
				if (!has)
					continue;
				
				sec.add(new StatRowService(ss, cl), 20, sec.getLastY2()+2);
			}
			sec.pad(2,5);
			rens.add(sec);
		}
		
		section.add(new GScrollRows(rens, HEIGHT, 0).view());
		
	}
	
	static final class StatRowService extends GuiSection{

		private final HCLASS cl;
		
		StatRowService(StatServiceGroup g, HCLASS cl, boolean force){
			
			this.cl = cl;
			
			boolean has = false;
			for (StatService ss : g.all()) {
				for (Race r : RACES.all()) {
					if (ss.total().standing().definition(r).get(cl).max > 0 || force) {
						has = true;
						break;
					}
				}
			}
			if (!has)
				return;
			
			add(new GText(UI.FONT().H2, g.name).lablify(), 0, 0);
			addCentredY(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, g.getD(cl, CitizenMain.current, 0));
				}
			}, StatRow.StatX);
			
			addCentredY(new RENDEROBJ.RenderImp(StatRow.MeterW, 20) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
//					double max = g.maxHappiness(cl, CitizenMain.current);
					double now = g.getD(cl, CitizenMain.current, 0);
					double nor = g.nomalized(cl, CitizenMain.current);
//					for (StatService s : g.all()) {
//						max = Math.max(max, s.total().standing().max(cl, CitizenMain.current));
//						now += Math.max(now, s.total().standing().get(cl, CitizenMain.current));
//						nor = Math.max(nor, s.total().standing().normalized(cl, CitizenMain.current));
//					}
					GMeter.render(r, GMeter.C_REDGREEN, now, body.x1(), (int) (body().x1() + body().width()*nor), body().y1(), body().y2());
				}
			}, StatRow.MeterX);
			
			for (StatService ss : g.all()) {
				for (Race r : RACES.all()) {
					if (ss.total().standing().definition(r).get(cl).max > 0 || force) {
						add(service(ss, false), 20, getLastY2()+2);
						break;
					}
				}
				
			}
			
			pad(2,5);
		}
		
		StatRowService(StatService g, HCLASS cl){
			
			this.cl = cl;
			
			add(service(g, true), 20, 0);
			pad(2,5);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);
			GCOLOR.UI().border().render(r, body().x1(), body().x2(), body().y2()-1, body().y2());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (isHoveringAHoverElement()) {
				super.hoverInfoGet(text);
				return;
			}
			super.hoverInfoGet(text);
		}
		
		private RENDEROBJ service(StatService ss, boolean main) {
			GuiSection s = new GuiSection() {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					if (!isHoveringAHoverElement()) {
						StatRow.hoverStat(text, ss.total(), cl);
						GBox b = (GBox) text;
						
						b.NL(8);
						b.textLL(ss.access().info().name);
						b.add(GFORMAT.perc(b.text(), ss.access().data(cl).getD(CitizenMain.current)));
						b.NL().text(ss.access().info().desc);
						b.NL(4);
						b.textLL(ss.proximity().info().name);
						b.add(GFORMAT.perc(b.text(), ss.proximity().data(cl).getD(CitizenMain.current)/ss.access().data(cl).getD(CitizenMain.current)));
						b.NL().text(ss.proximity().info().desc);
						b.NL(4);
						b.textLL(ss.quality().info().name);
						b.add(GFORMAT.perc(b.text(), ss.quality().data(cl).getD(CitizenMain.current)/ss.access().data(cl).getD(CitizenMain.current)));
						b.NL().text(ss.quality().info().desc);
						b.NL(4);
						b.textLL(DicMisc.¤¤Total);
						b.add(GFORMAT.perc(b.text(), ss.total().data(cl).getD(CitizenMain.current)));
						StatRow.hoverStanding(b, ss.total(), cl);
					}
					super.hoverInfoGet(text);
				}
			};
			s.add(new StatRow.Arrow(ss.total(), cl));
			s.addRightC(4, new GButt.Checkbox() {
				@Override
				protected void clickA() {
					ss.permission().toggle(cl, CitizenMain.current);
				}
				
				@Override
				protected void renAction() {
					selectedSet(is());
					//activeSet(ss.usesTarget);
				}
				
				private boolean is() {
					return ss.permission().get(cl, CitizenMain.current);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					if (CitizenMain.current != null)
						text.text(¤¤AllowRace);
					else
						text.text(¤¤Allow);
				}
			});
			s.addRightC(4, ss.service().room().iconBig());
			s.addRightC(4, new GText(UI.FONT().S, ss.service().names).lablifySub());
			s.addCentredY(new GStat() {
				
				@Override
				public void update(GText text) {
					text.setFont(UI.FONT().S);
					StatRow.format(text, ss.total(), ss.total().data(cl).getD(CitizenMain.current), cl);
				}
			}, StatRow.StatX-20);
			
			s.addCentredY(new RENDEROBJ.RenderImp(StatRow.MeterW, main ? 16 : 12) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					double max = ss.total().standing().max(cl, CitizenMain.current);
					double now = ss.total().standing().get(cl, CitizenMain.current);
					double nor = ss.total().standing().normalized(cl, CitizenMain.current);
					GMeter.render(r, main ? GMeter.C_REDGREEN :  GMeter.C_BLUE, now/max, body.x1(), (int) (body().x1() + body().width()*nor), body().y1(), body().y2());
				}
			}, StatRow.MeterX-20);
			

			return s;
		}
		
	}

}