package snake2d.util.sets;

public interface ADDABLE <E> {

	int add(E e);
	default void add(Iterable<E> es) {
		for (E e : es)
			add(e);
	}
	default void add(E[] es) {
		for (E e : es)
			add(e);
	}
	
	int tryAdd(E e);
	
	boolean hasRoom();
	
}
