package snake2d;

import java.io.PrintStream;

import snake2d.util.sets.LinkedList;

public final class LOG {

	private static final Log l = new Log();
	private LOG() {
		
	}
	
	private static String lasth;
	private static int li = 0;
	public static void ln() {
		System.out.println();
	}
	
	private static void header(PrintStream o) {
		StackTraceElement[] ee = new RuntimeException().getStackTrace();
		String s = ee[2].getClassName();
		String ln = "";
		if (s.contains(".")) {
			String ss[] = s.split("\\.");
			s = ss[ss.length-1] +":";
			ln = ""+ee[2].getLineNumber();
		}
		if (!s.equals(lasth) || li > 250) {
			o.println();
			o.print("[GAME]" );
			o.println(s+ln);
			li = 0;
		}
		li++;
		lasth = s;
	}
	
	public static void ln(Object info) {
		
		header(System.out);
		
		System.out.print("\t");
		System.out.println(info);

	}
	
	public static void ln(Object a, Object b) {
		
		header(System.out);
		
		System.out.print("\t");
		System.out.println(a);
		System.out.print("\t");
		System.out.println(b);
	}
	
	public static void ln(Object a, Object b, Object c) {
		
		header(System.out);
		
		System.out.print("\t");
		System.out.println(a);
		System.out.print("\t");
		System.out.println(b);
		System.out.print("\t");
		System.out.println(c);
	}
	
	public static void ln(Object[] info) {
		header(System.out);
		for (Object oo : info) {
			System.out.print("\t");
			System.out.print(oo);
		}
		System.out.println();
	}
	
	public static void err(Object info) {
		
		header(System.err);
		
		System.err.print("\t");
		System.err.println(info);

	}
	
	public static void err(Object[] info) {
		header(System.err);
		for (Object oo : info) {
			System.err.print("\t");
			System.err.print(oo);
		}
		System.err.println();
	}
	
	public static String bits(long l) {
		String s = "";
		int sp = 0;
		for (int bi = 0; bi < 64; bi++) {
			if ((sp == 8)) {
				s += "_";
				sp = 0;
			}
			long m = 1l << (63-bi);
			
			if ((l & m) != 0) {
				s += "1";
			}else {
				s += "0";
			}
			sp++;
		}
		return s;
	}
	
	public static Log l() {
		l.bb.clear();
		return l;
	}
	
	public static class Log implements AutoCloseable {

		private LinkedList<String> bb = new LinkedList<String>();
		private String h =  new RuntimeException().getStackTrace()[2].getClassName();
		@Override
		public void close() {
			if (bb.isEmpty())
				return;
			if (h.contains(".")) {
				String ss[] = h.split("\\.");
				h = ss[ss.length-1];
			}
			
			System.out.println(h);
			while(!bb.isEmpty()) {
				System.out.print("\t");
				System.out.println(bb.removeFirst());
			}
		}
		
		public void ln(Object info) {
			
			bb.add(""+info);
		}
		
		public void ln(Object... info) {
			for (Object oo : info) {
				bb.add(""+oo); 
			}
		}
		
		public void ln() {
			bb.add(System.lineSeparator());
		}
	}
	
}
