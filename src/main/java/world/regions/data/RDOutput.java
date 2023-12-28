package world.regions.data;

import game.boosting.*;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.dic.DicMisc;
import world.regions.Region;
import world.regions.data.RD.RDInit;

public final class RDOutput {

	public final LIST<RDResource> all;
	
	public RDOutput(RDInit init) {

		ArrayList<RDResource> all = new ArrayList<>(RESOURCES.ALL().size());
		
		for (RESOURCE res : RESOURCES.ALL()) {
			all.add(new RDResource(init, res));
		}
		this.all = all;
	}
	

	public static class RDResource {
		
		public final Boostable boost;
		public final RESOURCE res;
		
		RDResource(RDInit init, RESOURCE res) {
			boost = BOOSTING.push("RESOURCE_PRODUCTION_" + res.key, 0, DicMisc.¤¤Production + ": " + res.names, res.desc, res.icon(), BoostableCat.WORLD_PRODUCTION);
			this.res = res;
			
		}

		public int getDelivery(Region reg) {
			return (int) boost.get(reg);
		}
		
	}
	
	public RDResource get(RESOURCE res){
		return all.get(res.index());
	}
	
	public RESOURCE fromBoost(Boostable bo) {
		if (bo.index() >= all.get(0).boost.index() && bo.index() < all.get(all.size()-1).boost.index()) {
			return RESOURCES.ALL().get(bo.index()-all.get(0).boost.index());
		}
		return null;
	}
	
	
	
}
