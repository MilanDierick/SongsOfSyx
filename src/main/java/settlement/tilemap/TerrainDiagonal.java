package settlement.tilemap;

import init.D;
import init.sprite.SPRITES;
import settlement.main.SETT;
import snake2d.util.datatypes.AREA;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public final class TerrainDiagonal {

	private static CharSequence ¤¤name = "¤Make Diagonal";
	private static CharSequence ¤¤desc = "¤Turns walls diagonal, only for aesthetic purposes.";
	
	private static CharSequence ¤¤undo = "¤Make Rectangular";
	private static CharSequence ¤¤undoDesc = "¤Turns walls rectangular, only for aesthetic purposes.";
	
	private static CharSequence ¤¤problem = "¤Must be placed on a wall like structure";
	
	static {
		D.ts(TerrainDiagonal.class);
	}
	
	public final PlacableMulti placer = new PlacableMulti(¤¤name, ¤¤desc, SPRITES.icons().m.place_ellispse) {
		
		@Override
		public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
			Diagonalizer t = (Diagonalizer) SETT.TERRAIN().get(tx, ty);
			t.setDia(tx, ty, true);
			
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return SETT.TERRAIN().get(tx, ty) instanceof Diagonalizer ? null : ¤¤problem;
		}
	};
	
	public final PlacableMulti undo = new PlacableMulti(¤¤undo, ¤¤undoDesc, SPRITES.icons().m.place_rec) {
		
		@Override
		public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
			Diagonalizer t = (Diagonalizer) SETT.TERRAIN().get(tx, ty);
			t.setDia(tx, ty, false);
			
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return SETT.TERRAIN().get(tx, ty) instanceof Diagonalizer ? null : E;
		}
	};
	
	interface Diagonalizer {
		
		public void setDia(int tx, int ty, boolean dia);
		public boolean getDia(int tx, int ty);
		
	}
	
}
