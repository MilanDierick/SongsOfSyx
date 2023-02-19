package game.faction.npc;

import game.faction.FCredits;
import game.time.TIMECYCLE;

public class NPCCredits extends FCredits{

	private final FactionNPC faction;
	
	public NPCCredits(FactionNPC faction, int saved, TIMECYCLE time) {
		super(saved, time);
		this.faction = faction;
	}
	
	public double trueCredits() {
		return faction.buyer().credits();
	}

}
