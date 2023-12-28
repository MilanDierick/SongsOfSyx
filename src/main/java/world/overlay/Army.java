package world.overlay;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.sprite.SPRITES;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import util.colors.GCOLORS_MAP;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.regions.Region;

class Army extends WorldOverlays.OverlayTile{

	private Faction army;
	private final COLOR rebel = new ColorImp(GCOLORS_MAP.FRebel).shadeSelf(0.3);
	private final COLOR ally = new ColorImp(GCOLORS_MAP.FAlly).shadeSelf(0.3);
	private final COLOR enemy = new ColorImp(GCOLORS_MAP.FEnemy).shadeSelf(0.3);
	private final COLOR soso = new ColorImp(GCOLORS_MAP.SOSO).shadeSelf(0.3);
	Army() {
		super(true, true);
	}
	
	
	void add(Faction army) {
		this.army = army;
		super.add();
	}
	
	
	@Override
	protected void renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
		int m = 0x0F;
		
		if (WORLD.PATH().route.is(it.tile())) {
			COLOR.WHITE100.bind();
			renderUnder(m, r, it);
			return;
			
		}
		
		Region reg = WORLD.REGIONS().map.get(it.tile());
		if (WORLD.REGIONS().border().is(it.tile())) {
			m = 0;
			for (DIR d : DIR.ORTHO) {
				if (!WORLD.IN_BOUNDS(it.tx(), it.ty(), d) || reg == WORLD.REGIONS().map.get(it.tx(), it.ty(), d)){
					m |= d.mask();
				}
			}
		}
			
		COLOR c = rebel;
		
		if (reg != null && reg.faction() != null) {
			if (reg.faction() == army)
				c = ally;
			else if (FACTIONS.DIP().war.is(army, reg.faction())) {
				c = enemy;
			}else {
				c = soso;
			}
		}
		c.bind();
		renderUnder(m, r, it);
		
	}
	
	@Override
	public void renderAbove(Renderer r, ShadowBatch s, RenderData data) {
		WORLD.OVERLAY().regNames.renderAbove(r, s, data);
		COLOR.WHITE100.bind();
		super.renderAbove(r, s, data);
	}
	
	@Override
	protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
		if (WORLD.PATH().route.is(it.tile()) && WORLD.WATER().isBig.is(it.tile()))
			SPRITES.cons().BIG.line.render(r, 0, it.x(), it.y());
	}
	
}
