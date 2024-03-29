package world.regions.data.updating;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import snake2d.util.file.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import util.updating.IUpdater;
import world.WORLD;
import world.regions.Region;
import world.regions.WREGIONS;
import world.regions.data.RD.RDInit;
import world.regions.data.RD.RDUpdatable;

public final class RDUpdater {

	private final LIST<RDUpdatable> all;
	private float[] timers = new float[WREGIONS.MAX];

	private final Shipper shipper = new Shipper();
	private final Builder builder = new Builder();


	private final double upD = TIME.secondsPerDay/2;
	private final double ship = TIME.secondsPerDay;
	private final double build = TIME.secondsPerDay*2;
	
	public RDUpdater(RDInit init){
		
		this.all = init.upers;
		for (int i = 0; i < timers.length; i++) {
			timers[i] = (float) (RND.rFloat()*build);
		}
		
	}
	
	private final IUpdater uper = new IUpdater(WREGIONS.MAX, 8) {
		

		
		@Override
		protected void update(int i, double timeSinceLast) {
			Region r = WORLD.REGIONS().getByIndex(i);
			if (r != null && r.active()) {
				

				
				if (r.faction() == FACTIONS.player()) {
					timers[r.index()] += timeSinceLast;
					if (timers[r.index()] > ship) {
						shipper.ship(r, ship);
						timers[r.index()] -= ship;
					}
					
					for (RDUpdatable u : all)
						u.update(r, timeSinceLast);
				}else {
					float next = (float) (timers[r.index()] + timeSinceLast);
					if (timers[r.index()] < upD && next >= upD) {
						for (RDUpdatable u : all)
							u.update(r, upD);
					}
					if (next >= build) {
						builder.build(r);
						shipper.ship(r, build);
						next -= build;
						
					}	
					timers[r.index()] = next;
				}
				
			}
		}
	};
	
	public final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.fs(timers);
			uper.save(file);
		
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			file.fs(timers);
			uper.load(file);
		}
		
		@Override
		public void clear() {

		}
	};


	public void update(float ds) {
		uper.update(ds);
	}
	
	public void BUILD(Region reg) {
		this.builder.build(reg);
	}
	
	public void BUILD(Region reg, RealmBuilder bu) {
		this.builder.build(reg, bu);
	}
	
	public void init(Region reg) {
		this.builder.build(reg);
	}
	
	public void ship(Region reg, double days) {
		shipper.ship(reg, days*TIME.secondsPerDay);
	}
	
	public void shipAll(Faction f, double days) {
		shipper.shipAll(f, days);
	}
	
}
