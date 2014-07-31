package org.pinto.test02;

/**
 * A little more complex POJO class
*/
public class Test02{
	private static int staticValue = 0;
	private String name;
	private Integer privateValue;
	Test02(){
		staticValue++;
		privateValue = staticValue;
	}
	public Integer value(){
		return privateValue;
	}
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
