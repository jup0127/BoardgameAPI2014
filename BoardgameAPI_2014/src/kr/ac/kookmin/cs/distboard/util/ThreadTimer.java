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
	
	
	//ù��° ���� : �θ��� Ÿ��
	//�ι�° ���� : Ÿ�Ӿƿ� �ð�
	public ThreadTimer(final int callingType, int millisecTimeOut){
		this.callingType = callingType;
		this.timeOut = millisecTimeOut;
	}
	
	public void run(){
		
		Log.i(TAG, "Ÿ�̸� ����");
		
		try {
			Thread.sleep(timeOut/5);
			Log.i(TAG, "Ÿ�̸� �ð���� 1/5");
			Thread.sleep(timeOut/5);
			Log.i(TAG, "Ÿ�̸� �ð���� 2/5");
			Thread.sleep(timeOut/5);
			Log.i(TAG, "Ÿ�̸� �ð���� 3/5");
			Thread.sleep(timeOut/5);
			Log.i(TAG, "Ÿ�̸� �ð���� 4/5");
			Thread.sleep(timeOut/5);
			Log.i(TAG, "Ÿ�̸� �ð���� 5/5");
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Log.i(TAG, "Ÿ�̸� ��");
		
		switch(callingType){
			case DICE_PLUS_MANAGER_ESTABLISH_CALL:
				Log.i(TAG, "Ÿ�̸� ������ �ݸ�Ÿ�� : ���̽� �÷��� ��");
				DicePlusManager.getInstance().onEstablishTimeOut();
				break;
				
			case BLUETOOTH_MANAGER_ESTABLISH_CALL:
				Log.i(TAG, "Ÿ�̸� ������ �ݸ�Ÿ�� : ������� ��");
				BluetoothManager.getInstance().onEstablishTimeOut();
				break;
				
			case ELECTRIC_YUT_MANAGER_ESTABLISH_CALL:
				Log.i(TAG, "Ÿ�̸� ������ �ݸ�Ÿ�� : ���� �� ��");
				ElectricYutManager.getInstance().onEstablishTimeOut();
				break;
	
		}
		
		
		
	}

}
