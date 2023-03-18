package game.faction.player;

import java.io.IOException;
import java.util.*;

import game.faction.FACTIONS;
import game.faction.player.PLocks.PLocker;
import init.D;
import init.boostable.*;
import init.boostable.BOOST_LOOKUP.BOOSTER_LOOKUP_IMP;
import init.boostable.BOOST_LOOKUP.SIMPLE;
import init.paths.PATH;
import init.paths.PATHS;
import init.tech.Unlocks;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import settlement.stats.STATS;
import snake2d.Errors;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.info.GFORMAT;
import util.info.INFO;
import view.interrupter.IDebugPanel;
import view.main.MessageSection;
import world.World;

public final class PLevels {


	
	private static CharSequence ¤¤mTitle = "¤New level unlocked!";
	private static CharSequence ¤¤mMessage = "¤A new level has been bestowed upon your name!";
	private static CharSequence ¤¤UnlocksByTech = "¤Unlocks with level:";
	private static CharSequence ¤¤PopReq = "¤Citizens required: ";
	{
		D.t(this);
	}
	
	public final INFO info = new INFO(
			D.g("Level"), 
			D.g("Desc", "As you grow in might and population, titles will be bestowed upon your name. Levels will unlock great advantages to a ruler."));
	
	
	private final ArrayList<Level> levels;
	
	private Level current;
	private boolean increase;
	private final int[] roomLock = new int[SETT.ROOMS().all().size()];
	public BOOST_LOOKUP.SIMPLE BOOSTER;
	private final Boost boost;
	
	public PLevels() {
		Arrays.fill(roomLock, 0);
		PATH data = PATHS.INIT().getFolder("player").getFolder("level");
		PATH text = PATHS.TEXT().getFolder("player").getFolder("level");
		String[] ss = data.getFiles();
		HashMap<String, Integer> map = new HashMap<>();
		for (String s : ss) {
			Json j = new Json(data.get(s));
			map.put(s, j.i("POPULATION", 0, Integer.MAX_VALUE));
		}
			
		
		Arrays.sort(ss, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return map.get(o1) - map.get(o2);
			}
		});
		
		levels = new ArrayList<>(ss.length);
		
		for (String s : ss) {
			new Level(levels, s, data, text);
		}
		
		if (levels.size() == 0)
			new Errors.DataError("Insufficient levels declared. Needs more than 0", data.get());
		current = levels.get(0);
		
		for (Level l : levels) {
			for (RoomBlueprintImp b : l.roomsUnlocks()) {
				if (l.index() > roomLock[b.index()])
					roomLock[b.index()] = l.index();
			}
		}
		
		IDebugPanel.add("Increase level", new ACTION() {
			
			@Override
			public void exe() {
				increase = true;
			}
		});
		
		boost = new Boost(levels);
		BOOSTER = boost;
	}
	
	private class Boost extends BOOSTER_LOOKUP_IMP implements SIMPLE {

		private final double[] add = new double[BOOSTABLES.all().size()];
		private final double[] mul = new double[BOOSTABLES.all().size()];
		
		protected Boost(ArrayList<Level> levels) {
			super(info.name);
			for (Level t : levels)
				init(t);
			setBonuses(0);
			makeBoosters(this, true, false, true);
		}

		@Override
		public double add(BOOSTABLE b) {
			return add[b.index()];
		}

		@Override
		public double mul(BOOSTABLE b) {
			return mul[b.index()];
		}
		
		private void setBonuses(int i) {
			Arrays.fill(add, 0);
			Arrays.fill(mul, 1);
			
			for (int l = 0; l <= i; l++) {
				for (BBoost b : levels.get(l).boosts()) {
					if (b.isMul())
						mul[b.boostable.index()] *= b.value();
					else
						add[b.boostable.index()] += b.value();
				}
			}
		}
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(current.index);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			int i = file.i();
			if (i >= levels.size())
				i = levels.size()-1;
			current = levels.get(i);
			
			boost.setBonuses(i);
		}
		
		@Override
		public void clear() {
			current = levels.get(0);
			boost.setBonuses(0);
		}
	};
	
	void update() {
		int p = STATS.POP().POP.data(HCLASS.CITIZEN).get(null, 0) + World.ARMIES().cityDivs().total();
		if (current().index() < levels.size()-1 && (increase || levels.get(current().index()+1).condition <= p)) {
			current = levels.get(current().index()+1);
			boost.setBonuses(current().index());
			new Mess(current.index).send();
			increase = false;
		}
	}
	
	boolean roomIsLocked(RoomBlueprint room) {
		return roomLock[room.index()] > current().index;
	}
	
	public LIST<Level> all(){
		return levels;
	}
	
	public Level current() {
		return current;
	}	
	
	public static class Level extends Unlocks implements INDEXED{
		
		private final int condition;
		private final int index;
		public final CharSequence male;
		public final CharSequence female;
		public final CharSequence desc;
		
		Level(ArrayList<Level> all, String key, PATH data, PATH text){
			super("", new Json(data.get(key)));
			this.index = all.add(this);
			Json d = new Json(data.get(key));
			Json t = new Json(text.get(key));
			this.condition = d.i("POPULATION");
			male = t.text("MALE");
			female = t.text("FEMALE");
			desc = t.text("DESC");
			
		}
		
		public int popNeeded() {
			return condition;
		}
		
		public int noblesAllowed() {
			return nobles;
		}
		
		public CharSequence name() {
			return male;
		}

		@Override
		public CharSequence boosterName() {
			return male;
		}
		
		@Override
		public int index() {
			return index;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(name());
			b.text(desc);
			b.NL(4);
			b.add(b.text().errorify().add(¤¤PopReq));
			b.add(GFORMAT.i(b.text(), popNeeded()));
			b.NL();
			
			super.hoverInfoGet(text);
			
		}
	}

	public final PLocker locker = new PLocker(¤¤UnlocksByTech) {

		@Override
		protected int unlocks() {
			return all().size() - (current().index()+1);
		}

		@Override
		protected Unlocks unlock(int i) {
			return all().get(current().index()+1 +i);
		}
	};
	
	private static class Mess extends MessageSection {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int lev;
		
		public Mess(int lev) {
			super(¤¤mTitle);
			this.lev = lev;
			
		}

		@Override
		protected void make(GuiSection section) {
			
			Level l = FACTIONS.player().level().all().get(lev);
			paragraph(¤¤mMessage);
			section.addRelBody(16, DIR.S, new GHeader(l.name()));
			
			
			GBox b = new GBox();
			l.hoverInfoGet(b);
			
			section.addRelBody(8, DIR.S, new RENDEROBJ.RenderImp(b.width(), b.height()) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					b.renderWithout(r, body.x1(), body.y1());
				}
			});
			
		}
		
		
	}
	
}
