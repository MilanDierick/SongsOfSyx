package settlement.thing.halfEntity.caravan;

import static settlement.main.SETT.*;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import snake2d.util.misc.CLAMP;
import util.gui.misc.GBox;

class TypeArmySupply extends Type{

	private static CharSequence ¤¤verb = "¤fetching";
	
	TypeArmySupply() {
		super(¤¤verb);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean init(Caravan c, int amount) {
		c.reservedGlobally = (short) amount;
		return fetch(c);
	}
	
	private boolean fetch(Caravan c) {
		
		c.path.clear();
		CaravanPickup coo = ROOMS().SUPPLY.reservable(c.ctx(), c.cty(), c.res);
		if (coo == null) {
			c.reserved = 0;
			return false;
		}
		int am = coo.reservable();
		am = CLAMP.i(am, 0, c.reservedGlobally-c.amountCarried);
		c.reserved = (short) am;
		coo.reserve(am);
		c.path.request(c.ctx(), c.cty(), coo.x(), coo.y(), false);
		
		if (!c.path.isSuccessful()) {
			ROOMS().SUPPLY.reserved(c.path.destX(), c.path.destY(), c.res).reserve(-c.reserved);
			c.reserved = 0;
			GAME.Notify(c.ctx() + " " + c.cty() + "->" + coo.x() + " " + coo.y());
			return false;
		}
		
		c.move();
		
		return true;
	}
	
	private boolean grab(Caravan c) {
		
		CaravanPickup coo = ROOMS().SUPPLY.reserved(c.path.destX(), c.path.destY(), c.res);
		if (coo == null || coo.reserved() <= 0) {
			c.reserved = 0;
			return false;
		}
		
		c.reserved --;
		c.amountCarried ++;
		coo.pickup(1);
		FACTIONS.player().res().inc(c.res, RTYPE.ARMY_SUPPLY, -1);

		return true;
	}

	@Override
	public boolean update(Caravan c, float ds) {
		if (c.returning)
			return false;
		
		if (c.reserved > 0 && grab(c)) {
			return true;
		}
		
		if (c.amountCarried < c.reservedGlobally && fetch(c)) {
			return true;
		}
		
		if (c.amountCarried != 0 && PATH().finders.entryPoints.find(c.ctx(), c.cty(), c.path, Integer.MAX_VALUE)) {
			
			c.move();
			c.returning = true;
			return true;
		}
		
		return false;
		
	}

	@Override
	public void cancel(Caravan c) {
		
		if (c.reserved > 0) {
			CaravanPickup coo = ROOMS().SUPPLY.reserved(c.path.destX(), c.path.destY(), c.res);
			if (coo != null && coo.reserved() > 0) {
				coo.pickup(CLAMP.i(c.reserved, 0, coo.reservable()));
			}
		}
		
		c.reserved = 0;
		
	}
	
	@Override
	public void hoverInfo(GBox box, Caravan c) {
		box.text(name);
		if (c.reservedGlobally-c.amountCarried > 0)
			box.setResource(c.res, c.reservedGlobally-c.amountCarried);
	}
	
}
