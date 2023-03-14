package world.map.regions;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.faction.Faction;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import world.World;
import world.entity.army.WArmy;

public class Region {

	public static final int nameSize = 24;
	
	private final short index;
	
	private final Str name = new Str(nameSize);
	int area;
	final Rec bounds = new Rec();
	private short cx,cy;
	private short tx,ty;
	private byte textSize;
	byte upI = (byte) RND.rInt(RegionUpdater.shipmentsInteval);
	boolean isWater;
	short besieged = -1;
	
	public static int attackRange = 5;
	
	final int[] data;
	
	short[] distances = new short[0];
	
	Region(int index, int intDataLength) {
		this.index = (short) index;
		
		data = new int[intDataLength];
		clear();
	}

	public int index() {
		return index;
	}
	
	public boolean isWater() {
		return isWater;
	}
	
	public boolean besieged() {
		short b = (short) (GAME.updateI() & 0x07FFF);
		if (b == besieged)
			return true;
		besieged = -1;
		return false;
	}
	
	public void besiege(WArmy a, double time) {
		besieged = (short) ((GAME.updateI()+1) & 0x07FFF);
	}
	
	void save (FilePutter f) {
		name.save(f);
		f.s(cx).s(cy);
		f.s(tx).s(ty);
		bounds.save(f);
		f.i(textSize);
		f.i(area);
		f.is(data);
		f.i(distances.length);
		f.ss(distances);
		f.b(upI);
		f.bool(isWater);
		f.s(besieged);
	}
	
	void load (FileGetter f) throws IOException {
		name.load(f);
		cx = (short) f.s();
		cy = (short) f.s();
		tx = f.s();
		ty = f.s();
		bounds.load(f);
		textSize = (byte) f.i();
		area = f.i();
		f.is(data);
		distances = new short[f.i()];
		f.ss(distances);
		upI = f.b();
		isWater = f.bool();
		besieged = f.s();
	}
	
	void clear(){
		name.clear();
		cx = -1;
		cy = -1;
		tx = -1;
		ty = -1;
		bounds.clear();
		textSize = -1;
		area = 0;
		Arrays.fill(data, 0);
		upI = 0;
		isWater = false;
		besieged = -1;
		distances = new short[0];
	}
	
	void init(int cx, int cy, int tx, int ty, int area, int textDir) {
		this.area = area;
		
		this.cx = (short) cx;
		this.cy = (short) cy;
		this.tx = (short) tx;
		this.ty = (short) ty;
		this.textSize = (byte) textDir;
	}
	
	public Str name() {
		return name;
	}

	public int cx() {
		return cx;
	}
	
	public int cy() {
		return cy;
	}
	
	public int textx() {
		return tx;
	}
	
	public int texty() {
		return ty;
	}
	
	Region centreSet(int tx, int ty) {
		cx = (short) tx;
		cy = (short) ty;
		return this;
	}
	
	public int fontSize() {
		return textSize;
	}
	
	public void fontSize(byte t) {
		textSize = t;
	}
	
	public int area() {
		return area;
	}
	
	public RECTANGLE bounds() {
		return bounds;
	}

	public Faction faction() {
		return REGIOND.faction(this);
	}

	public FRegions realm() {
		if (faction() != null)
			return faction().kingdom().realm();
		return null;
	}
	
	public int distances() {
		return distances.length/2;
	}
	
	public int distance(int index) {
		return distances[index*2+1];
	}
	
	public Region distanceNeigh(int index) {
		return World.REGIONS().all().get(distances[index*2]);
	}
	
	public COLOR color() {
		if (faction() != null)
			return faction().banner().colorBG();
		return COLOR.WHITE65;
	}
	
}
