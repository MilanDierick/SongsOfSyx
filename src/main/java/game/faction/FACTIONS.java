package game.faction;

import java.io.IOException;

import game.GAME.GameResource;
import game.GameConRandom;
import game.faction.npc.FactionNPC;
import game.faction.npc.UpdaterNPC;
import game.faction.player.Player;
import game.faction.trade.PlayerPrices;
import game.faction.trade.TradeManager;
import game.time.TIME;
import init.RES;
import init.race.RACES;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;
import util.updating.IUpdater;
import world.World;

public class FACTIONS extends GameResource {

	public static final int MAX = 64;
	private static FACTIONS self;
	
	
	private final IUpdater updater = new IUpdater(MAX, TIME.days().bitSeconds()/World.SPEED) {

		@Override
		protected void update(int i, double timeSinceLast) {
			if (all.get(i).isActive())
				all.get(i).update(timeSinceLast);
			
		}
	};
	
	private final ArrayList<Faction> all = new ArrayList<>(MAX);
	private final ArrayList<FactionNPC> npcs = new ArrayList<>(MAX - 1);
	private final Player player;
	private final FactionResource npcManager;
	public final UpdaterNPC ncpUpdater;
	public final PlayerPrices tradeUtil;
	private final FDiplomacy relations;

	public FACTIONS(KeyMap<Double> boosts){
		self = this;
		this.player = new Player(all, boosts);
		ncpUpdater = new UpdaterNPC();
		for (int i = 1; i < MAX; i++) {
			npcs.add(new FactionNPC(RACES.all().get(0), all, ncpUpdater));
			
		}
		

		tradeUtil = new PlayerPrices();
		relations = new FDiplomacy();
		npcManager = new TradeManager(this);

		
		
		
	}
	
	@Override
	protected void save(FilePutter file) {
		for (Faction f : all)
			f.save(file);
		updater.save(file);
		npcManager.save(file);
		relations.saver.save(file);
//		if (GAME.achieving() && self != null) {
//			Flusher.flush(self.player);
//		}
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		for (Faction f : all)
			f.load(file);
		updater.load(file);
		npcManager.load(file);
		relations.saver.load(file);
//		if (GAME.achieving()) {
//			Flusher.load(self.player);
//		}
		
	}

	@Override
	protected void update(float ds) {
		updater.update(ds);
		npcManager.update(ds);
		player.updateSpecial(ds);
		
	}
	
	public static Player player() {
		return self.player;
	}
	
	public static Faction other() {
		return FACTIONS.NPCs().get(0);
	}

	public static Faction getByIndex(int index) {
		return self.all.get(index);
	}

	public static LIST<Faction> all() {
		return self.all;
	}

	public static LIST<FactionNPC> NPCs() {
		return self.npcs;
	}

	public static boolean isNPC(Faction f) {
		return f.index() >= NPCs().get(0).index();
	}
	
	public static FDiplomacy rel() {
		return self.relations;
	}
	
	public static PlayerPrices tradeUtil() {
		return self.tradeUtil;
	}
	
	public void prime() {
		
		RES.loader().print("Simulating factions...");
		
		ncpUpdater.init(((TradeManager)self.npcManager));
	
	}
	
	public void generate(GameConRandom random) {
		new Generator();
		
		FactionProfileFlusher.load(FACTIONS.player());
	}
	

}
