package settlement.main;

import game.GameDisposable;
import snake2d.Renderer;
import snake2d.util.sets.ArrayList;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

public abstract class ON_TOP_RENDERABLE{
	
	static final ArrayList<ON_TOP_RENDERABLE> renderables = new ArrayList<ON_TOP_RENDERABLE>(64);
	static {
		new GameDisposable() {
			@Override
			protected void dispose() {
				renderables.clear();
			}
		};
	}
	
	
	private boolean isAdded = false;
	public abstract void render(Renderer r, ShadowBatch shadowBatch, RenderData data);
	
	public void add() {
		if (isAdded)
			return;
		renderables.add(this);
		isAdded = true;
	}
	
	public void remove() {
		if (!isAdded)
			return;
		renderables.remove(this);
		isAdded = false;
	}
	
	public boolean isAdded() {
		return isAdded;
	}
	

}