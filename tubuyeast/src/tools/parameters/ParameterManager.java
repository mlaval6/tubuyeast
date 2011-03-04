package tools.parameters;

import java.util.LinkedList;
import java.util.List;


public class ParameterManager {
	
	private List<ParameterListener> parameterListeners = new LinkedList<ParameterListener>();

	private List<Parameter> parameters = new LinkedList<Parameter>();

	public void addParameterListener(ParameterListener l) {
		parameterListeners.add(l);
		
		// Add this parameter listener to all parameteters
		for (Parameter p : parameters) {
			if (!p.getParameterListeners().contains(l)) {
				p.addParameterListener(l);
			}
		}
	}
	
	public void addParameter(Parameter p) {
		parameters.add(p);

		// Add all current listeners to this parameter
		for (ParameterListener pl : parameterListeners) {
			if (!p.getParameterListeners().contains(pl)) {
				p.addParameterListener(pl);
			}
		}
	}
	
	public void removeParameterListeners() {
		for (ParameterListener pl : parameterListeners) {
			for (Parameter p : parameters) {
				p.removeParameterListener(pl);
			}
		}
		parameterListeners.clear();
	}
	
	public void removeParameters() {
		parameters.clear();
	}
	
	private boolean holding = false;
	
	private Object lockingObject = null;
	
	public boolean isHolding() {
		return holding;
	}
	
	public void hold(boolean bool) {
		if (holding == bool) {
			// Mismatch in hold calls!
			// TODO: handle this in some cleaner way
			return;
		}
		
		holding = bool;
		
		if (bool) {
			for (ParameterListener pl : parameterListeners) {
				for (Parameter p : parameters) {
					p.removeParameterListener(pl);
				}
			}
		}
		else {
			for (ParameterListener pl : parameterListeners) {
				for (Parameter p : parameters) {
					p.addParameterListener(pl);
				}
			}
		}
	}
	
	public List<Parameter> getParameters() {
		return parameters;
	}
	public List<ParameterListener> getParameterListeners() {
		return parameterListeners;
	}
	
	/**
	 * Manually notify the parameter listeners that a change has occured.
	 */
	public void notifyListeners() {
		for (ParameterListener pl : parameterListeners) {
			pl.parameterChanged(null);
		}
	}
}
