package view.world.generator;

import static world.WORLD.*;

import game.faction.FACTIONS;
import init.C;
import init.D;
import init.sprite.SPRITES;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.panel.GPanel;
import view.tool.PlacableFixedImp;
import view.tool.ToolConfig;
import view.world.generator.tools.UIWorldToolCapitolPlaceInfo;
import world.WORLD;
import world.map.buildings.WorldGeneratorBuildings;
import world.map.buildings.camp.WoldGeneratorCamps;
import world.map.landmark.WorldGeneratorLandmarks;
import world.map.pathing.WGenPath;
import world.map.terrain.WorldGenTerrain;
import world.regions.centre.WCentre;
import world.regions.centre.WorldCentrePlacablity;
import world.regions.data.RD;

class StageCapitol{

	private static CharSequence ¤¤name = "Place Capital";
	static CharSequence ¤¤prompt = "¤Generating new terrain will reset faction and regions. Continue?";
	static {
		D.ts(StageCapitol.class);
	}
	
	private final WorldGeneratorBuildings buildings = new WorldGeneratorBuildings();
	private final WorldGeneratorLandmarks landmarks = new WorldGeneratorLandmarks();
	
	public StageCapitol(WorldViewGenerator stages, boolean clear) {
		if (clear) {
			WorldViewGenerator.loadPrint.exe();
			clear(buildings, landmarks);
			MINIMAP().repaint();
		}
		stages.minimap.show();
//		LinkedList<RENDEROBJ> butts = new LinkedList<>();

		GuiSection butts = new GuiSection();
		
		butts.add(new GButt.ButtPanel(new SPRITE.Twin(SPRITES.icons().m.terrain, SPRITES.icons().m.rotate)){
			@Override
			protected void clickA() {
				WORLD.GEN().seed = RND.rInt(Integer.MAX_VALUE);
				new WorldGenTerrain().generateAll(WORLD.GEN(), WorldViewGenerator.loadPrint);
			};
			
			@Override
			protected void renAction() {
				WORLD.OVERLAY().landmarks.add();
			}
			
		}.hoverInfoSet(WorldViewGenerator.¤¤regenerate));
		butts.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.admin){
			@Override
			protected void clickA() {
				new StageEdit(stages);
			};
			
			@Override
			public void hoverInfoGet(snake2d.util.gui.GUI_BOX text) {
				text.title(StageEdit.¤¤name);
			};
			
		}.hoverInfoSet(DicMisc.¤¤Terrain));
		if (WORLD.GEN().playerX > -1) {
			butts.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.arrow_right){
				@Override
				protected void clickA() {
					stages.set();
				};
			}.hoverInfoSet(DicMisc.¤¤Next));
		}
		

		
		PlacableFixedImp t = new PlacableFixedImp(¤¤name, 1, 1) {
			
			final UIWorldToolCapitolPlaceInfo info = new UIWorldToolCapitolPlaceInfo();
			
			@Override
			public int width() {
				return WCentre.TILE_DIM;
			}
			
			@Override
			public void place(int tx, int ty, int rx, int ry) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterPlaced(int tx1, int ty1) {
				int cx = tx1+WCentre.TILE_DIM/2;
				int cy = ty1+WCentre.TILE_DIM/2;
				stages.reset();
				generate(cx, cy);
				stages.set();
			}
			
			@Override
			public CharSequence placableWhole(int tx1, int ty1) {
				return WorldCentrePlacablity.terrain(tx1, ty1);
			}

			
			@Override
			public int height() {
				return WCentre.TILE_DIM;
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return null;
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

		GPanel p = new GPanel(260, butts.body().height()).setButt();
		p.setTitle(¤¤name);
		p.inner().set(butts);
		butts.add(p);
		butts.moveLastToBack();
		butts.body().moveY1(64).centerX(C.DIM());
		
		ToolConfig fixed = new ToolConfig() {
			
			@Override
			public boolean back() {
				return false;
			};
			
			@Override
			public void addUI(LISTE<RENDEROBJ> uis) {
				uis.add(butts);
				//stages.tools.placer.addStandardButtons(uis, false);
			}
			
		};
		
		stages.tools.place(t,fixed);
		
	}
	
	private void generate(int cx, int cy) {
		WORLD.GEN().playerX = cx;
		WORLD.GEN().playerY = cy;
		generate(buildings, landmarks);
	}
	
	static void regenerate() {
		WorldViewGenerator.loadPrint.exe();
		final WorldGeneratorBuildings buildings = new WorldGeneratorBuildings();
		final WorldGeneratorLandmarks landmarks = new WorldGeneratorLandmarks();
		
		landmarks.clear();
		buildings.clear();
		
		generate(buildings, landmarks);
	}
	
	
	public static void generate(WorldGeneratorBuildings buildings, WorldGeneratorLandmarks landmarks) {

		WORLD.OVERLAY().regNames.active.set(false);
		WorldViewGenerator.loadPrint.exe();
		landmarks.generate();
		WorldViewGenerator.loadPrint.exe();
		buildings.generate();
		WorldViewGenerator.loadPrint.exe();
		
		
		new WorldGenTerrain().connect(WorldViewGenerator.loadPrint);
		
 
		WORLD.REGIONS().generator().generate(WORLD.GEN().playerX, WORLD.GEN().playerY, WorldViewGenerator.loadPrint);
		
		WGenPath roads = new WGenPath();
		roads.generateAll(WORLD.GEN().playerX, WORLD.GEN().playerY, WorldViewGenerator.loadPrint);
//
//		

		WorldViewGenerator.loadPrint.exe();
		new WoldGeneratorCamps();
		WorldViewGenerator.loadPrint.exe();
		RD.generator().generate(WorldViewGenerator.loadPrint);
		RD.generator().makeFactions(WORLD.GEN(), WorldViewGenerator.loadPrint);
		
		WorldViewGenerator.loadPrint.exe();
		//RD.generator().finish(WORLD.GEN());
		MINIMAP().repaint();
		WORLD.initBeforePlay();
		WORLD.OVERLAY().regNames.active.set(true);
	}
	
	public static void clear() {
		WorldGeneratorBuildings buildings = new WorldGeneratorBuildings();
		WorldGeneratorLandmarks landmarks = new WorldGeneratorLandmarks();
		clear(buildings, landmarks);
	}
	
	private static void clear(WorldGeneratorBuildings buildings, WorldGeneratorLandmarks landmarks) {
		WorldViewGenerator.loadPrint.exe();
		WORLD.GEN().playerX = -1;
		WORLD.GEN().playerY = -1;
		landmarks.clear();
		buildings.clear();
		new WGenPath().clear();
		WORLD.REGIONS().generator().clear();
		
	}
	
}
