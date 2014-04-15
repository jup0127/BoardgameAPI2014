package kr.ac.kookmin.cs.distboard.system;

import java.util.List;

import kr.ac.kookmin.cs.distboard.DistributedBoardgame;
import kr.ac.kookmin.cs.distboard.protocol.GameToolData;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

public class EmulatorReceiver extends BroadcastReceiver {

	private static final String TAG = "20083271:EmulatorReceiver";
	
	private static EmulatorReceiver instance = new EmulatorReceiver();

	private boolean initialized = false;
	
	private Context context = null;
	private String ableButton = "0";
	private String diceNumber = "";
	private String setDiceNumber = "";
	private String setYutNumber = "";
	private String activityName = "";
	private static IntentFilter filter = null;
	private String inputActivityName = "";
	private String packageName="";

	//presented methods
	
	public static EmulatorReceiver getInstance() {

		return instance;
	}
	
	public void initialize(){
		Log.i(TAG, "초기화");
		initialized = true;
		
		context = DistributedBoardgame.getInstance().getContext();
		Log.d(TAG, "초기화 컨텍스트" + context.toString());
		ableButton = "0";
		diceNumber = "";
		setDiceNumber = "";
		setYutNumber = "";
		activityName = "";
		filter = new IntentFilter("android.intent.action.SUPER");
		inputActivityName = "";
		packageName=context.getPackageName();
		
		setNumberOfDice(DistributedBoardgame.getInstance().getNumOfDiceIntention());
		//윷추가할것
		
		setReceiver();
		
	}
	
	@Deprecated
	public void setInputActivityName(String inputActivityName){
		
		this.inputActivityName = context.getPackageName() + "." +inputActivityName;
		Log.d(TAG, "입력 액티비티 이름 설정됨 : " + inputActivityName);
	}

	public void appear() {
		
		
		Log.i(TAG, "appear()호출");
		
		if(initialized == false){
			Log.e(TAG, "초기화되지 않음");
			return;
		}
		
		ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> infoList = activityManager.getRunningTasks(1);
		RunningTaskInfo info = infoList.iterator().next();
		String className = info.topActivity.getClassName();
		int lastIndex = className.lastIndexOf(".");
		String currentActivityName = className.substring(lastIndex+1);
		activityName = context.getPackageName() + "." +currentActivityName;
		

		
		Log.e(TAG, "TOP 액티비티 명 " + activityName);
		
		
		
		//activityName = inputActivityName;
		ComponentName compName = new ComponentName(
				"com.wwforever.eclipsebuildunity3d",
				"com.wwforever.eclipsebuildunity3d.MainActivity");

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		// dice number ;
		intent.putExtra("activity", activityName); // "紐낆묶", "�ㅼ젣媛�
		intent.putExtra("setDiceNumber", setDiceNumber); // "紐낆묶", "�ㅼ젣媛�
		intent.putExtra("getPackageName", context.getPackageName()); // "紐낆묶",
																		// "�ㅼ젣媛�
		
		intent.putExtra("ableButton", ableButton); // "紐낆묶", "�ㅼ젣媛�
		// package, activity name
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(compName);

		context.startActivity(intent);
		
		
	}

	public void disappear() {
		
		
		Log.i(TAG, "disappear()호출");

		if(this.initialized == false){
			Log.e(TAG, "초기화되지 않음");
			return;
		}
		
		
		Intent intentSk = new Intent("android.intent.action.SUPERSK");
		intentSk.setData(Uri.parse("sample:"));
		// intent.setData(Uri.parse(""));
		
		
		context.sendBroadcast(intentSk);
		
		
	}

	public void reset() {
	}

	public void lock() {
	}
	
	public void enableExitButton() {
		ableButton = "1";
	}

	public void disableExitButton() {
		ableButton = "0";
	}
	
	public void setNumberOfDice(int numOfDice) {
		setDiceNumber = "" + numOfDice;
	}
	
	public void setNumberOfYut(int numOfYut) {
		setYutNumber = "" + numOfYut;
	}
	
	
	//private methods
	
	
	private int[] toToolDataArrayStringToPrimitiveValue(String rollValues) {
		int[] saveRollValues = new int[rollValues.length()];
		for (int i = 0; i < rollValues.length(); i++)
			saveRollValues[i] = Integer.parseInt(Character.toString(rollValues.charAt(i)));
		return saveRollValues;
	}

	private void setReceiver() {
		context.registerReceiver(instance, filter);
		Log.d(TAG, "setReceiver");
	}

	private void unSetReceiver() {
		context.unregisterReceiver(instance);
		Log.d(TAG, "unSetReceiver");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceiver");
		// Intent intentGetDiceValue = new
		// Intent();//MainActivity.class.getIntent();
		diceNumber = intent.getExtras().getString("diceNumber");
		Log.d(TAG, diceNumber);
		
		
		int[] resultValues = toToolDataArrayStringToPrimitiveValue(diceNumber);
		//GameToolData[] gameToolData = new GameToolData[resultValues.length];
		
	
		GameToolSystemManager.getInstance().onLocalVirtualDiceRoll(resultValues);
		
		
		
		//disappear();

	}
	
	private void finishActivity() {
	    Log.i(TAG, "finishActivity 진입 : " + packageName);
        if(packageName.equals("") == true)
            return;
        Log.i(TAG, "끄는중..");
        //ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //activityManager.restartPackage("com.wwforever.eclipsebuildunity3d");
        //Log.i(TAG, "끄기 완료?!");
    }
	
	public synchronized void clear(){
	    
	    Log.i(TAG, "에뮬레이터 클리어 진입");
	    initialized = false;
		unSetReceiver();
		
		//finishActivity();
	}
	
}