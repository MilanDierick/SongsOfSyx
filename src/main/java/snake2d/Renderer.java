package snake2d;

import snake2d.util.color.*;
import snake2d.util.light.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.TextureCoords;

public final class Renderer extends CORE_RESOURCE implements SPRITE_RENDERER{
	
	private RenderMode renderMode;
	private int zoomout = 0;
	private final int pointSize;
	
	private final byte noShadows = (byte) 255;
	private final byte shadows = 0;
	private byte shadowDepth = noShadows;
	private byte lightDepth = shadows;
	private TextureHolder texture;
	
	Renderer (RenderMode mode, int pointSize){
		renderMode = mode;
		this.pointSize = pointSize;
		zoomout = 0;
	}
	
	void setTexture(TextureHolder texture) {
		this.texture = texture;
	}
	
	@Override
	void dis() {
		renderMode.dis();
	}
	
	void flush() {
		renderMode.flush(pointSize >> zoomout);
		if (texture != null)
			texture.flush();
		clear();
	}
	
	private int spritesRendered = 0;
	private int shadowsRendered = 0;
	private int particlesRendererd  = 0;
	private int lightsRendererd  = 0;
	private int shadowsRenderedO = 0;
	private int spritesRenderedO = 0;
	private int particlesRendererdO  = 0;
	private int lightsRendererdO  = 0;
	
	private final COLOR white = ColorImp.WHITE100;
	private COLOR current = white;
	private final OPACITY OpacityDefault = OpacityImp.O100;
	private OPACITY currentOpacity = OpacityDefault;
	
	public int pointsize() {
		return pointSize >> zoomout;
	}
	
	public int getSpritesSprocessed(){
		return spritesRenderedO;
	}
	
	public int getParticlesProcessed(){
		return particlesRendererdO;
	}
	
	public int getLightsProcessed(){
		return lightsRendererdO;
	}
	
	public int getShadowsRendered() {
		return shadowsRenderedO;
	}
	
    public void setColor(COLOR color){
    	current = color;
    }
    
    public COLOR colorGet() {
    	return current;
    }
    
    public COLOR getBoundColor() {
    	return current;
    }
    
    public void setNormalColor(){
    	current = white;
    }
    
	public void setOpacity(OPACITY o){
		currentOpacity = o;
	}
	
	public void setNormalOpacity(){
		currentOpacity = OpacityDefault;
	}
	
	public boolean isNormalOpacity(){
		return currentOpacity == OpacityDefault;
	}
    
	/**
	 * 
	 * @param shade shadows shade all light vs no lights
	 */
	public void shadeLight(boolean shade){
		lightDepth = shade ? shadows : noShadows;
	}
	
	/**
	 * set depth. Lights with smaller or equal depth will be shaded by the shadows
	 * @param depth
	 */
	public void shadowDepthSet(byte depth) {
		this.shadowDepth = depth;
	}
	
	/**
	 * Set shadow depth to shade all lights
	 */
	public void shadowDepthDefault() {
		this.shadowDepth = noShadows;
	}
	
	/**
	 * set a depth. Shadows with higher or equal dephs will shade lights
	 * @param depth
	 */
	public void lightDepthSet(byte depth) {
		lightDepth = depth;
	}
	
	@Override
	public void renderSprite(int x1, int x2, int y1, int y2, TextureCoords t){
		renderTextured(x1, x2, y1, y2, t, t);
	}
	
	public void renderTextured(int x1, int x2, int y1, int y2, 
			TextureCoords texture, 
			TextureCoords stencil){
		
		if (zoomout != 0){
			x1 = x1 >> zoomout;
			x2 = x2 >> zoomout;
			y1 = y1 >> zoomout;
			y2 = y2 >> zoomout;
		}
		if (x2 < 0 || y2 < 0 || x1 > CORE.getGraphics().nativeWidth || y1 > CORE.getGraphics().nativeHeight)
			return;
		renderMode.renderSprite(
				stencil, 
				texture, 
				x1, x2, y1, y2, current, currentOpacity);
		
		spritesRendered ++;
	}
	
	public void renderDisplaced(int x1, int x2, int y1, int y2, 
			TextureCoords displacement, 
			TextureCoords texture){
		
		renderDisplace(displacement.x1(), displacement.y1(), texture.x1(), texture.y1(), texture.x2()-texture.x1(), texture.y2()-texture.y1(), 16, x1, x2, y1, y2);
		
	}
	
	public void renderDisplaced(int x1, int x2, int y1, int y2, double scale,
			TextureCoords displacement, 
			TextureCoords texture){
		
		renderDisplace(displacement.x1(), displacement.y1(), texture.x1(), texture.y1(), texture.x2()-texture.x1(), texture.y2()-texture.y1(), scale*16.0, x1, x2, y1, y2);
		
	}
	
	public void renderDisplace(float tx1, float ty1, float dx1, float dy1, int w, int h, double scale, int x1, int x2, int y1, int y2) {
		if (zoomout != 0){
			x1 = x1 >> zoomout;
			x2 = x2 >> zoomout;
			y1 = y1 >> zoomout;
			y2 = y2 >> zoomout;
		}
		
		renderMode.renderDisplace(tx1, ty1, dx1, dy1, w, h, scale, x1, x2, y1, y2, current, currentOpacity);
		spritesRendered ++;
	}

	private void renderShadow(
    		TextureCoords t, 
    		int x1, int y1, int x2, int y2,
    		int x3, int y3, int x4, int y4, byte d){
		
    	renderMode.renderShadow(t, 
    			x1, y1, x2, y2, x3, y3, x4, y4, d, shadowDepth);
    	shadowsRendered ++;
	}
	
	public void renderShadow(int x1, int x2, int y1, int y2, TextureCoords stencil, byte d){
		
		if (zoomout != 0){
			x1 = x1 >> zoomout;
			x2 = x2 >> zoomout;
			y1 = y1 >> zoomout;
			y2 = y2 >> zoomout;
		}
		
		if (x2 < 0 || y2 < 0 || x1 > CORE.getGraphics().nativeWidth || y1 > CORE.getGraphics().nativeHeight)
			return;
		
		renderShadow(
				stencil, 
				x1, y1, 
				x2, y1, 
				x2, y2, 
				x1, y2, d
				);
	}
	
	public void renderParticle(int x, int y){
		renderParticle(x, y, (byte) 128, (byte) 128, (byte) 255, (byte)255);
	}
    
	public void renderParticleFlat(int x, int y){
		renderParticle(x, y, (byte) 128, (byte) 128, (byte) 255, (byte)0);
	}
	
	public void renderParticle(int x, int y, byte nX, byte nY, byte nZ, byte nA){
		if (zoomout > 2) {
			return;
		}
		if (zoomout != 0){
			x = x >> zoomout;
			y = y >> zoomout;
		}
		
		if (x < 0 || y < 0 || x > CORE.getGraphics().nativeWidth || y > CORE.getGraphics().nativeHeight)
			return;
		
		renderMode.renderParticle((short)x, (short)y, nX, nY, nZ, nA, current, currentOpacity);
		particlesRendererd ++;
	}
	
	public void registerLight(LIGHT_POINT light, int x1, int x2, int y1, int y2){
		registerLight(light, x1, x2, y1, y2, (byte)255, (byte)255, (byte)255, (byte)255);
	}
	
	/**
	 * 
	 * @param light
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param mask 0b0000|NW|SW|SE|NE
	 */
	public void registerLight(LIGHT_POINT light, int x1, int x2, int y1, int y2, byte ne, byte se, byte sw, byte nw){
		lightsRendererd ++;
		if (zoomout != 0){
			x1 = x1 >> zoomout;
			x2 = x2 >> zoomout;
			y1 = y1 >> zoomout;
			y2 = y2 >> zoomout;
			renderMode.registerLight(light, (int)light.cx()>>zoomout, (int)light.cy()>>zoomout, (int)light.cz(), light.getRadius() >> zoomout, x1, x2, y1, y2, ne, se, sw, nw, lightDepth);
			return;
		}
		renderMode.registerLight(light, light.cx(), light.cy(), light.cz(), light.getRadius(), x1, x2, y1, y2, ne, se, sw, nw, lightDepth);
	}
	
	public void registerAmbient(LIGHT_AMBIENT light, int x1, int x2, int y1, int y2){
		lightsRendererd ++;
		if (zoomout != 0){
			x1 = x1 >> zoomout;
			x2 = x2 >> zoomout;
			y1 = y1 >> zoomout;
			y2 = y2 >> zoomout;
		}
		renderMode.registerAmbient(light, x1, x2, y1, y2, lightDepth);
	}
	
	/**
	 * 
	 * @param keeplight - the lights from previous layer?
	 * @param zoomout - how much zoom-out, will be an exponent of two.
	 * @return the index of this layer
	 */
	public void newLayer(boolean keeplight, int zoomout){
		this.zoomout = zoomout;
		int pz = CLAMP.i(pointSize >> zoomout, 1, pointSize);
		renderMode.newLayer(keeplight, pz);
	}
	
	
	/**
	 * Makes a new layer. This layer and all the following will be lit with the same light. Should only
	 * be called once. For optimisation (need only upload the added lights light and process them 
	 * once for all layers.)
	 * @param zoomout
	 */
	public int newFinalLightWithShadows(int zoomout){
		this.zoomout = zoomout;
		int pz = CLAMP.i(pointSize >> zoomout, 1, pointSize);
		return renderMode.newFinalLightLayer(pz);
	}
	
	
	public void clear(){
		renderMode.clear(pointSize >> zoomout);
		spritesRenderedO = spritesRendered;
		particlesRendererdO = particlesRendererd;
		lightsRendererdO = lightsRendererd;
		shadowsRenderedO = shadowsRendered;
		shadowsRendered = 0;
		spritesRendered = 0;
		particlesRendererd = 0;
		lightsRendererd = 0;
		zoomout = 0;
	}

	public int getZoomout() {
		return zoomout;
	}
	
	public void renderTileLight(int x1, int y1, int dim, byte nw, byte ne, byte se, byte sw) {
		if (zoomout != 0){
			x1 = x1 >> zoomout;
			y1 = y1 >> zoomout;
			dim = dim >> zoomout;
		}
		renderMode.renderTilelight(x1, y1, dim, nw, ne, se, sw);
	}
	
	public void setTileLight(AmbientLight l) {
		renderMode.setTileLight(l, shadowDepth);
	}

	public void renderUniLight(int x, int y, int z, int radius) {
		if (zoomout != 0){
			x = x >> zoomout;
			y = y >> zoomout;
			z = z >> zoomout;
			radius = radius >> zoomout;
		}
		renderMode.renderPointlight(x, y, z, radius);
	}
	
	public void setUniLight(LIGHT_POINT l) {
		renderMode.setPointLight(l, shadowDepth);
	}

	public void setZoom(int i) {
		zoomout = i;
	}
	
}
