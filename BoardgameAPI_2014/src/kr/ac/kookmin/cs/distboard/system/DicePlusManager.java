package kr.ac.kookmin.cs.distboard.system;

import java.util.ArrayList;

import kr.ac.kookmin.cs.distboard.DistributedBoardgame;
import kr.ac.kookmin.cs.distboard.GameTool;
import kr.ac.kookmin.cs.distboard.Mediator;
import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import kr.ac.kookmin.cs.distboard.util.ArrayListConverter;
import kr.ac.kookmin.cs.distboard.util.ThreadTimer;
import us.dicepl.android.sdk.BluetoothManipulator;
import us.dicepl.android.sdk.DiceConnectionListener;
import us.dicepl.android.sdk.DiceController;
import us.dicepl.android.sdk.DiceResponseAdapter;
import us.dicepl.android.sdk.DiceResponseListener;
import us.dicepl.android.sdk.DiceScanningListener;
import us.dicepl.android.sdk.Die;
import us.dicepl.android.sdk.responsedata.RollData;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class DicePlusManager {

	private static final String TAG = "20083271:DicePlusManager";
	
	
	private static DicePlusManager instance = new DicePlusManager();
	private static final int[] developerKey = new int[] { 0x83, 0xed, 0x60, 0x0e, 0x5d, 0x31, 0x8f, 0xe7 };

	
	private boolean isTimeOuted = false; // 타임아웃 되었는지 나타냄 재 구성할때는 이 값이 다시 false 가 되겠지
										//이 값이 false 라는 것은 "연결 구성 중" 이라는 것.
	
	private boolean hasPerfectlyNominated = false;//null인 주사위없이 완벽하게 후보화되었으면, 이것은 다시 취소될 수 있다.
	
	private ThreadTimer threadTimer = null; // 추가
	private boolean initialized = false;
	private int exactElectricGameToolDicePlus = 0;
	private ArrayList<Die> dice = new ArrayList<Die>();
	private ArrayList<Die> scaned = new ArrayList<Die>();//이미 검색되어 커넥트 시도했던 Die - 속도때문에 한번에 2개의 스캐닝된것 커넥트하는 문제 해결하기위함
	//연결끊어지면 scaned에서도 지워주자!!(다시 연결해야되니까.)
	
	DiceScanningListener scanningListener = new DiceScanningListener() {

		@Override
		public synchronized void onNewDie(Die die) {
			Log.d(TAG, "새로운 Dice+ : " + die.getAddress());
			//dicePluses.add(die);
			if(scaned.indexOf(die) == -1 && isTimeOuted == false && hasPerfectlyNominated == false){//기존에 접속 안된놈이면
				Log.d(TAG, "기존 탐색 목록에 없던 Dice+ 판명: " + die.getAddress());
				scaned.add(die);
				DiceController.connect(die);
			}
		}
		

		@Override
		public synchronized void onScanStarted() {
			Log.d(TAG, "스캔 시작됨");
			if(isTimeOuted == true || hasPerfectlyNominated == true)
				BluetoothManipulator.cancelScan();
		}

		@Override
		public synchronized void onScanFailed() {
			Log.d(TAG, "스캔 실패");
			//BluetoothManipulator.startScan()
		}

		@Override
		public synchronized void onScanFinished() {
			Log.d(TAG, "스캔 종료");
			
			//타임아웃 되지않았으면 다시시도
			if(isTimeOuted == false && hasPerfectlyNominated == false){
				Log.i(TAG, "리스캔 시도중");
				BluetoothManipulator.startScan();
			}
			/*if (dice.size() != exactElectricGameToolDicePlus) {
				Log.d(TAG, "Rescan Started");
				BluetoothManipulator.startScan();
			}*/
		}
	};
	
	DiceConnectionListener connectionListener = new DiceConnectionListener() {

		@Override
		public synchronized void onConnectionEstablished(Die die) {
			Log.d(TAG, "DICE+ Connected");

			// Signing up for roll events
			if (dice.indexOf(die) == -1) {// 기존에 연결안된놈이면
				dice.add(die);
				CommunicationStateManager.getInstance().onDicePlusConnected(die);

				// 만약 모든 주사위가 모였으면 이제그만 스캔
				if (dice.size() == exactElectricGameToolDicePlus && hasPerfectlyNominated == false) {
					Log.i(TAG, "모든 Dice+ 연결 완료");

					hasPerfectlyNominated = true;
					BluetoothManipulator.cancelScan();

					//노미네이트로 바꿀 것
					/*CommunicationStateManager.getInstance()
							.onDicePlusEstablishComplete(
									ArrayListConverter
											.toDiceArrayListToArray(dice));*/
					CandidateManager.getInstance().nominateDice(ArrayListConverter.toDiceArrayListToArray(dice));
					
					

					// 구독 등록
					for (int i = 0; i < dice.size(); i++) {
						DiceController.subscribeRolls(dice.get(i));
					}
					Log.i(TAG, "모든 Dice+ 구독 등록 완료");
				}
			}else{
				//이미 존재하던 놈이면
				// = 안되는데..
				Log.e(TAG, "이미 연결된 DICE+");
			}
		}

		@Override
		public synchronized void onConnectionFailed(Die die, Exception e) {
			Log.d(TAG, "연결 실패", e);
			scaned.remove(die);
			if(isTimeOuted == false)
				BluetoothManipulator.startScan();
		}

		@Override
		public synchronized void onConnectionLost(Die die) {
			Log.d(TAG, "Connection Lost");
			
			//끊김보고
			CommunicationStateManager.getInstance().onDicePlusLost(die);

			if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE && initialized == true){//연결 구성 중일때, 또는 초기화되었을때, 초기화는 clear시에 연결 종료로 다시 불려지지 않게하기 위함.
				//해당 주사위를 삭제 - 어차피 구성 엔트리이어서는 안되기 때문
				hasPerfectlyNominated = false;
				dice.remove(die);
				scaned.remove(die);
				BluetoothManipulator.startScan();
			}else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME && initialized == true){//게임중에 끊겼다면
			    Log.e(TAG, "게임중일때 DICE+ 접속 끊김.");
			}
			//BluetoothManipulator.startScan();
		}

	};
	
	DiceResponseListener responseListener = new DiceResponseAdapter() {

		@Override
		public synchronized void onRoll(Die die, RollData rollData, Exception e) {
			super.onRoll(die, rollData, e);

			if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
				Log.d(TAG, "Roll: " + rollData.face);
				SubjectDeviceMapper.getInstance().map(die).setFace(rollData.face);//다이스 플러스 게임툴의 정보도 갱신하고
				
				GameToolSystemManager.getInstance().onDicePlusRoll(die, rollData);//따로 시스템적 보고도 한다
			}else{
				Log.w(TAG, "게임중이 아닐때 굴려진 주사위");
			}

		}
	};
	
	
	//생성자
	private DicePlusManager() {
		
	}
	
	public static DicePlusManager getInstance(){
		return instance;
	}
	
	public void initialize(){//싱글톤 조심용
		Log.i(TAG, "DICE+ 초기화");
		
		initialized = true;
		
		
		threadTimer = null;
		isTimeOuted = false;
		hasPerfectlyNominated = false;
		exactElectricGameToolDicePlus = 0;
		dice = new ArrayList<Die>();
		scaned = new ArrayList<Die>();
		
	}
	
	public void establish(int exactElectricGameToolDicePlus, int millisecTimeOut){
		
			Log.i(TAG, "Dice+ 구성 시작");

			// 초기화 되지 않았으면 : 싱글톤 조심용
			if (initialized == false) {
				Log.e(TAG, "초기화 되지 않음");
				return;
			}

			// 연결할 Dice+ 개수가 0개이면
			if (exactElectricGameToolDicePlus == 0) {
				// 보고 후 바로 리턴
				hasPerfectlyNominated = true;
				CandidateManager.getInstance().nominateDice(new Die[0]);
				// CommunicationStateManager.getInstance().onDicePlusEstablishComplete(new
				// Die[0]);
				return;
			}

			BluetoothManipulator.initiate(DistributedBoardgame.getInstance().getContext());
			DiceController.initiate(developerKey);

			this.exactElectricGameToolDicePlus = exactElectricGameToolDicePlus;

			// Listen to all the state occurring during the discovering process
			// of DICE+
			BluetoothManipulator.registerDiceScanningListener(scanningListener);

			// When connecting to DICE+ you get two responses: a good one and a
			// bad one ;)
			DiceController.registerDiceConnectionListener(connectionListener);

			// Attaching to DICE+ events that we subscribed to.
			DiceController.registerDiceResponseListener(responseListener);

			// Scan for a DICE+
			BluetoothManipulator.startScan();
			Log.i(TAG, "Dice+ 구성 시작");

			Log.i(TAG, "Dice+ 타임아웃 측정 시작 : " + millisecTimeOut);
			threadTimer = new ThreadTimer(
					ThreadTimer.DICE_PLUS_MANAGER_ESTABLISH_CALL,
					millisecTimeOut);
			threadTimer.start();
		
	}
	
	public void discardDicePluses(){
		
		/**
		 * 적어도 연결된놈이 하나이상있을때 이놈이 불리면안되 ; 강제 구성 버튼 없애는거로 막아주긴했는데..
		 */
		
		Log.i(TAG, "Dice plus 강제완성 진입");
		
		//강제 완성
		hasPerfectlyNominated = true;
		
		BluetoothManipulator.cancelScan();
		
		Log.e(TAG, "discard시 clear() 이것때문에 오류가 생길 수 있어서 에러 로그 ; 그리고 전자윷이나 클라이언트는 이런처리 안해줌");
		clear();
		
		CandidateManager.getInstance().nominateDice(new Die[0]);
		
		CommunicationStateManager.getInstance().onDicePlusEstablishForceComplete();
		
	}
	
	public synchronized boolean isAvailableDevice(Die die){
        if(dice.indexOf(die) != -1){
            return true;
        }
        return false;
    }
	
	//call by ThreadTimer
	
	public synchronized void onEstablishTimeOut(){
		Log.i(TAG, "Dice+ 구성 타임아웃됨");
		//이 내부에 있을때 connected 되는거는 어떻게 처리해?
		
		//타임아웃 뒤에 모든 접속시도 취소
	    isTimeOuted = true;
	    
	    //일단 스캔취소
	    BluetoothManipulator.cancelScan();
	    
	    //불완전한 구성이면 
	    if(exactElectricGameToolDicePlus != dice.size() && hasPerfectlyNominated == false){
	    	Log.e(TAG, "불완전한 Dice+ 구성 : " + dice.size() + "/" + exactElectricGameToolDicePlus);
	    	if(dice.size() > 0){
	    		//모두 삭제 "는 일단 하지말자." : 재구성할때 조금만 더 구성하면되도록 하기위해..
	    		//dice.removeAll(dice);
	    	}
	    	
			Log.w(TAG, "타임아웃됨을 노미네이트 - 다이스 플러스");
			CandidateManager.getInstance().nominateDicePlusEstablishFail();
			
	    	//CommunicationStateManager.getInstance().onDicePlusEstablishFailed();
	    	
	    }else if(exactElectricGameToolDicePlus == dice.size() && hasPerfectlyNominated == false){
	    	Log.i(TAG, "완전한 Dice+ 구성 : " + dice.size() + "/" + exactElectricGameToolDicePlus);
	    }
	    
	}
	
	//getter
	
	@Deprecated
	public Die[] getDice(){
		return (Die[])dice.toArray();
	}
	
	//장치 해제
	
	public void release(Die die){
		
		DiceController.disconnectDie(die);
		dice.set(dice.indexOf(die), null);
		//die = null; null 하면 맵퍼에서 isAvaiable 오류날듯
	}
	
	public void releaseAll(){
		for(int i = 0 ; i < dice.size() ; i++){
			DiceController.disconnectDie(dice.get(i));
			dice.set(i, null);
			//dice.set(i, null); null 하면 맵퍼에서 isAvaiable 오류날듯
		}
	}
	
	//멈추거나 파괴시 이것 호출
	public synchronized void clear(){
		
		Log.i(TAG, "Dice+ 정리");
		initialized = false;
		isTimeOuted = true;
		
		if(Mediator.getInstance().getMode() == Mode.HOST ){
			Log.i(TAG, "호스트 모드에서 정리");
			
			BluetoothManipulator.cancelScan();
			
			BluetoothManipulator.unregisterDiceScanningListener(scanningListener);
			DiceController.unregisterDiceConnectionListener(connectionListener);
			DiceController.unregisterDiceResponseListener(responseListener);

			Log.i(TAG, "호스트 모드에서 cancelScan, unregister() 완료");
			
			for(int i = 0 ; i < dice.size() ; i++){
				DiceController.disconnectDie(dice.get(i));//initialized false면 더이상 scan 호출 안하도록 하자.
				//dice.set(i, null); null 하면 맵퍼에서 isAvaiable 오류날듯
			}
		
			//주사위 리스트의 모든 원소 삭제
			dice.removeAll(dice);
		}
	}
	
	public synchronized void setTimeouted(){
        this.isTimeOuted = true;
    }
}