package view.world.generator;

import static world.World.*;

import game.faction.FACTIONS;
import init.D;
import init.RES;
import init.sprite.ICON;
import init.sprite.SPRITES;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import view.tool.PlacableFixedImp;
import view.tool.ToolConfig;
import view.world.generator.tools.UIWorldToolCapitolPlaceInfo;
import world.World;
import world.map.buildings.WorldGeneratorBuildings;
import world.map.buildings.camp.WoldGeneratorCamps;
import world.map.landmark.WorldGeneratorLandmarks;
import world.map.regions.CapitolPlacablity;
import world.map.regions.WorldGeneratorRegions;
import world.map.terrain.WorldGeneratorTerrain;

class StageCapitol{

	private static CharSequence ¤¤name = "Place Capitol";
	static {
		D.ts(StageCapitol.class);
	}
	
	private final WorldGeneratorRegions g = new WorldGeneratorRegions();
	private final WorldGeneratorBuildings buildings = new WorldGeneratorBuildings();
	private final WorldGeneratorLandmarks landmarks = new WorldGeneratorLandmarks();
	
	public StageCapitol(Stages stages, boolean clear) {
		if (clear) {
			RES.loader().print(DicMisc.¤¤Generating);
			clear();
			MINIMAP().repaint();
		}
		stages.reset();
		LinkedList<CLICKABLE> butts = new LinkedList<>();
		butts.add(new GButt.ButtPanel(SPRITES.icons().m.arrow_left) {
			
			@Override
			protected void clickA() {
				clear();
				stages.reset();
				stages.terrain();
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(DicMisc.¤¤Back);
			}
			
			@Override
			protected void renAction() {
				World.OVERLAY().minerals().add();
			}
			
		});
		butts.add(new GButt.ButtPanel(new ICON.MEDIUM.Twin(SPRITES.icons().m.terrain, SPRITES.icons().m.rotate)){
			@Override
			protected void clickA() {
				World.GEN().seed = RND.rInt(Integer.MAX_VALUE);
				new WorldGeneratorTerrain().generateAll(World.GEN());
			};
		}.hoverInfoSet(Stages.¤¤regenerate));
		if (World.GEN().playerX > -1) {
			butts.add(new GButt.ButtPanel(SPRITES.icons().m.arrow_right){
				@Override
				protected void clickA() {
					stages.finish();
				};
			}.hoverInfoSet(DicMisc.¤¤Next));
		}
		
		PlacableFixedImp t = new PlacableFixedImp(¤¤name, 1, 1) {
			
			final UIWorldToolCapitolPlaceInfo info = new UIWorldToolCapitolPlaceInfo();
			
			@Override
			public int width() {
				return CapitolPlacablity.TILE_DIM;
			}
			
			@Override
			public void place(int tx, int ty, int rx, int ry) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterPlaced(int tx1, int ty1) {
				int cx = tx1+CapitolPlacablity.TILE_DIM/2;
				int cy = ty1+CapitolPlacablity.TILE_DIM/2;
				generate(cx, cy);
				stages.finish();
			}
			
			@Override
			public CharSequence placableWhole(int tx1, int ty1) {
				return CapitolPlacablity.terrain(tx1, ty1);
			}

			
			@Override
			public int height() {
				return CapitolPlacablity.TILE_DIM;
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return butts;
			}

			@Override
			public CharSequence placable(int tx, int ty, int rx, int ry) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void placeInfo(GBox b, int x1, int y1) {
				info.placeInfo(b, x1, y1, FACTIONS.player().race());
			}
		};

		ToolConfig fixed = new ToolConfig() {
			
			@Override
			public boolean back() {
				return false;
			};
			
			@Override
			public void addUI(LISTE<RENDEROBJ> uis) {
				stages.v.tools.placer.addStandardButtons(uis, false);
			}
			
		};
		
		stages.v.tools.place(t,fixed);
		
	}
	
	private void generate(int cx, int cy) {
		World.GEN().playerX = cx;
		World.GEN().playerY = cy;
		generate(g, buildings, landmarks);
	}
	
	static void regenerate() {
		final WorldGeneratorRegions g = new WorldGeneratorRegions();
		final WorldGeneratorBuildings buildings = new WorldGeneratorBuildings();
		final WorldGeneratorLandmarks landmarks = new WorldGeneratorLandmarks();
		
		landmarks.clear();
		buildings.clear();
		g.clear();
		
		generate(g, buildings, landmarks);
	}
	
	
	public static void generate(WorldGeneratorRegions g, WorldGeneratorBuildings buildings, WorldGeneratorLandmarks landmarks) {
		
		RES.loader().print(DicMisc.¤¤Generating);
		landmarks.generate();
		RES.loader().print(DicMisc.¤¤Generating);
		buildings.generate();
		RES.loader().print(DicMisc.¤¤Generating);
		g.clear();
		g.setPlayer(World.GEN().playerX, World.GEN().playerY);
		RES.loader().print(DicMisc.¤¤Generating);
		g.generateAllAreas();
		RES.loader().print(DicMisc.¤¤Generating);
		g.initAll(true);
		RES.loader().print(DicMisc.¤¤Generating);
		g.randomiseNames();
		RES.loader().print(DicMisc.¤¤Generating);
		g.makeRoads();
		RES.loader().print(DicMisc.¤¤Generating);
		g.makeDistances();
		RES.loader().print(DicMisc.¤¤Generating);
		g.populateAll();
		RES.loader().print(DicMisc.¤¤Generating);
		new WoldGeneratorCamps();
		RES.loader().print(DicMisc.¤¤Generating);
		g.makeFactions(World.GEN());
		RES.loader().print(DicMisc.¤¤Generating);
		
		MINIMAP().repaint();
		
	}
	
	public void clear() {
		landmarks.clear();
		buildings.clear();
		g.clear();
		
	}
	
}
