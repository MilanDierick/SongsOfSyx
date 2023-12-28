package view.world.ui.camps;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.C;
import init.sprite.UI.Icon;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLORS_MAP;
import util.data.GETTER;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.gui.table.GTableSorter;
import util.gui.table.GTableSorter.GTSort;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import world.WORLD;
import world.map.buildings.camp.*;

public final class UICampList extends ISidePanel{

	private final Rec hBody = new Rec(C.TILE_SIZE*2);
	
	private final GTableSorter<WCampInstance> sorter = new GTableSorter<WCampInstance>(WorldCamp.MAX) {
		@Override
		protected WCampInstance getUnsorted(int index) {
			return WORLD.camps().all().get(index);
		};
	};
	
	
	private final GTSort<WCampInstance> sort = new GTSort<WCampInstance>("blabla") {

		@Override
		public int cmp(WCampInstance current, WCampInstance cmp) {
			return get(current) - get(cmp);
		}
		
		private int get(WCampInstance current) {
			int res = current.index();
			Faction f = current.regionFaction();
			if (f == FACTIONS.player()) {
				;
			}else if (f == null && WORLD.REGIONS().map.get(current.coo()) != null && WORLD.REGIONS().map.get(current.coo()).faction() == FACTIONS.player()) {
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
		
		GuiSection s = new GuiSection();
		
		for (WCampType t : WORLD.camps().types) {
			s.addDownC(4, new CampInfo(t));
		}
		
		section.add(s);
		
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

		builder.column(null, 290, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Button(ier);
			}
		});
		
		s = builder.createHeight(HEIGHT-section.body().height()-C.SG*4, true);
		section.addDown(2, s);

		
		
	}
	
	@Override
	protected void update(float ds) {

		sorter.sortForced();
	}
	
	private final class Button extends GuiSection {
		
		private final GETTER<Integer> ier;
		
		Button(GETTER<Integer> ier){
			this.ier = ier;
			
			add(new SPRITE.Imp(Icon.M) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					WCampInstance f = sorter.get(ier);
					f.race().appearance().icon.render(r, X1, X2, Y1, Y2);
				}
			},0 ,0);
			
			addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					WCampInstance f = sorter.get(ier);
					text.setMaxWidth(232);
					text.setMultipleLines(false);
					text.lablify().add(f.name);
					
				}
			});
			
			body().setWidth(276);
			
			pad(4, 2);
			

			
			
		}
		
		@Override
		protected void clickA() {
			WCampInstance f = sorter.get(ier);
			VIEW.world().window.centererTile.set(f.coo());
			
//			Region f = sorter.get(ier);
//			VIEW.world().UI.region.openFromList(f, VIEW.world().panels);
			
		}
		
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GButt.ButtPanel.renderBG(r, true, false, hoveredIs(), body());
			super.render(r, ds);
			WCampInstance f = sorter.get(ier);
			if (f.regionFaction() != FACTIONS.player()) {
				OPACITY.O66.bind();
				COLOR.BLACK.render(r, body(), -2);
				OPACITY.unbind();
			}
			
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			
			if (super.hover(mCoo)) {
				WCampInstance f = sorter.get(ier);
				hBody.moveC(f.coo().x()*C.TILE_SIZE+C.TILE_SIZEH, f.coo().y()*C.TILE_SIZE+C.TILE_SIZEH);
				WORLD.OVERLAY().things.hover(hBody, GCOLORS_MAP.get(f.regionFaction()), false, 0);
				return true;
			}

			return false;
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			WCampInstance f = sorter.get(ier);
			CampInfo.hover(text, f);
		}
		
		
	}
	
	public ISidePanel get(WCampInstance f) {
		
		sorter.sortForced();
		if (f != null) {
			builder.set(sorter.getIndex(f));
		}
		
		return this;

	}

	public void hover(GUI_BOX box, WCampInstance w) {
		CampInfo.hover(box, w);
	}
	
	
}
