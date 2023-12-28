package world.army;

import java.util.LinkedList;

import game.faction.Faction;
import util.data.DataOL;
import world.army.AD.Imp;
import world.entity.army.WArmy;

class ADInit {

	public final DataOL<WArmy> dataA = new DataOL<WArmy>() {
		@Override
		protected long[] data(WArmy t) {
			return t.divs().data;
		}		
	};
	public final  DataOL<Faction> dataT = new DataOL<Faction>() {
		@Override
		protected long[] data(Faction t) {
			return t.armies().data;
		}
	};
	
	public final  LinkedList<Imp> imps = new LinkedList<>();
	
	ADInit(){
		
	}
	
}
