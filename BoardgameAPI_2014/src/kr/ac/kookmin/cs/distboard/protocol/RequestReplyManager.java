package kr.ac.kookmin.cs.distboard.protocol;

import java.io.Serializable;

import kr.ac.kookmin.cs.distboard.DistributedBoardgame;
import kr.ac.kookmin.cs.distboard.GameTool;
import kr.ac.kookmin.cs.distboard.Mediator;
import kr.ac.kookmin.cs.distboard.Player;
import kr.ac.kookmin.cs.distboard.activity.AssistanceActivity;
import kr.ac.kookmin.cs.distboard.subobject.DicePlusGameTool;
import kr.ac.kookmin.cs.distboard.subobject.YutGameTool;
import kr.ac.kookmin.cs.distboard.system.ClientManager;
import kr.ac.kookmin.cs.distboard.system.DicePlusManager;
import kr.ac.kookmin.cs.distboard.system.ElectricYutManager;
import kr.ac.kookmin.cs.distboard.system.EmulatorReceiver;
import kr.ac.kookmin.cs.distboard.system.GameToolSystemManager;
import kr.ac.kookmin.cs.distboard.system.SubjectDeviceMapper;
import kr.ac.kookmin.cs.distboard.util.AsynchronousNotAvailableThread;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class RequestReplyManager{
	
	public static final String TAG = "20083271:RequestReplyManager";
	
	private static RequestReplyManager instance = new RequestReplyManager();

	
	//message byte 테이블 끝
	
	
	private boolean initialized = false;
	
	private RequestReplyManager(){
		
	}
	
	public static RequestReplyManager getInstance(){
		return instance;
	}
	
	public void initialize(){
		instance.initialized = true;
	}

	//받았을 때 처리
	public void handleMessage(Player player, Object message){
		Log.d(TAG, "메시지 수신, 핸들러 처리 진입");
		Request currentRequest = null;
		Reply currentReply = null;
		
		if(message instanceof Request){
			Log.d(TAG, "요청 메시지로 판명");
			currentRequest = (Request)message;
			
			switch(currentRequest.code){
				case Request.OK_TO_GO:
					Log.d(TAG, "OK_TO_GO로 판명");
					Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.CONNECT_OK_TO_GO).sendToTarget();
					break;
				case Request.OK_TO_RESUME:
					Log.d(TAG, "OK_TO_RESUME으로 판명");
					Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.CONNECT_OK_TO_RESUME).sendToTarget();
					break;
				case Request.GIVE_OBJECT://
					Log.d(TAG, "GIVE_SITUATION로 판명");
					Player.getPlayerListener().onReceiveObject(player, currentRequest.content);
					break;
				case Request.GIVE_SITUATION://
                    Log.d(TAG, "GIVE_SITUATION로 판명");
                    ObjectArgument objArgument = ((ObjectArgument)currentRequest.content);
                    if(currentRequest.content instanceof ObjectArgument == false){
                        Log.e(TAG, "GIVE_SITUATION 상황에서 오브젝트가 '오브젝트인자'형이 아님");
                        break;
                    }
                    Player.getPlayerListener().onReceiveSituation(player, objArgument.obj , objArgument.arg1, objArgument.arg2);
                    break;
				case Request.APPEAR_DICE_EMULATOR:
					Log.d(TAG, "APPEAR_DICE_EMULATOR로 판명");
					EmulatorReceiver.getInstance().appear();
					break;
				case Request.DISSAPEAR_DICE_EMULATOR:
					Log.d(TAG, "DISSAPPEAR_DICE_EMULATOR로 판명");
					EmulatorReceiver.getInstance().disappear();
					break;
				case Request.SET_NUM_OF_DICE:
                    Log.d(TAG, "SET_NUM_OF_DICE로 판명");
                    EmulatorReceiver.getInstance().setNumberOfDice((Integer)currentRequest.content);
                    break;
				case Request.SET_NUM_OF_YUT:
                    Log.d(TAG, "SET_NUM_OF_YUT로 판명");
                    EmulatorReceiver.getInstance().setNumberOfYut((Integer)currentRequest.content);
                    break;
			}

		}else if(message instanceof Reply){
			Log.d(TAG, "응답 메시지로 판명");
			currentReply = (Reply)message;
			
			switch(currentReply.code){
			case Reply.ROLL_DICE_RESULT:
				Log.d(TAG, "ROLL_DICE_RESULT로 판명");
				GameToolSystemManager.getInstance().onRemoteVirtualDiceRoll(player, (int[])currentReply.content);
			}
		}
	}
	
	public void handleMessage(Player player, byte[] bytesMessage){
		
	}
	
	public void sendRequest(Player player, int requestType, Object obj){
		
	    if(isAvaliable(player) == false){
		    new AsynchronousNotAvailableThread(player).start();
		    return;
		}
		
		//Log.i(TAG, "플레이어에게 객체전송 진입 : " + SubjectDeviceMapper.getInstance().map(player).getName());
		
		
		if(obj == null){
			Log.w(TAG, "전송되는 컨텐츠는 null ! ");
		}
		
		
		if(obj != null && obj instanceof Serializable == false){
			Log.e(TAG, "객체가 직렬화가능하지 않습니다. 전송을 취소합니다.");
			return;
		}
		
		
		//나 인경우 포함하자.... ㄷㄷㄷ
		
		
		Request request = Request.getRequest(requestType, obj);
		
		Log.i(TAG, SubjectDeviceMapper.getInstance().map(player).getName() + "으로 메시지를 송신");
		Log.i(TAG, "송신되는 내용은 : 오브젝트 : " + request.content);
		Log.i(TAG, "송신되는 내용은 : 코드 : " + request.code);
		ClientManager.getInstance().write(SubjectDeviceMapper.getInstance().map(player), request);
		
		
	}
	
	public void sendRequestToAllPlayer(int requestType, Object obj){
	    
		Log.d(TAG, "모든 플레이어에게 객체 전송 진입");
		Player[] currentPlayers = DistributedBoardgame.getInstance().getPlayers();
		
		
		for(int i = 0 ; i < currentPlayers.length ; i++){
			Log.d(TAG, "플레이어 수 : " + currentPlayers.length);
			if(currentPlayers[i] != null){//연결이 끊기지 않았다면
				Log.d(TAG, "현재 메시지 쓰기 목적지 클라이언트 : " + SubjectDeviceMapper.getInstance().map(currentPlayers[i]).getName());
				RequestReplyManager.getInstance().sendRequest(currentPlayers[i], requestType, obj);
			}
		}
	}
	
	public void sendReply(Player player, int replyType, Object obj){
	    if(isAvaliable(player) == false){
            new AsynchronousNotAvailableThread(player).start();
            return;
        }
	    
		//나 인경우 포함하자.... ㄷㄷㄷ
		Reply reply = Reply.getRequest(replyType, obj);
		ClientManager.getInstance().write(SubjectDeviceMapper.getInstance().map(player), reply);
	}

	private boolean isAvaliable(Object obj){
	    Log.i(TAG, "객체가 전송가능한 상태인지 검사");
	    if(obj instanceof Player){
	        Log.i(TAG, "객체(플레이어)가 전송가능한 상태인지 검사");
	        return ClientManager.getInstance().isAvailableDevice(SubjectDeviceMapper.getInstance().map((Player)obj));
	    }else if(obj instanceof GameTool){
	        if(obj instanceof DicePlusGameTool){
	            Log.i(TAG, "객체(DICE+)가 전송가능한 상태인지 검사");
	            return DicePlusManager.getInstance().isAvailableDevice(SubjectDeviceMapper.getInstance().map((DicePlusGameTool)obj));
	        }else if(obj instanceof YutGameTool){
	            Log.i(TAG, "객체(I-BAR)가 전송가능한 상태인지 검사");
	            return ElectricYutManager.getInstance().isAvailableDevice(SubjectDeviceMapper.getInstance().map((Player)obj));
	        }
	    }
	    Log.e(TAG, "객체가 전송가능한 상태인지 검사 : 알 수 없는 타입의 객체");
	    return false;
	}
}
