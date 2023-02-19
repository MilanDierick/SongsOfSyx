package settlement.path.finder;

import settlement.path.components.SCompFinder.SCompPatherFinder;

public interface SFINDER extends SCompPatherFinder{

	boolean isTile(int tx, int ty, int tileNr);
	
}
