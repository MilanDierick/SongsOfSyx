package game.battle;

import java.io.IOException;

import game.GAME.GameResource;
import game.time.TIME;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import view.main.VIEW;
import world.army.WARMYD;
import world.entity.army.WArmy;
import world.map.regions.REGIOND;
import world.map.regions.Region;

public final class BATTLE extends GameResource{

	
	private final Conflict conflict = new Conflict();
	final PromptUtil ui = new PromptUtil();
	final PollerFieldBattles pollField = new PollerFieldBattles(conflict, ui);
	final PollerSieges pollSiege = new PollerSieges(conflict, ui);
	Prompt promptt = null;
	
	public BATTLE() {
		super(true);
	}
	
	@Override
	protected void save(FilePutter file) {
		pollField.save(file);
		pollSiege.save(file);
		conflict.saver.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		pollField.load(file);
		pollSiege.load(file);
		conflict.saver.load(file);
	}

	@Override
	protected void update(float ds) {

		
	}
	
	public void reportArmyMovement(WArmy a) {
		pollField.reportArmyMovement(conflict, a);
	}
	
	public void besiegeFirst(WArmy a, Region r, double timer) {
		pollSiege.besiegeFirst(a, r, timer);
		
	}
	
	public void besiegeContinous(WArmy a, Region r, double timer) {
		pollSiege.besiegeContinous(a, r, timer);
	}
	
	public void attack(Region r, WArmy a) {
		if (conflict.make(r, a)) {
			pollField.make(conflict);
		}
	}

	public boolean poll() {
		
		if (VIEW.b().isActive())
			return false;
		
		if (promptt != null && promptt.isActive())
			return true;
		
		promptt = pollField.poll();
		
		if (promptt != null)
			return true;
		
		promptt = pollSiege.poll();
		
		if (promptt != null)
			return true;

		return false;

	}
	
	public boolean canSave() {
		return !VIEW.b().isActive() && (promptt == null || promptt.canSave());
	}
	
	public static boolean regionCanHoldOut(Region r, WArmy besieger, double time) {
		double mul = TIME.years().bitSeconds()/(2*time+1)-1;
		mul = CLAMP.d(mul, 0, 50);
		mul *= REGIOND.MILITARY().power.get(r);
		double a = WARMYD.quality().get(besieger);
		return a > mul/2;
	}

}
