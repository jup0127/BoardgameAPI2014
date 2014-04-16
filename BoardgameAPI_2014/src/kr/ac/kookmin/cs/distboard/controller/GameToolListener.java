package kr.ac.kookmin.cs.distboard.controller;

import bli.ab;
import kr.ac.kookmin.cs.distboard.Player;
import kr.ac.kookmin.cs.distboard.subobject.DicePlusGameTool;
import kr.ac.kookmin.cs.distboard.subobject.GameToolData;
import kr.ac.kookmin.cs.distboard.subobject.YutGameTool;

public interface GameToolListener {

	public abstract void onDiceRoll(Player player, GameToolData rollData);
	
	public abstract void onYutRoll(Player player, GameToolData rollData);
	
	public abstract void onDiceRolls(Player player, GameToolData[] rollDatas);
	
	public abstract void onYutRolls(Player player, GameToolData[] rollDatas);
	
}
