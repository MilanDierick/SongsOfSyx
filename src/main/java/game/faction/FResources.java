package game.faction;

import java.io.IOException;

import game.time.TIMECYCLE;
import init.D;
import init.resources.RESOURCE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.statistics.HISTORY_COLLECTION;
import util.statistics.HistoryResource;

public abstract class FResources extends FactionResource{
	
	private static CharSequence ¤¤worn = "¤Furniture";
	private static CharSequence ¤¤theft = "¤Theft";
	
	static {
		D.ts(FResources.class);
	}
	
	private final HistoryResource[] all = new HistoryResource[RTYPE.all.size()*2+1];
	public final TIMECYCLE time;
	
	public FResources(int saved, TIMECYCLE time){
		for (int i = 0; i < all.length; i++)
			all[i] = new HistoryResource(saved, time, false);
		this.time = time;
	}

	
	
	
	public abstract int get(RESOURCE t);

	public HISTORY_COLLECTION<RESOURCE> in(RTYPE t){
		return all[t.ordinal()];
	}
	
	public HISTORY_COLLECTION<RESOURCE> out(RTYPE t){
		return all[RTYPE.all.size() + t.ordinal()];
	}
	
	public HISTORY_COLLECTION<RESOURCE> total(){
		return all[all.length-1];
	}
	
	public void inc(RESOURCE res, RTYPE type, int am) {

		if (am > 0)
			all[type.ordinal()].inc(res, am);
		else
			all[RTYPE.all.size() + type.ordinal()].inc(res, -am);
		all[all.length-1].inc(res, am);
	}
	
	public void dec(RESOURCE res, RTYPE type, int am) {
		inc(res, type, -am);
	}

	@Override
	protected void save(FilePutter file) {
		file.i(all.length);
		for (HistoryResource i : all) {
			i.save(file);
		}
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		int am = file.i();
		if (am != all.length) {
			HistoryResource tmp = all[0];
			for (int i = 0; i < am; i++) {
				tmp.load(file);
			}
			clear();
		}else {
			for (HistoryResource i : all) {
				i.load(file);
			}
		}
		
	}

	@Override
	public void clear() {
		for (HistoryResource i : all) {
			i.clear();
		}
	}

	@Override
	protected void update(double ds, Faction f) {
		// TODO Auto-generated method stub
		
	}
	
	public static enum RTYPE {
		
		PRODUCED(DicMisc.¤¤Production),
		CONSUMED(DicMisc.¤¤Consumed),
		TRADE(DicMisc.¤¤Trade),
		TAX(DicMisc.¤¤taxes),
		CONSTRUCTION(DicMisc.¤¤construction),
		FURNISH(¤¤worn),
		EQUIPPED(DicMisc.¤¤Equipped),
		MAINTENANCE(DicMisc.¤¤Maintenance),
		SPOILAGE(DicMisc.¤¤Spoilage),
		ARMY_SUPPLY(DicArmy.¤¤Supplies + ": " + DicArmy.¤¤Armies),
		SPOILS(DicArmy.¤¤Battle + ": "+ DicArmy.¤¤Spoils),
		DIPLOMACY(DicMisc.¤¤Diplomacy),
		THEFT(¤¤theft),
		
		;
		
		public static final LIST<RTYPE> all = new ArrayList<FResources.RTYPE>(values());
		
		public final CharSequence name;
		
		private RTYPE(CharSequence name) {
			this.name = name;
		}
	}
	
}
