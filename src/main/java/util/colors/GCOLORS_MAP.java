package util.colors;

import game.faction.FACTIONS;
import game.faction.Faction;
import snake2d.util.color.*;

public final class GCOLORS_MAP {
	
	public static final COLOR GOOD = new ColorImp(10, 127, 10);
	public static final COLOR BAD = new ColorImp(127, 10, 10);
	
	public final static COLOR ok = new ColorImp(0, 64, 127);
	public final static COLOR ok2 = new ColorImp(0, 127, 64);
	public final static COLOR ok_hovered = new ColorShifting(ok, new ColorImp(0, 90, 120));
	
	public final static COLOR map_ok = new ColorImp(10, 10, 120);
	public final static COLOR map_not_ok = new ColorImp(100, 10, 10);
	
	public final static COLOR SOSO = COLOR.YELLOW100;
	
	public final static COLOR GOOD2 = new ColorImp(29, 29, 127);
	
	public final static COLOR FPlayer = new ColorImp(20, 127, 20);
	public final static COLOR FAlly = new ColorImp(20, 20, 127);
	public final static COLOR FEnemy = new ColorImp(127, 20, 20);
	public final static COLOR FRebel = new ColorImp(50, 50, 50);
	public final static COLOR bestOverlay = new ColorImp(50, 120, 120);
	public final static COLOR worstOverlay = new ColorImp(50, 5, 5);
	
	public static COLOR get(Faction f) {
		if (f == null)
			return FRebel;
		if (f == FACTIONS.player())
			return FPlayer;
		if (FACTIONS.rel().war.get(FACTIONS.player(), f) == 0)
			return FAlly;
		else
			return FEnemy;
	}
	
	
	private GCOLORS_MAP() {
		
	}
	
}
