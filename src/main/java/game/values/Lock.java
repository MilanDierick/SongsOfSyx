package game.values;

public final class Lock<T> {
	
	public final Lockable<T> lockable;
	public final Locker<T> unlocker;
	
	Lock(Lockable<T> lockable, Locker<T> unlocker){
		this.lockable = lockable;
		this.unlocker = unlocker;
	}
	
}