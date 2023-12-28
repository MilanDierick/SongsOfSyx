package game.events.world;

import java.io.IOException;
import java.util.Arrays;

import game.events.EVENTS.EventResource;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DWar;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.D;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.updating.IUpdater;
import view.ui.message.MessageText;
import world.WORLD;
import world.entity.army.WArmy;
import world.regions.data.RD;

public class EventWorldPeace extends EventResource{

	private double[] lastPowerBalance = new double[FACTIONS.MAX];
	private double[] secondOfOffer = new double[FACTIONS.MAX];
	private double secondOfLastOffer = 0;
	private double[] secondUntilPeace = new double[FACTIONS.MAX];
	
	private double timer = 0;
	
	private static CharSequence ¤¤ally = "Ally Withdraws";
	private static CharSequence ¤¤allyBody = "We, the people of {0} have had enough bloodshed, and must now withdraw from hostilities to put our efforts into the stability of our kingdom.";
	
	private static CharSequence ¤¤truce = "Truce";
	private static CharSequence ¤¤truceD = "The faction of {0} can no longer reach you, and has ceased hostilities towards you temporarily.";
	
	static {
		D.ts(EventWorldWarPlayer.class);
	}
	
	EventWorldPeace(){
		new DWar.DWarListener() {
			
			@Override
			protected void exe(Faction a, Faction b, boolean war) {
				if (war) {
					secondUntilPeace[b.index()] = peaceTime();
					secondUntilPeace[a.index()] = peaceTime();
					if (a == FACTIONS.player()) {
						lastPowerBalance[b.index()] = -1;
					}else if (b == FACTIONS.player()) {
						lastPowerBalance[a.index()] = -1;
					}
				}else {
					secondOfOffer[b.index()] = 0;
					secondOfOffer[a.index()] = 0;
					secondOfLastOffer = 0;
					for (double d : secondOfOffer)
						secondOfLastOffer = Math.max(secondOfLastOffer, d);
					
				}
			}
		};
	}
	
	private double peaceTime() {
		return TIME.playedGame() + TIME.secondsPerDay + RND.rFloat()*TIME.secondsPerDay*32.0;
	}
	
	private final IUpdater updater = new IUpdater(FACTIONS.MAX, TIME.secondsPerDay/2) {
		
		@Override
		protected void update(int i, double timeSinceLast) {
			Faction f = FACTIONS.getByIndex(i);
			if (f.isActive() && f instanceof FactionNPC) {
				up((FactionNPC) f);
			}
		}
	};
	
	@Override
	protected void update(double ds) {
		updater.update(ds);
		
		int ei = (int) timer;
		timer += ds*TIME.secondsPerDayI*4.0;
		if (ei != (int)timer) {
			
			if (timer >= FACTIONS.DIP().war.getEnemies(FACTIONS.player()).size()) {
				timer = 0;
			}
			
			FactionNPC f = (FactionNPC) FACTIONS.DIP().war.getEnemies(FACTIONS.player()).getC((int)timer);
			if (f != null) {
				if (!RD.DIST().factionBordersPlayer(f)) {
					for (int ri = 0; ri < f.realm().regions(); ri++) {
						for (WArmy a : WORLD.ENTITIES().armies.fill(f.realm().region(ri))) {
							if (a.faction() == FACTIONS.player())
								return;
						}
					}
					new MessageText(¤¤truce).paragraph(Str.TMP.clear().add(¤¤truceD).insert(0, f.name)).send();
					FACTIONS.DIP().war.set(f, FACTIONS.player(), false);
					return;
				}else {
					double c = DWar.peaceValue(f);
					if (Math.abs(c-lastPowerBalance[f.index()]) > 0.1) {
						lastPowerBalance[f.index()] = c;
						secondOfOffer[f.index()] = TIME.playedGame();
						secondOfLastOffer = secondOfOffer[f.index()];
					}
				}
				
				
			}
			
			
		}
		
		
	}
	
	private void up(FactionNPC f) {
		
		if (FACTIONS.DIP().war.getEnemies(f).size() == 0)
			return;
		
		if (FACTIONS.DIP().war.is(FACTIONS.player(), f)) {
			return;
		}
		
		if (TIME.playedGame() > secondUntilPeace[f.index()]) {
			int am = 1 + RND.rInt(FACTIONS.DIP().war.getEnemies(f).size());
			am = CLAMP.i(am, 1, FACTIONS.DIP().war.getEnemies(f).size());
			boolean player = false;
			for (int i = 0; i < am; i++) {
				Faction e = FACTIONS.DIP().war.getEnemies(f).get(0);
				FACTIONS.DIP().war.set(f,e, false);
				if (FACTIONS.DIP().war.is(e, FACTIONS.player())) {
					player = true;
				}
			}
			secondUntilPeace[f.index()] = peaceTime()/2.0;
			if (player) {
				new MessageText(¤¤ally).paragraph(Str.TMP.clear().add(¤¤allyBody).insert(0, f.name)).send();
			}
		}else {
			
		}
		
		
		
	}
	
	public boolean hasPeaceOffer() {
		double d = secondOfLastOffer - TIME.playedGame();
		if (d <= 0)
			return false;
		return d < TIME.secondsPerDay;
	}

	public boolean hasPeaceOffer(FactionNPC f) {
		double d = secondOfOffer[f.index()] - TIME.playedGame();
		if (d <= 0)
			return false;
		return d < TIME.secondsPerDay;
	}

	@Override
	protected void save(FilePutter file) {
		file.d(timer);
		file.ds(secondUntilPeace);
		file.ds(lastPowerBalance);
		file.ds(secondOfOffer);
		file.d(secondOfLastOffer);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
		file.ds(secondUntilPeace);
		file.ds(lastPowerBalance);
		file.ds(secondOfOffer);
		secondOfLastOffer = file.d();
	}

	@Override
	protected void clear() {
		timer = TIME.secondsPerDay*(1+RND.rExpo());
		Arrays.fill(secondUntilPeace, 0);
		Arrays.fill(lastPowerBalance, 0);
		Arrays.fill(secondOfOffer, 0);
		secondOfLastOffer = 0;
	}



}
