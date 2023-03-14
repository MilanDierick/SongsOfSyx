package view.world.generator;

import static world.World.*;

import game.GAME;
import game.faction.FACTIONS;
import init.*;
import init.sprite.ICON;
import init.sprite.SPRITES;
import settlement.main.SGenerationConfig;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.*;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import view.tool.PlacableSimple;
import view.tool.ToolConfig;
import view.world.ui.WorldHoverer;
import world.World;
import world.map.regions.WorldGeneratorRegions;

class StageFinish{

	private static CharSequence ¤¤inspectRegions = "Inspect World";
	static {
		D.ts(StageFinish.class);
	}
	
	
	public StageFinish(Stages stages) {
		
		stages.reset();
		LinkedList<CLICKABLE> butts = new LinkedList<>();
		
		final PlacableSimple simp = new PlacableSimple(¤¤inspectRegions) {
			
			@Override
			public void place(int x, int y) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				
				
				return null;
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return butts;
			}
			
			@Override
			public void renderPlaceHolder(SPRITE_RENDERER r, int cx, int cy, boolean isPlacable) {
				
			}
			
			@Override
			public void renderAction(int cx, int cy) {
				//World.OVERLAY().landmarks().add();
				World.OVERLAY().regions().add();
				super.renderAction(cx, cy);
			}
			
			@Override
			public void placeInfo(GBox hoverBox, int cx, int cy) {
				int tx = cx/C.TILE_SIZE;
				int ty = cy/C.TILE_SIZE;
				if (World.REGIONS().getter.get(tx, ty) == FACTIONS.player().capitolRegion())
					;
				else if (World.REGIONS().getter.get(tx, ty) != null) {
					WorldHoverer.hover(hoverBox, World.REGIONS().getter.get(tx, ty));
					World.OVERLAY().hoverRegion(World.REGIONS().getter.get(tx, ty));
				}else if (World.LANDMARKS().setter.get(tx, ty) != null) {
					World.OVERLAY().hoverLandmark(World.LANDMARKS().setter.get(tx, ty));
				}
				
			}
		
			
		};
		

		
		butts.add(new GButt.ButtPanel(SPRITES.icons().m.arrow_left) {
			
			@Override
			protected void clickA() {
				new StageCapitol(stages, true);
			}
			
			@Override
			protected void renAction() {
				
			}
			
		}.hoverInfoSet(DicMisc.¤¤Back));
		butts.add(new GButt.ButtPanel(new ICON.MEDIUM.Twin(SPRITES.icons().m.terrain, SPRITES.icons().m.rotate)){
			@Override
			protected void clickA() {
				StageCapitol.regenerate();
			};
		}.hoverInfoSet(Stages.¤¤regenerate));

		butts.add(new GButt.ButtPanel(SPRITES.icons().m.crossair){
			@Override
			protected void clickA() {
				stages.v.window.centerAtTile(World.GEN().playerX, World.GEN().playerY);
			};
		}.hoverInfoSet(Stages.¤¤home));
		
		butts.add(new GButt.ButtPanel(SPRITES.icons().m.arrow_right){
			@Override
			protected void clickA() {
				RES.loader().minify(false, DicMisc.¤¤Generating);
				new WorldGeneratorRegions().finish(GEN());
				GAME.s().CreateFromWorldMap(GEN().playerX-1, GEN().playerY-1, new SGenerationConfig());
				GAME.saveNew();
				CORE.getInput().clearAllInput();
			};
		}.hoverInfoSet(DicMisc.¤¤OK));

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
		stages.v.tools.place(simp,fixed);
		
	}
	
}
