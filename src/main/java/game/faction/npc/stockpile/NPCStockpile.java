package game.faction.npc.stockpile;

import java.io.IOException;
import java.util.Arrays;

import game.GameDisposable;
import game.faction.npc.FactionNPC;
import game.faction.npc.NPCResource;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import util.data.DOUBLE;
import world.regions.data.pop.RDRace;

public class NPCStockpile extends NPCResource{

	public static final int AVERAGE_PRICE = 400;
	
	static Updater updater;
	
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				updater = null;
			}
		};
	}
	
	private int totalAmount = 0;
	private long invValueI;
	private long invValueII = 0x01000_0000L;
	private final double[] amounts = new double[RESOURCES.ALL().size()];
	private final double[] buyCaps = new double[RESOURCES.ALL().size()];
	final double[] prodCap = new double[RESOURCES.ALL().size()];
	final short[] prio = new short[RESOURCES.ALL().size()];
	
	private final DOUBLE credits;
	
	public NPCStockpile(LISTE<NPCResource> all, DOUBLE credits){
		super(all);
		if (updater == null)
			updater = new Updater();
		this.credits = credits;
		invValueI =  invValueII*RESOURCES.ALL().size();
	}
	
	public int total() {
		return totalAmount;
	}
	
	public int amount(RESOURCE res) {
		return (int) amounts[res.index()];
	}
	
	public int amount(int ri) {
		return (int) amounts[ri];
	}
	
	public void set(RESOURCE res, double amount) {

		invValueI -= intV(amounts[res.index()]);
		totalAmount -= (int) amounts[res.index()];	
		
		amounts[res.index()] = Math.max(amount, 0);
		
		totalAmount += (int) amounts[res.index()];
		invValueI += intV(amounts[res.index()]);
	}
	
	private long intV(double a) {
		long am = (long) a;
		am += 1;
		return invValueII/am;
	}
	
	public void inc(RESOURCE res, double amount) {
		set(res, amounts[res.index()] + amount);
	}
	
	public void setBuyValue(RESOURCE res, double value) {
		buyCaps[res.index()] = value;
	}
	
	public double buyCap(RESOURCE res) {
		return buyCaps[res.index()];
	}
	
	public double creditScore() {
		return (totalAmount + credits.getD())/(totalAmount+1);
	}
	

	
	public double credit() {
		return (totalAmount*5.0 + credits.getD());
	}

	


	public double price(int ri, int amount) {
		if (amounts[ri]+amount <= 0)
			return AVERAGE_PRICE*RESOURCES.ALL().size();
		
		long tot = invValueI;
		long intV = intV(amounts[ri]);
		tot -= intV;
		long iv = intV(amounts[ri]+amount);
		tot += iv;
		
		double v = iv;
		v /= tot;
		
		double price = v*AVERAGE_PRICE*RESOURCES.ALL().size();
		return price;
		
	}
	
	public double priceBuy(int ri, int amount) {
		
		double price = price(ri, amount);
		double tot = Math.max(totalAmount + credits.getD() + amount, RESOURCES.ALL().size());
		tot/=(totalAmount+amount);
		tot = CLAMP.d(tot, 0, 0.5);
		price *= tot*amount;
		if (price > amount*buyCaps[ri]*AVERAGE_PRICE)
			price = amount*buyCaps[ri]*AVERAGE_PRICE;
		return price;
	}
	
	public double priceSell(int ri, int amount) {
		
		double price = price(ri, -amount);
		double tot = Math.max(totalAmount + credits.getD() + amount, RESOURCES.ALL().size());
		tot/=(totalAmount+amount);
		tot = CLAMP.d(tot, 0, 0.5);
		price *= tot*amount;
		if (price <= 0)
			price = 1;
		return price;
		
	}
	
	public double prodRate(RESOURCE res) {
		return prodCap[res.index()];
	}
	
	public int prio(RESOURCE res) {
		return prio[res.index()];
	}

	@Override
	protected SAVABLE saver() {
		return new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				file.dsE(amounts);
				file.dsE(buyCaps);
				file.dsE(prodCap);
				file.ssE(prio);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				file.dsE(amounts);
				file.dsE(buyCaps);
				file.dsE(prodCap);
				file.ssE(prio);
				totalAmount = 0;
				for (double d : amounts)
					totalAmount += (int) d;
				invValueI = 0;
				for (double d : amounts)
					invValueI += invValueII/((int)d+1);
			}
			
			@Override
			public void clear() {
				Arrays.fill(amounts, 0);
				Arrays.fill(buyCaps, 0);
				Arrays.fill(prodCap, 0);
				Arrays.fill(prio, (short)0);
				totalAmount = 0;
				invValueI = invValueII*RESOURCES.ALL().size();
			}
		};
	}

	@Override
	public void update(FactionNPC faction, double seconds) {
		updater.update(this, faction, seconds);
		
	}

	@Override
	protected void generate(RDRace race, FactionNPC faction, boolean init) {
		saver().clear();
		if (init) {
			for (int i = 0; i < 200; i++) {
				update(faction, TIME.secondsPerDay*8);
			}
		}else {
			for (int i = 0; i < 20; i++) {
				update(faction, TIME.secondsPerDay*8);
			}
		}
		faction.res().clear();
		
	}
	
	
	
}
