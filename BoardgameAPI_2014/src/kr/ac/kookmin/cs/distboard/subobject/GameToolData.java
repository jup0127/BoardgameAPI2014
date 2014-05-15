package kr.ac.kookmin.cs.distboard.subobject;

import java.io.Serializable;

import android.util.Log;

import kr.ac.kookmin.cs.distboard.Player;

/**
 * ���� ������ ���� �����͸� �����ϴ� Ŭ�����Դϴ�.
 * @author jeon2
 */
public class GameToolData implements Serializable{
	
	private static final String TAG = "20083271:GameToolData";
	
	/**
	 * ���ӵ����� �� ��
	 */
	private int face = 0;
	
	private boolean isMarked = false;
	
	/**
	 * ���ӵ����� �ٿ� �÷��̾� 
	 */
	private Player source = null;
		
	/**
	 * �����ڰ� ȣ������ �ʽ��ϴ�.
	 * ���� �������Դϴ�.
	 * @param face
	 * @param source
	 */
	public GameToolData(int face, Player source) {
		super();
		this.face = face;
		this.source = source;
	}
	
	/**
	 * �� ���� ����ϴ�.
	 * @return �� ��
	 */
	public int getFace() {
		return face;
	}
	
	/**
	 * �ν��Ͻ��� ���ʷ� ������ �ٿ� �÷��̾ ����ϴ�.
	 * @return �ν��Ͻ��� ���ʷ� ������ �ٿ� �÷��̾�
	 */
	public Player getSource() {
		return source;
	}
	
	public void setIsMarked(boolean isMarked){
	    this.isMarked = isMarked;
	}
	
	public boolean isMarked(){
	    return isMarked;
	}
	
}
