package snake2d.util.sets;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;

import snake2d.util.file.*;

public class ArrayListShortResize implements SAVABLE{

	private short[] es;
	private final int maxSize;
	private final int minSize;
	private int last = 0;
	
	/**
	 * 
	 * @param maxSize the fixed size of the list. Connot be changed.
	 */
	public ArrayListShortResize(int minSize, int maxSize){
		this.maxSize = maxSize;
		this.minSize = minSize;
		es = new short[minSize];
	}
	
	private void increase(){
		if (last == es.length-1 && es.length != maxSize){
			int size = es.length*2;
			if (size > maxSize)
				size = maxSize;
			short[] esNew = new short[size];
			for (int i = 0; i < last; i++){
				esNew[i] = es[i];
			}
			es = esNew;
		}
	}
	
	private void decrease(){
		if (es.length != minSize && last < es.length/2){
			int size = es.length/2;
			if (size < minSize)
				size = minSize;
			short[] esNew = new short[size];
			for (int i = 0; i < last; i++){
				esNew[i] = es[i];
			}
			es = esNew;
		}
	}
	
	public int get(int index) {
		if (index < this.last)
			return es[index];
		throw new NoSuchElementException("no element at index: " + index);
	}


	public int add(short e){
		increase();
		if (!hasRoom())
			return -1;
		es[last] = e;
		last++;
		return last -1;
	}

	/**
	 * removes and messes up the order.
	 * @param i
	 * @return if success
	 */
	public boolean remove(int i){
		
		if (i >= last)
			return false;
		
		if (i == last-1){
			last --;
			decrease();
			return true;
		}
		
		es[i] = es[last-1];
		last --;
		decrease();
		return true;
	}
	
	public void removeShort(short s) {
		remove(indexOf(s));
	}
	
	public int indexOf(short s) {
		for (int i = 0; i < last; i++)
			if (es[i] == s)
				return i;
		return -1;
	}
	
	public int remainingSlots() {
		return maxSize-last;
	}

	public boolean hasRoom() {
		return remainingSlots() > 0;
	}
	
	/**
	 * Start anew!
	 */
	@Override
	public void clear(){
		if (es.length != minSize)
			es = new short[minSize];
		last = 0;
	}
	
	@Override
	public void save(FilePutter file) {
		file.i(es.length);
		file.i(last);
		file.ss(es);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		int l = file.i();
		last = file.i();
		if (es.length != l)
			es = new short[l];
		file.ss(es);
	}
	
	public int size() {
		return last;
	}
	
	public int max(){
		return maxSize;
	}
	
	public boolean isEmpty() {
		return last == 0;
	}
	
	
	public void trim(){
		short[] no = new short[last];
		for (int i = 0; i < last; i++)
			no[i] = es[i];
		es = no;
	}

	public int getLast() {
		return get(last-1);
	}
	
	public void sort(){
		if (last <= 1)
			return;
		
		Arrays.sort(es, 0, last);
	}
	
	public void swap(int indexA, int indexB) {
		if (indexA < 0 || indexB >= size() || indexB < 0 || indexB >= size())
			throw new RuntimeException();
		short a = es[indexA];
		es[indexA] = es[indexB];
		es[indexB] = a;
	}
	
}
