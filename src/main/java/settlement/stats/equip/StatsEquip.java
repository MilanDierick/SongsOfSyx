package settlement.stats.equip;

import java.io.IOException;

import init.D;
import init.paths.PATH;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.StatInitable;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.stat.StatCollection;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import util.keymap.RCollection;

public final class StatsEquip extends StatCollection {

	private final ArrayList<Equip> all;
	private final ArrayList<EquipBattle> military;
	private final LIST<EquipBattle> military_all;
	private final ArrayList<EquipRange> ammo;
	private final ArrayList<EquipCivic> civic;

	static CharSequence ¤¤Level = "¤{0} Level";
	static CharSequence ¤¤Target = "¤{0} Target";
	static CharSequence ¤¤Level_desc = "¤The target number of items each individual should equip. Special cases for this is tools, which is set at each industry. Weapons are also set separately for each division.";
	public static CharSequence ¤¤Wear = "¤Wear-out rate per item and year:";
	static CharSequence ¤¤more = "would like to be allowed more {0}.";
	
	public final Equip CLOTHES;
	public final RCollection<EquipBattle> militaryColl;
	
	static {
		D.ts(StatsEquip.class);
	}
	
	public StatsEquip(StatsInit init) throws IOException {
		super(init, "EQUIP");
		
		LinkedList<Equip> all = new LinkedList<>();
		
		PATH data = init.pd.getFolder("equip");
		
		{
			LinkedList<EquipCivic> tmp = new LinkedList<>();
			PATH d = data.getFolder("civic");
			this.CLOTHES = new EquipCivic("_CLOTHES", d, all, tmp, init);
			for (String k : d.getFiles()) {
				new EquipCivic(k, d, all, tmp, init);
			}
			this.civic = new ArrayList<>(tmp);
		}
		
		LinkedList<EquipBattle> mil = new LinkedList<>();
		{
			LinkedList<EquipBattle> tmp = new LinkedList<>();
			PATH d = data.getFolder("battle");
			for (String k : d.getFiles()) {
				
				EquipBattle e = new EquipBattle("BATTLE", k, d, all, mil, init);
				tmp.add(e);
			}
			this.military = new ArrayList<>(tmp);
		}
		
		{
			LinkedList<EquipRange> tmp = new LinkedList<>();
			PATH d = data.getFolder("ranged");
			for (String k : d.getFiles()) {
				new EquipRange(k, d, all, tmp, mil, init);
			}
			this.ammo = new ArrayList<>(tmp);
		}
		
		this.military_all = new ArrayList<EquipBattle>(mil);
		KeyMap<EquipBattle> map = new KeyMap<EquipBattle>();
		for (EquipBattle mm : military_all)
			map.put(mm.eKey(), mm);
		
		militaryColl = new RCollection<EquipBattle>("EQUIPMENT", map) {

			@Override
			public EquipBattle getAt(int index) {
				return military_all.get(index);
			}

			@Override
			public LIST<EquipBattle> all() {
				return military_all;
			}
		
		};
		
		this.all = new ArrayList<>(all);
		
		D.t(this);

		init.updatable.add(new StatUpdatableI() {
			
			@Override
			public void update16(Humanoid h, int updateR, boolean day, int updateI) {
				for (Equip t : all) {
					t.update16(h, updateI, updateI, day);
				}
			}
		});
		
		init.initable.add(new StatInitable() {
			
			@Override
			public void init(Induvidual h) {
				for (Equip t : all) {
					t.set(h, t.arrivalAmount);
				}
			}
		});
		
	}
	
	public void drop(Humanoid h) {
		
		for (Equip e : all) {
			int a = Math.round(e.stat().indu().get(h.indu())*RND.rFloat());
			if (a > 0) {
				SETT.THINGS().resources.create(h.physics.tileC(), e.resource(), a);
			}
		}
	}

	public LIST<Equip> allE() {
		return all;
	}
	
	public LIST<EquipCivic> civics() {
		return civic;
	}
	
	public LIST<EquipBattle> BATTLE() {
		return military;
	}
	
	public LIST<EquipRange> RANGED() {
		return ammo;
	}
	
	public LIST<EquipBattle> BATTLE_ALL() {
		return military_all;
	}

}
