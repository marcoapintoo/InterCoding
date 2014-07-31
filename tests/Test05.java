package org.pinto.test05;

/**
 * Control structures with complex conditions
*/
class Test05{
	public void testingIf4(long limit){
		int i = 0, j;
		i++;
		if((j=i)==0){
			System.out.println("Counter is zero");
		}else{
			System.out.println("Counter is not zero");
		}
	}
	public void testingFor2(long limit){
		int i = 0, j;
		for(; (j=++i) <= (int) limit; i++){
			System.out.println("Counter = " + i);
		}
	}
	public void testingWhile2(long limit){
		int i = 0;
		//NOTE: ++i in condition
		while(++i <= (int) limit){
			System.out.println("Counter = " + i);
			i++;
		}
	}
	public void testingWhile3(long limit){
		int i = 0;
		//NOTE: ++i in condition
		while(i++ <= (int) limit){
			System.out.println("Counter = " + i);
			i++;
		}
	}
	public void testingWhile4(long limit){
		int i = 0, j;
		//NOTE: complex condition
		while((j=++i) <= (int) limit){
			System.out.println("Counter = " + i);
			i++;
		}
	}
	public void testingDoWhile2(long limit){
		int i = 0, j;
		//NOTE: complex condition
		do{
			System.out.println("Counter = " + i);
			i++;
		}while((j=++i) <= (int) limit);
	}
}
