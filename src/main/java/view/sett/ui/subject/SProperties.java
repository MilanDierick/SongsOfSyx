package view.sett.ui.subject;

import game.boosting.*;
import init.D;
import init.sprite.UI.UI;
import settlement.room.service.module.RoomService;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsNeeds.StatNeed;
import settlement.stats.stat.STAT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.*;
import util.colors.GCOLOR;
import util.colors.GCOLOR_UI;
import util.gui.misc.*;
import util.gui.misc.GMeter.GMeterCol;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;

final class SProperties {
	
	private final GuiSection section = new GuiSection();
	private final UISubject a;
	private static CharSequence ¤¤percPerDay = "¤Increase per day: {0}% / day";
	private static CharSequence ¤¤services = "¤Related Services";
	private static CharSequence ¤¤need = "¤Current Need";
	
	static {
		D.ts(SProperties.class);
	}
	
	SProperties(UISubject a, int height) {
		this.a = a;
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		makeStats(rows);
		makeProperties(rows);
	
		GScrollRows sc = new GScrollRows(rows, height-4, 0);
		section.add(sc.view());
		
	}


	private void makeProperties(LinkedList<RENDEROBJ> rows) {
		
		MapIndexed<Boostable> dis = new MapIndexed<>();
		for (StatNeed s : STATS.NEEDS().SNEEDS) {
			dis.add(s.need.rate);
		}

		
		
		for (BoostableCat cat : BOOSTABLES.colls()) {
			
//			if (cat == BOOSTABLES.RATES())
//				continue;
			
			GuiSection s = null;
			
		
			rows.add(new GText(UI.FONT().H2, cat.name).lablify().r(DIR.E));
			
			for (Boostable b : cat.all()) {
				if (dis.contains(b))
					continue;
				Boo boo = new Boo(b);
				if (s == null || s.elements().size() >= 5) {
					s = new GuiSection();
					rows.add(s);
				}
				s.addRight(2, boo);
			}
			
			rows.add(new RENDEROBJ.Sprite(10, 8));
		}
	
	}
	
	private class Boo extends HOVERABLE.HoverableAbs {

		private final Boostable boo;
		private final GText text = new GText(UI.FONT().S, 6);

		Boo(Boostable b){
			super(100, 26);
			this.boo = b;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			GCOLOR.UI().border().render(r, body,-1);
			GCOLOR.UI().bg(true, false, isHovered).render(r, body,-2);
			double curr = boo.get(a.a.indu());
			double m = boo.max(Induvidual.class);
			
			if (m > 0) {
				COLOR col = GCOLOR_UI.color(GCOLOR.UI().NEUTRAL.inactive, true, false, isHovered);
				int w = (int) (curr/m*(body().width()-6));
				col.render(r, body().x1()+3, body().x1()+3+w, body().y1()+3, body().y2()-3);
						
			}
			
			
			boo.icon.render(r, body().x1()+3, body().y1()+3);
			
			text.clear();
			GFORMAT.fRel(text, curr, boo.get(a.a.race()));
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
	
	void makeStats(LISTE<RENDEROBJ> rows){
	
		GText work = new GText(UI.FONT().S, 32);
		for (StatNeed s : STATS.NEEDS().SNEEDS) {
			CLICKABLE c = makeNeed(s, s.stat(), work); 				
			rows.add(c);
		}
		for (STAT s : STATS.NEEDS().OTHERS) {
			CLICKABLE c = makeNeed(null, s, work); 				
			rows.add(c);
		}
	}
	

	CLICKABLE makeNeed(StatNeed n, STAT s, GText work) {
		return new CLICKABLE.ClickableAbs(500, 32) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				
				GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
				GButt.ButtPanel.renderFrame(r, body);
				
				int x1 = body().x1()+8;
				
				if (n != null) {
					if (a.a.race().service().services(a.a.indu(), n.need).size() > 0) {
						a.a.race().service().services(a.a.indu(), n.need).get(0).room().icon.small.renderCY(r, x1, body().cY());
					}else
						n.need.rate.icon.renderCY(r, x1, body().cY());
				}
				x1+= 20;
				
				work.setFont(UI.FONT().S);
				work.clear();
				work.add(s.info().name);
				work.lablify();
				work.renderCY(r, x1, body().cY());
				x1+= 280;
				
				int i = s.indu().get(a.a.indu());
				double m = s.indu().max(a.a.indu());
				double d = i/m;
				
				GMeterCol c = GMeter.C_GREEN; 
				if (n == null || i > n.breakpoint()) {
					c = GMeter.C_RED;
				}
				GMeter.render(r, c, 
						d, 
						x1, x1+75, body().y1()+8, body().y2()-8);
				
				
				
				if (n != null) {
					int x = (int) (x1 + 75*(n.breakpoint()/m));
					GCOLOR.UI().border().render(r, x, x+1, body().y1()+8, body().y2()-8);
				}
				
				x1 += 90;
				
				work.clear();
				
				if (n != null){
					double def = n.need.rate.baseValue;
					double rate = n.need.rate.get(a.a.race());
					GFORMAT.percInc(work, def);
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
				b.textLL(¤¤need);
				b.tab(6);
				double d = s.indu().getD(a.a.indu());
				b.add(GFORMAT.perc(b.text(), d));
				
				
				
				
				
				if (n != null) {
					b.NL();
					b.textLL(¤¤services);
					
					for (RoomService s : a.a.race().service().services(a.a.indu(), n.need)) {
						b.add(s.room().icon);
					}
					b.NL();
					b.sep();
					GText t = b.text();
					t.add(¤¤percPerDay).insert(0, (int)(100*n.need.rate.get(a.a.indu())));
					n.need.rate.hover(b, a.a.indu(), t, true);
				}
			}
			
			@Override
			protected void clickA() {
				SDebugInput.activate(s.indu(), a.a);
			}
		};
	}

	
	private void hoverBoost(Boostable boo, GUI_BOX text) {
		
		text.title(boo.name);
		text.text(boo.desc);
		text.NL(8);
		boo.hover(text, a.a.indu(), true);
		
	}
	
	GuiSection activate() {
		return section;
	}


}
