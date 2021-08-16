import javax.swing.AbstractSpinnerModel;

/* class SpinnerBinaryIntModel:
 * - provides a SpinnerModel that includes all integer powers of two (0, 1, 2, 4, 8, ...)
 */

public class SpinnerBinaryIntModel extends AbstractSpinnerModel {
	private static final long serialVersionUID = 5017737605225758694L;
	
	int value;
	
	public SpinnerBinaryIntModel() {
		this(1);
	}
	
	public SpinnerBinaryIntModel(int startValue) {
		value = startValue;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object v) {
		value = (int)v;
		fireStateChanged();
	}
	
	// return next power of two larger than current value
	public Object getNextValue() {
		return 1 << ((int)(Math.log(value)/Math.log(2)) + 1);
	}

	// return next power of two smaller than current value, or null if value is already at minimum (zero)
	public Object getPreviousValue() {
		if(value == 0) {
			return null;
		}
		
		int low = 1 << (int)(Math.log(value)/Math.log(2));
		if(low == value) {
			return value/2;	// if value is already a power of two, return half of it
		}
		else {
			return low;		// if value isn't a power of two, return next power of two smaller than it
		}
	}
}
