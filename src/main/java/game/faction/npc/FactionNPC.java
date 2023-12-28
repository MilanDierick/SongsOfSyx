package game.faction.npc;

import java.io.IOException;

import game.faction.*;
import game.faction.FCredits.CTYPE;
import game.faction.npc.ruler.NPCCourt;
import game.faction.npc.stockpile.NPCStockpile;
import game.time.TIME;
import init.race.Race;
import init.resources.RESOURCE;
import settlement.main.SETT;
import snake2d.util.file.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.text.Str;
import world.log.WLogger;
import world.regions.data.RD;
import world.regions.data.pop.RDRace;

public final class FactionNPC extends Faction{

	private final ArrayListGrower<NPCResource> res = new ArrayListGrower<>();
	
	public final Str nameIntro = new Str(64);
	private final NPCCourt court = new NPCCourt(this, res);
	private final FBanner banner = new FBanner(this);
	private final TradeNPC trade = new TradeNPC(this);
	private final FResources stats = new FResources(4, TIME.years()) {

		@Override
		public int get(RESOURCE t) {
			if (t == null)
				return stockpile.total();
			return stockpile.amount(t.index());
		}
		
	};
	private final FCredits credits = new FCredits(4, TIME.years());
	private final FRuler ruler = new FRuler();
	public final NPCBonus bonus = new NPCBonus(this, res);
	public final NPCStockpile stockpile = new NPCStockpile(res, credits);
	
	public final NPCRequest request = new NPCRequest(this);
	private int iteration;
	private boolean active;
	
	public FactionNPC(LISTE<Faction> all, UpdaterNPC up){
		super(all);
	}
	
	public void generate(RDRace pref, boolean init) {
		
		court.init();
		
		if (pref == null) {
			pref = RD.RACES().all.rnd();
			double pop = 0;
			for (RDRace r : RD.RACES().all)
				pop += r.pop.growth(capitolRegion());
			pop *= RND.rFloat();
			for (RDRace r : RD.RACES().all) {
				pop -= r.pop.growth(capitolRegion());
				if (pop <= 0) {
					pref = r;
					break;
				}
					
			}
		}
		
		nameIntro.clear().add(pref.names.intros.next());
		name.clear().add(pref.names.fNames.next());
		
		realm().capitol().info.name().clear().add(name);
		iteration++;
		
		credits.inc(-credits.getD(), CTYPE.DIPLOMACY);
		
		for (NPCResource r : res) {
			r.generate(pref, this, init);
		}
		if (init && SETT.exists())
			WLogger.newFaction(this);
		
	}
	
	@Override
	public Race race() {
		return court.race();
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
		nameIntro.save(file);
		for (NPCResource r : res) {
			SAVABLE s = r.saver();
			if (s != null)
				s.save(file);
		}
		trade.saver.save(file);
		file.i(iteration);
		file.bool(active);
		request.save(file);
		super.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		nameIntro.load(file);
		for (NPCResource r : res) {
			SAVABLE s = r.saver();
			if (s != null)
				s.load(file);
		}
		trade.saver.load(file);
		iteration = file.i();
		active = file.bool();
		request.load(file);
		super.load(file);
	}
	
	@Override
	protected void clear() {
		for (NPCResource r : res) {
			SAVABLE s = r.saver();
			if (s != null)
				s.clear();
		}
		trade.saver.clear();
		iteration = 0;
		active = false;
		request.clear();
		super.clear();
	}
	
	@Override
	protected void update(double ds) {
		for (NPCResource r : res) {
			r.update(this, ds);
		}
		request.update();
		super.update(ds);
	}
	
	public int getWorkers(RESOURCE res) {
		return (int) 0;
	}



	@Override
	public FBanner banner() {
		return banner;
	}
	
	@Override
	public FCredits credits() {
		return credits;
	}

	@Override
	public FRuler ruler() {
		return ruler;
	}
	
	public NPCCourt court() {
		return court;
	}
	
	public int iteration() {
		return iteration;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	protected void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public FResources res() {
		return stats;
	}
	
}
