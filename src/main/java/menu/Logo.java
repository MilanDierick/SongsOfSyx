package menu;

import init.C;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.Pendulum;
import snake2d.util.light.AmbientLight;
import snake2d.util.light.PointLight;
import snake2d.util.rnd.RND;

final class Logo{

	private final int startY;
	private final int startX;
	private int letterI = 1;
	private double letterTimer = 0;
	private final int presX1;
	private final int presY1;
	private boolean presents = false;
	private double presentsTimer = 0;
	private final int flashY1;
	private boolean flashRetreat = false;
	private final static double letterMax = 0.06;
	private double wait = 0.0;
	private final AmbientLight light = new AmbientLight(1.2,1.2,1.2,45,45);
	private final Pendulum lightD = new Pendulum().setZero(0.3).setFactor(1 + RND.rFloat1(0.3));
	private final PointLight finish = new PointLight();
	
	Logo(){
		
		int w = 0;
		for (int i = 0; i < RESOURCES.s().logoGlyps.length; i++) {
			w+= RESOURCES.s().logoGlyps[i].width();
		}
		
		int m = 10;
		
		startX = (C.WIDTH() - w)/2;		
		startY = (C.HEIGHT() - (RESOURCES.s().logoGlyps[0].height() + m + RESOURCES.s().logoPresents.height()))/2;
		
		flashY1 = startY - (RESOURCES.s().logoFlash.height() - RESOURCES.s().logoGlyps[0].height())/2;
		
		presX1 = (C.WIDTH() - RESOURCES.s().logoPresents.width())/2;
		presY1 = startY + m + RESOURCES.s().logoGlyps[0].height();
		finish.setRadius(RESOURCES.s().logoGlyps[0].height());
		finish.setZ(40);
		finish.setRed(10);
		
	}
	
	private double finTimer = 0;
	
	boolean update(float ds) {
		
		
		if (d > 0) {
			d-= ds;
			if (d == 0)
				d = -1;
			return true;
		}
			
		
		if (d != 0)
			RESOURCES.sound().logo.play();
		d= 0;
		
		if (letterI < RESOURCES.s().logoGlyps.length) {
			letterTimer += ds;
			if (letterTimer >= letterMax) {
				letterTimer = 0;
				if (flashRetreat) {
					letterI++;
				}
				flashRetreat = !flashRetreat;
			}
			
		}else if (wait > 0) {
			wait -= ds;
		}else if (finTimer < 0.5){
			finTimer += ds;
			
		}else {
			presents = true;
			presentsTimer += ds;
			finTimer += ds;
			if (presentsTimer > 3)
				return false;
		}
		
		return true;
	
	}

	private final ColorImp co = new ColorImp();
	
	private double d = 0.25;
	
	protected void render(Renderer r, float ds) {
	
		if (d > 0)
			return;
		
		
		int x1 = startX;
		for (int i = 0; i < letterI; i++) {
			x1 += RESOURCES.s().logoGlyps[i].width();
		}
		
		light.r(1.2*RND.rFloat1(0.05));
		light.g(1.2*RND.rFloat1(0.05));
		light.b(1.2*RND.rFloat1(0.05));
		light.setDir(45 + RND.rFloat0(15));
		
		
		if (flashRetreat || letterI < RESOURCES.s().logoGlyps.length) {
			double op;
			if (flashRetreat)
				op =  (255-255.0*letterTimer/letterMax);
			else
				op = (255*letterTimer/letterMax);

			int q = (int) op/2;
			co.setRed(q).setGreen(q).setBlue(q);
			co.bind();
			
			lightD.update(ds);
			light.r(lightD.get() + 0.9);
			light.g(lightD.get() + 0.9);
			light.b(lightD.get() + 0.9);
			
			x1 = x1 - (RESOURCES.s().logoFlash.width()-RESOURCES.s().logoGlyps[letterI].width())/2;
			RESOURCES.s().logoFlash.render(r, x1, flashY1);
			COLOR.unbind();
		}
		light.register(C.DIM());
		x1 = startX;
		for (int i = 0; i < letterI; i++) {
			RESOURCES.s().logoColors[i].bind();
			RESOURCES.s().logoGlyps[i].render(r, x1, startY);
			x1 += RESOURCES.s().logoGlyps[i].width();
		}
		COLOR.unbind();
		
		
		
		if (presents) {
			RESOURCES.s().logoPresents.render(r, presX1, presY1);
		}
		
		if (finTimer > 0) {
			
			int xl = (int) (C.MIN_WIDTH*finTimer + (C.WIDTH()-C.MIN_WIDTH)/2);
			finish.set(xl, C.HEIGHT()/2);
			finish.register();
		}
		
		
	}

}
