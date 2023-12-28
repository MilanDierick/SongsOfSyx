package settlement.room.main.util;

import static settlement.main.SETT.*;

import game.GAME;
import game.faction.FResources.RTYPE;
import init.D;
import init.sprite.SPRITES;
import settlement.room.main.*;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.furnisher.FurnisherItem;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public final class Deleter extends PlacableMulti{
	
	private static CharSequence ¤¤name = "Delete Room";
	private static CharSequence ¤¤desc = "Destroys the room, recovering some of the resources used to build it. Cannot be undone.";
	static {
		D.ts(Deleter.class);
	}
	
	public Deleter(ROOMS m){
		super(¤¤name, ¤¤desc, SPRITES.icons().m.clear_room);
	}
	
	@Override
	public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
		return ROOMS().map.is(tx, ty) && !ROOMS().THRONE.is(tx, ty) ? null : "";
	}

	@Override
	public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
		Room i = ROOMS().map.get(tx, ty);
		if (i == null || i.blueprint() == ROOMS().THRONE)
			return;
		
		TmpArea ar = i.remove(tx, ty, true, this, false);
		if (ar != null)
			ar.clear();
	}
	
	@Override
	public boolean canBePlacedAs(PLACER_TYPE t) {
		return t == PLACER_TYPE.BRUSH || t == PLACER_TYPE.SQUARE;
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		if (ROOMS().THRONE.is(fromX, fromY))
			return false;
		
		return ROOMS().map.is(fromX, fromY) && ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY);
	}
	
	
	private final static int[] amounts = new int[Furnisher.MAX_RESOURCES];
	
	private static int[] getResources(AREA r,  Furnisher furnisher, int upgrade) {
		for (int i = 0; i < amounts.length; i++) {
			amounts[i] = 0;
		}
		for (COORDINATE c : r.body()) {
			
			if (!r.is(c))
				continue;
			
			if (ROOMS().fData.isMaster.is(c)) {
				FurnisherItem it = ROOMS().fData.item.get(c);
				for (int i = 0; i < furnisher.resources(); i++) {
					amounts[i] += it.cost(i, upgrade);
				}
			}
		}
		
		for (int i = 0; i < furnisher.resources(); i++) {
			amounts[i] += Math.ceil(r.area()*furnisher.areaCost(i, upgrade));
			double mm = amounts[i]*0.75;
			amounts[i] = (int) mm;
			
			if (mm-amounts[i] > RND.rFloat())
				amounts[i] ++;
			
			
		}
		return amounts;
	}
	
	public static void scatterMaterials(AREA r, Furnisher furnisher, int upgrade) {
		
		getResources(r, furnisher, upgrade);
		
		
		int resAll = 0;
		int resPiles = 0;
		for (int i = 0; i < furnisher.resources(); i++) {
			resAll += amounts[i];
			resPiles += Math.ceil(amounts[i]/32.0);
		}
		
		if (resPiles == 0)
			return;
		
		double resPerPile = (double)resAll/r.area();
		double am = 0;
		
		for (COORDINATE c : r.body()) {
			if (!r.is(c))
				continue;
			am += resPerPile;
			if (am >= 1) {
				
				int di = RND.rInt(furnisher.resources());
				for (int i = 0; i < furnisher.resources() && am >= 1; i++) {
					int ri = (di+i)%furnisher.resources();
					if (amounts[ri] > 0) {
						
						int a = CLAMP.i(amounts[ri], 0, (int) am);
						THINGS().resources.create(c, furnisher.resource(ri), a);
						GAME.player().res().inc(furnisher.resource(ri), RTYPE.CONSTRUCTION, a);
						am -= a;
						amounts[ri] -= a;						
					}
				}
				
				
			}
		}
		
	}
	
	
}
