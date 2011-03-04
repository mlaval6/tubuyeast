package tools.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import tools.parameters.Parameter;

/**
 * @author piuze
 * @param <E>
 */
public final class ListComboBox<E> extends Parameter {

    public ListComboBox(List<E> list) {
        super("");
        
        values = new LinkedList<Object>(list);
        
        createControls();
        setSelected(0);
    }

    private List<Object> values;
    
    private Object selected;
    
    public Object getSelected() {
        return selected;
    }
    
    public void setSelected(Object e) {
        int i = 0;
        for (Object m : values) {
            if (m.equals(e)) {
                setSelected(i);
                return;
            }
            i++;
        }
    }
    
    public void setSelected(int n) {
        selected = values.get(n);
        interList.setSelectedIndex(n);
    }

    
    private JComboBox interList;
    
    private VerticalFlowPanel vfp;
    
    private void createControls() {
        vfp = new VerticalFlowPanel();
        
        int n = values.size();
        final String[] pmethods = new String[n];
        for (int i = 0; i < n; i++) {
            pmethods[i] = values.get(i).toString();
        }

        interList = new JComboBox(pmethods);
        interList.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                JComboBox cb = (JComboBox) ae.getSource();
                String mode = (String) cb.getSelectedItem();

                // Assign the interpolation mode selected
                for (Object m : values) {
                    if (m.toString().equalsIgnoreCase(mode)) {
                        selected = m;
                        notifyListeners();
                    }
                }
            }
        });
        vfp.add(interList);
    }
    
    /**
     * @return A combox box for selecting between elements in a list.
     */
    public JPanel getControls() {
        
        return vfp.getPanel();
    }    
}
