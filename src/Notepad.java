// import all the libraries i need
import javax.swing.*; // there's too many so i just used *
import java.awt.Toolkit; 
import java.awt.Dimension; 
import java.awt.Font; 
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.GraphicsEnvironment; 
import java.awt.event.WindowEvent; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File; 	
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter; 
import java.lang.Exception; 
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner; 
import java.util.Locale; 

// this is the notepad class which inherits runnable (for multithreading) and sets up the gui
public final class Notepad implements Runnable {
	// create the application frame
	private JFrame frame = new JFrame("Notepad"); 	

	// define a few variables 
	private int tabWidth = 2; // for tabwidth
	private int fontSize; // for font size
	private String fontStyle;  // for font style
	private String[] fontStyles; // all the possible font styles
	private Font font; // the current font 
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // the screen size
	private int screenWidth = (int)screenSize.getWidth();  // the screen width (horizontal)
	private int screenHeight = (int)screenSize.getHeight(); // the screen length (vertical)

	private JTextArea textArea = new JTextArea(); // the area where you type
	private JTextArea lines; // the line numbers
	private JMenuBar menu; // the menu at the top

	private JMenu file; // the file menu
	private JMenu edit; // the edit menu
	private JMenu more; // the more menu

	private JMenuItem save; // the save file function
	private JMenuItem saveAs; // the save file as function
	private JMenuItem open; // the open file function
	
	private JMenuItem cut; // the cut function
	private JMenuItem copy; // the copy function
	private JMenuItem paste; // the paste function
	private JMenuItem selectAll; // the select all function

	private JMenuItem fontSizeItem; // the font size function
	private JMenuItem fontStyleItem; // the font style function

	private JMenuItem findReplace; // the find and replace function
	private JMenuItem find; // the find function
	private JMenuItem newWindow; // the new window function
	private JMenuItem closeWindow; // the close window function

	private JPanel panel; // holds the scroll bar
	private JScrollPane scroller; // scroll bar itself

	public File currentFile; // the current file user has opened and edits

	private StringSelection ss; // basically what's on the clipboard for copy paste
  	private Toolkit toolkit; // assists with above

  	/* here are my inner classes. i made them inner so they can access the variables of this instance of Notepad easily*/
  	// making them external classes that extend notepad is too much of a pain 
  	// they all implement action listener which is an interface that controls what happens when buttons are clicked

  	private class NewWindow implements ActionListener  { // the new window inner class
		public void actionPerformed(ActionEvent e) { // the action performed abstract method
	        Main.newApp(); // create another application
		}
	}

	private class CloseWindow implements ActionListener { // the close window inner class
		public void actionPerformed(ActionEvent e) { // see line 83 
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); // make the frame close this window
		}
	}

	private class Save implements ActionListener { // save inner class
		public void actionPerformed(ActionEvent e) { // see line 83
			if (currentFile != null) { // if user is editing a file
				try {
					PrintWriter w = new PrintWriter(currentFile); // use print writer to edit file on disk
					w.println(getTextArea()); // set the file on disk to whatever the user typed up
					w.close(); // close once done
				} catch (Exception exception) { // catch exception
					System.out.println("Exception");
				} 
			}
		}

		private Save() { // this is a private constructor so Save() cannot be called i had to override the default
			
		}
	}

	private class Open implements ActionListener { // open inner class
		private JFileChooser chooser; // this is to pick a file from the user hard disk (kind of like file explorer)
		private String data = ""; // the file data

		public void actionPerformed(ActionEvent e) { // see line 83
			chooser = new JFileChooser(); // create an instance of this file explorer object
			int result = chooser.showOpenDialog(null);  // show the "open file" prompt
			if (result == JFileChooser.APPROVE_OPTION) { // if user picked file
				try {
					File file = chooser.getSelectedFile(); // open up file
					currentFile = file; 
					textArea.setText(""); // set the text area on Notepad to file data
					String data = extractTextFromFile(file); 
					textArea.setText(data); // change the title of the app
					frame.setTitle("Notepad -- " + file.getName()); 
				} catch (Exception exception) {
					System.out.println("Exception");
				}
			}
		}

		public String getData() { // get the file data
			return data;
		}

		private Open() { // this is a private constructor so Open() cannot be called i had to override the default
			// this is for defense purposes so program doesn't crash if someone calls Open() again
		}
	}

	private class SaveAs implements ActionListener { // save as inner class 
		private JFileChooser chooser; // similar to open file
		private String path; // the path of the file

		public void actionPerformed(ActionEvent e) {
			chooser = new JFileChooser(); // see line 117
			int result = chooser.showSaveDialog(null); // kind of like line 118 but show save as instead of open
			if (result == JFileChooser.APPROVE_OPTION) { // if user clicked save as
				try {
					currentFile = chooser.getSelectedFile(); // save file as name
					frame.setTitle("Notepad -- " + currentFile.getName()); 
					PrintWriter w = new PrintWriter(currentFile); 		
					w.println(getTextArea()); 
					w.close(); 
				} catch (Exception exception) {
					System.out.println("Exception");
				} 
			}
		}

		private SaveAs() {

		}
	}

	private class FontSize implements ActionListener { // font size inner class 
		public void actionPerformed(ActionEvent e) {
			JTextField fontSizeField = new JTextField(); // create a text field for font size
			JLabel label = new JLabel("Font size (enter an integer between 1 and 100)"); // create object to prompt user to do what is says

			JComponent inputs[] = new JComponent[] { // POLYMORPHISM!!! basically using a JComponent array to hold objects that extend JComponent
				label, 
				fontSizeField
			}; 
			int result = JOptionPane.showConfirmDialog(null, inputs, "Set font", 1); // prompt user to set font
			if (result == JOptionPane.OK_OPTION) { // if user enters number and presses ok 
				fontSize = Integer.valueOf(fontSizeField.getText()); // set font size
				if (fontSize > 0) { // just in case someone tries to break rajesh's code with a negative font size
					font = new Font(fontStyle, Font.PLAIN, fontSize); // change font 
					textArea.setFont(font); // set font for text area (where user enters)
					lines.setFont(font); // set font for line numbers (on the left size)
				}
			}
		}

		private FontSize() { 

		}
	}

	private class FontStyle implements ActionListener { // font style inncer class
		public void actionPerformed(ActionEvent e) {
			String result = (String)JOptionPane.showInputDialog( // this time show input dialog because imma show options not text field 
				frame, 
				"Font type (Select font below)", 
				"Set font type", 
				JOptionPane.PLAIN_MESSAGE,
				null, 
				fontStyles, // this is the array with all the font styles
				fontStyles[0] // set default to first font style
			); 
			if (result != null) { // if they enter something 
				fontStyle = result; // update font style
				font = new Font(fontStyle, Font.PLAIN, fontSize); 
				textArea.setFont(font); 
				lines.setFont(font); 
			}
		}

		private FontStyle() { 

		}
	}

	private class Find implements ActionListener { // this is find inner class

		protected class CharacterNoExist extends Exception { // i didn't find an exception for this so i made my own
			public CharacterNoExist() { // i called it "CharacterNoExist" 
				super("Substring not Found"); // if no character exist in char array it's an exception
			}
		}

		protected int getLineFromIndex(int startIndex, String content) throws CharacterNoExist {// finds which line a char is in 
			char[] ch = content.toCharArray(); // convert string to char array
			int line = 0; // set line to 0
			for (int i = 0; i < ch.length; i++) {
				if (ch[i] == '\n') {
					line++; // when you reach \n move to new line
				}
				if (i == startIndex) { // return line where char found
					return line + 1; 
				}
			}
			throw new CharacterNoExist(); // else throw no such substring 
		}

		protected String search(String string, String subString) {
			ArrayList<String> strList = new ArrayList<String>();
			ArrayList<Integer> intList = new ArrayList<Integer>();
			int totalMatches = 0; 

			Pattern pattern = Pattern.compile(subString, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(string); 
			while (matcher.find()) {
				strList.add(matcher.group()); 
				intList.add(matcher.start()); 
			}

			totalMatches = intList.size(); 

			for (int i = 0; i < totalMatches; i++) {
				System.out.println(strList.get(i) + " " + intList.get(i)); 
			}
			String result = "No matches"; 
			if (totalMatches > 0) {
				result = "Found " + strList.size() + " match(es)\n"; 
				try {
					for (int i : intList) {
						result += "At line " + getLineFromIndex(i, string) + "\n"; 
					}
				} catch(CharacterNoExist exception) {}
				
				return result; 
			} 
			return result; 
		}

		public void actionPerformed(ActionEvent e) {
			try {
				String find = JOptionPane.showInputDialog(frame, "Find text"); 
				System.out.println(find); 
				String content = textArea.getText(); 
				JOptionPane.showMessageDialog(null, search(content, find), "Results", 1);
			} catch (Exception exception) {
				exception.printStackTrace(); 
			}
		}
	}

	private class FindReplace implements ActionListener {
		JTextField find = new JTextField(); 
		JTextField replaceWith = new JTextField();
		JComponent comps[] = new JComponent[]{
			new JLabel("Find"), find, new JLabel("Replace with"), replaceWith
		}; 

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				int result = JOptionPane.showConfirmDialog(null, comps, "Find and replace", 1);
				if (result == JOptionPane.OK_OPTION) {
					String findText = find.getText(); 
					String replaceText = replaceWith.getText(); 
					String content = getTextArea(); 
					content = content.replaceAll(Pattern.quote(findText), replaceText); 
					setTextArea(content); 
				}
			} catch (Exception exception) {
				exception.printStackTrace(); 
			}
		}
	}

	public void setLineNumbers(boolean flag) {
		if (flag) {
			lines = new JTextArea("1"); 
			lines.setEditable(false);
        	lines.setFont(font); 

			textArea.getDocument().addDocumentListener(new DocumentListener(){
				public String getText(){
					int caretPosition = textArea.getDocument().getLength();
					Element root = textArea.getDocument().getDefaultRootElement();
					String text = "1 " + System.getProperty("line.separator");
					for(int i = 2; i < root.getElementIndex( caretPosition ) + 2; i++){
						text += i + System.getProperty("line.separator");
					}
					return text;
				}
				@Override
				public void changedUpdate(DocumentEvent de) {
					lines.setText(getText());
				}

				@Override
				public void insertUpdate(DocumentEvent de) {
					lines.setText(getText());
				}

				@Override
				public void removeUpdate(DocumentEvent de) {
					lines.setText(getText());
				}
			});
			scroller.getViewport().add(textArea);
			scroller.setRowHeaderView(lines);
		}
	}

	protected void clearTextArea(String text, int start, int end) {
		textArea.replaceRange(text, start, end); 
	}


	protected String getTextArea() {
		return textArea.getText();
	}

	protected void setTextArea(String text) {
		textArea.setText(text);
	}

	private void copyFromClipboard(String toCopy) {
		Clipboard clipboard = toolkit.getSystemClipboard();
		ss = new StringSelection(toCopy); 
		clipboard.setContents(ss, null);
	}

	private void configureMenu() {
		Open openListener = new Open(); 
		open.addActionListener(openListener); 

		Save saveListener = new Save(); 
		save.addActionListener(saveListener);

		NewWindow newWindowListener = new NewWindow(); 
		newWindow.addActionListener(newWindowListener);

		CloseWindow closeListener = new CloseWindow();
		closeWindow.addActionListener(closeListener); 

		cut.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String highlighted = textArea.getSelectedText(); 
        		clearTextArea(highlighted, 0, highlighted.length()); 
        		copyFromClipboard(highlighted);
			}
		}); 	

		copy.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String highlighted = textArea.getSelectedText(); 
				copyFromClipboard(highlighted);
				System.out.println(highlighted);
			}
		}); 	

		paste.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				textArea.paste();  
			}
		}); 

		selectAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				textArea.selectAll(); 
			}
		}); 		

		FontSize fontSizeListener = new FontSize();
		fontSizeItem.addActionListener(fontSizeListener);

		FontStyle fontStyleListener = new FontStyle();
		fontStyleItem.addActionListener(fontStyleListener);

		SaveAs saveAsDialog = new SaveAs();
		saveAs.addActionListener(saveAsDialog); 		

		Find findDialog = new Find(); 
		find.addActionListener(findDialog); 

		FindReplace findReplaceDialog = new FindReplace();
		findReplace.addActionListener(findReplaceDialog);
	}	

	private void addMenu() {
		menu = new JMenuBar(); 

		file = new JMenu("File");
		edit = new JMenu("Edit"); 
		more = new JMenu("More"); 

		save = new JMenuItem("Save"); 
		saveAs = new JMenuItem("Save As"); 
		open = new JMenuItem("Open"); 

		JMenuItem[] fileItems = {save, saveAs, open}; 

		for (JMenuItem item : fileItems) {
			file.add(item); 
		}

		cut = new JMenuItem("Cut"); 
		copy = new JMenuItem("Copy"); 
		paste = new JMenuItem("Paste"); 
		selectAll = new JMenuItem("Select All");
		fontSizeItem = new JMenuItem("Set Font Size");
		fontStyleItem = new JMenuItem("Set Font Style");

		JMenuItem[] editItems = {cut, copy, paste, selectAll, fontSizeItem, fontStyleItem}; 

		for (JMenuItem item : editItems) {
			edit.add(item); 
		}

		findReplace = new JMenuItem("Find and Replace");
		find = new JMenuItem("Find");
		newWindow = new JMenuItem("New Window");
		closeWindow = new JMenuItem("Close Window");

		JMenuItem[] moreItems = {find, findReplace, newWindow, closeWindow}; 

		for (JMenuItem item : moreItems) {
			more.add(item); 
		}

		menu.add(file); 
		menu.add(edit); 
		menu.add(more); 

		frame.setJMenuBar(menu); 
	}

	private void addTextBox() {
		fontStyle = fontStyles[0]; 
		fontSize = 20; 
		font = new Font(fontStyle, Font.PLAIN, fontSize); 
		textArea.setBounds(0, 0, screenWidth, screenHeight); 
        textArea.setFont(font); 
        textArea.setTabSize(tabWidth); 
		scroller = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroller.setPreferredSize(new Dimension(100,100));
        scroller.setEnabled(true);
        scroller.setVisible(true);
        frame.add(scroller); 
	}

	private String extractTextFromFile(File file) throws IOException {
		Scanner scanner = new Scanner(file); 
		String data = ""; 
		while (scanner.hasNextLine()) {
			data += scanner.nextLine(); 
			data += "\n"; 
		}
		return data; 
	}

	private void addFontsToArray() throws IOException {
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Font[] allFonts = ge.getAllFonts();
		int i = 0;
		fontStyles = new String[allFonts.length];  
		for (Font font : allFonts) {
			fontStyles[i] = font.getFontName(Locale.US); 
			i++; 
		}
		fontStyle = fontStyles[0]; 	
	}

	private void init() throws IOException {
		addFontsToArray();
		toolkit = Toolkit.getDefaultToolkit();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(screenWidth, screenHeight); 

        addMenu(); 
        configureMenu(); 
        addTextBox(); 
        setLineNumbers(true);

        textArea.insert(extractTextFromFile(new File("sample.txt")), 0); 
        frame.setTitle("Notepad -- sample.txt"); 

        frame.setResizable(true);      
        frame.setVisible(true);
	}

	public void run() {
		try {
			init(); 	
		} catch (Exception exception) {

		}
	}

	public Notepad() {

	}
}