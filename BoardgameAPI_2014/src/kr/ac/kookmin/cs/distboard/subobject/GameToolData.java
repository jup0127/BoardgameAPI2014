package kr.ac.kookmin.cs.distboard.subobject;

import java.io.Serializable;

import android.util.Log;

import kr.ac.kookmin.cs.distboard.Player;

/**
 * 게임 도구에 대한 데이터를 포함하는 클래스입니다.
 * @author jeon2
 */
public class GameToolData implements Serializable{
	
	private static final String TAG = "20083271:GameToolData";
	
	/**
	 * 게임도구의 면 값
	 */
	private int face = 0;
	
	private boolean isMarked = false;
	
	/**
	 * 게임도구의 근원 플레이어 
	 */
	private Player source = null;
		
	/**
	 * 개발자가 호출하지 않습니다.
	 * 공용 생성자입니다.
	 * @param face
	 * @param source
	 */
	public GameToolData(int face, Player source) {
		super();
		this.face = face;
		this.source = source;
	}
	
	/**
	 * 면 값을 얻습니다.
	 * @return 면 값
	 */
	public int getFace() {
		return face;
	}
	
	/**
	 * 인스턴스를 최초로 생성한 근원 플레이어를 얻습니다.
	 * @return 인스턴스를 최초로 생성한 근원 플레이어
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
