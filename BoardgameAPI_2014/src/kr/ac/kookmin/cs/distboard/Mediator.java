package kr.ac.kookmin.cs.distboard;

import kr.ac.kookmin.cs.distboard.activity.AssistanceActivity;
import kr.ac.kookmin.cs.distboard.controller.ParticipantAdapter;
import kr.ac.kookmin.cs.distboard.controller.ParticipantListener;
import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import kr.ac.kookmin.cs.distboard.protocol.Request;
import kr.ac.kookmin.cs.distboard.protocol.RequestReplyManager;
import kr.ac.kookmin.cs.distboard.subobject.DicePlusGameTool;
import kr.ac.kookmin.cs.distboard.subobject.YutGameTool;
import kr.ac.kookmin.cs.distboard.system.ClientManager;
import kr.ac.kookmin.cs.distboard.system.DicePlusManager;
import kr.ac.kookmin.cs.distboard.system.ElectricYutManager;
import kr.ac.kookmin.cs.distboard.system.SubjectDeviceMapper;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Mediator{
	
	public static String TAG = "20083271:Mediator";
	
	private static Mediator instance = new Mediator(); 
	
	private static final int DEFAULT_HOST_PREPARE_TIMEOUT = 120;
	private static final int DEFAULT_HOST_WAIT_TIMEOUT = 30;
	private static ParticipantListener participantListener = new ParticipantAdapter();
	
	public int hostPrepareTimeout = DEFAULT_HOST_PREPARE_TIMEOUT;
	public int hostWaitPlayerTimeout = DEFAULT_HOST_WAIT_TIMEOUT;
	
	private boolean initialized = false;
	private Handler handler = null;
	private Mode mode = Mode.NONE;
	private int minPlayers;
	private int maxPlayers;
	private int exactElectricGameToolDicePlus;
	private int exactElectricGameToolYut;
	
	private Mediator(){
		
	}
	
	public static Mediator getInstance(//초기화하면서 인스턴스를 얻습니다.(최초 1회)
			int minPlayers, 
			int maxPlayers, 
			int exactElectricGameToolDicePlus,
			int exactElectricGameToolYut){
		
		//초기화되지 않았다면
		
		instance.mode = Mode.NONE;//싱글톤 스태틱 조심용
		instance.hostPrepareTimeout = DEFAULT_HOST_PREPARE_TIMEOUT;//싱글톤 스태틱 조심용
		instance.hostWaitPlayerTimeout = DEFAULT_HOST_WAIT_TIMEOUT;//싱글톤 스태틱 조심용
		instance.minPlayers = minPlayers;
		instance.maxPlayers = maxPlayers;
		instance.exactElectricGameToolDicePlus = exactElectricGameToolDicePlus;
		instance.exactElectricGameToolYut = exactElectricGameToolYut;
			
		instance.initialized = true;
		
		return instance;
	}
	
	public static Mediator getInstance(){
		if(instance.initialized == true)
			return instance;
		else{
			Log.e(TAG, "is not initailized.");
			return null;//에러처리할것
		}
	}
	
	//싱글톤용 메서드
	
	public void negotiate(){
		//현재 설정된 시간제한으로 재협상
		negotiate(hostPrepareTimeout);
	}
	
	public void negotiate(int hostPrepareTimeout){
		DistributedBoardgame.getInstance().setState(DistributedBoardgame.HOST_CLIENT_SELECT_MODE);
		instance.hostPrepareTimeout = hostPrepareTimeout;
		Log.i(TAG, "negotiate");
		Intent intent = getDefaultIntent();
		intent.putExtra(AssistanceActivity.ACTIVITY_MODE, AssistanceActivity.HOST_CLIENT_SELECT_MODE);
		activateActivityAs(intent);
		
	}
	
	//called by Activity
	
	public void setHandler(Handler handler){
		this.handler = handler;
	}
	
	public Handler getHandler(){
		return this.handler;
	}
	
	private void prepare(){
		Log.d(TAG, "호스트 준비 입장");
		DistributedBoardgame.getInstance().setState(DistributedBoardgame.HOST_PREPARE_MODE);
		
		if(exactElectricGameToolDicePlus != 0 && exactElectricGameToolYut != 0){
			Log.w(TAG, "블루투스 어댑터 2개를 동시에 접근 하는것은 아직 불안정합니다!!!!!!");
			Log.w(TAG, "중재자의 연결구성 취소됨.");
			return;
		}
		
		//블루투스 매니저 접근
		ClientManager.getInstance().initialize(Mode.HOST, maxPlayers, exactElectricGameToolYut, hostPrepareTimeout);
		ClientManager.getInstance().establish();

		//다이스 플러스 매니저 접근
		DicePlusManager.getInstance().establish(exactElectricGameToolDicePlus, hostPrepareTimeout);
		
		//전자 윷 매니저 접근
		ElectricYutManager.getInstance().establish(exactElectricGameToolYut, hostPrepareTimeout);
		
		//액티비티 생성
		Intent intent = getDefaultIntent();
		intent.putExtra(AssistanceActivity.ACTIVITY_MODE, AssistanceActivity.HOST_PREPARE_MODE);
		intent.putExtra(AssistanceActivity.MIN_PLAYERS, minPlayers);
		intent.putExtra(AssistanceActivity.MAX_PLAYERS, maxPlayers);
		intent.putExtra(AssistanceActivity.EXACT_YUTS, exactElectricGameToolYut);
		intent.putExtra(AssistanceActivity.EXACT_DICEPLUSES, exactElectricGameToolDicePlus);
		activateActivityAs(intent);
		
	}
	
	private void join(){
		Log.d(TAG, "클라이언트 합류 입장");
		ClientManager.getInstance().initialize(Mode.CLIENT, maxPlayers, exactElectricGameToolYut, hostPrepareTimeout);
		DistributedBoardgame.getInstance().setState(DistributedBoardgame.CLIENT_JOIN_MODE);
		
		//블루투스 매니저 접근
		//BluetoothDevice[] devices = BluetoothManager.getInstance(Mode.CLIENT).getDiscoveredDevices();
		
		
		//액티비티 생성
		Intent intent = getDefaultIntent();
		intent.putExtra(AssistanceActivity.ACTIVITY_MODE, AssistanceActivity.CLIENT_JOIN_MODE);
		//intent.putExtra(AssistanceActivity.DISCOVERED_DEVICES, devices);
		activateActivityAs(intent);//액티비티가 연결 코드 호출
		
	}

	public void leave(){
		
	}
	
	public void getOut(Player player){
		
	}
	
	public static void registerParticipantListener(ParticipantListener participantListener){
		Mediator.participantListener = participantListener;
	}
	
	public static void unregisterParticipantListener(){
		participantListener = new ParticipantAdapter();
	}

	//called by bluetooth manager
	
	public void setMode(Mode mode){
		//인자로 온 모드가 부적절하거나 기존에 모드가 있다면 에러.
		if(this.mode == Mode.NONE && mode != Mode.NONE)
			this.mode = mode;
		else
			Log.e(TAG, "Unexpected mode");
	}
	
	public Mode getMode(){
		return this.mode;
	}
	
	public void setHostPrepareTimeout(int hostPrepareTimeout){
		this.hostPrepareTimeout = hostPrepareTimeout;
	}
	
	public void setHostWaitPlayerTimeout(int hostWaitPlayerTimeout){
		this.hostWaitPlayerTimeout = hostWaitPlayerTimeout;
	}
	
	//called by Activity
	
	public void completeSelectHostClient(Mode mode){
		this.mode = mode;
		DistributedBoardgame.getInstance().setMode(mode);
		Log.i(TAG, "mode selected : " + mode);
		
		//모드 선택 액티비티에 의해 선택된 모드의 협상을 진행합니다.
		if(mode == Mode.HOST){
			prepare();
		}else if(mode == Mode.CLIENT){
			join();
		}else{
			Log.e(TAG, "mode was not selected.");
		}
		
	}
	
	//called by mapper
	
	public void completeMapping(Player[] players, YutGameTool[] yutGameTools, DicePlusGameTool[] dicePlusGameTools){
		//보드게임의 구성요소 설정
		Log.i(TAG, "맵핑 완료");
		
		Log.i(TAG, "플레이어들 " + players);
		Log.i(TAG, "전자윷들 " + yutGameTools);
		Log.i(TAG, "DICE+들 " + dicePlusGameTools);
		
		DistributedBoardgame.getInstance().setPlayers(players);
		DistributedBoardgame.getInstance().setYutGameTools(yutGameTools);
		DistributedBoardgame.getInstance().setDicePlusGameTools(dicePlusGameTools);
		
		Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.COMPLETE_ACTIVITY);//액티비티 끄고
        RequestReplyManager.getInstance().sendRequestToAllPlayer(Request.OK_TO_GO, null);//모든 애들에게 시작해도 좋다는 신호 전송
        message.sendToTarget();
		
		DistributedBoardgame.getInstance().setState(DistributedBoardgame.MIDDLE_OF_GAME);
			
		//
		if(mode == Mode.HOST){//호스트인 경우, 클라이언트 경우엔 OK_TO_GO 기다리자.
			Log.i(TAG, "온 게임 스타터블 작동 : " + Mediator.getInstance().getMode() + " (모드 에서)");
			DistributedBoardgame.getInstance().getDistributedBoardgameListener().onGameStartable(mode);
		}
	}
	
	//helper method
	
	private Intent getDefaultIntent(){
		return new Intent(
				DistributedBoardgame.getInstance().getContext(), 
				kr.ac.kookmin.cs.distboard.activity.AssistanceActivity.class);
	}
	
	private void activateActivityAs(Intent intent){
		//액티비티 활성화
		DistributedBoardgame.getInstance().getContext().startActivity(intent);
	}

	//static method
	
	public static ParticipantListener getParticipantListener(){
		return Mediator.participantListener;
	}

	//getter and setter 
	
	 public int getMinPlayers() {
	        return minPlayers;
	    }

	    public int getMaxPlayers() {
	        return maxPlayers;
	    }

	    public int getExactElectricGameToolDicePlus() {
	        return exactElectricGameToolDicePlus;
	    }

	    public int getExactElectricGameToolYut() {
	        return exactElectricGameToolYut;
	    }

	    public void setMinPlayers(int minPlayers) {
	        this.minPlayers = minPlayers;
	    }

	    public void setMaxPlayers(int maxPlayers) {
	        this.maxPlayers = maxPlayers;
	    }

	    public void setExactElectricGameToolDicePlus(int exactElectricGameToolDicePlus) {
	        this.exactElectricGameToolDicePlus = exactElectricGameToolDicePlus;
	    }

	    public void setExactElectricGameToolYut(int exactElectricGameToolYut) {
	        this.exactElectricGameToolYut = exactElectricGameToolYut;
	    }
	
}
