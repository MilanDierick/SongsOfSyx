package game.faction.npc.ruler;

import init.D;
import snake2d.util.sets.*;

public final class RTraits{

	private final ArrayListGrower<RTrait> all = new ArrayListGrower<>();
	
	{
		D.gInit(this);
	}
	
	public final RTrait war = new RTrait(all,
			new Title(
					D.g("Pacifist"),
					D.g("Bringer of Peace"),
					D.g("peacefulD", "Reluctant about starting and joining any kind of conflict.")
					),
			new Title(
					D.g("Warlike"),
					D.g("The Conqueror"),
					D.g("warriorD", "A seeker or glory that will eagerly find or create a conflict for a chance of battle.")
					)
			);
	public final RTrait pride = new RTrait(all,
			new Title(
					D.g("Calm"),
					D.g("The Modest"),
					D.g("modestD", "Cares little about gifts or slights.")
					),
			new Title(
					D.g("Proud"),
					D.g("The Dignified"),
					D.g("ProudD", "Impacted greatly by flattery and offence.")
					)
			);
	public final RTrait honesty = new RTrait(all,
			new Title(
					D.g("Deceitful"),
					D.g("The Cunning"),
					D.g("cunningD", "Will look for good deals as it suits them, and cares little about integrity and agreements.")
					),
			new Title(
					D.g("Honourable"),
					D.g("Defender of Honour"),
					D.g("HonestD", "Has an integrity of stone, and will stick by treaties and alliances.")
					)
			);
	public final RTrait mercy = new RTrait(all,
			new Title(
					D.g("Cruel"),
					D.g("The Merciless"),
					D.g("mercilessD", "Rules by fear and ruthlessly destroys all opposition.")
					),
			new Title(
					D.g("Merciful"),
					D.g("The Merciful"),
					D.g("MercyD", "Values life and will choose life when possible.")
					)
			
			);
	public final RTrait competence = new RTrait(all,
			new Title(
					D.g("Lazy"),
					D.g("The Drinker"),
					D.g("drinkerD", "Jolly and fun, but state matters suffers as a result.")
					),
			new Title(
					D.g("Ambitious"),
					D.g("The Great"),
					D.g("GreatD", "Makes the city state prosper.")
					)
			);
	public final RTrait tolerance = new RTrait(all,
			new Title(
					D.g("Conservative"),
					D.g("Defender of Tradition"),
					D.g("conservativeD", "Likes things how they've always been. Takes care of its own, and little about other species. Has an understanding for genocides.")
					),
			new Title(
					D.g("Tolerant"),
					D.g("The Great Humanist"),
					D.g("ToleranceD", "A tolerant ruler will appreciate and encourage other races.")
					)
			);
	
	private RTraits(){
		
	}

	private static RTraits self;
	
	public static RTraits get() {
		if (self == null)
			self = new RTraits();
		return self;
	}
	
	public LIST<RTrait> all(){
		return all;
	}
	
	public static class RTrait implements INDEXED{

		private final int index;

		public final Title good;
		public final Title bad;
		
		RTrait(ArrayListGrower<RTrait> all, Title good, Title bad){
			index = all.add(this);
			this.good = good;
			this.bad = bad;
		}
		
		@Override
		public int index() {
			return index;
		}
		
	}
	
	public static class Title {

		public final CharSequence name;
		public final CharSequence desc;
		public final CharSequence title;

		Title(CharSequence name, CharSequence title, CharSequence desc){
			this.name = name;
			this.desc = desc;
			this.title = title;
		}
		
		Title(CharSequence name, CharSequence desc){
			this(name, name, desc);
		}		
	}

	
}
