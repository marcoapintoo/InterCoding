package org.pinto.test06;

/**
 * A little more complex POJO class
*/
public class Test06 extends Test03Parent{
	@Override
	public String getName(){
		return "Child! " + super.getName();
	}
}

class Test03Parent{
	private String name;
	public String getName(){
		return name;
	}
	public void setName(String name){
		if(name==null){
			name = "";
		}
		this.name = name;
	}
}

