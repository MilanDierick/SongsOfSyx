package settlement.room.infra.importt;

import static settlement.main.SETT.*;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.faction.trade.ITYPE;
import init.RES;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.misc.util.RESOURCE_TILE;
import settlement.thing.halfEntity.caravan.Caravan;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import util.statistics.IntResource;

public final class ImportThingy {

	final static Bits bAmount 			= new Bits(0x0000FF);
	final static Bits bReserved 		= new Bits(0x00FF00);
	final static Bits bReservedSpace 	= new Bits(0xFF0000);
	private final Resource tile = new Resource();
	private final ROOM_IMPORT b;
	private final ImportTally tally;
	private int data;
	private int tx,ty;
	private ImportInstance ins;
	
	ImportThingy(ROOM_IMPORT imp, ImportTally tally){
		this.b = imp;
		this.tally = tally;
	}
	
	void update(float ds) {
		
		if (SETT.ENTRY().isClosed())
			return;
		
		RESOURCE r = RESOURCES.ALL().rnd();
		for (ITYPE t : ITYPE.all) {
			
			update(r, tally.iAll[t.index], t.rtype);
		}
		
	}
	
	private void update(RESOURCE r, IntResource ir, RTYPE in) {
		
		int amount = ir.get(r);
		
		if(amount > 0) {
			
			int am = amount;
			if (am > Caravan.MAX_LOAD)
				am = Caravan.MAX_LOAD;
			if (SETT.HALFENTS().caravans.createDelivery(r, am, true)) {
				if (in != null)
					FACTIONS.player().res().inc(r, in, am);
				ir.set(r, (int) (amount-am));
			}else {
				ir.set(r, (int) (amount*0.5));
			}
		}
		
		
	}
	
	RESOURCE_TILE resourceTile(int tx, int ty) {
		ImportInstance ins = b.getter.get(tx, ty);
		if (ins == null)
			return null;
		if (ins.resource() == null)
			return null;
	
		if (!b.constructor.isCrate(tx, ty))
			return null;
		data = ROOMS().data.get(tx, ty);
		this.ins = ins;
		this.tx = tx;
		this.ty = ty;
		return tile;
	}
	
	void clear(int tx, int ty) {
		if (resourceTile(tx, ty) == null)
			return;
		if (ins.resource() != null) {
			int am = bAmount.get(data);
			data = 0;
			save();
			if (am <= 0)
				return;
			for (DIR d : DIR.ORTHO) {
				if (!PATH().solidity.is(tx, ty, d)) {
					THINGS().resources.createPrecise(tx+d.x(), ty+d.y(), ins.resource(), am);
					break;
				}	
			}
		}
		
	}
	
	public COORDINATE getReservableSpot(int sx, int sy, RESOURCE res) {

		if (tally.capacity.get(res)-(tally.amount.get(res)) < 0) {
			return null;
		}
			
		
		ImportInstance ins = b.get(sx, sy);
		if (ins == null || ins.resource() != res || reservable(ins) <= 0) {
			ins = null;
			if (b.all().size() == 0)
				return null;
			int r = RND.rInt(b.all().size());
			for (int i = 0; i < b.all().size(); i++) {
				ImportInstance ins2 = b.all().get((i+r)%b.all().size());
				if (ins2.resource() == res && reservable(ins2) > 0) {
					ins = ins2;
					break;
				}
			}
		}
		
		if (ins != null) {
			return getReservableSpot(ins, sx, sy, res);
		}
		return null;
	}
	
	private COORDINATE getReservableSpot(ImportInstance i, int sx, int sy, RESOURCE res) {
		if (!i.is(sx, sy)) {
			sx = i.mX();
			sy = i.mY();
		}
		RES.filler().init(this);
		RES.filler().filler.set(sx, sy);
		DIR dir = DIR.ORTHO.rnd();
		int q = 0;
		while(RES.filler().hasMore()) {
			COORDINATE c = RES.filler().poll();
			if (reservable(res, c) > 0) {
				RES.filler().done();
				return c;
			}
			q++;
			DIR d = dir;
			for (int k = 0; k < DIR.ORTHO.size(); k++) {
				if (i.is(c, d))
					RES.filler().fill(c, d);
				d = d.next(2);
			}
			
		}
		RES.filler().done();
		GAME.Notify("oh no " + res + " " + q + " " + i.area() + " " + i.mX() + " " + i.mY() + " " + reservable(ins));
		return null;
	}
	
	private int reservable(ImportInstance ins) {
		int space = ins.allocated;
		space -= ins.amount;
		space -= ins.spaceReserved;
		return space;
	}
	
	public int reservable(RESOURCE r, COORDINATE c) {
		if (resourceTile(c.x(), c.y()) == null)
			return 0;
		if (this.tile.resource() != r)
			return 0;
		int am = ImportInstance.crateMax - (bAmount.get(data)+bReservedSpace.get(data));
		return am;
		
	}

	public void reserve(RESOURCE r, COORDINATE c, int amount) {
		if (resourceTile(c.x(), c.y()) == null)
			throw new RuntimeException();
		if (this.tile.resource() != r)
			throw new RuntimeException();
		data = bReservedSpace.inc(ROOMS().data.get(c), amount);
		save();
	}
	
	public int reserved(RESOURCE r, COORDINATE c) {
		if (resourceTile(c.x(), c.y()) == null)
			return 0;
		if (this.tile.resource() != r)
			return 0;
		return bReservedSpace.get(data);
	}
	
	public void finish(RESOURCE r, COORDINATE c, int amount) {
		reserve(r, c, -amount);
		
		data = bAmount.inc(data, amount);
		tally.delivering.inc(r, -amount);
		save();
	}
	
	public void cancel(RESOURCE r, int amount) {
		tally.delivering.inc(r, -amount);
	}
	
	public void initCaravan(RESOURCE r, int attempting) {
		tally.delivering.inc(r, attempting);
	}
	
	private void save() {
		
		
		int current = data;
		data = ROOMS().data.get(tx, ty);
		ins.count(-bAmount.get(data), -bReservedSpace.get(data));
		boolean was = tile.findableReservedCanBe();
		data = current;
		ROOMS().data.set(ins, tx, ty, data);
		if (was) {
			if (!tile.findableReservedCanBe()) {
				PATH().finders.resource.reportAbsence(tile);
			}
		}else {
			if (tile.findableReservedCanBe())
				PATH().finders.resource.reportPresence(tile);
		}
		ins.count(bAmount.get(data), bReservedSpace.get(data));

	}

	
	private class Resource implements RESOURCE_TILE{
		
		@Override
		public boolean findableReservedCanBe() {
			return bAmount.get(data) > bReserved.get(data);
		}
		
		@Override
		public void findableReserve() {
			if (!findableReservedCanBe())
				throw new RuntimeException();
			data = bReserved.inc(data, 1);
			save();
		}
		
		@Override
		public boolean findableReservedIs() {
			return bReserved.get(data) > 0;
		}
		@Override
		public void findableReserveCancel() {
			if (findableReservedIs()) {
				data = bReserved.inc(data, -1);
				save();
			}
				
		}
		@Override
		public int x() {
			return tx;
		}
		@Override
		public int y() {
			return ty;
		}
		@Override
		public RESOURCE resource() {
			return ins.resource();
		}
		@Override
		public void resourcePickup() {
			findableReserveCancel();
			if (bAmount.get(data) > 0)
				data = bAmount.inc(data, -1);
			save();
		}

		@Override
		public int reservable() {
			return bAmount.get(data)-bReserved.get(data);
		}

		@Override
		public int amount() {
			return bAmount.get(data);
		}
		
		@Override
		public double spoilRate() {
			return 0.5;
		}
		
	}
	
}
