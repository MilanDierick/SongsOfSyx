package settlement.army.order;

import java.io.IOException;

import settlement.army.Div;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public final class DivTDataInfo implements Copyable<DivTDataInfo>{

	public int men = 0;
	public short unreachable;
	public double projVel;
	
	public DivTDataInfo() {

	}
	
	void set(Div div) {
		men = div.menNrOf();
		unreachable = (short) div.reporter.unreachable();
		if (div.settings.ammo() != null) {
			projVel = div.settings.ammo().speed(div);
		}else
			projVel = 0;
	}
	
	@Override
	public void copy(DivTDataInfo tmp) {
		men = tmp.men;
		unreachable = tmp.unreachable;
		projVel = tmp.projVel;
	}
	
	@Override
	public void save(FilePutter file) {
		file.i(men);
		file.s(unreachable);
		file.d(projVel);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		men = file.i();
		unreachable = file.s();
		projVel = file.d();
	}
	
	@Override
	public void clear() {
		men = 0;
		unreachable = 0;
		projVel = 0;
	}
	
}