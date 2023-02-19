package world.map.buildings.camp;

import java.io.IOException;

import game.VERSION;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.statistics.G_REQ;
import init.race.Race;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.ShortCoo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.rnd.RND;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import world.World;
import world.map.regions.Region;

public class WCampInstance implements INDEXED{

	final int index;
	private final int ti;
	public final double size;
	
	private final ShortCoo coo;
	public final long ran;
	public final Str name = new Str(16);
	public final int max;
	public final double replenishRateDay;
	private int factionI = -1;
	
	WCampInstance(int index, int tx, int ty, WCampType t, double size, long ran){
		this.index = index;
		ti = t.index();
		this.size = size;
		this.ran = (int) ran;
		name.clear().add(t.names.getC((int) this.ran));
		name.insert(0, t.race.appearance().types.getC(RND.rInt()&0x0FF).names.lastNamesNoble.getC(RND.rInt(0x0FFFF)));
		max = (int) (t.min + (t.max-t.min)*size);
		replenishRateDay = (t.reMin + (t.reMax-t.reMin)*size);
		coo = new ShortCoo(tx, ty);
		
		World.BUILDINGS().camp.factions.add(this, 1);
	}
	
	WCampType type() {
		return World.BUILDINGS().camp.types.get(ti);
	}
	
	public Race race() {
		return type().race;
	}
	
	public Faction faction() {
		if (factionI == -1)
			return null;
		return FACTIONS.getByIndex(factionI);
	}
	
	public Faction regionFacton() {
		Region r = World.REGIONS().getter.get(coo);
		if (r != null)
			return r.faction();
		return null;
	}
	
	public void factionSet(Faction f) {
		World.BUILDINGS().camp.factions.add(this, -1);
		factionI = f == null ? -1 : f.index();
		World.BUILDINGS().camp.factions.add(this, 1);
	}
	
	
	static WCampInstance load(FileGetter file) throws IOException {
		int index = file.i();
		WCampType t = World.BUILDINGS().camp.types.getC(file.i());
		double size = file.d();
		long ran = file.l();
		int fi = file.i();
		ShortCoo c = new ShortCoo();
		c.load(file);
		WCampInstance ins = new WCampInstance(index, c.x(), c.y(), t, size, ran);
		ins.factionI = fi;
		World.BUILDINGS().camp.factions.add(ins, 1);
		if (!VERSION.versionIsBefore(63, 7))
			ins.name.load(file);
		return ins;
	}
	
	void save(FilePutter file) {
		file.i(index);
		file.i(ti);
		file.d(size);
		file.l(ran);

		file.i(factionI);
		coo.save(file);
		name.save(file);
		
	}

	@Override
	public int index() {
		return index;
	}
	
	public COORDINATE coo() {
		return coo;
	}
	
	public LIST<G_REQ> reqs(){
		return type().requiremets(size);
		
	}
	public void hoverInfo(GUI_BOX text) {
		GBox b = (GBox) text;
		
		b.title(name);
		b.NL();
		b.textL(race().info.names);
		b.tab(5);
		b.add(GFORMAT.i(b.text(), max));
		b.NL(8);
		
		if (faction() == FACTIONS.player()) {
			GText t = b.text();
			t.add(WorldCamp.¤¤unlocked);
			t.insert(0, race().info.names);
			t.insert(1, race().info.names);
			b.add(t);
			
			b.NL(8);
			G_REQ.hover(type().requiremets(size), b);
			
		}else if (faction() == null) {
			GText t = b.text();
			t.add(WorldCamp.¤¤unlockableD);
			t.insert(0, race().info.names);
			b.add(t);
			
			b.NL(8);
			G_REQ.hover(type().requiremets(size), b);
		}else {
			GText t = b.text();
			t.add(WorldCamp.¤¤lockedD);
			t.insert(0, race().info.names);
			b.add(t);
		}
		
		
		
		
		
	}
	
}
