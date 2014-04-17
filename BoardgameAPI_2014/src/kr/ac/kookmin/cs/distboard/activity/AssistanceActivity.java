package kr.ac.kookmin.cs.distboard.activity;


import java.util.ArrayList;
import java.util.HashMap;

import kr.ac.kookmin.cs.distboard.DistributedBoardgame;
import kr.ac.kookmin.cs.distboard.Mediator;
import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import kr.ac.kookmin.cs.distboard.protocol.Request;
import kr.ac.kookmin.cs.distboard.protocol.RequestReplyManager;
import kr.ac.kookmin.cs.distboard.system.ClientManager;
import kr.ac.kookmin.cs.distboard.system.DicePlusManager;
import kr.ac.kookmin.cs.distboard.system.ElectricYutManager;
import kr.ac.kookmin.cs.distboard.system.SubjectDeviceMapper;
import kr.ac.kookmin.cs.distboard.util.ArrayListConverter;
import us.dicepl.android.sdk.Die;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class AssistanceActivity extends Activity {

	private static final String TAG = "20083271:AssistanceActivity";
	
	private static final String DEFAULT_PLAYER_NAME = "Player";
	private static final String DEFAULT_DICE_PLUS_NAME = "Dice+";
	private static final String DEFAULT_VIRTUAL_DICE_NAME = "Virtual Dice";
	private static final String DEFAULT_ELECTRIC_YUT_NAME = "Ibar";
	private static final String DEFAULT_VIRTUAL_YUT_NAME = "Virtual Yut";
	
	private static final String DEFAULT_FORCE_PLAYERS_NAME = "Play with these people";
	private static final String DEFAULT_WITHOUT_DICE_PLUS_NAME = "Without Dice+";
	private static final String DEFAULT_WITHOUT_ELECTRIC_YUT_NAME = "Without Ibar";

	
	private static final String DEFAULT_START_BUTTON_NAME = "Start";
	
	
	
	//액티비티 모드 네임에 대해
	public static final String ACTIVITY_MODE = "activity_mode";
	//가능한 값들은
	public static final int HOST_CLIENT_SELECT_MODE = 0;
	public static final int HOST_PREPARE_MODE = 1;
	public static final int CLIENT_JOIN_MODE = 2;//클라이언트 접속 액티비티
	

	//호스트 준비모드 : 최소 플레이어 네임에 대해
	public static final String MIN_PLAYERS = "min_hosts";//가능한 값들은 정수

	//호스트 준비모드 : 최대 플레이어 네임에 대해
	public static final String MAX_PLAYERS = "max_hosts";//가능한 값들은 정수

	//호스트 준비모드 : 정확한 다이스 플러스 네임에 대해
	public static final String EXACT_DICEPLUSES = "exact_dicepluses";//가능한 값들은 정수

	//호스트 준비모드 : 정확한 윷 네임에 대해
	public static final String EXACT_YUTS = "exact_yuts";//가능한 값들은 정수
	
	//클라이언트 합류 모드 : 탐색된 장치들에 대해
	//public static final String DISCOVERED_DEVICES = "discovered_devices";//가능한 값들은 블루투스 장치 목록

	
	
	//여기서부터 핸들러 관련(비동기 연결 사건)
	
	//핸들러의 what
	
	
	
	public static final int ESTABLISH_COMPLETE = 0;
	public static final int ESTABLISH_PLAYER_FORCE_COMPLETE = 1;
	public static final int ESTABLISH_TIMEOUT = 2;
	public static final int ESTABLISH_PLAYER_CONNECTED = 3;//obj : 클라이언트 장치
	public static final int ESTABLISH_PLAYER_DISCONNECTED = 4;//obj : 클라이언트 장치
	
	public static final int ESTALBISH_YUT_COMPLETE = 5;
	public static final int ESTABLISH_YUT_FORCE_COMPLETE = 6;
	public static final int ESTABLISH_YUT_CONNECTED = 7;//obj : 윷 장치
	public static final int ESTABLISH_YUT_DISCONNECTED = 8;//obj : 윷 장치
	
	public static final int ESTABLISH_DICEPLUS_COMPELETE = 9;
	public static final int ESTABLISH_DICEPLUS_FORCE_COMPLETE = 10;
	public static final int ESTABLISH_DICEPLUS_CONNECTED = 11;//obj : Die객체
	public static final int ESTABLISH_DICEPLUS_DISCONNECTED = 12;//obj : Die객체

	public static final int CONNECT_COMPLETE = 13;//obj : 호스트 장치
	public static final int CONNECT_LOST = 14;//obj : 호스트 장치
	public static final int CONNECT_FAILED = 15;//obj : 호스트 장치
	
	public static final int RECONNECT_COMPLETE = 16;//obj : 장치
	public static final int RECONNECT_FAILED = 17;//?????????
	
	public static final int CONNECT_OK_TO_GO = 18;//호스트의 다음화면 넘어가도 좋은 신호
	public static final int CONNECT_OK_TO_RESUME = 19;//호스트의 다음화면 넘어가도 좋은 신호
	
	public static final int NEW_DEVICE_DISCOVERED = 20;//새로운 장치 발견!
	
	public static final int COMPLETE_ACTIVITY = 21;
	public static final int GAME_IS_STARTABLE = 22;//시작해도된다는!
	public static final int GAME_IS_NOT_STARTABLE = 23;
	
	
	//인스턴스 변수
	private int activityMode = -1;
	
	//private String[] discoveredDeviceAddresses = null;
	//BluetoothDevice[] discoveredDevices = null;
	ArrayList<BluetoothDevice> discoveredDevices = new ArrayList<BluetoothDevice>();
	
	
	//레이아웃
	LinearLayout linear = null;
	LinearLayout linearClients = null;
	LinearLayout linearClientsForce = null;
	LinearLayout linearDice = null;
	LinearLayout linearDiceForce = null;
	LinearLayout linearYuts = null;
	LinearLayout linearYutsForce = null;
	LinearLayout linearStart = null;
	
	
	//레이아웃:임시 인스턴스 변수
	ArrayList<Button> dicePlusButtons = new ArrayList<Button>();
	ArrayList<Button> playerButtons = new ArrayList<Button>();
	ArrayList<Button> yutButtons = new ArrayList<Button>();
	
	HashMap<String, Button> commonButtonsMap = new HashMap<String, Button>();//버튼과 해당 버튼 식별용! <맥주소,버튼>, 연결된 애들이 여기에 식별됨
	
	Button forcePlayerButton = null;
	Button forceDicePlusButton = null;
	Button forceElectricYutButton = null;
	
	Button startButton = null;
	//TextView tempTextView;//연결중이나 연결완료 표시할때
	
	//int lastPlayerActiveButtonIndex++ = 0;
	
	//플레이어 수 측정용
	int currentConnectedPlayers = 0;
	
	//도구 개수(마지막 인덱스) 측정용
	int lastDicePlusActiveButtonIndex = 0;
	int lastYutActiveButtonIndex = 0;
	
	//다룰 넘어온 인텐트
	Intent currentIntent = null;
	
	//텍스트 뷰 리스트
	//ArrayList<TextView> tvs = new ArrayList<TextView>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "액티비티가 만들어졌습니다.");
		
		//CommunicationStateManager.getInstance().initialize(handler);//handler의 존재를 알 필요가 있음!
		Mediator.getInstance().setHandler(handler);
		
		
		///블루투스 관련 설정////
		
		
		if(BluetoothAdapter.getDefaultAdapter() == null){
			Toast.makeText(this, "블루투스를 사용할 수 없습니다.", Toast.LENGTH_LONG).show();
		}
		
		
		
		
		
		////////////////////////
		
		
		currentIntent = getIntent();
		
		int activityMode = (int) currentIntent.getExtras().get(ACTIVITY_MODE);

		switch(activityMode){
		
		case HOST_CLIENT_SELECT_MODE:
			Log.i(TAG, "모드는 호스트 클라이언트 선택 모드");
			setHostClientSelectMode();
			break;
		case HOST_PREPARE_MODE:
			Log.i(TAG, "모드는 호스트 준비 모드");
			setHostPrepareMode();
			break;
		case CLIENT_JOIN_MODE:
			Log.i(TAG, "모드는 클라이언트 합류 모드");
			setClientJoinMode();
			break;
		}
	}
	
	private void setHostClientSelectMode() {
		activityMode = HOST_CLIENT_SELECT_MODE;
		
		linear = new LinearLayout(this);
		
		LinearLayout.LayoutParams liearParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		LinearLayout.LayoutParams buttonProperty = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		linear.setOrientation(LinearLayout.VERTICAL);
		linear.setLayoutParams(liearParams);

		/**
		 * 호스트 모드 버튼
		 */
		Button hostModeButton = new Button(this);
		hostModeButton.setLayoutParams(buttonProperty);
		hostModeButton.setText("호스트 모드");
		hostModeButton.setBackgroundColor(Color.MAGENTA);
		hostModeButton.setTextSize(14);
		hostModeButton.setPadding(5, 5, 5, 5);
		
		hostModeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View button) {
				Log.d(TAG, "온 클릭 호스트 모드");
                Mediator.getInstance().completeSelectHostClient(Mode.HOST);
                AssistanceActivity.this.finish();
			}
		});

		/**
		 * 클라이언트 모드 버튼
		 */
		Button clientModeButton = new Button(this);
		
		clientModeButton.setText("클라이언트 모드");
		clientModeButton.setBackgroundColor(Color.CYAN);
		
		clientModeButton.setLayoutParams(buttonProperty);
		clientModeButton.setTextSize(14);
		clientModeButton.setPadding(5, 5, 5, 5);
		clientModeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View button) {
				Log.d(TAG, "온 클릭 클라이언트 모드");
				Mediator.getInstance().completeSelectHostClient(Mode.CLIENT);
				AssistanceActivity.this.finish();
				
			}
		});

		/**
		 * 두 가지 버튼 설정
		 */
		linear.addView(hostModeButton);
		linear.addView(clientModeButton);

		setContentView(linear);
	}
	
	private void setHostPrepareMode() {
		activityMode = HOST_PREPARE_MODE;
		
		linear = new LinearLayout(this);
		linearClients = new LinearLayout(this);
		linearClientsForce = new LinearLayout(this);
		linearDice = new LinearLayout(this);
		linearDiceForce = new LinearLayout(this);
		linearYuts = new LinearLayout(this);
		linearYutsForce = new LinearLayout(this);
		linearStart = new LinearLayout(this);

		
		LinearLayout.LayoutParams buttonProperty = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		
		setPropertyOfLinearLayouts();

		Log.i(TAG, "호스트 준비 모드 입장");
		if(currentIntent == null)
			Log.e(TAG, "현재 인텐트가 null 입니다.");
		
		
		linear.addView(linearClients);
		linear.addView(linearClientsForce);
		linear.addView(linearDice);
		linear.addView(linearDiceForce);
		linear.addView(linearYuts);
		linear.addView(linearYutsForce);
		linear.addView(linearStart);
		
		
		int minPlayers = (int) currentIntent.getExtras().get(MIN_PLAYERS);
		int maxPlayers = (int) currentIntent.getExtras().get(MAX_PLAYERS);
		int exactDicePluses = (int) currentIntent.getExtras().get(EXACT_DICEPLUSES);
		int exactYuts = (int) currentIntent.getExtras().get(EXACT_YUTS);
		

		Log.i(TAG, "maxPlayers : " + maxPlayers);
		for(int i = 0 ; i < maxPlayers ; i++){
			Button button = new Button(this);
			button.setLayoutParams(buttonProperty);
			
			button.setText(DEFAULT_PLAYER_NAME);
			button.setBackgroundColor(Color.GRAY);
			playerButtons.add(button);
			linearClients.addView(button);
		}
		
		
        forcePlayerButton = new Button(this);
        forcePlayerButton.setLayoutParams(buttonProperty);
        forcePlayerButton.setText(DEFAULT_FORCE_PLAYERS_NAME);
        forcePlayerButton.setBackgroundColor(Color.BLUE);
        if(Mediator.getInstance().getMinPlayers() == 0 && Mediator.getInstance().getMaxPlayers() != 0){
            //forcePlayerButton.setVisibility(View.VISIBLE);
            linearClientsForce.addView(forcePlayerButton);
        }
        
        forcePlayerButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View button) {
                Log.d(TAG, "온 클릭 플레이어 강제구성");

                if (DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE) {
                    ClientManager.getInstance().forceClientEstablishment();
                }
                // AssistanceActivity.this.finish();
            }
        });
            
        //linearClientsForce.addView(forcePlayerButton);
        
        Log.i(TAG, "exactDicePluses : " + exactDicePluses);
		for(int i = 0 ; i < exactDicePluses ; i++){
			Button button = new Button(this);
			button.setLayoutParams(buttonProperty);
			
			button.setText(DEFAULT_DICE_PLUS_NAME);
			button.setBackgroundColor(Color.GRAY);
			dicePlusButtons.add(button);
			linearDice.addView(button);
		}
		
		if(exactDicePluses > 0){
		    
			forceDicePlusButton = new Button(this);
			forceDicePlusButton.setLayoutParams(buttonProperty);
			forceDicePlusButton.setText(DEFAULT_WITHOUT_DICE_PLUS_NAME);
			forceDicePlusButton.setBackgroundColor(Color.BLUE);
			forceDicePlusButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View button) {
					Log.d(TAG, "온 클릭 Dice plus 없이");
					Toast.makeText(AssistanceActivity.this, "Dice Plus없이 진행합니다...", Toast.LENGTH_LONG).show();
					//tempTextView.setText("연결중...");
					//setContentView(tempTextView);
					
					if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
						DicePlusManager.getInstance().discardDicePluses();
					}
					//AssistanceActivity.this.finish();
				}
			});
			
			linearDiceForce.addView(forceDicePlusButton);

		}

		Log.i(TAG, "exactYuts : " + exactYuts);
		for(int i = 0 ; i < exactYuts ; i++){
			Button button = new Button(this);
			button.setLayoutParams(buttonProperty);
			
			button.setText(DEFAULT_ELECTRIC_YUT_NAME);
			forceDicePlusButton.setBackgroundColor(Color.BLUE);
			yutButtons.add(button);
			linearYuts.addView(button);
		}
		
		if(exactYuts > 0){
			forceElectricYutButton = new Button(this);
			forceElectricYutButton.setLayoutParams(buttonProperty);
			forceElectricYutButton.setText(DEFAULT_WITHOUT_ELECTRIC_YUT_NAME);
			forceElectricYutButton.setBackgroundColor(Color.BLUE);
			forceElectricYutButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View button) {
					Log.d(TAG, "온 클릭 Electric yut 없이");
					Toast.makeText(AssistanceActivity.this, "전자 윷 없이 진행합니다...", Toast.LENGTH_LONG).show();
					//tempTextView.setText("연결중...");
					//setContentView(tempTextView);
					
					if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
						ElectricYutManager.getInstance().discardYuts();
					}
					//AssistanceActivity.this.finish();
				}
			});
			
			linearYutsForce.addView(forceElectricYutButton);
		}
		

		startButton = new Button(this);
		startButton.setLayoutParams(buttonProperty);
		startButton.setText(DEFAULT_START_BUTTON_NAME);
		startButton.setTextColor(Color.WHITE);
		startButton.setBackgroundColor(Color.BLACK);
		//startButton.setVisibility(View.INVISIBLE);
		startButton.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View button) {
				Log.d(TAG, "온 클릭 게임시작");

				Mediator.getInstance().completeMapping(SubjectDeviceMapper.getInstance().getPlayers(), 
				        SubjectDeviceMapper.getInstance().getYutGameTools(), 
				        SubjectDeviceMapper.getInstance().getDicePlusGameTools());
				
			}
		});
		
		//linearStart.addView(startButton);
		
		
		setContentView(linear);
	}
	
	private void setClientJoinMode() {
		
		Log.i(TAG, "클라이언트 조인 모드 메서드 진입");
		activityMode = CLIENT_JOIN_MODE;
		DistributedBoardgame.getInstance().setState(DistributedBoardgame.CLIENT_JOIN_MODE);
		linear = new LinearLayout(this);
		
		LinearLayout.LayoutParams liearParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		LinearLayout.LayoutParams buttonProperty = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		
		linear.setOrientation(LinearLayout.VERTICAL);
		linear.setLayoutParams(liearParams);
		linear.setVerticalScrollBarEnabled(true);
		
		
		
		Log.d(TAG, "블루투스 매니저에게 장치 검색 요청 시작");
		discoveredDevices = ArrayListConverter.bluetoothDeviceArrayToArrayList(ClientManager.getInstance().getDiscoveredDevices());//블루투스 매니저 최초 초기화 지점
		
		Log.d(TAG, "블루투스 매니저에게 장치 검색 요청 완료");
		for(int i = 0 ; i < discoveredDevices.size() ; i++){
			
			Button button = new Button(this);
			
			button.setGravity(Gravity.CENTER);
			button.setLayoutParams(buttonProperty);
			
			playerButtons.add(button);//버튼 추가
			button.setText(discoveredDevices.get(i).getName());
			
			button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View button) {
					Log.d(TAG, "온 클릭 접속중");
					Toast.makeText(AssistanceActivity.this, "연결중..", Toast.LENGTH_LONG).show();
					//tempTextView.setText("연결중...");
					//setContentView(tempTextView);
					
					ClientManager.getInstance().connect(discoveredDevices.get(playerButtons.indexOf(button)));
					//AssistanceActivity.this.finish();
				}
			});
			linear.addView(button);
		}
		setContentView(linear);
	}
	
	//핸들러
	
	private Handler handler = new Handler(){ //메인(UI)스레드의 핸들러
		public void handleMessage(Message msg){
			Log.i(TAG, "핸들러 메시지 받음");
			switch(msg.what){
			
			//호스트 준비 모드
				case ESTABLISH_COMPLETE:
					Log.i(TAG, "핸들러 메시지 : 모든 구성이 완료됨");
					Toast.makeText(getApplicationContext(), "prepare complete", Toast.LENGTH_LONG).show();
					break;
				case ESTABLISH_PLAYER_FORCE_COMPLETE:
                    
                    Log.i(TAG, "핸들러 메시지 : 플레이어 강제 구성 완료");
                    //가상으로 연결된 상태로 바꿈
                    for(int i = 0 ; i < playerButtons.size() ; i++){
                        if(commonButtonsMap.containsValue(playerButtons.get(i)) == false){//연결되지 않은 애들은 여기에없을꺼니까
                            //playerButtons.get(i).setVisibility(View.INVISIBLE);
                            linearClients.removeView(playerButtons.get(i));
                        }
                    }
                    //forcePlayerButton.setVisibility(View.INVISIBLE);
                    linearClientsForce.removeView(forcePlayerButton);
                    
                    break;	
					
				case ESTABLISH_TIMEOUT:
				    
				    
					Log.i(TAG, "핸들러 메시지 : 구성이 타임아웃");
					Toast.makeText(getApplicationContext(), "타임아웃 되었습니다!!", Toast.LENGTH_LONG).show();
					
					break;
					
					
				case ESTABLISH_PLAYER_CONNECTED:
				    
				    
					Log.i(TAG, "핸들러 메시지 : 플레이어 연결됨");
					Toast.makeText(getApplicationContext(), ((BluetoothDevice)msg.obj).getAddress() + "is connected", Toast.LENGTH_SHORT).show();
					
					for(int i = 0 ; i < playerButtons.size() ; i++){//모든 버튼을 검사하는데,
							
						if(commonButtonsMap.containsValue(playerButtons.get(i)) == false){//현재 버튼이 해쉬맵에 없으면(맵핑정보가 없으면)
							commonButtonsMap.put(((BluetoothDevice)msg.obj).getAddress(), playerButtons.get(i));
							playerButtons.get(i).setBackgroundColor(Color.GREEN);
							playerButtons.get(i).setText(((BluetoothDevice)msg.obj).getName());
							currentConnectedPlayers++;
							break;
						}
					}
					
					if(Mediator.getInstance().getMinPlayers() <= currentConnectedPlayers && Mediator.getInstance().getMaxPlayers() != currentConnectedPlayers){
					    //forcePlayerButton.setVisibility(View.VISIBLE);
					    if(linearClientsForce.indexOfChild(forcePlayerButton) == -1)
					        linearClientsForce.addView(forcePlayerButton);
					}

					break;
					
					
				case ESTABLISH_PLAYER_DISCONNECTED:
				    
				    
					Log.i(TAG, "핸들러 메시지 : 플레이어 연결 끊김");
					Toast.makeText(getApplicationContext(), ((BluetoothDevice)msg.obj).getAddress() + "is disconnected", Toast.LENGTH_LONG).show();
					commonButtonsMap.get(((BluetoothDevice)msg.obj).getAddress()).setBackgroundColor(Color.GRAY);
					commonButtonsMap.get(((BluetoothDevice)msg.obj).getAddress()).setText(DEFAULT_PLAYER_NAME);
					commonButtonsMap.remove(((BluetoothDevice)msg.obj).getAddress());
					currentConnectedPlayers--;
					break;
					
					
				case ESTALBISH_YUT_COMPLETE:
				    
				    
					Log.i(TAG, "핸들러 메시지 : 전자윷 구성 완료");
					break;
					
					
				case ESTABLISH_YUT_FORCE_COMPLETE:
                    
				    
                    Log.i(TAG, "핸들러 메시지 : 윷 강제 구성 완료");
                    
                    //가상으로 연결된 상태로 바꿈
                    for(int i = 0 ; i < yutButtons.size() ; i++){
                        yutButtons.get(i).setText(DEFAULT_VIRTUAL_YUT_NAME);
                        yutButtons.get(i).setBackgroundColor(Color.GREEN);
                    }
                    //forceElectricYutButton.setVisibility(View.INVISIBLE);
                    linearYuts.removeView(forceElectricYutButton);
                    
                    break;	
                    
                    
				case ESTABLISH_YUT_CONNECTED:
				    
				    
					Log.i(TAG, "핸들러 메시지 : 전자 윷 연결됨");
					Toast.makeText(getApplicationContext(), ((BluetoothDevice)msg.obj).getAddress() + "is connected", Toast.LENGTH_SHORT).show();
					yutButtons.get(lastYutActiveButtonIndex++).setBackgroundColor(Color.GREEN);
					if(forceElectricYutButton != null)
    					linearYuts.removeView(forceElectricYutButton);
					break;
					
					
				case ESTABLISH_YUT_DISCONNECTED:
				    
				    
					Log.i(TAG, "핸들러 메시지 : 전자 윷 연결 끊김");
					Toast.makeText(getApplicationContext(), ((BluetoothDevice)msg.obj).getAddress() + "is disconnected", Toast.LENGTH_LONG).show();
					yutButtons.get(lastYutActiveButtonIndex--).setBackgroundColor(Color.GRAY);
					break;
					
					
				case ESTABLISH_DICEPLUS_COMPELETE:
				    
				    
					Log.i(TAG, "핸들러 메시지 : DICE+ 구성 완료");
					break;
					
					
				case ESTABLISH_DICEPLUS_FORCE_COMPLETE:
				    
					
					Log.i(TAG, "핸들러 메시지 : DICE 강제 구성 완료");
					
					//가상으로 연결된 상태로 바꿈
					for(int i = 0 ; i < dicePlusButtons.size() ; i++){
						dicePlusButtons.get(i).setText(DEFAULT_VIRTUAL_DICE_NAME);
						dicePlusButtons.get(i).setBackgroundColor(Color.GREEN);
					}
					//forceDicePlusButton.setVisibility(View.INVISIBLE);
					linearDiceForce.removeView(forceDicePlusButton);
					break;
					
					
				case ESTABLISH_DICEPLUS_CONNECTED:
				    
				    
					Log.i(TAG, "핸들러 메시지 : DICE+ 연결됨");
					Toast.makeText(getApplicationContext(), ((Die)msg.obj).getAddress() + "is connected", Toast.LENGTH_SHORT).show();
					dicePlusButtons.get(lastDicePlusActiveButtonIndex++).setBackgroundColor(Color.GREEN);
					
					if(forceDicePlusButton != null)
						linearDiceForce.removeView(forceDicePlusButton);
					
					break;
					
					
				case ESTABLISH_DICEPLUS_DISCONNECTED:
				    
				    
					Log.i(TAG, "핸들러 메시지 : DICE+ 연결 끊김");
					Toast.makeText(getApplicationContext(), ((Die)msg.obj).getAddress() + "is disconnected", Toast.LENGTH_LONG).show();
					dicePlusButtons.get(lastDicePlusActiveButtonIndex--).setBackgroundColor(Color.GRAY);
					
					break;
					
					
			//클라이언트 접속 모드
				case CONNECT_COMPLETE:
				    
				    
					Log.i(TAG, "핸들러 메시지 : 호스트 연결됨");
					Toast.makeText(getApplicationContext(), ((BluetoothDevice)msg.obj).getAddress() + "is connected", Toast.LENGTH_SHORT).show();
					//tempTextView.setText("연결완료! 다른 플레이어 대기중...");
					//playerButtons.get(lastDicePlusActiveButtonIndex).setBackgroundColor(10);
					break;
					
					
				case CONNECT_LOST://필요없을 듯?
					
				    
				    Log.i(TAG, "핸들러 메시지 : 호스트 연결끊김");
					Toast.makeText(getApplicationContext(), ((BluetoothDevice)msg.obj).getAddress() + "is disconnected", Toast.LENGTH_LONG).show();
					break;
					
					
				case CONNECT_FAILED:
					
				    
				    Log.i(TAG, "핸들러 메시지 : 호스트 연결 실패");
					Toast.makeText(getApplicationContext(), ((BluetoothDevice)msg.obj).getAddress() + " : connection fail", Toast.LENGTH_LONG).show();
					break;
					
					
				case CONNECT_OK_TO_GO:
					
				    
				    Log.i(TAG, "OK TO GO 출력!!");
					handler = null;
					DistributedBoardgame.getInstance().getDistributedBoardgameListener().onGameStartable(Mode.CLIENT);//////////!!!코드바꿈
					//Toast.makeText(getApplicationContext(), "OK TO GO", Toast.LENGTH_SHORT).show();
					AssistanceActivity.this.finish();
					
					break;
					
					
				case CONNECT_OK_TO_RESUME:
				    
				    
					Log.i(TAG, "OK TO RESUME 출력!!");
					handler = null;
					DistributedBoardgame.getInstance().getDistributedBoardgameListener().onGameResumable();//////////!!!코드바꿈
					AssistanceActivity.this.finish();
					
					break;
					
					
				case NEW_DEVICE_DISCOVERED:
				    
				    
					Log.i(TAG, "새로운 장치 찾았음");
					//onAdditionalDeviceDiscovered((BluetoothDevice)msg.obj);
					
					break;
					
					
			//액티비티 종료(호스트 입장에서 부를때 이 않에 있는 내용은 맞다. 클라이언트가 부르면 안됨.);
				case COMPLETE_ACTIVITY:
				    
				    
					Log.i(TAG, "종료");
					handler = null;
					Toast.makeText(getApplicationContext(), "게임 시작됨", Toast.LENGTH_LONG).show();
					
					AssistanceActivity.this.finish();
					
					break;
					
				case GAME_IS_STARTABLE:
				    Log.i(TAG, "시작해도 좋음");
				    
				    //startButton.setVisibility(View.VISIBLE);
				    linearStart.addView(startButton);
				    break;
				    
				case GAME_IS_NOT_STARTABLE:
                    Log.i(TAG, "시작하면 안뎀");
                    
                    linearStart.removeView(startButton);
                    break;    

			}
		}
	};
	
	public void onAdditionalDeviceDiscovered(BluetoothDevice device){
		
		Log.i(TAG, "새로운 장치 발견! : " + device.getName());
		
		Button button = new Button(this);
		
		discoveredDevices.add(device);//장치 추가
		playerButtons.add(button);//버튼 추가
		button.setText(device.getName());
		
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View button) {
				Log.d(TAG, "온 클릭 접속중");
				Toast.makeText(AssistanceActivity.this, "연결중..", Toast.LENGTH_LONG).show();
				ClientManager.getInstance().connect(discoveredDevices.get(playerButtons.indexOf(button)));
				//AssistanceActivity.this.finish();
			}
		});
		linear.addView(button);
	}

	public Handler getHandler(){
		return this.handler;
	}

	//helper
	
	private void setPropertyOfLinearLayouts(){
	    LinearLayout.LayoutParams liearParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        
        linear.setOrientation(LinearLayout.VERTICAL);
        linear.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        linearClients.setOrientation(LinearLayout.VERTICAL);
        linearClients.setLayoutParams(liearParams);
        
        linearClientsForce.setOrientation(LinearLayout.VERTICAL);
        linearClientsForce.setLayoutParams(liearParams);
        
        linearDice.setOrientation(LinearLayout.VERTICAL);
        linearDice.setLayoutParams(liearParams);
        
        linearDiceForce.setOrientation(LinearLayout.VERTICAL);
        linearDiceForce.setLayoutParams(liearParams);
        
        linearYuts.setOrientation(LinearLayout.VERTICAL);
        linearYuts.setLayoutParams(liearParams);
        
        linearYutsForce.setOrientation(LinearLayout.VERTICAL);
        linearYutsForce.setLayoutParams(liearParams);
        
        linearStart.setOrientation(LinearLayout.VERTICAL);
        linearStart.setLayoutParams(liearParams);
	}
	
}
