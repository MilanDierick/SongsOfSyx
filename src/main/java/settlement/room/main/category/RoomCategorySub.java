package settlement.room.main.category;

import init.sprite.ICON;
import settlement.room.main.RoomBlueprintImp;
import snake2d.util.color.COLOR;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.LIST;

public final class RoomCategorySub {
	
	private final ArrayListResize<RoomBlueprintImp> all = new ArrayListResize<RoomBlueprintImp>(32, 256);
	public final COLOR color;
	private final CharSequence name;
	private final ICON.MEDIUM icon;
	
	RoomCategorySub(CharSequence name, ICON.MEDIUM icon, COLOR color) {
		this.name = name;
		this.icon = icon;
		
		this.color = color;
	}
	
	public int add(RoomBlueprintImp imp) {
		return all.add(imp);
	}
	
	public CharSequence name() {
		return name;
	}
	
	public ICON.MEDIUM icon(){
		return icon;
	}
	
	public LIST<RoomBlueprintImp> rooms(){
		return all;
	}
	
}
