package settlement.army.order;

import snake2d.util.file.SAVABLE;

public interface Copyable<T> extends SAVABLE {

	void copy(T toBeCopied);
	
}
