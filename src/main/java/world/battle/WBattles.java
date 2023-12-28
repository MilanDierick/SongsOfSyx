package world.battle;

import java.io.IOException;

import game.Profiler;
import game.faction.FACTIONS;
import init.D;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sprite.text.Str;
import view.main.VIEW;
import view.ui.message.MessageText;
import world.WORLD;
import world.WORLD.WorldResource;
import world.battle.PollSieges.Siege;
import world.entity.army.WArmy;
import world.regions.Region;

public final class WBattles extends WorldResource{

	public static final double retreatPenalty = 0.1;
	private final PollSieges siege;
	private final PollBattles battles;
	private final Rnd rnd = new Rnd();
	
	private boolean hasMessagedFirst = false;
	
	private static CharSequence ¤¤firstTitle = "Foreign army spotted!";
	private static CharSequence ¤¤firstDesc = "Scouts report that an foreign army is trespassing in the region of {0}. It might be and attack. You can use a region's garrison to attack any army within its borders.";
	
	static {
		D.ts(WBattles.class);
	}
	
	private int specialAttackAI = -1;

	private final AA_Creator creator;
	
	public WBattles() {
		rnd.clear();
		SideUnitFactory factory = new SideUnitFactory();
		creator =  new AA_Creator(factory, new PUnitFactory());
		siege = new PollSieges();
		battles = new PollBattles();
		
		new Tests();
	}
	
	@Override
	public void save(FilePutter file) {
		siege.save(file);
		battles.save(file);
		rnd.save(file);
		file.bool(hasMessagedFirst);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		siege.load(file);
		battles.load(file);
		rnd.load(file);
		specialAttackAI = -1;
		hasMessagedFirst = file.bool();
	}


	@Override
	public void update(float ds, Profiler prof) {
		prof.logStart(this);
		siege.update(ds);
		prof.logEnd(this);
	}
	
	public void poll() {
		
		if (!canPoll())
			return;
		
		
		int death = 10000;
		
		
		while(canPoll()) {
			
			
			
			WArmy a = battles.poll();
			if (a == null) {
				break;
			}
			
			if (creator.battle(a))
				continue;
			
			if (a.region() != null) {
				if (a.region().faction() == FACTIONS.player() && a.faction() != FACTIONS.player()) {
					if (!hasMessagedFirst) {
						new MessageText(Str.TMP.clear().add(¤¤firstTitle).insert(0, a.region().info.name())).paragraph(Str.TMP.clear().add(¤¤firstDesc).insert(0, a.region().info.name())).send();
						hasMessagedFirst = true;
						
					}
					
				}else if (creator.regAttack(a.region(), a))
					continue;
			}
			
			battles.skip();
			
		}
		
		if (canPoll() && specialAttackAI != -1) {
			WArmy a = WORLD.ENTITIES().armies.get(specialAttackAI);
			specialAttackAI = -1;
			if (a != null) {
				if (a.besieging() != null && a.besieging().faction() == FACTIONS.player())
					creator.regAttack(a.besieging(), a);
				else if (a.region() != null && a.region().faction() == FACTIONS.player())
					creator.regAttack(a.region(), a);
			}
		}
		
		while(canPoll()) {
			
			if (death -- < 0)
				throw new RuntimeException();
			
			Siege si = siege.next();
			if (si == null)
				break;
			creator.siege(si.besieger, si.reg, si.time);
			if (canPoll() || si.besieger.faction() == FACTIONS.player())
				siege.skip();
		}
		
	}
	
	
	
	private boolean canPoll() {
		return !VIEW.b().isActive() && !VIEW.world().UI.battle.isBusty();
	}

	
	public double besigedTime(Region reg) {
		return siege.besigedTime(reg);
	}
	
	public boolean besiged(Region reg) {
		return siege.besiged(reg);
	}
	
	public void besige(WArmy a, Region reg) {
		siege.besige(a, reg);
	}
	
	public void reportActivity(WArmy a) {
		battles.add(a);
	}
	
	public void regAttack(Region reg, WArmy a) {
		specialAttackAI = a.armyIndex();
	}
	
	

}
