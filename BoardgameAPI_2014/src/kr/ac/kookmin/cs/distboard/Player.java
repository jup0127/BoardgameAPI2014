package kr.ac.kookmin.cs.distboard;

import java.io.Serializable;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import kr.ac.kookmin.cs.distboard.controller.PlayerAdapter;
import kr.ac.kookmin.cs.distboard.controller.PlayerListener;
import kr.ac.kookmin.cs.distboard.protocol.ObjectArgument;
import kr.ac.kookmin.cs.distboard.protocol.Request;
import kr.ac.kookmin.cs.distboard.protocol.RequestReplyManager;
import kr.ac.kookmin.cs.distboard.subobject.Distracter;
import kr.ac.kookmin.cs.distboard.system.BluetoothManager;
import kr.ac.kookmin.cs.distboard.system.EmulatorReceiver;
import kr.ac.kookmin.cs.distboard.system.SubjectDeviceMapper;

public class Player implements Serializable{
	
	private static final String TAG = "20083271:Player";
	
	private static PlayerListener playerListener = new PlayerAdapter();
	
	private static Player thisPlayer = new Player(true);
	
	private boolean isThisPlayer = false;
	
	private Player(boolean isThisPlayer){
	    this.isThisPlayer = isThisPlayer;
	}

	public Player(){
	    
	}
	
	//normal listener
	
	public static void registerPlayerListenr(PlayerListener playerListener){
		Player.playerListener = playerListener;
	}
	
	public static void unRegisterPlayerListenr(){
		Player.playerListener = new PlayerAdapter();
	}
	
	//getter
	
	public String getName(){
	    if(this.isThisPlayer()){
	        return BluetoothAdapter.getDefaultAdapter().getName();
	    }
		return SubjectDeviceMapper.getInstance().map(this).getName();
	}
	
	
	//normal methods

	public boolean isThisPlayer(){
	    return isThisPlayer;
	}

	public void giveObject(Object obj){
	    Log.i(TAG, "������Ʈ �ֱ� �޼��� ����");
	    if(this.isThisPlayer() == true){
	        Log.w(TAG, "�� �÷��̾��� ��� : �ϴ� ����");
	        //�߰��� �� �� ����
	        return;
	    }else{
	        Log.i(TAG, "�� �÷��̾ �ƴ�");
	        RequestReplyManager.getInstance().sendRequest(this, Request.GIVE_OBJECT, obj);
	    }
	}
	
	public void giveSituation(Object obj, int arg1, int arg2){
	    Log.i(TAG, "��Ȳ �ֱ� �޼��� ����");
        if(this.isThisPlayer() == true){
            Log.w(TAG, "�� �÷��̾��� ��� : �ϴ� ����");
            //�߰��� �� �� ����
            return;
        }else{
            Log.i(TAG, "�� �÷��̾ �ƴ�");
            RequestReplyManager.getInstance().sendRequest(this, Request.GIVE_OBJECT, new ObjectArgument(obj, arg1, arg2));
        }
	}
	
	
	
	//called by host
	public void openVirtualGameToolDice(){
	    Log.i(TAG, "���� ���ӵ��� �ֻ��� ���� �޼��� ����");
		if(this.isThisPlayer() == true){
		    Log.i(TAG, "�� �÷��̾��� ���");
		    EmulatorReceiver.getInstance().appear();
		}else{
		    Log.i(TAG, "�� �÷��̾ �ƴ�");
		    RequestReplyManager.getInstance().sendRequest(this, Request.APPEAR_DICE_EMULATOR, null);
		}
	}
	
	//called by host
	public void closeVirtualGameToolDice(){
		//this play ó�� ���߿� �߰�
	    if(this.isThisPlayer() == true){
            Log.i(TAG, "�� �÷��̾��� ���");
            EmulatorReceiver.getInstance().disappear();
	    }else{
	        Log.i(TAG, "�� �÷��̾ �ƴ�");
	        RequestReplyManager.getInstance().sendRequest(this, Request.DISSAPEAR_DICE_EMULATOR, null);
	    }
	}
	
	public void setNumOfDice(int numOfDice){
	    if(this.isThisPlayer()){
	        EmulatorReceiver.getInstance().setNumberOfDice(numOfDice);
	    }else{
	        RequestReplyManager.getInstance().sendRequest(this, Request.SET_NUM_OF_DICE, numOfDice);
	    }
	}
	
	public void setNumOfYuts(int numOfYuts){
	    
	}
	
	//static method
	
	public static void giveObjectToAllPlayer(Object obj){
        RequestReplyManager.getInstance().sendRequestToAllPlayer(Request.GIVE_OBJECT, obj);
    }
    
    public static void sendSituationToAllPlayer(Object obj, int arg1, int arg2){
        RequestReplyManager.getInstance().sendRequestToAllPlayer(Request.GIVE_OBJECT, new ObjectArgument(obj, arg1, arg2)); 
    }
	
	public static Player getThisPlayer(){
	    return thisPlayer;
	}
	
	public static PlayerListener getPlayerListener(){
		return Player.playerListener;
	}
}
