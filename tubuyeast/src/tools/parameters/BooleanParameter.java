package tools.parameters;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;


/**
 * Simple boolean parameter class
 * @author kry
 */
public class BooleanParameter extends Parameter implements ItemListener {
    /**
     * The current value of this parameter.
     */
    protected boolean value;

    /**
     * The default value of this parameter.
     */
    protected boolean defaultValue;

    /**
     * Creates a new <code>BooleanParameter</code> with the specified name and
     * default value.
     * @param name the name of this parameter
     * @param defaultValue the default value of this parameter
     */
    public BooleanParameter(String name, boolean defaultValue) {
        super(name);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }
        
    /**
     * Sets the value of this parameter.  Registered
     * <code>ParameterListener</code>s are notified of the change.
     * @param value the new value of this parameter
     */
    public void setValue(boolean value)
    {
        if (tsetBeingSet()) return;
        this.value = value;
        updateView();
        notifyListeners();
        this.finishSetting();
    }

    /**
     * Gets the value of this parameter.
     * @return the current value of this parameter.
     */
    public boolean getValue() {
        return value;
    }
    
    /**
     * Gets the default value of this parameter.
     * 
     * @return the default value of this parameter.
     */
    public boolean getDefaultValue()
    {
        return defaultValue;
    }

    private JCheckBox checkBox;

    /**
     * Enable/disable the checkbox linked to this parameter.
     * @param b
     */
    public void setEnabled(boolean b) {
        if (checkBox != null)
        checkBox.setEnabled(b);
    }
    
    private JPanel panel = null;
    
    /**
     * Gets a swing panel with a check box to control this parameter
     * @return the control panel
     */
    public JPanel getControls() {
        if ( panel != null ) return panel;
        checkBox = new JCheckBox( getName(), getValue() );
        checkBox.addItemListener(this);
        //parameter.addView(this);
        panel = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.LEFT);
        panel.setLayout(layout);
        panel.add(checkBox);
        return panel;    
    }

    /**
     * Updates this checkBox's <code>JCheckBox</code> to reflect the current
     * value of this checkBox's parameter.
     */
    private void updateView() {
        if ( checkBox == null ) return;
        checkBox.setSelected( getValue() );
    }

    /**
     * Called whenever the state of this checkBox's <code>JCheckBox</code> is
     * changed.  The change in state is passed down to this check box's
     * <code>BooleanParameter</code>.
     * 
     * @param arg0 the change event (ignored)
     */
    public void itemStateChanged(ItemEvent arg0) {
        setValue( checkBox.isSelected() );
    }

}
