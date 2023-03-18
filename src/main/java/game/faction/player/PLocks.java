package game.faction.player;

import init.tech.Unlocks;
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
		
		protected abstract int unlocks();
		protected abstract Unlocks unlock(int i);
		
		public CharSequence unlockText(Floor f) {
			s.clear();
			for (int i = 0; i < unlocks(); i++) {
				Unlocks u = unlock(i);
				if (u == null)
					continue;
				for (Floor ff : u.unlocksRoads()) {
					if (f == ff) {
						s.add(u.boosterName());
						s.NL();
					}
				}
			}
			return s;
		}
		
		public CharSequence unlockText(Industry f) {
			s.clear();
			for (int i = 0; i < unlocks(); i++) {
				Unlocks u = unlock(i);
				if (u == null)
					continue;
				for (Industry ff : u.unlocksIndustry()) {
					if (f == ff) {
						s.add(u.boosterName());
						s.NL();
					}
				}
			}
			return s;
			
		}

		public CharSequence unlockText(RoomBlueprint f) {
			
			s.clear();
			for (int i = 0; i < unlocks(); i++) {
				Unlocks u = unlock(i);
				if (u == null)
					continue;
				for (RoomBlueprintImp ff : u.roomsUnlocks()) {
					if (f == ff) {
						s.add(u.boosterName());
						s.NL();
					}
				}
			}
			return s;
		}

		public int lockedUpgrades(RoomBlueprint b) {
			int am = 0;
			for (int i = 0; i < unlocks(); i++) {
				Unlocks u = unlock(i);
				if (u == null)
					continue;
				for (RoomBlueprintImp bb : u.unlocksUpgrades()) {
					if (b == bb) {
						am++;
					}
				}
			}
			return am;
		}

		public CharSequence unlockTextUpgrade(RoomBlueprint b) {
			s.clear();
			for (int i = 0; i < unlocks(); i++) {
				Unlocks u = unlock(i);
				if (u == null)
					continue;
				for (RoomBlueprintImp bb : u.unlocksUpgrades()) {
					if (b == bb) {
						s.add(u.boosterName());
						s.NL();
					}
				}
			}
			return s;
		}
	}
	
}
