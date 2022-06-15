// Vedant Jhawar 
// June 14 2022
// Notepad project
// This is a notepad application that works on unix and windows.
// You can type up a document and save it to your hard drive
// Or open up an existing document and edit it and save it
// You can use find and replace, search, cut, copy, paste, set font style, and set font size features
// to help you with editing your document
// I made this using Java and it's swing library for GUI. 


import java.util.ArrayList; 
import javax.swing.SwingUtilities; 

public class Main {
	private static ArrayList<Thread> applications = new ArrayList<Thread>(); 

	protected static void newApp() {
		Notepad _app = new Notepad(); 
		Thread app = new Thread(_app); 
		app.start(); 
		applications.add(app); 
	}

	public static void main(String[] args) throws Exception {
		newApp(); 
	}

	private Main() {
		
	}
}