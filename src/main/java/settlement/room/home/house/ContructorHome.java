package settlement.room.home.house;

import java.io.IOException;

import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.*;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.tilemap.Floors.Floor;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;

final class ContructorHome extends Furnisher{

	private final ROOM_HOME blue;
	
	final double[] space = new double[] {
		1.0,
		0.75,
		0.5,
		0,
	};
	
	final FurnisherStat occupants = new FurnisherStat.FurnisherStatI(this, 1);
	
	public final FurnisherItemTile tOpening;
	public final Sprites sp;
	public final Floor flooring;
	protected ContructorHome(RoomInitData init, ROOM_HOME blue)
			throws IOException {
		super(init, 4, 1, 88, 44);
		
		flooring = floor.get(0);
		sp = new Sprites();
		
		
		
		
		this.blue = blue;
		final FurnisherItemTile ee = new FurnisherItemTile(this, true, sp.theDummy, AVAILABILITY.ROOM, false);
		ee.setData(2);
		ee.noWalls = true;
		tOpening = ee;
		final FurnisherItemTile __ = new FurnisherItemTile(this, false, sp.theDummy, AVAILABILITY.ROOM, false);
		__.setData(1);
		final FurnisherItemTile xx = new FurnisherItemTile(this, false, sp.theDummy, AVAILABILITY.NOT_ACCESSIBLE, false);
		
		LOG.ln();
		
		create(new FurnisherItemTile[][] {
			 {xx,xx,xx},
			 {xx,__,xx},
			 {xx,ee,xx},
		}, 9);
		
		create(new FurnisherItemTile[][] {
			 {xx,xx,xx,xx,xx},
			 {xx,__,__,__,xx},
			 {xx,xx,ee,xx,xx},
		}, 8);
		
		create(new FurnisherItemTile[][] {
			 {xx,xx,xx,xx,xx},
			 {xx,__,__,__,xx},
			 {xx,__,__,__,xx},
			 {xx,__,__,__,xx},
			 {xx,xx,ee,xx,xx},
		}, 7);
		
		create(new FurnisherItemTile[][] {
			 {xx,xx,xx,xx,xx},
			 {xx,__,__,__,xx},
			 {xx,__,__,__,xx},
			 {xx,__,__,__,xx},
			 {xx,__,__,__,xx},
			 {xx,__,__,__,xx},
			 {xx,__,__,__,xx},
			 {xx,__,__,__,xx},
			 {xx,xx,ee,xx,xx},
		}, 7);
		
	}
	
	private void create(FurnisherItemTile[][] tt, int am) {
		am++;
		new FurnisherItem(tt, 1);
		for (int i = 2; i < am; i++) {
			FurnisherItemTile[][] tn = new FurnisherItemTile[tt.length][tt[0].length*i];
			for (int y = 0; y < tt.length; y++) {
				for (int x = 0; x < tn[0].length; x++) {
					tn[y][x] = tt[y][x%tt[0].length];
				}
			}
			new FurnisherItem(tn, i);
		}
		flush(3);
	}

	@Override
	public boolean mustBeIndoors() {
		return true;
	}

	@Override
	public boolean mustBeOutdoors() {
		return false;
	}
	
	
	@Override
	protected TILE_SHEET sheet(ComposerUtil c, ComposerSources s, ComposerDests d, int y1) {
		return null;
	}

	@Override
	public boolean usesArea() {
		return false;
	}
	
	@Override
	public Room create(TmpArea area, RoomInit init) {
		
		int mx = area.mx();
		int my = area.my();
		Room r = blue.instance.place(area);
		
		HomeHouse h = blue.houses.get(mx, my, this);
		h.create();
		
		for (COORDINATE c : h.body()) {
			for (int di = 0; di < DIR.ALL.size(); di++) {
				int x = c.x() + DIR.ALL.get(di).x();
				int y = c.y() + DIR.ALL.get(di).y();
				SETT.TERRAIN().get(x, y).placeFixed(x, y);
			}
			
		}
		h.done();
		return r;
	}
	
	@Override
	public FurnisherItem secretReplacementItem(int rot, FurnisherItem it) {
		return it.group.item(0, rot);
	}
	
	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
	@Override
	public boolean needsIsolation() {
		return true;
	}

}
