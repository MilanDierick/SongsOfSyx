package view.sett.ui.standing;

import init.D;
import init.race.RACES;
import init.race.Race;
import init.resources.*;
import init.sprite.ICON;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.*;
import settlement.stats.StatsEquippables.StatEquippableCivic;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.DOUBLE;
import util.data.INT.INTE;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.slider.GAllocator;
import util.gui.slider.GTarget;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.sett.ui.standing.Cats.Cat;

class CatAccess extends Cat {
	
	private static CharSequence ¤¤PreferedBy = "¤Preferred By:";
	private static CharSequence ¤¤Allowed = "¤Allowed to consume";
	private static CharSequence ¤¤AllowedNot = "¤Not allowed to consume";
	private static CharSequence ¤¤Yearly = "¤{0} per item per year, estimation: -{1} in total per year.";
	private static CharSequence ¤¤FurnitureD = "The amount allowed to furnish a subject's home. More allowed and available will increase happiness from furnishing.";
	
	static {
		D.ts(CatAccess.class);
	}
	
	CatAccess(HCLASS c){
		super(new StatCollection[] {
			STATS.FOOD(), STATS.EQUIP(), STATS.HOME()
		});
		titleSet(DicMisc.¤¤Access);
		
		LinkedList<RENDEROBJ> rens = new LinkedList<>();
		
		{
			StatsFood s = STATS.FOOD();
			
			rens.add(new StatRow.Title(s.info));
			
			for (STAT st : s.all()) {
				rens.add(new StatRow(st, c));
			}
			
			GuiSection ss = new GuiSection();
			
			int ww = 6;
			int w = rens.get(rens.size()-1).body().width();
			
			LIST<RESOURCE> ll = RESOURCES.EDI().res();
			
			for (int i = 0; i < ll.size(); i++) {
				RESOURCE e = ll.get(i);
				final int k = i;
				CLICKABLE cl = new GButt.CheckboxTitle(e.icon()) {
					
					@Override
					protected void clickA() {
						s.allowed(k).toggle(c, CitizenMain.current);
					}
					
					@Override
					protected void renAction() {
						selectedSet(s.allowed(k).get(c, CitizenMain.current));
					}
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
						
						if (CitizenMain.current != null && (CitizenMain.current.pref().foodMask & e.bit) != 0) {
							COLOR.WHITE100.render(r, body, 1);
							COLOR.WHITE15.render(r, body, 0);
						}
						super.render(r, ds, isActive, isSelected, isHovered);
						
						
					};
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						text.title(e.name);
						if (selectedIs())
							b.text(¤¤Allowed);
						else
							b.error(¤¤AllowedNot);
						b.NL(4);
						b.textLL(¤¤PreferedBy);
						b.NL();
						if (RESOURCES.EDI().is(e)) {
							for (Race r : RACES.all()) {
								if (r.pref().food.contains(RESOURCES.EDI().toEdible(e))){
									b.add(r.appearance().iconBig);
								}
							}
						}

							
					};
					
					
				}.hoverTitleSet(e.name);
				
				ss.add(cl, w*(i%ww)/ww, (i/ww)*30);
			}
			
			rens.add(ss);
		}
		
		{
			StatsEquippables s = STATS.EQUIP();
			rens.add(new StatRow.Title(s.info));
			
			for (StatEquippableCivic ss : s.civics()) {
				rens.add(new StatRowEquip(ss, c));
			}
		}
		
		{

			LIST<RES_AMOUNT> rr = RACES.homeResMax(c);
			
			
			StatsHome s = STATS.HOME();
			rens.add(new StatRow.Title(s.info));
			
			for (STAT st : s.all()) {
				rens.add(new StatRow(st, c));
					
			}
			
			GuiSection ss = new GuiSection();
			
			for (int ri = 0; ri < rr.size(); ri++) {
				ss.add(new StatHomeFurniture(ri, c, rr), (ri%3)*170, 38*(ri/3));
				
				
			}
			
			rens.add(ss);
			
		}
		
		
		
		
		
		
		
		section.add(new GScrollRows(rens, HEIGHT, 0).view());
		
	}
	
	private static class StatHomeFurniture extends GuiSection {
		
		private final int ri;
		private final HCLASS cl;
		private final LIST<RES_AMOUNT> rr;
		
		public StatHomeFurniture(int resource, HCLASS cl, LIST<RES_AMOUNT> rr) {
			ri = resource;
			this.cl = cl;
			this.rr = rr;
			final INTE tar = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					if (res() == null)
						return 1;
					return res().amount();
				}
				
				@Override
				public int get() {
					return STATS.HOME().target(cl, CitizenMain.current, res().resource());
				}
				
				@Override
				public void set(int t) {
					STATS.HOME().targetSet(t, cl, CitizenMain.current, res().resource());
				}
			};
			
			GAllocator al = new GAllocator(COLOR.ORANGE100, tar, 6, 16, 16);
			
			add(new SPRITE.Imp(ICON.MEDIUM.SIZE) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					res().resource().icon().render(r, X1, Y1);
					
				}
			}, 0, 0);
			
			DOUBLE d = new DOUBLE() {
				
				@Override
				public double getD() {
					double tot = STATS.HOME().needed(cl, CitizenMain.current, ri);
					if (tot == 0)
						return 0;
					double am = STATS.HOME().current(cl, CitizenMain.current, ri);
					return am/tot;
				}
			};
			
			add(new GMeter.GMeterSprite(GMeter.C_REDGREEN, d, 130, 16), body().x2()+4, 0);
			
			add(al, getLastX1(), getLastY2()+2);
			
			pad(8, 8);
			
		}
		
		private RES_AMOUNT res(){
			if (CitizenMain.current == null) {
				if (ri >= rr.size())
					return null;
				return rr.get(ri);
			}
			
			if (CitizenMain.current.home().clas(cl).resources().size() <= ri)
				return null;
			
			return CitizenMain.current.home().clas(cl).resources().get(ri);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			activeSet(res() != null);
			if (activeIs())
				super.render(r, ds);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (res() == null)
				return;
			GBox b = (GBox) text;
			b.title(res().resource().name);
			
			b.text(¤¤FurnitureD);
			b.NL(8);
			
			
			int tot = STATS.HOME().needed(cl, CitizenMain.current, ri);
			int am = STATS.HOME().current(cl, CitizenMain.current, ri);
			b.add(GFORMAT.iofkInv(b.text(), am, tot));
			b.NL(8);
			
			b.textL(DicMisc.¤¤ConsumptionRate);
			b.NL();
			GText t = b.text();
			t.add(¤¤Yearly);
			t.insert(0, STATS.HOME().rate(cl, CitizenMain.current), 2);
			t.insert(1, (int)(STATS.HOME().rate(cl, CitizenMain.current)*STATS.HOME().current(cl, CitizenMain.current, ri)));
			b.add(t);
			super.hoverInfoGet(text);
		}
		
	}
	
	static class StatRowEquip extends GuiSection{

		private final StatEquippableCivic ss;
		private final HCLASS cl;
		
		public StatRowEquip(StatEquippableCivic ss, HCLASS cl) {
			this.ss = ss;
			this.cl = cl;
			
			add(new StatRow.Arrow(ss.stat(), cl));
			addRightC(4, ss.resource.icon());
			
			StatEquippableCivic s = ss;
			INTE in = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return s.max();
				}
				
				@Override
				public int get() {
					return s.target(cl, CitizenMain.current);
				}
				
				@Override
				public void set(int t) {
					s.targetSet(t, cl, CitizenMain.current);
				}
			};
			
			addRightC(16, new GTarget(40, false, true, in).hoverInfoSet(ss.sTarget));
			
			add(new GStat() {
				
				@Override
				public void update(GText text) {
					StatRow.format(text, ss.stat(), ss.stat().data(cl).getD(CitizenMain.current), cl);
				}
			}, 230, 0);
			

			
			add(new StatRow.Meter(ss.stat(), cl), 320, 0);
			pad(2, 4);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (!isHoveringAHoverElement()) {
				StatRow.hoverStat(text, ss.stat(), cl);
				ss.hover(text, cl, CitizenMain.current);
				text.NL();
				StatRow.hoverStanding(text, ss.stat(), cl);
			}else {
				super.hoverInfoGet(text);
			}
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);
			GCOLOR.UI().border().render(r, body().x1(), body().x2(), body().y2()-1, body().y2());
		}
		
	}

}