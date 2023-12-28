package view.world.ui.region;

import game.faction.FACTIONS;
import game.faction.FBanner;
import game.faction.npc.FactionNPC;
import game.faction.npc.ruler.ROpinions;
import game.faction.player.emissary.EMissionType;
import init.sprite.UI.UI;
import snake2d.util.gui.GUI_BOX;
import util.data.GETTER.GETTER_IMP;
import util.dic.DicGeo;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import world.regions.Region;
import world.regions.data.RD;

final class OtherHov {

	private final GETTER_IMP<Region> g = new GETTER_IMP<>();
	
//	private final SPRITE info = MiscBasics.info(g).asSprite();
//	private final SPRITE gar = MiscMore.garrison(g).asSprite();
//	
	void hover(Region reg, GUI_BOX box) {
		g.set(reg);
		box.title(reg.info.name());
		GBox b = (GBox) box;
		
		if (reg.faction() == null) {
			b.add(FBanner.rebel.BIG);
			b.text(DicGeo.¤¤NoRuler);
			
		}else {
			
			FactionNPC f = (FactionNPC) reg.faction();
			b.add(f.banner().BIG);
			b.textLL(f.name);
			
		}
		
		b.tab(9);
		b.add(RD.RACES().visuals.cRace(reg).appearance().icon);
		b.add(GFORMAT.i(b.text(), RD.RACES().population.get(reg)));
		
		b.NL();
		b.add(UI.icons().s.sword);
		if (RD.OWNER().affiliation.get(g.get()) >= 0.5)
			b.add(GFORMAT.i(b.text(),RD.MILITARY().garrison.get(reg)));
		else
			b.add(b.text().add('?'));
		b.tab(3);
		b.add(UI.icons().s.flag);
		b.add(GFORMAT.perc(b.text(), RD.OWNER().affiliation.getD(reg)));
		b.tab(6);
		b.add(UI.icons().s.flags);
		b.add(GFORMAT.i(b.text(), FACTIONS.player().emissaries.active(EMissionType.SUPPORT_R, reg, null)));
		
		if (reg.faction() != null) {
			b.tab(9);
			FactionNPC f = (FactionNPC) reg.faction();
			if (FACTIONS.DIP().war.is(FACTIONS.player(), reg.faction()))
				b.error(DicGeo.¤¤AtWar);
			else {
				b.add(UI.icons().s.heart);
				b.add(GFORMAT.perc(b.text(), ROpinions.current(f.court().king().roy())));
			}
		}
		
//		b.NL(8);
//		b.add(info);
//		b.NL(8);
//		b.textLL(DicMisc.¤¤garrison);
//		b.NL();
//		if (RD.OWNER().affiliation.get(reg) < 0.5)
//			b.add(b.text().add('?'));
//		else
//			b.add(gar);
//		
//		b.NL(8);
//		b.textLL(DicArmy.¤¤Spoils);
//		b.tab(6);
//		if (RD.OWNER().affiliation.get(reg) < 0.25)
//			b.add(b.text().add('?'));
//		else
//			b.add(GFORMAT.i(b.text(), RD.DEVASTATION().raidCredits(reg)));
		
	}
	
}
