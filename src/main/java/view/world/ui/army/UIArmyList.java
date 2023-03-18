package view.world.ui.army;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.config.Config;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.colors.GCOLORS_MAP;
import util.data.GETTER;
import util.dic.DicArmy;
import util.dic.DicGeo;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.tool.PLACABLE;
import view.tool.PlacableSimpleTile;
import view.world.ui.WorldHoverer;
import world.World;
import world.army.WARMYD;
import world.entity.army.WArmy;

final class UIArmyList extends ISidePanel{

	private Faction f = FACTIONS.player();
	private static int width = 125;
	private final GTableBuilder builder;
	
	public UIArmyList() {
		titleSet(DicArmy.¤¤Armies);
		
		section.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, WARMYD.conscriptable(null).get(f) - WARMYD.conscriptableInService(null).get(f));
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(DicArmy.¤¤Conscriptable);
				b.text(DicArmy.¤¤ConscriptsD);
				b.NL(8);
				
				for (Race r : RACES.all()) {
					if (r.population().rarity == 0)
						continue;
					b.add(r.appearance().icon);
					b.add(GFORMAT.i(b.text(), WARMYD.conscriptable(r).get(f) - WARMYD.conscriptableInService(r).get(f)));
					b.NL();
				}
				
			};
			
		}.hv(DicArmy.¤¤Conscriptable));
		
		section.addDownC(4, new GButt.ButtPanel(DicArmy.¤¤Recruit) {
			
			PLACABLE p = new Placer();
			
			@Override
			protected void renAction() {
				activeSet(f.kingdom().armies().canCreate());
			}
			
			@Override
			protected void clickA() {
				last().add(UIArmyList.this, true);
				VIEW.world().tools.place(p);
			}
			
		}.pad(8, 0));
		
		
		
		builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return f.kingdom().armies().all().size();
			}
			
			@Override
			public void hover(int index) {
				if (index >= 0)
					World.OVERLAY().hover(f.kingdom().armies().all().get(index).body(), GCOLORS_MAP.get(f), false, 4);
			}
			
			
		};

		builder.column(null, width+12, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Button(ier);
			}
		});
		
		
		section.addRelBody(8, DIR.S, builder.createHeight(HEIGHT-section.getLastY2()-8, false));
		
	
	}
	
	void set(WArmy a) {
		int i = 0;
		for (WArmy aa : f.kingdom().armies().all()) {
			if (aa == a) {
				builder.set(i);
				break;
			}
			i++;
		}
		
		
	}
	
	private class Button extends GButt.BSection {
		
		private final GETTER<Integer> ier;
		
		Button(GETTER<Integer> ier){
			this.ier = ier;
			body().setWidth(width);
			add(new GStat() {
				
				@Override
				public void update(GText text) {
					text.setFont(UI.FONT().H2);
					text.lablify();
					text.add(g().name, 12);
				}
			}, 4, 4);
			
			
			addDown(4, new RENDEROBJ.RenderImp(width, 10) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					
					
					GMeter.renderDelta(r, (double)WARMYD.men(null).get(g())/Config.BATTLE.MEN_PER_ARMY, (double)WARMYD.men(null).target().get(g())/Config.BATTLE.MEN_PER_ARMY, body);
				}
			});
			
//			addDown(2, new GStat() {
//				
//				@Override
//				public void update(GText text) {
//					GFORMAT.i(text, WARMYD.admin().get(g()));
//				}
//			}.decrease().hh(SPRITES.icons().s.admin));
			
			addDown(2, new RENDEROBJ.Sprite(SPRITES.icons().s.muster) {
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					if (!g().path().moving(g().body()) && WARMYD.men(null).get(g())< WARMYD.men(null).target().get(g())) {
						super.render(r, ds);
					}
				}
			});
			
			addRightC(4, new RENDEROBJ.Sprite(SPRITES.icons().s.world) {
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					if (g().region() != null) {
						if (g().region().faction() == null)
							setColor(GCOLOR.T().IBAD);
						else if(FACTIONS.rel().war.get(g().region().faction(), GAME.player()) == 1)
							setColor(GCOLOR.T().IWORST);
						super.render(r, ds);
					}
					
					
				}
			});
			
			addRightC(4, new RENDEROBJ.Sprite(SPRITES.icons().s.crossheir) {
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					if (g().path().moving(g().body())) {
						super.render(r, ds);
					}
					
					
				}
			});
			
			pad(6, 3);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			WorldHoverer.hover(text, g());
		}
		
		private WArmy g() {
			return f.kingdom().armies().all().get(ier.get());
		}
		
		@Override
		protected void clickA() {
			VIEW.world().UI.armies.openList(g(), last());
		}
		
		@Override
		protected void renAction() {
			selectedSet(last().added(VIEW.world().UI.armies.army) && UIArmy.army == g());
		}
	}
	
	private class Placer extends PlacableSimpleTile {

		public Placer() {
			super(DicArmy.¤¤Recruit);
		}

		@Override
		public CharSequence isPlacable(int tx, int ty) {
			if (World.REGIONS().getter.get(tx, ty) == null || World.REGIONS().getter.get(tx, ty).faction() != f)
				return DicGeo.¤¤MustBeOwnRegion;
			if (World.REGIONS().getter.get(tx, ty).isWater())
				return DicGeo.¤¤MustNotBeWater;
			return null;
		}

		@Override
		public void place(int tx, int ty) {
			WArmy e = f.kingdom().armies().create(tx, ty);
			VIEW.world().tools.place(null);
			VIEW.world().UI.armies.openList(e);
			
		}
		
	}
	
	
	
}
