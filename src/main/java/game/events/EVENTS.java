package game.events;

import java.io.IOException;
import java.util.LinkedList;

import game.GAME.GameResource;
import game.GameConRandom;
import game.GameDisposable;
import game.events.advice.EventAdvisor;
import game.events.citizen.EventCitizen;
import game.events.killer.EventKiller;
import game.events.slave.EventUprising;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public final class EVENTS extends GameResource{

	public final EventRaider raider = new EventRaider();
	public final EventSlaver slaver = new EventSlaver();
	public final EventCitizen riot = new EventCitizen();
	public final EventUprising uprising = new EventUprising();
	public final EventDisease disease = new EventDisease();
	public final EventKiller killer = new EventKiller();
	public final EventTemperature temperature = new EventTemperature();
	public final EventFarm farm = new EventFarm();
	public final EventPasture pasture = new EventPasture();
	public final EventOrchard orchard = new EventOrchard();
	public final EventFish fish = new EventFish();
	public final EventRaceTensions raceWars = new EventRaceTensions();
	public final EventAdvisor advice = new EventAdvisor();
	public final EventAccident accident = new EventAccident();
	
	
	public EVENTS() {
		super(false);
	}
	
	@Override
	protected void save(FilePutter file) {
		for (EventResource e : all)
			e.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		for (EventResource e : all)
			e.load(file);
	}

	@Override
	protected void update(float ds) {
		for (EventResource e : all)
			if (!e.supress)
				e.update(ds);
	}
	
	public void generate(GameConRandom random) {
		for (EventResource e : all)
			e.clear();
	}
	
	private static LinkedList<EventResource> all = new LinkedList<>();
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				all = new LinkedList<>();
			}
		};
	}
	
	public static abstract class EventResource{
		
		private boolean supress;
		
		protected EventResource() {
			all.add(this);
		}
		
		protected abstract void update(double ds);

		protected abstract void save(FilePutter file) ;

		protected abstract void load(FileGetter file) throws IOException;

		protected abstract void clear();
		
		/**
		 * will stop the event from updating.
		 */
		public void supress() {
			supress = true;
		}
		
	}

}
