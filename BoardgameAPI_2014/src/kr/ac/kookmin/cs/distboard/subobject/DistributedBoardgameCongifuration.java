package kr.ac.kookmin.cs.distboard.subobject;

public class DistributedBoardgameCongifuration {
	
	private int millisecondTimeout = 0;
	private int minPlayers = 0;
	private int maxPlayers = 0;
	private int exactElectricGameToolDicePlus = 0;
	private int exactElectricGameToolYut = 0;
	
	public DistributedBoardgameCongifuration(
			int millisecondTimeout,
			int minPlayers, 
			int maxPlayers, 
			int exactElectricGameToolDicePlus,
			int exactElectricGameToolYut) {
		super();
		this.millisecondTimeout = millisecondTimeout;
		this.minPlayers = minPlayers;
		this.maxPlayers = maxPlayers;
		this.exactElectricGameToolDicePlus = exactElectricGameToolDicePlus;
		this.exactElectricGameToolYut = exactElectricGameToolYut;
	}
	
	public static DistributedBoardgameCongifuration getInstance(
			int millisecondTimeout,
			int minPlayers, 
			int maxPlayers, 
			int exactElectricGameToolDicePlus,
			int exactElectricGameToolYut){
		return null;
	}
	
	public static DistributedBoardgameCongifuration fromString(){
		return null;
	}
	
	//getters
	
	public int getMillisecondTimeout() {
		return millisecondTimeout;
	}
	
	public int getMinPlayers() {
		return minPlayers;
	}
	
	public int getMaxPlayers() {
		return maxPlayers;
	}
	
	public int getExactElectricGameToolDicePlus() {
		return exactElectricGameToolDicePlus;
	}
	
	public int getExactElectricGameToolYut() {
		return exactElectricGameToolYut;
	}
	
	

}
