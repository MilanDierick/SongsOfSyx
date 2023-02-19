package settlement.room.main;

import game.GameDisposable;
import init.biomes.CLIMATE;
import init.sprite.ICON;
import settlement.path.finder.SFinderFindable;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListResize;
import util.info.INFO;

public abstract class RoomBlueprintImp extends RoomBlueprint{

	public final String key;
	public final String type;
	public final INFO info;
	public final RoomCategorySub cat;
	private double degradeRate;
	private final RoomUpgrades upgrades;
	private final int typeIndex;
	
	static ArrayListResize<RoomBlueprintImp> IMPS = new ArrayListResize<>(10, 512);
	static {
		new GameDisposable() {
			@Override
			protected void dispose() {
				IMPS.clear();
			}
		};
	}
	
	protected RoomBlueprintImp(RoomInitData init, int typeIndex, String key, RoomCategorySub cat) {
		this(init, typeIndex, key, cat, null);
		
	}

	protected RoomBlueprintImp(RoomInitData init, int typeIndex, String key, RoomCategorySub cat, ACTION wiki) {
		init.init(key);
		this.typeIndex = typeIndex;
		if (cat != null)
			cat.add(this);
		this.type = init.type();
		this.key = key;
		info = new INFO(init.text(), wiki);
		this.cat = cat;
		if (init.data().has("DEGRADE_RATE"))
			degradeRate = init.data().d("DEGRADE_RATE", 0, 1);
		else
			degradeRate = 0.75;
		upgrades = new RoomUpgrades(init);
		IMPS.add(this);
	}
	
	public ICON.BIG iconBig(){
		if (constructor() != null)
			return constructor().icon();
		return null;
	}

	@Override
	public abstract SFinderFindable service(int tx, int ty);

	
	@Override
	public COLOR miniC(int tx, int ty) {
		return constructor().miniColor(tx, ty);
	}
	
	@Override
	public COLOR miniCPimped(ColorImp origional, int tx, int ty, boolean northern, boolean southern) {
		return constructor().miniColorPimped(origional, tx, ty, northern, southern);
	}
	
	public abstract Furnisher constructor();
	
	
	public boolean isAvailable(CLIMATE c) {
		return true;
	}
	

	
	public double degradeRate() {
		return degradeRate;
	}
	
	public RoomUpgrades upgrades() {
		return upgrades;
	}
	
	public int typeIndex() {
		return typeIndex;
	}
	
}
