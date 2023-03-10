package snake2d;

import snake2d.SoundCore.Source;

public interface SoundEffectStream extends SoundStream{
	
	public void setCoos(int x, int y);

	static class Dummy implements SoundEffectStream {

		@Override
		public boolean play() {
			return true;
		}

		@Override
		public void setGain(double gain) {
			
		}

		@Override
		public void stop() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void resume() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setLooping(boolean yes) {
			
		}

		@Override
		public double getProgress() {
			return 1.0;
		}

		@Override
		public boolean isPlaying() {
			return true;
		}

		@Override
		public void setCoos(int x, int y) {
			// TODO Auto-generated method stub
			
		}
		
		
	}
	
	public class SoundEffectStreamImp extends SoundStream.SoundStreamImp implements SoundEffectStream{
		
		private int x = 0;
		private int y = 0;
		private boolean posChanged = false;
		
		SoundEffectStreamImp(java.nio.file.Path path, boolean music) {
			super(path, music);
		}

		@Override
		public void setCoos(int x, int y){
			this.x = x;
			this.y = y;
			posChanged = true;
			if (isPlaying()){
				
			}
		}
		
		@Override
		protected void set(Source source) {
			source.setPosition(x, y);
		}
		
//		public void setPitch(float pitch){
//			this.pitch = pitch;
//			if (isPlaying()){
//				source.setPitch(pitch);
//			}
//		}
		
		@Override
		boolean refillBuffers(Source source) {
			if (super.refillBuffers(source)) {
				if (posChanged) {
					posChanged = false;
					source.setPosition(x, y);
				}
				return true;
			}
			return false;
		}

	}
	
}
