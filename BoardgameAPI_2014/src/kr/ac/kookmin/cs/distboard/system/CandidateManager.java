package kr.ac.kookmin.cs.distboard.system;


import kr.ac.kookmin.cs.distboard.Mediator;
import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import us.dicepl.android.sdk.Die;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class CandidateManager {
	
	private static final String TAG = "20083271:CandidateManager";
	private static CandidateManager instance = new CandidateManager();
	
	private CandidateManager(){
		
	}
	
	public void initialize(){
		
		initialized = true;
		
		this.dice = null;
		this.clientDevices = null;
		this.yutDevices = null;
		
		atLeastOneEstablishTimeoutFlag = false;
	}
	
	private boolean initialized = false;
	
	private Die[] dice = null;
	private BluetoothDevice[] clientDevices = null;
	private BluetoothDevice[] yutDevices = null;
	
	private boolean atLeastOneEstablishTimeoutFlag = false;//최소 하나라도 타임아웃되면 전체타임아웄이다!
	
	
	public static CandidateManager getInstance(){
		
		return instance;
		
	}
	
	//장치관련
	
	public synchronized void nominateDice(Die[] dice){
		Log.d(TAG, "DICE+ 노미네이트");
		
		if(instance.initialized == false){
			Log.i(TAG, "초기화되지 않았습니다.");
			return;
		}
		
		//debug code
		for(int i = 0 ; i < dice.length ; i++){
			Log.d(TAG, "노미네이트된 주사위 : " + dice[i].getAddress());
		}
		
		//후보화
		this.dice = dice;
		//모든 후보가 null이 아닌지, 즉 구성중 Lost 되지 않았는지 검사
		checkAllCandidates();
	}
	
	public synchronized void nominateClientDevices(BluetoothDevice[] clientDevices){
		Log.d(TAG, "클라이언트 장치 노미네이트");
		if(instance.initialized == false){
			Log.i(TAG, "초기화되지 않았습니다.");
			return;
		}
		
		//debug code
		for(int i = 0 ; i < clientDevices.length ; i++){
			Log.d(TAG, "노미네이트된 클라이언트 : " + clientDevices[i].getName());
		}
		
		//후보화
		this.clientDevices = clientDevices;
		//모든 후보가 null이 아닌지, 즉 구성중 Lost 되지 않았는지 검사
		checkAllCandidates();
	}
	
	public synchronized void nominateYutDevices(BluetoothDevice[] yutDevices){
		Log.d(TAG, "전자윷 장치 노미네이트");
		
		if(instance.initialized == false){
			Log.i(TAG, "초기화되지 않았습니다.");
			return;
		}
		
		//debug code
		for(int i = 0 ; i < yutDevices.length ; i++){
			Log.d(TAG, "노미네이트된 전자윷 : " + yutDevices[i].getName());
		}
		
		//후보화
		this.yutDevices = yutDevices;
		
		//모든 후보가 null이 아닌지, 즉 구성중 Lost 되지 않았는지 검사
		checkAllCandidates();
	}
	
	
	
	//완벽하게 후보화되었다면 보고 아니면 해당 부분고침
	private synchronized void checkAllCandidates(){
		Log.d(TAG, "후보자 체크");
		if(instance.initialized == false){
			Log.i(TAG, "초기화되지 않았습니다.");
			return;
		}
		
		if(dice != null && clientDevices != null && yutDevices != null){
			
			for(int i = 0 ; i < dice.length ; i++){
				
				if(dice[i] == null){
					dice = null;
					return;
				}
			}
			
			for(int i = 0 ; i < clientDevices.length ; i++){
				if(clientDevices[i] == null){
					clientDevices = null;
					return;
				}
			}
			
			for(int i = 0 ; i < yutDevices.length ; i++){
				if(yutDevices[i] == null){
					yutDevices = null;
					return;
				}
			}
			
			//여기까지왔으면 후보군 완벽
			
			Log.d(TAG, "후보군이 완벽합니다.");
			
			Log.i(TAG, "맵퍼에 길이 수정 결과 반영 : 클=" + clientDevices.length + "다=" + dice.length + "윷=" + yutDevices.length );
			
			SubjectDeviceMapper.getInstance().setExactPlayers(clientDevices.length);
			SubjectDeviceMapper.getInstance().setExactDicePluses(dice.length);
			SubjectDeviceMapper.getInstance().setExactYutGameTools(yutDevices.length);
			
			if(Mediator.getInstance().getMode() == Mode.HOST){
				Log.i(TAG, "후보군 호스트용 보고");
				CommunicationStateManager.getInstance().onEstablishComplete(clientDevices, yutDevices, dice);
			}else{
				Log.i(TAG, "후보군 클라이언트용 보고");
				CommunicationStateManager.getInstance().onConnectionComplete(clientDevices[0]);//clientDevices는 사실 host
			}
		}
		
		return;
	}

	//타임아웃관련 - 해당 구성이 실패했다는 것을 후보화함
	
	public synchronized void nominateClientEstablishFail(){
		if(atLeastOneEstablishTimeoutFlag == false){
			atLeastOneEstablishTimeoutFlag = true;
			CommunicationStateManager.getInstance().onEstablishTimeOut();
		}
	}
	
	public synchronized void nominateDicePlusEstablishFail(){
		if(atLeastOneEstablishTimeoutFlag == false){
			atLeastOneEstablishTimeoutFlag = true;
			CommunicationStateManager.getInstance().onEstablishTimeOut();
		}
	}
	
	public synchronized void nominateElectricYutEstablishFail(){
		if(atLeastOneEstablishTimeoutFlag == false){
			atLeastOneEstablishTimeoutFlag = true;
			CommunicationStateManager.getInstance().onEstablishTimeOut();
		}
	}
}
