package org.pinto.test01;

/**
 * A simple POJO class
*/
public class Test01{
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
