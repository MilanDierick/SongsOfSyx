package world.regions.data;

import game.boosting.*;
import game.time.TIME;
import init.religion.Religion;
import init.religion.Religions;
import init.sprite.UI.UI;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.ACTION.ACTION_O;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.data.INT_O.INT_OE;
import world.regions.Region;
import world.regions.data.RD.RDInit;
import world.regions.data.RD.RDUpdatable;
import world.regions.data.pop.RDRace;

public class RDReligions {

	private final ArrayListGrower<RDReligion> all = new ArrayListGrower<>();
	public final INT_OE<Region> opposition;
	private static CharSequence ¤¤Opposition = "Religious differences";
	
	RDReligions(RDInit init){
		
		opposition = init.count.new DataByte();
		
		for (Religion r : Religions.ALL()) {
			all.add(new RDReligion(init, r));
		}
		
		init.connectable.add(new ACTION() {
			
			@Override
			public void exe() {

				
				RBooster lo = new RBooster(new BSourceInfo(¤¤Opposition, UI.icons().s.heat), 0.8, 1.0, true) {
					
					@Override
					public double get(Region t) {
						return opposition.getD(t);
					}
				};
				
				for (RDRace race : RD.RACES().all) {
					lo.add(race.loyalty.target);
				}
				
			}
		});
		
		init.upers.add(new RDUpdatable() {
			
			final double dt = 1.0/(TIME.secondsPerDay*16);
			
			@Override
			public void update(Region reg, double time) {
				
				double min = min(reg);
				double tot = tot(reg)-min;
				
				for (RDReligion r : all) {
					double target = Math.round(0x0FF*(r.religion.boostable.get(reg)-min)/tot);
					double now = target + dt*(target-r.current.get(reg));
					now = CLAMP.d(now, Math.min(target, r.current.get(reg)), Math.max(target, r.current.get(reg)));
					r.current.set(reg, (int) now);
				}
				
				setop(reg);
				
			}
			
			@Override
			public void init(Region reg) {
				double min = min(reg);
				double tot = tot(reg)-min;
				for (RDReligion r : all) {
					double target = Math.round(0x0FF*(r.religion.boostable.get(reg)-min)/tot);
					r.current.set(reg, (int) target);
				}
				setop(reg);
			}
			

			
			private void setop(Region reg) {
				double op = 0;
				
				for (int ri = 0; ri < all.size(); ri++) {
					double vv = 0;
					RDReligion r = all.get(ri);
					for (int ri2 = 0; ri2 < all.size(); ri2++) {
						RDReligion r2 = all.get(ri2);
						double am = r2.current.getD(reg);
						am *= r.religion.opposition(r2.religion);
						vv += am;
					}
					op += vv*r.current.getD(reg);
				}
				
				op = CLAMP.d(op, 0, 1);
				opposition.setD(reg, op);
			}
		});
		
		init.gens.add(new ACTION_O<Region>() {
			
			@Override
			public void exe(Region reg) {
				double min = min(reg);
				double tot = tot(reg)-min;
				
				
				for (RDReligion r : all) {
					double target = Math.round(0x0FF*(r.religion.boostable.get(reg)-min)/tot);
					r.current.set(reg, (int)target);
				}
			}
		});
		
	}
	
	private double min(Region reg) {
		double mi = 0;
		for (RDReligion r : all) {
			mi = Math.min(mi, r.religion.boostable.get(reg));
		}
		return mi;
	}
	
	private double tot(Region reg) {
		double tot = 0;
		for (RDReligion r : all) {
			tot += Math.max(r.religion.boostable.get(reg), 0);
		}
		return tot;
	}
	
	public LIST<RDReligion> all(){
		return all;
	}
	
	public RDReligion get(Religion t) {
		return all.get(t.index());
	}
	
	
	public class RDReligion {
		
		public final BoostSpecs boosts;
		public final Religion religion;
		public final INT_OE<Region> current;
		
		private RDReligion(RDInit init, Religion reg) {
			boosts = new BoostSpecs(reg.info.name, reg.icon, true);
			religion =  reg;
			current = init.count.new DataByte();
			
			BOOSTING.connecter(new ACTION() {
				
				@Override
				public void exe() {
					
					
					for (BoostSpec s : reg.bworld.all()) {
						boosts.push(s.booster, s.boostable);
					}
					
					
				}
			});
		}
		
		public double target(Region reg) {
			double tot = 0;
			for (RDReligion r : all) {
				tot += r.religion.boostable.get(reg);
			}
			return religion.boostable.get(reg)/tot;
		}
		
	}



	
	
}
