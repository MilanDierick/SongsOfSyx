package game.faction.player;

import game.GameDisposable;
import snake2d.util.color.ColorImp;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LinkedList;

public class PlayerColors {

	private final static KeyMap<LinkedList<PlayerColor>> cats = new KeyMap<>();
	
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				cats.clear();
			}
		};
	}
	
	public static KeyMap<LinkedList<PlayerColor>> cats(){
		return cats;
	}
	
	public static class PlayerColor {
		
		public final ColorImp color;
		public final CharSequence name;
		public final String cat;
		public final String key;
		
		public PlayerColor(String key, CharSequence category, CharSequence name){
			this(new ColorImp(), key, category, name);
		}
		
		public PlayerColor(ColorImp col, String key, CharSequence category, CharSequence name){
			this.key = key;
			this.cat = ""+category;
			this.name = name;
			this.color = col;
			if (!cats.containsKey(cat))
				cats.put(cat, new LinkedList<>());
			cats.get(cat).add(this);;
		}
		
	}
	
}
