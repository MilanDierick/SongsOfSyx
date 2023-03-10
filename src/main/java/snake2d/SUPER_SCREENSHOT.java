package snake2d;

import snake2d.CORE.GlJob;
import snake2d.util.file.FileManager;
import snake2d.util.file.SnakeImage;

public abstract class SUPER_SCREENSHOT extends GlJob{

	private final int scale = 2;
	
	public abstract int getWidth();
	public abstract int getHeight();
	public abstract boolean renderAndHasNext();
	public abstract void init();
//	public abstract void recieveResult(SnakeImage image);
	
	@Override
	public final void doJob() {
		
		SnakeImage image = new SnakeImage(getWidth()/scale, getHeight()/scale);
		
		init();
		
		double x1 = 0;
		double y1 = 0;
		
		while(renderAndHasNext()) {
			if (y1 >= getHeight())
				throw new RuntimeException();
			CORE.getGraphics().flushRenderer();
			CORE.getGraphics().copyFB(image, (int)Math.round(x1/scale), (int)Math.round(y1/scale), scale);
			CORE.getGraphics().pollEvents();
			x1 += CORE.getGraphics().nativeWidth;
			if (x1 >= getWidth()) {
				y1 += CORE.getGraphics().nativeHeight;
				x1 = 0;
				
			}

		}
		
		CORE.getGraphics().pollEvents();
		
		String path = FileManager.NAME.timeStampString(CORE.getGraphics().screenShotPath + "super") + ".jpg";
	
		image.saveJpg(path);
		image.dispose();
		gc();
		CORE.getInput().clearAllInput();
		
	}
	
}
