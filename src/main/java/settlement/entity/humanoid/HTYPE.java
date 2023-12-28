package settlement.entity.humanoid;

import init.D;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.sets.*;
import util.dic.DicMisc;
import util.info.INFO;

public final class HTYPE extends INFO implements INDEXED {
	
	private static final LinkedList<HTYPE> all = new LinkedList<>();
	
	static{
		if (HCLASS.CITIZEN.player)
			;
		D.gInit(HTYPE.class);
	}
	
	public final static HTYPE SUBJECT = new HTYPE("CITIZEN",
			HCLASS.CITIZEN,
			D.g("Citizen"), D.g("Citizens"), 
			D.g("CitizenD", "Citizens are the bulk of your population and will carry out your wishes."),
			true, true, false, new ColorImp(3,1,19));
	public final static HTYPE RETIREE = new HTYPE("RETIREE",
			HCLASS.CITIZEN,
			D.g("Retiree"), D.g("Retirees"), 
			D.g("RetireeD", "Retired people are citizens that have served you for many years and are now entitled to some relaxation their final years. They do not work."),
			true, false, false, new ColorImp(8,8,16));
	public final static HTYPE RECRUIT = new HTYPE("RECRUIT",
			HCLASS.CITIZEN,
			D.g("Recruit"), D.g("Recruits"), 
			D.g("RecruitD", "Recruits are citizens either training their combat skills for a place in a division, or honing these skills towards the limit you've set for said division."),
			true, false, false, new ColorImp(20,8,16));
	public final static HTYPE STUDENT = new HTYPE("STUDENT",
			HCLASS.CITIZEN,
			D.g("Student"), D.g("Students"), 
			D.g("StudentD", "Students are citizens currently attending university. They do not count towards your workforce."),
			true, false, false, new ColorImp(20,8,16));
	public final static HTYPE PRISONER = new HTYPE("PRISONER",
			HCLASS.OTHER,
			D.g("Prisoner"), D.g("Prisoners"), 
			D.g("PrisonerD", "Prisoners are caught criminals, or POWs. Prisoners will spend their time in your dungeons. They can be used as sacrifices in temples, or gladiators. They can also be enslaved, or executed."),
			false, false, false, new ColorImp(20,20,8));
	public final static HTYPE TOURIST = new HTYPE("TOURIST",
			HCLASS.OTHER,
			DicMisc.¤¤Tourist, DicMisc.¤¤Tourists, 
			D.g("TouristD", "Tourists are foreigners visiting your city in search of a spectacle. Treat them well, and they will show their appreciation by tossing you some coins."),
			false, false, false, new ColorImp(20,20,8));
	
	public final static HTYPE SOLDIER = new HTYPE("SOLDIER",
			HCLASS.CITIZEN,
			D.g("Soldier"), D.g("Soldiers"), 
			D.g("SoldierD", "Soldiers are men on the battlefield."),
			true, false, false, false, new ColorImp(3,1,19));
	public final static HTYPE ENEMY = new HTYPE("ENEMY",
			HCLASS.OTHER,
			D.g("Enemy"), D.g("Enemies"),
			D.g("EnemyD", "Enemies are hostile peoples, bend on destroying your rule"),
			false, false, true, new ColorImp(30,1,1));
	public final static HTYPE RIOTER = new HTYPE("RIOTER",
			HCLASS.OTHER,
			D.g("Rioter"), D.g("Rioters"),
			D.g("RioterD", "Rioters are former citizens, who have had enough of your rule and express their disappointment by burning your city to ashes."),
			false, false, true, new ColorImp(30,1,1));
	public final static HTYPE DERANGED = new HTYPE("DERANGED",
			HCLASS.OTHER,
			D.g("Deranged"), D.g("Derangeds", "Deranged"),
			D.g("DerangedD", "Deranged are people who have gone insane. They will do no work, and wander around your city doing erratic things. Can be cured in an asylum."),
			false, false, false, new ColorImp(30,30,1));
	public final static HTYPE NOBILITY = new HTYPE("NOBILITY",
			HCLASS.NOBLE,
			D.g("Nobility"), D.g("Nobles"),
			D.g("NobilityD", "The nobility are above the common plebs. Do not work in a traditional sense and require the best of services."),
			true, false, false, new ColorImp(20, 8, 20));
	public final static HTYPE SLAVE = new HTYPE("SLAVE",
			HCLASS.SLAVE,
			D.g("Slave"), D.g("Slaves"), 
			D.g("SlaveD", "Slaves do mundane and hard work, but asks for very little in return. They can not be trained into soldiers, or be educated. Slaves are gained through winning battles, or punishing criminals. They have little to no reproduction rate. If you mistreat slaves, and they feel that they have the upper hand in numbers, they may revolt (just leave your settlement for now)"),
			true, true, false, new ColorImp(20, 20, 20));
	
	public final static HTYPE CHILD = new HTYPE("CHILD",
			HCLASS.CHILD,
			D.g("Child"), D.g("Children"), 
			D.g("ChildD", "Children require staffed nurseries to grow up to become subjects. Children can be educated by schools."),
			true, false, false, new ColorImp(20, 20, 20));
	
	private static final ArrayList<HTYPE> ALL = new ArrayList<>(all);
	
	public static LIST<HTYPE> ALL(){
		return ALL;
	}
	
	HTYPE(String key, HCLASS c, CharSequence name, CharSequence names, CharSequence desc, boolean player, boolean works, boolean hostile, COLOR color){
		this(key, c, name, names, desc, player, works, hostile, player, color);
	}
	
	HTYPE(String key, HCLASS c, CharSequence name, CharSequence names, CharSequence desc, boolean player, boolean works, boolean hostile, boolean visible, COLOR color){
		super(name, names, desc, null);
		this.player = player;
		this.works = works;
		this.hostile = hostile;
		this.color = color;
		this.visible = visible;
		this.CLASS = c;
		this.key = key;
		index = all.add(this);
	}
	
	private final int index;
	public final String key;
	public final boolean player;
	public final boolean hostile;
	public final boolean works;
	public final boolean visible;
	public final COLOR color;
	public final HCLASS CLASS;
	
	@Override
	public String toString() {
		return ""+name;
	}
	
	@Override
	public int index() {
		return index;
	};
}
