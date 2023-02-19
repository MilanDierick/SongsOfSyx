package settlement.thing.halfEntity.caravan;

import java.io.IOException;
import java.util.Arrays;

import game.faction.Faction;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.thing.halfEntity.Factory;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;

public class Caravans extends Factory<Caravan>{

	private final Type fetcher = new TypeFetcher();
	private final Type delivery = new TypeDelivier();
	private final Type delivery_throne = new TypeDelivierStorage();
	private final Type supply = new TypeArmySupply();
	private final Type fetcherWarehouse = new TypeFetcherWarehouse();
	int[] tmpSold = new int[RESOURCES.ALL().size()];
	
	public Caravans(LISTE<Factory<?>> all) {
		super(all);
	}

	@Override
	protected void save(FilePutter file) {
		file.is(tmpSold);
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		file.is(tmpSold);
		
	}

	public int tmpSold(RESOURCE res) {
		return tmpSold[res.index()];
	}
	
	@Override
	protected void clear() {
		Arrays.fill(tmpSold, 0);
	}

	@Override
	protected Caravan make() {
		return new Caravan();
	}
	
	public boolean createFetcher(RESOURCE res, int amount) {
		return create(res, amount, fetcher);
	}
	
	public boolean createTmpExport(RESOURCE res, int amount, int price, Faction f) {
		TypeFetcherWarehouse.price = price;
		TypeFetcherWarehouse.faction = f.index();
		return create(res, amount, fetcherWarehouse);
	}
	
	public boolean createWarehouseFetcher(RESOURCE res, int amount, int price) {
		return create(res, amount, fetcher);
	}
	
	
	public boolean createDelivery(RESOURCE res, int amount, boolean dump) {
		if (create(res, amount, delivery))
			return true;
		if (dump)
			return create(res, amount, delivery_throne);
		return false;
	}
	
	public boolean createSupply(RESOURCE res, int amount) {
		return create(res, amount, supply);
	}
	
	private boolean create(RESOURCE res, int amount, Type type) {
		COORDINATE coo = SETT.PATH().entryPoints.rnd();
		if (coo == null)
			return false;
		
		Caravan c = create();
		if (c.init(coo.x(), coo.y(), type, res, amount)) {
			return true;
		}else {
			type.cancel(c);
		}
		return false;
	}
	
	
	

}
