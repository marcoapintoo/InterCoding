package org.pinto.test07;

/**
 * A little more complex POJO class
*/
public class Test07{
	public TestEnum value;
	public void toC(){
		value = TestEnum.OptionC;
	}
	public void to(TestEnum value){
		this.value = value;
	}
}

enum TestEnum{
	OptionA,
	OptionB,
	OptionC,
	OptionD
}

