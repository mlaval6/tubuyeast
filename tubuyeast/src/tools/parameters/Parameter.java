package tools.parameters;

import java.util.ArrayList;
import java.util.List;


/**
 * This abstract class is the parent class for a variety of parameters.  A
 * <code>Parameter</code> is meant to represent a varying application parameter
 * whose value may be displayed/changed through a variety of views.
 * 
 * @author tedmunds
 */
public abstract class Parameter
{
    /**
     * The name of this parameter.
     */
    protected String name;

    @Override
    /**
     * @return a string containing the name of this parameter.
     */
    public String toString() {
    	return name;
    }

    /**
     * The <code>List</code> of {@link ParameterListener}s registered to receive
     * notification whenever this parameter's value is changed.
     */
    private List<ParameterListener> parameterListeners = new ArrayList<ParameterListener>();
    
    /**
     * An indicator of whether a set of this parameter's value is currently
     * in progress.
     */
    private boolean isBeingSet;

//    /**
//     * The list of {@link ParameterView}s registered as views of this
//     * parameter.
//     */
//    private List<ParameterView> views;
    
    /**
     * Constructs a parameter with the specified name.
     * 
     * @param name the name of the parameter
     */
    public Parameter(String name) {
        super();        
        this.name = name;
    }

    /**
     * Gets the name of this parameter.
     * 
     * @return the name of this parameter
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this parameter.
     * @param name the name of this parameter.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Registers the specified {@link ParameterListener} to receive
     * notification whenever this parameter changes.
     * 
     * @param listener the listener to register
     */
    public void addParameterListener(ParameterListener listener) {
        parameterListeners.add(listener);
    }
    
    /**
     * Returns the list of all the <code>ParameterListener</code> s added to
     * this <code>Parameter</code> with
     * {@link #addParameterListener(ParameterListener)}.
     * 
     * @return all of the <code>ParameterListener</code> s added or an empty
     *         list if no listeners have been added
     */
    public List<ParameterListener> getParameterListeners()
    {
        return parameterListeners;
    }
    
    /**
     * Removes a <code>ParameterListener</code>.
     * @param listener the <code>ParameterListener</code> to remove
     */
    public void removeParameterListener(ParameterListener listener)
    {
        parameterListeners.remove(listener);
    }

    /**
     * Removes all <code>ParameterListener</code>.
     */
    public void removeParameterListeners()
    {
        parameterListeners.clear();
    }

//    /**
//     * Registers the provided <code>ParameterView</code> as a view of this
//     * parameter.
//     * 
//     * @param view
//     *        the view to register
//     */
//    public void addView(ParameterView view)
//    {
//        views.add(view);
//        notifyView(view);
//    }

    /**
     * If this parameter is not already in the process of being set, it is
     * marked as being set.
     * 
     * @return whether this parameter was already in the process of being
     *         set.
     */
    protected synchronized boolean tsetBeingSet() {
        boolean value = isBeingSet;
        isBeingSet = true;
        return value;
    }

    /**
     * Marks this parameter as having finished its set.
     */
    protected synchronized void finishSetting() {
        isBeingSet = false;
    }

    /**
     * Notifies all registered <code>ParameterListener</code>s of a change
     * in this parameter's value.
     */
    protected void notifyListeners() {
        for ( ParameterListener l : parameterListeners ) {
            l.parameterChanged(this);
        }
    }

//    /**
//     * Notifies all of the registered <code>ParameterView</code> s of this
//     * parameter's condition.
//     */
//    protected void notifyViews() {
//        for (int i = 0; i < views.size(); i++)
//        {
//            ParameterView view = views.get(i);
//            notifyView(view);
//        }
//    }
//
//    /**
//     * Notifies the specified view of this parameter's condition.
//     * 
//     * @param view
//     *        the view to be notified
//     */
//    protected void notifyView(ParameterView view)
//    {
//        view.updateView(this);
//    }
       
}
