package settlement.tilemap;

import init.sprite.ICON;
import init.sprite.SPRITES;
import settlement.main.RenderData;
import settlement.path.AVAILABILITY;
import settlement.tilemap.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import util.gui.misc.GBox;
import util.rendering.ShadowBatch;

public class TNothing extends TerrainTile{

	protected TNothing(Terrain shared) {
		super(
				shared, "clear", 
				new ICON.MEDIUM.Twin(SPRITES.icons().m.terrain, SPRITES.icons().m.cancel), 
				null);
	}

	@Override
	protected boolean place(int x, int y) {
		placeRaw(x, y);
		return false;
	}

	@Override
	protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		return false;
	}

	@Override
	protected boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		return false;
	}

	@Override
	public boolean isPlacable(int tx, int ty) {
		return true;
	}

	@Override
	public AVAILABILITY getAvailability(int tx, int ty) {
		return null;
	}
	
	@Override
	void hoverInfo(GBox box, int tx, int ty) {
		
	}
	
	@Override
	public int miniDepth() {
		return 0;
	}
	
}
