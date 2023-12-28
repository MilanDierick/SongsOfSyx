package settlement.entity.humanoid;

import init.D;
import init.race.*;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.sets.*;
import util.info.INFO;
import util.keymap.RCollection;

public abstract class HCLASS extends INFO implements INDEXED {
	
	private static final ArrayList<HCLASS> all = new ArrayList<>(10);
	private static final ArrayList<HCLASS> allP = new ArrayList<>(10);
	
	
	static{
		D.gInit(HCLASS.class);
	}
	private final static KeyMap<HCLASS> map = new KeyMap<>();
	public final static HCLASS NOBLE = new HCLASS(
			"NOBLE",
			D.g("Noble"), D.g("Nobilities"), 
			D.g("NobilityD", "The Nobility are the top social layer of your kingdom. They do not work traditionally and demand a salary amongst high tier services. The rewards for having nobles around can be great however."),
			true, new ColorImp(3,1,19)) {
		
		@Override
		public Icon icon() {
			return SPRITES.icons().m.noble;
		}

		@Override
		public Icon iconSmall() {
			return SPRITES.icons().s.noble;
		}
		
	};
	public final static HCLASS CITIZEN = new HCLASS(
			"CITIZEN",
			D.g("Plebeian"), D.g("Plebeians"), 
			D.g("PlebeianD", "Plebeians are the bulk of your population and will carry out your wishes."),
			true, new ColorImp(3,1,19)) {
		
		@Override
		public Icon icon() {
			return SPRITES.icons().m.citizen;
		}

		@Override
		public Icon iconSmall() {
			return SPRITES.icons().s.citizen;
		}
		
	};
	public final static HCLASS SLAVE = new HCLASS(
			"SLAVE",
			D.g("Slave"), D.g("Slaves"), 
			D.g("SlaveD", "Slaves do mundane and hard work, but asks for very little in return. Slaves are gained through winning battles, or punishing criminals. They do not reproduce. If you mistreat slaves, and they feel that they have the upper hand in numbers, they may revolt, take the throne and leave with a bunch of goods."),
			true, new ColorImp(20,20,8)) {
		
		@Override
		public Icon icon() {
			return SPRITES.icons().m.slave;
		}

		@Override
		public Icon iconSmall() {
			return SPRITES.icons().s.slave;
		}
		
	};
	public final static HCLASS CHILD = new HCLASS(
			"CHILD",
			D.g("Child"), D.g("Children"), 
			D.g("ChildD", "Children are to-be citizens. They require food and protection from a manned nursery and can be educated in a school. They will sometimes run around causing mischief."),
			true, new ColorImp(3,1,19)) {
		
		@Override
		public Icon icon() {
			return SPRITES.icons().m.citizen;
		}

		@Override
		public Icon iconSmall() {
			return SPRITES.icons().s.citizen;
		}
		
	};

	public final static HCLASS OTHER = new HCLASS(
			"OTHER",
			"Other", "Others", 
			"",
			false, new ColorImp(20,20,8)) {
		
		@Override
		public Icon icon() {
			return SPRITES.icons().m.citizen;
		}

		@Override
		public Icon iconSmall() {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	public final static RCollection<HCLASS> MAP = new RCollection<HCLASS>("CLASS", map) {

		@Override
		public HCLASS getAt(int index) {
			return all.get(index);
		}

		@Override
		public LIST<HCLASS> all() {
			return all;
		}
	};

	public static LIST<HCLASS> ALL = new ArrayList<>(all);
	
	
	public static LIST<HCLASS> ALL(){
		return all;
	}
	
	private HCLASS(String key, CharSequence name, CharSequence names, CharSequence desc, boolean player, COLOR color){
		super(name, names, desc, null);
		this.player = player;
		if (player)
			playerIndex = allP.add(this);
		else
			playerIndex = -1;
		
		this.color = color;
		index = all.add(this);
		this.key = key;
		map.put(key, this);
	}
	
	public abstract Icon icon();
	public abstract Icon iconSmall();
	
	private final int index;
	public final boolean player;
	public final COLOR color;
	public final String key;
	public final int playerIndex;

	@Override
	public String toString() {
		return name + "#" + index; 
	}
	
	@Override
	public int index() {
		return index;
	};
	
	public POP_CL get(Race race) {
		return RACES.clP(race, this);
	}
}
