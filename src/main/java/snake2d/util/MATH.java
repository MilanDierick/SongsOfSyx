package snake2d.util;

public final class MATH {

	public final static MPOW pow15 = new MPOW(1.5, 64);
	
	private MATH() {
		
	}
	
	public static int mod(int a, int m) {
		int remainder = (a % m);
		a = ((remainder >> 31) & m) + remainder;
		return a;
	}
	
	public static double distance(double from, double to, double max) {
		if (to < from)
			return max-from + to;
		return to-from;
	}
	
	public static int distance(int start, int current, int max) {
		if (current < start)
			return max-start + current;
		return current-start;
	}
	
	/**
	 * Math.pow(x,2) is quicker than this fix
	 * Math.sqrt is almost as quick
	 * @param d
	 * @param pow2
	 * @return
	 */
	public final static class MPOW {
		
		public final double pow;
		
		private final double[] pows; 
		
		public MPOW(double pow, int precistion){
			pows = new double[precistion];
			this.pow = pow;
			for (int i = 0; i < precistion; i++) {
				double d = (double)i/precistion;
				pows[i] = Math.pow(d, pow);
			}
		}
		
		public double pow(double d) {
			int prec = pows.length-1;
			double ii = (d*prec);
			int i = (int) ii;
			if (i < 0)
				return 0;
			if (i >= prec)
				return 1.0;
			
			ii-= i;
			double res = pows[i]*(1-ii);
			res += pows[i+1]*ii;
			
			return res;
		}
		
	}
	
}
