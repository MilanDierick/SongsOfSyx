package settlement.army;

import static settlement.main.SETT.*;

import java.io.IOException;
import java.util.Arrays;

import init.config.Config;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.military.training.ROOM_M_TRAINER;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayList;
import world.WORLD;

public final class ArmyTrainingInfo {

	private final int[] soldiersTarget = new int[RACES.all().size()];
	
	final RaceDiv[] perRace = new RaceDiv[RACES.all().size()];
	private int raceU = 0;
	
	ArmyTrainingInfo(){
		for (int i = 0; i < perRace.length; i++)
			perRace[i] = new RaceDiv(RACES.all().get(i));
	}
	
	public ROOM_M_TRAINER<?> updateAndGetEmployment(Humanoid a, ROOM_M_TRAINER<?> current){
		
		
		Div div = updateExisting(a);
		
		if (div == null)
			return null;
		
		DivInfo in = div.info;
		if (current != null && current.employable() >= 0 && current.training().shouldTrain(a.indu(), in.trainingD(current).getD(), true)) {
			return current;
		}
		
		current = employmentTarget(a, div, current != null);
		if (current != null)
			return current;
		STATS.BATTLE().RECRUIT.set(a, null);
		return null;
		
	}
	
	private ROOM_M_TRAINER<?> employmentTarget(Humanoid a, Div div, boolean training){

		
		
		
		double bestV = 0;
		ROOM_M_TRAINER<?> best = null;
		DivInfo in = div.info;
		for (ROOM_M_TRAINER<?> tra : ROOM_M_TRAINER.ALL()) {
			
			double emp = tra.employable();
			
			
			
			if (emp >= 1) {
				if (tra.training().shouldTrain(a.indu(), in.trainingD(tra).getD(), training)) {
					return tra;
				}else if (emp > bestV) {
					bestV = emp;
					best = tra;
				}
			}
			
		}
		
		if (!STATS.BATTLE().basicTraining.isMax(a.indu()))
			return best;
		return null;
	}
	
	private Div updateExisting(Humanoid a) {
		
		Div div = STATS.BATTLE().DIV.get(a);
		
		if (div != null) {
			if (!canStayInDiv(a, div, false)) {
				STATS.BATTLE().DIV.set(a, null);
				return setNew(a);
			}
			
			if (perRace[a.race().index].tryBetter(a.indu(), div)) {
				Div match = perRace[a.race().index].getMatch(a.indu());
				if (match != null && match != div) {
					STATS.BATTLE().DIV.set(a, match);
					div = match;
					
				}
			}
			return div;
		}
		
		div = STATS.BATTLE().RECRUIT.get(a);
		if (div != null) {
			
			if (!canStayInDiv(a, div, true)) {
				STATS.BATTLE().RECRUIT.set(a, null);
				return setNew(a);
			}
			
			if (STATS.BATTLE().basicTraining.isMax(a.indu())) {
				STATS.BATTLE().RECRUIT.set(a, null);
				STATS.BATTLE().DIV.set(a, div);
				return div;
			}
			return div;
		}
		
		return setNew(a);

	}
	
	public boolean shouldJoinArmy(Humanoid a) {
	
		Div div = STATS.BATTLE().DIV.get(a);
		if (div == null)
			return false;
		if (WORLD.ARMIES().cityDivs().attachedArmy(STATS.BATTLE().DIV.get(a)) == null) {
			return false;
		}
		if (!SETT.ENTRY().points.hasAny())
			return false;
		
		return employmentTarget(a, div, false) == null;
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
	

	
	private boolean thereAreDivsToSignUpTo(Race race) {
		int am = (int) WORLD.ARMIES().cityDivs().total(race) + (STATS.BATTLE().DIV.stat().data(null).get(race, 0) + STATS.BATTLE().RECRUIT.stat().data(null).get(race, 0));
		return (am < soldiersTarget[race.index]);
	}

	
	private Div setNew(Humanoid a) {
		
		if (!thereAreDivsToSignUpTo(a.race())) {
			return null;
		}
		
		if (perRace[a.race().index].has(a.indu())) {
			Div match = perRace[a.race().index].getMatch(a.indu());
			if (match != null) {
				if (STATS.BATTLE().basicTraining.isMax(a.indu())) {
					STATS.BATTLE().DIV.set(a, match);
					return match;
				}else {
					STATS.BATTLE().RECRUIT.set(a, match);
					return match;
				}
			}
			
		}
		return null;
	}
	

	
	private boolean canStayInDiv(Humanoid a, Div div, boolean recruit) {
		DivInfo in = div.info;
		if (STATS.BATTLE().COMBAT_EXPERIENCE.indu().get(a.indu()) < in.experienceT.get())
			return false;
		if (a.race() != in.race())
			return false;
		if (recruit && WORLD.ARMIES().cityDivs().get(div).men() + STATS.BATTLE().DIV.stat().div().get(div) + STATS.BATTLE().RECRUIT.inDiv(div) > in.men.get())
			return false;
		if (!recruit && !STATS.BATTLE().basicTraining.isMax(a.indu()))
			return false;
		if (WORLD.ARMIES().cityDivs().get(div).men() + STATS.BATTLE().DIV.stat().div().get(div) > in.men.get())
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
					int am = div.info.men.get() - (WORLD.ARMIES().cityDivs().get(div).men() + STATS.BATTLE().DIV.stat().div().get(div) + STATS.BATTLE().RECRUIT.inDiv(div));
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
			int am = in.men.get() - (WORLD.ARMIES().cityDivs().get(div).men() + STATS.BATTLE().DIV.stat().div().get(div) + STATS.BATTLE().RECRUIT.inDiv(div));
			if (am > 0) {
				divs.add(div);
			}
			
		}
		
	}

	
}
