package settlement.room.home.house;

import java.util.Arrays;

import game.faction.FACTIONS;
import init.D;
import init.resources.RES_AMOUNT;
import init.sprite.ICON;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.util.RoomInitData;
import snake2d.util.datatypes.AREA;
import snake2d.util.sets.LIST;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

class Upgrader extends PlacableMulti{
	
	private static CharSequence ¤¤name = "¤House upgrader";
	private static CharSequence ¤¤desc = "¤Instantly upgrades houses to accommodate more lodgers. Resources used are not refundable.";
	
	private static CharSequence ¤¤probHouse = "¤Must be placed on existing houses.";
	private static CharSequence ¤¤probUpgrade = "¤House is already upgraded to current maximum.";
	private static CharSequence ¤¤residents = "¤Residents";
	private static CharSequence ¤¤noResources = "¤Not enough resources";
	
	private final LIST<RES_AMOUNT> costs;
	private final int[] resAcc;
	
	static {
		D.ts(Upgrader.class);
	}
	
	public Upgrader(ROOM_HOME room_HOME, RoomInitData init) {
		super(¤¤name, ¤¤desc, new ICON.BIG.Twin(room_HOME.iconBig(), SPRITES.icons().m.plus));
		
		this.costs = RES_AMOUNT.make(init.data().json("RESOURCE_UPGRADE"));
		resAcc = new int[costs.size()];
	}

	private int extra;
	private int tiles;
	private int maxU;
	private CharSequence prevProblem = ¤¤probHouse;
	
	@Override
	public CharSequence isPlacable(AREA area, PLACER_TYPE type) {
		extra = 0;
		tiles = 0;
		maxU = FACTIONS.player().locks.maxUpgrade(SETT.ROOMS().HOMES.HOME);
		Arrays.fill(resAcc, 0);
		prevProblem = ¤¤probHouse;
		
		return super.isPlacable(area, type);
	}
	
	@Override
	public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
		
		if (!SETT.ROOMS().HOMES.HOME.is(tx, ty))
			return prevProblem;
	
		
		Room r = SETT.ROOMS().map.get(tx, ty);
		
		int up = r.upgrade(tx, ty);
		if (r.upgrade(tx, ty) >= maxU) {
			prevProblem = ¤¤probUpgrade;
			return ¤¤probUpgrade;
		}
		
		if (r.mX(tx, ty) == tx && r.mY(tx, ty) == ty) {
			
			int ar = r.area(tx, ty)/3;
			tiles += ar;
			for (int i = 0; i < costs.size(); i++) {
				resAcc[i] += costs.get(i).amount()*ar;
				if (resAcc[i] > SETT.ROOMS().STOCKPILE.tally().amountReservable(costs.get(i).resource())) {
					prevProblem = ¤¤noResources;
					return ¤¤noResources;
				}
			}
			extra += SETT.ROOMS().HOMES.HOME.constructor.maxOccupants[SETT.ROOMS().fData.item.get(tx, ty).group.index()][up+1] - SETT.ROOMS().HOMES.HOME.constructor.maxOccupants[SETT.ROOMS().fData.item.get(tx, ty).group.index()][up];
			
		}
		
		return null;
	}

	@Override
	public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
		Room r = SETT.ROOMS().map.get(tx, ty);
		
		if (r.mX(tx, ty) == tx && r.mY(tx, ty) == ty) {
			int t = r.area(tx, ty)/3;
		
			for (RES_AMOUNT am : costs) {
				int a = am.amount()*t;
				if (a > SETT.ROOMS().STOCKPILE.tally().amountReservable(am.resource()))
					return;
			}
			
			for (RES_AMOUNT am : costs) {
				int a = am.amount()*t;
				SETT.ROOMS().STOCKPILE.remove(am.resource(), a, FACTIONS.player().res().outConstruction);
			}
			r.upgradeSet(tx, ty, r.upgrade(tx, ty)+1);
		}
		

	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		return SETT.ROOMS().HOMES.HOME.is(fromX, fromY) && SETT.ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY);
	}
	
	public PlacableMulti get() {
		return this;
	}
	
	@Override
	public void placeInfo(GBox b, int oktiles, AREA a) {
		
		oktiles /= 3;
		if (oktiles > 0) {
			
			b.textLL(¤¤residents);
			b.tab(6);
			b.add(GFORMAT.iIncr(b.text(), extra));
			b.NL(16);
			
			for (RES_AMOUNT am : costs) {
				b.add(am.resource().icon());
				b.textL(am.resource().names);
				b.tab(6);
				b.add(GFORMAT.iofk(b.text(), am.amount()*tiles, SETT.ROOMS().STOCKPILE.tally().amountReservable(am.resource())));
				b.NL();
			}
		}
		
		super.placeInfo(b, oktiles, a);
	}

}
