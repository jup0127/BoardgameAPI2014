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
	
	private boolean atLeastOneEstablishTimeoutFlag = false;//�ּ� �ϳ��� Ÿ�Ӿƿ��Ǹ� ��üŸ�ӾƟS�̴�!
	
	
	public static CandidateManager getInstance(){
		
		return instance;
		
	}
	
	//��ġ����
	
	public synchronized void nominateDice(Die[] dice){
		Log.d(TAG, "DICE+ ��̳���Ʈ");
		
		if(instance.initialized == false){
			Log.i(TAG, "�ʱ�ȭ���� �ʾҽ��ϴ�.");
			return;
		}
		
		//debug code
		for(int i = 0 ; i < dice.length ; i++){
			Log.d(TAG, "��̳���Ʈ�� �ֻ��� : " + dice[i].getAddress());
		}
		
		//�ĺ�ȭ
		this.dice = dice;
		//��� �ĺ��� null�� �ƴ���, �� ������ Lost ���� �ʾҴ��� �˻�
		checkAllCandidates();
	}
	
	public synchronized void nominateClientDevices(BluetoothDevice[] clientDevices){
		Log.d(TAG, "Ŭ���̾�Ʈ ��ġ ��̳���Ʈ");
		if(instance.initialized == false){
			Log.i(TAG, "�ʱ�ȭ���� �ʾҽ��ϴ�.");
			return;
		}
		
		//debug code
		for(int i = 0 ; i < clientDevices.length ; i++){
			Log.d(TAG, "��̳���Ʈ�� Ŭ���̾�Ʈ : " + clientDevices[i].getName());
		}
		
		//�ĺ�ȭ
		this.clientDevices = clientDevices;
		//��� �ĺ��� null�� �ƴ���, �� ������ Lost ���� �ʾҴ��� �˻�
		checkAllCandidates();
	}
	
	public synchronized void nominateYutDevices(BluetoothDevice[] yutDevices){
		Log.d(TAG, "������ ��ġ ��̳���Ʈ");
		
		if(instance.initialized == false){
			Log.i(TAG, "�ʱ�ȭ���� �ʾҽ��ϴ�.");
			return;
		}
		
		//debug code
		for(int i = 0 ; i < yutDevices.length ; i++){
			Log.d(TAG, "��̳���Ʈ�� ������ : " + yutDevices[i].getName());
		}
		
		//�ĺ�ȭ
		this.yutDevices = yutDevices;
		
		//��� �ĺ��� null�� �ƴ���, �� ������ Lost ���� �ʾҴ��� �˻�
		checkAllCandidates();
	}
	
	
	
	//�Ϻ��ϰ� �ĺ�ȭ�Ǿ��ٸ� ���� �ƴϸ� �ش� �κа�ħ
	private synchronized void checkAllCandidates(){
		Log.d(TAG, "�ĺ��� üũ");
		if(instance.initialized == false){
			Log.i(TAG, "�ʱ�ȭ���� �ʾҽ��ϴ�.");
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
			
			//������������� �ĺ��� �Ϻ�
			
			Log.d(TAG, "�ĺ����� �Ϻ��մϴ�.");
			
			Log.i(TAG, "���ۿ� ���� ���� ��� �ݿ� : Ŭ=" + clientDevices.length + "��=" + dice.length + "��=" + yutDevices.length );
			
			SubjectDeviceMapper.getInstance().setExactPlayers(clientDevices.length);
			SubjectDeviceMapper.getInstance().setExactDicePluses(dice.length);
			SubjectDeviceMapper.getInstance().setExactYutGameTools(yutDevices.length);
			
			if(Mediator.getInstance().getMode() == Mode.HOST){
				Log.i(TAG, "�ĺ��� ȣ��Ʈ�� ����");
				CommunicationStateManager.getInstance().onEstablishComplete(clientDevices, yutDevices, dice);
			}else{
				Log.i(TAG, "�ĺ��� Ŭ���̾�Ʈ�� ����");
				CommunicationStateManager.getInstance().onConnectionComplete(clientDevices[0]);//clientDevices�� ��� host
			}
		}
		
		return;
	}

	//Ÿ�Ӿƿ����� - �ش� ������ �����ߴٴ� ���� �ĺ�ȭ��
	
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
