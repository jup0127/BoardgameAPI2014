package kr.ac.kookmin.cs.distboard.subobject;

import kr.ac.kookmin.cs.distboard.GameTool;

public final class DicePlusGameTool extends GameTool {

	private int face = -1;
	
	public int getFace(){
		return face;
	}
	
	public void setFace(int face){
		this.face = face;
	}
	
	public int getRolledFace(){
		return 0;
	}
	
	/**
	 * etc
	 */
	
	public void setColor(int r, int g, int b){
		
	}
}
