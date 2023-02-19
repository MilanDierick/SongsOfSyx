package game.faction;

import java.io.IOException;

import game.GAME;
import init.sprite.SPRITES;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import util.dic.DicGeo;
import util.info.INFO;
import view.interrupter.IDebugPanel;
import world.World;

public final class FDiplomacy {

	public final FDiplomacyData war = new FDiplomacyData(1, 
			DicGeo.¤¤AtWar, 
			DicGeo.¤¤AtWarD,
			SPRITES.icons().s.sword, 0) {
		
		@Override
		public void set(Faction a, Faction b, int value) {
			int old = get(a, b);
			setBoth(this, a, b, value);
			if (value == 1) {
				setBoth(vassalTo, a, b, 0);
				setBoth(overlord, a, b, 0);
				setBoth(tradePartner, a, b, 0);
				
			}else {
				setBoth(tradePartner, a, b, 1);
			}
			if (old != value && value == 1)
				World.ai().initiateWar(a, b);
		};
		
	};
	
	public final FDiplomacyData vassalTo = new FDiplomacyData(1, 
			DicGeo.¤¤Vassal, 
			DicGeo.¤¤VassalD,
			SPRITES.icons().s.slave, 0) {
		
		@Override
		public void set(Faction a, Faction b, int value) {
			setP(a, b, value);
			overlord.setP(b, a, value);
			if (value == 1) {
				setBoth(war, a, b, 0);
				setBoth(tradePartner, a, b, 0);
			}
		};
	};
	
	public final FDiplomacyData overlord = new FDiplomacyData(1, 
			DicGeo.¤¤Protector, 
			DicGeo.¤¤ProtectorD,
			SPRITES.icons().s.noble, 0) {
		@Override
		public void set(Faction a, Faction b, int value) {
			for (int i = 0; i < FACTIONS.MAX; i++) {
				if (i != b.index())
					vassalTo.set(b, FACTIONS.getByIndex(i), 0);
			}
			setP(a, b, value);
			vassalTo.setP(b, a, value);
			if (value == 1) {
				setBoth(war, a, b, 0);
				setBoth(tradePartner, a, b, 0);
			}
		};
	};
	
	public final FDIPLOMACY allies = new FDIPLOMACY() {
		
		@Override
		public int get(Faction a, Faction b) {
			return a == b || vassalTo.get(a, b) + overlord.get(a, b) > 0 ? 1 : 0;
		}

		@Override
		public int max() {
			return 1;
		}
	};
	
	public final FDiplomacyData tradePartner = new FDiplomacyData(1, 
			DicGeo.¤¤TradePartner, 
			DicGeo.¤¤TradePartnerD,
			SPRITES.icons().s.urn, 0) {
		
		@Override
		public int get(Faction a, Faction b) {
			return !enemy(a, b) ? 1 : 0;
		};
		
	};
	
	private LIST<FDiplomacyData> all = new ArrayList<>(
			war, vassalTo, overlord, tradePartner);
	
	FDiplomacy() {
		saver.clear();
		
		IDebugPanel.add("Total War", new ACTION() {
			
			@Override
			public void exe() {
				for (Faction f : FACTIONS.all()) {
					if (f != GAME.player())
						war.set(GAME.player(), f, 1);
				}
			}
		});
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			for (FDiplomacyData d : all)
				d.bits.save(file);
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			for (FDiplomacyData d : all)
				d.bits.load(file);
		}
		
		@Override
		public void clear() {
			for (FDiplomacyData d : all)
				d.bits.clear();
			for (FDiplomacyData d : all) {
				d.bits.setAll(d.defaultI);
			}
		}
	};
	
	public LIST<FDiplomacyData> all(){
		return all;
	}

	private void setBoth(FDiplomacyData d, Faction a, Faction b, int value) {
		d.setP(a, b, value);
		d.setP(b, a, value);
	}
	
	public interface FDIPLOMACY {
		public int get(Faction a, Faction b);
		
		public int max();
	}
	
	public class FDiplomacyData implements FDIPLOMACY{
		
		private final Bitsmap1D bits;
		private final INFO info;
		public final int max;
		public final SPRITE icon;
		private final int defaultI;
		
		FDiplomacyData(int bits, CharSequence name, CharSequence desc, SPRITE icon, int defaultI) {
			this.bits = new Bitsmap1D(0, bits, FACTIONS.MAX*FACTIONS.MAX);
			info = new INFO(name, desc);
			this.max = 1 << bits;
			this.icon = icon;
			this.defaultI = defaultI;
		}
		
		@Override
		public int get(Faction a, Faction b) {
			if (a == b)
				return 0;
			if (a == null || b == null)
				return 0;
			int i = a.index() + b.index()*FACTIONS.all().size();
			
			return bits.get(i);
		}
		
		public void set(Faction a, Faction b, int value) {
			setP(a, b, value);
		}
		
		void setP(Faction a, Faction b, int value) {
			if (a == b)
				throw new RuntimeException();
			
			
			int i = a.index() + b.index()*FACTIONS.all().size();
			value = CLAMP.i(value, 0, max);
			bits.set(i, value);
		}
		
		public INFO info() {
			return info;
		}

		@Override
		public int max() {
			return max;
		}
		
		
		
	}

	public boolean ally(Faction a, Faction b) {
		if (a == b)
			return true;
		if (a == null || b == null)
			return false;
		if (FACTIONS.rel().allies.get(a, b) == 1)
			return true;
		return false;
	}
	
	public boolean enemy(Faction a, Faction b) {
		
		if (a == b)
			return false;
		if (a == null && b != null)
			return true;
		if (a != null && b == null)
			return true;
		if (FACTIONS.rel().war.get(a, b) == 1)
			return true;
		return false;
	}

	public void clear(Faction faction) {
		for (Faction f : FACTIONS.all()) {
			if (f == faction)
				continue;
			for (FDiplomacyData d : all) {
				d.setP(faction, f, 0);
				d.setP(f, faction, 0);
			}
				
		}
		
	}


	
}
