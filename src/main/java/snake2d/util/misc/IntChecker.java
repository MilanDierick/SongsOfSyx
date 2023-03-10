package snake2d.util.misc;

public class IntChecker {

	private final short[] check;
	private short checkI = 0;
	
	
	public IntChecker(int size) {
		check = new short[size];
	}
	
	public IntChecker init(){
		
		checkI ++;
		if (checkI == 0){
			for (int i = 0; i < check.length; i++)
				check[i] = 0;
			checkI = 1;
		}
		return this;
	}
	
	/**
	 * 
	 * @param c
	 * @return true if component previously has been set
	 */
	public boolean isSet(int i){
		return check[i] == checkI;
	}
	
	public void unset(int i) {
		check[i] = (short) (checkI -1);
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param c
	 * @return true if component previously has been set
	 */
	public boolean isSetAndSet(int i){
		if (!isSet(i)) {
			check[i] = (short) (checkI);
			return false;
		}
		return true;
	}
	
}