package org.pinto.test04;


/**
 * Control structures
*/
class Test04{
	public void testingIf1(long limit){
		if(true){
			System.out.println("Counter = " + i);
		}
	}
	public void testingIf2(long limit){
		int i = 0;
		i++;
		if(i==0){
			System.out.println("Counter is zero");
		}
	}
	public void testingIf3(long limit){
		int i = 0;
		i++;
		if(i==0){
			System.out.println("Counter is zero");
		}else{
			System.out.println("Counter is not zero");
		}
	}
	public void testingFor1(long limit){
		for(int i = 0; i <= (int) limit; i++){
			System.out.println("Counter = " + i);
		}
	}
	public void testingFor2(long limit){
		int i = 0;
		for(; i <= (int) limit; i++){
			System.out.println("Counter = " + i);
		}
	}
	public void testingWhile1(long limit){
		int i = 0;
		while(i <= (int) limit){
			System.out.println("Counter = " + i);
			i++;
		}
	}
	public void testingDoWhile1(long limit){
		int i = 0;
		do{
			System.out.println("Counter = " + i);
			i++;
		}while(i <= (int) limit);
	}
}
