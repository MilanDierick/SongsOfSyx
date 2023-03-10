package snake2d.util.sprite.text;

import java.io.*;
import java.util.Arrays;

import snake2d.util.file.*;

public class Str implements CharSequence{

	public final static Str TMP = new Str(64);
	public final static Str TMP2 = new Str(64);
	private final static Str TMP3 = new Str(64);
	
	protected char[] chars;
	protected int last = 0;
	private final static StringBuilder builder = new StringBuilder(1024);
	private static final String boolT = "true";
	private static final String boolF = "false";
	
	public Str(int size){
		chars = new char[size];
	}
	
	public Str(CharSequence s){
		chars = new char[s.length()];
		add(s);
	}
	
	protected Str() {
		
	}

	@Override
	public char charAt(int index) {
		if (index >= last)
			throw new IndexOutOfBoundsException(Integer.toString(index));
		return chars[index];
	}
	
	public Str setMaxChars(int max) {
		if (last > max) {
			last = max;
			chars[max-1] = '.';
			chars[max-2] = '.';
		}
		return this;
	}

	@Override
	public int length() {
		return last;
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return new String(chars, start, end-start);
	}
	
	public Str add(CharSequence string){
		add(string, string.length());
		return this;
	}
	
	public Str add(CharSequence string, int length){
		if (string.length() == 0)
			return this;
		
		if (length > string.length())
			length = string.length();
		
		if (last + length > chars.length)
			resize(last+length);
		
		int i = 0;
		while(i < length){
			chars[last++] = string.charAt(i++);
		}
		return this;
	}
	
	public Str add(CharSequence string, int start, int end){
		if (string.length() == 0)
			return this;
		
		if (last + end - start > chars.length)
			resize(last+string.length());
		
		int i = start;
		if (end > string.length())
			end = string.length();
		while(i < end){
			chars[last++] = string.charAt(i++);
		}
		return this;
	}
	
	public Str add(long i) {
		if (i < 0) {
			add('-');
			i = -i;
		}
		
		if (i == 0) {
			return add('0');
		}
		
		builder.setLength(0);
		
		while(i > 0) {
			builder.append((char) ('0' + i%10));
			i /= 10;
		}
		
		builder.reverse();
		
		for (int j = 0; j < builder.length(); j++) {
			add(builder.charAt(j));
		}
		
		return this;
	}
	
	public Str add(double d) {
		
		return add(d, 3);
		
	}
	
	public Str add(double d, int decimals) {
		
		if (!Double.isFinite(d)) {
			return add('N').add('a').add('N');
		}

		if (d < 0) {
			add('-');
			d= -d;
		}
		
		int mm = (int) Math.pow(10, decimals);
		
		int full = (int) (Math.round(d*(mm*10)))/10;
		
		int f = full/mm;
		
		add(f);
		add('.');
		full -= f*mm;
		
		while(decimals > 0) {
			decimals--;
			mm = (int) Math.pow(10, decimals);
			f = full/mm;
			add(f);
			full -= f*mm;
		}
		
		return this;
	}

	public Str add(char chare){
		if (last >= chars.length-1)
			resize(last+2);
		chars[last++] = chare;
		return this;
	}
	
	public Str add(boolean b) {
		return add(b ? boolT : boolF);
	}
	
	public Str addBinary(int bin) {
		
		long m = 0x000000080000000l;
		long b = bin & 0x00000000FFFFFFFFl;
		
		for (int i = 0; i < 32; i++) {
			char c = (b & m) == m ? '1' : '0';
			
			add(c);
			m = m >> 1;
		}
		return this;
	}
	
	public Str addBinary(long bin) {
		
		long m = 0x8000000000000000l;
		long b = bin & 0xFFFFFFFFFFFFFFFFl;
		
		for (int i = 0; i < 64; i++) {
			char c = (b & m) == m ? '1' : '0';
			
			add(c);
			m = m >> 1;
		}
		return this;
	}
	

	
	public Str clear(){
		last = 0;
		return this;
	}
	
	/**
	 * space
	 * @return
	 */
	public Str s() {
		add(' ');
		return this;
	}
	
	/**
	 * spaces
	 * @param a
	 * @return
	 */
	public Str s(int a) {
		for (int i = 0; i < a; i++)
			add(' ');
		return this;
	}
	
	public void clearLast() {
		last--;
		if (last < 0)
			last = 0;
	}
	
	public int spaceLeft() {
		return chars.length - last-1;
	}
	
	public int capacity() {
		return chars.length;
	}
	
	private void resize(int newsize){
		char[] newc = new char[newsize];
		for (int i = 0; i < chars.length; i++){
			newc[i] = chars[i];
		}
		chars = newc;
	}

	@Override
	public String toString() {
		return new String(Arrays.copyOf(chars, last));
	}
	
	public Str toCamel() {
		boolean big = true;
		for (int i = 0; i < last; i++) {
			if (chars[i] == ' ')
				big = true;
			else if(big) {
				big = false;
				if (chars[i] >= 'a' && chars[i] <= 'z') {
					chars[i] -= 32;
				}
			}else {
				if (chars[i] >= 'A' && chars[i] <= 'Z') {
					chars[i] += 32;
				}
			}
		}
		return this;
	}
	
	public Str toLower() {
		for (int i = 0; i < last; i++) {
			if (chars[i] >= 'A' && chars[i] <= 'Z') {
				chars[i] += 32;
			}
		}
		return this;
	}
	
	private final static Str TMPMATCH = new Str(64);
	
	public Str insert(int index, CharSequence v) {
		
		TMPMATCH.clear().add('{').add(index).add('}');
		return insert(v);
	}
	
	public Str insert(CharSequence key, CharSequence v) {
		
		TMPMATCH.clear().add('{').add(key).add('}');
		return insert(v);
		
	}
	
	public boolean hasinsert(CharSequence key) {
		
		TMPMATCH.clear().add('{').add(key).add('}');
		for (int i = 0; i < length(); i++) {
			if (matches(this, TMPMATCH, i)) {
				return true;
			}
		}
		return false;
	}
	
	private Str insert(CharSequence v) {
		builder.setLength(0);
		for (int i = 0; i < length(); i++) {
			if (matches(this, TMPMATCH, i)) {
				builder.append(v);
				i += TMPMATCH.length();
				for (; i < length(); i++)
					builder.append(charAt(i));
				clear();
				add(builder);
				return this;
				
			}else {
				builder.append(charAt(i));
			}
		}
		
		return this;
	}
	
	private static boolean matches(CharSequence s, CharSequence n, int i) {
		if (i+n.length() > s.length())
			return false;
		
		char c = s.charAt(i);
		if (n.charAt(0) == c) {
			for (int k = 0; k < n.length(); k++) {
				int ki = k+i;
				if (n.charAt(k) != s.charAt(ki))
					return false;
			}
			return true;
		}
		return false;
	}
	
	public Str insert(int index, char v) {
		TMP3.clear().add(v);
		return insert(index, TMP3);
	}
	
	public Str insert(int index, int v) {
		TMP3.clear().add(v);
		return insert(index, TMP3);
	}
	
	public Str insert(int index, double v, int decimals) {
		TMP3.clear().add(v, decimals);
		return insert(index, TMP3);
	}
	

	
	public final static class StringReusableSer extends Str implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public StringReusableSer(int size) {
			super(size);
		}
		
	    private void writeObject(ObjectOutputStream out) throws IOException {
	    	out.writeObject(this.chars);
	    	out.writeInt(this.last);
	    }

	    private void readObject(ObjectInputStream in) throws IOException,
	            ClassNotFoundException {

	    	this.chars = (char[]) in.readObject();
	    	this.last = in.readInt();
	    }
		
	}

	public Str toUpper() {
		for (int i = 0; i < last; i++) {
			if (chars[i] >= 'a' && chars[i] <= 'z') {
				chars[i] -= 32;
			}
		}
		return this;
	}
	
	public boolean startsWithIgnoreCase(CharSequence other) {
		return startsWithIgnoreCase(this, other);
		
	}
	
	public static boolean startsWithIgnoreCase(CharSequence a, CharSequence b) {
		if (a.length() == 0) {
			return true;
		}
		if (b.length() == 0)
			return true;
		if (b.length() > a.length())
			return false;
		for (int i = 0; i < b.length(); i++) {
			if (Character.toLowerCase(b.charAt(i)) != Character.toLowerCase(a.charAt(i)))
				return false;
		}
		return true;
		
	}
	
	public void save(FilePutter f) {
		f.i(last);
		for (int i = 0; i < last; i++) {
			f.i(chars[i]);
		}
	}
	
	public void load(FileGetter f) throws IOException {
		last = f.i();
		if (last > chars.length) {
			chars = new char[last];
		}
		
		for (int i = 0; i < last; i++) {
			chars[i] = (char) f.i();
		}
	}

	public Str trim() {
		char[] chars = new char[length()];
		for (int i = 0; i < chars.length; i++)
			chars[i] = this.chars[i];
		this.chars = chars;
		return this;
	}

	public Str NL() {
		add(Font.nl);
		return this;
	}
	
	public Str TAB() {
		add(Font.tab);
		return this;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CharSequence) {
			CharSequence cc = (CharSequence) obj;
			if (cc.length() != length())
				return false;
			for (int i = 0; i < cc.length(); i++) {
				if (cc.charAt(i) != charAt(i))
					return false;
			}
			return true;
		}
		return false;
	}

}
