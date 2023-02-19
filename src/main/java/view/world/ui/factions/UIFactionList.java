package view.world.ui.factions;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import init.C;
import init.D;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.colors.GCOLOR_UI;
import util.data.GETTER;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.gui.table.GTableSorter;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import world.army.WARMYD;
import world.entity.WPathing;
import world.entity.WPathing.FactionDistance;

final class UIFactionList extends ISidePanel{

	public static int ROW_HEIGHT = 30;
	private final int width = C.SG*264;
	private final int height = C.SG*70;
	private Bitmap1D reachable = new Bitmap1D(FACTIONS.MAX, false);
	
	
	{D.gInit(this);}
	private final CharSequence sReachable = D.g("reachable", "This realm is reachable and can be traded with.");
	private final CharSequence sReachableUn = D.g("unreachable", "This realm is too distant to interact with.");
	private final CharSequence sPopulation = D.g("Population");
	
	private final GTableSorter<Faction> sorter = new GTableSorter<Faction>(FACTIONS.NPCs().size()) {
		@Override
		protected Faction getUnsorted(int index) {
			Faction f = FACTIONS.NPCs().get(index);
			if (f.isActive())
				return f;
			return null;
		};
	};
	
	
	private final StringInputSprite s;
	
	private GTFilter<Faction> filterName = new GTFilter<Faction>(D.g("filter name")) {
		@Override
		public boolean passes(Faction h) {
			return h.appearence().name().startsWithIgnoreCase(s.text());
		}
		
	};
	
	private final GTableBuilder builder;
	
	UIFactionList() {
		titleSet(D.g("Factions"));
		
		sorter.setSort(new GTSort<Faction>("") {
			
			@Override
			public void format(Faction h, GText text) {
			}
			
			@Override
			public int cmp(Faction current, Faction cmp) {
				if (FACTIONS.rel().war.get(FACTIONS.player(), current) == 1)
					return -1;
				if (FACTIONS.rel().war.get(FACTIONS.player(), cmp) == 1)
					return 1;
				if (FACTIONS.rel().allies.get(FACTIONS.player(), current) == 1)
					return -1;
				if (FACTIONS.rel().allies.get(FACTIONS.player(), cmp) == 1)
					return 1;
				return 0;
			}
		});
		
		builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return sorter.size();
			}
			
			@Override
			public void hover(int index) {
				Faction f = sorter.get(index);
				if (f != null) {
					
				}
			}
			
		};
		
		
		
		builder.column(null, width, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Button(ier);
			}
		});
		
		section = builder.createHeight(HEIGHT-20, true);
		
		s = new StringInputSprite(10, UI.FONT().M){
			@Override
			protected void change() {
				sorter.setFilter(filterName);
			};
		}.placeHolder(filterName.name);
		
		section.add(s.c(DIR.W), 0, section.body().y1()-s.height()-C.SG*4);
	}
	
	@Override
	protected void update(float ds) {
		
		reachable.clear();
		for (FactionDistance f : WPathing.getFactions(FACTIONS.player())) {
			reachable.set(f.f.index(), true);
		}
		sorter.sort();
	}
	
	
	private final class Button extends GuiSection {
		
		private final GETTER<Integer> ier;
		
		Button(GETTER<Integer> ier){
			this.ier = ier;
			body().setWidth(width);
			body().setHeight(height);
			
			RENDEROBJ o;
			
			o = new RENDEROBJ.RenderImp(ICON.BIG.SIZE*2) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					Faction f = sorter.get(ier);
					if (f == null)
						return;
					
					f.banner().HUGE.render(r, body());
					
				}
			};
			
			o.body().centerY(this);
			o.body().moveX1((height-o.body().height())/2);
			add(o);
			
			o = new GStat() {
				
				@Override
				public void update(GText text) {
					Faction f = sorter.get(ier);
					if (f != null)
						text.lablifySub().add(sorter.get(ier).appearence().name());
				}
			}.r(DIR.NW);
			add(o, getLastX2()+getLastX1()+8, 4);
			
			int x1 = getLastX1();
			
			add(SPRITES.icons().s.human, getLastX1(), getLastY2()+4);
			
			o = new GStat() {
				
				@Override
				public void update(GText text) {
					Faction f = sorter.get(ier);
					if (f == null)
						return;
					int am = f.kingdom().realm().population().total().get() + f.capitol().population.total().get();
					GFORMAT.i(text, am);
				}
			}.r().hoverInfoSet(sPopulation);
			
			addRightC(4, o);
			
			o = new HoverableAbs(ICON.SMALL.SIZE) {
				
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
					Faction f = sorter.get(ier);
					if (f == null)
						return;
					if (reachable.get(f.index())) {
						COLOR.GREEN100.bind();
					}else {
						COLOR.RED100.bind();
					}
					SPRITES.icons().s.allRight.render(r, body);
					COLOR.unbind();
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					Faction f = sorter.get(ier);
					if (f == null)
						return;
					if (reachable.get(f.index())) {
						text.text(sReachable);
					}else {
						text.text(sReachableUn);
					}
				}
			};
			
			addRightC(80, o);
			
			o = new GStat() {
				
				@Override
				public void update(GText text) {
					Faction f = sorter.get(ier);
					if (f != null) {
						GFORMAT.i(text, f.kingdom().armies().all().size());
						text.s();
						GFORMAT.i(text, WARMYD.men(null).total().get(f));
					}
				}
			}.hh(SPRITES.icons().s.sword);
			add(o, x1, getLastY2()+4);
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GCOLOR.UI().border().render(r, body());
			boolean hovered = hoveredIs();
			Faction f = sorter.get(ier);
			boolean selected = VIEW.world().UI.faction.detail.isShowing(f);
			boolean active = f.capitolRegion() != null;
			
			
			COLOR c = GCOLOR.UI().SOSO.normal;
			if (FACTIONS.rel().enemy(FACTIONS.player(), f))
				c = GCOLOR.UI().BAD.normal;
			else if (FACTIONS.rel().overlord.get(FACTIONS.player(), f) == 1)
				c = GCOLOR.UI().GOOD.normal;
			else if (FACTIONS.rel().ally(FACTIONS.player(), f))
				c = GCOLOR.UI().GOOD2.normal;
			
			GCOLOR_UI.color(ColorImp.TMP.set(c).shadeSelf(0.5), active, selected, hovered).render(r, body(), -1);
			
			super.render(r, ds);
		}
		
		@Override
		protected void clickA() {

			open(sorter.get(ier), false);
		}
		
	}
	
	public void open(Faction f, boolean shove) {
		
		sorter.sortForced();
		VIEW.world().panels.add(this, true);
		if (f != null) {
			VIEW.world().panels.add(this, true);
			if (shove)
				builder.set(sorter.getIndex(f));
			VIEW.world().UI.faction.detail.activate((FactionNPC) f);
			
			VIEW.world().window.centererTile.set(f.capitolRegion().cx(), f.capitolRegion().cy());
		}

	}
	
	
}
