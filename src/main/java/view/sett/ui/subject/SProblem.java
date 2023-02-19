package view.sett.ui.subject;

import game.GAME;
import init.D;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.STATS;
import snake2d.util.sprite.text.Str;

final class SProblem{
	
	
	private static CharSequence ¤¤ProblemSick = "¤Currently ill with {0}";
	private static CharSequence ¤¤ProblemInjured = "¤Bleeding badly";
	private static CharSequence ¤¤Starving = "¤Starving to death.";
	private static CharSequence ¤¤CutOff = "¤Cut off from the throne.";
	private static CharSequence ¤¤Exposed = "¤Exposed to the temperature!";
	private static CharSequence ¤¤OnStrike = "¤On Strike, refuses to work";
	
	private static CharSequence ¤¤ProbLeisure = "¤Leisure Time.";
	
	
	static {
		D.ts(SProblem.class);
	}

	public static CharSequence problem(Humanoid a) {
		
		if (STATS.NEEDS().disease.getter.get(a.indu()) != null) {
			return Str.TMP.clear().add(¤¤ProblemSick).insert(0, STATS.NEEDS().disease.getter.get(a.indu()).info.name);
		}
		if (STATS.NEEDS().INJURIES.inDanger(a.indu())) {
			return ¤¤ProblemInjured;
		}
		if (STATS.NEEDS().EXPOSURE.inDanger(a.indu()))
			return ¤¤Exposed;
		if (STATS.FOOD().STARVATION.indu().get(a.indu()) > 0)
			return ¤¤Starving;
		if (STATS.POP().TRAPPED.indu().get(a.indu()) > 0)
			return ¤¤CutOff;
		
		
		return null;
		
	}
	
	public static CharSequence warning(Humanoid a) {
		if (STATS.WORK().WORK_TIME.indu().isMax(a.indu())) {
			return ¤¤ProbLeisure;
		}
		if (GAME.events().riot.onStrike(a))
			return ¤¤OnStrike;
		return null;
	}
	
}
