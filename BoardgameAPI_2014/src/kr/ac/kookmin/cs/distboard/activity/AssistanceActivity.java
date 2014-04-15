package kr.ac.kookmin.cs.distboard.activity;


import java.util.ArrayList;
import java.util.HashMap;

import kr.ac.kookmin.cs.distboard.DistributedBoardgame;
import kr.ac.kookmin.cs.distboard.Mediator;
import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import kr.ac.kookmin.cs.distboard.protocol.Request;
import kr.ac.kookmin.cs.distboard.protocol.RequestReplyManager;
import kr.ac.kookmin.cs.distboard.system.BluetoothManager;
import kr.ac.kookmin.cs.distboard.system.DicePlusManager;
import kr.ac.kookmin.cs.distboard.system.ElectricYutManager;
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
	
	
	
	//��Ƽ��Ƽ ��� ���ӿ� ����
	public static final String ACTIVITY_MODE = "activity_mode";
	//������ ������
	public static final int HOST_CLIENT_SELECT_MODE = 0;
	public static final int HOST_PREPARE_MODE = 1;
	public static final int CLIENT_JOIN_MODE = 2;//Ŭ���̾�Ʈ ���� ��Ƽ��Ƽ
	

	//ȣ��Ʈ �غ��� : �ּ� �÷��̾� ���ӿ� ����
	public static final String MIN_PLAYERS = "min_hosts";//������ ������ ����

	//ȣ��Ʈ �غ��� : �ִ� �÷��̾� ���ӿ� ����
	public static final String MAX_PLAYERS = "max_hosts";//������ ������ ����

	//ȣ��Ʈ �غ��� : ��Ȯ�� ���̽� �÷��� ���ӿ� ����
	public static final String EXACT_DICEPLUSES = "exact_dicepluses";//������ ������ ����

	//ȣ��Ʈ �غ��� : ��Ȯ�� �� ���ӿ� ����
	public static final String EXACT_YUTS = "exact_yuts";//������ ������ ����
	
	//Ŭ���̾�Ʈ �շ� ��� : Ž���� ��ġ�鿡 ����
	//public static final String DISCOVERED_DEVICES = "discovered_devices";//������ ������ ������� ��ġ ���

	
	
	//���⼭���� �ڵ鷯 ����(�񵿱� ���� ���)
	
	//�ڵ鷯�� what
	
	
	
	public static final int ESTABLISH_COMPLETE = 0;
	public static final int ESTABLISH_PLAYER_FORCE_COMPLETE = 1;
	public static final int ESTABLISH_TIMEOUT = 2;
	public static final int ESTABLISH_PLAYER_CONNECTED = 3;//obj : Ŭ���̾�Ʈ ��ġ
	public static final int ESTABLISH_PLAYER_DISCONNECTED = 4;//obj : Ŭ���̾�Ʈ ��ġ
	
	public static final int ESTALBISH_YUT_COMPLETE = 5;
	public static final int ESTABLISH_YUT_FORCE_COMPLETE = 6;
	public static final int ESTABLISH_YUT_CONNECTED = 7;//obj : �� ��ġ
	public static final int ESTABLISH_YUT_DISCONNECTED = 8;//obj : �� ��ġ
	
	public static final int ESTABLISH_DICEPLUS_COMPELETE = 9;
	public static final int ESTABLISH_DICEPLUS_FORCE_COMPLETE = 10;
	public static final int ESTABLISH_DICEPLUS_CONNECTED = 11;//obj : Die��ü
	public static final int ESTABLISH_DICEPLUS_DISCONNECTED = 12;//obj : Die��ü

	public static final int CONNECT_COMPLETE = 13;//obj : ȣ��Ʈ ��ġ
	public static final int CONNECT_LOST = 14;//obj : ȣ��Ʈ ��ġ
	public static final int CONNECT_FAILED = 15;//obj : ȣ��Ʈ ��ġ
	
	public static final int RECONNECT_COMPLETE = 16;//obj : ��ġ
	public static final int RECONNECT_FAILED = 17;//?????????
	
	public static final int CONNECT_OK_TO_GO = 18;//ȣ��Ʈ�� ����ȭ�� �Ѿ�� ���� ��ȣ
	public static final int CONNECT_OK_TO_RESUME = 19;//ȣ��Ʈ�� ����ȭ�� �Ѿ�� ���� ��ȣ
	
	public static final int NEW_DEVICE_DISCOVERED = 20;//���ο� ��ġ �߰�!
	
	public static final int COMPLETE_ACTIVITY = 21;
	
	
	//�ν��Ͻ� ����
	private int activityMode = -1;
	
	//private String[] discoveredDeviceAddresses = null;
	//BluetoothDevice[] discoveredDevices = null;
	ArrayList<BluetoothDevice> discoveredDevices = new ArrayList<BluetoothDevice>();
	
	
	//���̾ƿ�
	LinearLayout linear = null;
	
	
	
	//���̾ƿ�:�ӽ� �ν��Ͻ� ����
	ArrayList<Button> dicePlusButtons = new ArrayList<Button>();
	ArrayList<Button> playerButtons = new ArrayList<Button>();
	ArrayList<Button> yutButtons = new ArrayList<Button>();
	
	HashMap<String, Button> commonButtonsMap = new HashMap<String, Button>();//��ư�� �ش� ��ư �ĺ���! <���ּ�,��ư>, ����� �ֵ��� ���⿡ �ĺ���
	
	Button forcePlayerButton = null;
	Button forceDicePlusButton = null;
	Button forceElectricYutButton = null;
	
	Button startButton = null;
	//TextView tempTextView;//�������̳� ����Ϸ� ǥ���Ҷ�
	
	//int lastPlayerActiveButtonIndex++ = 0;
	
	//�÷��̾� �� ������
	int currentConnectedPlayers = 0;
	
	//���� ����(������ �ε���) ������
	int lastDicePlusActiveButtonIndex = 0;
	int lastYutActiveButtonIndex = 0;
	
	//�ٷ� �Ѿ�� ����Ʈ
	Intent currentIntent = null;
	
	//�ؽ�Ʈ �� ����Ʈ
	//ArrayList<TextView> tvs = new ArrayList<TextView>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "��Ƽ��Ƽ�� ����������ϴ�.");
		
		//CommunicationStateManager.getInstance().initialize(handler);//handler�� ���縦 �� �ʿ䰡 ����!
		Mediator.getInstance().setHandler(handler);
		
		
		///������� ���� ����////
		
		
		if(BluetoothAdapter.getDefaultAdapter() == null){
			Toast.makeText(this, "��������� ����� �� �����ϴ�.", Toast.LENGTH_LONG).show();
		}
		
		
		
		
		
		////////////////////////
		
		
		currentIntent = getIntent();
		
		int activityMode = (int) currentIntent.getExtras().get(ACTIVITY_MODE);

		switch(activityMode){
		
		case HOST_CLIENT_SELECT_MODE:
			Log.i(TAG, "���� ȣ��Ʈ Ŭ���̾�Ʈ ���� ���");
			setHostClientSelectMode();
			break;
		case HOST_PREPARE_MODE:
			Log.i(TAG, "���� ȣ��Ʈ �غ� ���");
			setHostPrepareMode();
			break;
		case CLIENT_JOIN_MODE:
			Log.i(TAG, "���� Ŭ���̾�Ʈ �շ� ���");
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
		 * ȣ��Ʈ ��� ��ư
		 */
		Button hostModeButton = new Button(this);
		hostModeButton.setLayoutParams(buttonProperty);
		hostModeButton.setText("ȣ��Ʈ ���");
		hostModeButton.setBackgroundColor(Color.MAGENTA);
		hostModeButton.setTextSize(14);
		hostModeButton.setPadding(5, 5, 5, 5);
		
		hostModeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View button) {
				Log.d(TAG, "�� Ŭ�� ȣ��Ʈ ���");
                Mediator.getInstance().completeSelectHostClient(Mode.HOST);
                AssistanceActivity.this.finish();
			}
		});

		/**
		 * Ŭ���̾�Ʈ ��� ��ư
		 */
		Button clientModeButton = new Button(this);
		
		clientModeButton.setText("Ŭ���̾�Ʈ ���");
		clientModeButton.setBackgroundColor(Color.CYAN);
		
		clientModeButton.setLayoutParams(buttonProperty);
		clientModeButton.setTextSize(14);
		clientModeButton.setPadding(5, 5, 5, 5);
		clientModeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View button) {
				Log.d(TAG, "�� Ŭ�� Ŭ���̾�Ʈ ���");
				Mediator.getInstance().completeSelectHostClient(Mode.CLIENT);
				AssistanceActivity.this.finish();
				
			}
		});

		/**
		 * �� ���� ��ư ����
		 */
		linear.addView(hostModeButton);
		linear.addView(clientModeButton);

		setContentView(linear);
	}
	
	private void setHostPrepareMode() {
		activityMode = HOST_PREPARE_MODE;
		
		linear = new LinearLayout(this);

		LinearLayout.LayoutParams liearParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		LinearLayout.LayoutParams buttonProperty = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		
		linear.setOrientation(LinearLayout.VERTICAL);
		linear.setLayoutParams(liearParams);
		
		
		Log.i(TAG, "ȣ��Ʈ �غ� ��� ����");
		if(currentIntent == null)
			Log.e(TAG, "���� ����Ʈ�� null �Դϴ�.");
		
		int minPlayers = (int) currentIntent.getExtras().get(MIN_PLAYERS);
		int maxPlayers = (int) currentIntent.getExtras().get(MAX_PLAYERS);
		int exactDicePluses = (int) currentIntent.getExtras().get(EXACT_DICEPLUSES);
		int exactYuts = (int) currentIntent.getExtras().get(EXACT_YUTS);
		

		
		for(int i = 0 ; i < maxPlayers ; i++){
			Button button = new Button(this);
			button.setLayoutParams(buttonProperty);
			
			button.setText(DEFAULT_PLAYER_NAME);
			button.setBackgroundColor(Color.GRAY);
			playerButtons.add(button);
			linear.addView(button);
		}
		
		
        forcePlayerButton = new Button(this);
        forcePlayerButton.setLayoutParams(buttonProperty);
        forcePlayerButton.setText(DEFAULT_FORCE_PLAYERS_NAME);
        forcePlayerButton.setBackgroundColor(Color.BLUE);
        if(Mediator.getInstance().getMinPlayers() == 0 && Mediator.getInstance().getMaxPlayers() != 0){
            forcePlayerButton.setVisibility(View.VISIBLE);
        }else{
            forcePlayerButton.setVisibility(View.INVISIBLE);
        }
        forcePlayerButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View button) {
                Log.d(TAG, "�� Ŭ�� �÷��̾� ��������");

                if (DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE) {
                    BluetoothManager.getInstance().forceClientEstablishment();
                }
                // AssistanceActivity.this.finish();
            }
        });
            
        linear.addView(forcePlayerButton);
        

		for(int i = 0 ; i < exactDicePluses ; i++){
			Button button = new Button(this);
			button.setLayoutParams(buttonProperty);
			
			button.setText(DEFAULT_DICE_PLUS_NAME);
			button.setBackgroundColor(Color.GRAY);
			dicePlusButtons.add(button);
			linear.addView(button);
		}
		
		if(exactDicePluses > 0){
		    
			forceDicePlusButton = new Button(this);
			forceDicePlusButton.setLayoutParams(buttonProperty);
			forceDicePlusButton.setText(DEFAULT_WITHOUT_DICE_PLUS_NAME);
			forceDicePlusButton.setBackgroundColor(Color.BLUE);
			forceDicePlusButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View button) {
					Log.d(TAG, "�� Ŭ�� Dice plus ����");
					Toast.makeText(AssistanceActivity.this, "Dice Plus���� �����մϴ�...", Toast.LENGTH_LONG).show();
					//tempTextView.setText("������...");
					//setContentView(tempTextView);
					
					if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
						DicePlusManager.getInstance().discardDicePluses();
					}
					//AssistanceActivity.this.finish();
				}
			});
			
			linear.addView(forceDicePlusButton);
			
			
		}

			
		for(int i = 0 ; i < exactYuts ; i++){
			Button button = new Button(this);
			button.setLayoutParams(buttonProperty);
			
			button.setText(DEFAULT_ELECTRIC_YUT_NAME);
			forceDicePlusButton.setBackgroundColor(Color.BLUE);
			yutButtons.add(button);
			linear.addView(button);
		}
		
		if(exactYuts > 0){
			forceElectricYutButton = new Button(this);
			forceElectricYutButton.setLayoutParams(buttonProperty);
			forceElectricYutButton.setText(DEFAULT_WITHOUT_ELECTRIC_YUT_NAME);
			forceElectricYutButton.setBackgroundColor(Color.BLUE);
			forceElectricYutButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View button) {
					Log.d(TAG, "�� Ŭ�� Electric yut ����");
					Toast.makeText(AssistanceActivity.this, "���� �� ���� �����մϴ�...", Toast.LENGTH_LONG).show();
					//tempTextView.setText("������...");
					//setContentView(tempTextView);
					
					if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
						ElectricYutManager.getInstance().discardYuts();
					}
					//AssistanceActivity.this.finish();
				}
			});
			
			linear.addView(forceElectricYutButton);
		}
		

		startButton = new Button(this);
		startButton.setLayoutParams(buttonProperty);
		startButton.setText("Start");
		startButton.setBackgroundColor(Color.YELLOW);
		startButton.setVisibility(View.INVISIBLE);
		startButton.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View button) {
				Log.d(TAG, "�� Ŭ�� ���ӽ���");

				if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
					ElectricYutManager.getInstance().discardYuts();
				}
				//AssistanceActivity.this.finish();
			}
		});
		
		setContentView(linear);
	}
	
	private void setClientJoinMode() {
		
		Log.i(TAG, "Ŭ���̾�Ʈ ���� ��� �޼��� ����");
		activityMode = CLIENT_JOIN_MODE;
		DistributedBoardgame.getInstance().setState(DistributedBoardgame.CLIENT_JOIN_MODE);
		linear = new LinearLayout(this);
		
		LinearLayout.LayoutParams liearParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		LinearLayout.LayoutParams buttonProperty = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		
		linear.setOrientation(LinearLayout.VERTICAL);
		linear.setLayoutParams(liearParams);
		linear.setVerticalScrollBarEnabled(true);
		
		
		
		Log.d(TAG, "������� �Ŵ������� ��ġ �˻� ��û ����");
		discoveredDevices = ArrayListConverter.bluetoothDeviceArrayToArrayList(BluetoothManager.getInstance().getDiscoveredDevices());//������� �Ŵ��� ���� �ʱ�ȭ ����
		
		Log.d(TAG, "������� �Ŵ������� ��ġ �˻� ��û �Ϸ�");
		for(int i = 0 ; i < discoveredDevices.size() ; i++){
			
			Button button = new Button(this);
			
			button.setGravity(Gravity.CENTER);
			button.setLayoutParams(buttonProperty);
			
			playerButtons.add(button);//��ư �߰�
			button.setText(discoveredDevices.get(i).getName());
			
			button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View button) {
					Log.d(TAG, "�� Ŭ�� ������");
					Toast.makeText(AssistanceActivity.this, "������..", Toast.LENGTH_LONG).show();
					//tempTextView.setText("������...");
					//setContentView(tempTextView);
					
					BluetoothManager.getInstance().connect(discoveredDevices.get(playerButtons.indexOf(button)));
					//AssistanceActivity.this.finish();
				}
			});
			linear.addView(button);
		}
		setContentView(linear);
	}
	
	//�ڵ鷯
	
	private Handler handler = new Handler(){ //����(UI)�������� �ڵ鷯
		public void handleMessage(Message msg){
			Log.i(TAG, "�ڵ鷯 �޽��� ����");
			switch(msg.what){
			
			//ȣ��Ʈ �غ� ���
				case ESTABLISH_COMPLETE:
					Log.i(TAG, "�ڵ鷯 �޽��� : ��� ������ �Ϸ��");
					Toast.makeText(getApplicationContext(), "prepare complete", Toast.LENGTH_LONG).show();
					break;
				case ESTABLISH_PLAYER_FORCE_COMPLETE:
                    
                    Log.i(TAG, "�ڵ鷯 �޽��� : �÷��̾� ���� ���� �Ϸ�");
                    //�������� ����� ���·� �ٲ�
                    for(int i = 0 ; i < playerButtons.size() ; i++){
                        if(commonButtonsMap.containsValue(playerButtons.get(i)) == false){//������� ���� �ֵ��� ���⿡�������ϱ�
                            playerButtons.get(i).setVisibility(View.INVISIBLE);
                        }
                    }
                    forcePlayerButton.setVisibility(View.INVISIBLE);
                    
                    break;	
					
				case ESTABLISH_TIMEOUT:
				    
				    
					Log.i(TAG, "�ڵ鷯 �޽��� : ������ Ÿ�Ӿƿ�");
					Toast.makeText(getApplicationContext(), "Ÿ�Ӿƿ� �Ǿ����ϴ�!!", Toast.LENGTH_LONG).show();
					
					break;
					
					
				case ESTABLISH_PLAYER_CONNECTED:
				    
				    
					Log.i(TAG, "�ڵ鷯 �޽��� : �÷��̾� �����");
					Toast.makeText(getApplicationContext(), ((BluetoothDevice)msg.obj).getAddress() + "is connected", Toast.LENGTH_SHORT).show();
					
					for(int i = 0 ; i < playerButtons.size() ; i++){//��� ��ư�� �˻��ϴµ�,
							
						if(commonButtonsMap.containsValue(playerButtons.get(i)) == false){//���� ��ư�� �ؽ��ʿ� ������(���������� ������)
							commonButtonsMap.put(((BluetoothDevice)msg.obj).getAddress(), playerButtons.get(i));
							playerButtons.get(i).setBackgroundColor(Color.GREEN);
							playerButtons.get(i).setText(((BluetoothDevice)msg.obj).getName());
							currentConnectedPlayers++;
							break;
						}
					}
					
					if(Mediator.getInstance().getMinPlayers() <= currentConnectedPlayers && Mediator.getInstance().getMaxPlayers() != currentConnectedPlayers){
					    forcePlayerButton.setVisibility(View.VISIBLE);
					}

					break;
					
					
				case ESTABLISH_PLAYER_DISCONNECTED:
				    
				    
					Log.i(TAG, "�ڵ鷯 �޽��� : �÷��̾� ���� ����");
					Toast.makeText(getApplicationContext(), ((BluetoothDevice)msg.obj).getAddress() + "is disconnected", Toast.LENGTH_LONG).show();
					commonButtonsMap.get(((BluetoothDevice)msg.obj).getAddress()).setBackgroundColor(Color.GRAY);
					commonButtonsMap.get(((BluetoothDevice)msg.obj).getAddress()).setText(DEFAULT_PLAYER_NAME);
					commonButtonsMap.remove(((BluetoothDevice)msg.obj).getAddress());
					currentConnectedPlayers--;
					break;
					
					
				case ESTALBISH_YUT_COMPLETE:
				    
				    
					Log.i(TAG, "�ڵ鷯 �޽��� : ������ ���� �Ϸ�");
					break;
					
					
				case ESTABLISH_YUT_FORCE_COMPLETE:
                    
				    
                    Log.i(TAG, "�ڵ鷯 �޽��� : �� ���� ���� �Ϸ�");
                    
                    //�������� ����� ���·� �ٲ�
                    for(int i = 0 ; i < yutButtons.size() ; i++){
                        yutButtons.get(i).setText(DEFAULT_VIRTUAL_YUT_NAME);
                        yutButtons.get(i).setBackgroundColor(Color.GREEN);
                    }
                    forceElectricYutButton.setVisibility(View.INVISIBLE);
                    
                    break;	
                    
                    
				case ESTABLISH_YUT_CONNECTED:
				    
				    
					Log.i(TAG, "�ڵ鷯 �޽��� : ���� �� �����");
					Toast.makeText(getApplicationContext(), ((BluetoothDevice)msg.obj).getAddress() + "is connected", Toast.LENGTH_SHORT).show();
					yutButtons.get(lastYutActiveButtonIndex++).setBackgroundColor(Color.GREEN);
					if(forceElectricYutButton != null)
						forceElectricYutButton.setVisibility(View.INVISIBLE);
					break;
					
					
				case ESTABLISH_YUT_DISCONNECTED:
				    
				    
					Log.i(TAG, "�ڵ鷯 �޽��� : ���� �� ���� ����");
					Toast.makeText(getApplicationContext(), ((BluetoothDevice)msg.obj).getAddress() + "is disconnected", Toast.LENGTH_LONG).show();
					yutButtons.get(lastYutActiveButtonIndex--).setBackgroundColor(Color.GRAY);
					break;
					
					
				case ESTABLISH_DICEPLUS_COMPELETE:
				    
				    
					Log.i(TAG, "�ڵ鷯 �޽��� : DICE+ ���� �Ϸ�");
					break;
					
					
				case ESTABLISH_DICEPLUS_FORCE_COMPLETE:
				    
					
					Log.i(TAG, "�ڵ鷯 �޽��� : DICE ���� ���� �Ϸ�");
					
					//�������� ����� ���·� �ٲ�
					for(int i = 0 ; i < dicePlusButtons.size() ; i++){
						dicePlusButtons.get(i).setText(DEFAULT_VIRTUAL_DICE_NAME);
						dicePlusButtons.get(i).setBackgroundColor(Color.GREEN);
					}
					forceDicePlusButton.setVisibility(View.INVISIBLE);
					break;
					
					
				case ESTABLISH_DICEPLUS_CONNECTED:
				    
				    
					Log.i(TAG, "�ڵ鷯 �޽��� : DICE+ �����");
					Toast.makeText(getApplicationContext(), ((Die)msg.obj).getAddress() + "is connected", Toast.LENGTH_SHORT).show();
					dicePlusButtons.get(lastDicePlusActiveButtonIndex++).setBackgroundColor(Color.GREEN);
					
					if(forceDicePlusButton != null)
						forceDicePlusButton.setVisibility(View.INVISIBLE);
					
					break;
					
					
				case ESTABLISH_DICEPLUS_DISCONNECTED:
				    
				    
					Log.i(TAG, "�ڵ鷯 �޽��� : DICE+ ���� ����");
					Toast.makeText(getApplicationContext(), ((Die)msg.obj).getAddress() + "is disconnected", Toast.LENGTH_LONG).show();
					dicePlusButtons.get(lastDicePlusActiveButtonIndex--).setBackgroundColor(Color.GRAY);
					
					break;
					
					
			//Ŭ���̾�Ʈ ���� ���
				case CONNECT_COMPLETE:
				    
				    
					Log.i(TAG, "�ڵ鷯 �޽��� : ȣ��Ʈ �����");
					Toast.makeText(getApplicationContext(), ((BluetoothDevice)msg.obj).getAddress() + "is connected", Toast.LENGTH_SHORT).show();
					//tempTextView.setText("����Ϸ�! �ٸ� �÷��̾� �����...");
					//playerButtons.get(lastDicePlusActiveButtonIndex).setBackgroundColor(10);
					break;
					
					
				case CONNECT_LOST://�ʿ���� ��?
					
				    
				    Log.i(TAG, "�ڵ鷯 �޽��� : ȣ��Ʈ �������");
					Toast.makeText(getApplicationContext(), ((BluetoothDevice)msg.obj).getAddress() + "is disconnected", Toast.LENGTH_LONG).show();
					break;
					
					
				case CONNECT_FAILED:
					
				    
				    Log.i(TAG, "�ڵ鷯 �޽��� : ȣ��Ʈ ���� ����");
					Toast.makeText(getApplicationContext(), ((BluetoothDevice)msg.obj).getAddress() + " : connection fail", Toast.LENGTH_LONG).show();
					break;
					
					
				case CONNECT_OK_TO_GO:
					
				    
				    Log.i(TAG, "OK TO GO ���!!");
					handler = null;
					DistributedBoardgame.getInstance().getDistributedBoardgameListener().onGameStartable(Mode.CLIENT);//////////!!!�ڵ�ٲ�
					//Toast.makeText(getApplicationContext(), "OK TO GO", Toast.LENGTH_SHORT).show();
					AssistanceActivity.this.finish();
					
					break;
					
					
				case CONNECT_OK_TO_RESUME:
				    
				    
					Log.i(TAG, "OK TO RESUME ���!!");
					handler = null;
					DistributedBoardgame.getInstance().getDistributedBoardgameListener().onGameResumable();//////////!!!�ڵ�ٲ�
					AssistanceActivity.this.finish();
					
					break;
					
					
				case NEW_DEVICE_DISCOVERED:
				    
				    
					Log.i(TAG, "���ο� ��ġ ã����");
					//onAdditionalDeviceDiscovered((BluetoothDevice)msg.obj);
					
					break;
					
					
			//��Ƽ��Ƽ ����(ȣ��Ʈ ���忡�� �θ��� �� �ʿ� �ִ� ������ �´�. Ŭ���̾�Ʈ�� �θ��� �ȵ�.);
				case COMPLETE_ACTIVITY:
				    
				    
					Log.i(TAG, "����");
					handler = null;
					Toast.makeText(getApplicationContext(), "���� ���۵�", Toast.LENGTH_LONG).show();
					
					AssistanceActivity.this.finish();
					
					break;

			}
		}
	};
	
	public void onAdditionalDeviceDiscovered(BluetoothDevice device){
		
		Log.i(TAG, "���ο� ��ġ �߰�! : " + device.getName());
		
		Button button = new Button(this);
		
		discoveredDevices.add(device);//��ġ �߰�
		playerButtons.add(button);//��ư �߰�
		button.setText(device.getName());
		
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View button) {
				Log.d(TAG, "�� Ŭ�� ������");
				Toast.makeText(AssistanceActivity.this, "������..", Toast.LENGTH_LONG).show();
				BluetoothManager.getInstance().connect(discoveredDevices.get(playerButtons.indexOf(button)));
				//AssistanceActivity.this.finish();
			}
		});
		linear.addView(button);
	}
	
	/*public void onStart(){
		super.onStart();
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if(adapter.isEnabled() == false){
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
	}*/
	
	public Handler getHandler(){
		return this.handler;
	}
}
