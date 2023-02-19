package game.faction.npc;

import java.io.IOException;

import game.GAME;
import game.faction.*;
import game.time.TIME;
import init.race.Race;
import init.resources.RESOURCE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;

public final class FactionNPC extends Faction{

	private final FCapitol population = new FCapitol(this);
	private final FAppearance appearence = new FAppearance();
	private final FBanner banner = new FBanner(this);
	private final TradeNPC trade = new TradeNPC(this);
	private final FResources stats = new FResources(4, TIME.years());
	private final NPCCredits credits = new NPCCredits(this, 4, TIME.years());
	private final FKingdom kingdom = new FKingdom(this);
	private final FRuler ruler = new FRuler();
	private final FBonus bonus = new Bonus();
	
	public FactionNPC(Race race, LISTE<Faction> all, UpdaterNPC up){
		super(race, all);
		NPCProduction.all();
	}
	
	@Override
	public TradeNPC buyer() {
		return trade;
	}
	
	@Override
	public TradeNPC seller() {
		return trade;
	}
	
	@Override
	protected void save(FilePutter file) {
		trade.saver.save(file);
		super.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		trade.saver.load(file);
		super.load(file);
	}
	
	@Override
	protected void clear() {
		trade.saver.clear();
		super.clear();
	}
	
	@Override
	protected void update(double ds) {
		GAME.factions().ncpUpdater.update(this);
		super.update(ds);
	}
	
	public int getWorkers(RESOURCE res) {
		return (int) 0;
	}

	@Override
	public FResources res() {
		return stats;
	}

	@Override
	public FCapitol capitol() {
		return population;
	}

	@Override
	public FAppearance appearence() {
		return appearence;
	}

	@Override
	public FBanner banner() {
		return banner;
	}
	
	@Override
	public NPCCredits credits() {
		return credits;
	}
	
	@Override
	public FKingdom kingdom() {
		return kingdom;
	}

	@Override
	public FRuler ruler() {
		return ruler;
	}

	@Override
	public FBonus bonus() {
		return bonus;
	}
	
}
