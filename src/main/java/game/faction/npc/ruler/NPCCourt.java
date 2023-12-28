package game.faction.npc.ruler;


import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.npc.NPCResource;
import game.time.TIME;
import init.race.Race;
import snake2d.util.file.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import world.log.WLogger;
import world.regions.data.RD;
import world.regions.data.pop.RDRace;

public final class NPCCourt extends NPCResource{

	public static final int MAX = 4;
	private final King king = new King(this);
	final ArrayList<Royalty> all = new ArrayList<>(4);
	public final FactionNPC faction;
	private double addT = 0;
	
	public NPCCourt(FactionNPC faction, LISTE<NPCResource> all){
		super(all);
		this.faction = faction;
	}
	
	public LIST<Royalty> all(){
		return all;
	}
	
	public King king() {
		return king;
	}
	

	@Override
	protected SAVABLE saver() {
		return new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				
				
				file.i(all.size());
				for (Royalty r : all)
					r.save(file);
				
				king.save(file);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				
				
				
				int k = file.i();
				all.clear();
				for (int i = 0; i < k; i++) {
					all.add(new Royalty(NPCCourt.this, file));
				}
				king.load(NPCCourt.this, file);
			}
			
			@Override
			public void clear() {
				all.clear();
			}
		};
	}
	
	@Override
	protected void update(FactionNPC faction, double seconds) {
		addT += seconds;
		if (addT > TIME.secondsPerDay*16) {
			addT -= TIME.secondsPerDay*16;
			addSuccessor();
		}
		
		king.roy().update(seconds);
		for (int i = 0; i < all.size(); i++) {
			Royalty r = all.get(i);
			r.update(seconds);
			if (TIME.days().bitsSinceStart() > r.deathDay) {
				kill(r);
				i--;
			}
		}
	}

	public void addSuccessor() {
		if (!all.hasRoom())
			return;
		all.add(newSuccessor(king.roy().induvidual.race()));
	}
	
	private Royalty newSuccessor(Race roy) {
		
		
		double tot = 0;
		for (RDRace r : RD.RACES().all) {
			double d = roy.pref().race(roy);
			if (r.race == roy)
				d += RD.RACES().all.size()*16 - 12*RD.RACES().all.size()*king.roy().trait(RTraits.get().tolerance);
			d*= r.pop.faction().get(faction);
			tot += d;
		}
		tot *= RND.rFloat();
		for (RDRace r : RD.RACES().all) {
			double d = roy.pref().race(roy);
			if (r.race == roy)
				d += RD.RACES().all.size()*16 - 12*RD.RACES().all.size()*king.roy().trait(RTraits.get().tolerance);
			d*= r.pop.faction().get(faction);
			tot -= d;
			if (tot <= 0)
				return new Royalty(this, r.race);
		}
		return new Royalty(this, roy);
	}
	
	void kill(Royalty r) {
		
		if (r.isKing()) {
			
			CharSequence oldKing = king.name;
			
			all.remove(r);
			
			
			if (all.size() == 0){
				all.add(new Royalty(this, r.induvidual.race()));
				//faction destroyed here
			}
			king.init();
			
			CharSequence newKing = king.name;
			
			WLogger.newLeader(faction, oldKing, newKing);
			
		}else {
			int i = all.indexOf(r);
			all.removeOrdered(i);
		}
		
	}

	@Override
	protected void generate(RDRace race, FactionNPC faction, boolean fromScratch) {
		all.clear();
		all.add(new Royalty(this, race.race));
		while(all.hasRoom())
			addSuccessor();
		king.init();
		
		
		
	}
	
	public void init() {
		if (all.size() == 0)
			all.add(new Royalty(this, RD.RACES().all.get(0).race));
	}
	
	public void promote(Royalty roy, boolean message) {
		if (!all.contains(roy))
			throw new RuntimeException();
		int i = all.indexOf(roy);
		all.swap(1, i);
	}
	
	public Royalty getByID(int id) {
		if (king.roy().ID == id)
			return king.roy();
		for (Royalty r : all)
			if (r.ID == id)
				return r;
		return null;
	}

	public Race race() {
		if (king.roy() == null)
			return FACTIONS.player().race();
		return king.roy().induvidual.race();
	}
	
}
