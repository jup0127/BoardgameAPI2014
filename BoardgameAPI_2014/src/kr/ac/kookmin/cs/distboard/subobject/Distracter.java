package kr.ac.kookmin.cs.distboard.subobject;

public final class Distracter {
	
	private String title = null;
	private String[] options = null;
	private boolean[] checkeds = null;
	
	public Distracter(String title, String[] options){
		this.options = options;
		this.checkeds = new boolean[options.length];//initialized with 'false'.
	}
	
	public void check(String option){
		
	}
	
	public void checkAll(){
		
	}
	
	public void unCheck(String option){
		
	}
	
	public void unCheckAll(){
		
	}
	
	public String getCheckedOption(){
		return null;
	}
	
	public String[] getCheckedOptons(){
		return null;
	}
	
}
