package settlement.overlay;

import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import snake2d.Renderer;
import util.rendering.ShadowBatch;

public interface ON_TOP_TILE {
	
	public default void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
		RenderIterator it = data.onScreenTiles();
		while(it.has()) {
			render(r, shadowBatch, it);
			it.next();
		}
	}
	
	public abstract void render(Renderer r, ShadowBatch shadowBatch, RenderIterator it);
	
}