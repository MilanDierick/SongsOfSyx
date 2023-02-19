package view.sett.ui.subject;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.D;
import init.boostable.*;
import init.race.RACES;
import init.sprite.UI.UI;
import settlement.stats.STAT;
import settlement.stats.STATS;
import settlement.stats.StatsNeeds.StatNeed;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import util.colors.GCOLOR;
import util.colors.GCOLOR_UI;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.misc.GMeter.GGaugeColor;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;

final class SProperties {
	
	private final GuiSection section = new GuiSection();
	private final UISubject a;
	private static CharSequence ¤¤percPerDay = "¤Rate: {0}% / day";
	
	static {
		D.ts(SProperties.class);
	}
	
	SProperties(UISubject a, int height) {
		this.a = a;
		
		section.addRelBody(8, DIR.S, makeStats());
		section.addRelBody(8, DIR.S, makeProperties(height-section.body().height()-16));
	}


	private RENDEROBJ makeProperties(int height) {
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		for (BoostableCollection cat : BOOSTABLES.colls()) {
			
			if (cat == BOOSTABLES.RATES())
				continue;
			
			GuiSection s = null;
			
		
			rows.add(new GText(UI.FONT().H2, cat.name).lablify().r(DIR.E));
			
			for (BOOSTABLE b : cat.all()) {
				Boo boo = new Boo(b);
				if (s == null || s.elements().size() >= 4) {
					s = new GuiSection();
					rows.add(s);
				}
				s.addRight(2, boo);
			}
			
			rows.add(new RENDEROBJ.Sprite(10, 8));
		}
	
		GuiSection s = new GuiSection();
		GScrollRows sc = new GScrollRows(rows, height-s.body().height()-4, 0);
		s.addDownC(4, sc.view());
		
		return s;
	}
	
	private class Boo extends HOVERABLE.HoverableAbs {

		private final BOOSTABLE boo;
		private final GText text = new GText(UI.FONT().S, 6);

		Boo(BOOSTABLE b){
			super(100, 26);
			this.boo = b;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			GCOLOR.UI().border().render(r, body,-1);
			GCOLOR.UI().bg(true, false, isHovered).render(r, body,-2);
			double curr = boo.get(a.a.indu());
			double m = boo.max(a.a.indu());
			
			if (m > 0) {
				COLOR col = GCOLOR_UI.color(GCOLOR.UI().NEUTRAL.inactive, true, false, isHovered);
				int w = (int) (curr/m*(body().width()-6));
				col.render(r, body().x1()+3, body().x1()+3+w, body().y1()+3, body().y2()-3);
						
			}
			
			
			boo.icon().render(r, body().x1()+3, body().y1()+3);
			
			text.clear();
			GFORMAT.fRel(text, curr, boo.race(a.a.race()));
			text.render(r, body().x1()+23, body().y1()+3);
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(boo.name);
			b.text(boo.desc);
			b.NL(8);
			
			hoverBoost(boo, text);
			
		}
		
		
	}
	
	GuiSection makeStats(){
		GuiSection sec = new GuiSection();
		
		GText work = new GText(UI.FONT().S, 32);
		for (StatNeed s : STATS.NEEDS().NEEDS) {
			CLICKABLE c = makeNeed(s, s.stat, work); 				
			sec.addDown(2, c);
		}
		for (STAT s : STATS.NEEDS().OTHERS) {
			CLICKABLE c = makeNeed(null, s, work); 				
			sec.addDown(2, c);
		}
		
		
		return sec;
	}
	

	CLICKABLE makeNeed(StatNeed n, STAT s, GText work) {
		return new CLICKABLE.ClickableAbs(300, 20) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				
				if (isHovered) {
					COLOR.BLUEDARK.render(r, body());
				}
				
				int x1 = body().x1();
				
				if (n != null)
					n.rate.icon().renderCY(r, x1, body().cY());
				x1+= 20;
				
				work.setFont(UI.FONT().S);
				work.clear();
				work.add(s.info().name);
				work.lablifySub();
				work.render(r, x1, body().y1());
				x1+= 180;
				
				int i = s.indu().get(a.a.indu());
				double m = s.indu().max(a.a.indu());
				double d = i/m;
				
				GGaugeColor c = GMeter.C_GREEN; 
				if (n == null || i > StatNeed.breakPoint) {
					c = GMeter.C_RED;
				}
				GMeter.render(r, c, 
						d, 
						x1, x1+50, body().y1()+3, body().y2()-3);
				
				
				
				if (n != null) {
					int x = (int) (x1 + 50*(StatNeed.breakPoint/m));
					GCOLOR.UI().border().render(r, x, x+1, body().y1()+3, body().y2()-3);
				}
				
				x1 += 60;
				
				work.clear();
				
				if (n != null){
					double def = n.rate.defValue*RACES.bonus().tot(n.rate, a.a.race());
					double rate = n.rate.get(a.a);
					GFORMAT.perc(work, rate);
					if (rate > def)
						work.color(GCOLOR.T().IBAD);
					else if (rate < def)
						work.color(GCOLOR.T().IGREAT);
					else
						work.color(GCOLOR.T().INORMAL);
					work.renderCY(r, x1, body().cY());
				}

			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(s.info().name);
				text.text(s.info().desc);
				text.NL(4);
				GBox b = (GBox) text;
				double d = s.indu().getD(a.a.indu());
				b.add(GFORMAT.perc(b.text(), d));
				
				if (n != null) {
					b.NL(8);
					GText t = b.text();
					t.add(¤¤percPerDay).insert(0, (int)(100*n.rate.get(a.a.indu())));
					t.lablify();
					b.add(t);
					b.NL();
					hoverBoost(n.rate, text);
				}
			}
			
			@Override
			protected void clickA() {
				SDebugInput.activate(s.indu(), a.a);
			}
		};
	}
	
	private void hoverBoost(BOOSTABLE boo, GUI_BOX text) {
		GBox b = (GBox) text;
		Faction f = a.a.indu().clas().player ? FACTIONS.player() : FACTIONS.other();
		
		b.text(DicMisc.¤¤Base);
		b.tab(6);
		GText t = b.text();
		t.add('*');
		b.add(GFORMAT.f(t, boo.defValue));
		
		for (BOOSTER_COLLECTION.SIMPLE c : f.bonus().subs()) {
			c.hover(b, boo);
		}
		
		RACES.bonus().hover(b, boo, a.a.race());
		
		STATS.BOOST().hover(b, boo, a.a.indu());
	}
	
	GuiSection activate() {
		return section;
	}


}
