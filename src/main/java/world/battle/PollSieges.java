package world.battle;

import java.io.IOException;
import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.D;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.*;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListInt;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GButt;
import view.main.VIEW;
import view.ui.message.MessageSection;
import view.ui.message.MessageText;
import world.WORLD;
import world.entity.army.WArmy;
import world.log.WLogger;
import world.regions.Region;
import world.regions.WREGIONS;
import world.regions.centre.WCentre;

final class PollSieges implements SAVABLE{

	private final ArrayListInt besieged = new ArrayListInt(WREGIONS.MAX);
	private final ArrayListInt toProcess = new ArrayListInt(WREGIONS.MAX);
	private final Bitmap1D processMap = new Bitmap1D(WREGIONS.MAX, false);
	private final Bitmap1D besigedMap = new Bitmap1D(WREGIONS.MAX, false);
	private final double[] besigeTime = new double[WREGIONS.MAX];
	private double dd;
	private static double dTime = 32;
	private static double dTimeI = WREGIONS.MAX/dTime;
	
	private static CharSequence ¤¤underSiege = "Settlement under siege!";
	private static CharSequence ¤¤underSiegeC = "Capital under siege!";
	private static CharSequence ¤¤underSiegeD = "{0} is under siege by {1} forces. It will hold out for as long as it can, but reinforcements should be dispatched immediately.";
	private static CharSequence ¤¤show = "Show";

	private static CharSequence ¤¤siegeReady = "¤Ripe Siege";
	private static CharSequence ¤¤siegeReadyD = "¤Milord, the general of army '{0}' reports that the besieged town of {1} has now all run out of fortitude and is ripe for an attack.";
	
	static {
		D.ts(PollSieges.class);
	}

	public PollSieges() {

	}
	
	@Override
	public void save(FilePutter file) {
		besieged.save(file);
		toProcess.save(file);
		processMap.save(file);
		besigedMap.save(file);
		file.ds(besigeTime);
		file.d(dd);
	}
	@Override
	public void load(FileGetter file) throws IOException {
		besieged.load(file);
		toProcess.load(file);
		processMap.load(file);
		besigedMap.load(file);
		file.ds(besigeTime);
		dd = file.d();
	}
	@Override
	public void clear() {
		besieged.clear();
		toProcess.clear();
		processMap.clear();
		besigedMap.clear();
		Arrays.fill(besigeTime, 0);
		dd = 0;
	}

	public void update(double ds) {
		
		for (int i = 0; i < besieged.size(); i++) {
			int ri = besieged.get(i);
			Region reg = WORLD.REGIONS().getByIndex(ri);
			if (getBesieger(reg) == null) {
				besieged.remove(i);
				besigedMap.set(ri, false);
				i--;
			}
		}
		
		int current = (int) dd;
		dd += ds*dTimeI;
		int next = (int) dd;
		
		while(current < next) {
			
			int ri = current % WREGIONS.MAX;
			current ++;
			Region reg = WORLD.REGIONS().getByIndex(ri);
			if (!reg.active())
				continue;
			WArmy a = getBesieger(reg);
			if (a != null) {
				double o = besigeTime[ri];
				besigeTime[ri] += dTime;
				if (a.faction() == FACTIONS.player()) {
					if (o < TIME.secondsPerDay*16 && besigeTime[ri] > TIME.secondsPerDay*16) {
						new MessageText(¤¤siegeReady).paragraph(Str.TMP.clear().add(¤¤siegeReadyD).insert(0, a.name).insert(1, reg.info.name())).send();
					}
				}else {
					besige(a, reg);
				}
			}else {
				besigeTime[ri] -= dTime*4;
			}
			besigeTime[ri] = CLAMP.d(besigeTime[ri], 0, Double.MAX_VALUE/2);
			
		}
		
		while(dd >= WREGIONS.MAX)
			dd -= WREGIONS.MAX;
		
	}
	
	private Rec fillBounds = new Rec(WCentre.TILE_DIM*2);
	
	private Siege res = new Siege();
	
	public Siege next() {
		
		while (toProcess.size() > 0) {
			int ri = toProcess.get(toProcess.size()-1);
			Region reg = WORLD.REGIONS().getByIndex(ri);
			WArmy a = getBesieger(reg);
			if (a != null) {
				res.besieger = a;
				res.reg = reg;
				res.time = besigeTime[ri];
				return res;
			}
			skip();
		}
		return null;
	}
	
	public void skip() {
		int ri = toProcess.remove(toProcess.size()-1);
		processMap.set(ri, false);
	}
	
	private WArmy getBesieger(Region reg) {
		if (!reg.active())
			return null;
		fillBounds.moveC(reg.cx(), reg.cy());
		for (WArmy a : WORLD.ENTITIES().armies.fillTiles(fillBounds)) {
			if (a.besieging(reg)) {
				return a;
			}
		}
		return null;
	}

	public double besigedTime(Region reg) {
		return besigeTime[reg.index()];
	}
	
	public boolean besiged(Region reg) {
		return besigedMap.get(reg.index());
	}
	
	public void besige(WArmy a, Region reg) {
		
		if (!besigedMap.get(reg.index())) {
			besigedMap.set(reg.index(), true);
			besieged.add(reg.index());
			WLogger.besiege(a, reg);
			if (reg.faction() == FACTIONS.player()) {
				new MessageSiege(reg, a.faction());
			}
		}
			
		if (!processMap.get(reg.index())) {
			processMap.set(reg.index(), true);
			toProcess.add(reg.index());
			
		}
	}
	
	private final static class MessageSiege extends MessageSection{

		private final int regI;
		private final String paragraph;
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public MessageSiege(Region r, Faction f) {
			super(¤¤underSiege);
			regI = r.index();
			if (FACTIONS.player().capitolRegion() == r)
				paragraph = "" + ¤¤underSiegeC;
			else
				paragraph = "" + new Str(¤¤underSiegeD).insert(0, r.info.name()).insert(1, Faction.name(f));
			send();
		}

		@Override
		protected void make(GuiSection section) {
			paragraph(paragraph);
			
			section.addDownC(16, new GButt.ButtPanel(¤¤show) {
				@Override
				protected void clickA() {
					VIEW.world().activate();
					VIEW.world().UI.regions.open(WORLD.REGIONS().getByIndex(regI));
				}
			});
			
		}
		
		
		
	}
	
	public final static class Siege {
		
		public Region reg;
		public WArmy besieger;
		public double time;
		
		private Siege() {
			
		}
		
		
		
	}
	
	
}
