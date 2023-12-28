package view.ui.credits;

import game.GAME;
import game.faction.player.PCredits.CredHistory;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import util.colors.GCOLOR;
import util.dic.DicRes;
import util.dic.DicTime;
import util.gui.misc.*;
import util.gui.table.GStaples;
import util.info.GFORMAT;

final class COverview extends GuiSection{

	private int hi;
	private static int w = 16;
	private int am = GAME.player().credits().creditsH().historyRecords();
	
	private int loCredits;
	private double maxin, maxout;

	
	
	COverview(int height){
		
		add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.f(text, GAME.player().credits().credits());
			}
		}.hh(DicRes.¤¤Treasury));
		addRelBody(4, DIR.S, amount());
		addRelBody(4, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				int am = 0;
				for (CredHistory h : GAME.player().credits().all()) {
					am += h.IN.get(1);
					am -= h.OUT.get(1);
				}
				GFORMAT.iIncr(text, am);
			}
		}.hh(DicRes.¤¤Earnings));
		addRelBody(4, DIR.S, new Profits());
		addRelBody(8, DIR.S, new Losses());
		
		addRelBody(24, DIR.S, details());
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		loCredits = Integer.MAX_VALUE;
		maxin = 0;
		maxout = 0;
		for (int i = 0; i < am; i++) {
			loCredits = Math.min(loCredits, (int)GAME.player().credits().creditsH().get(i));
			int m = 0;
			int o = 0;
			
			for (CredHistory h : GAME.player().credits().all()) {
				m += h.IN.get(i);
				o += h.OUT.get(i);
			}
			
			maxin = Math.max(maxin, m);
			maxout = Math.max(o, maxout);
		}
		if (loCredits > 1)
			loCredits --;
		super.render(r, ds);
		hi = -1;
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		if (hi >= 0) {
			GBox b = (GBox) text;
			int si = am-hi-1;
			{
				GText t = b.text();
				DicTime.setAgo(t, si*GAME.player().credits().creditsH().time().bitSeconds());
				b.add(t);
				b.NL();
				b.textL(DicRes.¤¤Treasury);
				b.add(GFORMAT.i(b.text(), GAME.player().credits().creditsH().get(si)));
				b.NL(8);
			}
			
			{
				b.tab(4);
				b.textL(DicRes.¤¤Earnings);
				b.tab(7);
				b.textL(DicRes.¤¤Expenses);
				b.tab(10);
				b.textL(DicRes.¤¤Net);
				b.NL();
				
				for (CredHistory h : GAME.player().credits().all()) {
					b.textL(h.type.name);
					b.tab(4);
					b.add(GFORMAT.iIncr(b.text(), h.IN.get(si)));
					b.tab(7);
					b.add(GFORMAT.iIncr(b.text(), -h.OUT.get(si)));
					b.tab(10);
					b.add(GFORMAT.iIncr(b.text(), h.IN.get(si)-h.OUT.get(si)));
					b.NL();
				}
			}
			
		}else
			super.hoverInfoGet(text);
	}
	
	private GStaples amount() {
		
		GStaples s = new GStaples(am) {
			
			@Override
			protected void hover(GBox box, int stapleI) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				if (hi >= 0) {
					setHovered(hi);
				}
				super.render(r, ds, hoveredIs());
			}
			
			@Override
			public boolean hover(COORDINATE mCoo) {
				if (super.hover(mCoo)) {
					hi = hoverI();
					return true;
				}
				return false;
			}
			
			@Override
			protected double getValue(int stapleI) {
				return CLAMP.d(GAME.player().credits().creditsH().get(am -stapleI-1) - loCredits, 0, Integer.MAX_VALUE);
			}
			
			@Override
			protected void setColor(ColorImp c, int stapleI, double value) {
				c.set(COLOR.YELLOW100).saturateSelf(0.5);
			}
		};
		s.body().setWidth(w*am);
		s.body().setHeight(78);
		return s;
		
	}
	

	
	private final class Profits extends HOVERABLE.HoverableAbs{
		
		Profits(){
			body.setWidth(w*am);
			body().setHeight(112);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			
			GCOLOR.UI().border().render(r, body(), 1);
			
			for (int x = 0; x < am; x++) {
				
				int x1 = body().x1()+w*x;

				
				
				if (x != hi) {
					GCOLOR.UI().bg().render(r, x1, x1+w, body().y1(), body().y2());
				}
				
				if (maxin == 0)
					continue;
				
				int si = am - x-1;
				
				int y2 = body().y2();
				for (CredHistory h : GAME.player().credits().all()) {
					
					double d = h.IN.get(si)/maxin;
					int hig = (int) Math.ceil(body().height()*d);
					ColorImp.TMP.set(COLOR.UNIQUE.getC(h.type.ordinal()));
					if (x == hi) {
						ColorImp.TMP.shadeSelf(1.5);
					}else {
						ColorImp.TMP.shadeSelf(0.5);
					}
					ColorImp.TMP.render(r, x1, x1+w, y2-hig, y2);
					
					if (hig > 1)
						COLOR.UNIQUE.getC(h.type.ordinal()).render(r, x1+1, x1+w-1, y2-hig+1, y2);
					if (hig > 0)
						hig--;
					y2-= hig;
					
				}
			}
			
			
			
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				hi = (mCoo.x()-body().x1())/w;
				if (hi >= am)
					hi=-1;
				return true;
			}
			return false;
		}
		
	}
	
	private final class Losses extends HOVERABLE.HoverableAbs{
		
		Losses(){
			body.setWidth(w*am);
			body().setHeight(112);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			
			GCOLOR.UI().border().render(r, body(), 1);
			
			for (int x = 0; x < am; x++) {
				
				int x1 = body().x1()+w*x;

				
				
				if (x != hi) {
					GCOLOR.UI().bg().render(r, x1, x1+w, body().y1(), body().y2());
				}
				
				if (maxout == 0)
					continue;
				
				int si = am - x-1;
				
				int y1 = body().y1();
				for (CredHistory h : GAME.player().credits().all()) {
					
					double d = h.OUT.get(si)/maxout;
					int hig = (int) Math.ceil(body().height()*d);
					ColorImp.TMP.set(COLOR.UNIQUE.getC(h.type.ordinal()));
					if (x == hi) {
						ColorImp.TMP.shadeSelf(1.5);
					}else {
						ColorImp.TMP.shadeSelf(0.5);
					}
					ColorImp.TMP.render(r, x1, x1+w, y1, y1+hig);
					
					if (hig > 1)
						COLOR.UNIQUE.getC(h.type.ordinal()).render(r, x1+1, x1+w-1, y1-1, y1+hig);
					if (hig > 0)
						hig--;
					y1+= hig;
					
				}
			}
			
			
			
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				hi = (mCoo.x()-body().x1())/w;
				if (hi >= am)
					hi=-1;
				return true;
			}
			return false;
		}
		
	}
	
	private RENDEROBJ details() {
		
		GuiSection s = new GuiSection();
		
		int i = 0;
		
		for (CredHistory h : GAME.player().credits().all()) {
			HOVERABLE hh = new HOVERABLE.Sprite(new SDetail(h) {
				
				@Override
				void up(GText text) {
					GFORMAT.iIncr(text, h.IN.get(1)-h.OUT.get(1));
				}
			}).hoverTitleSet(h.type.name).hoverInfoSet(h.type.desc);
			
			hh.body().moveX1Y1((i%1)*(hh.body().width()+32), (i/1)*(hh.body().height()+2));
			s.add(hh);
			i++;
		}
		
		return s;
	}
	
	private abstract static class SDetail implements SPRITE{
		
		private final GStat stat = new GStat() {
			
			@Override
			public void update(GText text) {
				up(text);;
			}
		};
		private final CredHistory cr;
		private static GText t = new GText(UI.FONT().S, 48).lablify();
		
		SDetail(CredHistory cr){
			this.cr = cr;
		}
		
		abstract void up(GText text);
		
		@Override
		public int width() {
			return 200;
		}



		@Override
		public int height() {
			return stat.height();
		}



		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			ColorImp.TMP.set(COLOR.UNIQUE.getC(cr.type.ordinal())).shadeSelf(0.5);
			ColorImp.TMP.render(r, X1, X1+height(), Y1, Y1+height());
			ColorImp.TMP.set(COLOR.UNIQUE.getC(cr.type.ordinal()));
			ColorImp.TMP.render(r, X1+2, X1+height()-2, Y1+2, Y1+height()-2);
			
			t.clear().add(cr.type.name);
			
			t.render(r, X1+height()*2, Y1);
			
			stat.adjust();
			
			stat.render(r, X2-stat.width(), Y1);
			
		}



		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
		
		
	}
	
}
