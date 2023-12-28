package settlement.room.infra.builder;

import static settlement.main.SETT.*;

import java.io.IOException;

import settlement.path.AVAILABILITY;
import settlement.path.finder.SFinderRoomService;
import settlement.room.main.*;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.*;
import settlement.room.main.job.ROOM_RADIUS.ROOM_RADIUSE;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.RoomSprite;
import settlement.room.sprite.RoomSprite1x1;
import snake2d.util.file.*;
import view.tool.PlacableMessages;

public final class ROOM_BUILDER extends RoomBlueprintIns<BuilderInstance> implements ROOM_RADIUSE{


	
	private final Furnisher constructor;

	public ROOM_BUILDER(RoomInitData init, RoomCategorySub cat) throws IOException {
		super(0, init, "_BUILDER", cat);
		constructor = new Furnisher(init, 0, 0, 88, 72) {
			
			{
				Json sp = init.data().json("SPRITES");
				RoomSprite sprite = new RoomSprite1x1(sp, "1x1");
				FurnisherItemTile t = new FurnisherItemTile(this, sprite, AVAILABILITY.AVOID_PASS, false);
				new FurnisherItem(new FurnisherItemTile[][] {
					{t,},
				}, 0);
				
				flushSingle(info);
			}
			
			@Override
			public boolean usesArea() {
				return false;
			}
			
			@Override
			public boolean mustBeIndoors() {
				return false;
			}
			
			@Override
			public Room create(TmpArea area, RoomInit init) {
				return new BuilderInstance(ROOM_BUILDER.this, area, init);
			}
			
			@Override
			public CharSequence placable(int tx, int ty) {
				if (TERRAIN().get(tx, ty).roofIs())
					return super.placable(tx, ty);
				if (TERRAIN().get(tx, ty) != TERRAIN().NADA && !TERRAIN().get(tx, ty).clearing().isEasilyCleared()) {
					return PlacableMessages.¤¤TERRAIN_BLOCK;
				}
				return super.placable(tx, ty);
			}
			
			@Override
			public RoomBlueprintImp blue() {
				return ROOM_BUILDER.this;
			}
			
			@Override
			public boolean needFlooring() {
				return false;
			}
			
			
		};

	}
	
	

	@Override
	protected void update(float ds) {


	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		
	}
	
	@Override
	protected void clearP() {
		
	}
	
	@Override
	public boolean degrades() {
		return false;
	}

	@Override
	public byte radiusRaw(Room t) {
		return ((BuilderInstance) t).radius; 
	}

	@Override
	public void radiusRawSet(Room t, byte r) {
		((BuilderInstance) t).radius = r;
		
	}

}
