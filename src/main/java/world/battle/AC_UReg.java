package world.battle;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.D;
import init.sprite.UI.UI;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import view.ui.message.MessageText;
import world.WORLD;
import world.army.AD;
import world.log.WLogger;
import world.regions.Region;
import world.regions.data.RD;
import world.regions.data.building.RDBuilding;
import world.regions.data.pop.RDRace;

class AC_UReg {

	private static CharSequence ¤¤lost = "Settlement lost!";
	private static CharSequence ¤¤lostC = "Region of {0} has fallen to our enemies.";

	private static CharSequence ¤¤factionMove = "Capital Relocated";
	private static CharSequence ¤¤factionMoveD = "The faction of {0} has moved its capital. Its people still resist.";

	
	static {
		D.ts(AC_UReg.class);
	}
	
	public void processConqured(Side attacker, double devistation, Region reg, Faction newFaction) {
		WLogger.siege(attacker.faction, reg, true);
		
		Faction fAttacker = attacker.faction;
		Faction fDefender = reg.faction();
		
		if (fDefender != null && fDefender.capitolRegion() == reg) {
			Region newCapitol = reg;
			int ri = Rnd.i(reg.faction().realm().all().size());
			for (int i = 0; i < reg.faction().realm().all().size(); i++) {
				Region r = reg.faction().realm().all().get((i+ri)%reg.faction().realm().all().size());
				if (r != newCapitol) {
					newCapitol = r;
					break;
				}
			}
			
			if (newCapitol == reg || AD.power().get(fDefender) <  AD.power().get(fAttacker)/2 && Rnd.oneIn(reg.faction().realm().all().size())){
				WLogger.factionDestroyed(fDefender);
				
				while(fDefender.realm().regions() > 0)
					RD.setFaction(fDefender.realm().region(0), null);
			}else {
				
				Str.TMP.clear().add(¤¤factionMoveD).insert(0, Faction.name(fDefender));
				WORLD.LOG().log(null, fDefender, UI.icons().s.arrow_right, Str.TMP, newCapitol.cx(), newCapitol.cy());
				new MessageText(¤¤factionMove).paragraph(Str.TMP).send();
				
				newCapitol.setCapitol();
			}
			
			
		}else if (fDefender == FACTIONS.player()) {
			Str.TMP.clear().add(¤¤lostC).insert(0, reg.info.name());
			new MessageText(¤¤lost).paragraph(Str.TMP).send();
		}
		
		double dev = RD.DEVASTATION().current.getD(reg);
		dev += devistation;
		dev = CLAMP.d(dev, 0, 1);
		RD.DEVASTATION().current.setD(reg, dev);
		
		
		if (newFaction == FACTIONS.player() && RD.OWNER().prevOwner(reg) != FACTIONS.player()) {
			for (RDBuilding bu : RD.BUILDINGS().all) {
				
				
				if (bu.level.get(reg) > 0) {
					bu.level.set(reg, 0);
					
				}
			}
		}
		
		for (RDBuilding bu : RD.BUILDINGS().all) {
			
			
			if (bu.level.get(reg) > 0) {
				double ll = bu.level.get(reg)*devistation;
				int l = (int) ll;
				ll-= l;
				if (ll > Rnd.f())
					l++;
				bu.level.set(reg, l);

			}
		}
		
		for (RDRace r : RD.RACES().all) {
			int tot = r.pop.get(reg);
			int nn = (int) (1 + tot*(1.0-devistation));
			nn = CLAMP.i(nn, 1, tot);
			r.pop.set(reg, nn);
		}
		
		reg.fationSet(newFaction);
	}
	
}
