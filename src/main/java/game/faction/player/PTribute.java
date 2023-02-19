package game.faction.player;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.D;
import init.sprite.UI.UI;
import snake2d.util.file.*;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.gui.misc.GHeader;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.main.MessageSection;

public final class PTribute implements SAVABLE{

	private byte year = -1;
	
	private static CharSequence ¤¤Tribute = "¤Yearly Tribute";
	private static CharSequence ¤¤TributeIn = "¤Our yearly tribute from our vassals has arrived!";
	private static CharSequence ¤¤TributeOut = "¤Our yearly tribute has been deducted from our treasury and payed to our overlord.";
	
	static {
		D.ts(PTribute.class);
	}
	
	@Override
	public void save(FilePutter file) {
		file.b(year);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		year = file.b();
	}

	@Override
	public void clear() {
		year = -1;
		
	}
	
	void update(double ds) {
		byte year = (byte) (TIME.years().bitCurrent() & 0x0FF);
		if (year == this.year)
			return;
		
		this.year = year;
		
		int ins = 0;
		int outs = 0;
		
		for (Faction f : FACTIONS.NPCs()) {
			if (FACTIONS.rel().overlord.get(FACTIONS.player(), f) == 1) {
				ins ++;
			}
			if (FACTIONS.rel().vassalTo.get(FACTIONS.player(), f) == 1) {
				outs ++;
			}
		}
		
		if (ins > 0) {
			int[] factions = new int[ins];
			int[] ams = new int[ins];
			for (FactionNPC f : FACTIONS.NPCs()) {
				if (FACTIONS.rel().overlord.get(FACTIONS.player(), f) == 1) {
					ins--;
					factions[ins] = f.index();
					int am = (int) (f.credits().trueCredits()*0.05);
					f.credits().tribute.OUT.inc(am);
					FACTIONS.player().credits().tribute.IN.inc(am);
					ams[ins] = am;
				}
			}
			
			new Message(factions, ams, true).send();;
			
		}
		
		if (outs > 0) {
			int[] factions = new int[outs];
			int[] ams = new int[outs];
			for (FactionNPC f : FACTIONS.NPCs()) {
				if (FACTIONS.rel().vassalTo.get(FACTIONS.player(), f) == 1) {
					outs--;
					factions[outs] = f.index();
					int am = (int) (FACTIONS.player().credits().credits()*0.25);
					FACTIONS.player().credits().tribute.OUT.inc(am);
					f.credits().tribute.IN.inc(am);
					ams[ins] = -am;
				}
			}
			
			new Message(factions, ams, false).send();
			
		}
		
	}
	
	private static class Message extends MessageSection {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int[] factions;
		private final int[] amounts;
		private final boolean in;
		
		Message(int[] factions, int[] amounts, boolean in){
			super(¤¤Tribute);
			this.factions = factions;
			this.amounts = amounts;
			this.in = in;
		}
		
		@Override
		protected void make(GuiSection section) {
			paragraph(in ? ¤¤TributeIn : ¤¤TributeOut);
			
			for (int i = 0; i < factions.length; i++) {
				Faction f = FACTIONS.getByIndex(factions[i]);
				int am = amounts[i];
				RENDEROBJ r = new GHeader.HeaderHorizontal(f.appearence().name(), GFORMAT.i(new GText(UI.FONT().M, 16), am));
				section.add(r, section.body().x1() + 32, section.getLastY2()+8);
			}
			
		}
		
		
	}


	
	
}
