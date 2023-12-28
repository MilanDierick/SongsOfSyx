package settlement.tilemap;

import static settlement.main.SETT.*;

import init.C;
import init.D;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.tilemap.growth.Fertility;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;

public final class GuiTerrainHoverInfo{

	private static CharSequence ¤¤Degrade= "¤Degrade:";
	private static CharSequence ¤¤Strength= "¤Strength:";
	private static CharSequence ¤¤Border= "¤This is a static entry point to your city. Keep this clear and reachable.";
	private static CharSequence ¤¤Fertility = "Fertility: {0}% (base: {1}%";
	
	static {
		D.ts(GuiTerrainHoverInfo.class);
	}
	
	private GuiTerrainHoverInfo() {
		
	}
	
	

	
	public static void add(GBox box, int tx, int ty) {
		GText t;
		
		box.add(box.text().lablify().add(Fertility.¤¤name));
		{
			t = box.text();
			t.add(¤¤Fertility);
			t.insert(0, (int)(FERTILITY().target.get(tx, ty)*100));
			t.insert(1, (int)(FERTILITY().baseD.get(tx, ty)*100));
			box.add(t);
			box.NL();
		}
		box.NL(8);
		
		if(!TERRAIN().NADA.is(tx, ty)) {
			TERRAIN().get(tx, ty).hoverInfo(box, tx, ty);
			double st = SETT.ARMIES().map.strength.get(tx, ty)/C.TILE_SIZE;
			if (st > 0) {
				box.NL();
				box.textL(¤¤Strength);
				box.add(GFORMAT.f0(box.text(), st));
			}
			box.NL();
		};
		

		
		if (FLOOR().getter.is(tx, ty)) {
			t = box.text();
			t.lablify().add(FLOOR().getter.get(tx, ty).name());
			box.add(t);
			box.add(box.text().add(¤¤Degrade));
			box.add(GFORMAT.percInv(box.text(), FLOOR().degrade.get(tx, ty)));

			if (MAINTENANCE().isser.is(tx, ty)) {
				box.add(SPRITES.icons().s.hammer);
			}
			box.NL(8);
		}
		
		if (MINERALS().getter.is(tx, ty)) {
			t = box.text();
			t.lablify().add(MINERALS().getter.get(tx, ty).name).add(':');
			box.add(t);
			t = box.text();
			MINERALS();
			double d = (double)MINERALS().amountD.get(tx,ty);
			GFORMAT.perc(t, d);
			box.add(t);
			box.space();
		}
		
		
		
		if (SETT.ENTRY().points.map.is(tx, ty)) {
			box.NL(8);
			box.add(box.text().normalify2().add(¤¤Border));
			box.NL();
		}
		
	}

}
