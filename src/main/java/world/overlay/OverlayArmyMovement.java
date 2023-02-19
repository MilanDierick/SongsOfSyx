package world.overlay;

import init.sprite.SPRITES;
import settlement.main.RenderData.RenderIterator;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import util.rendering.ShadowBatch;
import world.World;
import world.entity.WPathing;
import world.entity.army.WArmy;
import world.map.regions.Region;

final class OverlayArmyMovement extends WorldOverlayer{

	OverlayArmyMovement() {
		super("", "");
		// TODO Auto-generated constructor stub
	}
	
	private WArmy a;
	
	void renderInit() {
		WPathing.checkRegionMovement(a.ctx(), a.cty(), a.cost());
	}
	
	public void render(Renderer r, ShadowBatch s, RenderIterator it) {
		Region a = World.REGIONS().getter.get(it.tile());
		
		if (a != null) {
			
			if (!WPathing.checkRegionIs(a)) {
				COLOR.WHITE65.bind();
				SPRITES.cons().BIG.dashedThick.render(r, 0x0F, it.x(), it.y());
				COLOR.unbind();
			}
		}
		
	}
	
	void add(WArmy a) {
		add();
		this.a = a;
	}

}
