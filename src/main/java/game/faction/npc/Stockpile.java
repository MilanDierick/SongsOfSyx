package game.faction.npc;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.file.*;

final class Stockpile implements SAVABLE{

	
	private double total;
	private final double[] ams;
	private final int[] incoming;
	private final FactionNPC f;
	
	Stockpile(int size, FactionNPC f){
		ams = new double[size];
		incoming = new int[size];
		this.f = f;
	}
	
	@Override
	public void save(FilePutter file) {
		file.d(total);
		file.ds(ams);
		file.is(incoming);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		total = file.d();
		file.ds(ams);
		file.is(incoming);
	}

	@Override
	public void clear() {
		total = 0;
		Arrays.fill(ams, 0);
	}
	
	public void inc(int res, double amount, int incoming) {
		int i = res;
		total -= ams[i];
		ams[i] += amount;
		if (ams[i] < 0) {
			GAME.Notify(RESOURCES.ALL().get(res) + "" + amount);
			ams[i] =0;
		}
		this.incoming[res] += incoming;
		if (this.incoming[res] < 0 && incoming < 0) {
			throw new RuntimeException(RESOURCES.ALL().get(res) + "" + incoming);
		}
		
		total += ams[i];
		
		if (ams[i] >= Integer.MAX_VALUE) {
			GAME.Notify("here " + amount + " " + incoming);
		}
	}
	
	public double amount(int ri) {
		return ams[ri];
	}
	
	public int incoming(int ri) {
		return incoming[ri];
	}
	
	public double total() {
		return total;
	}
	
	private double value(double amount, double total) {
		return (total + ams.length)/(amount+1);
	}
	
	public double creditScore() {
		return (total + f.credits().credits())/total;
	}
	
	public double credit() {
		return (total + f.credits().credits());
	}
	
	public double creditScore(int am, double value) {
		double t = total-am;
		double c = value*creditScore();
		return (t + (f.credits().credits()-c))/t;
	}
	
	public double priceBuy(int ri, int amount) {
		double am = getAmount(ri)+incoming(ri);
		double high = value(am, total);
		double low = value(am+amount, total+amount);
		double price = (high+low)*0.5;
		price *= creditScore(amount, -price);
		if (price < 0)
			return Double.NEGATIVE_INFINITY;
		return price*amount;
	}
	
	private double getAmount(int ri) {
		double am = ams[ri];
		for (RESOURCE rr : RESOURCES.ALL().get(ri).tradeSameAs()) {
			am += ams[rr.index()]/(RESOURCES.ALL().get(ri).tradeSameAs().size()+1);
		}
		return am;
	}
	
	public double priceSell(int ri, int amount) {
		if (ams[ri]-amount <= 0)
			return Double.POSITIVE_INFINITY;
		
		double am = getAmount(ri);
		
		double low = value(am, total);
		double high = value(am-amount, total-amount);
		double price = (high+low)*0.5;
		
		price *= creditScore(-amount, price);
		if (price < 0.1)
			return 0.1;
		return price*amount;
	}

}
