package settlement.room.home;

import java.io.IOException;
import java.util.Arrays;

import settlement.room.home.chamber.ROOM_CHAMBER;
import settlement.room.home.house.ROOM_HOME;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.*;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.info.GFORMAT;

public final class HOMES {

	private final int[] total = new int[HOME_TYPE.ALL().size()+1];
	private final int[] used = new int[HOME_TYPE.ALL().size()+1];
	
	public final ROOM_HOME HOME;
	public final ROOM_CHAMBER CHAMBER;
	
	public HOMES(RoomInitData init, RoomCategorySub cat) throws IOException{
		HOME = new ROOM_HOME(init, cat);
		CHAMBER = new ROOM_CHAMBER(init, cat);
	}
	
	public final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.is(total);
			file.is(used);
			
		}

		@Override
		public void load(FileGetter file) throws IOException {
			file.is(total);
			file.is(used);
			
		}

		@Override
		public void clear() {
			Arrays.fill(total, 0);
			Arrays.fill(used, 0);
		}
		
	};
	
	public void report(int used, int total, HOME_TYPE t) {
		this.used[0] += used;
		this.total[0] += total;
		this.used[t.index()+1] += used;
		this.total[t.index()+1] += total;
	}
	
	public int total(HOME_TYPE t) {
		if (t == null)
			return total[0];
		return total[t.index()+1];
	}
	
	public int used(HOME_TYPE t) {
		if (t == null)
			return used[0];
		return used[t.index()+1];
	}
	
	public void hoverTable(GBox b) {
		b.NL(4);
		b.textL(DicMisc.¤¤Total);
		b.add(GFORMAT.iofk(b.text(), used(null), total(null)));
		b.NL();
		int tab = 0;
		for (HOME_TYPE t : HOME_TYPE.ALL()) {
			b.tab(tab*3);
			b.add(t.icon());
			b.add(GFORMAT.iofk(b.text(), used(t), total(t)));
			tab++;
			if (tab > 4) {
				b.NL();
				tab = 0;
			}
				
		}
		b.NL(4);
	}
	
}
