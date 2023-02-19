package launcher;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.Rec;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.BigSprite;
import snake2d.util.sprite.SPRITE;

class BG {

	private Rec quadBounds;
	private final Rec mapBounds;
	private BigSprite map;
	private final SPRITE[] sprites; 
	private Cloud[] clouds;
	private BigCloud[] bigClouds;
	private float cloudTimer;
	
	private final Coo mapAcc = new Coo(10,10);
	private final Coo mapSpeed;
	private final float maxSpeed = 100f;
	
	
	BG () {
		
		map = Resources.Sprites.syxMap;
		
		mapSpeed = new Coo(40 + 20*RND.rFloat(), 40 + 20*RND.rFloat());
		quadBounds = new Rec(0, Sett.WIDTH, 0, Sett.HEIGHT);
		mapBounds = new Rec(quadBounds);
		mapBounds.moveX1(RND.rInt(map.getGameWidth()-quadBounds.width()));
		mapBounds.moveY1(RND.rInt(map.getGameHeight()-quadBounds.height()));
		
		
		if (RND.rBoolean())
			mapSpeed.xInvert();
		if (RND.rBoolean())
			mapSpeed.yInvert();
		
		sprites = Resources.Sprites.clouds;
		clouds = new Cloud[15];
		for (int i = 0; i < clouds.length; i++)
			clouds[i] = new Cloud(RND.rFloat() + 1f);
		bigClouds = new BigCloud[25];
		for (int i = 0; i < bigClouds.length; i++)
			bigClouds[i] = new BigCloud(RND.rFloat()*3 + 3f);
		cloudTimer = RND.rInt(50)+50;
		
	}
	
	float s = 0;
	
	void update(float ms){
		
		s+= ms;
		
		if (mapSpeed.x() >= maxSpeed || mapSpeed.x() <= -maxSpeed)
			mapAcc.xInvert();
		if (mapSpeed.y() >= maxSpeed || mapSpeed.y() <= -maxSpeed)
			mapAcc.yInvert();

		else if (s >= 10){
			s = 0;
			if (RND.rBoolean())
				mapAcc.xInvert();
			if (RND.rBoolean())
				mapAcc.yInvert();
		}
		
		if (mapBounds.x2() >= map.getGameWidth()){
			mapAcc.xSet(-10);
			mapSpeed.xMakeNeg();
			mapBounds.moveX2(map.getGameWidth());
		}else if (mapBounds.x1() <= 0){
			mapAcc.xSet(10);
			mapSpeed.xMakePos();
			mapBounds.moveX1(0);
		}
		
		if (mapBounds.y2() >= map.getGameHeight()) {
			mapAcc.ySet(-10);
			mapSpeed.yMakeNeg();
			mapBounds.moveY2(map.getGameHeight());
		}else if (mapBounds.y1() <= 0){
			mapAcc.ySet(10);
			mapSpeed.yMakePos();
			mapBounds.moveY1(0);
		}
		
		mapSpeed.xIncrement(mapAcc.x()*ms);
		mapSpeed.yIncrement(mapAcc.y()*ms);
		
		mapBounds.incrX(mapSpeed.x()*ms);
		mapBounds.incrY(mapSpeed.y()*ms);

		
		for (Cloud cloud: clouds)
			if (!cloud.update(ms))
				cloud.reIni();
		
		releaseTheClouds(ms);
			
		for (BigCloud cloud: bigClouds)
			cloud.update(ms);
		
	}
	
	void render(SPRITE_RENDERER r, float ds){
		
		map.render(r, quadBounds.x1(), quadBounds.y1(), mapBounds);
		
		for (Cloud cloud: clouds)
			cloud.renderShadow(r, ds);
		for (Cloud cloud: clouds)
			cloud.render(r, ds);
	}
	
	public void renderClouds(SPRITE_RENDERER r, float ds) {
		for (BigCloud cloud: bigClouds)
			cloud.render(r, ds);
	}
	
	private void releaseTheClouds(float ds){
		cloudTimer -= ds;
		if (cloudTimer < 0){
			cloudTimer += RND.rInt(50)+50;
			for (BigCloud c: bigClouds)
				c.reIni();
		}
		//cloudRelease.play();
	}
	
	class Cloud extends LSprite{
		
		private static final float ySpeed = -30f;
		private static final float xSpeed = 28f; 
		private final float scale;
		private final Rec shadowBounds;
		private OpacityImp shadowOp;
		
		Cloud (float scale){
			super(sprites[RND.rInt(sprites.length)], 0, 0);
			this.scale = scale;
			body().scale(scale, scale);
			shadowBounds = new Rec(0, body().width()*scale, 0, body().height()*scale);
			shadowBounds.moveX1(- quadBounds.width() + 2*RND.rInt(quadBounds.width()));
			shadowBounds.moveY1(RND.rInt((int)  quadBounds.y2()));
			getOpacity().set(RND.rInt(255));
			shadowOp = new OpacityImp((int) (Byte.toUnsignedInt(getOpacity().get())*0.5));
			update(0);
		}
		
		boolean update(float ms){
			shadowBounds.incrY(ySpeed*scale*ms - mapSpeed.y()*ms);
			shadowBounds.incrX(xSpeed*scale*ms - mapSpeed.x()*ms);
			body().moveX1(shadowBounds.x1());
			body().moveY1(shadowBounds.y1());
			return shadowBounds.touches(quadBounds);
		}
		
		void reIni(){
			shadowBounds.moveX1(- quadBounds.width() + 2*RND.rInt(quadBounds.width()));
			
			if (mapSpeed.y() < ySpeed*scale*scale){
				shadowBounds.moveY2(0);
			}else{
				shadowBounds.moveY1(quadBounds.y2());
			}

		}

		private void renderShadow(SPRITE_RENDERER r, float ds){
			shadowOp.bind();
			COLOR.BLACK.bind();
			this.sprite.render(r, shadowBounds.x1(), shadowBounds.x2(), shadowBounds.y1(), shadowBounds.y2());
			OPACITY.unbind();
			COLOR.unbind();
		}
		
	}
	
	class BigCloud extends LSprite{
		
		private static final float ySpeed = -55f;
		private static final float xSpeed = 38f; 
		private final float scale;
		
		BigCloud (float scale){
			super(sprites[RND.rInt(sprites.length)], 0, 0);
			body().scale (scale, scale);
			this.scale = scale;
			reIni();
		}
		
		void update(float ms){
			body().incrY(ySpeed*scale*scale*ms - mapSpeed.y()*ms);
			body().incrX(xSpeed*scale*ms - mapSpeed.x()*ms);
		}
		
		void reIni(){
			body().moveX1(- quadBounds.width() + 2*RND.rInt((int)quadBounds.width()));
			body().moveY1(quadBounds.y2());
			getOpacity().set(RND.rInt(255));
		}
		
		
	}

	
}
