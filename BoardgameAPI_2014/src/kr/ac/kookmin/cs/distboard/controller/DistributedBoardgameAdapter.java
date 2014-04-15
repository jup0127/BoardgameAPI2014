package kr.ac.kookmin.cs.distboard.controller;

import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import android.util.Log;

public class DistributedBoardgameAdapter implements DistributedBoardgameListener{
	
	private static final String TAG = "DistributedBoardgameAdapter";
	
	@Override
	public void onGameStartable(Mode mode) {
		Log.w(TAG, "onGameStartable 리스너가 등록되지 않았네요.");
	}

	@Override
	public void onGameResumable() {
		Log.w(TAG, "onGameResumable 리스너가 등록되지 않았네요.");
	}
}
