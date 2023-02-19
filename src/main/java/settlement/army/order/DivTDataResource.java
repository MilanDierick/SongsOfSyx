package settlement.army.order;

import java.io.IOException;

import snake2d.util.file.*;

public final class DivTDataResource<T extends Copyable<T>> implements SAVABLE{

	private volatile boolean lock;
	private volatile int setI = 0;
	private final T t;
	
	DivTDataResource(T t){
		this.t = t;
	}
	
	private synchronized void lock() {
		while(lock)
			;
		lock = true;
	}
	
	public void get(T to) {
		lock();
		to.copy(t);
		lock = false;
	}
	
	public void set(T from) {
		lock();
		t.copy(from);
		setI++;
		lock = false;
	}
	
	public int setI() {
		return setI;
	}
	
	public boolean isNew(short i) {
		return (short)(setI & 0x0FFFF) != i;
	}
	
	@Override
	public void save(FilePutter file) {
		t.save(file);
		file.i(setI);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		t.load(file);
		setI = file.i();
	}
	
	@Override
	public void clear() {
		t.clear();
		setI = 0;
	}
	
}
