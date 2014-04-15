package kr.ac.kookmin.cs.distboard.util;

import kr.ac.kookmin.cs.distboard.system.BluetoothManager;
import kr.ac.kookmin.cs.distboard.system.DicePlusManager;
import kr.ac.kookmin.cs.distboard.system.ElectricYutManager;
import android.util.Log;

public class ThreadTimer extends Thread implements Runnable {
	
	private static final String TAG = "20083271:ThreadTimer";
	
	public static final int DICE_PLUS_MANAGER_ESTABLISH_CALL = 0;
	public static final int BLUETOOTH_MANAGER_ESTABLISH_CALL = 1;
	public static final int ELECTRIC_YUT_MANAGER_ESTABLISH_CALL = 2;

	private int callingType = -1;
	private int timeOut = 0;
	
	
	//첫번째 인자 : 부르는 타입
	//두번째 인자 : 타임아웃 시간
	public ThreadTimer(final int callingType, int millisecTimeOut){
		this.callingType = callingType;
		this.timeOut = millisecTimeOut;
	}
	
	public void run(){
		
		Log.i(TAG, "타이머 시작");
		
		try {
			Thread.sleep(timeOut/5);
			Log.i(TAG, "타이머 시간경과 1/5");
			Thread.sleep(timeOut/5);
			Log.i(TAG, "타이머 시간경과 2/5");
			Thread.sleep(timeOut/5);
			Log.i(TAG, "타이머 시간경과 3/5");
			Thread.sleep(timeOut/5);
			Log.i(TAG, "타이머 시간경과 4/5");
			Thread.sleep(timeOut/5);
			Log.i(TAG, "타이머 시간경과 5/5");
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Log.i(TAG, "타이머 끝");
		
		switch(callingType){
			case DICE_PLUS_MANAGER_ESTABLISH_CALL:
				Log.i(TAG, "타이머 스레드 콜링타입 : 다이스 플러스 콜");
				DicePlusManager.getInstance().onEstablishTimeOut();
				break;
				
			case BLUETOOTH_MANAGER_ESTABLISH_CALL:
				Log.i(TAG, "타이머 스레드 콜링타입 : 블루투스 콜");
				BluetoothManager.getInstance().onEstablishTimeOut();
				break;
				
			case ELECTRIC_YUT_MANAGER_ESTABLISH_CALL:
				Log.i(TAG, "타이머 스레드 콜링타입 : 전자 윷 콜");
				ElectricYutManager.getInstance().onEstablishTimeOut();
				break;
	
		}
		
		
		
	}

}
