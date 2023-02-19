package game.faction;

import java.io.IOException;

import game.time.TIMECYCLE;
import init.D;
import init.resources.RESOURCE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.info.INFO;
import util.statistics.HistoryResource;

public class FResources extends FactionResource{

	

	private static CharSequence ¤¤In = "in";
	private static CharSequence ¤¤InDesc = "Yearly amount gained";
	public final HistoryResource in;

	private static CharSequence ¤¤InImported = "Imported";
	private static CharSequence ¤¤InImportedD = "How much gained through imports.";	
	public final HistoryResource inImported;
	
	private static CharSequence ¤¤InTaxes = "¤Taxes";
	private static CharSequence ¤¤InTaxesD = "How much gained through taxed regions.";	
	public final HistoryResource inTaxes;

	private static CharSequence ¤¤Out = "Out";
	private static CharSequence ¤¤OutDesc = "Yearly amount lost.";
	public final HistoryResource out;
	
	private static CharSequence ¤¤OutExported = "Exported";
	private static CharSequence ¤¤OutExportedD = "How much loss through exports.";	
	public final HistoryResource outExported;
	

	private static CharSequence ¤¤OutTribute = "Tribute";
	private static CharSequence ¤¤OutTributeD = "How much loss through paying tribute to warlords or other factions.";	
	public final HistoryResource outTribute;

	protected LIST<HistoryResource> ins;
	protected LIST<HistoryResource> outs;

	public final int saved;
	public final TIMECYCLE time;
	
	static {
		D.t(FResources.class);
	}
	
	public FResources(int saved, TIMECYCLE time) {
		this.saved = saved;
		this.time = time;
		in = new HistoryResource(new INFO(¤¤In, ¤¤InDesc), saved, time, false);
		inImported = new InOut(new INFO(¤¤InImported, ¤¤InImportedD), in);
		inTaxes = new InOut(new INFO(¤¤InTaxes, ¤¤InTaxesD), in);
		out = new HistoryResource(new INFO(¤¤Out, ¤¤OutDesc), saved, time, false);
		outExported = new InOut(new INFO(¤¤OutExported, ¤¤OutExportedD), out);
		outTribute = new InOut(new INFO(¤¤OutTribute, ¤¤OutTributeD), out);
		ins = new ArrayList<>(inImported, inTaxes);
		outs = new ArrayList<>(outExported, outTribute);
	}
	
	@Override
	protected void save(FilePutter file) {
		in.save(file);
		for (HistoryResource r : ins)
			r.save(file);
		out.save(file);
		for (HistoryResource r : outs)
			r.save(file);
	}


	@Override
	protected void load(FileGetter file) throws IOException {
		in.load(file);
		for (HistoryResource r : ins)
			r.load(file);
		out.load(file);
		for (HistoryResource r : outs)
			r.load(file);
	}


	@Override
	protected void clear() {
		in.clear();
		for (HistoryResource r : ins)
			r.clear();
		out.clear();
		for (HistoryResource r : outs)
			r.clear();
	}

	
	public final LIST<HistoryResource> ins(){
		return ins;
	}
	
	public final LIST<HistoryResource> outs(){
		return outs;
	}

	@Override
	protected void update(double ds) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	protected class InOut extends HistoryResource{

		private final HistoryResource master;
		private final INFO info;
		
		public InOut(INFO info, HistoryResource total) {
			super(saved, time, false);
			this.master = total;
			this.info = info;
		}
		
		public InOut(CharSequence name, CharSequence desc, HistoryResource total) {
			super(saved, time, false);
			this.master = total;
			this.info = new INFO(name, desc);
		}
		
		public InOut(HistoryResource total) {
			super(saved, time, false);
			this.master = total;
			this.info = null;
		}

		@Override
		protected void change(RESOURCE r, int old, int current) {
			master.inc(r, -old);
			master.inc(r, current);
		}
		
		@Override
		public INFO info() {
			return info;
		}
		
		
	}
	
	
}
