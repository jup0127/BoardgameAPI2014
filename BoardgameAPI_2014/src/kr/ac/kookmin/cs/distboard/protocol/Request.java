package kr.ac.kookmin.cs.distboard.protocol;

import java.io.Serializable;

public class Request implements Serializable{
	
    @Deprecated
	public static final int CHOICE = 0;
	
	@Deprecated
	public static final int GIVE = 1;
	
	@Deprecated
	public static final int TAKE = 2;
	
	@Deprecated
	public static final int ROLL_DICE = 3;
	
	@Deprecated
	public static final int ROLL_YUT = 4;
	
	public static final int APPEAR_DICE_EMULATOR = 5;
	public static final int APPEAR_YUT_EMULATOR = 6;
	
	
	public static final int DISAPPEAR_EMULATOR = 7;
	
	@Deprecated
	public static final int DISSAPEAR_YUT_EMULATOR = 8;
	
	@Deprecated
	public static final int GET_OUT = 9;//no contents, no reply
	public static final int GIVE_OBJECT = 10;
	public static final int GIVE_SITUATION = 11;
	
	public static final int OK_TO_GO = 12; //no contents
	public static final int OK_TO_RESUME = 13;//no contents
	
	public static final int SET_NUM_OF_DICE = 14;
	public static final int SET_NUM_OF_YUT = 15;
	public static final int SET_NUM_OF_MARKED_YUT = 16;
	
	public int code = -1;
	public Object content = null;
	
	public Request(int code, Object content){
		this.code = code;
		this.content = content;
	}
	
	public static Request getRequest(int code, Object content){
		return new Request(code, content);
	}
	
}
