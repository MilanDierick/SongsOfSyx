package script;

import java.io.IOException;

import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.gui.misc.GBox;
import view.keyboard.KEYS;

/**
 * 
 * A dynamic script one can add to the game.
 *
 */
public interface SCRIPT {
	
	/**
	 * 
	 * @return name of the script
	 */
	public CharSequence name();
	
	/**
	 * 
	 * @return
	 */
	public CharSequence desc();
	/**
	 * Will be called before the game has had a chance to become initiated. One could
	 * potentially use reflection here to do funky stuff, but common practise is to do nothing.
	 */
	public void initBeforeGameCreated();
	
	/**
	 * 
	 * @return the script instance that will be actually updated by the game.
	 */
	public SCRIPT_INSTANCE initAfterGameCreated();
	
	/**
	 * A Script is a factory, responisble of creating this. This is where most logic should go.
	 * @author Jake
	 *
	 */
	public interface SCRIPT_INSTANCE {
		
		/**
		 * Called after each tick. A tick is an update of the game. the game is typically
		 * updated 60 times per second. Check your conditions here, and act on them. 
		 * @param ds - how many seconds of in-game time that has passed since the previous update.
		 */
		abstract void update(double ds);
		
		/**
		 * Use the fileputter here to save away any counters and variables
		 * @param file
		 */
		public abstract void save(FilePutter file);
		/**
		 * load your counters and variables back.
		 * @param file
		 * @throws IOException
		 */
		public abstract void load(FileGetter file) throws IOException;
		
		/**
		 * A chance to manipulate the tooltip shown when hovering something.
		 * @param mouseTimer
		 * @param text
		 */
		public default void hoverTimer(double mouseTimer, GBox text) {
			
		}
		
		/**
		 * Called after the game has rendered. There's a chance here to render something on the screen.
		 * @param r
		 * @param ds
		 */
		public default void render(Renderer r, float ds) {
			
		}
		/**
		 * Listen to a key. You can check if a key is pushed here.
		 * @param key
		 */
		public default void keyPush(KEYS key) {
			
		}
		
		/**
		 * 
		 * @param button the button that has been clicked.
		 */
		public default void mouseClick(MButt button) {
			
		}
		
		/**
		 * 
		 * @return if the script was not loaded correctly, here is a chance to fix the state and return true
		 * if your script can handle that situation. If return false, then the script will not be run.
		 */
		public default boolean handleBrokenSavedState() {
			return false;
		}
		
		/**
		 * if you want to do something when hovering the screen.
		 * @param mCoo
		 * @param mouseHasMoved
		 */
		public default void hover(COORDINATE mCoo, boolean mouseHasMoved) {
			
		}
	}
	
	

	

}
