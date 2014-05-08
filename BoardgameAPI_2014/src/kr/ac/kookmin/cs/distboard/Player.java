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
import kr.ac.kookmin.cs.distboard.system.ClientManager;
import kr.ac.kookmin.cs.distboard.system.EmulatorReceiver;
import kr.ac.kookmin.cs.distboard.system.SubjectDeviceMapper;

/**
 * �� Ŭ������ �ȵ���̵� ��ġ�� ����ϴ� �÷��̾ �߻�ȭ�մϴ�.
 * @author jeon2
 */
public class Player implements Serializable{
	
    
	private static final String TAG = "20083271:Player";
	
	/**
	 * �÷��̾� ������ �ν��Ͻ�
	 */
	private static PlayerListener playerListener = new PlayerAdapter();
	
	/**
	 * �ش� ��ġ�� �߻�ȭ�� �÷��̾�
	 */
	private static Player thisPlayer = new Player(true);
	
	/**
	 * Player �ν��Ͻ��� �� �ȵ���̵� ��ġ�� �߻�ȭ�� �������� ���� 
	 */
	private boolean isThisPlayer = false;
	
	/**
	 * �����ڰ� ȣ������ �ʽ��ϴ�.
	 * Player ��ü�� private ������, �ý��ۿ� ���ؼ��� ȣ��˴ϴ�.
	 * @param isThisPlayer �� Player �ν��Ͻ��� ���� �ȵ���̵� ��ġ�� �߻�ȭ�� �������� ����
	 */
	private Player(boolean isThisPlayer){
	    this.isThisPlayer = isThisPlayer;
	}

	/**
	 * �����ڰ� ȣ������ �ʽ��ϴ�.
	 * 
	 */
	public Player(){
	    
	}
	
	//normal listener
	
	/**
	 * �����ڰ� ȣ������ �ʽ��ϴ�.
	 * �÷��̾� �����ʸ� ����մϴ�.
	 * @param playerListener ����� �÷��̾� ������
	 */
	public static void registerPlayerListenr(PlayerListener playerListener){
		Player.playerListener = playerListener;
	}
	
	/**
	 * �����ڰ� ȣ������ �ʽ��ϴ�.
	 * ��ϵ� �÷��̾� �����ʸ� �����մϴ�.
	 */
	public static void unRegisterPlayerListenr(){
		Player.playerListener = new PlayerAdapter();
	}
	
	
	//getter
	
	/**
	 * �÷��̾��� �̸��� ��ȯ�մϴ�. 
	 * �÷��̾��� �̸��� ������� ����Ϳ� ��ϵ� �̸��� �����մϴ�.
	 * @return �÷��̾��� �̸�
	 */
	public String getName(){
	    if(this.isThisPlayer()){
	        return BluetoothAdapter.getDefaultAdapter().getName();
	    }
		return SubjectDeviceMapper.getInstance().map(this).getName();
	}
	
	
	//normal methods

	/**
	 * �÷��̾� �ν��Ͻ��� �� �ȵ���̵� ��ġ�� �߻�ȭ�� �������� ���� ���θ� ��ȯ�մϴ�.
	 * @return �÷��̾� �ν��Ͻ��� �� �ȵ���̵� ��ġ�� �߻�ȭ�� �������� ���� ����
	 */
	public boolean isThisPlayer(){
	    return isThisPlayer;
	}

	/**
	 * �÷��̾�� ������Ʈ�� �����մϴ�.
	 * �������� PlayerListener �������̽��� receiveObject �޼��带 �����Ͽ� �ش� ������Ʈ�� �����մϴ�.
	 * @param obj ������ ������Ʈ
	 */
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
	
	
	/**
	 * �÷��̾�� ��Ȳ�� �����մϴ�.
	 * ��Ȳ�� ������Ʈ �ϳ��� int Ÿ���� ���� 2���� �����մϴ�.
	 * �������� PlayerListener �������̽��� receiveSituation �޼��带 �����Ͽ� �ش� ��Ȳ�� �����մϴ�.
	 * @param obj ������ ������Ʈ
	 * @param arg1 ������ ����1
	 * @param arg2 ������ ����2
	 */
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
	
	/**
	 * �÷��̾��� ������ӵ���-�ֻ����� ��밡���� ���·� ����ϴ�.
	 */
	public void openVirtualGameToolDice(){
	    Log.i(TAG, "���� ���ӵ��� �ֻ��� ���� �޼��� ����");
		if(this.isThisPlayer() == true){
		    Log.i(TAG, "�� �÷��̾��� ���");
		    EmulatorReceiver.getInstance().appear(0);
		}else{
		    Log.i(TAG, "�� �÷��̾ �ƴ�");
		    RequestReplyManager.getInstance().sendRequest(this, Request.APPEAR_DICE_EMULATOR, null);
		}
	}
	
	/**
     * �÷��̾��� ������ӵ���-���� ��밡���� ���·� ����ϴ�.
     */
    public void openVirtualGameToolYut(){
        Log.i(TAG, "���� ���ӵ��� �� ���� �޼��� ����");
        if(this.isThisPlayer() == true){
            Log.i(TAG, "�� �÷��̾��� ���");
            EmulatorReceiver.getInstance().appear(1);
        }else{
            Log.i(TAG, "�� �÷��̾ �ƴ�");
            RequestReplyManager.getInstance().sendRequest(this, Request.APPEAR_YUT_EMULATOR, null);
        }
    }
	
	//called by host
	
	/**
	 * �÷��̾��� ������ӵ����� ��� �Ұ��� ���·� ����ϴ�.
	 */
	public void closeVirtualGameTool(){
		//this play ó�� ���߿� �߰�
	    if(this.isThisPlayer() == true){
            Log.i(TAG, "�� �÷��̾��� ���");
            EmulatorReceiver.getInstance().disappear();
	    }else{
	        Log.i(TAG, "�� �÷��̾ �ƴ�");
	        RequestReplyManager.getInstance().sendRequest(this, Request.DISAPPEAR_EMULATOR, null);
	    }
	}
	
	/**
	 * �÷��̾ ���Ͽ� ������ӵ���-�ֻ����� ������ ���ڸ�ŭ �����ϰ��մϴ�. 
	 * @param numOfDice ������ ������ӵ���-�ֻ����� ����
	 */
	public void setNumOfDice(int numOfDice){
	    if(this.isThisPlayer()){
	        EmulatorReceiver.getInstance().setNumberOfDice(numOfDice);
	    }else{
	        RequestReplyManager.getInstance().sendRequest(this, Request.SET_NUM_OF_DICE, numOfDice);
	    }
	}
	
	/**
	 * ���� �������� �ʴ� �޼���
	 * @param numOfYuts
	 */
	public void setNumOfYuts(int numOfYuts){
	    if(this.isThisPlayer()){
            EmulatorReceiver.getInstance().setNumberOfYuts(numOfYuts);
        }else{
            RequestReplyManager.getInstance().sendRequest(this, Request.SET_NUM_OF_YUT, numOfYuts);
        }
	}
	
	//static method
	
	/**
	 * ��� �÷��̾�� ������Ʈ�� �����մϴ�.
	 * Ŭ���̾�Ʈ �������� ȣ��Ʈ �÷��̾���� ���ü��� �����ϱ� ������ �� �޼��带 ȣ������ ����
	 * giveObject(...)�޼��带 ȣ���ϱ⸦ �����մϴ�.
	 * @param obj ������ ������Ʈ
	 */
	public static void giveObjectToAllPlayers(Object obj){
	    Log.i(TAG, "GIVE OBJECT TO ALL PLAYERS");
        RequestReplyManager.getInstance().sendRequestToAllPlayers(Request.GIVE_OBJECT, obj);
    }
    
	/**
	 * ��� �÷��̾�� ��Ȳ�� �����մϴ�.
	 * Ŭ���̾�Ʈ �������� ȣ��Ʈ �÷��̾���� ���ü��� �����ϱ� ������ �� �޼��带 ȣ������ ����
	 * giveSituation(...)�޼��带 ȣ���ϱ⸦ �����մϴ�.
	 * @param obj ������ ������Ʈ
	 * @param arg1 ������ ����1
	 * @param arg2 ������ ����2
	 */
    public static void sendSituationToAllPlayers(Object obj, int arg1, int arg2){
        RequestReplyManager.getInstance().sendRequestToAllPlayers(Request.GIVE_OBJECT, new ObjectArgument(obj, arg1, arg2)); 
    }
	
    /**
     * �����ڰ� ȣ������ �ʽ��ϴ�.
     * �� �ȵ���̵� ��ġ�� �߻�ȭ�� �÷��̾� �ν��Ͻ��� ��ȯ�մϴ�.
     * @return �� �ȵ���̵� ��ġ�� �߻�ȭ�� �÷��̾� �ν��Ͻ�
     */
	public static Player getThisPlayer(){
	    return thisPlayer;
	}
	
	/**
	 * �����ڰ� ȣ������ �ʽ��ϴ�.
	 * ��ϵ� �÷��̾� ������ �ν��Ͻ��� ��ȯ�մϴ�.
	 * @return ��ϵ� �÷��̾� ������ �ν��Ͻ�
	 */
	public static PlayerListener getPlayerListener(){
		return Player.playerListener;
	}
}
