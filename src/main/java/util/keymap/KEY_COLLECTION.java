package util.keymap;

import java.util.Set;

import game.GAME;
import snake2d.util.file.Json;
import snake2d.util.sets.*;

public interface KEY_COLLECTION<T> {

	public static final String WILDCARD = "*";
	
	public default T get(Json reader){
		return getByKey(this.key(), reader);
	}
	
	public default T get(String key, Json error) {
		T t = tryGet(key);
		if (t != null) {
			return t;
		}
		String k = "   Available: ";
		for (String s : available())
			k += s + ", ";
		if (key.endsWith(" ")) {
			
		}
		error.error("no " + this.key() + " named: " + key  + (key.endsWith(" ") ?  "It ends with space!" : "") + k, key);
		return null;
	}
	
	public default T getByKey(String key, Json reader){
		
		String value = reader.value(key);
		T t = tryGet(value);
		if (t != null) {
			return t;
		}
		String k = "   Available: ";
		for (String s : available())
			k += s + ", ";
		reader.error("no " + this.key() + " named: " + value + k, key);
		return null;
	}
	
	public default T getByKeyWarn(String key, Json reader){
		
		T t = tryGet(key);
		if (t != null) {
			return t;
		}
		String k = "   Available: ";
		for (String s : available())
			k += s + ", ";
		GAME.WarnLight(reader.errorGet("no " + this.key() + " named: " + key + k, key));
		return null;
	}
	
	public default T tryGetByKey(String key, Json reader){
		if (reader.has(key)) {
			return getByKey(key, reader);
			
		}
		return null;
	}
	
	public default T tryGet(Json reader){
		return tryGetByKey(key(), reader);
	}
	
	public default LIST<T> getMany(Json reader){

		return getManyByKey(this.key()+"S", reader);
		
	}
	
	public default LIST<T> getManyWarn(Json reader){

		return getManyByKeyWarn(this.key()+"S", reader);
		
	}
	
	
	public default void fill(double[] res, Json j, double min, double max){
		fill(key(), res, j, min, max);
	}
	
	public default void fill(String key, double[] res, Json j, double min, double max){
		j = j.json(key);
		for (String s : j.keys()) {
			if (s.equals(WILDCARD)) {
				double d = j.d(WILDCARD, min, max);
				for (int i = 0; i < all().size(); i++) {
					res[i] = d;
				}
			}else {
				if (tryGet(s) == null)
					GAME.WarnLight(j.errorGet("no key: " + s, s));
				else {
					T t = get(s, j);
					res[index(t)] = j.d(s, min, max);
				}
			}
		}
	}
	
	public  default int index(T t) {
		for (int i = 0; i < all().size(); i++)
			if (all().get(i) == t)
				return i;
		throw new RuntimeException();
	}

	public static <D extends INDEXED> double[] fill(KEY_COLLECTION<D> coll, Json j, double max){
		double[] res = new double[coll.all().size()];
		fill(res, coll, j, max);
		return res;
		
	}
	
	public static <D extends INDEXED> void fill(double[] res, KEY_COLLECTION<D> coll, Json j, double max){
		fill(res, coll, j, 0, max);
	}
	
	public static <D extends INDEXED> void fill(double[] res, KEY_COLLECTION<D> coll, Json j, double min, double max){
		j = j.json(coll.key());
		for (String s : j.keys()) {
			if (coll.tryGet(s) == null) {
				GAME.Notify("here");
				GAME.WarnLight(j.errorGet("no " + coll.key() + " resource named: " + s, s));
			}else {
				D t = coll.get(s, j);
				res[t.index()] = j.d(s, min, max);
			}
			
		}
	}
	
	public static <D extends INDEXED> boolean[] manyIs(KEY_COLLECTION<D> coll, Json j){
		boolean[] res = new boolean[coll.all().size()];
		for (D t : coll.getMany(j)) {
			
			res[t.index()] = true;
		}
		return res; 
	}
	
	public default LIST<T> getManyByKey(String value, Json reader){
		
		if (!reader.has(value))
			return new ArrayList<T>();
		
		String[] values = reader.values(value);
		for (String v : values) {
			if (v.equals("*")) {
				return new ArrayList<T>(all());
			}
		}
		
		ArrayList<T> res = new ArrayList<>(values.length);
		for (String v : values) {
			T t = tryGet(v);
			if (t != null) {
				res.add(t);
			}else {
				String k = "   Available: ";
				for (String s : available())
					k += s + ", ";
				reader.error("no " + this.key() + " named: " + v + k, value);
			}
		}
		return res;
		
	}
	
	public default LIST<T> getManyByKeyWarn(String value, Json reader){
		
		if (!reader.has(value))
			return new ArrayList<T>();
		
		String[] values = reader.values(value);
		for (String v : values) {
			if (v.equals("*")) {
				return new ArrayList<T>(all());
			}
		}
		
		ArrayList<T> res = new ArrayList<>(values.length);
		for (String v : values) {
			T t = tryGet(v);
			if (t != null) {
				res.add(t);
			}else {
				String k = "   Available: ";
				for (String s : available())
					k += s + ", \n";
				GAME.WarnLight(reader.errorGet("no " + this.key() + " named: " + v + k, v));
			}
		}
		return res;
		
	}
	
	public Set<String> available();
	
	public T tryGet(String value);
	public String key();
	
	public LIST<T> all();
}
