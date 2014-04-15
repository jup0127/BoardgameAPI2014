package kr.ac.kookmin.cs.distboard.controller;

import kr.ac.kookmin.cs.distboard.GameTool;
import kr.ac.kookmin.cs.distboard.Player;

public interface ParticipantListener {
	
	//specified to participants
	
	public abstract void onPlayerLeave(Player player);
	//has system default routine
	
	public abstract void onGameToolLeave(GameTool gameTool);
	//has system default routine
	
	@Deprecated
	public abstract void onPrepareFail();
	//has system default routine
	
	@Deprecated
	public abstract void onWaitBeBoring(Player player);
	//has system default routine
	
	@Deprecated
	public abstract void onRetreiveFail(GameTool gameTool);
	//has system default routine
	
	public abstract void onPlayerRejoin(Player player);
	
	public abstract void onGameToolRejoin(GameTool gameTool);
	
	public abstract void onNotAvailablePlayer(Player player);
	
	public abstract void onNotAvaliableGameTool(GameTool gameTool);
}
