package world.map.regions;

import java.io.IOException;

import game.GAME;
import game.faction.Faction;
import game.time.TIME;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import util.updating.IUpdater;
import world.World;
import world.entity.caravan.Shipment;
import world.map.regions.RegionTaxes.RegionResource;

final class RegionUpdater extends IUpdater{


	private final Regions regs;
	static final int shipmentsInteval = 2;
	private final short[] shuffled;
	
	private int minable;

	public RegionUpdater(Regions regs) {
		super(Regions.MAX, TIME.secondsPerDay/World.SPEED);
		this.regs = regs;
		shuffled = new short[Regions.MAX];
		for (int i = 0; i < shuffled.length; i++) {
			shuffled[i] = (short) i;
		}
		for (int i = 0; i < shuffled.length; i++) {
			int a = RND.rInt(shuffled.length);
			short s = shuffled[i];
			shuffled[i] = shuffled[a];
			shuffled[a] = s;
		}
	}

	@Override
	protected void update(int i, double timeSinceLast) {
		int in = shuffled[i];
		Region r = regs.getByIndex(in);
		
		
		
		
		if (r.area == 0)
			return;
		
		REGIOND.update(r, timeSinceLast);
		r.upI ++;
		if (r.upI >= shipmentsInteval){
			ship(r);
			r.upI = 0;
		}

	}
	
	private void ship(Region r) {
		
		Faction f = REGIOND.faction(r);
		
		if (f == null)
			return;
		
		if (r.besieged())
			return;
		
		int am = 0;
		
		if (f.capitolRegion() == null)
			return;
		
		if (f == GAME.player()) {
			
			
			for (RegionResource res : REGIOND.RES().res) {
				int a = res.current_output.get(r);
				am += a;
			}
			
			if (am <= 0)
				return;
			
			Shipment c = World.ENTITIES().caravans.create(r, f.capitolRegion());
			if (c != null) {
				for (RegionResource res : REGIOND.RES().res) {
					int a = res.current_output.get(r);
					if (a > 0) {
						c.load(res.resource, a*shipmentsInteval);
					}
				}
			}
			
			
		}else {
			int[] ams = World.REGIONS().outputter.getAmounts(f, r);
			for (int i = 0; i < REGIOND.RES().res.size(); i++) {
				int a = Math.min(f.buyer().spaceForTribute(REGIOND.RES().res.get(i).resource), ams[i]);
				am += a;
			}
		
			if (am <= 0)
				return;
			
			

			
			Shipment c = World.ENTITIES().caravans.create(r, f.capitolRegion());
			if (c != null) {
				for (int i = 0; i < REGIOND.RES().res.size(); i++) {
					c.load(REGIOND.RES().res.get(i).resource, ams[i]*shipmentsInteval);
				}
			}
		}

	}
	
	@Override
	public void save(FilePutter file) {
		file.i(minable);
		super.save(file);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		minable = file.i();
		super.load(file);
	}
	
	@Override
	public void clear() {
		minable = 0;
		super.clear();
	}

}
