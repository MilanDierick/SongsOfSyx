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
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sprite.text.Str;
import view.ui.message.MessageText;
import world.WORLD;
import world.army.AD;
import world.map.pathing.WRegSel;
import world.map.pathing.WRegs.RDist;
import world.map.pathing.WTREATY;

public class EventWorldWarPlayer extends EventResource{
	
	private double[] peacesecond = new double[FACTIONS.MAX];
	private final ArrayListResize<FactionNPC> enemies = new ArrayListResize<>(32, FACTIONS.MAX);
	private static final double peaceI = 1.0/TIME.secondsPerDay*32;
	private double timer = 0;
	
	private static CharSequence ¤¤title = "War!";
	private static CharSequence ¤¤body = "Grave news your majesty! War is upon us.";
	private static CharSequence ¤¤single = "The faction of {0} has become utterly fed up with you. Soon their armies will be upon us. We must muster our forces and counter attack them.";
	private static CharSequence ¤¤many = "Our enemies have formed a coalition against our freedom and way of life, and their armies are on march towards our lands. The following factions are now at war with us:";

	static {
		D.ts(EventWorldWarPlayer.class);
	}
	
	EventWorldWarPlayer(){
		new DWar.DWarListener() {
			
			@Override
			protected void exe(Faction a, Faction b, boolean war) {
				if (!war) {
					if (a == FACTIONS.player())
						peacesecond[b.index()] = TIME.playedGame();
					else if (b == FACTIONS.player())
						peacesecond[a.index()] = TIME.playedGame();
				}
			}
		};
	}
	
	@Override
	protected void update(double ds) {
		timer -= ds;
		if (timer > 0)
			return;
		
		timer += TIME.secondsPerDay*(1+RND.rExpo());
		
		enemies.clearSoft();
		
		for (RDist d : WORLD.PATH().tmpRegs.all(FACTIONS.player().capitolRegion(), WTREATY.NEIGHBOURSF(FACTIONS.player()), WRegSel.CAPITOLS(FACTIONS.player()))) {
			if (d.reg.faction() == FACTIONS.player())
				continue;
			
			FactionNPC f = (FactionNPC) d.reg.faction();
			
			enemies.add(f);
		}
		
		double pp = FACTIONS.player().power();
		
		prune(pp);
		
		
		if (enemies.size() == 0)
			return;
		
		for (FactionNPC f : enemies) {
			FACTIONS.DIP().war.set(f, FACTIONS.player(), true);
		}
		
		MessageText m = new MessageText(¤¤title);
		m.paragraph(¤¤body);
		
		if (enemies.size() == 1)
			m.paragraph(Str.TMP.clear().add(¤¤single).insert(0, enemies.get(0).name));
		else {
			Str.TMP.clear();
			Str.TMP.add(¤¤many);
			Str.TMP.NL();
			for (FactionNPC f : enemies) {
				Str.TMP.add(f.name);
				Str.TMP.NL();
			}
			m.paragraph(Str.TMP);
		}
		
		m.send();
		
		clear();
	}
	
	public void getNewEnemies(){
		
		
		
	}
	
	private void prune(double pp) {
		
		double currentPow = 0;
		double attackPow = 0;
		for (int i = 0; i < enemies.size(); i++) {
			if (FACTIONS.DIP().war.is(enemies.get(i), FACTIONS.player())) {
				currentPow += enemies.get(i).power();
				attackPow += AD.power().get(enemies.get(i));
				enemies.remove(i);
				i--;
			}
			
		}
		
		boolean dropout = true;
		while(enemies.size() > 0 && dropout) {
			dropout = false;
			double totPow = currentPow;
			double at = attackPow;
			for (FactionNPC f : enemies) {
				totPow += f.power();
				at += AD.power().get(f);
			}
			
			if (at == 0) {
				enemies.clearSoft();
				return;
			}
			
			for (int i = 0; i < enemies.size(); i++) {
				FactionNPC f = enemies.get(i);
				double pd = TIME.playedGame()-peacesecond[f.index()];
				pd*= peaceI;
				pd = CLAMP.d(pd*2-1, 0, 1);
				if (!DWar.wantsToFightAgainstPlayer(f, pd*totPow, pp)) {
					enemies.remove(i);
					dropout = true;
					break;
				}
			}
			
		}
	}

	@Override
	protected void save(FilePutter file) {
		file.d(timer);
		file.dsE(peacesecond);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
		file.dsE(peacesecond);
	}

	@Override
	protected void clear() {
		timer = TIME.secondsPerDay*(1+RND.rExpo());
		Arrays.fill(peacesecond, 0);
	}	

}
