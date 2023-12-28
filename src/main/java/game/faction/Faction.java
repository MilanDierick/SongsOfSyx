package game.faction;

import java.io.IOException;

import game.GAME;
import game.GameDisposable;
import game.boosting.*;
import game.faction.trade.FACTION_EXPORTER;
import game.faction.trade.FACTION_IMPORTER;
import init.race.Race;
import init.resources.RESOURCES;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.Str;
import util.dic.DicArmy;
import world.WORLD;
import world.army.AD;
import world.army.FactionArmies;
import world.regions.Region;
import world.regions.data.RD;
import world.regions.data.Realm;

public abstract class Faction implements BOOSTABLE_O{

	public final Str name = new Str(48);
	private final int index;
	
	protected Faction(LISTE<Faction> all){
		index = all.add(this);
	}

	public abstract Race race();
	
	

	public final int index() {
		return index;
	}
	
	public final Region capitolRegion() {
		return RD.REALM(this).capitol();
	}

	protected void save(FilePutter file) {
		name.save(file);
		res().save(file);
		banner().save(file);
		credits().save(file);
		ruler().save(file);
	}

	protected void load(FileGetter file) throws IOException {
		name.load(file);
		res().load(file);
		banner().load(file);
		credits().load(file);
		ruler().load(file);
	}

	protected void clear() {
		name.clear();
		res().clear();
		banner().clear();
		credits().clear();
	}

	protected void update(double ds) {
		res().update(ds, this);
		banner().update(ds, this);
		credits().update(ds, this);
	}
	
	public abstract boolean isActive();
	
	protected abstract void setActive(boolean active);

	public Realm realm() {
		return RD.REALM(this);
	}
	
	public abstract FACTION_IMPORTER buyer();
	
	public abstract FACTION_EXPORTER seller();
	
	public abstract FResources res();

	public abstract FBanner banner();
	
	public abstract FCredits credits();
	
	public FactionArmies armies() {
		return WORLD.ARMIES().army(this);
	}

	public abstract FRuler ruler();

	public static abstract class FactionActivityListener {
		
		public static final LinkedList<FactionActivityListener> all = new LinkedList<>();
		static {
			new GameDisposable() {
				
				@Override
				protected void dispose() {
					all.clear();
				}
			};
		}
		
		public FactionActivityListener() {
			all.add(this);
		}
		
		public abstract void remove(Faction f);
		public abstract void add(Faction f);
	}
	
	@Override
		public String toString() {
			return "[" + index + "]" + name;
		}
	
	int upI = -1;
	private double powerCache = 0;
	
	public double power() {
		if (upI == GAME.updateI())
			return powerCache;
		return powerW();
	}
	
	public double powerW() {
		upI = GAME.updateI();
		double d = RD.RACES().population.faction().getD(this)*0.25;
		if (this != FACTIONS.player() && SETT.INVADOR().invading() && FACTIONS.otherFaction() == this) {
			d += RESOURCES.ALL().size()*10000;
		}
		d += AD.power().get(this);
		d += res().get(null)*0.05;
		powerCache = d;
		return powerCache;
	}
	

	
	@Override
	public double boostableValue(Boostable bo, BValue v) {
		return v.vGet(this);
	}
	
	public static CharSequence name(Faction f) {
		if (f != null)
			return f.name;
		return DicArmy.¤¤Rebels;
	}

	
}
