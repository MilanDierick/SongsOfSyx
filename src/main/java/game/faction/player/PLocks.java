package game.faction.player;

import settlement.room.industry.module.Industry;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import settlement.tilemap.Floors.Floor;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;

public final class PLocks {

	private static final Str s = new Str(128);
	private final PLocker[] locks; 
	
	public PLocks(Player player) {
		locks = new PLocker[] {
			player.tech.locker,
			player.level().locker,
			player.titles.locker
		};
	}
	
	public int maxUpgrade(RoomBlueprintImp blue) {
		int tot = blue.upgrades().max();
		for (PLocker l : locks)
			tot -= l.lockedUpgrades(blue);
		return CLAMP.i(tot, 0, blue.upgrades().max());
	}
	
	public CharSequence unlockText(Floor f) {
		
		s.clear();
		
		for (PLocker l : locks) {
			CharSequence p = l.unlockText(f);
			if (p.length() > 0) {
				s.add(l.prefix).NL();
				s.add(p);
				s.NL();
			}
		}
		
		return s.length() == 0 ? null : s;
	}
	
	public CharSequence unlockText(RoomBlueprint b) {
		
		s.clear();
		
		for (PLocker l : locks) {
			CharSequence p = l.unlockText(b);
			if (p.length() > 0) {
				s.add(l.prefix).NL();
				s.add(p);
				s.NL();
			}
		}
		
		return s.length() == 0 ? null : s;
	}
	
	public CharSequence unlockTextUpgrade(RoomBlueprint b) {
		
		s.clear();
		
		for (PLocker l : locks) {
			CharSequence p = l.unlockTextUpgrade(b);
			if (p.length() > 0) {
				s.add(l.prefix).NL();
				s.add(p);
				s.NL();
			}
		}
		
		return s.length() == 0 ? null : s;
	}
	
	public CharSequence unlockText(Industry b) {
		
		s.clear();
		
		for (PLocker l : locks) {
			CharSequence p = l.unlockText(b);
			if (p.length() > 0) {
				s.NL();
				s.add(l.prefix).NL();
				s.add(p);
				
			}
		}
		
		return s.length() == 0 ? null : s;
	}

	
	public static abstract class PLocker {
		
		public final CharSequence prefix;
		protected final Str s = new Str(128);
		
		
		PLocker(CharSequence prefix){
			this.prefix = prefix;
		}
		
		public abstract CharSequence unlockText(RoomBlueprint b);
		public abstract CharSequence unlockText(Industry b);
		public abstract CharSequence unlockText(Floor f);
		public abstract int lockedUpgrades(RoomBlueprint b);
		public abstract CharSequence unlockTextUpgrade(RoomBlueprint b);
	}
	
}
