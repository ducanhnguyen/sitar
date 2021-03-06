/*	
 *  Copyright (c) 2009-@year@. The GUITAR group at the University of Maryland. Names of owners of this group may
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
package edu.umd.cs.guitar.replayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.umd.cs.guitar.event.GEvent;
import edu.umd.cs.guitar.exception.ComponentDisabled;
import edu.umd.cs.guitar.exception.ComponentNotFound;
import edu.umd.cs.guitar.exception.GException;
import edu.umd.cs.guitar.model.GComponent;
import edu.umd.cs.guitar.model.GUITARConstants;
import edu.umd.cs.guitar.model.GWindow;
import edu.umd.cs.guitar.model.IO;
import edu.umd.cs.guitar.model.data.AttributesType;
import edu.umd.cs.guitar.model.data.EFG;
import edu.umd.cs.guitar.model.data.EventType;
import edu.umd.cs.guitar.model.data.GUIStructure;
import edu.umd.cs.guitar.model.data.PropertyType;
import edu.umd.cs.guitar.model.data.StepType;
import edu.umd.cs.guitar.model.data.TestCase;
import edu.umd.cs.guitar.model.wrapper.ComponentTypeWrapper;
import edu.umd.cs.guitar.model.wrapper.GUIStructureWrapper;
import edu.umd.cs.guitar.model.wrapper.PropertyTypeWrapper;
import edu.umd.cs.guitar.replayer.monitor.GTestMonitor;
import edu.umd.cs.guitar.replayer.monitor.TestStepEndEventArgs;
import edu.umd.cs.guitar.replayer.monitor.TestStepStartEventArgs;

/**
 * 
 * Main replayer class, monitoring the replayer's behaviors
 * 
 * <p>
 * 
 * @author <a href="mailto:baonn@cs.umd.edu"> Bao Nguyen </a>
 * 
 */
public class Replayer {

	private static final Logger logger = LoggerFactory.getLogger(Replayer.class);
	
	/**
	 * Test case data
	 */
	private TestCase tc;

	// Test Monitor
	private GReplayerMonitor monitor;
	private List<GTestMonitor> lTestMonitor = new ArrayList<GTestMonitor>();

	// Secondary input
	private GUIStructureWrapper guiStructureAdapter;
	private EFG efg;
	private Document docGUI;

	// private Document docEFG;

	/**
	 * @param tc
	 * @param sGUIFile
	 * @param sEFGFile
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public Replayer(TestCase tc, String sGUIFile, String sEFGFile)
			throws ParserConfigurationException, SAXException, IOException {
		super();
		this.tc = tc;

		// Initialize GUI object
		GUIStructure gui = (GUIStructure) IO.readObjFromFile(sGUIFile,
				GUIStructure.class);
		guiStructureAdapter = new GUIStructureWrapper(gui);

		// Initialize EFG object
		this.efg = (EFG) IO.readObjFromFile(sEFGFile, EFG.class);

		// Initialize EFG XML file
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder;
		builder = domFactory.newDocumentBuilder();
		docGUI = builder.parse(sGUIFile);
		// docEFG = builder.parse(sEFGFile);
	}

	/**
	 * Constructor for the replayer
	 * 
	 * @param tc
	 *            input test case
	 */
	@Deprecated
	public Replayer(TestCase tc) {
		super();
		this.tc = tc;
	}

	/**
	 * Parse and run test case.
	 * 
	 * @throws ComponentNotFound
	 * 
	 */
	public void execute() throws ComponentNotFound {
		try {
			monitor.setUp();
			monitor.connectToApplication();

			// Monitor before the test case
			for (GTestMonitor monitor : lTestMonitor) {
				monitor.init();
			}

			List<StepType> lSteps = tc.getStep();
			int nStep = lSteps.size();

			for (int i = 0; i < nStep; i++) {
				StepType step = lSteps.get(i);
				executeStep(step);
			}
			// Monitor after the test case
			for (GTestMonitor monitor : lTestMonitor) {
				monitor.term();
			}

		} catch (GException e) {
			// GUITARLog.log.error("GUITAR Exception", e);
			for (GTestMonitor monitor : lTestMonitor) {
				monitor.exceptionHandler(e);
			}
			throw e;
		} finally {
			monitor.cleanUp();
		}
	}

	/**
	 * Execute a single step in the test case
	 * 
	 * <p>
	 * 
	 * @param step
	 * @throws ComponentNotFound
	 */
	private void executeStep(StepType step) throws ComponentNotFound {

		TestStepStartEventArgs stepStartArgs = new TestStepStartEventArgs(step);

		// -----------------------
		// Monitor before step
		for (GTestMonitor aTestMonitor : lTestMonitor) {
			aTestMonitor.beforeStep(stepStartArgs);
		}

		// Events
		String sEventID = step.getEventId();

		// Get widget ID and actions
		// String sWidgetID = getWidgetID("WidgetId", sEventID);
		String sWidgetID = null;
		String sAction = null;

		List<EventType> lEvents = efg.getEvents().getEvent();

		for (EventType event : lEvents) {
			String eventID = event.getEventId();
			if (sEventID.equals(eventID)) {
				sWidgetID = event.getWidgetId();
				sAction = event.getAction();
			}
		}

		if (sWidgetID == null) {
			logger.error("Component ID {} not found", sEventID);
			throw new ComponentNotFound();
		} else if (sAction == null) {
			logger.error("Action not found");
			throw new ComponentNotFound();
		}

		String sWindowID = getWindowName(sWidgetID);

		if (sWindowID == null) {
			logger.error("Window Title not found");
			throw new ComponentNotFound();
		}

		// TODO: Change this method to a fuzzy matching
		GWindow gWindow = monitor.getWindow(sWindowID);

		ComponentTypeWrapper comp = guiStructureAdapter
				.getComponentFromID(sWidgetID);

		if (comp == null)
			throw new ComponentNotFound();

		List<PropertyType> ID = monitor.selectIDProperties(comp
				.getDComponentType());
		List<PropertyTypeWrapper> IDAdapter = new ArrayList<PropertyTypeWrapper>();

		for (PropertyType p : ID)
			IDAdapter.add(new PropertyTypeWrapper(p));

		GComponent containter = gWindow.getContainer();

		GComponent gComponent = containter.getFirstChild(IDAdapter);

		if (gComponent == null)
			throw new ComponentNotFound();

		if (!gComponent.isEnable())
			throw new ComponentDisabled();

		// Actions
		GEvent gEvent = monitor.getAction(sAction);
		List<String> parameters = step.getParameter();

		// Optional data
		AttributesType optional = comp.getDComponentType().getOptional();
		Hashtable<String, List<String>> optionalValues = null;
		
		
		
		if (optional != null) {
			
			optionalValues = new Hashtable<String, List<String>>();
			for (PropertyType property : optional.getProperty()) {
				optionalValues.put(property.getName(), property.getValue());
			}
		}

		if (parameters == null)
			gEvent.perform(gComponent, optionalValues);
		else if (parameters.size() == 0) {
			gEvent.perform(gComponent, optionalValues);
		} else
			gEvent.perform(gComponent, parameters, optionalValues);

		TestStepEndEventArgs stepEndArgs = new TestStepEndEventArgs(step,
				gComponent.extractProperties(), gWindow.extractGUIProperties());
		// -----------------------
		// Monitor after step
		for (GTestMonitor aTestMonitor : lTestMonitor) {
			aTestMonitor.afterStep(stepEndArgs);
		}
	}

	/**
	 * Get container window
	 * 
	 * <p>
	 * 
	 * @return String
	 */
	private String getWindowName(String sWidgetID) {

		String sWindowName = null;
		// get widget ID
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr;
		Object result;
		NodeList nodes;
		
		String xpathExpression = "/GUIStructure/GUI[Container//Property[Name=\""
				+ GUITARConstants.ID_TAG_NAME
				+ "\" and Value=\""
				+ sWidgetID
				+ "\"]]/Window/Attributes/Property[Name=\""
				+ GUITARConstants.TITLE_TAG_NAME + "\"]/Value/text()";
		
		try {
			expr = xpath.compile(xpathExpression);
			result = expr.evaluate(docGUI, XPathConstants.NODESET);
			nodes = (NodeList) result;
			if (nodes.getLength() > 0)
				sWindowName = nodes.item(0).getNodeValue();
		} catch (XPathExpressionException e) {
			logger.error("Error evaluating XPath expression {}", xpathExpression, e);
		}
		return sWindowName;
	}

	// /**
	// * Get Widget ID from eventID
	// *
	// * @return
	// */
	// private String getWidgetID(String sTag, String sEventID) {
	// String sWidgetID = null;
	// // get widget ID
	// XPath xpath = XPathFactory.newInstance().newXPath();
	// XPathExpression expr;
	// Object result;
	// NodeList nodes;
	// try {
	//
	// expr = xpath.compile("/EFG/Events/Event[EventId=\"" + sEventID
	// + "\"]/" + sTag + "/text()");
	// result = expr.evaluate(docEFG, XPathConstants.NODESET);
	// nodes = (NodeList) result;
	// if (nodes.getLength() > 0)
	// sWidgetID = nodes.item(0).getNodeValue();
	// } catch (XPathExpressionException e) {
	// // TODO Auto-generated catch block
	// }
	// return sWidgetID;
	// }

	// /**
	// * @return
	// */
	// private String getWidgetID_bak(String sTag, String sEventID) {
	// String sWidgetID = null;
	// // get widget ID
	// XPath xpath = XPathFactory.newInstance().newXPath();
	// XPathExpression expr;
	// Object result;
	// NodeList nodes;
	// try {
	//
	// expr = xpath.compile("/EFG/Events/Event[EventId=\"" + sEventID
	// + "\"]/" + sTag + "/text()");
	// result = expr.evaluate(docEFG, XPathConstants.NODESET);
	// nodes = (NodeList) result;
	// if (nodes.getLength() > 0)
	// sWidgetID = nodes.item(0).getNodeValue();
	// } catch (XPathExpressionException e) {
	// // TODO Auto-generated catch block
	// }
	// return sWidgetID;
	// }

	/**
	 * Get the replayer monitor
	 * 
	 * @return the replayer monitor
	 */
	public GReplayerMonitor getMonitor() {
		return monitor;
	}

	/**
	 * @param monitor
	 *            the replayer monitor to set
	 */
	public void setMonitor(GReplayerMonitor monitor) {
		this.monitor = monitor;
	}

	/**
	 * 
	 * Add a test monitor
	 * 
	 * <p>
	 * 
	 * @param aTestMonitor
	 */
	public void addTestMonitor(GTestMonitor aTestMonitor) {
		aTestMonitor.setReplayer(this);
		this.lTestMonitor.add(aTestMonitor);
	}

	/**
	 * Remove a test monitor
	 * 
	 * <p>
	 * 
	 * @param mTestMonitor
	 */
	public void removeTestMonitor(GTestMonitor mTestMonitor) {
		this.lTestMonitor.remove(mTestMonitor);
	}

}
