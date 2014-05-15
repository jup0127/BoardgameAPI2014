package kr.ac.kookmin.cs.distboard;

import kr.ac.kookmin.cs.distboard.activity.AssistanceActivity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyTest {
	
	private Context currentConext = null;
	
	private Thread thread = new Thread(){
		public void run(){
			
			for(int i = 0 ; i < 5 ; i++){
				Intent myIntent = new Intent(currentConext, kr.ac.kookmin.cs.distboard.activity.AssistanceActivity.class);
				myIntent.putExtra(AssistanceActivity.ACTIVITY_MODE, AssistanceActivity.HOST_CLIENT_SELECT_MODE);
				currentConext.startActivity(myIntent);
				
				try {
					Thread.sleep(800);
					Log.i("20083271", "Thread Go!");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	public MyTest(Context context){
		this.currentConext = context;
	}
	
	public void goThread(){
		thread.start();
	}
	
	public void goJust(){
		for(int i = 0 ; i < 5 ; i++){
			Intent myIntent = new Intent(currentConext, kr.ac.kookmin.cs.distboard.activity.AssistanceActivity.class);
			myIntent.putExtra(AssistanceActivity.ACTIVITY_MODE, AssistanceActivity.HOST_CLIENT_SELECT_MODE);
			currentConext.startActivity(myIntent);
			
			try {
				Thread.sleep(800);
				Log.i("20083271", "Just Go!");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
