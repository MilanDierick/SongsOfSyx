package world.map.buildings.camp;

import game.faction.FACTIONS;
import game.statistics.G_REQ;
import game.time.TIME;
import init.D;
import snake2d.util.sprite.text.Str;
import util.updating.IUpdater;
import view.main.MessageText;
import world.World;

final class Checker extends IUpdater{

	public Checker() {
		super(WorldCamp.MAX, 10);
	}
	
	private double wait = 0;
	
	private static CharSequence ¤¤titleJoin = "Haven joins your cause.";
	private static CharSequence ¤¤bodyJoin = "{0}, a haven of {1} {2} have agreed to join your cause. They will now start immigrating to your capitol at a steady pace. Make sure you allow them in.";
	private static CharSequence ¤¤titleLeave = "Haven quits corporation!.";
	private static CharSequence ¤¤bodyLeave = "Since you've failed to uphold the standards of the haven: {0}, it has now stopped supporting you, and its {1} members will start to return home.";
	private final Str s = Str.TMP;
	
	static {
		D.ts(Checker.class);
	}
	
	@Override
	protected void update(int i, double timeSinceLast) {
		
		if (wait > TIME.currentSecond())
			return;
			
		
		WCampInstance ii = World.camps().all().get(i);
		if (ii == null)
			return;
		
		if (ii.faction() == FACTIONS.player()) {
			for (G_REQ r : ii.type().rmin) {
				if (!r.isFulfilled()) {
					wait = TIME.currentSecond()+60;
					ii.factionSet(null);
					
					s.clear().add(¤¤bodyLeave).insert(0, ii.name).insert(1, ii.max);
					new MessageText(¤¤titleLeave, s).send();
				}
					
			}
			
			
		}else if (ii.faction() == null && ii.regionFacton() == FACTIONS.player()) {
			for (G_REQ r : ii.reqs()) {
				if (!r.isFulfilled())
					return;
			}
			
			ii.factionSet(FACTIONS.player());
			s.clear().add(¤¤bodyJoin).insert(0, ii.name).insert(1, ii.max).insert(2, ii.race().info.names);
			new MessageText(¤¤titleJoin, s).send();
			wait = TIME.currentSecond()+10;
			
		}
		
	}
	
}
