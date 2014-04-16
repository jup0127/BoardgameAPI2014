package kr.ac.kookmin.cs.distboard.subobject;

import java.io.Serializable;

import android.util.Log;

import kr.ac.kookmin.cs.distboard.Player;

/**
 * 
 * @author jeon2
 *
 */
public class GameToolData implements Serializable{
	
	private static final String TAG = "20083271:GameToolData";
	
	private int face = 0;

	private Player source = null;
		
	public GameToolData(int face, Player source) {
		super();
		this.face = face;
		this.source = source;
	}
	
	
	public int getFace() {
		return face;
	}
	
	public Player getSource() {
		return source;
	}
	
}
