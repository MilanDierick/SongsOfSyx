package game.time;

import init.settings.S;
import snake2d.util.misc.CLAMP;

public final class LightShadows {

	
	private double dir;
	private double sx,sy;
	private double sLength;
	
	LightShadows(){
		
		
	}
	
	void update(Light light) {
		
		dir = (360+90) - TIME.days().bitPartOf()*360;
		dir %= 360;
		if (S.get().lightCycle.get() == 0)
			dir = 180;

	}
	
	double dir() {
		return dir;
	}
	
	double dirNight() {
		return (100+dir)%360;
	}
	
	public double sx() {
		return sx;
	}
	
	public double sy() {
		return sy;
	}
	
	void setShadowDay(double tilt) {
		setShadow(tilt, dir);
	}
	
	void setShadowNight(double tilt) {
		setShadow(tilt, dirNight());
	}
	
	private void setShadow(double tilt, double dir) {

		
		
		sLength = 0.5 + 2 - 2*Math.pow(CLAMP.d(tilt/40, 0, 1), 1);
		double ra = Math.toRadians(dir);
		sLength = (1+S.get().shadows.getD())*sLength;
		
		sx = -sLength * Math.cos(ra);
		sy = -sLength * Math.sin(ra);
	}
	
}
