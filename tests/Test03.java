package org.pinto.test03;

/**
 * A simple POJO class with its parent
*/
public class Test03{
	public String getSubName(){
		return "Child! " + getName();
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

