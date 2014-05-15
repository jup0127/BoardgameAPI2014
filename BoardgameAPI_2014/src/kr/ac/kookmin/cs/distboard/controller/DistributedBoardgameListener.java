package kr.ac.kookmin.cs.distboard.controller;

import kr.ac.kookmin.cs.distboard.enumeration.Mode;

public interface DistributedBoardgameListener {
	
	//호스트 또는 클라이언트
	public abstract void onGameStartable(Mode mode);
	
	//Client
	public abstract void onGameResumable();
}
