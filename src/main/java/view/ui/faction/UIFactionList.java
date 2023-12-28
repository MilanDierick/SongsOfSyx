package view.ui.faction;

import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.npc.ruler.ROpinions;
import game.faction.npc.ruler.Royalty;
import game.faction.player.emissary.EMission;
import game.faction.player.emissary.EMissionType;
import init.C;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Tree;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.DOUBLE;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import view.main.VIEW;
import world.WORLD;
import world.WorldMinimap;
import world.regions.Region;
import world.regions.data.RD;

final class UIFactionList extends GuiSection{

	private final ArrayList<FactionNPC> sorted = new ArrayList<>(FACTIONS.MAX);
	
	public static int ROW_HEIGHT = 30;
	private final int width = C.SG*264;
	private int[] emmi = new int[FACTIONS.MAX];
	
	private final StringInputSprite filter = new StringInputSprite(20, UI.FONT().S);
	private final GTableBuilder builder;
	
	private final GETTER_IMP<FactionNPC> getter;
	private final Tree<FactionNPC> sorter = new Tree<FactionNPC>(FACTIONS.MAX) {

		@Override
		protected boolean isGreaterThan(FactionNPC current, FactionNPC cmp) {
			return value(current) > value(cmp);
		}
		
		private int value(Faction f) {
			if (FACTIONS.DIP().war.is(FACTIONS.player(), f))
				return 0+f.index();
			if (FACTIONS.DIP().trades(FACTIONS.player(), f))
				return FACTIONS.MAX+f.index();
			if (RD.DIST().factionIsAlmostReachable(f))
				return FACTIONS.MAX*2+f.index();
			return FACTIONS.MAX*3+f.index();
		}
		
	};
	
	
	UIFactionList(GETTER_IMP<FactionNPC> getter, int HEIGHT) {
		
		add(new CLICKABLE.ClickableAbs(WorldMinimap.WIDTH+6, WorldMinimap.HEIGHT+6) {
			
			private Faction hf;
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
				GCOLOR.UI().borderH(r, body, 0);
				WORLD.MINIMAP().render(r, body().x1()+3, body.y1()+3);
			}
			
			@Override
			public boolean hover(COORDINATE mCoo) {
				hf = null;
				if (super.hover(mCoo)) {
					int x = mCoo.x()-body.x1()-3;
					int y = mCoo.y()-body.y1()-3;
					x = (WORLD.TWIDTH()*x)/WorldMinimap.WIDTH;
					y = (WORLD.THEIGHT()*y)/WorldMinimap.HEIGHT;
					if (WORLD.TBOUNDS().holdsPoint(x, y)) {
						Region reg = WORLD.REGIONS().map.get(x, y);
						if (reg != null && reg.faction() != null) {
							hf = reg.faction();
							WORLD.MINIMAP().hilight(hf);
						}
					}
					return true;
				}
				return false;
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (hf != null) {
					VIEW.UI().factions.hover(text, hf);
					
				}
			}
			
			@Override
			protected void clickA() {
				if (hf != null && hf instanceof FactionNPC) {
					getter.set((FactionNPC) hf);
					builder.set(sorted.indexOf(getter.get()));
				}
				super.clickA();
			}
			
		});
		
		
		this.getter = getter;
		
		
		
		filter.placeHolder(DicMisc.¤¤Search);
		
		GInput in = new GInput(filter);
		
		addDownC(8, in);
		
		
		
		builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return sorted.size();
			}			
		};
		
		builder.column(null, width, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Button(ier);
			}
		});
		
		addDownC(8, builder.createHeight(HEIGHT-16-body().height(), false));
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		sorted.clear();
		for (FactionNPC f : FACTIONS.NPCs())
			sorter.add(f);
		while(sorter.hasMore()) {
			FactionNPC f = sorter.pollSmallest();
			if (filter.text().length() > 0) {
				if (f.name.containsText(filter.text()))
					sorted.add(f);
			}else
				sorted.add(f);
		}
		Arrays.fill(emmi, 0);
		for (EMission e : FACTIONS.player().emissaries.all()) {
			for (EMissionType m : EMissionType.ALL()) {
				if (e.mission() == m && m.faction(e) != null)
					emmi[m.faction(e).index()] ++;
			}
		}
		
		super.render(r, ds);
	}
	
	
	private final class Button extends GuiSection {
		
		private final GETTER<Integer> ier;
		
		Button(GETTER<Integer> ier){
			this.ier = ier;
			
			RENDEROBJ o;
			
			o = new RENDEROBJ.RenderImp(Icon.L*2+16, Icon.L*2) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					FactionNPC f = g();
					if (f == null)
						return;
					f.banner().HUGE.render(r,  body().x1(), body().y1());
					Royalty ro = f.court().king().roy();
					int x1 = body().x1()+Icon.L+Icon.L/2;
					int y1 = body().y1()+16;
					STATS.APPEARANCE().portraitRender(r, ro.induvidual, x1, y1, 1);
					ro.induvidual.race().appearance().crown.all().get(0).renderScaled(r, x1, y1, 1);
				}
			};

			add(o);
			
			o = new GStat() {
				
				@Override
				public void update(GText text) {
					FactionNPC f = g();
					if (f != null)
						text.lablifySub().add(f.name);
				}
			}.r(DIR.NW);
			add(o, getLastX2()+12, 4);
			
			o = new GStat() {
				
				@Override
				public void update(GText text) {
					FactionNPC f = g();
					if (f == null)
						return;
					
					int am = RD.RACES().population.faction().get(f);
					GFORMAT.i(text, am);
				}
			}.hh(SPRITES.icons().s.human);
			add(o, getLastX1(), getLastY2()+4);
			addRightC(55, new GStat() {
				
				@Override
				public void update(GText text) {
					FactionNPC f = g();
					if (f == null)
						return;
					GFORMAT.i(text, emmi[f.index()]);
				}
			}.hh(UI.icons().s.flags));
			
			
			add(GMeter.sprite(GMeter.C_REDGREEN, new DOUBLE() {

				@Override
				public double getD() {
					FactionNPC f = g();
					if (f == null)
						return 0;
					return ROpinions.current(f.court().king().roy());
				}
				
			}, 100, 12), o.body().x1(), getLastY2()+1);
			
			add(GMeter.sprite(GMeter.C_ORANGE, new DOUBLE() {

				@Override
				public double getD() {
					FactionNPC f = g();
					if (f == null)
						return 0;
					return RD.RACES().population.faction().get(f)/(10*RD.RACES().maxPop());
				}
				
			}, 100, 12), getLastX1(), getLastY2()+1);
			
			pad(8, 6);
			body().setWidth(width);
			
			o = new RENDEROBJ.Sprite(UI.icons().s.money) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					FactionNPC f = g();
					if (f == null)
						return;
					if (!FACTIONS.DIP().trades(FACTIONS.player(), f)) {
						return;
					}
					
					if (!RD.DIST().factionBordersPlayer(f)) {
						OPACITY.O50.bind();
					}
					super.render(r, ds);
					OPACITY.unbind();
				}
				
			};
			o.body().moveX2(body().x2()-8);
			o.body().moveY1(8);
			add(o);

		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			
			boolean hovered = hoveredIs();
			FactionNPC f = g();
			boolean selected = getter.get() == f;
			boolean active = f.capitolRegion() != null;
			
			if (hovered || selected)
				WORLD.MINIMAP().hilight(f);
			
			GButt.ButtPanel.renderBG(r, active, selected, hovered, body());
			
			if (FACTIONS.DIP().war.is(FACTIONS.player(), f)) {
				OPACITY.O25.bind();
				COLOR.RED100.render(r, body(),-4);
				OPACITY.unbind();
			}
			
			super.render(r, ds);
			
			if (!RD.DIST().factionBordersPlayer(f)) {
				OPACITY.O50.bind();
				COLOR.BLACK.render(r, body(),-4);
				OPACITY.unbind();
			}
			
			GButt.ButtPanel.renderFrame(r, body());
			
			
		}
		
		@Override
		protected void clickA() {

			open(g(), false);
		
		}
		
		private FactionNPC g() {
			return sorted.get(ier.get());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			super.hoverInfoGet(text);
			if (text.emptyIs())
				VIEW.UI().factions.hover(text, g());
			
		}
		
	}
	
	void open(FactionNPC f, boolean shove) {

		if (f != null) {
			if (shove)
				builder.set(sorted.indexOf(f));
			getter.set(f);
//			VIEW.world().UI.faction.open(f);
//			VIEW.world().window.centererTile.set(f.capitolRegion().cx(), f.capitolRegion().cy());
		}

	}
	
	
}
