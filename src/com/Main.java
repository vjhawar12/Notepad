package com;

// Vedant Jhawar
// June 14 2022
// Grade 11 Computer science
// Notepad project
// This is a notepad application that works on unix and windows.
// You can type up a document and save it to your hard drive
// Or open up an existing document and edit it and save it
// You can use find and replace, search, cut, copy, paste, set font, and use text to speech to listen back and
// to help you with editing your document
// I made this using Java and it's swing library for GUI. For text to speech i used freetts for java. i tried using sphinx4 for speech to text
// but unfortunately it hasn't worked yet.

// import libs
import java.util.ArrayList; // to track threads
import javax.swing.SwingUtilities; // to use invokeLater method

public class Main { // driver class
	// list of all threads running
	private static ArrayList<Thread> applications = new ArrayList<Thread>(); 

	// method to create a new window
	protected static void newApp() {
		// basically just add a new instance of notepad to the existing pool of threads
		Notepad _app = new Notepad(); 
		Thread app = new Thread(_app); 
		app.start(); 
		applications.add(app);
	}

	public static void main(String[] args) { // driver method
		// don't put the instance on the main method instead put it on invokeLater which relieves the load basically
		SwingUtilities.invokeLater(Main::newApp);
	}

	// this is really important so Main() isn't accidentally called outside this class
	private Main() {
		
	}
}