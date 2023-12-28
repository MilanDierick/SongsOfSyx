package settlement.room.main.category;

import init.sprite.UI.Icon;
import settlement.room.main.RoomBlueprintImp;
import snake2d.util.color.COLOR;
import snake2d.util.sets.*;

public final class RoomCategorySub {
	
	private final ArrayListGrower<RoomBlueprintImp> all = new ArrayListGrower<RoomBlueprintImp>();
	public final COLOR color;
	private final CharSequence name;
	private final Icon icon;
	
	RoomCategorySub(ArrayListGrower<RoomCategorySub> all, CharSequence name, Icon icon, COLOR color) {
		this.name = name;
		this.icon = icon;
		
		this.color = color;
		all.add(this);
	}
	
	public int add(RoomBlueprintImp imp) {
		return all.add(imp);
	}
	
	public CharSequence name() {
		return name;
	}
	
	public Icon icon(){
		if (icon != null)
			return icon;
		return all.get(0).icon;
	}
	
	public LIST<RoomBlueprintImp> rooms(){
		return all;
	}
	
}
