package snake2d;

import snake2d.SoundCore.Source;

abstract class AbsBuffer{

	final boolean isMusic;
	
	AbsBuffer(boolean isMusic){
		this.isMusic = isMusic;
	}
	abstract void reclaimSource(Source source);
	abstract boolean refillBuffers(Source source);
	abstract void dis();
	abstract void setBuffer(Source source);
	
}
