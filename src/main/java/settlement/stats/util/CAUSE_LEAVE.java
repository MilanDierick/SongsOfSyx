package settlement.stats.util;

import init.D;
import init.paths.PATHS;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import util.info.INFO;

public final class CAUSE_LEAVE extends INFO implements INDEXED{

	private static ArrayList<CAUSE_LEAVE> all = new ArrayList<>(30);
	private static ArrayList<CAUSE_LEAVE> deaths = new ArrayList<>(30);
	
	static {
		D.spush(CAUSE_LEAVE.class);
	}
	
	public static final CAUSE_LEAVE ARMY = new CAUSE_LEAVE(
			"ARMY",
			D.g("Army-Duty"),
			D.g("Army-Duties"),
			D.g("ArmyDutyD", "Subjects that have left your city to join distant armies."),
			false,
			false,
			false
			); 
	
	public static final CAUSE_LEAVE EMMIGRATED = new CAUSE_LEAVE(
			"EMMIGRATED",
			D.g("Emigrated"),
			D.g("Emigration"),
			D.g("EmmigratedD", "Subjects that have left your city."),
			false,
			false,
			false
			); 
	
	public static final CAUSE_LEAVE STARVED = new CAUSE_LEAVE(
			"STARVED",
			D.g("Starved"),
			D.g("Starvation"),
			D.g("StarvedD", "Subjects that have starved to death from lack of food."),
			true,
			false,
			true
			); 
	
	public static final CAUSE_LEAVE SACRIFICED = new CAUSE_LEAVE(
			"SACRIFICED",
			D.g("Sacrificed"),
			D.g("Sacrifices"),
			D.g("SacrificedD", "Subjects that have been sacrificed to the gods."),
			true,
			true,
			true
			); 
	
	public static final CAUSE_LEAVE SLAYED = new CAUSE_LEAVE(
			"SLAYED",
			D.g("Slain"),
			D.g("Slaying"),
			D.g("SlainD", "Subjects that have fallen in battle."),
			true,
			false,
			true
			); 

	public static final CAUSE_LEAVE ANIMAL = new CAUSE_LEAVE(
			"ANIMAL",
			D.g("Mauled"),
			D.g("Mauling"),
			D.g("AnimalsD", "Subjects that have been slain by wild beasts."),
			true,
			false,
			true
			); 
	
	public static final CAUSE_LEAVE AGE = new CAUSE_LEAVE(
			"AGE",
			D.g("Aging"),
			D.g("Age"),
			D.g("AgeD", "Subjects that have died naturally from old age."),
			true,
			true,
			true
			); 

	public static final CAUSE_LEAVE ACCIDENT = new CAUSE_LEAVE(
			"ACCIDENT",
			D.g("Accident"),
			D.g("Accidents"),
			D.g("AccidentD", "Subjects that have died from accidents."),
			true,
			false,
			true
			); 
	
	public static final CAUSE_LEAVE HEAT = new CAUSE_LEAVE(
			"HEAT",
			D.g("Heat"),
			D.g("Heat Exposure"),
			D.g("HeatD", "Subjects that have died from heat exposure. Build bodies of water to prevent this."),
			true,
			false,
			true
			); 
	
	public static final CAUSE_LEAVE COLD = new CAUSE_LEAVE(
			"COLD",
			D.g("Cold"),
			D.g("Cold Exposure"),
			D.g("ColdD", "Subjects that have frozen to death. Build hearths to avoid."),
			true,
			false,
			true
			); 
	
	public static final CAUSE_LEAVE MURDER = new CAUSE_LEAVE(
			"MURDER",
			D.g("Murdered"),
			D.g("Murders"),
			D.g("MurderD", "Subjects that have been murdered."),
			true,
			false,
			true
			); 
	
	public static final CAUSE_LEAVE DISEASE = new CAUSE_LEAVE(
			"DISEASE",
			D.g("Disease"),
			D.g("Diseases"),
			D.g("DiseaseD", "Subjects that have died from diseases."),
			true,
			false,
			true
			); 
	
	public static final CAUSE_LEAVE EXECUTED = new CAUSE_LEAVE(
			"EXECUTED",
			D.g("Executed"),
			D.g("Executions"),
			D.g("ExecutedD", "Subjects that have been executed."),
			true,
			false,
			true
			);
	
	public static final CAUSE_LEAVE PUNISHED = new CAUSE_LEAVE(
			"PUNISHED",
			D.g("Punished"),
			D.g("Punishment"),
			D.g("PunishmentD", "Subjects that have been turned into prisoners."),
			true,
			false,
			true
			); 
	
	public static final CAUSE_LEAVE DROWNED = new CAUSE_LEAVE(
			"DROWNED",
			D.g("Drowned"),
			D.g("Drownings"),
			D.g("DrownedD", "Subjects that have drowned."),
			true,
			false,
			false
			); 
	
	public static final CAUSE_LEAVE DESERTED = new CAUSE_LEAVE(
			"DESERTED",
			D.g("Deserted"),
			D.g("Desertion"),
			D.g("DesertedD", "Soldiers that have deserted."),
			true,
			false,
			false
			); 
	
	public static final CAUSE_LEAVE EXILED = new CAUSE_LEAVE(
			"EXILED",
			D.g("Exiled"),
			D.g("Exile"),
			D.g("ExileD", "People condemned to exile. The will are forced to leave the city and never come back."),
			true,
			false,
			false
			); 
	
	public static final CAUSE_LEAVE BRAWL = new CAUSE_LEAVE(
			"BRAWL",
			D.g("Brawl"),
			D.g("Brawls"),
			D.g("BrawlD", "Subjects that have died from a brawl that has gone too far. Try separating the homes of species that hate each other."),
			true,
			false,
			true
			); 
	
	public static final CAUSE_LEAVE OTHER = new CAUSE_LEAVE(
			"OTHER",
			D.g("Other"),
			D.g("Others"),
			D.g("OtherD", "Other causes."),
			true,
			false,
			false
			); 
	


	static {
		all = new ArrayList<>(all);
		deaths = new ArrayList<>(deaths);
		Json json = new Json(PATHS.CONFIG().get("LEAVE_CAUSE"));
		for (CAUSE_LEAVE l : all) {
			l.defAgony = json.d(l.key, 0, 10);
		}
		D.spop();
	}
	
	public static LIST<CAUSE_LEAVE> ALL(){
		return all;
	}
	
	public static LIST<CAUSE_LEAVE> DEATHS(){
		return deaths;
	}
	
	public final String key;
	public final boolean death,natural,leavesCorpse;
	private final int index;
	public final int indexDeath;
	private double defAgony;
	
	
	private CAUSE_LEAVE(String key, CharSequence name, CharSequence names, CharSequence desc, boolean death, boolean natural, boolean leavesCorpse) {
		super(name, names, desc, null);
		this.key = key;
		this.death = death;
		this.natural = natural;
		this.leavesCorpse = leavesCorpse;
		index = all.add(this);
		if (death)
			indexDeath = deaths.add(this);
		else
			indexDeath = -1;
	}

	@Override
	public int index() {
		return index;
	}
	
	public double defaultStanding() {
		return defAgony;
	}
	
	
}
