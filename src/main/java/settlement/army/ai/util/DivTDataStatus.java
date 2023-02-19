package settlement.army.ai.util;

import java.io.IOException;
import java.util.Arrays;

import settlement.army.Div;
import settlement.army.order.Copyable;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;

public final class DivTDataStatus implements Copyable<DivTDataStatus>{

	int cx,cy;
	short inPosition;
	boolean isFighting;

	private static final int iSize = 8;
	private static int iFriendlyColl = 0;
	private static int iEnemyColl = iSize;
	private static int iEnemyInRange = 2*iSize;
	private static int iFriendlyInRange = 3*iSize;
	private static int iEnemyCharging = 4*iSize;
	
	final short[] lists = new short[iEnemyCharging +iSize];
	
	//dir faceEnemyDirection (the best way to face the enemy
	// faceEnemyWidth
	
	public DivTDataStatus() {

	}
	
	@Override
	public void save(FilePutter file) {
		file.ss(lists);
		file.s(inPosition);
		file.i(cx).i(cy);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.ss(lists);
		inPosition = file.s();
		cx = file.i();
		cy = file.i();
	}

	@Override
	public void clear() {
		Arrays.fill(lists, (short)-1);
		cx = -1;
		cy = -1;
	}

	@Override
	public void copy(DivTDataStatus toBeCopied) {
		for (int i = 0; i < lists.length; i++) {
			lists[i] = toBeCopied.lists[i];
		}
		cx = toBeCopied.cx;
		cy = toBeCopied.cy;
		inPosition = toBeCopied.inPosition;
	}

	public LIST<Div> friendlyCollisions(LISTE<Div> res){
		return fill(iFriendlyColl, res);
	}
	
	public int friendlyCollisions(){
		return count(iFriendlyColl);
	}
	
	void friendlyCollisionSet(short di) {
		set(iFriendlyColl, di);
	}
	
	public LIST<Div> enemyCollisions(LISTE<Div> res){
		return fill(iEnemyColl, res);
	}
	
	public int enemyCollisions(){
		return count(iEnemyColl);
	}
	
	void enemyCollisionSet(short di) {
		set(iEnemyColl, di);
	}
	
	public LIST<Div> enemiesClosest(LISTE<Div> res){
		return fill(iEnemyInRange, res);
	}
	
	public int enemiesClosest(){
		return count(iEnemyInRange);
	}
	
	public Div enemyClosest(){
		return getFirst(iEnemyInRange);
	}
	
	void enemiesClosestSet(short di) {
		set(iEnemyInRange, di);
	}
	
	public LIST<Div> friendlyClosest(LISTE<Div> res){
		return fill(iFriendlyInRange, res);
	}
	
	public int friendlyClosest(){
		return count(iFriendlyInRange);
	}
	
	void friendlyClosestSet(short di) {
		set(iFriendlyInRange, di);
	}
	
	
	private void set(int start, short di) {
		if (lists[start+iSize-1] != -1)
			return;
		for (int i = 0; i < iSize; i++) {
			int k = i+start;
			if (lists[k] == -1) {
				lists[k] = di;
				return;
			}
		}
	}
	
	private int count(int start) {
		for (int i = 0; i < iSize; i++) {
			int k = i+start;
			if (lists[k] == -1)
				return i;
		}
		return iSize;
	}
	
	private LIST<Div> fill(int start, LISTE<Div> res) {
		for (int i = 0; i < iSize; i++) {
			int k = i+start;
			if (lists[k] == -1)
				return res;
			res.add(SETT.ARMIES().division(lists[k]));
			if (!res.hasRoom())
				return res;
		}
		return res;
	}
	
	private Div getFirst(int start) {
		if (lists[start] == -1)
			return null;
		return SETT.ARMIES().division(lists[start]);
		
	}
	
	/**
	 * 
	 * @return current positions centre pixel. -1 if invalid
	 */
	public int currentPixelCX() {
		return cx;
	}

	/**
	 * 
	 * @return current positions centre pixel. -1 if invalid
	 */
	public int currentPixelCY() {
		return cy;
	}
	
	public int inFormation() {
		return inPosition; 
	}
	
	public boolean isFighting() {
		return isFighting;
	}
	
	
}
