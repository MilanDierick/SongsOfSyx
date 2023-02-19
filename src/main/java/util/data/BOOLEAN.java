package util.data;

public interface BOOLEAN {

	public boolean is();
	
	public interface BOOLEAN_MUTABLE extends BOOLEAN{
		
		public BOOLEAN_MUTABLE set(boolean b);
		
		public default BOOLEAN_MUTABLE toggle() {
			return set(!is());
		}
		
		public default BOOLEAN_MUTABLE setOn() {
			return set(true);
		}
		
		public default BOOLEAN_MUTABLE setOff() {
			return set(false);
		}
	}
	
}
