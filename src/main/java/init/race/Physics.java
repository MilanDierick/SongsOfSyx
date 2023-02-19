package init.race;

import init.C;
import snake2d.util.file.Json;

public final class Physics {

	private final transient double heightOverGround;
	private final transient int hitboxSize;
	public final int adultAt;
	public final boolean decays;
	public final boolean sleeps;
	
	Physics(Json json){
		json = json.json("PROPERTIES");

//		acceleration = json.d("ACCELERATION", 0, 1000)*C.TILE_SIZE;
//		topSpeed = json.d("TOP_SPEED", 1, 15)*C.TILE_SIZE;
		heightOverGround = json.i("HEIGHT", 0, 200);
		hitboxSize = json.i("WIDTH", 5, 15)*C.SCALE;
		adultAt = json.i("ADULT_AT_DAY");
		decays = json.bool("CORPSE_DECAY");
		sleeps = json.bool("SLEEPS");
	}

	
	public double height() {
		return heightOverGround;
	}
	
//	public double acceleration() {
//		return acceleration;
//	}
//	
//	public double topSpeed() {
//		return topSpeed;
//	}
	
	public int hitBoxsize() {
		return hitboxSize;
	}
	
}
