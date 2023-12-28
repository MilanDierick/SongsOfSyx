package view.world.ui.army;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.config.Config;
import init.race.RACES;
import init.race.Race;
import init.resources.*;
import init.resources.RBIT.RBITImp;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.colors.GCOLORS_MAP;
import util.data.GETTER;
import util.dic.*;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.PLACABLE;
import view.tool.PlacableSimpleTile;
import view.world.ui.WorldHoverer;
import world.WORLD;
import world.army.AD;
import world.entity.army.WArmy;

final class List extends ISidePanel{

	private Faction f = FACTIONS.player();
	private static int width = 200;
	private final GTableBuilder builder;
	
	public List() {
		titleSet(DicArmy.¤¤Armies);
		
		section.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, AD.conscripts().available(null).get(f));
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(DicArmy.¤¤Conscriptable);
				b.text(DicArmy.¤¤ConscriptsD);
				b.NL(8);
				
				for (Race r : RACES.all()) {
					if (r.population().max == 0)
						continue;
					b.add(r.appearance().icon);
					b.add(GFORMAT.iIncr(b.text(), AD.conscripts().available(null).get(f)));
					b.NL();
				}
				
			};
			
		}.hv(DicArmy.¤¤Conscriptable));
		
		section.addDownC(4, new GStat() {
			
			@Override
			public void update(GText text) {
				
				int am = 0;
				for (RESOURCE res : RESOURCES.ALL()) {
					am += SETT.ROOMS().SUPPLY.amount(res);
				}
				
				GFORMAT.iIncr(text, am);
			}
			
			RBITImp rs = new RBITImp();
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(DicArmy.¤¤Supplies);
				b.text(DicArmy.¤¤SuppliesD);
				b.sep();
				
				b.textLL(SETT.ROOMS().SUPPLY.info.names);
				
				rs.clear();
				for (ArmySupply s : RESOURCES.SUP().ALL())
					rs.or(s.resource.bit);
				
				int tab = 0;
				
				for (RESOURCE res : RESOURCES.ALL()) {
					if (rs.has(res)) {
						b.tab(tab*3);
						b.add(res.icon());
						b.add(GFORMAT.i(b.text(), SETT.ROOMS().SUPPLY.amount(res)));
						tab++;
						if (tab > 5) {
							tab = 0;
							b.NL();
						}
					}
				}
				b.NL(8);
				
				rs.clear();
				for (EquipBattle s : STATS.EQUIP().BATTLE_ALL())
					rs.or(s.resource());
				
				tab = 0;
				
				for (RESOURCE res : RESOURCES.ALL()) {
					if (rs.has(res)) {
						b.tab(tab*3);
						b.add(res.icon());
						b.add(GFORMAT.i(b.text(), SETT.ROOMS().SUPPLY.amount(res)));
						tab++;
						if (tab > 5) {
							tab = 0;
							b.NL();
						}
					}
				}
				
			};
			
		}.hv(DicArmy.¤¤Supplies));
		
		GButt.ButtPanel bb = new GButt.ButtPanel(DicArmy.¤¤Recruit) {
			
			PLACABLE p = new Placer();
			
			@Override
			protected void renAction() {
				activeSet(f.armies().canCreate());
			}
			
			@Override
			protected void clickA() {
				last().add(List.this, true);
				if (FACTIONS.player().armies().all().size() == 0) {
					COORDINATE c = WORLD.PATH().rnd(FACTIONS.player().capitolRegion());
					int tx = c.x();
					int ty = c.y();
					WArmy e = f.armies().create(tx, ty);
					VIEW.world().UI.armies.openList(e);
				}else
					VIEW.world().tools.place(p);
			}
			
		};
		bb.body.setWidth(width);
		section.addDownC(4, bb);
		
		
		
		builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return f.armies().all().size();
			}
			
			@Override
			public void hover(int index) {
				if (index >= 0)
					WORLD.OVERLAY().things.hover(f.armies().all().get(index).body(), GCOLORS_MAP.get(f), false, 4);
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
		for (WArmy aa : f.armies().all()) {
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
			add(new GStat(UI.FONT().M) {
				
				@Override
				public void update(GText text) {
					text.lablify();
					text.add(g().name, 12);
				}
			}, 0, 0);
			
			addRightCAbs(width, new RENDEROBJ.RenderImp(Icon.S) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {

					if (g().region() != null && FACTIONS.DIP().war.is(g().region().faction(), GAME.player())) {
						GCOLOR.T().IBAD.bind();
						SPRITES.icons().s.world.render(r, body);
						COLOR.unbind();

					}else if (g().path().moving(g().body())) {
						SPRITES.icons().s.crossheir.render(r, body);
					}else if (g().acceptsSupplies()){
						SPRITES.icons().s.muster.render(r, body);
					}
					
					
				}
			});
			
			
			add(new RENDEROBJ.RenderImp(body().width(), 12) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					double dw = (double)AD.men(null).target().get(g())/Config.BATTLE.MEN_PER_ARMY;
					dw = Math.sqrt(dw);
					int ww = (int) (body.width()*dw);
					GMeter.renderDelta(r, (double)AD.men(null).get(g())/AD.men(null).target().get(g()), 1.0, body.x1(), body.x1()+ww, body.y1(), body.y2());
					
				}
			}, 0, body().y2()+4);
			

			
			pad(6, 6);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			WorldHoverer.hover(text, g());
		}
		
		private WArmy g() {
			return f.armies().all().get(ier.get());
		}
		
		@Override
		protected void clickA() {
			VIEW.world().UI.armies.openList(g(), last());
		}
		
		@Override
		protected void renAction() {
			selectedSet(last().added(VIEW.world().UI.armies.army) && Army.army == g());
		}
	}
	
	private class Placer extends PlacableSimpleTile {

		public Placer() {
			super(DicArmy.¤¤Recruit);
		}

		@Override
		public CharSequence isPlacable(int tx, int ty) {
			if (!WORLD.PATH().route.is(tx, ty))
				return DicMisc.¤¤Unreachable;
			if (WORLD.REGIONS().map.get(tx, ty) == null || WORLD.REGIONS().map.get(tx, ty).faction() != f)
				return DicGeo.¤¤MustBeOwnRegion;
			return null;
		}

		@Override
		public void place(int tx, int ty) {
			WArmy e = f.armies().create(tx, ty);
			VIEW.world().tools.place(null);
			VIEW.world().UI.armies.openList(e);
			
		}
		
		@Override
		public void renderOverlay(GameWindow window) {
			WORLD.OVERLAY().hoverArmy(FACTIONS.player());
		}
		
	}
	
	
	
}
