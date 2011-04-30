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
package edu.umd.cs.guitar.replayer.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.umd.cs.guitar.replayer.SitarReplayer;
import edu.umd.cs.guitar.replayer.SitarReplayerConfiguration;
import edu.umd.cs.guitar.ripper.SitarRunner;
import edu.umd.cs.guitar.ripper.test.aut.SWTBasicApp;
import edu.umd.cs.guitar.ripper.test.aut.SWTHelloWorldApp;
import edu.umd.cs.guitar.ripper.test.aut.SWTListApp;
import edu.umd.cs.guitar.ripper.test.aut.SWTMultiWindowDynamicApp;
import edu.umd.cs.guitar.ripper.test.aut.SWTTabFolderApp;
import edu.umd.cs.guitar.util.GUITARLog;


public class SitarReplayerTest {
	
	private static String replay(Class<?> clazz, File testCase) {
		SitarReplayerConfiguration config = new SitarReplayerConfiguration();		
		config.setMainClass(clazz.getName());
		
		String autName = clazz.getSimpleName();
				
		config.setGuiFile("guistructures/" + autName + ".GUI.xml");
		config.setEfgFile("efgs/" + autName + ".EFG.xml");
		config.setTestcase(testCase.getAbsolutePath());
		config.setGuiStateFile("testoutput.STATE.xml");
		
		SitarReplayer replayer = new SitarReplayer(config);
		new SitarRunner(replayer).run();
		
		return config.getGuiStateFile();
	}
	
	private static String getExpectedFilename(Class<?> clazz, File testCase) {
		String name = testCase.getName();
		int extensionPos = name.lastIndexOf('.');
		name = name.substring(0, extensionPos);
		
		return "expected/" + clazz.getSimpleName() + "/" + name + ".STATE.xml";
	}
		
	private static void replayAndDiff(Class<?> clazz, File testCase) {
		diff(getExpectedFilename(clazz, testCase), replay(clazz, testCase));
	}
	
	// replay and diff all test cases for a given app
	@SuppressWarnings("unused")
	private static void replayAndDiffAll(Class<?> clazz) {
		String autName = clazz.getSimpleName();
		
		File[] testcases = new File("testcases/" + autName).listFiles();
		for (File testcase : testcases) {
			replayAndDiff(clazz, testcase);	
		}
	}
	
	// useful if you want to replay an app but its GUI state isn't stable
	private static void replayAll(Class<?> clazz) {
		String autName = clazz.getSimpleName();
		
		File[] testcases = new File("testcases/" + autName).listFiles();
		for (File testcase : testcases) {
			replay(clazz, testcase);	
		}
	}
	
	private static void diff(String expectedFilename, String actualFilename) {
		XMLUnit.setNormalizeWhitespace(true);

		Document actual;
		Document expected;
		try {
			// also called controlDoc by XMLUnit
			expected = XMLUnit.buildTestDocument(new InputSource(
					new FileReader(expectedFilename)));

			// also called expectedDoc by XMLUnit
			actual = XMLUnit.buildControlDocument(new InputSource(
					new FileReader(actualFilename)));
		} catch (SAXException e) {
			// so calling methods don't have to declare this as checked
			// exception
			throw new AssertionError(e);
		} catch (IOException e) {
			throw new AssertionError(e);
		}

		DetailedDiff diff = new DetailedDiff(new Diff(expected, actual));
		
		for (Object o : diff.getAllDifferences()) {
			GUITARLog.log.warn(o);
		}
		
		assertTrue(expectedFilename + " failed", diff.similar());
	}
		
	@Test
	public void testSWTBasicApp() {
		replayAll(SWTBasicApp.class);
	}
		
	@Test
	public void testSWTMultiWindowDynamicApp() {
		replayAll(SWTMultiWindowDynamicApp.class);
	}
	
	@Test
	public void testSWTHelloWorldApp() {
		replayAll(SWTHelloWorldApp.class);
	}
	
	@Test
	public void testSWTListApp() {
		replayAll(SWTListApp.class);
	}
	
	@Test
	public void testSWTTabFolderApp() {
		replayAll(SWTTabFolderApp.class);
	}
}
