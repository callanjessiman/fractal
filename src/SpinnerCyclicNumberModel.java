import javax.swing.SpinnerNumberModel;

/* class SpinnerCyclicNumberModel:
 * - provides a SpinnerNumberModel that cycles at min/max values instead of halting
 */

/* TODO:
 * - use the generic Number class like SpinnerNumberModel
 */

public class SpinnerCyclicNumberModel extends SpinnerNumberModel {
	private static final long serialVersionUID = 4280663078525294111L;
	
	public SpinnerCyclicNumberModel(double value, double minimum, double maximum, double stepSize) {
		super(value, minimum, maximum, stepSize);
	}

	public Object getNextValue() {
		Object superNext = super.getNextValue();
		if(superNext != null) {
			if((double)superNext < (double)getMaximum()) {
				return superNext;
			}
		}
		return (double)getValue() + (double)getStepSize() - (double)getMaximum() + (double)getMinimum(); // cycle around instead of halting
	}

	public Object getPreviousValue() {
		Object superPrevious = super.getPreviousValue();
		if(superPrevious != null) {
			return superPrevious;
		}
		return (double)getValue() - (double)getStepSize() + (double)getMaximum() - (double)getMinimum(); // cycle around instead of halting
	}
}
