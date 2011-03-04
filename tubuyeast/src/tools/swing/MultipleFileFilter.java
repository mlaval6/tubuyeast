/**
 * 
 */
package tools.swing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileFilter;

/**
 * @author piuze
 *
 */
public class MultipleFileFilter extends FileFilter {

	private final String filter;

    public MultipleFileFilter(String filetype) {
    	filter = filetype;
    }
	
	/* (non-Javadoc)
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File f) {
		
		String extension = getExtension(f);
		if (extension != null) {
			if (extension.equals(filter)) return true;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		return "." + filter;
	}

    /*
     * Get the extension of a file.
     */
    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

}
