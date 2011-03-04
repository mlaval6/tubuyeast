/*******************************************************************************
 * FloatParameter.java
 * 
 * Created on 21-Sep-2003
 * Created by tedmunds
 * 
 ******************************************************************************/
package tools.parameters;


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
 * This {@link Parameter}subclass represents a float-valued parameter that can
 * take on values within a specified range.
 * 
 * @author tedmunds (kry: modified and simplified for 599)
 */
public class FloatParameter extends Parameter implements PropertyChangeListener, ChangeListener {

    private float defaultValue;
    private float value;
    private float minValue;
    private float maxValue;    
    
    /**
     * Creates a new <code>FloatParameter</code> with the specified name,
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
    public FloatParameter(String name, float defaultValue, float minValue, float maxValue) {        
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
    public void setValue(float value) {
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
    public void setMinimum(float minValue) {
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
    public void setMaximum(float maxValue) {
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
    public float getMinimum() {
        return minValue;
    }

    /**
     * Gets the maximum value of this parameter.
     * @return the maximum value that this parameter may take on
     */
    public float getMaximum() {
        return maxValue;
    }

    /**
     * Gets the current value of this parameter
     * @return the current value.
     */
    public float getValue() {
        return value;
    }
    
    /**
     * The slider that displays/controls the parameter.
     */
    private JSlider slider;
    
    /**
     * Whether this slider is a logarithmic scale slider.
     */
    private boolean isLogarithmic;

    /**
     * Default number of ticks for number sliders.
     */
    public static int DEFAULT_SLIDER_TICKS = 2000;
    
    /**
     * Default floating point format for a floating point text field.
     */
    public static String DEFAULT_FLOATING_POINT_FORMAT = "0.0000";
    
    /**
     * Default floating point format as a <code>NumberFormat</code>.
     */
    public static NumberFormat DEFAULT_FLOATING_POINT_NUMBER_FORMAT = new DecimalFormat(DEFAULT_FLOATING_POINT_FORMAT);
    
    /**
     * Default width in pixels of the label to the left of a slider in a
     * slideNText panel. If the label doesn't fit in this width a tool tip with
     * the parameter name will be set.
     */
    public static int DEFAULT_SLIDER_LABEL_WIDTH = 160;
    
    /**
     * The text field that displays this parameter's value.
     */
    private JFormattedTextField textField = new JFormattedTextField( DEFAULT_FLOATING_POINT_NUMBER_FORMAT );
    
    private JPanel panel = null;
    
    /**
     * Gets a slider and text box control for this float parameter.
     * @param isLog set to true to have log behaviour with slider
     * @return controls
     */
    public JPanel getSliderControls( boolean isLog ) {
        if ( panel != null ) return panel;
        this.isLogarithmic = isLog;
        // Create the ui components
        JLabel label = new JLabel( getName() );
        slider = new JSlider(SwingConstants.HORIZONTAL, 0, DEFAULT_SLIDER_TICKS, DEFAULT_SLIDER_TICKS / 2);
        textField = new JFormattedTextField( DEFAULT_FLOATING_POINT_NUMBER_FORMAT );
        
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

        return panel;
    }

     /**
     * Gets a  text box control for this float parameter.
     * @return controls
     */
    public JPanel getControls() {
        if ( panel != null ) return panel;
        this.isLogarithmic = false;
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
            float v = ((Number)valueO).floatValue();
            setValue(v);
            return;
        }
    }

    /**
     * Updates this parameter's <code>JSlider</code> and <code>JFormattedTextField</code>
     * to reflect the range and
     * current value of this slider's parameter.
     */
    private void updateView() {
        if ( slider != null ) {
            int sliderRange = slider.getMaximum() - slider.getMinimum();
            int ivalue;
            double min = getMinimum();
            double max = getMaximum();
            double v = getValue();
            if (isLogarithmic)
            {
                min = Math.log(min);
                max = Math.log(max);
                v = Math.log(v);
            }
            ivalue = slider.getMinimum()
                     + (int)(Math.round((v - min) * sliderRange / (max - min)));
            slider.setValue(ivalue);
        }
        // update the text field
        textField.setValue(getValue());        
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
        int ivalue = slider.getValue();
        int sliderRange = slider.getMaximum() - slider.getMinimum();
        double min = getMinimum();
        double max = getMaximum();
        double v;
        if (isLogarithmic) {
            min = Math.log(min);
            max = Math.log(max);
        }
        v = min + ivalue * (max - min) / sliderRange;
        if (isLogarithmic) {
            v = Math.exp(v);
        }        
        setValue((float) v);
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
    public float getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default value for this parameter.
     * @param v
     */
    public void setDefaultValue(float v) {
        defaultValue = v;
    }
   
}
