package settlement.room.infra.janitor;

import init.resources.RESOURCES;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.DataO;
import util.data.INT_O.INT_OE;

final class Res {

	final LIST<INT_OE<JanitorInstance>> resources; 
	final int intsize;
	
	Res(){
		
		DataO<JanitorInstance> da = new DataO<JanitorInstance>() {

			@Override
			protected int[] data(JanitorInstance t) {
				return t.resData;
			}
		
		};
		
		ArrayList<INT_OE<JanitorInstance>> resources = new ArrayList<>(RESOURCES.ALL().size());
		
		for (int i = 0; i < RESOURCES.ALL().size(); i++) {
			resources.add(da.new DataNibble());
		}
		intsize = da.intCount();
		this.resources = resources;
		
	}
}
