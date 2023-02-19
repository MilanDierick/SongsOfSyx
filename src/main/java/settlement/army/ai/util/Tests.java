package settlement.army.ai.util;

import init.sprite.SPRITES;
import settlement.army.Div;
import settlement.main.*;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import util.colors.GCOLORS_MAP;
import util.rendering.ShadowBatch;
import view.sett.IDebugPanelSett;

class Tests {

	Tests(ArmyAIUtilThread t){
		ON_TOP_RENDERABLE top = new ON_TOP_RENDERABLE() {
			
			private final ArrayList<Div> res = new ArrayList<>(16);
			@Override
			public void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
				RenderData.RenderIterator it = data.onScreenTiles();
				
				DivsTileMap m = ArmyAIUtil.map();
				
				while(it.has()) {
					
					res.clear();
					int ai = 0;
					for (Div d : m.get(res, it.tx(), it.ty())){
						if (d.army() == SETT.ARMIES().player()) {
							GCOLORS_MAP.GOOD.bind();
							
						}else {
							GCOLORS_MAP.BAD.bind();
						}
						SPRITES.icons().s.dot.renderC(r, it.x()+(ai%4)*16, it.y()+ (ai/4)*16);
						ai++;
						
					}
					
					
					
					it.next();
					
				}
				
				COLOR.unbind();
				
			}
		};
		
		IDebugPanelSett.add("Army Maps", new ACTION() {
			
			@Override
			public void exe() {
				top.add();
			}
		});
		
	}
	
}
