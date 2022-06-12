import javax.swing.*; 
import java.awt.Toolkit; 
import java.awt.Dimension; 
import java.awt.Font; 
import java.awt.event.WindowEvent; 
import java.io.File; 	
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner; 
import java.io.PrintWriter; 
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import java.util.Scanner; 
import java.io.PrintWriter; 


public final class Notepad extends Thread implements Runnable {
	private JFrame frame = new JFrame("Notepad"); 	

	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private int screenWidth = (int)screenSize.getWidth(); 
	private int screenHeight = (int)screenSize.getHeight(); 

	private JTextArea textArea; 
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

	private JMenuItem print;
	private JMenuItem find; 
	private JMenuItem newWindow; 
	private JMenuItem closeWindow; 

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
			kill(); 
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
					clearTextArea(); 
					Scanner scanner = new Scanner(file); 
					while (scanner.hasNextLine()) {	
						data += scanner.nextLine();
					}	
					setTextArea(data); 
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

	protected void clearTextArea() {
		textArea.setText(""); 
	}

	protected void setTextArea(String data) {
		textArea.setText(data); 
	}

	protected String getTextArea() {
		return textArea.getText();
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
        		clearTextArea(); 
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

		SaveAs saveAsDialog = new SaveAs();
		saveAs.addActionListener(saveAsDialog); 		

	}

	private void addMenu() {
		menu = new JMenuBar(); 

		file = new JMenu("File");
		edit = new JMenu("Edit"); 
		more = new JMenu("More"); 

		save = new JMenuItem("Save"); 
		saveAs = new JMenuItem("Save as"); 
		open = new JMenuItem("open"); 

		JMenuItem[] fileItems = {save, saveAs, open}; 

		for (JMenuItem item : fileItems) {
			file.add(item); 
		}

		cut = new JMenuItem("cut"); 
		copy = new JMenuItem("copy"); 
		paste = new JMenuItem("paste"); 
		selectAll = new JMenuItem("selectAll");

		JMenuItem[] editItems = {cut, copy, paste, selectAll}; 

		for (JMenuItem item : editItems) {
			edit.add(item); 
		}

		print = new JMenuItem("print");
		find = new JMenuItem("find");
		newWindow = new JMenuItem("New window");
		closeWindow = new JMenuItem("Close window");

		JMenuItem[] moreItems = {print, find, newWindow, closeWindow}; 

		for (JMenuItem item : moreItems) {
			more.add(item); 
		}

		menu.add(file); 
		menu.add(edit); 
		menu.add(more); 

		frame.setJMenuBar(menu); 
	}

	private void addTextBox() {
		textArea = new JTextArea();
		textArea.setBounds(0, 0, screenWidth, screenHeight); 
        Font font = new Font("Lucida Console", Font.PLAIN, 20);
        textArea.setFont(font); 
        scroller = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        frame.add(scroller); 
        frame.add(textArea); 
        textArea.insert("Sample text", 0); 
	}

	private void init() {
		toolkit = Toolkit.getDefaultToolkit();
		JFrame.setDefaultLookAndFeelDecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(screenWidth, screenHeight); 

        addMenu(); 
        configureMenu(); 
        addTextBox(); 

        frame.setLayout(null);  	
        frame.setResizable(true);      
        frame.setVisible(true);
	}

	protected void kill() {
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}

	public void run() {
		init(); 
	}
}