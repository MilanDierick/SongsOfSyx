package settlement.room.home.house;

import settlement.room.sprite.RoomSprite;

abstract class SpriteH{

	public final boolean service;
	public final boolean bed;
	public final boolean solid;
	public final RoomSprite sp;
	
	public SpriteH(boolean service, boolean bed, boolean solid, RoomSprite sp) {
		this.service = service;
		this.bed = bed;
		this.solid = solid;
		this.sp = sp;
	}
	
	public SpriteH(RoomSprite sp) {
		this(false, false, true, sp);
	}
	
}
