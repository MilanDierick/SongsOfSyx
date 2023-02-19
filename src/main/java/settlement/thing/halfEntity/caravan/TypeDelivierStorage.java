package settlement.thing.halfEntity.caravan;

import static settlement.main.SETT.*;

import init.D;
import settlement.main.SETT;
import settlement.misc.util.TILE_STORAGE;
import settlement.room.infra.importt.ImportThingy;
import settlement.room.main.throne.THRONE;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import util.gui.misc.GBox;

class TypeDelivierStorage extends Type{

	static CharSequence ¤¤name = "¤delivering";
	static {
		D.ts(TypeDelivierStorage.class);
	}
	TypeDelivierStorage() {
		super(¤¤name);
	}

	@Override
	public boolean init(Caravan c, int amount) {
		c.amountCarried = (short) amount;
		c.reservedGlobally = (short) amount;
		ROOMS().IMPORT.UNLOADER.initCaravan(c.res, c.reservedGlobally);
		return find(c);
	}
	
	private boolean find(Caravan c) {
		c.reserved = 0;
		COORDINATE coo = SETT.PATH().finders().storage.reserve(c.ctx(), c.cty(), c.res, Integer.MAX_VALUE);
		if (coo != null) {
			c.reserved = 1;
			TILE_STORAGE s = SETT.PATH().finders().storage.getter.get(coo);
			int extra = CLAMP.i(c.amountCarried-c.reserved, 0, s.storageReservable());
			s.storageReserve(extra);
			c.reserved += extra;
			if (c.path.request(c.ctx(), c.cty(), coo.x(), coo.y(), false)) {
				c.move();
				return true;
			}
			return false;
		}else {
			
			c.reserved = c.amountCarried;
			coo = SETT.PATH().finders.rndCoo.find(THRONE.coo().x(), THRONE.coo().y(), 8);
			c.path.request(c.ctx(), c.cty(), coo.x(), coo.y(), false);
			
			if (c.path.isSuccessful()) {
				c.move();
				return true;
			}
			return false;
		}
	}
	
	private void pickup(Caravan c) {
		TILE_STORAGE s = SETT.PATH().finders().storage.getter.get(c.path.destX(), c.path.destY());
		if (s != null && s.storageReserved() > 0) {
			s.storageDeposit(1);
			c.reserved --;
			c.amountCarried--;
		}else {
			SETT.THINGS().resources.create(c.path.destX(), c.path.destY(), c.res, 1);
			c.amountCarried -= 1;
			c.reserved--;
		}
	}

	@Override
	public boolean update(Caravan c, float ds) {
		
		if (!c.returning) {
			if (c.reserved > 0) {
				pickup(c);
			}else if(c.amountCarried > 0 && find(c)) {
				;
			}else if (PATH().finders.entryPoints.find(c.ctx(), c.cty(), c.path, Integer.MAX_VALUE)) {
				c.move();
				cancel(c);
				c.returning = true;
			}else {
				return false;
			}
			return true;
			
		}else {
			return false;
		}
	}

	@Override
	public void cancel(Caravan c) {
		ImportThingy f = ROOMS().IMPORT.UNLOADER;
		f.cancel(c.res, c.reservedGlobally);
		c.reservedGlobally = 0;
		if (c.reserved > 0) {
			TILE_STORAGE s = SETT.PATH().finders().storage.getter.get(c.path.destX(), c.path.destY());
			if (s != null && s.storageReserved() > 0) {
				s.storageUnreserve(CLAMP.i(c.reserved, 0, s.storageReserved()));
				
			}
		}
		
		c.reserved = 0;
		c.amountCarried = 0;
	}

	@Override
	public void hoverInfo(GBox box, Caravan c) {
		box.text(name);
		if (c.amountCarried > 0)
			box.setResource(c.res, c.amountCarried);
	}
	
}
