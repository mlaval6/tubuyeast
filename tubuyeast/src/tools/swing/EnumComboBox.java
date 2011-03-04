package tools.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import tools.parameters.Parameter;

/**
 * @author piuze
 * @param <E>
 */
public final class EnumComboBox<E extends Enum<E>> extends Parameter {

    /**
     * @param e 
     * @param myEnum
     */
    public EnumComboBox(E myEnum) {
        super("");
        values = myEnum.getDeclaringClass().getEnumConstants();
        createControls();
        setSelected(myEnum);
    }

    private E[] values;
    
    private E selected;
    
    public E getSelected() {
        return selected;
    }
    
    public void setSelected(Enum e) {
        int i = 0;
        for (Enum m : values) {
            if (m.equals(e)) {
                setSelected(i);
                return;
            }
            i++;
        }
    }
    
    public void setSelected(int n) {
        selected = values[n];
        interList.setSelectedIndex(n);
    }

    
    private JComboBox interList;
    
    private VerticalFlowPanel vfp;
    
    private void createControls() {
        vfp = new VerticalFlowPanel();
        
        int n = values.length;
        final String[] pmethods = new String[n];
        for (int i = 0; i < n; i++) {
            pmethods[i] = values[i].toString();
        }

        interList = new JComboBox(pmethods);
        interList.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                JComboBox cb = (JComboBox) ae.getSource();
                String mode = (String) cb.getSelectedItem();

                // Assign the interpolation mode selected
                for (Enum m : values) {
                    if (m.toString().equalsIgnoreCase(mode)) {
                        selected = (E) m;
                        notifyListeners();
                    }
                }
            }
        });
        vfp.add(interList);
    }
    
    /**
     * @return A combox box for selecting between enums.
     */
    public JPanel getControls() {
        
        return vfp.getPanel();
    }    
}
