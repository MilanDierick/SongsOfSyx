package settlement.army;

import static settlement.main.SETT.*;

import java.io.IOException;
import java.util.Arrays;

import init.config.Config;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsBattle.StatTraining;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayList;
import world.World;

public final class ArmyTrainingInfo {

	private final int[] soldiersTarget = new int[RACES.all().size()];
	
	final RaceDiv[] perRace = new RaceDiv[RACES.all().size()];
	private int raceU = 0;
	
	ArmyTrainingInfo(){

		
		for (int i = 0; i < perRace.length; i++)
			perRace[i] = new RaceDiv(RACES.all().get(i));
	}
	
	public boolean updateAndShouldTrain(Humanoid a, boolean training) {
		
		if (STATS.BATTLE().DIV.get(a) != null) {
			return updateExisting(a, training);
		}else if(STATS.BATTLE().RECRUIT.get(a) != null) {
			return updateRecruit(a, training);
		}else {
			return setNew(a, training);
		}

	}
	
	public boolean updateDiv(Humanoid a, boolean training) {
		
		if (STATS.BATTLE().DIV.get(a) == null) {
			return false;
		}
		
		if (!canStayInDiv(a, STATS.BATTLE().DIV.get(a), training)) {
			STATS.BATTLE().DIV.set(a, null);
			return false;
		}
		return true;

	}
	
	public boolean shouldBarracks(Humanoid a, boolean training) {
		
		Div div = STATS.BATTLE().DIV.get(a);
		if (div == null)
			return true;
		return shouldTrain(div, STATS.BATTLE().TRAINING_MELEE, a, training, div.info.training.toStat());
	}
	
	public boolean shouldArchery(Humanoid a, boolean training) {
		
		Div div = STATS.BATTLE().DIV.get(a);
		if (div == null)
			return true;
		
		return shouldTrain(div, STATS.BATTLE().TRAINING_ARCHERY, a, training, div.info.trainingR.toStat());
		
	}
	
	private boolean shouldTrain(Div div, StatTraining s, Humanoid a, boolean training, double ta) {
		return s.shouldTrain(a.indu(), ta, training);
	}
	
	private boolean shouldTrain(Div match, Humanoid a, boolean training) {
		return shouldTrain(match, STATS.BATTLE().TRAINING_MELEE, a, training, match.info.training.toStat()) ||
				shouldTrain(match, STATS.BATTLE().TRAINING_ARCHERY, a, training, match.info.trainingR.toStat())
				;
	}
	
	public boolean shouldTrain(Humanoid a, boolean training) {
		Div div = STATS.BATTLE().DIV.get(a);
		if (div == null)
			return false;
		return shouldTrain(div, a, training);
	}
	
	void incTarget(Race r, int amount) {
		this.soldiersTarget[r.index] += amount;
	}
	
	public int targetMen(Race race) {
		if (race == null) {
			int am = 0;
			for (int i : soldiersTarget)
				am+= i;
			return am;
		}
		
		return soldiersTarget[race.index];
	}
	
	public int targetMen() {
		int am = 0;
		for (int i : soldiersTarget)
			am+= i;
		return am;
	}
	
	private boolean updateExisting(Humanoid a, boolean training) {
		
		Div div = STATS.BATTLE().DIV.get(a);
		
		if (!canStayInDiv(a, div, false)) {
			STATS.BATTLE().DIV.set(a, null);
			return setNew(a, training);
		}
		
		if (perRace[a.race().index].tryBetter(a.indu(), div)) {
			Div match = perRace[a.race().index].getMatch(a.indu());
			if (match != null && match != div) {
				STATS.BATTLE().DIV.set(a, match);
				div = match;
				
			}
		}

		return shouldTrain(div, a, training);
	}
	
	private boolean updateRecruit(Humanoid a, boolean training) {
		Div div = STATS.BATTLE().RECRUIT.get(a);
		
		if (!canStayInDiv(a, div, true)) {
			STATS.BATTLE().RECRUIT.set(a, null);
			return setNew(a, training);
		}
		
		int tr = STATS.BATTLE().TRAINING_MELEE.indu().get(a.indu());
		
		if (tr >= 1) {
			STATS.BATTLE().RECRUIT.set(a, null);
			if (canJoinDiv(a, div, false))
				STATS.BATTLE().DIV.set(a, div);
			return shouldTrain(div, a, training);
		}
	
		return true;
	}
	
	private boolean thereAreDivsToSignUpTo(Race race) {
		int am = (int) World.ARMIES().cityDivs().total(race) + (STATS.BATTLE().DIV.stat().data(null).get(race, 0) + STATS.BATTLE().RECRUIT.stat().data(null).get(race, 0));
		return (am < soldiersTarget[race.index]);
	}

	
	private boolean setNew(Humanoid a, boolean training) {
		
		
		
		if (!thereAreDivsToSignUpTo(a.race())) {
			return false;
		}
		
		if (perRace[a.race().index].has(a.indu())) {
			Div match = perRace[a.race().index].getMatch(a.indu());
			if (match != null) {
				int tr = STATS.BATTLE().TRAINING_MELEE.indu().get(a.indu());
				if (tr >= 1) {
					STATS.BATTLE().DIV.set(a, match);
					return shouldTrain(match, a, training);
				}else {
					STATS.BATTLE().RECRUIT.set(a, match);
					return true;
				}
			}
		}
		return false;
	}
	

	
	private boolean canStayInDiv(Humanoid a, Div div, boolean recruit) {
		DivInfo in = div.info;
		if (STATS.BATTLE().COMBAT_EXPERIENCE.indu().get(a.indu()) < in.experienceT.get())
			return false;
		if (a.race() != in.race())
			return false;
		if (recruit && World.ARMIES().cityDivs().get(div).men() + STATS.BATTLE().DIV.stat().div().get(div) + STATS.BATTLE().RECRUIT.inDiv(div) > in.men.get())
			return false;
		if (World.ARMIES().cityDivs().get(div).men() + STATS.BATTLE().DIV.stat().div().get(div) > in.men.get())
			return false;
		return true;
	}
	
	private boolean canJoinDiv(Humanoid a, Div div, boolean recruit) {
		DivInfo in = div.info;
		if (STATS.BATTLE().COMBAT_EXPERIENCE.indu().get(a.indu()) < in.experienceT.get())
			return false;
		if (a.race() != in.race())
			return false;
		if (STATS.BATTLE().DIV.stat().div().get(div) >= in.men.get())
			return false;
		if (recruit && World.ARMIES().cityDivs().get(div).men() + STATS.BATTLE().DIV.stat().div().get(div) + STATS.BATTLE().RECRUIT.inDiv(div) >= in.men.get())
			return false;
		return true;
	}
	

	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.is(soldiersTarget);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			file.is(soldiersTarget);

			
			
			for (int i = 0; i <= Config.BATTLE.DIVISIONS_PER_ARMY; i++)
				update();
		}
		
		@Override
		public void clear() {
			Arrays.fill(soldiersTarget, 0);

			for (int i = 0; i < 4; i++) {
				SETT.ARMIES().division((short) i).info.men.set(50);
			}
			
			for (int i = 0; i <= perRace.length; i++)
				update();
		}
	};

	public void update() {

		
		if (raceU == Config.BATTLE.DIVISIONS_PER_ARMY) {
			for (RaceDiv d : perRace)
				d.update();
			raceU = 0;
			return;
		}
		
		Div d = ARMIES().player().divisions().get(raceU);
		DivInfo in = d.info;
		perRace[in.race().index].update(d, in);
		
		raceU++;
	}
	
	private static class RaceDiv {
		
		private final Race race;
		private ArrayList<Div> divs = new ArrayList<>(16);
		
		RaceDiv(Race race){
			this.race = race;
		}
		
		public Div getMatch(Induvidual in) {
			
			return getNext();
			
		}
		
		private Div getNext() {
			while(!divs.isEmpty()) {
				Div div = divs.get(divs.size()-1);
				if (div.info.race() == race) {
					int am = div.info.men.get() - (World.ARMIES().cityDivs().get(div).men() + STATS.BATTLE().DIV.stat().div().get(div) + STATS.BATTLE().RECRUIT.inDiv(div));
					if (am > 0)
						return div;
					
				}
				divs.removeLast();
			}
			return null;
		}
		
		public boolean has(Induvidual i) {
			return getNext() != null;
				
		}
		
		public boolean tryBetter(Induvidual i, Div div) {
			return false;
				
		}
		
		void update() {
			divs.clearSloppy();
		}
		
		void update(Div div, DivInfo in) {
			
			if (!divs.hasRoom())
				return;
			int am = in.men.get() - (World.ARMIES().cityDivs().get(div).men() + STATS.BATTLE().DIV.stat().div().get(div) + STATS.BATTLE().RECRUIT.inDiv(div));
			if (am > 0) {
				divs.add(div);
			}
			
		}
		
	}

	
}
