package settlement.room.military.supply;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.faction.FACTIONS;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.thing.halfEntity.caravan.Caravan;
import settlement.thing.halfEntity.caravan.CaravanPickup;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.updating.IUpdater;
import world.army.WARMYD;
import world.army.WARMYD.WArmySupply;
import world.entity.army.WArmy;

final class SupplyTally implements SAVABLE{

	
	private int[] amounts = new int[RESOURCES.ALL().size()];
	private int[] reserved = new int[RESOURCES.ALL().size()];
	private final IUpdater updater = new IUpdater(8, TIME.secondsPerDay/4) {
		
		@Override
		protected void update(int q, double timeSinceLast) {
			if (SETT.ENTRY().isClosed())
				return;
			
			int di = RND.rInt(WARMYD.supplies().all.size());
			for (int i = 0; i < WARMYD.supplies().all.size(); i++) {
				updateSuppy(WARMYD.supplies().all.get((di+i)%WARMYD.supplies().all.size()));
			}
			
		}
	};
	private final ROOM_SUPPLY b;
	
	SupplyTally(ROOM_SUPPLY b) {
		this.b = b;
	}
	
	@Override
	public void save(FilePutter file) {
		file.is(amounts);
		file.is(reserved);
		updater.save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.is(amounts);
		file.is(reserved);
		updater.load(file);
	}

	@Override
	public void clear() {
		Arrays.fill(amounts, 0);
		Arrays.fill(reserved, 0);
		updater.clear();
	}
	
	void update(double ds) {
		updater.update(ds);
	}

	
	private void updateSuppy(WArmySupply r) {
		int needed = 0;
		for (WArmy e : FACTIONS.player().kingdom().armies().all()) {
			if (e.acceptsSupplies()) {
				needed += r.needed(e);
			}
		}
		
		int available = amount(r.res) - reserved(r.res);
		
		if (available <= 0)
			return;
		if (needed <= 0)
			return;
		double d = CLAMP.d((double)available/needed, 0, 1);
		for (WArmy e : FACTIONS.player().kingdom().armies().all()) {
			if (e.acceptsSupplies()) {
				int am = (int) Math.ceil(r.needed(e)*d);
				am = CLAMP.i(am, 0, available);
				am = CLAMP.i(am, 0, Caravan.MAX_LOAD);
				if (am > 0 && SETT.HALFENTS().caravans.createSupply(r.res, am)) {
					available -= am;
					r.current().inc(e, am);
				}
					
			}
		}
		
	}
	
	void count(RESOURCE res, int amount, int reserved) {
		if (res == null)
			return;
		int i = res.index();
		amounts[i] += amount;
		this.reserved[i] += reserved;
	}
	
	int amount(RESOURCE res) {
		return amounts[res.index()];
	}
	

	int reserved(RESOURCE res) {
		return reserved[res.index()];
	}
	
	CaravanPickup reserved(int tx, int ty, RESOURCE res) {
		
		Crate ss = b.crate.get(tx, ty);
		if (ss == null || ss.resource() != res || ss.pickup.reserved() <= 0)
			return null;
		return ss.pickup;		
	}
	
	CaravanPickup reservable(int tx, int ty, RESOURCE res) {
		
//		if (WARMYD.supplies().get(res).needed(FACTIONS.player()) <= 0)
//			return null;
		
		if (amount(res) - reserved(res) <= 0)
			return null;
		
		SupplyInstance ins = b.getter.get(tx, ty);
		if (ins != null && ins.resource() == res && ins.amount() > ins.reserved()) {
			for (COORDINATE c : ins.body()) {
				if (!ins.is(c))
					continue;
				Crate ss = b.crate.get(c.x(), c.y());
				if (ss != null && ss.pickup.reservable() > 0) {
					return ss.pickup;
				}
				
			}
			GAME.Notify("Weird food!!!");
		}
			
		
		int di = RND.rInt(b.instancesSize());
		for (int i = 0; i < b.instancesSize(); i++) {
			int ri = (di+i)%b.instancesSize();
			ins = b.getInstance(ri);
			if (ins.resource() != res || ins.amount() <= ins.reserved())
				continue;
			for (COORDINATE c : ins.body()) {
				if (!ins.is(c))
					continue;
				Crate ss = b.crate.get(c.x(), c.y());
				if (ss != null && ss.pickup.reservable() > 0) {
					return ss.pickup;
				}
				
			}
			GAME.Notify("Weird " + res.name);
		}
		GAME.Notify("Weird food " + res.name + " " + amount(res) + " " + reserved(res));
		return null;
		
	}

	public void withdraw(RESOURCE res, int amount) {
		
		if (amount(res) - reserved(res) <= 0)
			return;
		
		int di = RND.rInt(b.instancesSize());
		for (int i = 0; i < b.instancesSize(); i++) {
			int ri = (di+i)%b.instancesSize();
			SupplyInstance ins = b.getInstance(ri);
			if (ins.resource() != res || ins.amount() <= ins.reserved())
				continue;
			for (COORDINATE c : ins.body()) {
				if (!ins.is(c))
					continue;
				Crate ss = b.crate.get(c.x(), c.y());
				if (ss != null && ss.pickup.reservable() > 0) {
					int am = CLAMP.i(ss.pickup.reservable(), 0, amount);
					ss.pickup.reserve(am);
					ss.pickup.pickup(am);
					amount -= am;
					if (amount <= 0)
						return;
					if (amount(res) - reserved(res) <= 0)
						return;
				}
				
			}
			GAME.Notify("Weird " + res.name);
		}
		GAME.Notify("Weird food " + res.name + " " + amount(res) + " " + reserved(res));
		
	}


}
