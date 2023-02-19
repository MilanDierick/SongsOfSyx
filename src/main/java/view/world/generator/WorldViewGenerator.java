package view.world.generator;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import init.C;
import init.D;
import init.sprite.ICON;
import init.sprite.ICON.MEDIUM;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.*;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.gui.misc.*;
import util.gui.panel.GPanelL;
import view.interrupter.Interrupter;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.*;
import view.world.WorldIIMinimap;
import world.World;
import world.WorldGenerator;
import world.map.regions.CapitolPlacablity;
import world.map.regions.Region;
import world.map.terrain.WorldTerrainInfo;

public class WorldViewGenerator extends VIEW.ViewSub{
	
	private final GameWindow window;
	{D.t(this);}
	private static CharSequence ¤¤Settlement = "¤Settlement";
	private static CharSequence ¤¤place = "¤Place Capitol";
	private static CharSequence ¤¤Regenerate = "¤Regenerate";
	private static CharSequence ¤¤settle = "¤settle";
	private static CharSequence ¤¤seed = "¤Random Seed";
	private static CharSequence ¤¤seedgenerate = "¤Generate based on seed.";
	private final Inter inter;
	
	public WorldViewGenerator(GameWindow window){
		
		this.window = window;
		new WorldIIMinimap(null, uiManager);
		ToolManager tools = new ToolManager(uiManager, window);
		WorldTerrainInfo info = new WorldTerrainInfo();
		tools.place(new Placer(info), config);
		inter = new Inter(info);
		if (FACTIONS.player().titles.unlocked() > 0)
			new Interer(this);
	}

	@Override
	public void activate() {
		super.activate();
		window.stop();
	}
	
	@Override
	protected void hover(COORDINATE mCoo, boolean mouseHasMoved) {
		
		window.hover();
	}

	@Override
	protected void mouseClick(MButt button) {

	}
	
	@Override
	protected void hoverTimer(double mouseTimer, GBox text) {
		
	}
	
	@Override
	protected boolean update(float ds, boolean should){
		return true;
	}
	
	@Override
	protected void render(Renderer r, float ds, boolean hide) {

		window.crop(uiManager.viewPort());
		GAME.world().render(r, ds, window.zoomout(), window.pixels(), window.view().x1(), window.view().y1());
		
		
	}


	@Override
	protected void save(FilePutter file) {
		window.saver.save(file);
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		window.saver.load(file);
	}



	
	private final ToolConfig config = new ToolConfig() {
		
		private final RENDEROBJ title = SPRITES.specials().getSprite(¤¤place);
		private final CLICKABLE butt = new GButt.Standalone(new ICON.MEDIUM.Twin(SPRITES.icons().m.terrain, SPRITES.icons().m.rotate)){
			@Override
			protected void clickA() {
				RND.setSeed(RND.rInt(999999999));
				seed.text().clear().add(RND.seed());
				GAME.world().regenerate();
			};
		};
		GInput seed = new GInput(new StringInputSprite(9, UI.FONT().M) {
			@Override
			protected void acceptChar(char c) {
				if (c >= '0' && c <= '9')
					super.acceptChar(c);
			}
		}) {
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
				GCOLOR.UI().bg().render(r, body);
				super.render(r, ds, isActive, isSelected, isHovered);
			}
		};
		private final CLICKABLE buttSeed = new GButt.Standalone(SPRITES.icons().m.arrow_right){
			@Override
			protected void clickA() {
				int se = RND.seed();
				CharSequence s = seed.text();
				if ((s.length() > 9))
					s = (""+s).substring(0, 9);
				try {
					se = Integer.parseInt("" + s);
					RND.setSeed(se);
					GAME.world().regenerate();
				}catch(Exception e) {
					seed.text().clear().add('1');
				}
				
			};

		}.hoverInfoSet(¤¤seedgenerate);;
		
		{
			title.body().centerX(C.DIM());
			title.body().moveY1(80*C.SG);
			seed.hoverInfoSet(¤¤seed);
			seed.body().centerX(title);
			seed.body.moveY1(title.body().y2()+8);
			butt.body().moveX2(seed.body.x1());
			butt.body().moveY1(title.body().y2()+4);
			butt.hoverInfoSet(¤¤Regenerate);
			seed.text().clear().add(RND.seed());
			buttSeed.body().moveY1(title.body().y2()+4);
			buttSeed.body().moveX1(seed.body.x2());
		}
		@Override
		public void addUI(LISTE<RENDEROBJ> uis) {
			uis.add(title);
			if (World.conRandom() != null) {
				uis.add(seed);
				uis.add(butt);
				uis.add(buttSeed);
			}
		};
		@Override
		public boolean back() {
			return false;
		};
		
	};
	
	private class Placer extends PlacableFixedImp {

		private final WorldTerrainInfo info;
		private final SPRITE s;
		
		private Placer(WorldTerrainInfo info) {
			super(null, 1,1);
			this.info = info;
			GeneratorUI ui = new GeneratorUI(info);
			s = new SPRITE.Imp(ui.body().width(), ui.body().height()) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					ui.body().moveX1Y1(X1, Y1);
					ui.render(r, 0);
				}
			};
			
		}
		
		@Override
		public MEDIUM getIcon() {
			return SPRITES.icons().m.city;
		}

		@Override
		public CharSequence name() {
			return null;
		}
		
		@Override
		public void afterPlaced(int tx1, int ty1) {
			if (!inter.isActivated())
				inter.show();
		
		}
		
		@Override
		public void place(int tx, int ty, int rx, int ry) {
//			if (rx == 1 && ry == 1) {
//				Region r = World.REGIONS().getter.get(tx, ty);
//				if (CapitolPlacablity.whole(tx-1, ty-1) == null) {
//					REGIOND.OWNER().setCapitol(tx-1, ty-1, FACTIONS.player());
//					r.name().clear().add(FACTIONS.player().appearence().name());
//					
//				}
//			}
		}
		
		@Override
		public CharSequence placableWhole(int tx, int ty) {
			return CapitolPlacablity.whole(tx, ty);
		}



		@Override
		public int width() {
			return CapitolPlacablity.TILE_DIM;
		}

		@Override
		public int height() {
			return CapitolPlacablity.TILE_DIM;
		}

		@Override
		public CharSequence placable(int tx, int ty, int rx, int ry) {
			return CapitolPlacablity.tile(tx, ty);
		}
		
		@Override
		public void placeInfo(GBox b, int tx1, int ty1) {
			if (!inter.isActivated() && CapitolPlacablity.whole(tx1, ty1) == null) {
				info.initCity(tx1, ty1);
				b.title(¤¤Settlement);			
				b.add(s);
			}
			
			Region r = World.REGIONS().getter.get(tx1, ty1);
			for (int y = 0; y <= CapitolPlacablity.TILE_DIM; y++) {
				for (int x = 0; x <= CapitolPlacablity.TILE_DIM; x++) {
					if (World.REGIONS().getter.get(tx1 + x, ty1 + y) != r)
						r = null;
				}
			}
			
			if (r != null && !r.isWater()) {
				World.OVERLAY().hoverRegion(r);
			}
		
		
		
		}
		
		
		
	}
	
	private class Inter extends Interrupter {

		private final GuiSection s = new GuiSection();
		private final WorldTerrainInfo info;
		Inter(WorldTerrainInfo info){
			pin();
			this.info = info;
			GPanelL panel = new GPanelL();
			GuiSection ss = new GeneratorUI(info);
			panel.getInnerArea().set(ss.body());
			GuiSection butts = new GuiSection();
			butts.add(new GButt.Panel(SPRITES.icons().m.ok) {
				
				@Override
				protected void clickA() {
					new WorldGenerator(info.tx, info.ty);
				}
			});
			butts.addRightC(0, new GButt.Panel(SPRITES.icons().m.cancel) {
				
				@Override
				protected void clickA() {
					hide();
				}
			});
			panel.setTitle(¤¤settle);
			panel.centreNavButts(butts);
			s.add(panel);
			s.add(ss);
			s.add(butts);
			
			s.body().moveY2(C.DIM().y2()-50);
			s.body().centerX(C.DIM());
			
		}
		
		void show() {
			window.centererTile.set(info.tx, info.ty);
			show(uiManager);
		}
		
		@Override
		protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
			s.hover(mCoo);
			return true;
		}

		@Override
		protected void mouseClick(MButt button) {
			if (button == MButt.LEFT)
				s.click();
			else
				hide();
		}

		@Override
		protected void hoverTimer(GBox text) {
			s.hoverInfoGet(text);
		}

		@Override
		protected boolean render(Renderer r, float ds) {
			s.render(r, ds);
			return true;
		}

		private final Rec rec = new Rec();
		
		@Override
		protected boolean update(float ds) {
			rec.moveX1Y1((info.tx- CapitolPlacablity.TILE_DIM/2)*C.TILE_SIZE, (info.ty- CapitolPlacablity.TILE_DIM/2)*C.TILE_SIZE);
			rec.setDim(CapitolPlacablity.TILE_DIM*C.TILE_SIZE);
			World.OVERLAY().hover(rec, COLOR.BLUE2BLUE, true, 4);
			return false;
		}
		
		
	}
	
	
}
