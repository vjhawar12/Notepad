import javax.swing.*; 
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

public final class Notepad implements Runnable {
	private JFrame frame = new JFrame("Notepad"); 	

	private int tabWidth = 2; 
	private int fontSize; 
	private String fontStyle;  
	private String[] fontStyles; 
	private Font font;
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private int screenWidth = (int)screenSize.getWidth(); 
	private int screenHeight = (int)screenSize.getHeight(); 

	private JTextArea textArea = new JTextArea(); 
	private JTextArea lines; 
	private JMenuBar menu; 

	private JMenu file; 
	private JMenu edit; 
	private JMenu more; 

	private JMenuItem save; 
	private JMenuItem saveAs; 
	private JMenuItem open; 
	
	private JMenuItem cut; 
	private JMenuItem copy; 
	private JMenuItem paste; 
	private JMenuItem selectAll; 	

	private JMenuItem fontSizeItem;
	private JMenuItem fontStyleItem;

	private JMenuItem findReplace;
	private JMenuItem find; 
	private JMenuItem newWindow; 
	private JMenuItem closeWindow; 

	private JPanel panel; 
	private JScrollPane scroller; 	

	public File currentFile;

	private StringSelection ss; 
  	private Toolkit toolkit; 

  	private class NewWindow implements ActionListener  {
		public void actionPerformed(ActionEvent e) {
	        Main.newApp(); 
		}
	}

	private class CloseWindow implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		}
	}

	private class Save implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (currentFile != null) {
				try {
					PrintWriter w = new PrintWriter(currentFile); 	
					w.println(getTextArea()); 
					w.close(); 
				} catch (Exception exception) {
					System.out.println("Exception");
				} 
			}
		}

		private Save() {
			
		}
	}

	private class Open implements ActionListener{
		private JFileChooser chooser; 
		private String data = ""; 

		public void actionPerformed(ActionEvent e) {
			chooser = new JFileChooser();
			int result = chooser.showOpenDialog(null); 
			if (result == JFileChooser.APPROVE_OPTION) {
				try {
					File file = chooser.getSelectedFile();
					currentFile = file; 
					textArea.setText(""); 
					String data = extractTextFromFile(file); 
					textArea.setText(data); 	
					frame.setTitle("Notepad -- " + file.getName()); 
				} catch (Exception exception) {

				}
			}
		}

		public String getData() {
			return data; 
		}

		private Open() {

		}
	}

	private class SaveAs implements ActionListener {
		private JFileChooser chooser;
		private String path; 

		public void actionPerformed(ActionEvent e) {
			chooser = new JFileChooser();
			int result = chooser.showSaveDialog(null); 
			if (result == JFileChooser.APPROVE_OPTION) {
				try {
					currentFile = chooser.getSelectedFile(); 
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

	private class FontSize implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JTextField fontSizeField = new JTextField();
			JLabel label = new JLabel("Font size (enter an integer between 1 and 100)");

			JComponent inputs[] = new JComponent[] {
				label, 
				fontSizeField
			}; 
			int result = JOptionPane.showConfirmDialog(null, inputs, "Set font", 1);
			if (result == JOptionPane.OK_OPTION) {
				fontSize = Integer.valueOf(fontSizeField.getText());
				if (fontSize > 0) {
					font = new Font(fontStyle, Font.PLAIN, fontSize); 
					textArea.setFont(font); 
					lines.setFont(font); 
				}
			}
		}

		private FontSize() {

		}
	}

	private class FontStyle implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String result = (String)JOptionPane.showInputDialog(
				frame, 
				"Font type (Select font below)", 
				"Set font type", 
				JOptionPane.PLAIN_MESSAGE,
				null, 
				fontStyles,
				fontStyles[0]
			); 
			if (result != null) {
				fontStyle = result; 
				font = new Font(fontStyle, Font.PLAIN, fontSize); 
				textArea.setFont(font); 
				lines.setFont(font); 
			}
		}

		private FontStyle() {

		}
	}

	private class Find implements ActionListener {

		protected class NoSuchSubstring extends Exception {
			public NoSuchSubstring() {
				super("Substring not Found"); 
			}
		}

		protected int getLineFromIndex(int startIndex, String content) throws NoSuchSubstring {
			char[] ch = content.toCharArray(); 
			int line = 0; 
			for (int i = 0; i < ch.length; i++) {
				if (ch[i] == '\n') {
					line++; 
				}
				if (i == startIndex) {
					return line + 1; 
				}
			}
			throw new NoSuchSubstring(); 
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
				} catch(NoSuchSubstring exception) {}
				
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