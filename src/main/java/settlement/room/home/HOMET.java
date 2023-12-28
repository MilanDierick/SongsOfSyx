package settlement.room.home;

import game.GameDisposable;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;

public final class HOMET implements INDEXED{

	public final int index;
	public final Race race;
	public final HCLASS cl;
	public final SPRITE icon;
	public final String name;
	
	private HOMET(LISTE<HOMET> all, Race race, HCLASS cl) {
		this.index = all.add(this);
		this.race = race;
		this.cl = cl;
		if (race == null) {
			icon = SPRITES.icons().m.noble;
			name = ""+HCLASS.NOBLE.names;
		}else {
			icon = new SPRITE.Imp(Icon.M+12, Icon.M) {
				
				@Override
				public void render(SPRITE_RENDERER rr, int X1, int X2, int Y1, int Y2) {
					if (race == null || race.appearance() == null || race.appearance().icon == null)
						return;
					double scale = (double)(Y2-Y1)/height();
					int x2 =(int)(X1 +race.appearance().icon.width()*scale);
					race.appearance().icon.render(rr, X1, x2, Y1, (int)(Y1 +race.appearance().icon.height()*scale));
					x2 -= 6*scale;
					cl.iconSmall().render(rr, x2, (int)(x2 + cl.iconSmall().width()*scale), Y1, (int)(Y1 + cl.iconSmall().width()*scale));
					
					
				}
			};
			name = race.info.names + " ("+cl.names + ")";
		}
	}

	@Override
	public int index() {
		return index;
	}
	
	private static LIST<HOMET> all;
	private static LIST<HOMET> allNN;
	
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				all = null;
				allNN = null;
			}
		};
	}
	
	public static LIST<HOMET> ALL(){
		if (all != null) {
			return all;
		}

		ArrayList<HOMET> all = new ArrayList<>(1+RACES.all().size()*2);
		ArrayList<HOMET> allNN = new ArrayList<>(RACES.all().size()*2);
		new HOMET(all, null, HCLASS.NOBLE);
		
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			allNN.add(new HOMET(all, RACES.all().get(ri), HCLASS.CITIZEN));
		}
		
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			allNN.add(new HOMET(all, RACES.all().get(ri), HCLASS.SLAVE));
		}
		
		HOMET.all = all;
		HOMET.allNN = allNN;
		
		return HOMET.all;
	}
	
	public static LIST<HOMET> ALLNN(){
		ALL();
		return HOMET.allNN;
	}
	
	public static HOMET get(HCLASS cl, Race race){
		if (cl == HCLASS.NOBLE)
			return ALL().get(0);
		if (cl == HCLASS.CITIZEN)
			return ALL().get(1+race.index);
		if (cl == HCLASS.SLAVE)
			return ALL().get(1+RACES.all().size()+race.index);
		return null;
	}
	
	public static HOMET get(Humanoid h){
		return get(h.indu().clas(), h.race());
	}

	
}
