package world.regions.data;

import game.faction.Faction;
import util.data.INT_O;
import world.regions.Region;
import world.regions.data.RD.RDAddable;
import world.regions.data.RD.RDInit;

public class RData implements INT_O<Region>, RDAddable{
	
	public final CharSequence name;
	
	public RData(INT_OE<Region> plocal, RDInit init, CharSequence name){
		this.name = name;
		init.adders.add(this);
		this.plocal = plocal;
		ftotal = init.rCount.new DataInt();
	}
	
	protected final INT_OE<Region> plocal;
	private final INT_OE<Faction> ftotal;
	
	@Override
	public int get(Region t) {

		return plocal.get(t);
	}

	@Override
	public int min(Region t) {
		return 0;
	}

	@Override
	public int max(Region t) {
		return plocal.max(t);
	}

	public INT_O<Faction> faction() {
		return ftotal;
	}
	
	public static class RDataE extends RData implements INT_OE<Region> {

		public RDataE(INT_OE<Region> plocal, RDInit init, CharSequence name) {
			super(plocal, init, name);
		}
		
		@Override
		public void set(Region t, int i) {
			if (i != get(t)) {
				removeFromFaction(t);
				this.plocal.set(t, i);
				addToFaction(t);
			}
		}
	}

	@Override
	public void removeFromFaction(Region r) {
		if (r.faction() != null) {
			ftotal.inc(r.faction(), -plocal.get(r));
		}
	}

	@Override
	public void addToFaction(Region r) {
		if (r.faction() != null) {
			ftotal.inc(r.faction(), plocal.get(r));
		}
	}

}
