package game.faction.trade;

import java.io.IOException;

import game.faction.*;
import game.faction.npc.FactionNPC;
import game.faction.trade.TradeShipper.Partner;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.updating.TileUpdater;
import world.World;
import world.entity.caravan.Shipment;

public class TradeManager extends FactionResource{

	public static final int TRADE_INTERVAL = 2;
	
	private final TileUpdater updater;
	private final TradeShipper shipper = new TradeShipper();
	private final TradeSorter sorter = new TradeSorter();
	
	private static final double TOLL = 0.00625;
	public static final int MIN_LOAD = 32;

	public static double priceToll(double distance, int amount) {
		return amount*(distance * TOLL);
	}
	
	public TradeManager(FACTIONS fs){
		
		
		
		updater = new TileUpdater(FACTIONS.all().size(), FACTIONS.all().size()+1, TRADE_INTERVAL*TIME.days().bitSeconds()/World.SPEED) {
			
			@Override
			protected void update(int iteration, int factionI, int vv, double timeSinceLast) {
				
				if (factionI == FACTIONS.all().size()) {
					
					if (iteration == 0) {
						sellPlayer();
					}
					if (shipper.partners() > 0) {
						Partner p = shipper.popNextPartner();						
						Faction b = p.faction();
						ship(b, FACTIONS.player(), p, true);
					}
					return;
				}
				Faction buyer = FACTIONS.all().get(factionI);
				if (buyer.isActive()) {
					if (iteration == 0) {
						buy(buyer);
					}
					
					if (shipper.partners() > 0) {
						Partner p = shipper.popNextPartner();						
						Faction b = p.faction();
						ship(buyer, b, p, true);
					}					
				}
				
			}
		};
	}

	@Override
	protected void save(FilePutter file) {
		updater.save(file);
		shipper.save(file);
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		updater.load(file);
		shipper.load(file);
	}

	@Override
	protected void clear() {
		updater.clear();
		shipper.clear();
	}

	@Override
	protected void update(double ds) {
		updater.update(ds);
	}
	
	private void sellPlayer() {
	
		
		if (!SETT.exists() || SETT.ENTRY().isClosed())
			return;
		
		shipper.init(FACTIONS.player());
		sorter.sellPlayer(shipper);
		
	}
	
	void buy(Faction buyer) {

		shipper.init(buyer);
		sorter.buy(buyer, shipper);

	}
	
	private void ship(Faction buyer, Faction seller, Partner count, boolean shipping) {
		if (!buyer.isActive())
			return;
		
		int am = 0;
		for (RESOURCE r : RESOURCES.ALL()) {
			am += count.traded(r);
			
		}
		if (am <= 0)
			return;
	
		
		Shipment s = null;
		if (shipping && seller.isActive()) {
			s = World.ENTITIES().caravans.createTrade(seller.capitolRegion().cx(), seller.capitolRegion().cy(), buyer.capitolRegion());
			if (s == null)
				LOG.ln("here!");
		}
		
		if (s != null) {
			for (RESOURCE r : RESOURCES.ALL()) {
				int a = count.traded(r);
				if (a > 0) {
					buyer.buyer().reserveSpace(r, -a);
					s.load(r, a);
				}
			}
		}else {
			for (RESOURCE r : RESOURCES.ALL()) {
				int a = count.traded(r);
				if (a > 0) {
					buyer.buyer().reserveSpace(r, -a);
					//LOG.ln(r + " " + a);
					//buyer.buyer().reserveSpace(r, -a);
					buyer.buyer().addImport(r, a);
				}
			}
		}
		//LOG.ln();
		
	}
	
	public void prime() {
		for (FactionNPC f : FACTIONS.NPCs()) {
			
			if (!f.isActive())
				continue;
			buy(f);
			while (shipper.hasNextPartner()) {
				Partner p = shipper.popNextPartner();
				ship(f, p.faction(), p, false);
			}
			
		}
	}
	
}
