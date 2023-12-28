package view.ui.faction;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.npc.ruler.ROpinions;
import init.D;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.dic.*;
import util.gui.misc.*;
import util.info.GFORMAT;
import world.regions.data.RD;
import world.regions.data.pop.RDRace;

class Hoverer {

	GuiSection s = new GuiSection();
	private final SPRITE ss;
	private FactionNPC f;
	
	private static CharSequence ¤¤powerD = "The power of this faction compared to you. The power is a mix of military might and production capabilities. High powered factions are harder to please.";
	
	static {
		D.ts(Hoverer.class);
	}
	
	public Hoverer() {
		s.add(new GStat(UI.FONT().S) {

			@Override
			public void update(GText text) {

				text.lablifySub().add(f.nameIntro);

			}
		}.r(DIR.N), 0, 0);
		s.addDownC(2, new GStat(UI.FONT().H2) {

			@Override
			public void update(GText text) {

				text.lablify().add(f.name);

			}
		}.r(DIR.N));

		s.addRelBody(110, DIR.W, new SPRITE.Imp(Icon.L) {

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				f.banner().BIG.render(r, X1, X2, Y1, Y2);
			}
		});

		s.addRelBody(110, DIR.E, new SPRITE.Imp(Icon.L) {

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				f.banner().BIG.render(r, X1, X2, Y1, Y2);
			}
		});

		s.addRelBody(4, DIR.S, new GStat() {

			@Override
			public void update(GText text) {
				DicGeo.fType(f, text);

			}
		}.r(DIR.N));

		GETTER<FactionNPC> g = new GETTER<FactionNPC>() {

			@Override
			public FactionNPC get() {
				return f;
			}

		};


		s.add(facts(g, 2, 140), s.body().x1(), s.body().y2());

		ss = s.asSprite();

	}

	static GuiSection facts(GETTER<FactionNPC> f, int cols, int M) {

		GuiSection s = new GuiSection();
		int i = 0;

		
		{
			GuiSection ss = new GuiSection() {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(f.get().court().king().roy().induvidual.race().info.namePosessive);
				}
			};
			ss.add(UI.icons().s.crown, 0, 0);
			ss.addRightC(4, new RENDEROBJ.RenderImp(Icon.M) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					f.get().court().king().roy().induvidual.race().appearance().icon.render(r, body);
				}
			});
			
			s.addGridD(ss, i++, cols, M, 20, DIR.W);
		}
		
		s.addGridD(new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.percInc(text, ROpinions.current(f.get()));
			}

			@Override
			public void hoverInfoGet(GBox b) {
				b.title(DicGeo.¤¤Opinion);
				b.text(DicGeo.¤¤OpinionD);
			};

		}.hh(UI.icons().s.happy), i++, cols, M, 20, DIR.W);
		
		
		s.addGridD(new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text,(long) f.get().power());
				//GFORMAT.f1(text, CLAMP.d(f.get().power() / (FACTIONS.player().power() + 1.0), 0, 10));
			}

			@Override
			public void hoverInfoGet(GBox b) {
				b.title(DicArmy.¤¤Power);
				b.text(¤¤powerD);
			};

		}.hh(UI.icons().s.fist), i++, cols, M, 20, DIR.W);
		
		s.addGridD(new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, (int) f.get().buyer().credits());
			}

			@Override
			public void hoverInfoGet(GBox b) {
				b.title(DicRes.¤¤Riches);
			};

		}.hh(UI.icons().s.money), i++, cols, M, 20, DIR.W);

		s.addGridD(new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, RD.DIST().distance(f.get()));
			}

			@Override
			public void hoverInfoGet(GBox b) {
				b.add(RD.DIST().distance().info());
			};

		}.hh(UI.icons().s.wheel), i++, cols, M, 20, DIR.W);
		
		
		s.addGridD(new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, RD.RACES().population.faction().get(f.get()));
			}

			@Override
			public void hoverInfoGet(GBox b) {
				b.title(DicMisc.¤¤Subject);
				for (RDRace rr : RD.RACES().all) {
					b.add(rr.race.appearance().icon);
					b.text(rr.race.info.names);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), rr.pop.faction().get(f.get())));
					b.NL();
				}
			};

		}.hh(UI.icons().s.human), i++, cols, M, 20, DIR.W);
		
		SPRITE ss = new SPRITE.Imp(140, Icon.S) {

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				int am = FACTIONS.DIP().war.getEnemies(f.get()).size();
				
				if (am == 0)
					return;
				
				int dx = (width()-24)/am;
				
				dx = CLAMP.i(dx, 1, 24);
				
				double x1 = X1;
				for (Faction fa : FACTIONS.DIP().war.getEnemies(f.get())) {
					fa.banner().MEDIUM.render(r, (int) x1, Y1);
					x1 += dx;
					if (x1 > X2)
						break;
				}
			}
		};

		s.addGridD(new GHeader.HeaderHorizontal(UI.icons().s.sword, ss) {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(DicArmy.¤¤Enemies);
				for (Faction fa : FACTIONS.active()) {
					if (FACTIONS.DIP().war.is(fa, f.get())) {
						b.add(fa.banner().BIG);
						b.text(fa.name);
						b.NL();
					}
				}

			}
			
		}, i++, cols, M, 20, DIR.W);
		
		s.body().incrW(Math.max(M-s.body().width()-20, 0));
		return s;

	}




	void hover(GUI_BOX box, Faction f) {
		GBox b = (GBox) box;

		if (f == null) {
			b.title(DicGeo.¤¤NoRuler);
		} else if (f instanceof FactionNPC) {
			hoverFF(b, (FactionNPC) f);
		} else {
			box.title(f.name);
		}
	}

	void hoverFF(GBox b, FactionNPC f) {
		this.f = f;
		b.add(ss);

	}

}
