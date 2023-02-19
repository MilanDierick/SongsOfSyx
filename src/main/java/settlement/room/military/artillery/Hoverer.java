package settlement.room.military.artillery;

import init.sprite.SPRITES;
import settlement.entity.humanoid.HCLASS;
import util.dic.DicArmy;
import util.gui.misc.GBox;
import util.info.GFORMAT;

class Hoverer {

	public static void hover(GBox box, ArtilleryInstance i) {
		ArtilleryInstance ins = (ArtilleryInstance) i;
		
		if (ins.mustered()) {
			box.textL(DicArmy.¤¤Musterd);
			box.NL();
			if (i.isLoaded)
				box.textL(DicArmy.¤¤ReadyFire);
			else {
				box.textL(DicArmy.¤¤Reloading);
				box.tab(5);
				box.add(GFORMAT.percInc(box.text(), i.progress()));
			}
				box.NL(4);
		}
		
		if (i.hasTrajectory && ins.mustered()) {
			box.textL(DicArmy.¤¤Attacking);
		}
		
		box.add(SPRITES.icons().s.human);
		box.add(GFORMAT.iofkInv(box.text(), ins.men, 6));
		box.NL(6);
		box.textLL(DicArmy.¤¤Projectiles);
		box.NL();
		i.blueprintI().projectile.hover(box, i.blueprintI().bonus.get(HCLASS.CITIZEN, null), 1);
		box.NL();
		box.textL(DicArmy.¤¤ReloadTime);
		box.tab(6);
		box.add(GFORMAT.f(box.text(), i.blueprintI().RELOAD_TIME));
		
	}
	
}
