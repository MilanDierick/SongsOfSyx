package view.ui.tech;

import java.util.Arrays;

import game.faction.FACTIONS;
import init.D;
import init.tech.TECH;
import init.tech.TECH.TechRequirement;
import init.tech.TECHS;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Str;
import view.main.VIEW;

final class Prompt {

	private static CharSequence ¤¤Unlock = "¤Do you wish to allocate {0} knowledge and unlock the following: ";
	private static CharSequence ¤¤Forget = "¤Do you wish to forget the following technologies:";
	
	private double renderS = VIEW.renderSecond()-60;
	private boolean[] checks = new boolean[TECHS.ALL().size()];
	private double checkFoget = VIEW.renderSecond();
	
	static {
		D.ts(Prompt.class);
	}
	
	public void unlock(TECH tech){
		if (FACTIONS.player().tech().level(tech) == tech.levelMax)
			return;
		int costR = FACTIONS.player().tech().costOfNextWithRequired(tech);
		if (costR > FACTIONS.player().tech().available().get() || !tech.lockable.passes(FACTIONS.player()))
			return;
		
		if (costR > FACTIONS.player().tech().costLevelNext(tech) || VIEW.renderSecond()>renderS) {
			Str s = Str.TMP;
			s.clear();
			s.add(¤¤Unlock);
			s.insert(0, costR);
			s.NL();
			Arrays.fill(checks, false);
			addUnlocks(tech, s);
			this.tech = tech;
			VIEW.inters().yesNo.activate(s, askUnlock, askNo, true);
			renderS = VIEW.renderSecond()+120;
		}else
			pUnlock(tech, FACTIONS.player().tech().level(tech)+1);
	}
	
	private void addUnlocks(TECH tech, Str s) {
		if (checks[tech.index()])
			return;
		checks[tech.index()] = true;
		s.add(tech.info.name);
		s.NL();
		for (int i = 0; i < tech.requires().size(); i++) {
			TechRequirement r = tech.requires().get(i);
			if (FACTIONS.player().tech().level(r.tech)< r.level) {
				s.add(r.tech.info.name);
				if (r.tech.levelMax > 1)
					s.s().add(r.level);
				s.NL();
			}
		}
		
	}
	
	private void pUnlock(TECH tech, int level) {
		for (int i = 0; i < tech.requires().size(); i++) {
			TechRequirement r = tech.requires().get(i);
			if (FACTIONS.player().tech().level(r.tech)< r.level)
				pUnlock(r.tech, r.level);
			
		}
		FACTIONS.player().tech().levelSet(tech, level);
	}
	
	public void forget(TECH tech) {
		int l = FACTIONS.player().tech().level(tech);
		if (l == 0)
			return;
		
		int am = 1;
		Str s = Str.TMP;
		s.clear();
		s.add(¤¤Forget);
		s.NL();
		s.add(tech.info.name);
		s.NL();
		for (int ti = 0; ti < TECHS.ALL().size(); ti++) {
			TECH t = TECHS.ALL().get(ti);
			if (t != tech && t.requires(tech, l-1) && FACTIONS.player().tech().level(t) > 0) {
				
				s.add(t.info.name);
				s.NL();
				am++;
			}
		}
		this.tech = tech;
		
		if (am > 2 || VIEW.renderSecond()-checkFoget > 60) {
			VIEW.inters().yesNo.activate(s, askforget, askNo, true);
			checkFoget = VIEW.renderSecond();
		}else {
			askforget.exe();
		}
	}
	
	private TECH tech;
	
	private ACTION askforget = new ACTION() {
		
		@Override
		public void exe() {
			int l = FACTIONS.player().tech().level(tech);
			for (int ti = 0; ti < TECHS.ALL().size(); ti++) {
				TECH t = TECHS.ALL().get(ti);
				if (t != tech && t.requires(tech, l-1)) {
					FACTIONS.player().tech().levelSet(t, 0);
				}
				
			}
			FACTIONS.player().tech().levelSet(tech, l-1);
		}
	};
	
	private ACTION askUnlock = new ACTION() {
		
		@Override
		public void exe() {
			pUnlock(tech, FACTIONS.player().tech().level(tech)+1);
		}
	};
	
	private ACTION askNo = new ACTION() {
		
		@Override
		public void exe() {
			
		}
	};
	
}
