/*	
 *  Copyright (c) 2011-@year@. The GUITAR group at the University of Maryland. Names of owners of this group may
 *  be obtained by sending an e-mail to atif@cs.umd.edu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 *  documentation files (the "Software"), to deal in the Software without restriction, including without 
 *  limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *	the Software, and to permit persons to whom the Software is furnished to do so, subject to the following 
 *	conditions:
 * 
 *	The above copyright notice and this permission notice shall be included in all copies or substantial 
 *	portions of the Software.
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 *	LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO 
 *	EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
 *	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
 *	THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
 */
package edu.umd.cs.guitar.ripper.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.umd.cs.guitar.model.SitarWindow;
import edu.umd.cs.guitar.model.swtwidgets.SitarWidget;
import edu.umd.cs.guitar.model.swtwidgets.SitarWidgetFactory;


public class SitarWidgetTest {

	private Display display;
	private SitarWidgetFactory factory;
	
	@Before
	public void setUp() {
		factory = SitarWidgetFactory.INSTANCE;
		if (display == null || display.isDisposed()) {
			display = new Display();
		}
	}
	
	@After
	public void tearDown() {
		display.dispose();
	}
	
	@Test
	public void testIsTerminal() {
		final Shell shell = new Shell(display);
		Button button = new Button(shell, SWT.PUSH);
		
		SitarWidget swtButton = factory.newSWTWidget(button, new SitarWindow(shell));
		
		assertFalse(swtButton.isTerminal());
		// make sure we didn't actually close the shell
		assertFalse(shell.isDisposed());
		
		button.setText("Quit");
		assertFalse(swtButton.isTerminal());
		assertFalse(shell.isDisposed());
		
		button.setText("Close");
		assertFalse(swtButton.isTerminal());
		assertFalse(shell.isDisposed());
		
		button.setText("Exit");
		assertFalse(swtButton.isTerminal());
		assertFalse(shell.isDisposed());
		
		SelectionAdapter closeShell = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		}; 
		
		button.addSelectionListener(closeShell);
		assertTrue(swtButton.isTerminal());
		assertFalse(shell.isDisposed());
		
		button.removeSelectionListener(closeShell);
		assertFalse(swtButton.isTerminal());
		assertFalse(shell.isDisposed());
		
		// test that existing close listeners aren't notified
		final boolean[] listenerNotified = { false };
		
		ShellListener shellListener = new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				listenerNotified[0] = true;
			}
		};
		shell.addShellListener(shellListener);
		
		assertFalse(swtButton.isTerminal());
		assertFalse(listenerNotified[0]);
		
		shell.open(); // just to be sure
		shell.close();
		
		// make sure existing listeners are notified if real close happens
		assertTrue(listenerNotified[0]);
		
	}
		
}
