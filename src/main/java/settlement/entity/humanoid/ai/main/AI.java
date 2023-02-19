package settlement.entity.humanoid.ai.main;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIData.AIDataBit;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public final class AI {
	
	private static AI s;
	private final ArrayList<AIElement> all = new ArrayList<>(1024);
	private final ArrayList<AIElement> back = new ArrayList<>(16);
	private final AISTATES STATES;
	private final AIEventListeners listeners;
	private final AISUBS SUBS;
	private final AIPlans plans;
	private final AIModules modules;
	private final AIPLAN first;
	private final AIData data = new AIData();
	
	private AI() {
		AI.s = this;
		STATES = new AISTATES();
		listeners = new AIEventListeners();
		SUBS = new AISUBS();
		plans = new AIPlans();
		modules = new AIModules();
		first = new AIPLAN.PLANRES() {
			
			@Override
			protected AISubActivation init(Humanoid a, AIManager d) {
				return resumer.set(a, d);
			}
			
			private final Resumer resumer = new Resumer("standing") {
				
				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					return AI.SUBS().STAND.activateTime(a, d, 1);
				}
				
				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					// TODO Auto-generated method stub
					return null;
				}
				
				@Override
				public boolean con(Humanoid a, AIManager d) {
					return true;
				}
				
				@Override
				public void can(Humanoid a, AIManager d) {
					// TODO Auto-generated method stub
					
				}
			};
		};
		for (int i = back.size()-1; i >= 0; i--) {
			AIElement e = back.get(i);
			e.index = (short) all.add(e);
			
		}
		back.clear();
	}
	
	public static LIST<AIElement> ALL(){
		return s.all;
	}
	
	public static final AISTATES STATES() {
		return s.STATES;
	}

	public static final AISUBS SUBS() {
		return s.SUBS;
	}
	public static final AIPlans plans() {
		return s.plans;
	}
	public static final AIModules modules() {
		return s.modules;
	}
	public static final AIEventListeners listeners() {
		return s.listeners;
	}
	
	
	public static final AIPLAN first() {
		return s.first;
	}

	public static void init() {
		new AIData();
		new AI();
	}
	
	public static AIElement get(int index) {
		
		return s.all.get(index);
	}
	
	public static AIData data(){
		return s.data;
	}
	
	public static AIData.AIDataSuspender suspender(){
		return s.data.new AIDataSuspender();
	}
	
	public static AIDataBit bit(){
		return s.data.new AIDataBit();
	}
	
	public static class AIElement {
		
		private short index;
		final String className;
		{
			String cn =  this.getClass().getName();
			String[] ss = cn.split("\\.");
			className = ss[ss.length-1];
			
		
		}
		protected AIElement() {
			index = (short) s.all.add(this);
		}
	
		int index() {
			return index;
		}
		
		protected void moveLastToBack() {
			s.back.add(s.all.get(s.all.size()-1));
			s.all.remove(s.all.size()-1);
		}
		
		
	} 
	
}
