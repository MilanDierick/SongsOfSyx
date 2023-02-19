package game;

import java.io.IOException;

import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import view.keyboard.KEYS;

public final class GameSpeed{

	private boolean tmpPaused;
	private int speed;
	private int prevSpeed;
	private int actualSpeed;
	private double actualSpeedI;
	private boolean updateOnce;
	
	GameSpeed(){
		
	}
	
	double update(double slowTheFuckDown) {
		if (tmpPaused)
			return clearAndReturn(0);
		
		if (updateOnce)
			return clearAndReturn(1);
		
		int s = speed;
		if (SETT.ARMIES().enemy().men() > 0)
			s = CLAMP.i(s, 0, 25);
		
		if (actualSpeed < s && slowTheFuckDown < 1) {
			actualSpeed++;
		}else if (slowTheFuckDown >= 1){
			actualSpeed /= 1.0 + (slowTheFuckDown-1.0)*0.5;
		}
		
		actualSpeed = CLAMP.i(actualSpeed, 1, s);
		if (actualSpeed > s)
			actualSpeed = s;
		if (actualSpeed < 1 && s >= 1)
			actualSpeed = 1;
		
		actualSpeedI = 1.0/CLAMP.d(actualSpeed, 1, 1000);
		
		return clearAndReturn(actualSpeed);
		
	}
	
	private int clearAndReturn(int i) {
		tmpPaused = false;
		updateOnce = false;
		return i;
	}
	
	public boolean isPaused() {
		return speed == 0 || tmpPaused;
	}
	
	public int speedTarget() {
		return tmpPaused ? 0 : speed;
	}
	
	public void updateOnce(){
		updateOnce = true;
	}
	
	public void tmpPause() {
		tmpPaused = true;
	}
	
	public void togglePause() {
		if (speed == 0) {
			speed = prevSpeed;
			
		}
			
		else{
			prevSpeed = speed;
			speed = 0;
		}
		actualSpeed = speed;
		actualSpeedI = 1.0/CLAMP.d(actualSpeed, 1, 1000);
	}
	
	public int speed() {
		return actualSpeed;
	}
	
	public double speedI() {
		return actualSpeedI;
	}
	
	public void speedSet(int speed) {
		this.prevSpeed = this.speed;
		this.speed = speed;
		this.actualSpeed = speed;
		actualSpeedI = 1.0/CLAMP.d(actualSpeed, 1, 1000);
	}

	void save(FilePutter file) {
		file.i(speed);
	}

	void load(FileGetter file) throws IOException {
		prevSpeed = file.i();
		clear();
		
	}
	
	void clear() {
		speed = 0;
		actualSpeed = 0;
		tmpPaused = false;
		updateOnce = false;
	}
	
	public void poll() {
		if (KEYS.MAIN().SPEED0.consumeClick()) {
			speedSet(0);
		}
		if(KEYS.MAIN().SPEED1.consumeClick()) {
			speedSet(1);
		}
		if(KEYS.MAIN().SPEED2.consumeClick()) {
			speedSet(3);
		}
		if(KEYS.MAIN().SPEED3.consumeClick()) {
			if (speed == 25)
				speedSet(200);
			else
				speedSet(25);
		}
		if (KEYS.MAIN().PAUSE.consumeClick()){
			togglePause();
		}
	}
	
}
