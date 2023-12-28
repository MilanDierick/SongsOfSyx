package view.sett.ui.standing;

import game.boosting.BoostSpec;
import game.time.TIME;
import init.D;
import init.race.RACES;
import init.race.Race;
import init.religion.Religion;
import init.religion.Religions;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.room.spirit.grave.GraveData;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBurial;
import settlement.stats.colls.StatsBurial.StatGrave;
import settlement.stats.colls.StatsReligion;
import settlement.stats.colls.StatsReligion.StatReligion;
import settlement.stats.stat.STAT;
import settlement.stats.stat.StatCollection;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import util.colors.GCOLOR;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import view.main.VIEW;
import view.sett.ui.standing.Cats.Cat;

final class CatReligion extends Cat{

	private static CharSequence ¤¤Burrial = "¤Burial";
	private static CharSequence ¤¤AllowRace = "¤Allow/deny access for Species";
	private static CharSequence ¤¤Allow = "¤Allow/deny access for whole class";
	static {
		D.ts(CatReligion.class);
	}
	
	CatReligion(HCLASS cl) {
		super(new StatCollection[] { STATS.RELIGION(), STATS.BURIAL()});
		titleSet(cs[0].info.name);
		
		section.add(dvision(cl));
		
		GGrid grid = new GGrid(section, 2, section.body().y2()+4);
		for (StatReligion r : STATS.RELIGION().ALL){
			grid.add(temple(r, cl));
		}
		
		{
			
			grid = new GGrid(section, 6, section.body().y2()+4);
			for (BoostSpec ss : STATS.RELIGION().TEMPLE_TOTAL.boosters.all()) {
				grid.add(new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.f0(text, ss.inc(RACES.clP(CitizenMain.current, cl)));
					}
					
					@Override
					public void hoverInfoGet(GBox bb) {
						
						
						
						bb.title(ss.tName);
						double d = 0;
						for (Religion rr : Religions.ALL()) {
							for (BoostSpec sb : rr.bsett.all()) {
								if (sb.boostable == ss.boostable) {
									sb.booster.hoverDetailed(bb, sb.booster.get(sb.boostable, RACES.clP(CitizenMain.current, cl)));
									bb.NL();
								}
							};
						}
						bb.NL(8);
						
						bb.textLL(DicMisc.¤¤Boosts);
						bb.tab(7);
						bb.add(GFORMAT.f0(bb.text(), d));
						
					};
				}.hh(ss.boostable.icon));
			}
		}
		
		
		LinkedList<RENDEROBJ> rens = new LinkedList<>();
		
		{
			StatsReligion c = STATS.RELIGION();
			for (STAT s : c.all()) {
				if (s == c.OPPOSITION) {
					rens.add(new StatRow(s, cl) {
						@Override
						public void hoverInfoGet(GUI_BOX text) {
							GBox b = (GBox) text;
							hoverStat(text, s, cl);
							text.NL(4);
							for (int x = 0; x < c.ALL.size(); x++) {
								b.tab(1+x*2);
								b.add(c.ALL.get(x).religion.icon.small);
								
							}
							b.NL(4);
							for (int y = 0; y < c.ALL.size(); y++) {
								StatReligion r = c.ALL.get(y);
								b.add(r.religion.icon.small);
								for (int x = 0; x < c.ALL.size(); x++) {
									b.tab(1+x*2);
									if (r.opposition(c.ALL.get(x)) > 0)
										b.add(GFORMAT.f(b.text(), r.opposition(c.ALL.get(x))).errorify());
									else
										b.add(GFORMAT.f(b.text(), r.opposition(c.ALL.get(x))).normalify());
								}
								b.NL(2);
								
							}
							
							
							hoverStanding(text, s, cl);
						}
					});
				}else
					rens.add(new StatRow(s, cl));
				
			}
		}

		{
			StatsBurial s = STATS.BURIAL();
			rens.add(new StatRowGrave(cl));
			for (STAT ss : s.others())
				rens.add(new StatRow(ss, cl));
			
			
		}
		section.add(new GScrollRows(rens, HEIGHT-section.body().height()-8, 0).view(), 0, section.body().y2()+4);

	}
	
	private static RENDEROBJ dvision(HCLASS cl) {
		GStaples s = new GStaples(STATS.DAYS_SAVED) {
			
			@Override
			protected void hover(GBox box, int stapleI) {
				box.title(STATS.RELIGION().ALL.get(0).followers.info().name);
				int i = STATS.DAYS_SAVED - stapleI - 1;
				box.add(box.text().add(-i).s().add(TIME.days().cycleName()));
				box.NL(8);
				
				for (StatReligion s : STATS.RELIGION().ALL) {
					box.add(s.religion.icon);
					box.add(GFORMAT.i(box.text(), s.followers.data(cl).get(CitizenMain.current, i)));
					box.tab(7);
					if (i < STATS.DAYS_SAVED-1)
					box.add(GFORMAT.iIncr(box.text(), s.followers.data(cl).get(CitizenMain.current, i)-s.followers.data(cl).get(CitizenMain.current, i+1)));
					box.NL();
				}
			}
			
			@Override
			protected double getValue(int stapleI) {
				int i = STATS.DAYS_SAVED - stapleI - 1;
				return STATS.POP().POP.data(cl).get(CitizenMain.current, i);
			}
			
			@Override
			protected void renderExtra(SPRITE_RENDERER r, COLOR color, int stapleI, boolean hovered, double value,
					int x1, int x2, int y1, int y2) {
				
				
				int i = STATS.DAYS_SAVED-stapleI-1;
				
				int h = y2-y1;
				if (h <= 0)
					h = 1;
				
				for (StatReligion s : STATS.RELIGION().ALL) {
					int hh = (int) Math.ceil(h*s.followers.data(cl).getD(CitizenMain.current, i));
					if (hh > 0) {
						ColorImp c = ColorImp.TMP;
						c.set(s.religion.color);
						c.shadeSelf(hovered ? 0.75 : 0.55);
						c.render(r, x1, x2, y2-hh, y2);
						c.set(s.religion.color);
						c.shadeSelf(hovered ? 1 : 0.80);
						c.render(r, x1+1, x2-1, y2-hh+1, y2-1);
						y2-= hh;
					}
				}
			}
		};
		
		s.body().setWidth(StatRow.Width);
		s.body().setHeight(80);
		return s;
	}
	
	private static RENDEROBJ temple(StatReligion ss, HCLASS cl) {
		GuiSection s = new GuiSection() {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (!isHoveringAHoverElement()) {
					
					GBox b = (GBox) text;
					b.title(ss.info.name);
					b.text(ss.info.desc);
					b.NL(8);
					
					b.textL(ss.followers.info().name);
					b.tab(8);
					b.add(GFORMAT.i(b.text(), ss.followers.data(cl).get(CitizenMain.current)));
					b.NL();
					
					b.textL(ss.temple_access.info().name);
					b.tab(8);
					b.add(GFORMAT.perc(b.text(), ss.temple_access.data(cl).getD(CitizenMain.current)));
					b.NL();
					b.add(VIEW.s().ui.standing.hi.init(cl, ss.temple_access, false));
					b.NL(4);
					
					b.textL(ss.temple_quality.info().name);
					b.tab(8);
					b.add(GFORMAT.perc(b.text(), ss.temple_quality.data(cl).getD(CitizenMain.current)));
					b.NL();
					b.add(VIEW.s().ui.standing.hi.init(cl, ss.temple_quality, false));
					b.NL(4);
					
					b.NL(8);
					b.textLL(DicMisc.¤¤Boosts);
					b.NL();
					
					ss.religion.bsett.hover(text, RACES.clP(CitizenMain.current, cl));
					
				
				}
				super.hoverInfoGet(text);
			}
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				GCOLOR.UI().border().render(r, body());
				GCOLOR.UI().bg().render(r, body(), -1);
				super.render(r, ds);
			}
		};
		
		s.add(new GButt.Checkbox() {
			@Override
			protected void clickA() {
				ss.permission.toggle(cl, CitizenMain.current);
			}
			
			@Override
			protected void renAction() {
				selectedSet(is());
			}
			
			private boolean is() {
				return ss.permission.get(cl, CitizenMain.current);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (CitizenMain.current != null)
					text.text(¤¤AllowRace);
				else
					text.text(¤¤Allow);
			}
		});
		
		s.addRightC(16, ss.religion.icon);
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, ss.followers.data(cl).get(CitizenMain.current));
			}
		}, s.getLastX2()+8, s.getLastY1());
		
		s.addDown(2, new RENDEROBJ.RenderImp(100, 16) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				double v = ss.temple_quality.data(cl).getD(CitizenMain.current)*ss.temple_access.data(cl).getD(CitizenMain.current);
				GMeter.render(r, GMeter.C_BLUE, v, body());
			}
		});
		
		s.pad(10, 6);

		return s;
	}
	
	final static class StatRowGrave extends GuiSection{

		private final HCLASS cl;
		
		StatRowGrave(HCLASS cl){
			
			this.cl = cl;
			
			boolean has = false;
			for (StatGrave ss : STATS.BURIAL().graves()) {
				for (Race r : RACES.all()) {
					if (ss.standing().definition(r).get(cl).max > 0) {
						has = true;
						break;
					}
				}
			}
			if (!has)
				return;
			
			add(new GText(UI.FONT().H2, ¤¤Burrial).lablify(), 0, 0);
			addRightCAbs(StatRow.StatX, new GStat() {
				
				@Override
				public void update(GText text) {
					double d = 0;
					for (StatGrave ss : STATS.BURIAL().graves()) {
						d += ss.data(cl).getD(CitizenMain.current);
					}
					GFORMAT.perc(text,d);
				}
			});
			
			addCentredY(new RENDEROBJ.RenderImp(StatRow.MeterW, 20) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					double max = 0;
					double now = 0;
					double nor = 0;
					double prev = 0;
					for (StatGrave s : STATS.BURIAL().graves()) {
						max = Math.max(max, s.standing().max(cl, CitizenMain.current));
						now = Math.max(now, s.standing().get(cl, CitizenMain.current));
						prev = Math.max(prev, s.standing().getPrev(cl, CitizenMain.current, 8));
						nor = Math.max(nor, s.standing().normalized(cl, CitizenMain.current));
					}
					
					GMeter.renderDelta(r, prev/max, now/max, body.x1(), (int) (body().x1() + body().width()*nor), body().y1(), body().y2());
				}
			}, StatRow.MeterX);
			
			for (StatGrave ss : STATS.BURIAL().graves()) {
				for (Race r : RACES.all()) {
					if (ss.standing().definition(r).get(cl).max > 0) {
						add(service(ss), 20, getLastY2()+2);
						break;
					}
				}
				
			}
			
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
		
		private RENDEROBJ service(StatGrave ss) {
			GuiSection s = new GuiSection() {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					if (!isHoveringAHoverElement()) {
						GBox b = (GBox) text;
						b.title(ss.info().name);
						GraveData da = ss.grave();

						b.textLL(da.respect.info().name);
						b.add(GFORMAT.perc(b.text(), da.respect.getD(null)));
						b.NL().text(da.respect.info().desc);
						b.NL(4);
						b.textLL(da.get(cl).burried.info().name);
						b.add(GFORMAT.iofkInv(b.text(), (int)da.get(cl).burried.getD(CitizenMain.current), (int)da.get(cl).burried.getD(CitizenMain.current)+ (int)da.get(cl).failed.getD(CitizenMain.current) ));
						
						b.NL().text(da.get(cl).burried.info().desc);
						b.NL(4);
						StatRow.hoverStanding(b, ss, cl);
					}
					super.hoverInfoGet(text);
				}
			};
			s.add(new StatRow.Arrow(ss, cl));
			s.addRightC(4, new GButt.Checkbox() {
				@Override
				protected void clickA() {
					ss.grave().permission().toggle(cl, CitizenMain.current);
				}
				
				@Override
				protected void renAction() {
					selectedSet(is());
				}
				
				private boolean is() {
					return ss.grave().permission().get(cl, CitizenMain.current);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					if (CitizenMain.current != null)
						text.text(¤¤AllowRace);
					else
						text.text(¤¤Allow);
				}
			});
			s.addRightC(4, ss.grave().blueprint().iconBig());
			s.addRightC(4, new GText(UI.FONT().S, ss.grave().blueprint().info.names).lablifySub());
			s.addCentredY(new GStat() {
				
				@Override
				public void update(GText text) {
					text.setFont(UI.FONT().S);
					
					StatRow.format(text, ss, ss.data(cl).getD(CitizenMain.current), cl);
				}
			}, StatRow.StatX-20);
			
			s.addCentredY(new RENDEROBJ.RenderImp(StatRow.MeterW, 12) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					double max = ss.standing().max(cl, CitizenMain.current);
					double now = ss.standing().get(cl, CitizenMain.current);
					double nor = ss.standing().normalized(cl, CitizenMain.current);
					GMeter.render(r, GMeter.C_BLUE, now/max, body.x1(), (int) (body().x1() + body().width()*nor), body().y1(), body().y2());
				}
			}, StatRow.MeterX-20);
			

			return s;
		}
		
	}

}
