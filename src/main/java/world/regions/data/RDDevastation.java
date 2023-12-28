package world.regions.data;

import game.boosting.BSourceInfo;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.D;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import util.data.INT_O.INT_OE;
import world.regions.Region;
import world.regions.data.RD.RDInit;
import world.regions.data.RD.RDUpdatable;
import world.regions.data.RDOutput.RDResource;

public class RDDevastation {

	private static CharSequence ¤¤Name = "¤Devastation";
	private static CharSequence ¤¤Desc = "¤Devastation comes from military actions. Devastated regions produce less, and have slower population growth. Devastation takes 2 years to subside.";
	
	static {
		D.ts(RDDevastation.class);
	}
	
	private static final double dTime = 1.0/(TIME.secondsPerDay*32);
	public final INT_OE<Region> current;
	
	
	RDDevastation(RDInit init){
		current = init.count.new DataNibble(¤¤Name, ¤¤Desc);
		
		init.connectable.add(new ACTION() {
			
			@Override
			public void exe() {
				RBooster b = new RBooster(new BSourceInfo(¤¤Name, UI.icons().s.heat), 0.25, 1.0, true) {

					@Override
					public double get(Region t) {
						return 1.0 - current.getD(t);
					}
				
				};
				b.add(RD.RACES().capacity);
				for (RDResource o : RD.OUTPUT().all) {
					b.add(o.boost);
				}
				
			}
		});
		
		init.upers.add(new RDUpdatable() {
			
			@Override
			public void update(Region reg, double time) {
				current.incFraction(reg, -current.max(reg)*time*dTime);
			}
			
			@Override
			public void init(Region reg) {
				current.set(reg, 0);
			}
		});
		
	}
	
	public int raidCredits(Region reg) {
		double pop = RD.RACES().population.get(reg);
		double d = pop*RESOURCES.ALL().size();
		if (reg.faction() != null && reg.faction() instanceof FactionNPC) {
			FactionNPC f = (FactionNPC) reg.faction();
			d *= 1 + CLAMP.d(f.credits().credits()/RD.RACES().population.faction().get(f), 0, 100);
		}
		return (int) (d * (1.0-current.getD(reg)));
		
	}


	
}