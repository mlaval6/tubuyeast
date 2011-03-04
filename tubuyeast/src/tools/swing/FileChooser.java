package tools.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/*
 * FileChooserDemo.java uses these files:
 *   images/Open16.gif
 *   images/Save16.gif
 */
public class FileChooser extends JPanel
                             implements ActionListener {
    static private final String newline = "\n";
    private JButton openButton, saveButton;
    private JTextArea log;
    private JFileChooser fc;
    private ActionListener saveListener = null;
    private ActionListener openListener = null;
    private File currentFile;
    private JTextPane ntext = new JTextPane();
//    private JTextArea ntext = new JTextArea();
    private JLabel lblText = new JLabel();

    public FileChooser() {
    	this("");
    }
    
    public FileChooser(String directory) {
        this(directory, "");
    }    

    public FileChooser(String directory, String text) {
    	this(directory, text, new ArrayList<String>());
    }

    public FileChooser(String directory, String text, String filetype) {
    	this(directory, text, new ArrayList<String>(Arrays.asList(filetype)));
    }
    
    /**
     * 
     * @param directory
     * @param text
     * @param filetypes a list of file types to parse (without a leading dot e.g. xml, jpg, png)
     */
    public FileChooser(String directory, String text, List<String> filetypes) {
        super(new BorderLayout());

        //Create the log first, because the action listeners
        //need to refer to it.
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);

        //Create a file chooser
        fc = new JFileChooser(directory);
        fc.setFileHidingEnabled(true);
        
        if (filetypes == null || filetypes.size() == 0) {
            fc.setAcceptAllFileFilterUsed(true);
        }
        else {
            fc.setAcceptAllFileFilterUsed(false);
	
            for (final String filetype : filetypes) {
            	if (filetype == null || filetype.equals("")) continue;
            	fc.addChoosableFileFilter(new MultipleFileFilter(filetype));
            }
        }
        
        
        //fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        //Create the open button.
        URL urlOpen  = getClass().getResource("resources/Open16.gif");
        URL urlSave = getClass().getResource("resources/Save16.gif");
        ImageIcon openIcon = new ImageIcon(urlOpen);
        ImageIcon saveIcon = new ImageIcon(urlSave);

        openButton = new JButton("Open a File...", openIcon);
        openButton.addActionListener(this);

        //Create the save button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        saveButton = new JButton("Save a File...", saveIcon);
        saveButton.addActionListener(this);

        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout

        ntext.setText("");
        SimpleAttributeSet attribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        int size = 200;
        ntext.setPreferredSize(new Dimension(size, 20));
        ntext.setParagraphAttributes(attribs, true);  
        ntext.setBorder(BorderFactory.createEtchedBorder());
//        ntext.setPreferredSize(new Dimension(200, 15));
//        ntext.setLineWrap(true);
//        ntext.setWrapStyleWord(true); 
        
        lblText.setText(text);
        
        // Add controls
        buttonPanel.add(lblText);
        buttonPanel.add(ntext);
        buttonPanel.add(openButton);
        buttonPanel.add(saveButton);
        
        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
    }

    public void addOpenActionListener(ActionListener l) {
        openListener = l;
    }
    
    public void addSaveActionListener(ActionListener l) {
        saveListener = l;
    }

    public File getCurrentFile() {
        return currentFile;
    }
    
    public void actionPerformed(ActionEvent e) {

        //Handle open button action.
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(FileChooser.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                currentFile = fc.getSelectedFile();
                //This is where a real application would open the file.
                log.append("Opening: " + currentFile.getName() + "." + newline);

                if (openListener != null) {
                    openListener.actionPerformed(e);
                }
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());

        //Handle save button action.
        } else if (e.getSource() == saveButton) {
            int returnVal = fc.showSaveDialog(FileChooser.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                currentFile = fc.getSelectedFile();
                //This is where a real application would save the file.
                log.append("Saving: " + currentFile.getName() + "." + newline);
                
                if (saveListener != null) {
                    saveListener.actionPerformed(e);
                }

            } else {
                log.append("Save command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        }
        if (currentFile != null) {
            setPath("../" + currentFile.getParentFile().getName() + "/" + currentFile.getName());
        }
    }

    /**
     * @param text
     */
    public void setPath(String text) {
        ntext.setText(text);
    }

    /**
     * @param text
     */
    public void setLabel(String label) {
        lblText.setText(label);
    }

    @Override
    public String toString() {
        return log.getText();
    }
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("FileChooserDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new FileChooser("", "Some text"));

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                createAndShowGUI();
            }
        });
    }

	public String getPath() {
		return ntext.getText().substring(2);
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
