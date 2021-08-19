import javax.swing.SpinnerNumberModel;

/* class SpinnerBinaryIntModel:
 * - provides a SpinnerModel that includes all integer powers of two (1, 2, 4, 8, ...)
 */

public class SpinnerBinaryIntModel extends SpinnerNumberModel {
	private static final long serialVersionUID = 5017737605225758694L;
	
	public SpinnerBinaryIntModel() {
		this(1);
	}
	
	public SpinnerBinaryIntModel(int startValue) {
		super(startValue, 0, Integer.MAX_VALUE, 0);
	}
	
	// return next power of two larger than current value
	public Object getNextValue() {
		return 1 << ((int)(Math.log((int)getValue())/Math.log(2)) + 1);
	}

	// return next power of two smaller than current value, or null if value is already at minimum (zero)
	public Object getPreviousValue() {
		if((int)getValue() == 1) {
			return null;
		}
		
		int low = 1 << (int)(Math.log((int)getValue())/Math.log(2));
		if(low == (int)getValue()) {
			return (int)getValue()/2;	// if value is already a power of two, return half of it
		}
		else {
			return low;		// if value isn't a power of two, return next power of two smaller than it
		}
	}
}
