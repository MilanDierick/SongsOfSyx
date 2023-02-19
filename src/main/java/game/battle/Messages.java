package game.battle;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.D;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Str;
import util.dic.DicArmy;
import util.gui.misc.GButt;
import view.main.*;
import world.World;
import world.map.regions.Region;

final class Messages {

	private static CharSequence ¤¤underSiege = "Settlement under siege!";
	private static CharSequence ¤¤underSiegeC = "Capitol under siege!";
	private static CharSequence ¤¤underSiegeD = "{0} is under siege by {1} forces. It will hold out for as long as it can, but reinforcements should be dispatched immediately.";
	private static CharSequence ¤¤show = "Show";
	private static CharSequence ¤¤lost = "Settlement lost!";
	private static CharSequence ¤¤lostC = "Region of {0} has fallen to our enemies.";

	private static CharSequence ¤¤factionDestroyed = "Faction Destroyed!";
	private static CharSequence ¤¤factionDestroyedD = "The faction of {0} is no more.";
	
	private static CharSequence ¤¤factionMove = "Capitol Relocated";
	private static CharSequence ¤¤factionMoveD = "The faction of {0} has moved its capitol. Its people still resist.";
	
	private Messages() {
		
	}
	
	static {
		D.ts(Messages.class);
	}
	
	static class MessageSiege extends MessageSection {

		private final int regI;
		private final String paragraph;
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		MessageSiege(Region r, Faction f){
			super(¤¤underSiege);
			regI = r.index();
			if (FACTIONS.player().kingdom().realm().capitol() == r)
				paragraph = "" + ¤¤underSiegeC;
			else
				paragraph = "" + new Str(¤¤underSiegeD).insert(0, r.name()).insert(1, f== null ? DicArmy.¤¤Rebels : f.appearence().name());
			send();
		}
		
		@Override
		protected void make(GuiSection section) {
			paragraph(paragraph);
			
			section.addDownC(16, new GButt.ButtPanel(¤¤show) {
				@Override
				protected void clickA() {
					VIEW.world().activate();
					VIEW.world().UI.region.openList(World.REGIONS().getByIndex(regI));
				}
			});
			
		}
		
		
		
	}
	
	static class MessageFallen extends MessageSection {

		private final int regI;
		private final String paragraph;
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		MessageFallen(Region r){
			super(¤¤lost);
			regI = r.index();
			paragraph = "" + new Str(¤¤lostC).insert(0, r.name());
			send();
		}
		
		@Override
		protected void make(GuiSection section) {
			paragraph(paragraph);
			
			section.addDownC(16, new GButt.ButtPanel(¤¤show) {
				@Override
				protected void clickA() {
					VIEW.world().activate();
					VIEW.world().UI.region.openList(World.REGIONS().getByIndex(regI));
				}
			});
			
		}
		
		
		
	}
	
	static class MessageCapitolMoved extends MessageText {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		MessageCapitolMoved(Faction f){
			super(¤¤factionMove, new Str(¤¤factionMoveD).insert(0, f.appearence().name()));
			send();
		}
		
	}
	
	static class MessageFactionDestroyed extends MessageText {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		MessageFactionDestroyed(Faction f){
			super(¤¤factionDestroyed, new Str(¤¤factionDestroyedD).insert(0, f.appearence().name()));
			send();
		}
		
	}
	
	
}
