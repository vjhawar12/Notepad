import java.util.ArrayList; 
import javax.swing.SwingUtilities; 

public class Main {
	private static ArrayList<Thread> applications = new ArrayList<Thread>(); 

	protected static void newApp() {
		Thread app = new Thread(new Notepad()); 
		app.start(); 
		applications.add(app); 
	}

	public static void main(String[] args) throws Exception {
		newApp(); 
	}

	private Main() {
		
	}
}