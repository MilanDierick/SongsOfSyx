package world.map.buildings.camp;

import java.io.IOException;

import game.faction.Faction;
import init.race.Race;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.ShortCoo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sets.INDEXED;
import snake2d.util.sprite.text.Str;
import world.WORLD;
import world.regions.Region;

public class WCampInstance implements INDEXED{

	final int index;
	private final int ti;
	public final double size;
	
	private final ShortCoo coo;
	public final long ran;
	public final Str name = new Str(16);
	public final int max;
	public final double replenishRateDay;
	
	WCampInstance(int index, int tx, int ty, WCampType t, double size, long ran){
		this.index = index;
		ti = t.index();
		this.size = size;
		this.ran = (int) ran;
		name.clear().add(t.names.getC((int) this.ran));
		name.insert(0, t.race.appearance().lastNamesNoble.getC(RND.rInt(0x0FFFF)));
		max = (int) (t.popFrom + (t.popTo-t.popFrom)*size);
		replenishRateDay = (t.replenishMin + (t.replenishMax-t.replenishMin)*size);
		coo = new ShortCoo(tx, ty);
	}
	
	public WCampType type() {
		return WORLD.BUILDINGS().camp.types.get(ti);
	}
	
	public Race race() {
		return type().race;
	}
	
	public Faction regionFaction() {
		Region r = WORLD.REGIONS().map.get(coo);
		if (r != null)
			return r.faction();
		return null;
	}
	
	public Region region() {
		return  WORLD.REGIONS().map.get(coo);
	}
	
	
	static WCampInstance load(FileGetter file) throws IOException {
		int index = file.i();
		WCampType t = WORLD.BUILDINGS().camp.types.getC(file.i());
		double size = file.d();
		long ran = file.l();
		ShortCoo c = new ShortCoo();
		c.load(file);
		WCampInstance ins = new WCampInstance(index, c.x(), c.y(), t, size, ran);
		ins.name.load(file);	
		return ins;
	}
	
	void save(FilePutter file) {
		file.i(index);
		file.i(ti);
		file.d(size);
		file.l(ran);

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
	
}
