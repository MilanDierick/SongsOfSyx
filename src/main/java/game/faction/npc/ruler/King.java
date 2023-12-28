package game.faction.npc.ruler;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.race.Race;
import init.religion.Religion;
import init.resources.RESOURCE;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StrInserter;
import util.info.GFORMAT;
import world.regions.Region;
import world.regions.data.RD;
import world.regions.data.pop.RDRace.RDNames;
import world.regions.data.updating.RealmBuilder;

public class King {

	public Str name = new Str(64);
	private final Str intro = new Str(64);
	private static final StrInserter<Faction> iins = new StrInserter<Faction>("TITLE") {

		@Override
		protected void set(Faction t, Str str) {
			double d = t.realm().regions()/20.0;
			int i = (int) (d*FACTIONS.player().level().all().size());
			i = CLAMP.i(i, 0, FACTIONS.player().level().all().size()-1);
			str.add(FACTIONS.player().level().all().get(i).male);
		}
	};
	private final NPCCourt court; 
	
	King(NPCCourt court){
		this.court = court;
	}
	
	void init() {
		
		Royalty roy = court.all.get(0);
		RDNames nn = RD.RACES().get(roy.induvidual.race()).names;
		name.clear().add(nn.rNames.next());
		name.s();
		GFORMAT.toNumeral(name, RND.rInt(1 + 15));
		intro.clear().add(nn.rIntro.next());
		
	}

	void save(FilePutter file) {
		name.save(file);
		intro.save(file);
		
	}

	void load(NPCCourt c, FileGetter file) throws IOException {
		name.load(file);
		intro.load(file);
	}
	
	public Royalty roy() {
		return court.all.get(0);
	}

	
	public Str intro(Str str) {
		str.add(intro);
		iins.insert(court.faction, str);
		return str;
	}
	
	public double garrison() {
		return 0.5 + 0.5*roy().trait(RTraits.get().war);
	}
	
	public double size() {
		
		double c = 0.25*0.75*roy().trait(RTraits.get().competence);
		return CLAMP.d(c, 0, 1);
	}
	
	public final RealmBuilder builder = new RealmBuilder() {
		
		@Override
		public double priority(Religion religion, Region reg) {
			if (STATS.RELIGION().getter.get(roy().induvidual).religion == religion)
				return 1.0;
			return roy().trait(RTraits.get().tolerance)/2.0;
		}
		
		@Override
		public double priority(RESOURCE res, Region reg) {
			return 1.0;
		}
		
		@Override
		public double policy(Race race, Region reg) {
			double add = roy().trait(RTraits.get().tolerance);
			if (race == roy().induvidual.race())
				return 4*RD.RACES().all.size()-add*RD.RACES().all.size();
			return -1 + roy().induvidual.race().pref().race(race)+add;
			
			
			
		}
		
		@Override
		public double military(Region reg) {
			double ran = RD.RAN().get(reg, 9, 8)/(double)0x0FF;
			double v = 0.75 + roy().trait(RTraits.get().war)*0.25;
			ran = 0.5 + ran*0.5;
			v *= ran;
			return v;
		}

		@Override
		public double size() {
			double c = 0.25*0.75*roy().trait(RTraits.get().competence);
			return CLAMP.d(c, 0, 1);
		}
	};
	
}
