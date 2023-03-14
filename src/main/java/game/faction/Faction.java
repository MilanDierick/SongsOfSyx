package game.faction;

import java.io.IOException;

import game.faction.trade.FACTION_EXPORTER;
import game.faction.trade.FACTION_IMPORTER;
import init.race.RACES;
import init.race.Race;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;
import world.map.regions.Region;

public abstract class Faction{

	

//	public final FactionTrade trade = new FactionTrade(this);
	
	private final int index;
	protected int ri;
	
	public void remove() {
		kingdom().remove();
		FACTIONS.rel().clear(this);
	}
	
	protected Faction(LISTE<Faction> all){
		index = all.add(this);
	}

	public final Race race() {
		return RACES.all().get(ri);
	}
	
	

	public final int index() {
		return index;
	}
	
	public final Region capitolRegion() {
		return kingdom().realm().capitol();
	}

	protected void save(FilePutter file) {
		file.i(ri);
		
		res().save(file);
		capitol().save(file);
		appearence().save(file);
		banner().save(file);
		credits().save(file);
		kingdom().save(file);
		ruler().save(file);
	}

	protected void load(FileGetter file) throws IOException {
		ri = file.i();
		
		res().load(file);
		capitol().load(file);
		appearence().load(file);
		banner().load(file);
		credits().load(file);
		kingdom().load(file);
		ruler().load(file);
	}

	protected void clear() {
		res().clear();
		capitol().clear();
		appearence().clear();
		banner().clear();
		credits().clear();
		kingdom().clear();
	}

	protected void update(double ds) {
		res().update(ds);
		capitol().update(ds);
		appearence().update(ds);
		banner().update(ds);
		credits().update(ds);
		kingdom().update(ds);
	}
	
	public boolean isActive() {
		return capitolRegion() != null;
	}

	
	public abstract FACTION_IMPORTER buyer();
	
	public abstract FACTION_EXPORTER seller();
	
	public abstract FResources res();
	
	public abstract FCapitol capitol();
	
	public abstract FAppearance appearence();
	
	public abstract FBanner banner();
	
	public abstract FCredits credits();
	
	public abstract FKingdom kingdom();

	public abstract FRuler ruler();
	
	public abstract FBonus bonus();

}
