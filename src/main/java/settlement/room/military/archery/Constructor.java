package settlement.room.military.archery;

import java.io.IOException;

import settlement.path.AVAILABILITY;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.*;
import snake2d.util.datatypes.AREA;
import snake2d.util.file.Json;
import util.gui.misc.GText;
import util.info.GFORMAT;


abstract class Constructor extends Furnisher{
	
	private final ROOM_ARCHERY blue;
	
	final FurnisherItemTile plat;
	
	final FurnisherStat men = new FurnisherStat(this, 0) {
		
		@Override
		public double get(AREA area, double fromItems) {
			return fromItems;
		}
		
		@Override
		public GText format(GText t, double value) {
			return GFORMAT.i(t, (int)value);
		}
	};
	
	protected Constructor(ROOM_ARCHERY blue, RoomInitData init) throws IOException {
		super(init, 1, 1, 88, 44);
		this.blue = blue;
		
		Json js = init.data().json("SPRITES");
		
		RoomSprite1x1 sTarget = new RoomSprite1x1(js, "TARGET_1X1");
		RoomSprite1x1 sLane = new RoomSprite1x1(js, "LANE_1X1");
		RoomSprite1x1 sPlat = new RoomSprite1x1(js, "PLATFORM_1X1");	
		RoomSprite spriteCand = new RoomSprite1x1(js, "TABLE_1X1");	
		
		
		RoomSprite spriteFence = new RoomSpriteComboN(js, "FENCE_COMBO");
		
		FurnisherItemTile ta = new FurnisherItemTile(this, false, sTarget, AVAILABILITY.SOLID, false);
		FurnisherItemTile ll = new FurnisherItemTile(this, false, sLane, AVAILABILITY.SOLID, false);
		FurnisherItemTile pp = new FurnisherItemTile(this, true, sPlat, AVAILABILITY.ROOM, false);
		FurnisherItemTile ca = new FurnisherItemTile(this, false, spriteCand, AVAILABILITY.SOLID, true);
		FurnisherItemTile __ = new FurnisherItemTile(this, false, spriteFence, AVAILABILITY.SOLID, false);
		plat = pp;
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{__,__,__},
					{ca,ta,ca},
					{__,ll,__},
					{__,ll,__},
					{__,ll,__},
					{__,ll,__},
					{__,ll,__},
					{__,ll,__},
					{ca,pp,ca},
				},
				1.0);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{__,__,__,__},
					{ca,ta,ta,ca},
					{__,ll,ll,__},
					{__,ll,ll,__},
					{__,ll,ll,__},
					{__,ll,ll,__},
					{__,ll,ll,__},
					{__,ll,ll,__},
					{ca,pp,pp,ca},
				},
				2.0);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{__,__,__,__,__},
					{ca,ta,ta,ta,ca},
					{__,ll,ll,ll,__},
					{__,ll,ll,ll,__},
					{__,ll,ll,ll,__},
					{__,ll,ll,ll,__},
					{__,ll,ll,ll,__},
					{__,ll,ll,ll,__},
					{ca,pp,pp,pp,ca},
				},
				3.0);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{__,__,__,__,__,__},
					{ca,ta,ta,ta,ta,ca},
					{__,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,__},
					{ca,pp,pp,pp,pp,ca},
				},
				4.0);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{__,__,__,__,__,__,__},
					{ca,ta,ta,ta,ta,ta,ca},
					{__,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,__},
					{ca,pp,pp,pp,pp,pp,ca},
				},
				5.0);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{__,__,__,__,__,__,__,__},
					{ca,ta,ta,ta,ta,ta,ta,ca},
					{__,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,__},
					{ca,pp,pp,pp,pp,pp,pp,ca},
				},
				6.0);
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{__,__,__,__,__,__,__,__,__},
					{ca,ta,ta,ta,ta,ta,ta,ta,ca},
					{__,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,__},
					{ca,pp,pp,pp,pp,pp,pp,pp,ca},
				},
				7.0);
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{__,__,__,__,__,__,__,__,__,__},
					{ca,ta,ta,ta,ta,ta,ta,ta,ta,ca},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{ca,pp,pp,pp,pp,pp,pp,pp,pp,ca},
				},
				8.0);
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{__,__,__,__,__,__,__,__,__,__,__},
					{ca,ta,ta,ta,ta,ta,ta,ta,ta,ta,ca},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{ca,pp,pp,pp,pp,pp,pp,pp,pp,pp,ca},
				},
				9.0);
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{__,__,__,__,__,__,__,__,__,__,__,__},
					{ca,ta,ta,ta,ta,ta,ta,ta,ta,ta,ta,ca},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{__,ll,ll,ll,ll,ll,ll,ll,ll,ll,ll,__},
					{ca,pp,pp,pp,pp,pp,pp,pp,pp,pp,pp,ca},
				},
				10.0);
		
		flush(1, 3);
	}

	@Override
	public boolean usesArea() {
		return true;
	}

	@Override
	public boolean mustBeIndoors() {
		return false;
	}
	
	@Override
	public boolean mustBeOutdoors() {
		return true;
	}
	
	@Override
	public ROOM_ARCHERY blue() {
		return blue;
	}

}
