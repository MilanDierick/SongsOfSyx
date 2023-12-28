package settlement.thing.halfEntity.caravan;

import static settlement.main.SETT.*;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.trade.ITYPE;
import settlement.main.SETT;
import util.gui.misc.GBox;
import world.WORLD;
import world.entity.caravan.Shipment;

class TypeFetcherWarehouse extends Type{

	private static CharSequence ¤¤verb = "¤fetching";
	
	static int price;
	static int faction;
	
	TypeFetcherWarehouse() {
		super(¤¤verb);
	}

	@Override
	public boolean init(Caravan c, int amount) {
		SETT.HALFENTS().caravans.tmpSold[c.res.index()] += amount;
		c.tmp = (short) price;
		c.tmp2 = (short) faction;
		c.reservedGlobally = (short) amount;
		return fetch(c);
	}
	
	private boolean fetch(Caravan c) {
		c.path.clear();
		
		if (SETT.PATH().finders.resource.find(c.res().bit, c.res().bit, c.res().bit, c.ctx(), c.cty(), c.path, Integer.MAX_VALUE) != null) {
			int am = 1;
			am += SETT.PATH().finders.resource.reserveExtra(true, true, c.res, c.path.destX(), c.path.destY(), c.reservedGlobally-1);
			c.reservedGlobally -= am;
			SETT.HALFENTS().caravans.tmpSold[c.res.index()] -= am;
			c.reserved = (short) am;
			c.move();
			return true;
		}
		return false;
	}
	
	private boolean pickup(Caravan c) {
		if (SETT.PATH().finders.resource.pickup(c.res, c.path.destX(), c.path.destY(), 1) == 1) {
			c.reserved --;
			c.amountCarried ++;
			return true;
		}else {
			SETT.HALFENTS().caravans.tmpSold[c.res.index()] += c.reserved;
			c.reservedGlobally += c.reserved;
			c.reserved = 0;
			if (c.reservedGlobally > 0)
				return fetch(c);
		}
		return false;
	}

	@Override
	public boolean update(Caravan c, float ds) {
		if (c.returning) {
			Faction buyer = FACTIONS.getByIndex(faction);
			if (buyer == null || !buyer.isActive()) {
				cancel(c);
			}else {
				buyer.buyer().buy(c.res(), c.amountCarried, c.tmp*c.amountCarried, FACTIONS.player());
				SETT.ROOMS().EXPORT.tally.sellFake(c.res(), c.amountCarried, c.tmp*c.amountCarried);
				Shipment ss = WORLD.ENTITIES().caravans.create(FACTIONS.player().capitolRegion().cx(), FACTIONS.player().capitolRegion().cy(), buyer.capitolRegion(), ITYPE.trade);
				if (ss != null) {
					ss.load(c.res(), c.amountCarried);
					
				}else {
					buyer.buyer().deliverAndUnreserve(c.res(), c.amountCarried, ITYPE.trade);
				}
				c.reserved = 0;
				c.reservedGlobally = 0;
				c.amountCarried = 0;
			}
			return false;
		}
		
		if (pickup(c))
			return true;
			
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
			SETT.PATH().finders.resource.unreserve(c.res, c.path.destX(), c.path.destY(), c.reserved);
			c.reserved = 0;
		}
		SETT.HALFENTS().caravans.tmpSold[c.res.index()] -= c.reservedGlobally;
		c.reserved = 0;
		c.reservedGlobally = 0;
		if (c.amountCarried > 0) {
			THINGS().resources.create(c.ctx(), c.cty(), c.res(), c.amountCarried);
			c.amountCarried = 0;
		}
		
	}
	
	@Override
	public void hoverInfo(GBox box, Caravan c) {
		box.text(name);
		if (c.reservedGlobally-c.amountCarried > 0)
			box.setResource(c.res, c.reservedGlobally-c.amountCarried);
	}
	
}
