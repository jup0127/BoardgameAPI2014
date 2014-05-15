package kr.ac.kookmin.cs.distboard.protocol;

import java.io.Serializable;

public class Reply implements Serializable{

	public static final int CHOICE = 0;
	public static final int GIVE = 1;
	public static final int TAKE = 2;
	public static final int ROLL_DICE_RESULT = 3;
	public static final int ROLL_YUTS_RESULT = 4;
	public static final int GET_OUT = 5;
	public static final int GIVE_SITUATION = 6;
	
	public int code = -1;
	public Object content = null;
	
	public Reply(int code, Object content){
		this.code = code;
		this.content = content;
	}
	
	public static Reply getRequest(int code, Object content){
		return new Reply(code, content);
	}
	
}
