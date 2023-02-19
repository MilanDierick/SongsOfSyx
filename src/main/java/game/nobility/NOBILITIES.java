package game.nobility;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.GAME.GameResource;
import init.D;
import init.boostable.*;
import init.boostable.BOOSTER_COLLECTION.BOOSTER_COLLECTION_IMP;
import init.boostable.BOOSTER_COLLECTION.SIMPLE;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import snake2d.Errors;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.updating.IUpdater;
import view.main.MessageText;

public final class NOBILITIES extends GameResource{

	private final LIST<Nobility> all;
	private final IUpdater upper;
	private int active;
	
	private final Boost boost;
	public final SIMPLE BOOSTER;
	
	public NOBILITIES() {
		Init init = new Init();
		for (String k : init.pData.getFiles()) {
			new Nobility(k, init);
		}
		all = new ArrayList<>(init.all);
		if (all.size() > 64)
			throw new Errors.DataError("Too many nobilities declared", init.pData.get());
		
		boost = new Boost(all);
		BOOSTER = boost;
		
		upper = new IUpdater(all.size(), 10) {
			
			@Override
			protected void update(int i, double timeSinceLast) {
				all.get(i).update(timeSinceLast);
			}
		};

	}
	
	@Override
	protected void save(FilePutter file) {
		for (Nobility n : all)
			n.saver.save(file);
		upper.save(file);
		file.i(active);
		boost.setBonuses();
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		for (Nobility n : all)
			n.saver.load(file);
		upper.load(file);
		active = file.i();
		boost.setBonuses();
	}

	@Override
	protected void update(float ds) {
		upper.update(ds);
		
	}

	public LIST<Nobility> ALL(){
		return all;
	}
	
	public byte assign(Humanoid h, Nobility e) {
		if (e.subject() != null) {
			throw new RuntimeException();
		}
		e.assign(h);
		active++;
		boost.setBonuses();
		return (byte) e.index();
	}
	
	public void vacate(Humanoid h, byte pos) {
		Nobility e = all.get(pos);
		if (e.subject() != h)
			throw new RuntimeException();
		new MessageText(¤¤title, new Str(¤¤mess).insert(0, GAME.NOBLE().ALL().get(pos).info().name)).send();
		
		e.saver.clear();
		active--;
		boost.setBonuses();
	}
	
	public int active() {
		return active;
	}
	
	private static CharSequence ¤¤title = "Nobility passed!";
	private static CharSequence ¤¤mess = "It is a sad day. Our {0} passed today. His position is now vacant and in need of filling.";
	
	static {
		D.ts(NOBILITIES.class);
	}
	
	private class Boost extends BOOSTER_COLLECTION_IMP implements SIMPLE {

		private final double[] add = new double[BOOSTABLES.all().size()];
		private final double[] mul = new double[BOOSTABLES.all().size()];
		
		protected Boost(LIST<Nobility> titles) {
			super(HTYPE.NOBILITY.names);
			for (Nobility t : titles)
				init(t.BOOSTER);
			setBonuses();
		}

		@Override
		public double add(BOOSTABLE b) {
			return add[b.index()];
		}

		@Override
		public double mul(BOOSTABLE b) {
			return mul[b.index()];
		}
		
		private void setBonuses() {
			Arrays.fill(add, 0);
			Arrays.fill(mul, 1);
			
			for (Nobility t : all) {
				if (t.subject() != null) {
					for (BBoost b : t.BOOSTER.boosts()) {
						if (b.isMul())
							mul[b.boost.index()] *= b.value();
						else
							add[b.boost.index()] += b.value();
					}
				}
			}
		}
	}
	
	
	
}
