package util.data;

public interface GETTER<T> {
	
	public T get();
	
	public static class GETTER_IMP<T> implements GETTERE<T>{

		private T t;
		
		public GETTER_IMP() {
			// TODO Auto-generated constructor stub
		}
		
		public GETTER_IMP(T t) {
			this.t = t;
		}
		
		@Override
		public void set(T t) {
			this.t = t;
		}
		
		@Override
		public T get() {
			return t;
		}
		
	}
	
	public interface GETTERE<T> extends GETTER<T> {
		public void set(T t);
	}
	
}