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
 * 이 클래스는 안드로이드 장치를 사용하는 플레이어를 추상화합니다.
 * @author jeon2
 */
public class Player implements Serializable{
	
    
	private static final String TAG = "20083271:Player";
	
	/**
	 * 플레이어 리스너 인스턴스
	 */
	private static PlayerListener playerListener = new PlayerAdapter();
	
	/**
	 * 해당 장치를 추상화한 플레이어
	 */
	private static Player thisPlayer = new Player(true);
	
	/**
	 * Player 인스턴스가 이 안드로이드 장치를 추상화한 것인지의 여부 
	 */
	private boolean isThisPlayer = false;
	
	/**
	 * 개발자가 호출하지 않습니다.
	 * Player 객체의 private 생성자, 시스템에 의해서만 호출됩니다.
	 * @param isThisPlayer 이 Player 인스턴스가 현재 안드로이드 장치를 추상화한 것인지의 여부
	 */
	private Player(boolean isThisPlayer){
	    this.isThisPlayer = isThisPlayer;
	}

	/**
	 * 개발자가 호출하지 않습니다.
	 * 
	 */
	public Player(){
	    
	}
	
	//normal listener
	
	/**
	 * 개발자가 호출하지 않습니다.
	 * 플레이어 리스너를 등록합니다.
	 * @param playerListener 등록할 플레이어 리스너
	 */
	public static void registerPlayerListenr(PlayerListener playerListener){
		Player.playerListener = playerListener;
	}
	
	/**
	 * 개발자가 호출하지 않습니다.
	 * 등록된 플레이어 리스너를 해제합니다.
	 */
	public static void unRegisterPlayerListenr(){
		Player.playerListener = new PlayerAdapter();
	}
	
	
	//getter
	
	/**
	 * 플레이어의 이름을 반환합니다. 
	 * 플레이어의 이름은 블루투스 어댑터에 기록된 이름을 참조합니다.
	 * @return 플레이어의 이름
	 */
	public String getName(){
	    if(this.isThisPlayer()){
	        return BluetoothAdapter.getDefaultAdapter().getName();
	    }
		return SubjectDeviceMapper.getInstance().map(this).getName();
	}
	
	
	//normal methods

	/**
	 * 플레이어 인스턴스가 이 안드로이드 장치를 추상화한 것인지에 대한 여부를 반환합니다.
	 * @return 플레이어 인스턴스가 이 안드로이드 장치를 추상화한 것인지에 대한 여부
	 */
	public boolean isThisPlayer(){
	    return isThisPlayer;
	}

	/**
	 * 플레이어에게 오브젝트를 수여합니다.
	 * 수신측은 PlayerListener 인터페이스의 receiveObject 메서드를 구현하여 해당 오브젝트를 수신합니다.
	 * @param obj 수여할 오브젝트
	 */
	public void giveObject(Object obj){
	    Log.i(TAG, "오브젝트 주기 메서드 진입");
	    if(this.isThisPlayer() == true){
	        Log.w(TAG, "이 플레이어의 경우 : 일단 워닝");
	        //추가해 줄 수 있음
	        return;
	    }else{
	        Log.i(TAG, "이 플레이어가 아님");
	        RequestReplyManager.getInstance().sendRequest(this, Request.GIVE_OBJECT, obj);
	    }
	}
	
	
	/**
	 * 플레이어에게 상황을 수여합니다.
	 * 상황은 오브젝트 하나와 int 타입의 인자 2개를 랩핑합니다.
	 * 수신측은 PlayerListener 인터페이스의 receiveSituation 메서드를 구현하여 해당 상황을 수신합니다.
	 * @param obj 수여할 오브젝트
	 * @param arg1 수여할 인자1
	 * @param arg2 수여할 인자2
	 */
	public void giveSituation(Object obj, int arg1, int arg2){
	    Log.i(TAG, "상황 주기 메서드 진입");
        if(this.isThisPlayer() == true){
            Log.w(TAG, "이 플레이어의 경우 : 일단 워닝");
            //추가해 줄 수 있음
            return;
        }else{
            Log.i(TAG, "이 플레이어가 아님");
            RequestReplyManager.getInstance().sendRequest(this, Request.GIVE_OBJECT, new ObjectArgument(obj, arg1, arg2));
        }
	}
	

	//called by host
	
	/**
	 * 플레이어의 가상게임도구-주사위를 사용가능한 상태로 만듭니다.
	 */
	public void openVirtualGameToolDice(){
	    Log.i(TAG, "가상 게임도구 주사위 열기 메서드 진입");
		if(this.isThisPlayer() == true){
		    Log.i(TAG, "이 플레이어의 경우");
		    EmulatorReceiver.getInstance().appear(0);
		}else{
		    Log.i(TAG, "이 플레이어가 아님");
		    RequestReplyManager.getInstance().sendRequest(this, Request.APPEAR_DICE_EMULATOR, null);
		}
	}
	
	/**
     * 플레이어의 가상게임도구-윷을 사용가능한 상태로 만듭니다.
     */
    public void openVirtualGameToolYut(){
        Log.i(TAG, "가상 게임도구 윷 열기 메서드 진입");
        if(this.isThisPlayer() == true){
            Log.i(TAG, "이 플레이어의 경우");
            EmulatorReceiver.getInstance().appear(1);
        }else{
            Log.i(TAG, "이 플레이어가 아님");
            RequestReplyManager.getInstance().sendRequest(this, Request.APPEAR_YUT_EMULATOR, null);
        }
    }
	
	//called by host
	
	/**
	 * 플레이어의 가상게임도구를 사용 불가능 상태로 만듭니다.
	 */
	public void closeVirtualGameTool(){
		//this play 처리 나중에 추가
	    if(this.isThisPlayer() == true){
            Log.i(TAG, "이 플레이어의 경우");
            EmulatorReceiver.getInstance().disappear();
	    }else{
	        Log.i(TAG, "이 플레이어가 아님");
	        RequestReplyManager.getInstance().sendRequest(this, Request.DISAPPEAR_EMULATOR, null);
	    }
	}
	
	/**
	 * 플레이어에 대하여 가상게임도구-주사위의 개수를 인자만큼 설정하게합니다. 
	 * @param numOfDice 설정할 가상게임도구-주사위의 개수
	 */
	public void setNumOfDice(int numOfDice){
	    if(this.isThisPlayer()){
	        EmulatorReceiver.getInstance().setNumberOfDice(numOfDice);
	    }else{
	        RequestReplyManager.getInstance().sendRequest(this, Request.SET_NUM_OF_DICE, numOfDice);
	    }
	}
	
	/**
	 * 아직 지원하지 않는 메서드
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
	 * 모든 플레이어에게 오브젝트를 수여합니다.
	 * 클라이언트 측에서는 호스트 플레이어에대한 가시성만 존재하기 때문에 이 메서드를 호출하지 말고
	 * giveObject(...)메서드를 호출하기를 권장합니다.
	 * @param obj 수여할 오브젝트
	 */
	public static void giveObjectToAllPlayers(Object obj){
	    Log.i(TAG, "GIVE OBJECT TO ALL PLAYERS");
        RequestReplyManager.getInstance().sendRequestToAllPlayers(Request.GIVE_OBJECT, obj);
    }
    
	/**
	 * 모든 플레이어에게 상황을 수여합니다.
	 * 클라이언트 측에서는 호스트 플레이어에대한 가시성만 존재하기 때문에 이 메서드를 호출하지 말고
	 * giveSituation(...)메서드를 호출하기를 권장합니다.
	 * @param obj 수여할 오브젝트
	 * @param arg1 수여할 인자1
	 * @param arg2 수여할 인자2
	 */
    public static void sendSituationToAllPlayers(Object obj, int arg1, int arg2){
        RequestReplyManager.getInstance().sendRequestToAllPlayers(Request.GIVE_OBJECT, new ObjectArgument(obj, arg1, arg2)); 
    }
	
    /**
     * 개발자가 호출하지 않습니다.
     * 이 안드로이드 장치를 추상화한 플레이어 인스턴스를 반환합니다.
     * @return 이 안드로이드 장치를 추상화한 플레이어 인스턴스
     */
	public static Player getThisPlayer(){
	    return thisPlayer;
	}
	
	/**
	 * 개발자가 호출하지 않습니다.
	 * 등록된 플레이어 리스너 인스턴스를 반환합니다.
	 * @return 등록된 플레이어 리스너 인스턴스
	 */
	public static PlayerListener getPlayerListener(){
		return Player.playerListener;
	}
}
