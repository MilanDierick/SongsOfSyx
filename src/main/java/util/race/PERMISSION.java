package util.race;

import java.io.IOException;
import java.util.Arrays;

import init.D;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Induvidual;
import snake2d.util.file.*;
import util.info.INFO;

public interface PERMISSION {

	public boolean get(HCLASS cl, Race race);
	public void set(HCLASS cl, Race race, boolean value);
	public default void toggle(HCLASS cl, Race race) {
		set(cl, race, !get(cl, race));
	}
	public default boolean get(Induvidual indu) {
		return get(indu.clas(), indu.race());
	}
	public default boolean has(Humanoid h) {
		return get(h.indu());
	}
	public INFO info();
	
	
	
	public class Permission implements PERMISSION, SAVABLE{
		private static CharSequence ¤¤name = "¤Permission";
		private static CharSequence ¤¤desc = "¤Toggle permission";
		static {
			D.ts(PERMISSION.class);
		}
		
		private final byte[] access = new byte[RACES.all().size()*HCLASS.ALL.size()];
		private byte def = 0;
		private final INFO info;
		
		public Permission(INFO info){
			this.info = info;
			Arrays.fill(access, (byte)0);
		}
		
		public Permission(CharSequence name, CharSequence desc){
			this.info = new INFO(name, desc);
			Arrays.fill(access, (byte)0);
		}
		
		public Permission(){
			this.info = new INFO(¤¤name, ¤¤desc);
			Arrays.fill(access, (byte)0);
		}

		
		
		@Override
		public boolean get(HCLASS cl, Race race) {
			if (race == null) {
				for (Race r : RACES.all()) {
					if (get(cl, r))
						return true;
				}
				return false;
			}
			return access[cl.index()*RACES.all().size()+race.index] == 1;
		}

		@Override
		public void set(HCLASS cl, Race race, boolean value) {
			if (race == null) {
				for (Race r : RACES.all()) {
					set(cl, r, value);
				}
			}else {
				access[cl.index()*RACES.all().size()+race.index] = (byte) (value ? 1 : 0);
			}
		}

		@Override
		public INFO info() {
			return info;
		}

		@Override
		public void save(FilePutter file) {
			file.bs(access);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			file.bs(access);
		}

		@Override
		public void clear() {
			Arrays.fill(access, def);
		}
		
		public void setDef(boolean def) {
			this.def = (byte) (def ? 1 : 0);
		}
		
	}
	
}
