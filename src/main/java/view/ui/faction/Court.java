package view.ui.faction;

import java.util.Arrays;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.npc.ruler.*;
import game.faction.npc.ruler.RTraits.Title;
import game.faction.player.emissary.*;
import init.D;
import init.race.appearence.RPortrait;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.data.GETTER;
import util.data.INT.INTE;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.slider.GTarget;
import util.info.GFORMAT;
import view.main.VIEW;

final class Court extends GuiSection{

	private Emmi emmiPop = new Emmi();
	
	private static CharSequence ¤¤Heir = "¤First Heir";
	private static CharSequence ¤¤Heir2 = "¤Second Heir";
	private static CharSequence ¤¤Heir3 = "¤Third Heir";
	private static CharSequence ¤¤EDesc = "By clicking a royalty you can allocate emissaries to them to perform different actions.";

	private final CharSequence[] sss = new CharSequence[] {
		¤¤Heir,
		¤¤Heir2,
		¤¤Heir3
	};
	
	static {
		D.ts(Court.class);
		
	}
	
	Court(GETTER<FactionNPC> f, int WIDTH, int HEIGHT){

		body().setDim(WIDTH, HEIGHT);
		
		GuiSection suckers = new GuiSection();
		
		for (int i = 1; i < NPCCourt.MAX; i++) {
			suckers.addRightC(48, butt(f, i));
		}
		
		suckers.addRelBody(8, DIR.N, UI.decor().borderTop(suckers.body().width()+128));
		
		suckers.addRelBody(16, DIR.N, king(f));
		
		suckers.body().moveY1(0).moveCX(body().cX());
		add(suckers);
		
		
		RENDEROBJ oo = new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, get());
				text.s().add('|').s();
				GFORMAT.i(text, FACTIONS.player().emissaries.available());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(Emissaries.¤¤names);
				b.text(¤¤EDesc);
				b.NL();
				b.textLL(DicMisc.¤¤Allocated);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), get()));
				b.NL();
				b.textLL(DicMisc.¤¤Available);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), FACTIONS.player().emissaries.available()));
			};
			
			private int ui = 0;
			private int am = 0;
			
			private int get() {
				if (ui == GAME.updateI())
					return am;
				ui = GAME.updateI();
				am = 0;
				for (EMission e : FACTIONS.player().emissaries.all()) {
					if (e.mission().faction(e) == f.get())
						am++;
					
				}
				return am;
			}
		}.hh(UI.icons().m.flag);
		
		add(oo, body().cX()+180, body().y1()+50);
		
	}
	
	private CLICKABLE king(GETTER<FactionNPC> f) {
		GuiSection s = new GuiSection();
		s.addRelBody(2, DIR.S, new GStat(new GText(UI.FONT().M, 32)) {
			
			@Override
			public void update(GText text) {
				f.get().court().king().intro(text);
			}
		}.r(DIR.N));
		
		s.addRelBody(4, DIR.S, new GStat(new GText(UI.FONT().H2, 32)) {
			
			@Override
			public void update(GText text) {
				text.lablify().add(f.get().court().king().name);
			}
		}.r(DIR.N));
		

		
		s.addRelBody(2, DIR.S, new Portrait(4, new GETTER<Royalty>() {

			@Override
			public Royalty get() {
				return f.get().court().king().roy();
			}
			
		}));
		
		s.addRelBody(2, DIR.S, new GStat(new GText(UI.FONT().M, 32)) {
			
			@Override
			public void update(GText text) {
				LIST<Title> tt = f.get().court().king().roy().traits();
				for (int i = 0; i < tt.size(); i++) {
					text.add(tt.get(i).title);
					if (i < tt.size()-1)
						text.add(',').s();
				}
			}
		}.r(DIR.N));
		
		
		
		
		return s;
	}
	
	private class Emmi extends GuiSection {
		
		Royalty roy;
		
		
		Emmi(){
			
			add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, FACTIONS.player().emissaries.available());
				}
			}.hh(Emissaries.¤¤names));
			
			ArrayList<EMissionType> mm = new ArrayList<EMissionType>(EMissionType.FLATTER, EMissionType.FAVOUR, EMissionType.ASSASINATE);
			
			LinkedList<RENDEROBJ> rows = new LinkedList<>();
			
			for (EMissionType m : mm) {
				
				GuiSection s = new GuiSection() {
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.title(m.name);
						b.text(m.desc);
					}
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						if (roy == null || roy.successionI() < 0)
							return;
						super.render(r, ds);
						GButt.ButtPanel.renderFrame(r, body());
					}
					
				};
				
				s.add(m.icon, 0, 0);
				s.addRightC(4, new GHeader(m.name));
				
				INTE ii = new INTE() {
					
					@Override
					public int min() {
						return 0;
					}
					
					@Override
					public int max() {
						return get()+FACTIONS.player().emissaries.available();
					}
					
					@Override
					public int get() {
						cache(roy.court);
						return cache[m.index()][roy.successionI()];
					}
					
					@Override
					public void set(int t) {
						int am = t - get();
						FACTIONS.player().emissaries.assign(m, null, roy, am);
					}
				};
				
				s.addRightCAbs(220, new GTarget(64, true, true, ii) {
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						activeSet(m != EMissionType.FAVOUR || roy.successionI() > 1);
						super.render(r, ds);
					}
				});
				s.pad(4, 4);
				rows.add(s);
				
				addRelBody(2, DIR.S, s);
				
			}
			
			pad(16, 16);
		}
		
		
	}

	
	private CLICKABLE butt(GETTER<FactionNPC> f, int i) {
		
		return new Portrait(2, new GETTER<Royalty>() {

			@Override
			public Royalty get() {
				return f.get().court().all().get(i);
			}
			
			
			
		}) {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(sss[i-1]);
				text.NL();
				super.hoverInfoGet(text);
			}
		};
		
	}
	
	
	static void hover(GUI_BOX box, Royalty ro) {
		GBox b = (GBox) box;
		
		b.title(ro.nameFull(b.text()));
		
		b.textLL(DicMisc.¤¤Age);
		b.tab(6);
		int y = (int) (STATS.POP().age.years(ro.induvidual));
		b.add(GFORMAT.i(b.text(), y));
		b.NL(8);
		
		for (Title info : ro.traits()) {
			b.textL(info.name);
			b.NL();
			b.text(info.desc);
			b.NL(8);
		}
		
		ROpinions.GET().hover(box, ro, true);
		
		
	}
	

	private int cacheI = -1;
	private NPCCourt cacheCourt = null;
	private final int[][] cache = new int[EMissionType.ALL().size()][NPCCourt.MAX];
	
	private void cache(NPCCourt court) {
		if (GAME.updateI() != cacheI || cacheCourt != court) {
			for (int[] ii : cache)
				Arrays.fill(ii, 0);
			cacheI = GAME.updateI();
			cacheCourt = court;
			for (EMission e : FACTIONS.player().emissaries.all()) {
				if (e.mission() != null) {
					for (int ri = 0; ri < court.all().size(); ri++) {
						if (e.mission().targetIs(e, null, court.all().get(ri))) {
							cache[e.mission().index()][ri]++;
						}
					}
						
				}
			}
			
		}
	}
	
	private final ArrayList<COLOR> cols = new ArrayList<COLOR>(
			new ColorImp(127, 127, 50),
			new ColorImp(100, 100, 100),
			new ColorImp(88, 75, 62)
			);
	
	class Portrait extends ClickableAbs{

		

		
		final GETTER<Royalty> g;
		final int scale;
		final int bar;
		public Portrait(int scale, GETTER<Royalty> g) {
			super(RPortrait.P_WIDTH*scale+4*scale, RPortrait.P_HEIGHT*scale+12*scale+6*scale);
			this.scale = scale;
			this.g = g;
			bar = (int) (8*Math.ceil(scale/2.0));
		}
		
		

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			Royalty ro = g.get();
			isActive &= ro != null;
			GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
			if (ro == null)
				return;
			
			int X1 = body().x1() + 2*scale;
			int Y1 = body().y1() + 12*scale;
			
			STATS.APPEARANCE().portraitRender(r, ro.induvidual, X1, Y1, scale);
			
			double now = ROpinions.current(ro);
			now = 0.5+now/8.0;
			
			GMeter.render(r, GMeter.C_REDGREEN, now,  X1+4*scale,  body().x2()-4*scale, body().y2()-6*scale, body().y2()-2*scale);
			
			if (ro.isKing())
				ro.induvidual.race().appearance().crown.all().get(0).renderScaled(r, X1, Y1, scale);
			else {
				cols.getC(ro.successionI()-1).bind();
				UI.icons().s.star.render(r, body.x1()+4, body.y2()-20);
				COLOR.unbind();
			}
		
			cache(ro.court);
			
			int x1 = X1;
			Y1 = body().y1() + 4;
			for (EMissionType m : EMissionType.ALL()) {
				int am = cache[m.index()][ro.successionI()];
				if (am > 0) {
					
					OPACITY.O35.bind();
					COLOR.BLACK.render(r, x1, x1+64, Y1, Y1+16);
					OPACITY.unbind();
					m.icon.render(r, x1, Y1);
					Str.TMP.clear();
					GFORMAT.formatI(Str.TMP, am);
					UI.FONT().S.render(r, Str.TMP, x1+18, Y1);
					Y1 += 18;
				}
				
			}
			
			
			
			
			GButt.ButtPanel.renderFrame(r, isActive, isSelected, isHovered, body);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (g.get() == null)
				return;
			Court.hover(text, g.get());
			text.NL(8);
			super.hoverInfoGet(text);
		}
		
		@Override
		protected void clickA() {
			Royalty ro = g.get();
			if (ro == null)
				return;
			emmiPop.roy = ro;
			VIEW.inters().popup.show(emmiPop, this, true);
		}
		
	}
	
	
}
