package view.world.ui.camps;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.statistics.G_REQ;
import init.C;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLORS_MAP;
import util.data.GETTER;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.gui.table.GTableSorter;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import world.World;
import world.map.buildings.camp.WCampInstance;
import world.map.buildings.camp.WorldCamp;

public final class UICampList extends ISidePanel{

	private final int width = C.SG*240;
	private final Rec hBody = new Rec(C.TILE_SIZE*2);
	
	private final GTableSorter<WCampInstance> sorter = new GTableSorter<WCampInstance>(WorldCamp.MAX) {
		@Override
		protected WCampInstance getUnsorted(int index) {
			return World.camps().all().get(index);
		};
	};
	
	private final StringInputSprite s;
	
	private GTFilter<WCampInstance> filterName = new GTFilter<WCampInstance>(DicMisc.¤¤Search) {
		@Override
		public boolean passes(WCampInstance h) {
			return h.name.startsWithIgnoreCase(s.text()) || Str.startsWithIgnoreCase(s.text(), h.race().info.names);
		}
		
	};
	
	private final GTSort<WCampInstance> sort = new GTSort<WCampInstance>("blabla") {

		@Override
		public int cmp(WCampInstance current, WCampInstance cmp) {
			return get(current) - get(cmp);
		}
		
		private int get(WCampInstance current) {
			int res = current.index();
			Faction f = current.faction();
			if (f == FACTIONS.player()) {
				;
			}else if (f == null && World.REGIONS().getter.get(current.coo()) != null && World.REGIONS().getter.get(current.coo()).faction() == FACTIONS.player()) {
				res += WorldCamp.MAX;
			}else if (f == null) {
				res += WorldCamp.MAX*2;
			}else
				res += WorldCamp.MAX*3;
			
			return res;
			
		}

		@Override
		public void format(WCampInstance h, GText text) {
			// TODO Auto-generated method stub
			
		}
		
	};

	private final GTableBuilder builder;
	
	public UICampList() {
		
		sorter.setSort(sort);
		
		s = new StringInputSprite(16, UI.FONT().H2){
			@Override
			protected void change() {
				sorter.setFilter(filterName);
			};
		}.placeHolder(filterName.name);
		
		GInput in = new GInput(s);
		
		section.add(in);
		
		builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return sorter.size();
			}
			
			@Override
			public void hover(int index) {
//				Region r = sorter.get(index);
//				if (r != null) {
//					WorldHoverer.hover(VIEW.hoverBox(), r);
//				}
			}
			
			
		};

		builder.column(null, width, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Button(ier);
			}
		});
		
		GuiSection s = builder.createHeight(HEIGHT-in.body.height()-C.SG*4, true);
		section.addDown(2, s);

		
		
	}
	
	@Override
	protected void update(float ds) {

		sorter.sortForced();
	}
	
	private final class Button extends GButt.BSection {
		
		private final GETTER<Integer> ier;
		
		Button(GETTER<Integer> ier){
			this.ier = ier;
			
			add(new SPRITE.Imp(ICON.BIG.SIZE) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					WCampInstance f = sorter.get(ier);
					f.race().appearance().iconBig.render(r, X1, X2, Y1, Y2);
				}
			},0 ,0);
			
			add(new GStat() {
				
				@Override
				public void update(GText text) {
					WCampInstance f = sorter.get(ier);
					text.lablify().add(f.name);
					text.setMaxChars(16);
				}
			}, getLastX2()+8, 0);
			
			add(new GStat() {
				
				@Override
				public void update(GText text) {
					WCampInstance f = sorter.get(ier);
					GFORMAT.i(text, f.max);
				}
			}.hh(SPRITES.icons().m.citizen), getLastX1(), getLastY2());
			
			body().setWidth(width);
			
			addRelBody(4, DIR.S, new SPRITE.Imp(body().width(), 8) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					WCampInstance f = sorter.get(ier);
					if (f.faction() == FACTIONS.player()) {
						GMeter.render(r, GMeter.C_BLUE, 1.0, X1, X2, Y1, Y2);
					}else if (f.faction() == null) {
						double d = 0;
						for (G_REQ re : f.reqs()) {
							d += re.progress();
						}
						d /= f.reqs().size();
						GMeter.render(r, GMeter.C_ORANGE, d, X1, X2, Y1, Y2);
					}else {
						OPACITY.O25.bind();
						COLOR.BLACK.render(r, body(), -2);
						OPACITY.unbind();
					}
				}
			});
			
			pad(8, 4);
			

			
			
		}
		
		@Override
		protected void clickA() {
			WCampInstance f = sorter.get(ier);
			VIEW.world().window.centererTile.set(f.coo());
			
//			Region f = sorter.get(ier);
//			VIEW.world().UI.region.openFromList(f, VIEW.world().panels);
			
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			
			if (super.hover(mCoo)) {
				WCampInstance f = sorter.get(ier);
				hBody.moveC(f.coo().x()*C.TILE_SIZE+C.TILE_SIZEH, f.coo().y()*C.TILE_SIZE+C.TILE_SIZEH);
				World.OVERLAY().hover(hBody, GCOLORS_MAP.get(f.faction()), false, 0);
				return true;
			}

			return false;
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			WCampInstance f = sorter.get(ier);
			f.hoverInfo(text);
		}
		
		
	}
	
	public ISidePanel get(WCampInstance f) {
		
		sorter.sortForced();
		if (f != null) {
			builder.set(sorter.getIndex(f));
		}
		
		return this;

	}
	
	
}
