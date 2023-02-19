package menu;

import init.C;
import init.D;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.light.AmbientLight;
import snake2d.util.sprite.SPRITE;

final class Intro{

	private double timer = 0;
	private int stage = 0;
	private AmbientLight moon = new AmbientLight();
	{
		D.gInit(this);
	}
	private HOVERABLE head = GUI.getBigText(D.g("greeting", "HAIL MIGHTY DESPOT!"));
	
	private SPRITE[] greeting = new SPRITE[] {
			GUI.getSmallText(D.g("1", "This is a EA and not a complete game, nor a complete demo.")),
			GUI.getSmallText(D.g("2", "That means a lot of visual placeholders and unfinished, missing or broken features")),
			GUI.getSmallText(D.g("3", "Feedback is much welcome!")),
	};
	private final ScMain main;
	private final Background bg;
	
	Intro(ScMain main, Background bg){
		moon.Set(AmbientLight.Strongmoonlight, 0);
		this.main = main;
		this.bg = bg;
	}
	
	boolean update(float ds) {
		
		timer += ds;
		
		switch(stage) {
		case 0 :
			moon.Set(AmbientLight.Strongmoonlight, timer/1.5);
			if (timer > 1.5) {
				moon.Set(AmbientLight.Strongmoonlight, 1.0);
				timer = 0;
				stage++;
			}
			break;
		case 1 :
			if (timer > 15) {
				timer = 0;
				stage++;
			}
			break;
		case 2 :
			moon.Set(AmbientLight.Strongmoonlight, 1.0-timer*2.0);
			if (timer > 0.5) {
				moon.Set(AmbientLight.Strongmoonlight, 0);
				timer = 0;
				stage++;
			}
			break;
		case 3 :
			if (timer > 0.5) {
				timer = 0;
				stage++;
			}
			break;
		case 4 :
			moon.Set(AmbientLight.Strongmoonlight, timer/2.0);
			if (timer > 1.0) {
				mask.setRed((int) ((timer-1)*128));
				mask.setGreen((int) ((timer-1)*128));
				mask.setBlue((int) ((timer-1)*128));
			}
			
			if (timer > 2.0) {
				moon.Set(AmbientLight.Strongmoonlight, 1.0);
				timer = 0;
				stage++;
			}
			break;
		case 5 :
			return false;
			
		}
		
		
		return true;
		
	}

	private final ColorImp mask = new ColorImp(COLOR.BLACK);
	
	protected void render(Renderer r, float ds) {
		
		if (stage >= 0 && stage < 3) {
			int y = GUI.inner.cY()-100;
			moon.register(C.DIM());
			head.body().moveY1(y);
			head.body().centerX(C.DIM());
			head.render(r, ds);
			
			y+= head.body().height()*2;
			for (SPRITE s : greeting) {
				int x1 = (C.WIDTH() - s.width())/2;
				s.render(r, x1, y);
				y+= s.height();
			}
			
		}
		
		
		
		if(stage > 3) {
			moon.register(C.DIM());
			main.render(r, ds);
		}
		
		if (stage >= 4) {
			r.newLayer(false, 0);
			mask.bind();
			bg.render(r, ds);
		}
		
		
	}

}
