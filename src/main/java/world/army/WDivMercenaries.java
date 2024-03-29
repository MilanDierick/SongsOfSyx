package world.army;

import java.io.IOException;
import java.util.Iterator;

import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.time.TIME;
import init.D;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import view.ui.message.MessageText;
import world.entity.army.WArmy;

public final class WDivMercenaries implements LIST<WDivMercenary>{

	private ArrayList<WDivMercenary> all = new ArrayList<>(48);
	private double timer = 0;
	private int di = 0; 
	private boolean hasSent = false;
	private CharSequence ¤¤mTitle = "¤Mercenaries leaving!";
	private CharSequence ¤¤mBody = "¤Since you don't have enough credits to pay them, your conscripted mercenaries are leaving you.";
	
	
	WDivMercenaries() {
		
		D.t(this);
		
		for (int i = 0; i < all.max(); i++) {
			WDivMercenary d = new WDivMercenary(i);
			all.add(d);
		}
		
	}
	
	void randmoize() {
		for (int i = 0; i < all.max(); i++) {
			WDivMercenary d = all.get(i);
			d.randomize();
		}
	}
	
	void save(FilePutter file) {
		for (WDivMercenary d : all)
			d.save(file);
		file.d(timer);
		file.i(di);
	}
	
	void load(FileGetter file) throws IOException {
		for (WDivMercenary d : all)
			d.load(file);
		timer = file.d();
		di = file.i();
	}
	
	public void debug() {
		debug = true;
		update(TIME.secondsPerDay*all.size());
	}
	
	void update(double ds) {
		
		timer += ds;
		while (timer >= TIME.secondsPerDay/all.size()) {
			di = (di+1)%all.size();
			if (di == 0)
				hasSent = false;
			WDivMercenary d = all.get(di);
			
			if (d.army() == null || d.army().acceptsSupplies()) {
				d.menSet(CLAMP.i(d.men() + 10, 0, d.menTarget()));
			}
			
			
			
			
			if (d.army() == null && RND.oneIn(16)) {
				d.randomize();
			}
			
			
			if (d.army() != null && d.army().faction() == FACTIONS.player()) {

				int cost = d.costPerMan()*d.men();
				if (cost > FACTIONS.player().credits().credits()) {
					d.reassign(null);
					if (!hasSent) {
						new MessageText(¤¤mTitle, ¤¤mBody).send();
						hasSent = true;
					}
				}else {
					
					FACTIONS.player().credits().inc(-cost, CTYPE.MERCINARIES);
				}
				
			}
			
			timer -= TIME.secondsPerDay/all.size();
		}
	}
	
	private boolean debug = false;
	
	public int max() {
		if (debug)
			return size();
		else
			return (int) (size()*CLAMP.i(FACTIONS.player().realm().all().size(), 1, 10)/10.0);
	}
	
	public int upkeepCost(int index) {
		return all.get(index).costPerMan()*all.get(index).menTarget();
	}
	
	public int signingCost(int index) {
		return 2*all.get(index).costPerMan()*all.get(index).menTarget();
	}
	

	ADDiv get(long l) {
		return all.get((int) (l & 0x00000FFFF));
	}
	
	public void hire(WArmy a, WDivMercenary div) {
		div.reassign(a);
	}


	
	@Override
	public Iterator<WDivMercenary> iterator() {
		return all.iterator();
	}

	@Override
	public WDivMercenary get(int index) {
		return all.get(index);
	}

	@Override
	public boolean contains(int i) {
		return all.contains(i);
	}

	@Override
	public boolean contains(WDivMercenary object) {
		return all.contains(object);
	}

	@Override
	public int size() {
		return all.size();
	}

	@Override
	public boolean isEmpty() {
		return all.isEmpty();
	}


	
	
}
