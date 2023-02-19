package game.faction;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import world.World;
import world.army.FactionArmies;
import world.map.regions.FRegions;
import world.map.regions.REGIOND;

public class FKingdom extends FactionResource{

	private final Faction f;
	
	public FKingdom(Faction f){
		this.f = f;
	}
	
	public FactionArmies armies() {
		return World.ARMIES().army(f);
	}
	
	public FRegions realm() {
		return World.REGIONS().realm(f);
	}
	
	public int spentAdmin() {
		return realm().spentAdmin().get();
	}
	
	public void remove() {
		while (armies().all().size() > 0) {
			armies().all().get(0).disband();
		}
		while (realm().regions().size() > 0) {
			REGIOND.OWNER().realm.set(realm().regions().get(0), null);
		}
		
	}
	
	
	
	@Override
	protected void save(FilePutter file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void update(double ds) {
		// TODO Auto-generated method stub
		
	}

	

}
