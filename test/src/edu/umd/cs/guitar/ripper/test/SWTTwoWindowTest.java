package edu.umd.cs.guitar.ripper.test;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class SWTTwoWindowTest {
	
	public SWTTwoWindowTest(Display display){
		Shell shell1 = new Shell();
		Shell shell2 = new Shell();
		
		shell1.setText("Window 1");
		shell2.setText("Window 2");
		
		shell1.setSize(250,250);
		shell2.setSize(300, 250);
		
		shell1.open();
		shell2.open();
		
		while(!shell1.isDisposed() || !shell2.isDisposed()){
			if(!display.readAndDispatch())
				display.sleep();
		}
	}
	
	public static void main (String [] args){
		Display display = new Display();
		new SWTTwoWindowTest(display);
		display.dispose();
	}
}
