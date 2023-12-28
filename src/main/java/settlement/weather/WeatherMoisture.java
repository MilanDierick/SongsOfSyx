package settlement.weather;

import game.faction.FACTIONS;
import game.time.TIME;
import init.D;
import settlement.main.SETT;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import view.main.VIEW;
import view.ui.message.MessageText;

public final class WeatherMoisture extends WeatherThing{


	private static CharSequence ¤¤name = "Moisture";
	private static CharSequence ¤¤desc = "The moisture content of the ground. Low moisture can cause plant-life and crops to shrivel. Moisture is gained by rainfall and will evaporate with high outside temperature.";
	
	private static CharSequence ¤¤mTitle = "¤Drought!";
	private static CharSequence ¤¤mBody = "¤The gods have forsaken {0}, and the rains have stopped. If this keeps up, it will devastate our crops!. Everyone must now pray.";
	
	private static double rainspeed = 2.0/(TIME.secondsPerHour);
	private static double dry = 1.0/(4*TIME.secondsPerDay);
	private double lastSnow = 0;
	private double sendTimer;
	
	
	static {
		D.ts(WeatherMoisture.class);
	}
	
	
	WeatherMoisture() {
		super(¤¤name, ¤¤desc);
	}
	
	@Override
	void update(double ds) {
		double d = getD();
		if (!SETT.WEATHER().snow.rainIsSnow()) {
			d += ds*rainspeed*SETT.WEATHER().rain.getD();
		}
		
		double snow = SETT.WEATHER().snow.getD();
		double thawed = lastSnow - snow;
		lastSnow = snow;
		
		if (thawed > 0)
			d += thawed;
		lastSnow = SETT.WEATHER().snow.getD();
	
		
		if (SETT.WEATHER().temp.heat() > 0)
			d -= dry*ds;
		
		sendTimer -= ds;;
		

		setD(d);
		
		
		
	}
	
	@Override
	public DOUBLE_MUTABLE setD(double d) {
		if (d < 0.25 && getD() >= 0.25) {
			if (sendTimer < 0 && !VIEW.b().isActive()) {
				Str.TMP.clear().add(¤¤mBody).insert(0, FACTIONS.player().name);
				new MessageText(¤¤mTitle).paragraph(Str.TMP).send();
				sendTimer = 10;
			}
		}
		return super.setD(d);
	}
	
	public double growthValue() {
		return CLAMP.d(getD()*4.0, 0, 1);
	}
	
	@Override
	protected void init() {
		setD(0.75);
	}
	
}
