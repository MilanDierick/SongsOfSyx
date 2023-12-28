package game.events.world;

public final class EventWorld {


	public final EventWorldExpand factionExpand = new EventWorldExpand();
	public final EventWorldBreakoff factionBreak = new EventWorldBreakoff();
	public final EventWorldPopup popup = new EventWorldPopup();
	public final EventWorldWar war = new EventWorldWar();
	public final EventWorldWarPlayer warPlayer = new EventWorldWarPlayer();
	public final EventWorldPeace warPeace = new EventWorldPeace();
	public final EventWorldRaider raider = new EventWorldRaider();
	public final EventWorldRebellion rebellion = new EventWorldRebellion();
	public final EventWorldDip dip = new EventWorldDip();
}
