package snake2d.util.misc;

public interface TOGGLEBLE {
	
	public default void setOn() {
		set(true);
	}
	public default void setOff() {
		set(false);
	}
	public void set(boolean bool);
	public boolean isOn();
	public default void toggle() {
		set(!isOn());
	}
	
	public class Imp implements TOGGLEBLE {

		private boolean value;
		

		@Override
		public boolean isOn() {
			return value;
		}

		@Override
		public void set(boolean bool) {
			value = bool;
		}
		
	}
	
}
