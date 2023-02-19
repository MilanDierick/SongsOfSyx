package game.battle;

import java.io.IOException;

import game.GAME;
import game.battle.Resolver.SideResult;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayListShortResize;
import world.World;
import world.entity.army.WArmy;

final class PollerFieldBattles implements Poller{

	private final ArrayListShortResize armies = new ArrayListShortResize(16, 1024);
	private final PromptFieldBattle prompt;
	private final Conflict conflict;
	
	public PollerFieldBattles(Conflict conflict, PromptUtil util) {
		this.conflict = conflict;
		prompt = new PromptFieldBattle(conflict, util);
	}
			
	@Override
	public Prompt poll() {
		if (armies.size() == 0)
			return null;
		
		while(!armies.isEmpty()) {
			WArmy r = World.ENTITIES().armies.get(armies.getLast());
			
			if (r != null && r.added() &&  conflict.make(r)) {
				if (conflict.isPlayer()) {
					
					prompt.activivate();
					return prompt;
				}else {
					Resolver.resolveAI(conflict);
					armies.remove(armies.size()-1);
				}
			}else {
				armies.remove(armies.size()-1);
			}
			
		}
		
		return null;
		
	}
	
	public Prompt make(Conflict conflict) {
		if (conflict.isPlayer()) {
			prompt.activivate();
			return prompt;
		}else {
			Resolver.resolveAI(conflict);
		}
		return null;
	}
	
	public void reportArmyMovement(Conflict conflict, WArmy a) {
		if (conflict.test(a))
			armies.add(a.armyIndex());
	}

	@Override
	public void save(FilePutter file) {
		armies.save(file);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		armies.load(file);
		
	}

	@Override
	public void clear() {
		armies.clear();
	}
	
	public Prompt resolve(Resolver.PlayerBattle res) {
		SideResult a = res.apply(conflict.sideA);
		SideResult b =  res.apply(conflict.sideB);
		return new PromptResult(conflict, a, b, GAME.battle().ui);
	}

}
