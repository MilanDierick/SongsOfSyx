package game.faction.trade;

import java.io.IOException;

import game.faction.*;
import game.faction.npc.FactionNPC;
import game.faction.npc.ruler.ROpinions;
import game.faction.trade.TradeShipper.Partner;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import util.updating.TileUpdater;
import view.interrupter.IDebugPanel;
import world.WORLD;
import world.entity.caravan.Shipment;
import world.regions.data.RD;

public class TradeManager extends FactionResource{

	public static final int TRADE_INTERVAL = 2;
	
	private final TileUpdater updater;
	private final TradeShipper shipper = new TradeShipper();
	private final TradeSorter sorter = new TradeSorter();
	

	private static final double TOLLP = 0.5;
//	private static final double TOLL = 0.05;
	public static final int MIN_LOAD = 32;

	public static int toll(Faction f, Faction to, double distance, int price) {
		
		if (f == FACTIONS.player()) {
			
			int t = tollPlayer((FactionNPC) to, price, distance);
			t += price*ROpinions.tradeCost((FactionNPC) to);
			
			return (int) Math.ceil(t);
		}else {
			double t = price*TOLLP*(distance+50.0)/(375.0);
			
//			double tt = TOLL;
//			double t = price*tt;
//			t += amount * distance* tt;
			return (int) Math.ceil(t);
		}
		
	}
	
	public static int tollPlayer(FactionNPC to, int price, double distance) {
		double d = (distance+50.0)/(500.0* RD.DIST().boostable.get(HCLASS.CITIZEN.get(null)));
		d = CLAMP.d(d, 0, 1);
		return (int) (price*d);
	}

	
	public TradeManager(FACTIONS fs){
		
		IDebugPanel.add("Trade all", new ACTION() {
			
			@Override
			public void exe() {
				clear();
				prime();
			}
		});
		
		updater = new TileUpdater(FACTIONS.MAX, FACTIONS.MAX+1, TRADE_INTERVAL*TIME.days().bitSeconds()) {
			
			@Override
			protected void update(int iteration, int factionI, int vv, double timeSinceLast) {
				
				if (factionI == FACTIONS.MAX) {
					
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
				Faction buyer = FACTIONS.getByIndex(factionI);
				if (buyer.isActive() && buyer.capitolRegion() != null) {
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
	protected void update(double ds, Faction f) {
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
			boolean create = buyer == FACTIONS.player() || seller == FACTIONS.player();
			if (!create)
				create = WORLD.ENTITIES().all().size() < 200;
			
				if (create) {
					s = WORLD.ENTITIES().caravans.create(seller.capitolRegion(), buyer.capitolRegion(), ITYPE.trade);
					if (s == null)
						LOG.ln("here!");
				}
			
			
		}
		
		if (s != null) {
			for (RESOURCE r : RESOURCES.ALL()) {
				int a = count.traded(r);
				if (a > 0) {
					s.load(r, a);
				}
			}
		}else {
			for (RESOURCE r : RESOURCES.ALL()) {
				int a = count.traded(r);
				if (a > 0) {
					buyer.buyer().deliverAndUnreserve(r, am, ITYPE.trade);
				}
			}
		}
		//LOG.ln();
		
	}
	
	public void prime() {
		for (int i = 0; i < FACTIONS.NPCs().size(); i++) {
			FactionNPC f = FACTIONS.NPCs().get(i);
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
