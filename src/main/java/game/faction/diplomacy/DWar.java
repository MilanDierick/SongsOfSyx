package game.faction.diplomacy;


import java.io.IOException;
import java.util.Arrays;

import game.GameDisposable;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.npc.ruler.ROpinions;
import game.time.TIME;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import world.WORLD;
import world.army.AD;
import world.entity.army.WArmy;
import world.log.WLogger;
import world.regions.Region;
import world.regions.data.RD;

public final class DWar {

	private final DiplomacyData bits = new DiplomacyData();
	private final short[][] enemies = new short[FACTIONS.MAX][FACTIONS.MAX];
	private final ArrayList<Faction> tmp;
	DWar(ArrayList<Faction> tmp){
		saver.clear();
		this.tmp = tmp;
	}
	
	private static ArrayListGrower<DWarListener> listeners = new ArrayListGrower<>();
	static {
		new GameDisposable() {
			@Override
			protected void dispose() {
				listeners.clear();
			}
		};
	}
	
	public static abstract class DWarListener {
		
		protected DWarListener() {
			listeners.add(this);
		}
		
		protected abstract void exe(Faction a, Faction b, boolean war);
		
	}
	
	public void set(Faction a, Faction b, boolean v) {
		if (a == b)
			throw new RuntimeException(a + " " + b);
		if (a == null || b == null)
			return;
		
		
		if (v) {
			if (is(a, b))
				return;
			add(a, b);
			add(b, a);
			WLogger.war(a, b, true);
			FACTIONS.DIP().trade(a, b, false);
			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).exe(a, b, true);
			}
		}else {
			if (!is(a, b))
				return;
			
			remove(a, b);
			remove(b, a);
			WLogger.war(a, b, false);
			if (a instanceof FactionNPC && b instanceof FactionNPC)
				FACTIONS.DIP().tradeSilent(a, b, true);
			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).exe(a, b, false);
			}
		}
		
	}
	
	public void clear(Faction a) {
		
		for (Faction b : getEnemies(a)) {
			remove(a, b);
			remove(b, a);
		}
		
		
	}
	
	private void add(Faction a, Faction b) {

		short[] es = enemies[a.index()];
		
		for (int i = 0; i < FACTIONS.MAX; i++) {
			if (es[i] == b.index()+1)
				throw new RuntimeException();
			if (es[i] == 0) {
				es[i] = (short) (b.index()+1);
				int k = a.index() + b.index()*FACTIONS.MAX;
				bits.set(k, true);
				return;
			}
		}
		throw new RuntimeException();
		
	}
	
	private void remove(Faction a, Faction b) {
		
		short[] es = enemies[a.index()];
		for (int i = 0; i < FACTIONS.MAX; i++) {
			if (es[i] == b.index()+1) {
				int ii = a.index() + b.index()*FACTIONS.MAX;
				bits.set(ii, false);
				
				for (int k = i+1; k < FACTIONS.MAX; k++) {
					es[k-1] = enemies[a.index()][k];
				}
				es[FACTIONS.MAX-1] = 0;
				return;
			}
			
		}
		for (int i = 0; i < FACTIONS.MAX; i++) {
			if (es[i] == b.index()+1) {
				throw new RuntimeException(i + " " + b.index());
			}
			
		}
		throw new RuntimeException();
	}
	
	public boolean is(Faction a, Faction b) {
		if (a == null || b == null)
			return a != b;
		return bits.get(a, b);
	}
	
	public boolean is(FactionNPC a) {
		return is(a, FACTIONS.player());
	}
	
	public LIST<Faction> getEnemies(Faction f){
		tmp.clearSloppy();
		short[] es = enemies[f.index()];
		for (int i = 0; i < FACTIONS.MAX; i++) {
			if (es[i] == 0) {
				break;
			}
			tmp.add(FACTIONS.getByIndex(es[i]-1));
		}
		return tmp;
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			bits.save(file);
			for (short[] ss : enemies)
				file.ss(ss);

		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			bits.load(file);
			for (short[] ss : enemies)
				file.ss(ss);
		}
		
		@Override
		public void clear() {
			bits.clear();
			for (short[] ss : enemies)
				Arrays.fill(ss, (short)0);
		}
	};
	
	public static double joinWarValue(Faction asker, Faction joiner) {
		
		double enemies = 0;
		
		for (Faction e : FACTIONS.DIP().war.getEnemies(asker)) {
			enemies += e.power();
		}
		
		double counter = asker.power() + joiner.power();
		
		double v = 0.1 + enemies / counter;
		return CLAMP.d(v, 0, 1);
	}
	
	public static double peaceValue(FactionNPC f) {
		
		
		double epow = f.power()*(1+ROpinions.attackValue(f));
		for (Faction e : FACTIONS.DIP().war.getEnemies(FACTIONS.player())) {
			if (e != f)
				epow += f.power();
		}
		
		epow *=  1+extraLeveragePower(f);
		double ppow = FACTIONS.player().power();
		for (Faction e : FACTIONS.DIP().war.getEnemies(f)) {
			if (e != FACTIONS.player())
				ppow += e.power()*0.5;
		}
		double v = ppow/(epow+ppow);
		
		return CLAMP.d(v-0.5, -1, 1);
		
	}
	
	public static double extraLeveragePower(FactionNPC underAttack) {
		
		double threat = 0;
		
		for (int ri = 0; ri < underAttack.realm().regions(); ri++) {
			Region reg = underAttack.realm().region(ri);
			double armies = 0;
			double besige = 0;
			for (WArmy a : WORLD.ENTITIES().armies.fill(underAttack.realm().region(ri))) {
				if (FACTIONS.DIP().war.is(a.faction(), underAttack)) {
					armies += AD.power().get(a);
					if (a.faction() == FACTIONS.player()) {
						besige = Math.max(besiegeB(a, reg), besige);
					}else {
						besige = Math.max(besiegeB(a, reg)*0.5, besige);
					}
				}else if (a.faction() == underAttack) {
					armies -= AD.power().get(a);
				}
			}
			armies -= RD.MILITARY().power.getD(reg);
			
			if (armies > 0) {
				besige *= reg.capitol() ? 2 : 1;
				threat += besige;
				
			}
		}
		
		threat /= underAttack.realm().regions();
		
		return threat;
			
			
		
	}
	
	private static double besiegeB(WArmy a, Region reg){
		return WORLD.BATTLES().besigedTime(reg)*AD.power().get(a)/(RD.MILITARY().power.getD(reg)*TIME.secondsPerDay*2);
	}
	
	public static boolean wantsToFightAgainstPlayer(FactionNPC f, double totalPower, double playerPower) {
		if (!FACTIONS.DIP().war.is(f, FACTIONS.player()) && FACTIONS.DIP().war.getEnemies(f).size() != 0)
			return false;
		if (FACTIONS.DIP().war.is(f, FACTIONS.player()))
			return true;
		double hate = ROpinions.attackValue(f)*2;
		double d = hate*totalPower/playerPower;
		return d > 0;
	}
	
}
