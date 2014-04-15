package kr.ac.kookmin.cs.distboard.controller;

import bli.ab;
import kr.ac.kookmin.cs.distboard.Player;
import kr.ac.kookmin.cs.distboard.protocol.GameToolData;
import kr.ac.kookmin.cs.distboard.subobject.DicePlusGameTool;
import kr.ac.kookmin.cs.distboard.subobject.YutGameTool;

public interface GameToolListener {
	
	//public abstract void onDicePlusRoll(DicePlusGameTool dicePlusGameTool);
	
	//public abstract void onYutRoll(YutGameTool yutGameTool);
	
	//public abstract void onPlayerDiceRoll(Player player, int[] faces);
	
	//public abstract void onPlayerYutRoll(Player player, int[] faces);
	
	//������ �ǹ��� "���� ����" ������
	
	public abstract void onDiceRoll(Player player, GameToolData rollData);
	
	public abstract void onYutRoll(Player player, GameToolData rollData);
	
	public abstract void onDiceRolls(Player player, GameToolData[] rollDatas);
	
	public abstract void onYutRolls(Player player, GameToolData[] rollDatas);
	
}
