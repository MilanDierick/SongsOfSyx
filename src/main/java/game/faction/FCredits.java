package game.faction;

import java.io.IOException;

import game.time.TIME;
import game.time.TIMECYCLE;
import init.D;
import init.resources.RESOURCE;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.info.INFO;
import util.statistics.*;

public class FCredits extends FactionResource{

	private double credits;
	private double inf = 0;

	private static CharSequence ¤¤Treasury = "¤Treasury";
	private static CharSequence ¤¤TreasuryD = "¤The amount of Denarii at disposal.";
	private final HistoryInt creditsH;
	
	private static CharSequence ¤¤InExported = "¤Exported";
	private static CharSequence ¤¤InExportedD = "¤Moneys earned from exports.";
	public final HistoryResource inExported;
	
	private static CharSequence ¤¤OutImported = "¤Imported";
	private static CharSequence ¤¤OutImportedD = "¤Moneys spent on imports.";
	public final HistoryResource outImported;
	
	private static CharSequence ¤¤Trade = "¤Trade";
	private static CharSequence ¤¤TradeD = "¤Profits / losses due to trade.";
	public final CredHistory trade;
	
	private static CharSequence ¤¤Inflation = "¤Inflation";
	private static CharSequence ¤¤InflationD = "¤Inflation is applied to positive and negative treasuries. Each year a negative treasury will have 10% of its debt removed. A positive treasury will have 10% removed.";
	public final CredHistory inflation;
	
	private static CharSequence ¤¤Purchases = "¤Purchases";
	private static CharSequence ¤¤PurchasesD = "¤Special purchases/sales such as from the slave trader.";
	public final CredHistory purchases;
	
	private static CharSequence ¤¤Tribute = "¤Tribute";
	private static CharSequence ¤¤TributeD = "¤Denari spent/gained from paying off other armies and factions.";
	public final CredHistory tribute;
	
	static {
		D.ts(FCredits.class);
	}
	
	protected LIST<SAVABLE> saves;
	protected LIST<CredHistory> all;
	
	public final int saved;
	public final TIMECYCLE time;
	
	
	public FCredits(int saved, TIMECYCLE time) {
		this.time = time;
		this.saved = saved;
		creditsH = new HistoryInt(¤¤Treasury, ¤¤TreasuryD, saved, time, true);
		trade = new CredHistory(¤¤Trade, ¤¤TradeD, new ColorImp(89, 0, 127));
		inflation = new CredHistory(¤¤Inflation, ¤¤InflationD, new ColorImp(94, 36, 0));
		purchases = new CredHistory(¤¤Purchases, ¤¤PurchasesD, new ColorImp(68, 100, 0));
		tribute = new CredHistory(¤¤Tribute, ¤¤TributeD, new ColorImp(94, 94, 0));
		inExported = new HistoryResource(new INFO(¤¤InExported, ¤¤InExportedD), saved, time, false) {
			@Override
			protected void change(RESOURCE r, int old, int current) {
				trade.IN.inc(-old);
				trade.IN.inc(current);
			};
		};
		outImported = new HistoryResource(new INFO(¤¤OutImported, ¤¤OutImportedD), saved, time, false) {
			@Override
			protected void change(RESOURCE r, int old, int current) {
				trade.OUT.inc(-old);
				trade.OUT.inc(current);
			};
		};
		

		saves = new ArrayList<SAVABLE>(creditsH, inExported, outImported, trade.saver, inflation.saver, purchases.saver, tribute.saver);
		all = new ArrayList<>( trade, inflation, purchases, tribute);
		
		
	}
	
	@Override
	protected void save(FilePutter file) {
		for (SAVABLE i : saves)
			i.save(file);
		file.d(credits);
		file.d(inf);
	}


	@Override
	protected void load(FileGetter file) throws IOException {
		for (SAVABLE i : saves)
			i.load(file);
		credits = file.d();
		inf = file.d();
	}


	@Override
	protected void clear() {
		for (SAVABLE i : saves)
			i.clear();
		credits = 0;
		inf = 0;
	}

	@Override
	protected void update(double ds) {
		
		inf -= (credits*0.1*ds/TIME.years().bitSeconds());
		int i = (int) inf;
		inf -= i;
		
		if (i < 0) {
			inflation.OUT.inc(-i);
		}else if (i > 0) {
			inflation.IN.inc(i);
		}
		
	}
	
	public HISTORY_INT creditsH() {
		return creditsH;
	}
	
	public double credits() {
		return credits;
	}
	
	protected void inccc(double amount) {
		credits += amount;
		creditsH.set((int) credits);
	}
	
	public LIST<CredHistory> ALL(){
		return all;
	}

	public class CredHistory {

		public final INFO info;
		public final COLOR color;
		public final HistoryInt IN;
		public final HistoryInt OUT;
		
		public CredHistory(INFO info, COLOR color) {
			IN = new HistoryInt(saved, time, false) {
				@Override
				protected void change(int old, int current) {
					inccc(current-old);
				}
			};
			OUT = new HistoryInt(saved, time, false) {
				@Override
				protected void change(int old, int current) {
					
					inccc(-(current-old));
				}
			};
			this.info = info;
			this.color = color;
		}
		
		public CredHistory(CharSequence name, CharSequence desc, COLOR color) {
			this(new INFO(name, desc), color);
		}

		public final SAVABLE saver = new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				IN.save(file);
				OUT.save(file);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				IN.load(file);
				OUT.load(file);
			}
			
			@Override
			public void clear() {
				IN.clear();
				OUT.clear();
			}
		};
		
		
	}
	
	
	
}
