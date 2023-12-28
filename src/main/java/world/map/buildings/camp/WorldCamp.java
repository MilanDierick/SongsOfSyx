package world.map.buildings.camp;

import java.io.IOException;

import game.GAME;
import init.C;
import init.D;
import init.race.RACES;
import init.race.Race;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import util.info.INFO;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.map.buildings.WorldBuilding;

public class WorldCamp extends WorldBuilding{

	public final LIST<WCampType> types = WCampType.types();
	public final WCampType[] tmap = new WCampType[RACES.all().size()];
	public static final int MAX = 512;
	private final WCampInstance[] instances = new WCampInstance[MAX];
	private final IntegerStack stack = new IntegerStack(instances.length).fill();
	public final FactionCamps factions;
	public final LIST<Race> available;
	private final boolean[] av = new boolean[RACES.all().size()];
	{
		D.t(this);
	}
	
	public static CharSequence ¤¤unlockableD = "¤A neutral {0} heaven. If certain requirements are met, and you control the region in which it resides, its population will join your cause. The requirements increase with size, and with the amount of havens that have joined you.";
	public static CharSequence ¤¤lockedD = "¤A {0} heaven that is aligned with another faction.";
	public static CharSequence ¤¤unlocked = "¤A {0} heaven that is aligned to your cause. It will steadily increase your {1} immigration pool.";
	
	public final INFO info = new INFO(D.g("Haven"), D.g("HavenD", "Havens are smaller settlements that contain individuals of a specific race. They are independent, but can join a faction if their criteria are met and the faction controls the region in which they are located."));
	
	
	public WorldCamp(LISTE<WorldBuilding> all) throws IOException {
		super(all);
		factions = new FactionCamps(types);
		new Placer(types);
		
		LinkedList<Race> a = new LinkedList<>();
		for (WCampType t : types) {
			if (!av[t.race.index]) {
				a.add(t.race);
				av[t.race.index] = true;
			}
		}
		available = new ArrayList<>(a);
		
		
		
		
	}

	@Override
	protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it, int data) {
		WCampInstance c = instances[data];
		
		int off = (c.type().sheet.size()-C.TILE_SIZE)/2;
	
		int ran = (int) ((c.ran&0b011)*8);
		ran += (c.ran >> 2) & 1;
		int size = (int) (Math.ceil(c.size*3))*2;
		c.type().cMask.bind();
		c.type().sheet.render(r, ran+size, it.x()-off, it.y()-off);
		s.setHeight(4).setDistance2Ground(0);
		c.type().sheet.render(s, ran+size, it.x()-off, it.y()-off);
		
		COLOR.unbind();
	}
	
	public boolean available(Race race) {
		return av[race.index];
	}
	
	
	@Override
	protected void renderOnGround(SPRITE_RENDERER r, RenderIterator it, int data) {
		// TODO Auto-generated method stub
		super.renderOnGround(r, it, data);
	}
	
	@Override
	protected void renderAboveTerrain(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it, int data) {
		
	
	}
	
	@Override
	protected void unplace(int tx, int ty) {
		WCampInstance c = map.get(tx, ty);
		stack.push(c.index);
		instances[c.index] = null;
	}

	@Override
	protected void save(FilePutter file) {
		file.i(instances.length-stack.size());
		stack.save(file);
		for (WCampInstance i : instances)
			if (i != null)
				i.save(file);
		factions.saver.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		
		clear();
		int am = file.i();
		stack.load(file);
		
		for (int i = 0; i < am; i++) {
			WCampInstance in = WCampInstance.load(file);
			instances[i] = in;
		}
		factions.saver.load(file);
	}

	@Override
	protected void clear() {
		stack.clear();
		for (int i = 0; i < instances.length; i++)
			instances[i] = null;
		stack.fill();
		factions.saver.clear();
	}
	
	public void update(float ds) {
		factions.update(ds);
	}
	
	public MAP_OBJECT<WCampInstance> map = new MAP_OBJECT<WCampInstance>() {

		@Override
		public WCampInstance get(int tile) {
			if (map().get(tile) == WorldCamp.this) {
				return instances[data().get(tile)];
			}
			return null;
		}

		@Override
		public WCampInstance get(int tx, int ty) {
			if (map().get(tx, ty) == WorldCamp.this) {
				return instances[data().get(tx, ty)];
			}
			return null;
		}

	};
	
	
	
	void create(int tx, int ty, WCampType t, double size) {
		if (stack.isEmpty())
			return;
		WCampInstance i = new WCampInstance(stack.pop(), tx, ty, t, size, RND.rLong());
		instances[i.index] = i;
		map().set(tx, ty, this);
		data().set(tx, ty, i.index);
	}
	
	private final ArrayList<WCampInstance> tmp = new ArrayList<>(instances.length);
	private int tick = GAME.updateI();
	
	public LIST<WCampInstance> all(){
		if (tick == GAME.updateI())
			return tmp;
		tick = GAME.updateI();
		return pall();
		
	}
	
	private LIST<WCampInstance> pall(){
		tmp.clear();
		for (int i = 0; i < instances.length; i++) {
			if (instances[i] != null)
				tmp.add(instances[i]);
		}
		return tmp;
	}
	
	@Override
	protected void initBeforePlay() {
		factions.init(pall());
		super.initBeforePlay();
	}

}
