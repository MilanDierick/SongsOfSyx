package settlement.room.home;

import java.io.IOException;

import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.home.house.HomeHouse;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.*;

public final class HomeSettings {

	private static final int DELTA = 1024;
	private static final int INC = DELTA/2;
	private static final int MAX = DELTA*16;
	
	private final HomeSetting[] specific = new HomeSetting[HOMET.ALL().size()];
	private final HomeSetting def = new HomeSetting(0).setEveryone();
	private boolean hasExpanded = false;
	private HomeSetting[] all = new HomeSetting[INC*2];
	private int allI = 1;
	private HomeSetting last = null;
	

	
	HomeSettings(){
		for (HOMET t : HOMET.ALL()) {
			specific[t.index] = new HomeSetting(0).set(t);
		}
		
		all[0] = def;
		
		for (int i = 1; i < all.length; i++)
			all[i] = new HomeSetting(i);
		
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(all.length);
			file.i(allI);
			
			for (int i = 0; i < allI; i++) {
				file.i(all[i].index);
				file.ls(all[i].bits);
			}
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			int l = file.i();
			if (l > all.length) {
				HomeSetting[] nn = new HomeSetting[l];
				for (int i = 0; i < all.length; i++)
					nn[i] = all[i];
				for (int i = all.length; i < nn.length; i++)
					nn[i] = new HomeSetting(i);
				all = nn;
			}
			
			allI = file.i();
			for (int i = 0; i < allI; i++) {
				all[i].index = file.i();
				file.ls(all[i].bits);
			}
		}
		
		@Override
		public void clear() {
			allI = 1;
		}
	}; 
	
	
	public void set(int tx, int ty, HomeSettingTmp setting) {
		HomeSetting s = get(setting);
		if (s == null) {
			LOG.err("Max house settings are reached. You can not set any more different ones. Max house settings are " + MAX + ", how the hell have you managed to use that many different settings for houses. Get a grip and make more generic settings.");
			return;
		}
		
		last = s;
		
		set(tx, ty, s);

	}
	
	public void set(int tx, int ty, HomeSetting s) {
		
		HomeHouse h = SETT.ROOMS().HOMES.HOME.house(tx, ty, this);
		if (h == null)
			return;
		
		if (h.sFiddler.getCurrentSettingIndex() != s.index) {
			h.sFiddler.setAndReport(s.index, s);
		}
		
		h.done();
	}
	
	void reportChange() {
		hasExpanded = false;
	}
	
	private HomeSetting get(HomeSettingTmp setting) {
		HomeSetting tmp = setting.tmp;
		if (last != null && last.isSame(tmp))
			return last;
		
		for (int i = 0; i < allI; i++) {
			if (all[i].isSame(tmp)) {
				return all[i];
			}
		}
		
		if (allI < all.length || expand()) {
			HomeSetting s = all[allI];
			s.index = allI;
			allI ++;
			s.copy(tmp);
			return s;
		}
		
		return null;
		
	}
	
	private boolean expand() {
		
		if (allI >= MAX) {
			if (hasExpanded)
				return false;
		}
		
		compact();
		
		if (allI < all.length-INC)
			return true;
		
		if (allI >= MAX) {
			hasExpanded = true;
			return false;
		}
		
		HomeSetting[] nn = new HomeSetting[all.length + DELTA];
		for (int i = 0; i < all.length; i++)
			nn[i] = all[i];
		for (int i = all.length; i < nn.length; i++)
			nn[i] = new HomeSetting(i);
		all = nn;
		return true;
		
	}
	
	private void compact() {
		int[] used = new int[all.length];
		int[] nmap = new int[all.length];
		
		Rec tiles = new Rec(SETT.TILE_BOUNDS);
		
		for (COORDINATE c : tiles) {
			if (SETT.ROOMS().HOMES.HOME.isService(c.x()+c.y()*SETT.TWIDTH)) {
				HomeHouse h = SETT.ROOMS().HOMES.HOME.house(c.x(), c.y(), this);
				int i = h.sFiddler.getCurrentSettingIndex();
				used[i] ++;
				h.done();
			}
			
		}
		
		int nid = 1;
		for (int oi = 1; oi < used.length; oi++) {
			if (used[oi] > 0) {
				nmap[oi] = nid;
				all[nid].copy(all[oi]);
				all[nid].index = nid;
				nid++;
			}
		}
		
		for (COORDINATE c : tiles) {
			if (SETT.ROOMS().HOMES.HOME.isService(c.x()+c.y()*SETT.TWIDTH)) {
				HomeHouse h = SETT.ROOMS().HOMES.HOME.house(c.x(), c.y(), this);
				int i = h.sFiddler.getCurrentSettingIndex();
				int ni = nmap[i];
				h.sFiddler.setIndexOnly(ni);
				h.done();
			}
		}
		
		allI = nid;
		
	}
	
	public HomeSetting specific(HOMET type) {
		return specific[type.index];
	}
	
	public static abstract class SettingFiddler {
		
		protected abstract void setIndexOnly(int settingIndex);
		protected abstract void setAndReport(int settingIndex, HomeSetting setting);
		
		public HomeSetting get(int settingIndex) {
			return SETT.ROOMS().HOMES.settings.all[settingIndex];
		}
		
		protected abstract int getCurrentSettingIndex();
		
	}
	
	public final static class HomeSetting {

		public int index;
		private final long[] bits = new long[(int) (Math.ceil(HOMET.ALL().size()/64.0))*2];
		
		HomeSetting(int index){
			this.index = index;
		}
		
		public boolean is(HOMET type) {
			if (type == null)
				return false;
			int index = type.index;
			int li = index>>>6;
			long m = 1l << (index&(64-1));
			return (bits[li] & m) != 0;
		}
		
		public boolean is(int index) {
			int li = index>>>6;
			long m = 1l << (index&(64-1));
			return (bits[li] & m) != 0;
		}
		
		public boolean is(Humanoid h) {
			return is(HOMET.get(h));
		}
		
		HomeSetting set(HOMET type) {
			int index = type.index;
			int li = index>>>6;
			long m = 1l << (index&(64-1));
			bits[li] |= m;
			
			return this;
		}
		
		HomeSetting setEveryone() {
			for (int i = 0; i < bits.length; i++) {
				bits[i] = -1l;
			}
			clear(HOMET.get(HCLASS.NOBLE, null));
			return this;
		}
		
		HomeSetting clear(HOMET type) {
			int index = type.index;
			int li = index>>>6;
			long m = 1l << (index&(64-1));
			bits[li] &= ~m;
			return this;
		}
		
		void copy(HomeSetting other) {
			for (int i = 0; i < bits.length; i++) {
				bits[i] = other.bits[i];
			}
		}
		
		HomeSetting clear() {
			for (int i = 0; i < bits.length; i++) {
				bits[i] = 0l;
			}
			return this;
		}
		
		public boolean isSame(HomeSetting other) {
			for (int i = 0; i < bits.length; i++) {
				if (bits[i] != other.bits[i])
					return false;
			}
			return true;
		}
		
	}
	
	public final static class HomeSettingTmp {

		private final HomeSetting tmp = new HomeSetting(0);
		
		public HomeSettingTmp() {
			
		}
		
		public boolean is(HOMET type) {
			return tmp.is(type);
		}
		
		public HomeSettingTmp set(HOMET type) {
			tmp.set(type);
			return this;
		}
		
		public HomeSettingTmp setEveryone() {
			tmp.setEveryone();
			return this;
		}
		
		public HomeSettingTmp clear(HOMET type) {
			tmp.clear(type);
			return this;
		}
		
		public HomeSettingTmp clear() {
			tmp.clear();
			return this;
		}
		
	}
	
}
