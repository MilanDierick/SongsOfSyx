package view.world.ui.regions;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.C;
import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.gui.table.GTableSorter;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.world.ui.WorldHoverer;
import world.World;
import world.entity.WPathing;
import world.entity.WPathing.FactionDistance;
import world.map.regions.*;

final class UIRegionList extends ISidePanel{

	private final int width = C.SG*240;
	private final int height = C.SG*55;
	private Bitmap1D reachable = new Bitmap1D(FACTIONS.MAX, false);
	{D.gInit(this);}
	
	private final GTableSorter<Region> sorter = new GTableSorter<Region>(Regions.MAX) {
		@Override
		protected Region getUnsorted(int index) {
			Region f = World.REGIONS().getByIndex(index);
			if (f.area() > 0)
				return f;
			return null;
		};
	};
	
	private final StringInputSprite s;
	
	private GTFilter<Region> filterName = new GTFilter<Region>(D.g("search")) {
		@Override
		public boolean passes(Region h) {
			return h.name().startsWithIgnoreCase(s.text());
		}
		
	};
	
	private final GTSort<Region> sort = new GTSort<Region>("blabla") {

		@Override
		public int cmp(Region current, Region cmp) {
			return get(current) - get(cmp);
		}
		
		private int get(Region current) {
			int m = Regions.MAX*FACTIONS.MAX;
			int res = current.index();
//			if (!Region.DATA().marked.is(current)) {
//				res += 4*m;
//			}
			Faction f = REGIOND.faction(current);
			if (f == null) {
				res += 3*m;
			}else if (f != FACTIONS.player()) {
				res += m;
				res += Regions.MAX*f.index();
			}
			return res;
			
		}

		@Override
		public void format(Region h, GText text) {
			// TODO Auto-generated method stub
			
		}
		
	};

	private final GTableBuilder builder;
	
	UIRegionList() {
		
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
				Region r = sorter.get(index);
				if (r != null) {
					WorldHoverer.hover(VIEW.hoverBox(), r);
				}
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

		
		
		titleSet(D.g("Regions"));
	}
	
	@Override
	protected void update(float ds) {
		reachable.clear();
		for (FactionDistance f : WPathing.getFactions(FACTIONS.player())) {
			reachable.set(f.f.index(), true);
		}
		sorter.sort();
	}
	
	private final GText textH = new GText(UI.FONT().H2, 30);
	private final GText textN = new GText(UI.FONT().S, 30);
	
	private final class Button extends ClickableAbs {
		
		private final GETTER<Integer> ier;
		
		Button(GETTER<Integer> ier){
			this.ier = ier;
			body.setWidth(width);
			body.setHeight(height);
			

			
			
		}
		
		@Override
		protected void clickA() {
			
			Region f = sorter.get(ier);
			VIEW.world().UI.region.openFromList(f, VIEW.world().panels);
			
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			Region f = sorter.get(ier);
			selectedSet(last().added(VIEW.world().UI.region.detail) && UIRegion.reg == f);
			GCOLOR.UI().border().render(r, body());
			if (f.faction() == GAME.player()) {
				GCOLOR.UI().bg(true, last().added(VIEW.world().UI.region.detail) && (UIRegion.reg == f), hoveredIs()).render(r, body(), -1);
				textH.clear();
				textH.lablify().add(f.name(), 17);
				textH.render(r, body().x1()+4, body().y1()+2);
				
				SPRITES.icons().s.admin.render(r, body().x1()+180, body().y1()+2);
				textN.clear();
				GFORMAT.i(textN, REGIOND.OWNER().adminCostAll(f));
				textN.render(r, body().x1()+198, body().y1()+2);
				
				int cy = body().y1() + textH.height() + 2 + ( body().height()-textH.height())/2;
				GMeter.renderDelta(r, REGIOND.OWNER().order.getD(f), REGIOND.OWNER().order.next(f), body().x1()+4, body().x1()+60, cy-6, cy+6);
				
				SPRITES.icons().s.citizen.renderCY(r, body().x1()+64, cy);
				textN.clear();
				GFORMAT.i(textN, REGIOND.POP().total().get(f));
				textN.renderCY(r, body().x1()+82, cy);
				
				SPRITES.icons().s.sword.renderCY(r, body().x1()+122, cy);
				textN.clear();
				GFORMAT.i(textN, REGIOND.MILITARY().soldiers.get(f));
				textN.renderCY(r, body().x1()+140, cy);
				
				
			}else {
				GCOLOR.UI().bg(false, false, false).render(r, body(), -1);
				textH.clear();
				textH.add(f.name());
				if (f.faction() == null) {
					textH.color(GCOLOR.T().INORMAL);
					
				}else if (FACTIONS.rel().war.get(FACTIONS.player(), f.faction()) == 1){
					textH.color(GCOLOR.T().IBAD);
				}else {
					textH.color(GCOLOR.T().IGOOD);
				}
				textH.render(r, body().x1()+4, body().y1()+2);
				int cy = body().y1() + textH.height() + 2 + ( body().height()-textH.height())/2;
				if (f.faction() != null) {
					f.faction().banner().MEDIUM.renderCY(r, body().x1()+1, cy);
				}
				
				SPRITES.icons().s.citizen.renderCY(r, body().x1()+64, cy);
				textN.clear();
				GFORMAT.i(textN, REGIOND.POP().total().get(f));
				textN.renderCY(r, body().x1()+82, cy);
				
			}
		}
		
	}
	
	public ISidePanel get(Region f) {
		
		sorter.sortForced();
		if (f != null) {
			builder.set(sorter.getIndex(f));
		}
		
		return this;

	}
	
	
}
