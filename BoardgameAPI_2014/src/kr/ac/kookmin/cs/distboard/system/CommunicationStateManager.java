package kr.ac.kookmin.cs.distboard.system;

import kr.ac.kookmin.cs.distboard.DistributedBoardgame;
import kr.ac.kookmin.cs.distboard.Mediator;
import kr.ac.kookmin.cs.distboard.Player;
import kr.ac.kookmin.cs.distboard.activity.AssistanceActivity;
import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import kr.ac.kookmin.cs.distboard.protocol.Reply;
import kr.ac.kookmin.cs.distboard.protocol.Request;
import kr.ac.kookmin.cs.distboard.protocol.RequestReplyManager;
import us.dicepl.android.sdk.Die;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CommunicationStateManager {
	
	private static final String TAG = "20083271:CommunicationStateManager";
	private static CommunicationStateManager instance = new CommunicationStateManager();
	
	
	//private boolean initialized = false;
	//private Handler handler = null;
	
	private CommunicationStateManager(){
		
	}
	
	public static CommunicationStateManager getInstance(){
		return instance;
	}
	
	/*public void initialize(Handler handler){
		initialized = true;
		this.handler = handler;
	}*/
	
	/*public void checkInitialized(){
		if(initialized == false){
			Log.e(TAG, "초기화되지 않음");
		}
	}*/
	
	
	//called by BluetoothManager
	
	
	//호스트
	
	
	//호스트 - 초기 구성설정 완료되면
	public void onEstablishComplete(BluetoothDevice[] clientDevices, BluetoothDevice[] yutDevices, Die[] dice){
		
		
		//checkInitialized();
		Log.i(TAG, "장치구성 완료됨");
		
		if(DistributedBoardgame.getInstance().getMode() != Mode.HOST){
			Log.e(TAG, "호스트 장치가 아닌 안드로이드 인스턴스가 호출");
		}
		
		//맵핑 등록
		SubjectDeviceMapper.getInstance().registerClientDevices(clientDevices);
		SubjectDeviceMapper.getInstance().registerYutDevices(yutDevices);
		SubjectDeviceMapper.getInstance().registerDice(dice);
		
		DicePlusManager.getInstance().setTimeouted();//2014년 4월 14일 추가됨 구성완료후 isTimeout를 "true"로 만들지 않았다.
		ElectricYutManager.getInstance().setTimeouted();//2014년 4월 14일 추가됨 구성완료후 isTimeout를 "true"로 만들지 않았다.
		
		//호스트 경우 자기 액티비티 끄고 (클라이언트도 여기 들어오긴해..망할)

		//모두 끝나면 OK_TO_GO 각 클라이언트에게 전송
	}
	
	public synchronized void onPlayerEstablishForceComplete(){
        Log.i(TAG, "클라이언트 구성 강제 성공 - 강제완성 버튼 없애줌");
        Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_PLAYER_FORCE_COMPLETE);
        message.sendToTarget();
    }
	
	//호스트 - 클라이언트또는 전자도구와 연결이 성공하면
	public void onConnected(BluetoothDevice device){
		//checkInitialized();
		Log.i(TAG, "장치 연결됨 : " + device.getAddress());
		//두 가지 경우
		//1. 구성중
		if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
			Log.i(TAG, "호스트 준비 모드에서 연결됨");
			Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_PLAYER_CONNECTED);
			message.obj = device;
			message.sendToTarget();
		}
		//2. 게임중
		else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
			Log.w(TAG, "호스트 게임중 모드에서 연결됨");
		}else{
			Log.w(TAG, "어떤 상황에서 연결되었는지 모름");
		}
	}
	
	
	public void onReconnected(BluetoothDevice device){
	    SubjectDeviceMapper.getInstance().replaceClientDevice(device);
		RequestReplyManager.getInstance().sendRequest((Player)SubjectDeviceMapper.getInstance().map(device), Request.OK_TO_RESUME, null);
		Mediator.getParticipantListener().onPlayerRejoin((Player)(SubjectDeviceMapper.getInstance().map(device)));
	}
	
	
	//호스트 - 초기 구성 시간넘으면
	public void onEstablishTimeOut(){
		Log.i(TAG, "총체적 구성이 타임아웃됨 : 곧 어플리케이션 종료");
		
		//checkInitialized();
		Mediator.getParticipantListener().onPrepareFail(); //의미없을듯 어차피 끌꺼야..
		
		Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_TIMEOUT);
		message.sendToTarget();
	}
	
	//호스트 - 끊긴 장치에 대해 WAIT 성공하면(성공적으로 접속했을 때)
	public void onWaitComplete(BluetoothDevice device){
		//checkInitialized();
		//구성도중에는 이 개념이 없다.
	}
	
	//호스트 - 해당 장치가 시간내에 접속안되면/안하면
	public void onWaitTimeout(BluetoothDevice device){
		//checkInitialized();
		//전자도구 경우
		//클라이언트 경우
	}
	
	//호스트 - 예상에 없던 장치가 접속시도하면
	@Deprecated
	public void onUnexpectedConnectionDetected(BluetoothDevice device){
		//checkInitialized();
	}
	
	
	
	//클라이언트
	
	
	//클라이언트 - 호스트로 접속 성공시
	public void onConnectionComplete(BluetoothDevice hostDevice){
		
		if(Mediator.getInstance().getMode() != Mode.CLIENT){
			Log.e(TAG, "클라이언트 장치가 아닌 안드로이드 인스턴스가 호출");
		}
		//checkInitialized();
		if(hostDevice == null){
			Log.e(TAG, "호스트 장치가 null임");
		}
		
		Log.i(TAG, "호스트 연결됨 : " + hostDevice.getAddress());
		//두 가지 경우
		//1. 구성중
		if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.CLIENT_JOIN_MODE){
			Log.i(TAG, "클라이언트 핸들러 보고 : 클라이언트 합류 모드");
			
			SubjectDeviceMapper.getInstance().registerClientDevices(new BluetoothDevice[]{hostDevice});//사실 이것은 길이가 1
			SubjectDeviceMapper.getInstance().registerYutDevices(new BluetoothDevice[0]);//길이 0
			SubjectDeviceMapper.getInstance().registerDice(new Die[0]);//길이 0
			
			Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.CONNECT_COMPLETE);
			message.obj = hostDevice;
			message.sendToTarget();
		}
		//2. 게임중
		else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
			Log.i(TAG, "클라이언트 핸들러 보고 : 클라이언트 게임중 모드");
		}else{
			Log.w(TAG, "클라이언트 핸들러 보고 : 무슨경우인지 모름");
		}
		
	}
	
	//클라이언트 - 접속 실패시
	public void onConnectionFail(BluetoothDevice device){
		//checkInitialized();
		Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.CONNECT_FAILED);
		message.obj = device;//호스트 장치
		message.sendToTarget();
	}
	
	//클라이언트 - 재접속 성공시
	public void onReconnectComplete(BluetoothDevice device){
		//checkInitialized();
		if(DistributedBoardgame.getInstance().getState() != DistributedBoardgame.MIDDLE_OF_GAME){
			Log.e(TAG, "게임중이 아닌 상황에서 호출된 재 접속 완료");
			return;
		}
		//checkInitialized();
		if(device == null){
			Log.e(TAG, "인자의 장치가 null임");
			return;
		}
		
		Log.i(TAG, "장치 재연결됨 : " + device.getName());

		Log.i(TAG, "클라이언트 핸들러 보고 : 클라이언트 합류 모드");

		//Mediator.getParticipantListener().onPlayerRejoin((Player)(SubjectDeviceMapper.getInstance().map(device)));

	}
	
	//????????????????? - 재접속 실패시
	public void onReconnectFailed(){
		//checkInitialized();
		Log.w(TAG, "재연결 실패");
	}
	
	
	
	//호스트/클라이언트
	
	//호스트 - 클라이언트 접속 끊기면
	public void onConnectionLost(BluetoothDevice device){
		//checkInitialized();
		Log.i(TAG, "클라이언트 연결 끊김 : " + device.getAddress());
		//세 가지 경우
		//1. 호스트 준비중
		if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
			Log.i(TAG, "호스트 핸들러 보고 : 호스트 준비 모드에서 연결 끊어짐");
			SubjectDeviceMapper.getInstance().lostPlayer();
			
			Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_PLAYER_DISCONNECTED);
			message.obj = device;
			message.sendToTarget();
			
            Message message2 = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.GAME_IS_NOT_STARTABLE);
            message2.sendToTarget();
		}
		//2. 클라이언트 합류중
		else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.CLIENT_JOIN_MODE){
			Log.i(TAG, "클라이언트 핸들러 보고 : 클라이언트 합류 모드에서 연결 끊어짐");
			Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.CONNECT_LOST);
			message.obj = device;
			message.sendToTarget();
		}
		//3. 게임중
		else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
			Log.i(TAG, "클라이언트 핸들러 보고 : 호스트/클라이언트 게임중에 연결 끊어짐");
			Mediator.getParticipantListener().onPlayerLeave((Player)SubjectDeviceMapper.getInstance().map(device));
		}else{
			Log.w(TAG, "무슨경우인지 모름");
		}
	}
	
	//오브젝트 또는 바이트 배열 도착시 호출
	public synchronized void onObjectDelivered(BluetoothDevice device, Object obj){
		RequestReplyManager.getInstance().handleMessage((Player)SubjectDeviceMapper.getInstance().map(device), obj);
		//적절한 처리
	}
	
	public synchronized void onBytesDelivered(BluetoothDevice device, byte[] bytes){
		RequestReplyManager.getInstance().handleMessage((Player)SubjectDeviceMapper.getInstance().map(device), bytes);
	}
	
	
	//called by DicePlusManager
	
	
	//Dice+
	
	/*public synchronized void onDicePlusEstablishComplete(Die[] dice){
		//checkInitialized();
		Log.i(TAG, "Dice+ 구성 완료");
		
		SubjectDeviceMapper.getInstance().registerDice(dice);
	}*/
	
	//타임아웃되거나 등 실패!!!
	
	
	public synchronized void onDicePlusEstablishComplete(Die[] dice){
		Log.i(TAG, "DICE+ 구성 성공");
		Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_DICEPLUS_COMPELETE);
		message.obj = dice;
		message.sendToTarget();
	}
	
	public synchronized void onDicePlusEstablishForceComplete(){
		Log.i(TAG, "DICE+ 구성 강제 성공 - 강제완성 버튼 없애줌");
		Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_DICEPLUS_FORCE_COMPLETE);
		message.sendToTarget();
	}
	
	@Deprecated
	//onEstablishTimeOut로 대체(onEstablishTimeOut할때 한꺼번에)
 	public synchronized void onDicePlusEstablishFailed(){
		//checkInitialized();
		Log.w(TAG, "Dice+ 구성 실패(타임아웃)");//즉, 구성 타임아웃.
		int state = DistributedBoardgame.getInstance().getState();
		if(state == DistributedBoardgame.HOST_PREPARE_MODE || state == DistributedBoardgame.CLIENT_JOIN_MODE){
			//Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.);
			//message.obj = die;
			//message.sendToTarget();
		}else{
			Log.w(TAG, "이 시점에 구성이 실패하면 안됨");
		}
	}
	
	public synchronized void onDicePlusConnected(Die die){
		//checkInitialized();
		Log.i(TAG, "Dice+ 연결됨 : " + die.getAddress());
		//두 가지 경우
		//1. 구성중
		if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
			Log.i(TAG, "DICE+ 핸들러 보고 : 호스트 준비 모드");
			Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_DICEPLUS_CONNECTED);
			Log.d(TAG, "die " + die.toString());
			message.obj = die;
			message.sendToTarget();
		}
		//2. 게임중
		else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
			Log.i(TAG, "DICE+ 핸들러 보고 : 호스트 게임중 모드");
		}else{
			Log.w(TAG, "DICE+ 핸들러 보고 : 무슨경우인지 모름");
		}
	}
	
	public synchronized void onDicePlusLost(Die die){
		//checkInitialized();
		Log.i(TAG, "Dice+ 끊어짐 : " + die.getAddress());
		//두 가지 경우
		//1. 구성중
		if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
		    SubjectDeviceMapper.getInstance().lostDicePlus();
			Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_DICEPLUS_DISCONNECTED);
			message.obj = die;
			message.sendToTarget();
		}
		//2. 게임중
		else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
			CommunicationStateManager.getInstance().onDicePlusLost(die);
			
			//그리고 다시 재구성한다.
		}
	}
	
	public synchronized void onDicePlusWaitComplete(Die die){
		//checkInitialized();
	}
	
	public synchronized void onDicePlusWaitTimeOut(Die die){
		//checkInitialized();
	}
	
	public synchronized void onElectricYutEstablishComplete(BluetoothDevice[] yuts){
		Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTALBISH_YUT_COMPLETE);
		message.sendToTarget();
	}
	
	public synchronized void onElectricYutEstablishForceComplete(){
        Log.i(TAG, "전자윷 구성 강제 성공 - 강제완성 버튼 없애줌");
        Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_YUT_FORCE_COMPLETE);
        message.sendToTarget();
    }
	
	//onEstablishTimeOut로 대체(onEstablishTimeOut할때 한꺼번에)
	@Deprecated
	public synchronized void onElectricYutEstablishFailed(){
		Log.w(TAG, "전자 윷 구성 실패");//즉, 구성 타임아웃.
	}
	
	public synchronized void onElectricYutConntected(BluetoothDevice device){
		//checkInitialized();
				Log.i(TAG, "전자 윷 연결됨 : " + device.getAddress());
				//두 가지 경우
				//1. 구성중
				if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
					Log.i(TAG, "DICE+ 핸들러 보고 : 호스트 준비 모드");
					Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_YUT_CONNECTED);
					message.obj = device;
					message.sendToTarget();
				}
				//2. 게임중
				else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
					Log.i(TAG, "전자윷 핸들러 보고 : 호스트 게임중 모드");
				}else{
					Log.w(TAG, "전자윷 핸들러 보고 : 무슨경우인지 모름");
				}
	}
	
	public synchronized void onElectricYutLost(BluetoothDevice device){
		Log.i(TAG, "전자 윷 끊어짐 : " + device.getAddress());
		//두 가지 경우
		//1. 구성중
		if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
		    SubjectDeviceMapper.getInstance().lostElectricYut();
			Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_YUT_DISCONNECTED);
			message.obj = device;
			message.sendToTarget();
		}
		//2. 게임중
		else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
			
		}
	}
}
