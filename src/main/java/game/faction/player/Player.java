package game.faction.player;

import java.io.IOException;

import game.faction.*;
import game.faction.trade.FACTION_EXPORTER;
import game.faction.trade.FACTION_IMPORTER;
import init.race.RACES;
import init.race.Race;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LISTE;
import view.interrupter.IDebugPanel;

public final class Player extends Faction {

	public final PTech tech = new PTech();
	public final PTitles titles = new PTitles();
	public final PlayerRaces races;
	private final PResources resources = new PResources();
	private final FCapitol population = new FCapitol(this);
	private final FAppearance appearence = new FAppearance();
	private final FBanner banner = new FBanner(this);
	private final PLevels level = new PLevels();
	private final PBonus bonus;
	private final PCredits credits = new PCredits();
	private final PAdmin admin = new PAdmin();
	private final FKingdom kingdom = new FKingdom(this);
	private final PTribute tribute = new PTribute();
	private final FRuler ruler = new FRuler();
	public final PLocks locks = new PLocks(this);
	
	public Player(Race race, LISTE<Faction> all, KeyMap<Double> boosts){
		super(race, all);
		races = new PlayerRaces(race);
		
		IDebugPanel.add("add credits", new ACTION() {
			
			@Override
			public void exe() {
				credits.purchases.IN.inc(50);
			}
		});
		
		IDebugPanel.add("add credits+", new ACTION() {
			
			@Override
			public void exe() {
				credits.purchases.IN.inc(500000);
			}
		});
		bonus = new PBonus(this, boosts);
	}
	
	@Override
	protected void save(FilePutter file) {
		super.save(file);
		tech.saver.save(file);
		titles.saver.save(file);
		level.saver.save(file);
		admin.saver.save(file);
		tribute.save(file);
		races.saver.save(file);
		bonus.saver.save(file);

	}

	@Override
	protected void load(FileGetter file) throws IOException {
		super.load(file);
		tech.saver.load(file);
		titles.saver.load(file);
		level.saver.load(file);
		admin.saver.load(file);
		tribute.load(file);
		races.saver.load(file);
		bonus.saver.load(file);
	}
	
	@Override
	protected void clear() {
		super.clear();
		tech.saver.clear();
		titles.saver.clear();
		level.saver.clear();
		admin.saver.clear();
		tribute.clear();
		races.saver.clear();
		bonus.saver.clear();
	}
	
	@Override
	public FACTION_IMPORTER buyer() {
		return SETT.ROOMS().IMPORT.tally;
	}
	
	@Override
	public FACTION_EXPORTER seller() {
		return SETT.ROOMS().EXPORT.tally;
	}
	
	@Override
	public boolean isActive() {
		return super.isActive() && SETT.exists();
	}
	
	@Override
	protected void update(double ds) {
		tribute.update(ds);
		super.update(ds);
	}
	
	public void updateSpecial(double ds) {
		for (Race r : RACES.all()) {
			population.population.set(r, STATS.POP().POP.data(null).get(r, 0));
			
		}
		tech.update(ds);
		titles.update(ds);
		level.update();
		bonus.update(ds);
	}

	@Override
	public PResources res() {
		return resources;
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
	public PCredits credits() {
		return credits;
	}
	
	public PLevels level() {
		return level;
	}
	
	public PTech tech() {
		return tech;
	}
	
	public PAdmin admin() {
		return admin;
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
	public PBonus bonus() {
		return bonus;
	}
	
	public static final class PlayerRaces{
		
		private final int[] order = new int[RACES.all().size()];
		
		final SAVABLE saver = new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				file.is(order);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				file.is(order);
			}
			
			@Override
			public void clear() {
				
			}
		};
		
		PlayerRaces(Race player){
			order[0] = player.index;
			int playable = 1;
			for (Race r : RACES.all()) {
				if (r != player && r.playable) {
					playable++;
				}
			}
			int i = 1;
			for (Race r : RACES.all()) {
				if (r != player && r.playable) {
					order[i++] = r.index;
				}else if (r != player){
					order[playable++] = r.index;
				}
				
					
			}
		}
		
		public void order(Race r, int index) {
			int i = 0;
			for (; i < order.length; i++)
				if (order[i] == r.index)
					break;
			
			
			for (; i < order.length-1; i++)
				order[i] = order[i+1];	
			
			for (i = order.length-1; i > index; i--)
				order[i] = order[i-1];
			
			order[index] = r.index;
			
		}
		
		public Race get(int index) {
			if (index < 0)
				return null;
			return RACES.all().get(order[index]);
		}

		public int size() {
			return order.length;
		}
		
	}

}
