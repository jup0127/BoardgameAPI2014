package kr.ac.kookmin.cs.distboard.subobject;

import android.util.Log;
import us.dicepl.android.sdk.DiceController;
import us.dicepl.android.sdk.Die;
import kr.ac.kookmin.cs.distboard.GameTool;
import kr.ac.kookmin.cs.distboard.Mediator;
import kr.ac.kookmin.cs.distboard.system.DicePlusManager;
import kr.ac.kookmin.cs.distboard.system.SubjectDeviceMapper;

public final class DicePlusGameTool extends GameTool {
    
    private static final String TAG = "20083271:DicePlusGameTool";

	private int face = -1;
	
	private int red = 0;
	private int green = 0;
	private int blue = 0;
	
	public int getFace(){
		return face;
	}
	
	public void setFace(int face){
		this.face = face;
	}
	
	public int getRolledFace(){
		return 0;
	}
	
	/**
	 * etc
	 */
	
	public void setColor(int r, int g, int b){
	    this.red = r;
	    this.green = g;
	    this.blue = b;
	}
	
	public void blink(int ledMask, int priority, int ledOnPeriod, int ledCyclePeriod, int blinkNumber){
	    Die die = (Die)SubjectDeviceMapper.getInstance().map(this);
		if(DicePlusManager.getInstance().isAvailableDevice(die) == false){
		    Log.i(TAG, "사용할 수 없는 DIE(연결끊김)");
		    Mediator.getParticipantListener().onNotAvaliableGameTool(this);
		    return;
		}
		Log.i(TAG, "Blink 호출");
		DiceController.runBlinkAnimation(die, ledMask, priority, this.red, this.green, this.blue, ledOnPeriod, ledCyclePeriod, blinkNumber);
		
	}
}
