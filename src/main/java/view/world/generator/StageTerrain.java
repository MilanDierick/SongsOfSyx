package view.world.generator;

import init.C;
import init.sprite.SPRITES;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.dic.DicMisc;
import util.gui.misc.GButt;
import view.world.generator.tools.UIWorldGenerateTerrain;
import world.World;
import world.map.terrain.WorldGeneratorTerrain;

class StageTerrain{

	public StageTerrain(Stages stages) {
		

		GuiSection s = new UIWorldGenerateTerrain(World.GEN());
		s.body().centerIn(C.DIM());
		
		GuiSection ss = new GuiSection();
		ss.add(new GButt.ButtPanel(SPRITES.icons().m.arrow_left) {
				@Override
				protected void clickA() {
					stages.titles();
				}
		}.hoverInfoSet(DicMisc.¤¤Previous));
		
		ss.addRightC(2, new GButt.ButtPanel(Stages.¤¤generate) {
			@Override
			protected void clickA() {
				stages.dummy.add(null);
				new WorldGeneratorTerrain().generateAll(World.GEN());
				World.GEN().hasGeneratedTerrain = true;
				stages.set();
				
			}
			
		}.hoverInfoSet(DicMisc.¤¤Next));
		if (World.GEN().hasGeneratedTerrain) {
			ss.addRightC(2, new GButt.ButtPanel(SPRITES.icons().m.arrow_right) {
				@Override
				protected void clickA() {
					new StageCapitol(stages, false);
				}
				
			});
		}
		
		s.addRelBody(8, DIR.S, ss);
		
		stages.dummy.add(s);
		
	}
	
}
