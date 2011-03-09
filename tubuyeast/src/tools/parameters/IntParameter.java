/*******************************************************************************
 * IntParameter.java
 * 
 * Created on 21-Sep-2003
 * Created by tedmunds
 * 
 ******************************************************************************/
package tools.parameters;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tools.swing.HorizontalFlowPanel;


/**
 * This {@link Parameter}subclass represents an int-valued parameter that can
 * take on values within a specified range.
 * 
 * @author epiuze (adapted from tedmunds)
 */
public class IntParameter extends Parameter implements PropertyChangeListener, ChangeListener {

    private int defaultValue;
    private int value;
    private int minValue;
    private int maxValue;    
    private BooleanParameter booleanp = new BooleanParameter("enabled", true);
    private ParameterListener booleanListener;

    /**
     * Creates a new <code>IntParameter</code> with the specified name,
     * default value, minimum value, and maximum value.
     * 
     * @param name
     *        the name of this parameter
     * @param defaultValue
     *        the default value of this parameter
     * @param minValue
     *        the minimum value of this parameter
     * @param maxValue
     *        the maximum value of this parameter
     */
    public IntParameter(String name, int defaultValue, int minValue, int maxValue) {        
        super(name);
        // Make sure default is within range
        this.defaultValue = defaultValue;
        if (defaultValue < minValue) this.defaultValue = minValue;
        if (defaultValue > maxValue) this.defaultValue = maxValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = defaultValue;
    }
    
    /**
     * Sets the value of this parameter. Registered
     * <code>ParameterListener</code> s are notified of the change. The value
     * is clamped to [minValue, maxValue] where minValue and maxValue are the
     * parameter's minimum and maximum allowable values respectively.
     * @param value
     *        the new value of this parameter
     */
    public void setValue(int value) {
        if (tsetBeingSet()) return;
        this.value = value;
        if (this.value < minValue) this.value = minValue;
        if (this.value > maxValue) this.value = maxValue;
        updateView();
        notifyListeners();
        this.finishSetting();
    }

    /**
     * Sets the minimum allowed value of this parameter. The default and current
     * value may be adjusted to comply.
     * @param minValue
     */
    public void setMinimum(int minValue) {
        if (tsetBeingSet()) return;
        this.minValue = minValue;
        if (defaultValue < minValue) this.defaultValue = minValue;
        if (value < minValue) value = minValue;
        updateView();
        notifyListeners();
        this.finishSetting();
    }

    /**
     * Sets the maximum allowed value of this parameter. The default and current
     * value may be adjusted to comply.
     * @param maxValue
     */
    public void setMaximum(int maxValue) {
        if (tsetBeingSet()) return;        
        this.maxValue = maxValue;
        if (defaultValue > maxValue) this.defaultValue = maxValue;
        if (value > maxValue) value = maxValue;
        updateView();
        notifyListeners();
        this.finishSetting();
    }

    /**
     * Gets the minimum value that this parameter may take on.
     * @return the minimum value of this parameter
     */
    public int getMinimum() {
        return minValue;
    }

    /**
     * Gets the maximum value of this parameter.
     * @return the maximum value that this parameter may take on
     */
    public int getMaximum() {
        return maxValue;
    }

    /**
     * Gets the current value of this parameter
     * @return the current value.
     */
    public int getValue() {
        return value;
    }
    
    /**
     * The slider that displays/controls the parameter.
     */
    private JSlider slider;
    
    /**
     * Default floating point format for a floating point text field.
     */
    public static String DEFAULT_FLOATING_POINT_FORMAT = "0.0000";

    /**
     * Default floating point format for an integer text field.
     */
    public static String DEFAULT_INTEGER_FORMAT = "0";

    /**
     * Default floating point format as a <code>NumberFormat</code>.
     */
    public static NumberFormat DEFAULT_FLOATING_POINT_NUMBER_FORMAT = new DecimalFormat(DEFAULT_FLOATING_POINT_FORMAT);

    /**
     * Default floating point format as a <code>NumberFormat</code>.
     */
    public static NumberFormat DEFAULT_INTEGER_NUMBER_FORMAT = new DecimalFormat(DEFAULT_INTEGER_FORMAT);
    
    /**
     * Default width in pixels of the label to the left of a slider in a
     * slideNText panel. If the label doesn't fit in this width a tool tip with
     * the parameter name will be set.
     */
    public static int DEFAULT_SLIDER_LABEL_WIDTH = 160;
    
    /**
     * The text field that displays this parameter's value.
     */
    private JFormattedTextField textField = new JFormattedTextField( DEFAULT_INTEGER_NUMBER_FORMAT );
    
    private JPanel panel = null;
    
    /**
     * Gets a slider and text box control for this int parameter.
     * @return controls
     */
    public JPanel getSliderControls() {
        if ( panel != null ) return panel;

        // Create the ui components
        JLabel label = new JLabel( getName() );
        
        slider = new JSlider(SwingConstants.HORIZONTAL, getMinimum(), getMaximum(), getDefaultValue());
        
        textField = new JFormattedTextField( DEFAULT_INTEGER_NUMBER_FORMAT );
        
        textField.setText( "" + value );

        slider.addChangeListener(this);
        
        textField.addPropertyChangeListener("value", this);
        
        Dimension d;
        
        d = new Dimension(textField.getPreferredSize());
        d.width = DEFAULT_SLIDER_LABEL_WIDTH;
        textField.setPreferredSize(d);
        textField.setMinimumSize(d);

        d = new Dimension(label.getPreferredSize());
        if (d.width > DEFAULT_SLIDER_LABEL_WIDTH) {
            label.setToolTipText( getName() );
        }
        d.width = DEFAULT_SLIDER_LABEL_WIDTH;
        label.setPreferredSize(d);
        label.setMinimumSize(d);
        
        // Create the panel that holds the ui component
        panel = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;


        panel.setLayout(layout);
        panel.add(label, gbc);
        gbc.gridx++;
        gbc.weightx = 1;
        panel.add(slider, gbc);
        gbc.gridx++;
        gbc.weightx = 0;
        panel.add(textField, gbc);
        gbc.gridx++;

        updateView();
        
        return panel;
    }

    /**
     * Gets a slider, a text box and checkbox control for this double parameter.
     * Use "enabled" as the check box label.
     * @return controls
     */
    public Component getSliderControlsExtended() {
        return getSliderControlsExtended("enabled");
    }

    /**
     * Gets a slider, a text box and checkbox control for this double parameter.
     * @param checkboxText 
     * @return controls
     */
    public JPanel getSliderControlsExtended(String checkboxText) {
        booleanp.setName(checkboxText);

        if ( panel != null ) return panel;
        // Create the ui components
        JLabel label = new JLabel( getName() );
        slider = new JSlider(SwingConstants.HORIZONTAL, getMinimum(), getMaximum(), getDefaultValue());
        textField = new JFormattedTextField( DEFAULT_INTEGER_NUMBER_FORMAT );
        
        textField.setText( "" + value );

        slider.addChangeListener(this);
        textField.addPropertyChangeListener("value", this);
        
        Dimension d;
        
        d = new Dimension(textField.getPreferredSize());
        d.width = 3*DEFAULT_SLIDER_LABEL_WIDTH/4;
        textField.setPreferredSize(d);
        textField.setMinimumSize(d);
//        textField.setMaximumSize(d);

        d = new Dimension(label.getPreferredSize());
        if (d.width > DEFAULT_SLIDER_LABEL_WIDTH) {
            label.setToolTipText( getName() );
        }
        d.width = DEFAULT_SLIDER_LABEL_WIDTH;
        label.setPreferredSize(d);
        label.setMinimumSize(d);
        
        // Create the panel that holds the ui component
        panel = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;

        panel.setLayout(layout);
        
        gbc.ipadx = 10;
        gbc.ipady = 10;
        panel.add(label, gbc);
        gbc.gridx++;
        gbc.weightx = 0;
        gbc.ipadx = 0;
        gbc.ipady = 1;
        panel.add(slider, gbc);
        gbc.gridx++;
        gbc.weightx = 0;
        panel.add(textField, gbc);
        gbc.gridx++;
        
        panel.add(booleanp.getControls(), gbc);
        gbc.gridx++;
        gbc.weightx = 0;

        
        booleanListener = new ParameterListener() {

            @Override
            public void parameterChanged(Parameter parameter) {
                notifyListeners();
            }
            
        };

        booleanp.addParameterListener(booleanListener);

        updateView();

        return panel;
    }
    
     /**
     * Gets a  text box control for this int parameter.
     * @return controls
     */
    public JPanel getControls() {
        if ( panel != null ) return panel;
        // Create the ui components
        JLabel label = new JLabel( getName() );
        
        textField.setText( "" + value );

        textField.addPropertyChangeListener("value", this);
        
        Dimension d;
        
        d = new Dimension(textField.getPreferredSize());
        d.width = DEFAULT_SLIDER_LABEL_WIDTH;
        textField.setPreferredSize(d);
        textField.setMinimumSize(d);

        d = new Dimension(label.getPreferredSize());
        if (d.width > DEFAULT_SLIDER_LABEL_WIDTH) {
            label.setToolTipText( getName() );
        }
        d.width = DEFAULT_SLIDER_LABEL_WIDTH;
        label.setPreferredSize(d);
        label.setMinimumSize(d);
        
        // Create the panel that holds the ui component
        HorizontalFlowPanel hfp = new HorizontalFlowPanel();
        hfp.add( label );
        hfp.add( textField );
        panel = hfp.getPanel();
        
        updateView();

        return panel;
    }

    
    /**
     * Called by the text field when a new value is committed. The updated value
     * of the text field is passed down to the represented parameter.
     * 
     * @param evt
     *        ignored
     * @see java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        Object valueO = textField.getValue();
        if (valueO instanceof Number) {
            int v = ((Number)valueO).intValue();
            setValue(v);
            return;
        }
    }

    /**
     * Called whenever the state of this slider's <code>JSlider</code> is
     * changed. The change in state is passed down to this slider's
     * <code>Parameter</code>
     * 
     * @param event
     *        the change event (ignored)
     */
    public void stateChanged(ChangeEvent event) {
    	if (isBlocking()) return;
    	setBlocking(true);
    	
        int ivalue = slider.getValue();

        setValue(ivalue);
        
        setBlocking(false);
    }
 
    /**
     * Updates this parameter's <code>JSlider</code> and <code>JFormattedTextField</code>
     * to reflect the range and
     * current value of this slider's parameter.
     */
    private void updateView() {
        if ( slider != null ) {
            slider.setValue(getValue());
        }
        // update the text field
        textField.setValue(getValue());        
    }

    private boolean blocking = false;
    private void setBlocking(boolean b) {
    	blocking = b;
    }
    private boolean isBlocking() {
    	return blocking;
    }
    
	private double previous = 0;

   
    /**
     * Check or uncheck the checkbox linked to this parameter.
     * @param b
     */
    public void setChecked(boolean b) {
        booleanp.setValue(b);
    }
    
    /**
     * @return whether this control is enabled.
     */
    public boolean isChecked() {
        return booleanp.getValue();
    }

    /**
     * Enables or disables the slider linked to this parameter.
     * @param b
     */
    public void setEnabled(boolean b) {
        slider.setEnabled(b);
        textField.setEnabled(b);
    }
    
    /**
     * @return the default value of this parameter.
     */
    public int getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default value for this parameter.
     * @param v
     */
    public void setDefaultValue(int v) {
        defaultValue = v;
    }
}
