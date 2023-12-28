package game.faction.diplomacy;

import java.io.Serializable;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import init.resources.RESOURCES;
import world.WORLD;
import world.regions.Region;

public final class DealSave implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int fi;
	private final int fii;
	public final boolean[] bools;
	public final Party player;
	public final Party npc;
	
	public DealSave(Deal deal){
		fi = deal.faction().index();
		fii = deal.faction().iteration();
		bools = new boolean[deal.bools().size()];
		for (int i = 0; i < bools.length; i++) {
			bools[i] = deal.bools().get(i).b;
		}
		player = new Party(deal.player);
		npc = new Party(deal.npc);
		
	}
	
	public boolean set(Deal deal) {
		FactionNPC npc = f();
		if (npc == null)
			return false;
		deal.setFactionAndClear(npc);
		for (int i = 0; i < bools.length; i++) {
			deal.bools().get(i).set(bools[i]);
		}
		return player.set(deal.player) && this.npc.set(deal.npc);
	}
	
	public FactionNPC f() {
		Faction f = FACTIONS.getByIndex(fi);
		if (f == null || !f.isActive() || !(f instanceof FactionNPC))
			return null;
		FactionNPC npc = (FactionNPC) f;
		if (npc.iteration() != fii)
			return null;
		return npc;
	}
	
	public static final class Party implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public final int creditsP;
		public final int[] regsP;
		public final int[] resP;
		
		Party(DealParty p){
			creditsP =p.credits.get();
			regsP = new int[p.regs.size()];
			for (int i = 0; i < regsP.length; i++) {
				regsP[i] = i < p.regs.size() ? p.regs.get(i).index() : -1;
			}
			resP = new int[RESOURCES.ALL().size()];
			for (int i = 0; i < resP.length; i++) {
				resP[i] = p.resources.get(RESOURCES.ALL().get(i));
			}
		}
		
		boolean set(DealParty p) {
			if (creditsP > p.credits.max())
				return false;
			p.credits.set(creditsP);
			for (int i : regsP) {
				if (i != -1) {
					Region reg = WORLD.REGIONS().getByIndex(i);
					if (!reg.active() || reg.faction() != p.f.get())
						return false;
					p.regs.add(reg);
				}
			}
			for (int i = 0; i < resP.length; i++) {
				if (resP[i] > p.resources.max(RESOURCES.ALL().get(i)))
					return false;
				p.resources.set(RESOURCES.ALL().get(i), resP[i]);
			}
			return true;
		}
		
	}
	
}