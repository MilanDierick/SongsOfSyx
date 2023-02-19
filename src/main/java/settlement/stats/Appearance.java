package settlement.stats;

import java.io.IOException;

import init.race.Race;
import init.race.appearence.RType;
import init.race.appearence.RType.RTypeSpec;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Init.Initable;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.data.INT_O.INT_OE;

public final class Appearance {

	public final INT_OE<Induvidual> hasTop1;
	public final INT_OE<Induvidual> hasTop2;
	public final INT_OE<Induvidual> name;
	public final INT_OE<Induvidual> customName;
	public final INT_OE<Induvidual> gender;
	public final LIST<INT_OE<Induvidual>> all;
	
	private final Initable init = new Initable() {
		
		@Override
		public void init(Induvidual h) {
			
			double ri = RND.rFloat(h.race().appearance().tMax);
			int gi = 0;
			for (RType t : h.race().appearance().types) {
				ri -= t.spec.occurrence;
				if (ri <= 0) {
					gender.set(h, gi);
					int n =  (RND.rInt(t.names.firstNames.size()&0x0FF) << 8);
					n |= RND.rInt(t.names.lastNames.size()&0x0FF);
					name.set(h, n);
					break;
				}
				gi++;
			}
			
			changeHtype(h, null);
			
		}
	};
	
	void changeHtype(Induvidual h, HTYPE old) {
		
		if (old == null || (old != h.hType() && (old == HTYPE.CHILD || h.hType() == HTYPE.CHILD))){
			RTypeSpec s = h.race().appearance().child;
			if (h.hType() != HTYPE.CHILD) {
				s = h.race().appearance().types.getC(gender.get(h)).spec;
			}
			hasTop1.set(h, RND.rFloat() < s.occurrenceTop1 ? 1 : 0);
			hasTop2.set(h, RND.rFloat() < s.occurrenceTop2 ? 1 : 0);
		}
	}
	
	Appearance(Init init){
		hasTop1 = init.count.new DataBit();
		hasTop2 = init.count.new DataBit();
		name = init.count.new DataShort();
		customName = init.count.new DataBit();
		gender = init.count.new DataCrumb();
		init.initable.add(this.init);
		all = new ArrayList<>(
				hasTop1, hasTop2, name, customName, gender);
		
		init.savables.add(new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				file.i(allNames.size());
				for (Str s : allNames)
					s.save(file);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				int am = file.i();
				allNames.clearSoft();
				for (int i = 0; i < am; i++) {
					Str s = new Str(32);
					s.load(file);
					allNames.add(s);
				}
			}
			
			@Override
			public void clear() {
				allNames.clearSoft();
				
			}
		});
	}
	
	private RTypeSpec get(Induvidual i) {
		if (i.hType() == HTYPE.CHILD)
			return i.race().appearance().child;
		return i.race().appearance().types.getC(gender.get(i)).spec;
	}
	
	public void randomize(Humanoid h) {
		init.init(h.indu());
	}
	
	public boolean hasHair(Induvidual i) {
		return hasTop2.get(i) == 1;
	}
	
	public boolean hasBeard(Induvidual i) {
		return hasTop1.get(i) == 1;
	}

	public COLOR colorSkin(Induvidual i) {
		return STATS.NEEDS().disease.colorAdd(get(i).skin.get(i, false), i);
	}
	
	public COLOR colorHair(Induvidual i) {
		return get(i).top2.get(i, false);
	}
	
	public COLOR colorBeard(Induvidual i) {
		return get(i).top1.get(i, false);
	}
	
	public COLOR colorClothes(Induvidual i) {
		if (i.hType().hostile) {
			return clothesEnemy[(int) (i.randomness & 0x07)];
		}
		if (i.hType() == HTYPE.SLAVE || i.hType() == HTYPE.PRISONER)
			return clothesSlave[(int) ((i.randomness>>3) & 0x07)];
		if (i.hType() == HTYPE.NOBILITY) {
			return clothesNoble[0];
		}
		if (i.hType() == HTYPE.TOURIST) {
			return clothesTourist[(int) ((i.randomness>>3) & 0x0F)];
		}
		return i.race().appearance().colors.clothes(STATS.EQUIP().CLOTHES.stat().indu().get(i), (int) ((i.randomness>>6) & 0x07));
	}
	
	static final COLOR[] armorEnemy = new COLOR[] {
		new ColorImp(20, 20, 23),
		new ColorImp(21, 21, 24),
		new ColorImp(22, 22, 25),
		new ColorImp(23, 23, 26),
		new ColorImp(24, 24, 27),
		new ColorImp(25, 25, 28),
		new ColorImp(26, 26, 29),
		new ColorImp(27, 27, 30),
		};

	private final static COLOR[] clothesSlave = new COLOR[] {
		new ColorImp(114, 67, 36).shade(0.5),
		new ColorImp(114+5, 67, 36).shade(0.5),
		new ColorImp(114, 67+5, 36).shade(0.5),
		new ColorImp(114, 67, 36+5).shade(0.5),
		new ColorImp(114+5, 67+5, 36).shade(0.5),
		new ColorImp(114, 67+5, 36+5).shade(0.5),
		new ColorImp(114+5, 67, 36+5).shade(0.5),
		new ColorImp(114+5, 67+5, 36+5).shade(0.5),};

	private final static COLOR[] clothesTourist = COLOR.interpolate(new ColorImp(80,80, 127), new ColorImp(40, 40, 62), 16);
	
	public final static COLOR[] clothesEnemy = new COLOR[] {
		new ColorImp(20, 20, 20),
		new ColorImp(20+5, 20, 20),
		new ColorImp(20, 20+5, 20),
		new ColorImp(20, 20, 20+5),
		new ColorImp(20+5, 20+5, 20),
		new ColorImp(20, 20+5, 20+5),
		new ColorImp(20+5, 20, 20+5),
		new ColorImp(20+10, 20, 20),};
	
	public final static COLOR[] clothesNoble = new COLOR[] {
		new ColorImp(84, 0, 127),
		};
	
	public COLOR colorArmour(Induvidual i) {
		if (i.hType() == HTYPE.ENEMY) {
			return armorEnemy[(int) (i.randomness & 0x07)];
		}
		return i.race().appearance().colors.armour(STATS.EQUIP().BATTLEGEAR.stat().indu().get(i), (int) ((i.randomness>>9) & 0x07));
	}
	
	public COLOR colorLegs(Induvidual i) {
		return get(i).leg.get(i, false);
	}
	
	private final Str ss = new Str(32);
	
	public CharSequence name(Induvidual i) {
		return name(i.race(), i.hType(), gender.get(i), name.get(i), customName.get(i));
	}
	
	public CharSequence name(Race r, HTYPE t, int gender, int name, int custom) {
		if (custom != 0) {
			return allNames.get(name);
		}
		int first = name >>> 8;
		int last = name & 0x0FF;
		ss.clear();
		if (t == HTYPE.NOBILITY) {
			ss.add(r.appearance().types.getC(gender).names.firstNamesNoble.getC(first));
			ss.s();
			ss.add(r.appearance().types.getC(gender).names.lastNamesNoble.getC(last));
		}else {
			ss.add(r.appearance().types.getC(gender).names.firstNames.getC(first));
			ss.s();
			ss.add(r.appearance().types.getC(gender).names.lastNames.getC(last));
		}
		return ss;
	}
	
	public Str customName(Induvidual i) {
		if (customName.get(i) != 0) {
			return allNames.get(name.get(i));
		}
		
		int k = 0;
		if (allNames.size() >= 0x0FFFF) {
			k = RND.rInt(0x0FFFF);
		}else {
			k = allNames.size();
			Str s = new Str(32);
			s.add(name(i));
			allNames.add(s);
		}
		
		customName.set(i, 1);
		name.set(i, k);
		return allNames.get(k);
	}
	
	public void portraitRender(SPRITE_RENDERER r, Induvidual a, int x, int y, int scale) {
		
		a.race().appearance().types.getC(gender.get(a)).portrait.render(r, x, y, a, scale, false);
		
	}
	
	public void portraitRender(SPRITE_RENDERER r, Induvidual a, int x, int y, int scale, boolean dead) {
		
		a.race().appearance().types.getC(gender.get(a)).portrait.render(r, x, y, a, scale, dead);
		
	}
	
	private ArrayListResize<Str> allNames= new ArrayListResize<>(1024, 0x0FFFF); 
	
}
