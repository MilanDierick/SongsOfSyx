package view.world.ui.region;

import game.boosting.BoostSpec;
import init.D;
import init.biomes.*;
import init.resources.Minable;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Tree;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.info.GFORMAT;
import world.regions.Region;
import world.regions.data.RD;
import world.regions.data.RDReligions.RDReligion;
import world.regions.data.building.RDBuilding;
import world.regions.data.pop.RDRace;

class MiscBasics extends GuiSection{

	public static CharSequence ¤¤fertilityD = "Fertility can increase production of buildings and determines how many subjects the region can support.";
	public static CharSequence ¤¤areaD = "The size of the region along with the fertility determines how many subjects it can support.";
	public static CharSequence ¤¤mineralsD = "Minerals and ore that can be excavated with buildings.";
	
	static {
		D.ts(MiscBasics.class);
	}
	
	public static GuiSection info(GETTER<Region> g) {
		GuiSection sec = new GuiSection();
		
		
		
		int w = 140;
		sec.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, g.get().info.area());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.text(¤¤areaD);
			};
			
		}.hh(DicMisc.¤¤Area, w));
		
		sec.addRightC(80, new HOVERABLE.HoverableAbs(w, 16) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				double d = RD.DEVASTATION().current.getD(g.get());
				int am = (int) (d*(body().width()/16));
				int x = body().x1();
				GCOLOR.UI().BAD.hovered.bind();
				for (int i = 0; i < am; i++) {
					UI.icons().s.degrade.render(r, x, body().y1());
					x+= 16;
				}
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.add(RD.DEVASTATION().current.info());
				b.NL();
				b.add(GFORMAT.percInv(b.text(), RD.DEVASTATION().current.getD(g.get())));
			}
		});
		
		int DIM = 24;
		int AM = 10;
		SPRITE s;
		
		s = new SPRITE.Imp(AM*DIM, DIM) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				
				COLOR.WHITE25.render(r, X1, X2, Y1, Y2);
				int ff = (int) Math.ceil(g.get().info.fertility()*(AM-1));
				for (int i = 0; i < AM; i++) {
					OPACITY.unbind();
					if (i >= ff) {
						OPACITY.O25.bind();
					}
					SPRITES.icons().m.agriculture.render(r, X1, Y1);
					X1 += DIM;
				}
				OPACITY.unbind();
				
			}
		};
		sec.add(new GHeader.HeaderHorizontal(DicMisc.¤¤Fertility, s, w) {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(DicMisc.¤¤Fertility);
				b.text(¤¤fertilityD);
				b.NL();
				b.textL(DicMisc.¤¤Current);
				b.tab(6);
				b.add(GFORMAT.perc(b.text(), g.get().info.fertility()));
			}
			
		}, 0, sec.getLastY2()+2);
		
		s = new SPRITE.Imp(AM*DIM, DIM) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				
				COLOR.WHITE25.render(r, X1, X2, Y1, Y2);
				int x = X1;
				for (int i = 0; i < AM/2; i++) {
					ColorImp.TMP.interpolate(CLIMATES.COLD().color, CLIMATES.TEMP().color, i/5.0);
					ColorImp.TMP.render(r, x, x+DIM, Y1+2, Y2-2);
					x += DIM;
				}
				for (int i = 0; i < AM/2; i++) {
					ColorImp.TMP.interpolate(CLIMATES.TEMP().color, CLIMATES.HOT().color, i/5.0);
					ColorImp.TMP.render(r, x, x+DIM, Y1+2, Y2-2);
					x += DIM;
				}
				
				
				double d = 0;
				for (CLIMATE c : CLIMATES.ALL()) {
					d+= g.get().info.climate(c)*(c.index());
				}
				d /= (CLIMATES.ALL().size()-1);
				x = (int) (X1+d*(X2-X1));
				x -= Icon.M/2;
				COLOR.BLACK.bind();
				SPRITES.icons().m.crossair.render(r, x+2, Y1+2);
				COLOR.unbind();
				SPRITES.icons().m.crossair.render(r, x, Y1);
				
				
			}
		};
		sec.addDown(2, new GHeader.HeaderHorizontal(CLIMATES.INFO().name, s, w) {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				for (CLIMATE c : CLIMATES.ALL()) {
					b.text(c.name);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), g.get().info.climate(c)));
					b.NL();
				}
			}
			
		});
		
		s = new SPRITE.Imp(AM*DIM, DIM) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				COLOR.WHITE25.render(r, X1, X2, Y1, Y2);
				int aa = AM;
				for (TERRAIN c : TERRAINS.ALL()) {
					if (c == TERRAINS.NONE())
						continue;
					int am = (int) ((aa)*g.get().info.terrain(c));
					for (int i = 0; i < am; i++) {
						c.icon().render(r, X1, X1+DIM, Y1, Y2);
						X1+=DIM;
					}
				}
			}
		};
		sec.addDown(2, new GHeader.HeaderHorizontal(DicMisc.¤¤Terrain, s, w) {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				for (TERRAIN t : TERRAINS.ALL()) {
					b.text(t.name);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), g.get().info.terrain(t)));
					b.NL();
				}
			}
			
		});
		
		
		
		s = new SPRITE.Imp(AM*DIM, DIM) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				COLOR.WHITE25.render(r, X1, X2, Y1, Y2);
				for (Minable c : RESOURCES.minables().all()) {
					int am = g.get().info.minable(c);
					for (int i = 0; i < am; i++) {
						c.resource.icon().render(r, X1, X1+DIM, Y1, Y2);
						X1+=DIM/2;
					}
					if (am > 0)
						X1+=DIM/2;
				}
			}
		};
		sec.addDown(2, new GHeader.HeaderHorizontal(DicMisc.¤¤Minerals, s, w) {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				for (Minable m : RESOURCES.minables().all()) {
					b.text(m.name);
					b.tab(6);
					b.add(GFORMAT.f0(b.text(), g.get().info.minableBonus(m)));
					b.NL();
				}
			}
			
		});
		

		

		
		sec.addRelBody(2, DIR.S, race(g, sec.body().width()));
		
		
		
		return sec;
	}
	
	private static RENDEROBJ race(GETTER<Region> g, int width) {
		int YS = 3;
		int wi = 16*((width-8)/16);
		
		final double maxPop = (3*RD.RACES().maxPop());
		final int maxAm = (int) ((wi/8)*YS);

		return new HOVERABLE.HoverableAbs(wi+8, 24*YS+8) {

			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				COLOR.WHITE25.render(r, body);
				
				
				int m = (body.width()-wi)/2;
				final int x1 = body.x1()+m;
				final int y1 = body.y1()+4;
				
				
				
				double d = (double)maxPop/(3*RD.RACES().population.get(g.get()));
				d = CLAMP.d(d, 0, 1);
				
				int dx = (int) (24*d);
				dx = CLAMP.i(dx, 1, 24);

				int i = 0;
				
				int lineMax = (int) Math.ceil((double)(wi-20)/dx);
				
				for (RDRace ra : RD.RACES().all) {
					int am = (int)Math.ceil(maxAm*ra.pop.get(g.get())/maxPop);
					while(am > 0) {
						am--;
						
						int x = dx*(i%lineMax);
						int y = (i/lineMax);
						i++;
						if (y >= YS)
							break;
						
						ra.race.appearance().icon.render(r, x1+x, y1+y*24);
						
					}
				}
				
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(DicMisc.¤¤Population);
				
				b.tab(6);
				b.textL(DicMisc.¤¤Population);
				b.tab(9);
				b.textL(DicMisc.¤¤Biome);
				b.NL();
				
				for (RDRace ra : RD.RACES().all) {
					
					double v = ra.pop.base(g.get());
					b.add(ra.race.appearance().iconBig);
					b.text(ra.race.info.names);
					b.tab(6);
					
					b.add(GFORMAT.i(b.text(), ra.pop.get(g.get())));
					b.tab(9);
					b.add(GFORMAT.perc(b.text(), v));
					b.NL();
					
					
				}
				
				
				b.textLL(DicMisc.¤¤Total);
				b.tab(6);
				b.add(GFORMAT.iBig(b.text(), RD.RACES().population.get(g.get())));
				
			}
		};
	}
	
	public static RENDEROBJ rel(GETTER<Region> g) {
		int AM = 24;
		return new HOVERABLE.HoverableAbs(16*AM, 20) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				COLOR.WHITE25.render(r, body);
				
				int a = AM;
				int X1 = body.x1();
				double tot = 1.0;
				for (RDReligion ra : RD.RELIGION().all()) {
					
					double v = ra.current.getD(g.get());
					int aa = (int) Math.round(a*ra.current.getD(g.get())/tot);
					
					tot -= v;
					a -= aa;
					
					while(aa > 0) {
						ra.religion.icon.small.renderCY(r, X1, body().cY());
						X1 += Icon.S;
						aa--;
					}
					
				}
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				
				b.title(STATS.RELIGION().info.name);
				
				for (RDReligion ra : RD.RELIGION().all()) {
					
					b.add(ra.religion.icon);
					b.text(ra.religion.info.name);
					b.tab(7);
					b.add(GFORMAT.perc(b.text(), ra.current.getD(g.get())));
					b.NL();
					
					
				}
				
			}
		};
	}
	
	public static RENDEROBJ prospect(GETTER<Region> g) {
		int AM = 12;
		final ArrayList<BuildSort> all = new ArrayList<BuildSort>(RD.BUILDINGS().all.size());
		{
			for (RDBuilding b : RD.BUILDINGS().all) {
				all.add(new BuildSort(b));
			}
		}
		final Tree<BuildSort> sort = new Tree<BuildSort>(RD.BUILDINGS().all.size()) {

			@Override
			protected boolean isGreaterThan(BuildSort current, BuildSort cmp) {
				return current.value > cmp.value;
			}
			
		};
		
		return new HOVERABLE.HoverableAbs(32*AM, 40) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				COLOR.WHITE25.render(r, body);
				sort.clear();
				for (BuildSort b : all) {
					if (b.b.baseFactors.size() > 0) {
						b.init(g.get());
						sort.add(b);
					}
				}
				
				int max = AM;
				int X1 = body.x1();
				while(sort.hasMore() && max > 0) {
					BuildSort b = sort.pollGreatest();
					max--;
					if (b.value == 1)
						continue;
					int Y1 = body.y1();
					b.b.levels.get(1).icon.renderCY(r, X1, body().cY());
					int am = (int) Math.round(CLAMP.d((b.value-1)*3, 0, 3));
					COLOR.GREEN100.bind();
					for (int i = 0; i < am; i++) {
						UI.icons().s.plus.render(r, X1+16, Y1+10*i);
					}
					COLOR.unbind();
					X1 += 32;
				}
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(DicMisc.¤¤Prospect);
				sort.clear();
				for (BuildSort bu : all) {
					if (bu.b.baseFactors.size() > 0) {
						bu.init(g.get());
						sort.add(bu);
					}
					
				}
				while(sort.hasMore()) {
					BuildSort bu = sort.pollGreatest();
					b.add(bu.b.levels.get(1).icon);
					b.text(bu.b.info.name);
					b.tab(7);
					b.add(GFORMAT.f1(b.text(), bu.value));
					b.NL();
				}
			}
		};
	}
	
	private static class BuildSort {
		
		public final RDBuilding b;
		public double value = 0;
		
		public BuildSort(RDBuilding b) {
			this.b = b;
		}
		
		void init(Region reg) {
			value = 1;
			for (BoostSpec f : b.baseFactors) {
				value *= f.get(reg);
			}
		}
		
	}
	
}
