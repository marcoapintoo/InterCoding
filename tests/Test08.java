package org.pinto.test08;

/**
 * A little more complex POJO class
*/
public class Test08{
	private EnumPriceType value;
	public EnumPriceType getValue(){
		return value;
	}
	public void setValue(EnumPriceType value){
		this.value = value;
	}
	public float getCurrentPrice(){
		value.getPrice();
	}
}

//From StackOverFlow
enum EnumPriceType {

    WITH_TAXES {
        @Override
        public float getPrice(float input) {
            return input*1.20f;
        }
        public String getFormattedPrice(float input) {
            return input*1.20f + " €";
        }
        },

    WITHOUT_TAXES {
        @Override
        public float getPrice(float input) {
            return input;
        }
    },
    ;

    public abstract float getPrice(float input);

    public static void main(String[] args) {
        WITH_TAXES.getFormattedPrice(33f);
    }

}
