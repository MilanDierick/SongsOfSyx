package settlement.room.home;

import init.D;
import init.race.RACES;
import init.race.Race;
import init.sprite.ICON;
import init.sprite.SPRITES;
import settlement.entity.humanoid.*;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.dic.DicMisc;
import util.info.INFO;

public abstract class HOME_TYPE extends INFO implements INDEXED{

	private final int index;

	
	private HOME_TYPE(LISTE<HOME_TYPE> all, LISTE<HOME_TYPE> extra, CharSequence name, CharSequence desc) {
		super(name, desc);
		this.index = all.add(this);
		if (extra != null)
			extra.add(this);
	}

	@Override
	public int index() {
		return index;
	}
	
	private static LIST<HOME_TYPE> all;
	private static HOME_TYPE everyone;
	private static HOME_TYPE noble;
	private static LIST<HOME_TYPE> slaves;
	private static LIST<HOME_TYPE> citizens;
	private static CharSequence ¤¤allD = "¤Homes for everyone.";
	private static CharSequence ¤¤allClassD = "¤Homes for all {0}";
	private static CharSequence ¤¤raceClass = "¤{0}, {1}";
	private static CharSequence ¤¤raceClassD = "¤Homes for {0}, {1}";

	static {
		D.ts(HOME_TYPE.class);
	}
	
	public static LIST<HOME_TYPE> ALL(){
		create();
		return all;
	}
	
	public static HOME_TYPE EVERYONE(){
		return everyone;
	}

	public static HOME_TYPE SLAVE(Race r){
		int i = r == null ? 0 : 1+r.index;
		return slaves.get(i);
	}
	
	public static HOME_TYPE CITIZEN(Race r){
		int i = r == null ? 0 : 1+r.index;
		return citizens.get(i);
	}

	public static HOME_TYPE NOBLE(){
		return noble;
	}
	

	
	
	private static void create() {
		if (all != null) {
			return;
		}
		
		LinkedList<HOME_TYPE> all = new LinkedList<>();
		
		everyone = new HOME_TYPE(all, null, DicMisc.¤¤All,  ¤¤allD) {

			@Override
			public boolean isValid(Humanoid h) {
				return h.indu().clas() != HCLASS.NOBLE;
			}

			@Override
			public SPRITE icon() {
				return SPRITES.icons().m.questionmark;
			}

			@Override
			public Race race() {
				return null;
			}

			@Override
			public HCLASS clas() {
				return null;
			}
			
		};
		
		noble = new HOME_TYPE(all, null, HTYPE.NOBILITY.name, "") {

			@Override
			public boolean isValid(Humanoid h) {
				return h.indu().clas() == HCLASS.NOBLE;
			}

			@Override
			public SPRITE icon() {
				return SPRITES.icons().m.noble;
			}

			@Override
			public Race race() {
				return null;
			}

			@Override
			public HCLASS clas() {
				return HCLASS.NOBLE;
			}
			
		};
		
		citizens = createEmp(HCLASS.CITIZEN, all);
		slaves = createEmp(HCLASS.SLAVE, all);
		HOME_TYPE.all = new ArrayList<>(all);
	}
	


	private static LIST<HOME_TYPE> createEmp(HCLASS c, LISTE<HOME_TYPE> all){
		
		LinkedList<HOME_TYPE> oddjobber = new LinkedList<>();
		Str t = Str.TMP;
		
		new HOME_TYPE(all, oddjobber, c.names, "" + t.clear().add(¤¤allClassD).insert(0, c.names)) {
			@Override
			public boolean isValid(Humanoid h) {
				return h.indu().clas() == c;
			}
			
			@Override
			public SPRITE icon() {
				return c.icon();
			}

			@Override
			public Race race() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public HCLASS clas() {
				return c;
			}
		};
		
		for (Race r : RACES.all()) {
			
			CharSequence name = "" + t.clear().add(¤¤raceClass).insert(0, c.names).insert(1, r.info.namePosessive);
			CharSequence desc = "" + t.clear().add(¤¤raceClassD).insert(0, c.names).insert(1, r.info.namePosessive);
			
			SPRITE s = new SPRITE.Imp(ICON.MEDIUM.SIZE+12, ICON.MEDIUM.SIZE) {
				
				@Override
				public void render(SPRITE_RENDERER rr, int X1, int X2, int Y1, int Y2) {
					double scale = (double)(Y2-Y1)/height();
					int x2 =(int)(X1 +r.appearance().icon.width()*scale);
					r.appearance().icon.render(rr, X1, x2, Y1, (int)(Y1 +r.appearance().icon.height()*scale));
					x2 -= 6*scale;
					c.iconSmall().render(rr, x2, (int)(x2 + c.iconSmall().width()*scale), Y1, (int)(Y1 + c.iconSmall().width()*scale));
					
					
				}
			};
			
			new HOME_TYPE(all, oddjobber, name, desc) {
				@Override
				public boolean isValid(Humanoid h) {
					return h.indu().clas() == c && h.race() == r;
				}
				
				@Override
				public SPRITE icon() {
					return s;
				}

				@Override
				public Race race() {
					return r;
				}

				@Override
				public HCLASS clas() {
					return c;
				}
			};
		}
		
		return new ArrayList<HOME_TYPE>(oddjobber);
		
	}

	public abstract boolean isValid(Humanoid h);
	
	public abstract Race race();
	public abstract HCLASS clas();
	
	public static HOME_TYPE getGeneral(Humanoid h) {
		if (h.indu().hType() == HTYPE.NOBILITY)
			return NOBLE();
		return EVERYONE();
	}
	
	public static HOME_TYPE getSpecific(Humanoid h) {
		if (h.indu().hType() == HTYPE.NOBILITY)
			return NOBLE();
		if (h.indu().clas() == HCLASS.SLAVE) {
			return SLAVE(h.race());
		}else {
			return CITIZEN(h.race());
		}
	}
	
	public static HOME_TYPE getSpecific2(Humanoid h) {
		if (h.indu().hType() == HTYPE.NOBILITY)
			return NOBLE();
		if (h.indu().clas() == HCLASS.SLAVE) {
			return SLAVE(null);
		}else {
			return CITIZEN(null);
		}
	}
	
	public abstract SPRITE icon();
	
	@Override
	public String toString() {
		return clas() + " " + race();
	}
	
}
