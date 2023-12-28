package game.faction.player;

import java.io.IOException;

import game.Profiler;
import game.faction.*;
import game.faction.FCredits.CTYPE;
import game.faction.player.emissary.Emissaries;
import game.faction.trade.FACTION_EXPORTER;
import game.faction.trade.FACTION_IMPORTER;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LISTE;
import view.interrupter.IDebugPanel;
import world.regions.data.RD;
import world.regions.data.pop.RDRace;

public final class Player extends Faction {

	public final PTech tech = new PTech();
	public final PTitles titles = new PTitles();
	public final PlayerRaces races;
	private final FResources resources = new FResources(48, TIME.seasons()) {

		@Override
		public int get(RESOURCE t) {
			long stocked = SETT.ROOMS().STOCKPILE.tally().amountReservable(t);
			stocked += SETT.ROOMS().EXPORT.tally.forSale(t);
			return (int) stocked;
		}
		
		
	};
	private final FBanner banner = new FBanner(this);
	private final PLevels level = new PLevels();
	private final PCredits credits = new PCredits();
	private final PAdmin admin = new PAdmin();
	private final FRuler ruler = new FRuler();
	public final Emissaries emissaries = new Emissaries();
	private final PBonusSetting psett;
	private int ri;
	public final PRel rel = new PRel();
	
	public Player(LISTE<Faction> all, KeyMap<Double> boosts) throws IOException{
		super(all);
		races = new PlayerRaces();
		ri = races.get(0).index();
		
		IDebugPanel.add("add credits", new ACTION() {
			
			@Override
			public void exe() {
				credits.inc(50, CTYPE.MISC);
			}
		});
		
		IDebugPanel.add("add credits+", new ACTION() {
			
			@Override
			public void exe() {
				credits.inc(500000, CTYPE.MISC);
			}
		});
		
		psett = new PBonusSetting(boosts);
	}
	
	public void setRace(Race race) {
		ri = race.index;
		races.set(race);
	}
	
	@Override
	public Race race() {
		return RACES.all().get(ri);
	}
	
	@Override
	protected void save(FilePutter file) {
		super.save(file);
		tech.saver.save(file);
		titles.saver.save(file);
		level.saver.save(file);
		admin.saver.save(file);
		races.saver.save(file);
		emissaries.saver.save(file);
		psett.save(file);
		rel.saver.save(file);
		file.i(ri);
		PlayerColors.saver.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		super.load(file);
		tech.saver.load(file);
		titles.saver.load(file);
		level.saver.load(file);
		admin.saver.load(file);
		races.saver.load(file);
		emissaries.saver.load(file);
		psett.load(file);
		rel.saver.load(file);
		ri = file.i();
		PlayerColors.saver.load(file);
	}
	
	@Override
	protected void clear() {
		super.clear();
		tech.saver.clear();
		titles.saver.clear();
		level.saver.clear();
		admin.saver.clear();
		races.saver.clear();
		emissaries.saver.clear();
		psett.clear();
		rel.saver.clear();
		PlayerColors.saver.clear();
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
		return true;
	}
	
	@Override
	protected void update(double ds) {
		super.update(ds);
		
	}
	
	public void updateSpecial(double ds, Profiler prof) {
		if (capitolRegion() != null) {
			int ex = 0;
			for (Race r : RACES.all()) {
				if (RD.RACES().get(r) == null)
					ex += STATS.POP().POP.data(null).get(r, 0);
			}
			
			ex /= (RACES.all().size()-RD.RACES().all.size() + 1);
			
			for (RDRace rr : RD.RACES().all) {
				rr.pop.set(capitolRegion(),  STATS.POP().POP.data(null).get(rr.race, 0)+ex);
			}
		}
		
		
		prof.logStart(tech.getClass());
		tech.update(ds);
		prof.logEnd(tech.getClass());
		
		prof.logStart(titles.getClass());
		titles.update(ds);
		prof.logEnd(titles.getClass());
		
		prof.logStart(level.getClass());
		level.update();
		prof.logEnd(level.getClass());
		
		prof.logStart(emissaries.getClass());
		emissaries.update(ds);
		prof.logEnd(emissaries.getClass());
		
		prof.logStart(rel.getClass());
		rel.update(ds);
		prof.logEnd(rel.getClass());
	}

	@Override
	public FResources res() {
		return resources;
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
	public FRuler ruler() {
		return ruler;
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
		
		
		void set(Race player){
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
		
		PlayerRaces(){
			int playable = 0;
			for (Race r : RACES.all()) {
				if (r.playable) {
					playable++;
				}
			}
			int i = 0;
			for (Race r : RACES.all()) {
				if (r.playable) {
					order[i++] = r.index;
				}else {
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

	@Override
	protected void setActive(boolean active) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double power() {
		return super.power();
	}
	
	@Override
	public double powerW() {
		return super.powerW() + RD.MILITARY().power.getD(capitolRegion()) + Math.max(credits.getD()/RESOURCES.ALL().size(), 0);
	}
	
}
