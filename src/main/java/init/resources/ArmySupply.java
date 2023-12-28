package init.resources;

import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;

public final class ArmySupply implements INDEXED{

	public final RESOURCE resource;
	public final double morale;
	public final double health;
	public final double consumption_day;
	public final int minimum;
	private final int index;
	final String key;
	public final boolean mandatory;
	
	ArmySupply(String key, Json json, ArrayList<ArmySupply> all){
		this.key = key;
		this.index = all.add(this);
		resource = RESOURCES.map().get(json);
		mandatory = json.bool("MANDATORY", false);
		morale = json.d("MORALE_EFFECT", 0, 1);
		health = json.d("HEALTH_EFFECT", 0, 1);
		consumption_day = json.d("CONSUMPTION_RATE_DAILY", 0, 1000);
		minimum = json.i("MINIMUM", 1, 10000);
	}

	@Override
	public int index() {
		return index;
	}
	
}
