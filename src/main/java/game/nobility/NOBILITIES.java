package game.nobility;

import java.io.IOException;

import game.GAME;
import game.GAME.GameResource;
import game.Profiler;
import game.boosting.*;
import game.faction.npc.FactionNPC;
import game.faction.player.BoostCompound;
import init.D;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import snake2d.Errors;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.updating.IUpdater;
import view.ui.message.MessageText;

public final class NOBILITIES extends GameResource{

	private final LIST<Nobility> all;
	private final IUpdater upper;
	private int active;
	
	public final BoostSpecs boosters;
	public final Boostable MAX;
	private final BoostCompound<Nobility> bos;
	
	public NOBILITIES() {
		MAX = BOOSTING.push("NOBLES_MAX", 0, HCLASS.NOBLE.names, HCLASS.NOBLE.names, UI.icons().s.noble, BOOSTABLES.START());
		boosters = new BoostSpecs(HCLASS.NOBLE.names, UI.icons().s.noble, true);
		
		
		Init init = new Init();
		for (String k : init.pData.getFiles()) {
			new Nobility(k, init);
		}
		all = new ArrayList<>(init.all);
		if (all.size() > 64)
			throw new Errors.DataError("Too many nobilities declared", init.pData.get());

		upper = new IUpdater(all.size(), 10) {
			
			@Override
			protected void update(int i, double timeSinceLast) {
				all.get(i).update(timeSinceLast);
			}
		};

		bos = new BoostCompound<Nobility>(boosters, all) {

			double npc = CLAMP.d(all.size()/20.0, 0, 1);
			
			@Override
			protected BoostSpecs bos(Nobility t) {
				return t.boosters;
			}

			@Override
			protected double get(Boostable bo, FactionNPC f, boolean isMul) {
				return npc*super.get(bo, f, isMul);
			}
			
			@Override
			protected double getValue(Nobility t) {
				if (t.subject() != null) {
					return 0.5 + t.skill()*0.5;
				}
				return 0;
			}
			
			
		};
		
	}
	
	@Override
	protected void save(FilePutter file) {
		for (Nobility n : all)
			n.saver.save(file);
		upper.save(file);
		file.i(active);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		for (Nobility n : all)
			n.saver.load(file);
		upper.load(file);
		active = file.i();
		bos.clearChache();
	}

	@Override
	protected void update(float ds, Profiler prof) {
		prof.logStart(NOBILITIES.class);
		upper.update(ds);
		prof.logEnd(NOBILITIES.class);
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
		bos.clearChache();
		return (byte) e.index();
	}
	
	public void vacate(Humanoid h, byte pos) {
		Nobility e = all.get(pos);
		if (e.subject() != h)
			throw new RuntimeException();
		new MessageText(¤¤title, new Str(¤¤mess).insert(0, GAME.NOBLE().ALL().get(pos).info().name)).send();
		
		e.saver.clear();
		active--;
		bos.clearChache();
	}
	
	public int active() {
		return active;
	}
	
	private static CharSequence ¤¤title = "Nobility passed!";
	private static CharSequence ¤¤mess = "It is a sad day. Our {0} passed today. His position is now vacant and in need of filling.";
	
	static {
		D.ts(NOBILITIES.class);
	}
	
}
