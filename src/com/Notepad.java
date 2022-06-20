package com;

// import all the libraries i need
import javax.swing.*; // there's too many so i just used *
import java.awt.Toolkit; // for cut copy paste
import java.awt.Dimension; // to get size of user screen
import java.awt.Font; // to change font
import java.awt.datatransfer.Clipboard; // for cut copy paste
import java.awt.datatransfer.StringSelection;
import java.awt.GraphicsEnvironment; // to set graphics
import java.awt.event.WindowEvent; // to close window
import java.awt.event.ActionEvent; // to control what happens when button pressed
import java.awt.event.ActionListener; // to contorl what happens when button pressed
import java.io.File; // to work with files
import java.io.IOException; // exception in case error while reading file
import java.io.PrintWriter; // to write to files
import java.lang.Exception; // to define my own exception
import javax.swing.JFileChooser; // to open/save as files
import javax.swing.event.DocumentEvent; // to contorl jDialog
import javax.swing.event.DocumentListener; // to work with files
import javax.swing.text.Element; // for pattern matching
import java.util.regex.Matcher; // for pattern matching
import java.util.regex.Pattern; // for pattern matching
import java.util.ArrayList;
import java.util.Scanner; 
import java.util.Locale; // to get user location to set language

// this is the notepad class which inherits runnable (for multithreading) and sets up the gui
public final class Notepad implements Runnable {
	// create the application frame
	private final JFrame frame = new JFrame("Notepad");

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
	private JMenuItem textToSpeechItem; // the speak aloud function
	private JMenuItem speechToTextItem;
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
					System.out.println("Exception"); // print out message
				} 
			}
		}

		private Save() { // this is a private constructor so Save() cannot be called  outside  i had to override the default
			
		}
	}

	private class Open implements ActionListener { // open inner class
		private JFileChooser chooser; // this is to pick a file from the user hard disk (kind of like file explorer)
		private String data = ""; // the file data

		public void actionPerformed(ActionEvent e) { // see line 83
			chooser = new JFileChooser(); // create an instance of this file explorer object
			int result = chooser.showOpenDialog(null);  // show the "open file" prompt
			if (result == JFileChooser.APPROVE_OPTION) { // if user picked file and clicked ok
				try {
					File file = chooser.getSelectedFile(); // open up file
					currentFile = file; // set currentFile to chosen file
					textArea.setText(""); // set the text area on Notepad to file data
					String data = extractTextFromFile(file); 
					textArea.setText(data); // change the title of the app
					frame.setTitle("Notepad -- " + file.getName()); 
				} catch (Exception exception) { // in case of any error while opening file
					System.out.println("Exception");
				}
			}
		}

		public String getData() { // get the file data
			return data;
		} // accessor method

		private Open() { // this is a private constructor so Open() cannot be called outside i had to override the default
			// just so no one tries to instantiate this class
		}
	}

	private class SaveAs implements ActionListener { // save as inner class 
		private JFileChooser chooser; // similar to open file
		private String path; // the path of the file

		public void actionPerformed(ActionEvent e) { // on button click
			chooser = new JFileChooser(); // see line 117
			int result = chooser.showSaveDialog(null); // kind of like line 118 but show save as instead of open
			if (result == JFileChooser.APPROVE_OPTION) { // if user clicked save as
				try {
					currentFile = chooser.getSelectedFile(); // save file as name
					frame.setTitle("Notepad -- " + currentFile.getName()); // update name
					PrintWriter w = new PrintWriter(currentFile); // use print writer to write to file
					w.println(getTextArea()); // write whatever is in the text box user has typed in
					w.close(); // close this resource
				} catch (Exception exception) { // in case of error
					System.out.println("Exception");
				} 
			}
		}

		private SaveAs() { // this is a private constructor so SaveAs() cannot be called outside i had to override the default

		}
	}

	private class FontSize implements ActionListener { // font size inner class 
		public void actionPerformed(ActionEvent e) { // on button press
			JTextField fontSizeField = new JTextField(); // create a text field for font size
			JLabel label = new JLabel("Font size (enter an integer between 1 and 100)"); // create object to prompt user to do what is says

			JComponent inputs[] = new JComponent[] { // basically using a JComponent array to hold objects that extend JComponent
				label, 
				fontSizeField
			}; 
			int result = JOptionPane.showConfirmDialog(null, inputs, "Set font", JOptionPane.YES_NO_CANCEL_OPTION); // prompt user to set font
			if (result == JOptionPane.OK_OPTION) { // if user enters number and presses ok 
				fontSize = Integer.valueOf(fontSizeField.getText()); // set font size
				if (fontSize > 0 && fontSize < 101) { // make sure valid font size
					font = new Font(fontStyle, Font.PLAIN, fontSize); // change font 
					textArea.setFont(font); // set font for text area (where user enters)
					lines.setFont(font); // set font for line numbers (on the left size)
				}
			}
		}

		private FontSize() { // this is a private constructor so FontSize() cannot be called outside  i had to override the default

		}
	}

	private class FontStyle implements ActionListener { // font style inncer class
		public void actionPerformed(ActionEvent e) { // on button press
			String result = (String)JOptionPane.showInputDialog( // this time show input dialog because imma show options not text field 
				frame,  // the app frame
				"Font type (Select font below)", // prompt
				"Set font type",  // prompt
				JOptionPane.PLAIN_MESSAGE, // nothing fancy
				null, 
				fontStyles, // this is the array with all the font styles
				fontStyles[0] // set default to first font style
			); 
			if (result != null) { // if they enter something 
				fontStyle = result; // update font style
				font = new Font(fontStyle, Font.PLAIN, fontSize); // set the font
				textArea.setFont(font); // set text box font
				lines.setFont(font); // set lines (on left side) font
			}
		}

		private FontStyle() { // this is a private constructor so FontStyle() cannot be called  outside i had to override the default

		}
	}

	private class TextToSpeechItem implements ActionListener { // textToSpeech inner class
  		public void actionPerformed(ActionEvent e) { // on button click
  			JLabel label = new JLabel("Read text aloud?"); // prompt
			JComponent comps[] = new JComponent[]{label}; // using a jcompoenent to hold a subclass
			int result = JOptionPane.showConfirmDialog(null, comps, "Read aloud", JOptionPane.YES_NO_CANCEL_OPTION);
			// if user enters ok
			if (result == JOptionPane.OK_OPTION) {
				TextToSpeech.tts(getTextArea()); // read whats on the screen
			}
		}
	}

	private class SpeechToTextItem implements ActionListener { // speech to text inner class
  		private void start() { // start method (to begin speech to text)
			SpeechToText.init(); // call the static init method
		}

		private void stop() { // stop method (to end speech to text)
			SpeechToText.kill(); // call the static kill method
			addToTextArea(SpeechToText.get()); // add to text box whatever user said
		}

		@Override
		public void actionPerformed(ActionEvent e) { // on button press
			JLabel label = new JLabel("Convert speech to text"); // show prompt
			JComponent comps[] = new JComponent[]{label}; // using a jCompoenent to hold a sub class
			int result = JOptionPane.showConfirmDialog(null, comps, "Start speaking", JOptionPane.YES_NO_CANCEL_OPTION);
			if (result == JOptionPane.YES_NO_OPTION) { // if user clicks yes
				System.out.println("start"); // for debugging
				start(); /// start speech to text
			} else { // if user click no
				System.out.println("stop"); // for debugging
				stop(); // stop speech to text
			}
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
			for (int i = 0; i < ch.length; i++) { // loop the length of string
				if (ch[i] == '\n') { // on reach new line
					line++; // move to new line
				}
				if (i == startIndex) { // return line where char found
					return line + 1; // return the line number (should offset by 1 cuz line number is never 0)
				}
			}
			throw new CharacterNoExist(); // else throw exception
		}

		protected String search(String string, String subString) { // search method to find substring in string
			ArrayList<String> strList = new ArrayList<String>(); // create an arrayList for matches
			ArrayList<Integer> intList = new ArrayList<Integer>(); // create an intLIst for line numbers
			int totalMatches = 0; // variable for totalMatches is currently 0

			Pattern pattern = Pattern.compile(subString, Pattern.CASE_INSENSITIVE); // create pattern object to hold substring
			Matcher matcher = pattern.matcher(string); // create matcher object to hold string
			while (matcher.find()) { // as long as match is found
				strList.add(matcher.group()); // add that word to list
				intList.add(matcher.start());  // add line number
			}

			totalMatches = intList.size(); // set totalMatches to the size of the arrayList
			// (either one can be used since both have same size)

			for (int i = 0; i < totalMatches; i++) { // loop for the length of the arrayLists
				System.out.println(strList.get(i) + " " + intList.get(i));  // print out for debugging
			}
			String result = "No matches"; // set result to no matches for now
			if (totalMatches > 0) { // if there is at least 1 match
				result = "Found " + strList.size() + " match(es)\n"; // update result value by setting it to word with substring
				try {
					for (int i : intList) { // loop length of int list
						result += "At line " + getLineFromIndex(i, string) + "\n"; // update result value by setting it to line number
					}
				} catch(CharacterNoExist exception) {} // in case of error
				
				return result; // return what was found
			} 
			return result; // return what was found
		}

		public void actionPerformed(ActionEvent e) { // on button click
			try {
				String find = JOptionPane.showInputDialog(frame, "Find text"); // variable to hold string
				System.out.println(find); // debug
				String content = textArea.getText(); // what user typed
				JOptionPane.showMessageDialog(null, search(content, find), "Results", 1); // show search dialog
			} catch (Exception exception) {
				exception.printStackTrace(); // in case of error print message
			}
		}
	}

	private class FindReplace implements ActionListener { // find and replace function
		JTextField find = new JTextField(); // for user to enter what they want to find
		JTextField replaceWith = new JTextField(); // for user to enter what they want to replace it with
		JComponent comps[] = new JComponent[]{ // using a jcompoenent to hold jlabels and jtexfields which are subclasses
			new JLabel("Find"), find, new JLabel("Replace with"), replaceWith
		}; 

		@Override
		public void actionPerformed(ActionEvent e) { // on button click
			try {
				// show find and replace message
				int result = JOptionPane.showConfirmDialog(null, comps, "Find and replace", 1);
				// if user clicks yes
				if (result == JOptionPane.OK_OPTION) {
					// store user input in variables
					String findText = find.getText(); 
					String replaceText = replaceWith.getText(); 
					String content = getTextArea();
					// use Java's matching algorithm to find and replace
					content = content.replaceAll(Pattern.quote(findText), replaceText); 
					setTextArea(content);  // update text area value
				}
			} catch (Exception exception) { // in case of error
				exception.printStackTrace(); // print message
			}
		}
	}

	public void setLineNumbers(boolean flag) { // set left side line numbers
		if (flag) { // switch to enable/disable line numbers
			lines = new JTextArea("1"); // set first line
			lines.setEditable(false); // user cannot change
        	lines.setFont(font); // set font to what user has chosen

			textArea.getDocument().addDocumentListener(new DocumentListener(){ // add listener
				public String getText(){ // accessor method
					int caretPosition = textArea.getDocument().getLength(); // get size of doc
					Element root = textArea.getDocument().getDefaultRootElement();
					String text = "1 " + System.getProperty("line.separator"); // set first text to 1
					for(int i = 2; i < root.getElementIndex( caretPosition ) + 2; i++){
						text += i + System.getProperty("line.separator"); // use seperators to updat text
					}
					return text; // return value
				}
				@Override // abstract method must be overriden
				public void changedUpdate(DocumentEvent de) {
					lines.setText(getText());
				} // on change text

				@Override // abstract method must be overriden
				public void insertUpdate(DocumentEvent de) {
					lines.setText(getText());
				} // on update text

				@Override // abstract method must be overriden
				public void removeUpdate(DocumentEvent de) {
					lines.setText(getText());
				} // on remove text
			});
			scroller.getViewport().add(textArea); // set this beside text area
			scroller.setRowHeaderView(lines); // add this to app
		}
	}

	// cleat what's in user text box
	protected void clearTextArea(String text, int start, int end) {
		textArea.replaceRange(text, start, end); 
	}

	// accessor method to get what's in user text box
	protected String getTextArea() {
		return textArea.getText();
	}

	// change what's in user text box completely
	protected void setTextArea(String text) {
		textArea.setText(text);
	}

	// add to what's in user text box
	protected void addToTextArea(String text) {
  		textArea.append(text);
	}

	// copt method
	private void copyFromClipboard(String toCopy) {
		Clipboard clipboard = toolkit.getSystemClipboard(); // get OS clipboard
		ss = new StringSelection(toCopy); // obj to copy text
		clipboard.setContents(ss, null); // set OS clipboard
	}

	private void configureMenu() { // set up top menu
		Open openListener = new Open(); // instantiate open obj
		open.addActionListener(openListener); // add listener so it knows when user presses button

		Save saveListener = new Save(); // instantiate save obj
		save.addActionListener(saveListener);  // add listener so it knows when user presses button

		NewWindow newWindowListener = new NewWindow(); // instantiate new window obj
		newWindow.addActionListener(newWindowListener); // add listener so it knows when user presses button

		CloseWindow closeListener = new CloseWindow(); // instantiate close window ibj
		closeWindow.addActionListener(closeListener);  // add listener so it knows when user presses button

		// add listener so it knows when user presses button
		cut.addActionListener(e -> {  // user lambda function to override onButtonPressed()
			String highlighted = textArea.getSelectedText(); // get highlighed text
			clearTextArea(highlighted, 0, highlighted.length()); // clear what's on text box
			copyFromClipboard(highlighted); // update text box value with clipboard vlaue
		});

		// add listener so it knows when user presses button
		copy.addActionListener(e -> { // use lambda function to override onButtonPressed()
			String highlighted = textArea.getSelectedText(); // get highlighed text
			copyFromClipboard(highlighted); // call copy method
			System.out.println(highlighted); // for debugging
		});

		paste.addActionListener(e -> textArea.paste()); // add listener so it knows when user presses button using lambda
		// to override

		selectAll.addActionListener(e -> textArea.selectAll()); // add listener so it knows when user presses button using lambda
		// to override

		FontSize fontSizeListener = new FontSize(); // instantiate fontSize obj
		fontSizeItem.addActionListener(fontSizeListener); // add listener so it knows when user presses button using lambda
		// to override

		FontStyle fontStyleListener = new FontStyle(); // instantiate fontStyle obj
		fontStyleItem.addActionListener(fontStyleListener); // add listener so it knows when user presses button using lambda
		// to override

		TextToSpeechItem textToSpeechItemListener = new TextToSpeechItem();// instantiate tts obj
		textToSpeechItem.addActionListener(textToSpeechItemListener); // add listener so it knows when user presses button using lambda
		// to override

		SpeechToTextItem speechToTextItemListener = new SpeechToTextItem(); // instantiate stt obj
		speechToTextItem.addActionListener(speechToTextItemListener);// add listener so it knows when user presses button using lambda
		// to override

		SaveAs saveAsDialog = new SaveAs(); // instantiate save as obj
		saveAs.addActionListener(saveAsDialog); // add listener so it knows when user presses button using lambda
		// to override

		Find findDialog = new Find(); // instantiate font obj
		find.addActionListener(findDialog); // add listener so it knows when user presses button using lambda
		// to override

		FindReplace findReplaceDialog = new FindReplace();// instantiate findREplace obj
		findReplace.addActionListener(findReplaceDialog);// add listener so it knows when user presses button using lambda
		// to override
	}	

	private void addMenu() { // add items to menu
		menu = new JMenuBar();  // instantiate menuBar

		file = new JMenu("File"); // instantiate file Menu
		edit = new JMenu("Edit"); // instantiate edit menu
		more = new JMenu("More");  // instantiate more menu

		// submenu's that will go inside file
		save = new JMenuItem("Save");  // instantite save menu
		saveAs = new JMenuItem("Save As"); // instantiate save as menu
		open = new JMenuItem("Open"); // instantiate open menu

		JMenuItem[] fileItems = {save, saveAs, open}; // create file items obj

		for (JMenuItem item : fileItems) { // add the three to file
			file.add(item); 
		}

		// submenu's that will go inside edit
		cut = new JMenuItem("Cut");  // instantiate cut submenu
		copy = new JMenuItem("Copy");  // insatantiate copy submenu
		paste = new JMenuItem("Paste");  // instantiate paste submenu
		selectAll = new JMenuItem("Select All"); // instantiate selectAll submenu
		fontSizeItem = new JMenuItem("Set Font Size"); // instantiate fontSize submenu
		fontStyleItem = new JMenuItem("Set Font Style"); // instantiate fontStyle submenu

		JMenuItem[] editItems = {cut, copy, paste, selectAll, fontSizeItem, fontStyleItem}; // create edit items obj

		for (JMenuItem item : editItems) { // add the 6 to edit
			edit.add(item); 
		}

		// submenu's that will go inside more
		findReplace = new JMenuItem("Find and Replace"); // instantiate findReplace submenu
		find = new JMenuItem("Find"); // instantiate find submenu
		newWindow = new JMenuItem("New Window"); // instantiate new window submenu
		closeWindow = new JMenuItem("Close Window"); // instantiate close window submenu
		textToSpeechItem = new JMenuItem("Text to speech"); // instantiate tts submenu
		speechToTextItem = new JMenuItem("Speech to text"); // instantiate stt submenu

		JMenuItem[] moreItems = {find, findReplace, newWindow, closeWindow, textToSpeechItem, speechToTextItem}; // creat moreItems obj

		for (JMenuItem item : moreItems) { // add the 6 to more
			more.add(item);
		}

		menu.add(file); // add file menu to menubar
		menu.add(edit);  // add edit menu to menubar
		menu.add(more); // add more menu to menubar

		frame.setJMenuBar(menu); // set the menubar to menu
	}

	// method to add text box
	private void addTextBox() {
		fontStyle = fontStyles[0]; // set font style to first one in array
		fontSize = 20;  // set font size to 20
		font = new Font(fontStyle, Font.PLAIN, fontSize); // create font obj
		textArea.setBounds(0, 0, screenWidth, screenHeight); // set size of text area
        textArea.setFont(font); // set font
        textArea.setTabSize(tabWidth); // set tab size (important if you're going to use this app to code)
		// create scroll bar and set  size
		scroller = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroller.setPreferredSize(new Dimension(100,100));
        // make the thing visible and add it to frame
        scroller.setEnabled(true);
        scroller.setVisible(true);
        frame.add(scroller); 
	}

	// method to retrieve the text from a file
	private String extractTextFromFile(File file) throws IOException {
  		// create scanner and pass in file
		Scanner scanner = new Scanner(file);
		// basically a string but good for concatenation
		StringBuilder data = new StringBuilder();
		// iterate through file
		while (scanner.hasNextLine()) {
			// concatenate line in file to current
			data.append(scanner.nextLine());
			// add new line char
			data.append("\n");
		}
		// convert stringBuilder to string
		return data.toString();
	}

	// method to set the possible fonts
	private void addFontsToArray() {
  		// create a graphics environment obj
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		// get all fonts for this OS
		Font[] allFonts = ge.getAllFonts();

		// add possible fonts to fontlist
		int i = 0; // counter variable
		fontStyles = new String[allFonts.length];  
		for (Font font : allFonts) {
			// assume user stationed in US/Canada so get all american fonts
			fontStyles[i] = font.getFontName(Locale.US); 
			i++; 
		}
		// set fontstyle to first one
		fontStyle = fontStyles[0]; 	
	}

	// init method which runs everytime new window made
	private void init() throws IOException {
  		// set possible fonts
		addFontsToArray();
		// set toolkit obj
		toolkit = Toolkit.getDefaultToolkit();
		// try to load app icon
		try {
			ImageIcon img = new ImageIcon("/img/notepad.png");
			// set icon
			frame.setIconImage(img.getImage());
		} catch (Exception ioe) {
			// on error print error
			ioe.printStackTrace();
		}
		// on close button clicked close this window
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// set size of app
        frame.setSize(screenWidth, screenHeight); 

        // add the menu bar
        addMenu();
        // setup menubar
        configureMenu();
        // add textbox
        addTextBox();
        // add line numbers
        setLineNumbers(true);

        // insert sample text into textbox on first line
 		textArea.insert("This is a test", 0);
 		// set title
        frame.setTitle("Notepad -- sample.txt"); 

        // make app resizable and set visibility
        frame.setResizable(true);      
        frame.setVisible(true);
	}

	// abstract method must be overriden
	@Override
	public void run() {
  		// call init method
  		try {
			init();
		} catch (IOException ioe) {

		}
	}

	// constructor
	public Notepad() {

	}
}