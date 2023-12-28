package world.regions.data;

import game.boosting.BSourceInfo;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.D;
import init.sprite.UI.UI;
import snake2d.util.misc.ACTION;
import util.data.INT_O.INT_OE;
import world.regions.Region;
import world.regions.data.RD.RDInit;
import world.regions.data.RD.RDUpdatable;
import world.regions.data.pop.RDRace;

public class RDOwner implements RDUpdatable {

	private static CharSequence ¤¤Affiliation = "¤Support";
	private static CharSequence ¤¤AffiliationD = "¤Support towards your majesty. Low support increases the chance of rebellion. When a region is controlled, support will increase with time. For other regions, emissaries can be sent to increase support.";
	
	static {
		D.ts(RDOwner.class);
	}
	
	private static final double dTime = 1.0/(TIME.secondsPerDay*8);
	public final INT_OE<Region> affiliation;
	
	private final INT_OE<Region> prevOwner;
	private final INT_OE<Region> prevOwnerII;
	public final INT_OE<Region> ownerI;
	
	RDOwner(RDInit init){
		affiliation = init.count.new DataByte(¤¤Affiliation, ¤¤AffiliationD);
		prevOwner = init.count.new DataShort();
		prevOwnerII = init.count.new DataNibble();
		ownerI = init.count.new DataByte();
		
		init.upers.add(this);
		
		
		
		init.connectable.add(new ACTION() {
			
			@Override
			public void exe() {
				RBooster b = new RBooster(new BSourceInfo(¤¤Affiliation, UI.icons().s.happy), 0.0, -5, false) {

					@Override
					public double get(Region t) {
						if (t.faction() == FACTIONS.player())
							return 1.0-affiliation.getD(t);
						return 0;
					}
				
				};
				
				for (RDRace r : RD.RACES().all) {
					b.add(r.loyalty.target);
				}
			}
		});
		
	}

	@Override
	public void update(Region reg, double time) {
		int tar = 0;
		double d = -255.0*time*dTime;
		if (reg.faction() == FACTIONS.player()) {
			tar = 255;
			d = -d;
			affiliation.moveTo(reg, d, tar);
		}else
			affiliation.moveTo(reg, d*0.25, tar);
		
		if (prevOwner(reg) == null) {
			Faction ff = reg.faction();
			if (ff != null) {
				prevOwner.set(reg, ff.index()+1);
				if (ff instanceof FactionNPC)
					prevOwnerII.set(reg, ((FactionNPC) ff).iteration()&0x0F);
			}
			
		}
		
	}
	
	@Override
	public void init(Region reg) {
		// TODO Auto-generated method stub
		
	}
	
	public Faction prevOwner(Region reg) {
		int i = prevOwner.get(reg);
		if (i != 0) {
			Faction f = FACTIONS.getByIndex(i-1);
			if (f == null || !f.isActive() || (f instanceof FactionNPC && prevOwnerII.get(reg) != (((FactionNPC) f).iteration()&0x0F))) {
				prevOwner.set(reg, 0);
				return null;
			}
				
			return f;
		}
		return null;
	}


	
}