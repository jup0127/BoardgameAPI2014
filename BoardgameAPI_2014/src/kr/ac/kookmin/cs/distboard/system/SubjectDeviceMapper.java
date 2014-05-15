package kr.ac.kookmin.cs.distboard.system;

import kr.ac.kookmin.cs.distboard.Mediator;
import kr.ac.kookmin.cs.distboard.Player;
import kr.ac.kookmin.cs.distboard.activity.AssistanceActivity;
import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import kr.ac.kookmin.cs.distboard.subobject.DicePlusGameTool;
import kr.ac.kookmin.cs.distboard.subobject.YutGameTool;
import us.dicepl.android.sdk.Die;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SubjectDeviceMapper {
	
	private static final String TAG = "20083271:SubjectDeviceMapper";
	private static SubjectDeviceMapper instance = new SubjectDeviceMapper();
	
	private boolean isAvaliable = false;//모두 맵핑이 완료되었는지?(모든 장치가 등록되었는지)
	private boolean initialized = false;//초기화되었는지(검증위해서)
	
	
	private int minPlayers;
	private int maxPlayers;
	
	private int exactPlayers;
	private int exactYutGameTools;
	private int exactDicePluses;
	
	private Player[] players = null;
	private BluetoothDevice[] clientDevices = null;
	
	private YutGameTool[] yutGameTools = null;
	private BluetoothDevice[] yutDevices = null;
	
	private DicePlusGameTool[] dicePlusGameTools = null;
	private Die[] dice = null;
	
	//constructor
	private SubjectDeviceMapper(){
		
	}
	
	//여기서 핸들러는 단지 완료 보고용!
	public void initialize(int minPlayers, int maxPlayers, int exactDicePluses, int exactYutGameTools){//싱글톤 조심 최초 1회 반드시 초기화할 것
		Log.i(TAG, "맵퍼 초기화");
		isAvaliable = false;//싱글톤 조심용
		initialized = true;//초기화됨.
		
		this.minPlayers = minPlayers;
		this.maxPlayers = maxPlayers;
		
		this.exactPlayers = maxPlayers; 
		this.exactDicePluses = exactDicePluses;
		this.exactYutGameTools = exactYutGameTools;
		
		this.players = null;//싱글톤 조심용
		this.clientDevices = null;//싱글톤 조심용
		
		this.yutGameTools = null;//싱글톤 조심용
		this.yutDevices = null;//싱글톤 조심용
		
		this.dicePlusGameTools = null;//싱글톤 조심용
		this.dice = null;//싱글톤 조심용
	}
	
	//static method
	
	public static SubjectDeviceMapper getInstance(){
		return instance;
	}
	
	//method
	
	public synchronized boolean isAvailable(){
		
		
		if(	players == null || clientDevices == null || yutGameTools == null ||
				yutDevices == null || dicePlusGameTools == null || dice == null){
			isAvaliable = false;
			return false;
				
		}else if(players.length == clientDevices.length && yutGameTools.length == yutDevices.length && dicePlusGameTools.length == dice.length){
			isAvaliable = true;
			Log.i(TAG, "사용 가능");
		}else{
			Log.e(TAG, "사용 불가능");
		}
				
		
		
		return isAvaliable;
	}
	
	//called by bluetoothManager Or DicePlusManager
	
	public synchronized void registerClientDevices(BluetoothDevice[] clientDevices){
		Log.i(TAG, "클라이언트 배열 등록");
		if(initialized == false){
			Log.e(TAG, "초기화되지 않았습니다.");
			return;
		}
		
		if(clientDevices == null){
			Log.e(TAG, "클라이언트 장치 목록은 null이 될 수 없습니다.");
			return;
		}
		
		if(clientDevices.length >= minPlayers && clientDevices.length <= maxPlayers  || Mediator.getInstance().getMode() == Mode.CLIENT){
			this.clientDevices = clientDevices;
			
			this.players = new Player[clientDevices.length];
			
			for(int i = 0 ; i < clientDevices.length ; i++){
				this.players[i] = new Player();
			}
			
			//이 등록으로부터 맵퍼가 사용가능하면 중재자의 정보에 반영
			checkIsAvaiableAndCommit();
			
		}else{
			Log.e(TAG, "서로 맞지않는 길이 에러 : 클라이언트목록");
		}
	}
	
	public synchronized void registerYutDevices(BluetoothDevice[] yutDevices){
		
		Log.i(TAG, "윷 배열 등록");
		
		if(initialized == false)
			Log.e(TAG, "초기화되지 않았습니다.");
		
		if(yutDevices == null)
			Log.e(TAG, "윷 장치목록은 null이 될 수 없습니다.");
		
		if(yutDevices.length == exactYutGameTools || Mediator.getInstance().getMode() == Mode.CLIENT){
			this.yutDevices = yutDevices;
		
			this.yutGameTools = new YutGameTool[yutDevices.length];
		
			for(int i = 0 ; i < yutDevices.length ; i++){
				this.yutGameTools[i] = new YutGameTool();
			}
			
			checkIsAvaiableAndCommit();
			
		}else{
			Log.e(TAG, "서로 맞지 않는 길이 에러 : yut");
		}
	}
	
	public synchronized void registerDice(Die[] dice){
		Log.i(TAG, "주사위 배열 등록");
		if(initialized == false)
			Log.e(TAG, "초기화되지 않았습니다.");
		
		if(dice == null)
			Log.e(TAG, "주사위 배열은 null이 될 수 없습니다.");
		
		if(dice.length == exactDicePluses || Mediator.getInstance().getMode() == Mode.CLIENT){//중요한 태크닉
			this.dice = dice;
			this.dicePlusGameTools = new DicePlusGameTool[dice.length];
			
			for(int i = 0 ; i < dice.length ; i++){
				this.dicePlusGameTools[i] = new DicePlusGameTool();
			}
			
			checkIsAvaiableAndCommit();
			
		}else{
			Log.e(TAG, "서로 맞지않는 길이 에러 : dice+");
		}
	}

	//등록될 때마다 모두 등록되엇는지 확인 후 완료 보고
	private void checkIsAvaiableAndCommit(){
	    Log.i(TAG, "checkIsAvaiableAndCommit() 진입");
		if(isAvailable() == true){
		    Log.i(TAG, "checkIsAvaiableAndCommit() : 시작가능함!");
			Log.i(TAG, "맵퍼 결과 반영 - 최종 연결구성 완료");
			Log.i(TAG, "총 클라이언트(호스트 수) :" + players.length);
			Log.i(TAG, "총 윷 수 :" + yutGameTools.length);
			Log.i(TAG, "총 주사위 수 :" + dicePlusGameTools.length);
			
			
			//완료 보고 // 사실 중재자가 하는게 맞는거같에.. 나중에 바꾸자, 상태 매니저의 핸들러도..
			if(Mediator.getInstance().getMode() == Mode.HOST){
			    Log.i(TAG, "GAME_IS_STARTABLE 핸들러 메시지 전송 직전 모드는 : " + Mediator.getInstance().getMode());
			    Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.GAME_IS_STARTABLE);
			    message.sendToTarget();
			}
		}
	}
	
	//구성중에 맵퍼가 등록하지 못하도록
	public synchronized void lostPlayer(){
	    clientDevices = null;
	}
	
	public synchronized void lostDicePlus(){
	    yutDevices = null;
	}
	
	public synchronized void lostElectricYut(){
	    dice = null;
	}
	
	//게임중 인스턴스 바뀌었을 때
	public void replaceClientDevice(BluetoothDevice replaceDevice){//대체할 디바이스
	    for(int i = 0 ; i < clientDevices.length ; i++){
	        if(clientDevices[i].getAddress().equals(replaceDevice.getAddress())){
	            Log.i(TAG, clientDevices[i].hashCode() + "가 " + replaceDevice.hashCode() + "로 대체됨");
	            clientDevices[i] = replaceDevice;
	            
	            return;
	        }
	    }
	}
	
	public void replaceDie(Die replaceDie){
	    for(int i = 0 ; i < dice.length ; i++){
            if(dice[i].getAddress().equals(replaceDie.getAddress())){
                Log.i(TAG, dice[i].hashCode() + "가 " + replaceDie.hashCode() + "로 대체됨");
                dice[i] = replaceDie;
                return;
            }
        }
	}
	
	public void replaceYutDevice(BluetoothDevice replaceYutDevice){
        for(int i = 0 ; i < yutDevices.length ; i++){
            if(yutDevices[i].getAddress().equals(replaceYutDevice.getAddress())){
                Log.i(TAG, yutDevices[i].hashCode() + "가 " + replaceYutDevice.hashCode() + "로 대체됨");
                yutDevices[i] = replaceYutDevice;
                return;
            }
        }
    }
	
	//called by mediator
	
	//플레이어를 리턴, 사용가능하지 않으면 null
	public Player[] getPlayers(){
		if(isAvailable()){
			return this.players;
		}
		Log.e(TAG, "사용가능하지 않음");
		return null;
	}
	
	//전자윷을 리턴, 사용가능하지 않으면 null
	public YutGameTool[] getYutGameTools(){
		if(isAvailable()){
				return this.yutGameTools;
		}
		Log.e(TAG, "사용가능하지 않음");
		return null;
	}
	
	//전자윷을 리턴, 사용가능하지 않으면 null
	public DicePlusGameTool[] getDicePlusGameTools(){
		if(isAvailable()){
					return this.dicePlusGameTools;
		}
		Log.e(TAG, "사용가능하지 않음");
		return null;
	}
	
	//mapping method - device to subject
	//맵핑실패하거나 맵핑될 객체가 null 이면 null 리턴
	
	public Object map(BluetoothDevice device){
		if(isAvailable()){
			//먼저 클라이언트 훑어보고
			for(int i = 0 ; i < clientDevices.length ; i++){
				if(device.equals(clientDevices[i]) == true){
					return players[i];//플레이어 리턴
				}
			}
			//윷 훑어봄
			for(int i = 0 ; i < yutDevices.length ; i++){
				if(device.equals(yutDevices[i]) == true){
					return yutGameTools[i];//윷 리턴
				}
			}
		}
		return null;
	}
	
	public DicePlusGameTool map(Die die){
		if(isAvailable()){
			
			//주사위 장치를 훑어본다
			for(int i = 0 ; i < dice.length ; i++){
				if(die.equals(dice[i]) == true){
					return dicePlusGameTools[i];//플레이어 리턴
				}
			}
		}else{
			Log.e(TAG, "사용가능하지 않음");
		}
		return null;
	}
	
	//mapping method - subject to device
	//맵핑실패하거나 맵핑될 객체가 null 이면 null 리턴
	
	public BluetoothDevice map(Player player){
		if(isAvailable()){
			//플레이어 훑어본다
			for(int i = 0 ; i < players.length ; i++){
				if(player.equals(players[i]) == true){
					return clientDevices[i];//플레이어 리턴
				}
			}
			
		}else{
			Log.e(TAG, "Mapper is not available.");
		}
		return null;
	}
	
	public BluetoothDevice map(YutGameTool yutGameTool){
		if(isAvailable()){
			//먼저 클라이언트 훑어보고
			for(int i = 0 ; i < yutGameTools.length ; i++){
				if(yutGameTool.equals(yutGameTools[i]) == true){
					return yutDevices[i];//플레이어 리턴
				}
			}
		}else{
			Log.e(TAG, "Mapper is not available.");
		}
		return null;
	}

	public Die map(DicePlusGameTool dicePlusGameTool){
		if(isAvailable()){
			//먼저 클라이언트 훑어보고
			for(int i = 0 ; i < dicePlusGameTools.length ; i++){
				if(dicePlusGameTool.equals(dicePlusGameTools[i]) == true){
					return dice[i];//플레이어 리턴
				}
			}
		}else{
			Log.e(TAG, "Mapper is not available.");
		}
		return null;
	}
	
	
	//맵핑 정보랑 다 날림, 새로 구성하기위함.
	public void clear(){
		
	}

	
	
	//setter Mapper
	
	public void setMinPlayers(int minPlayers) {
		this.minPlayers = minPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}
	
	public void setExactPlayers(int exactPlayers){
	    this.exactPlayers = exactPlayers;
	}

	public void setExactYutGameTools(int exacetYutGameTools) {
		this.exactYutGameTools = exacetYutGameTools;
	}

	public void setExactDicePluses(int exacetDicePluses) {
		this.exactDicePluses = exacetDicePluses;
	}
	
}
