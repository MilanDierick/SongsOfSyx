package init.resources;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public final class ArmySupplies {

	private final ArrayList<ArmySupply> all;
	private final ArmySupply[] resource = new ArmySupply[RESOURCES.ALL().size()];
	
	ArmySupplies() {
		
		PATH p = PATHS.INIT().getFolder("resource").getFolder("armySupply");
		String[] keys = p.getFiles();
		all = new ArrayList<>(keys.length);
		for (String k : keys) {
			Json j = new Json(p.get(k));
			ArmySupply s = new ArmySupply(k, j, all);
			if (resource[s.resource.index()] != null)
				j.error("Army supply: " +resource[s.resource.index()].key + " refers to the same resource: " + s.resource.key, k);
		}
		
	}
	
	public LIST<ArmySupply> ALL(){
		return all;
	}
	
	public ArmySupply get(RESOURCE res) {
		return resource[res.index()];
	}
	
}
