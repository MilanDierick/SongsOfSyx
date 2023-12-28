package snake2d;

import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.light.LIGHT_AMBIENT;
import snake2d.util.light.LIGHT_POINT;
import snake2d.util.sprite.TextureCoords;

abstract class RenderMode extends CORE_RESOURCE{

	public abstract int newLayer(boolean keepLights, int pointSize);
	public abstract int newFinalLightLayer(int pointSize);
	public abstract void clear(int pointSize);
	
	public abstract void renderSprite(
			TextureCoords t, 
			TextureCoords to, 
    		int x1, int x2, int y1, int y2, 
    		COLOR color, OPACITY opacity);
	

	
	public abstract void renderShadow(
			TextureCoords t, 
    		int x1, int y1, int x2, int y2,
    		int x3, int y3, int x4, int y4, byte d, byte depth
    		);
	
	public abstract void renderParticle(short x, short y, byte nx, byte ny, byte nz, byte nA, COLOR color, OPACITY opacity);
	
	/**
	 * 
	 * @param l
	 * @param x
	 * @param y
	 * @param radius
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param mask  0b0000|NW|SW|SE|NE
	 * @param depth
	 */
	public abstract void registerLight(LIGHT_POINT l, float x, float y, float z, int radius, 
    		int x1, int x2, int y1, int y2, byte ne, byte se, byte sw, byte nw, byte depth);

	public abstract void registerAmbient(LIGHT_AMBIENT l, 
    		int x1, int x2, int y1, int y2, byte depth);
	
	public abstract void applySettings(SETTINGS sett);

	public abstract void flush(int pointSize);

	public abstract void renderTilelight(int x1, int y1, int dim, byte nw, byte ne, byte se, byte sw);
	
	public abstract void setTileLight(LIGHT_AMBIENT l, byte depth);
	
	public abstract void renderPointlight(int x, int y, int z, int radius);
	
	public abstract void setPointLight(LIGHT_POINT l, byte depth);
	
	public abstract void renderDisplace(float tx1, float ty1, float dx1, float dy1, int w, int h, double scale, int x1, int x2, int y1, int y2, COLOR color, OPACITY opacity);
	
	public abstract void setMaxDepth(int x1, int x2, int y1, int y2, TextureCoords stencil, int depth);
	
}
