package game.faction.trade;

import java.io.IOException;
import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayListShort;
import snake2d.util.sets.LIST;
import world.entity.WPathing;
import world.entity.WPathing.FactionDistance;

final class TradeShipper implements SAVABLE {

	private final Partner[] partners = new Partner[FACTIONS.MAX];
	private final ArrayListShort neighFactions = new ArrayListShort(FACTIONS.MAX);
	
	public TradeShipper() {
		for (int i = 0; i < partners.length; i++)
			partners[i] = new Partner(FACTIONS.getByIndex(i));
	}
	
	@Override
	public void save(FilePutter file) {
		neighFactions.save(file);
		for (int i = 0; i < partners(); i++) {
			file.d(partner(i).toll);
		}
		for (int i = 0; i < partners(); i++) {
			file.is(partner(i).traded);
		}
	}

	@Override
	public void load(FileGetter file) throws IOException {
		
		neighFactions.load(file);
		for (int i = 0; i < partners(); i++) {
			partner(i).toll = file.d();
		}
		for (int i = 0; i < partners(); i++) {
			file.is(partner(i).traded);
		}
	}

	@Override
	public void clear() {
		neighFactions.clear();
	}
	
	void init(Faction buyer) {
		LIST<FactionDistance> n = WPathing.getFactions(buyer);
		
		neighFactions.clear();
		for (FactionDistance d : n) {
			if (d.f == buyer)
				continue;
			if (FACTIONS.rel().tradePartner.get(d.f, buyer) == 0)
				continue;
			
			Partner p = partners[d.f.index()];
			Arrays.fill(p.traded, 0);
			p.toll = TradeManager.priceToll(d.distance, 1);
			neighFactions.add(d.f.index());
		}
	}
	
	public Partner popNextPartner() {
		int i = neighFactions.remove(neighFactions.size()-1);
		return partners[i];
	}
	
	public boolean hasNextPartner() {
		return neighFactions.size() > 0;
	}
	
	public int partners() {
		return neighFactions.size();
	}
	
	public Partner partner(int i) {
		return partners[neighFactions.get(i)];
	}
	
	final static class Partner {
		
		private final short faction;
		private double toll;
		private final int[] traded = new int[RESOURCES.ALL().size()];
		
		Partner(Faction faction){
			this.faction = (short) faction.index();
		}
		
		public Faction faction() {
			return FACTIONS.getByIndex(faction);
		}
		
		public double toll() {
			return toll;
		}
		
		public int traded(RESOURCE res) {
			return traded[res.index()];
		}
		
		public void trade(RESOURCE res, int amount) {
			traded[res.index()] += amount;
			
		}
		
	}


	
	
	
}
