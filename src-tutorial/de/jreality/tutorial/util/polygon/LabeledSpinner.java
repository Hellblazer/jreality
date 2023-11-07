package de.jreality.tutorial.util.polygon;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

public class LabeledSpinner extends JComponent {

	static final long serialVersionUID = 2L;

	private SpinnerNumberModel model;
	private JSpinner spinner;

	private JLabel jLabel;

	public SpinnerNumberModel getModel() {
		return model;
	}

	public void setModel(SpinnerNumberModel model) {
		this.model = model;
	}

	public LabeledSpinner(String label, double curr, double min, double max,
			double step) {
		super();
		setLayout(new GridLayout());
		jLabel = new JLabel(label);
		model = new SpinnerNumberModel(curr, min, max, step);
		spinner = new JSpinner(model);
		JComponent editor = new JSpinner.NumberEditor(spinner,
				"###0.##########");
		spinner.setEditor(editor);
		this.add(jLabel);
		this.add(spinner);
	}
	
	public void setText(String text){
		jLabel.setText(text);
	}
	
	@Override
	public void setToolTipText(String text) {
		jLabel.setToolTipText(text);
	}

	public void addChangeListener(ChangeListener listener) {
		model.addChangeListener(listener);
	}

	public double getValue() {
		return (Double) model.getValue();
	}
	
	@SuppressWarnings("rawtypes")
	public void setMinimum(Comparable minimum){
		model.setMinimum(minimum);
	}
	
	@SuppressWarnings("rawtypes" )
	public void setMaximum(Comparable maximum){
		model.setMaximum(maximum);
	}
	
	public void setValue(Double value){
		model.setValue(value);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		spinner.setEnabled(enabled);
	}

	public void setValue(double d) {
		model.setValue(new Double(d));
	}

	public void setStepsize(double step) {
		model.setStepSize(step);
	}

}