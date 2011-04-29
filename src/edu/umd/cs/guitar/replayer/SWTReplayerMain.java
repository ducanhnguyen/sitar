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

import java.net.URL;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import edu.umd.cs.guitar.ripper.SWTGuitarRunner;
import edu.umd.cs.guitar.ripper.URLArrayOptionHandler;

/**
 * Entry class for SWTReplayer.
 * 
 * @author Gabe Gorelick
 */
public class SWTReplayerMain {

	private SWTReplayerMain() {
		// this space intentionally left blank
	}

	/**
	 * Used by scripts to run the replayer. Can also be invoked manually on the
	 * command line by users, but this is not recommended as then the user would
	 * have to manage SWTGuitar's classpath.
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {
		CmdLineParser.registerHandler(String[].class, StringArrayOptionHandler.class);
    	CmdLineParser.registerHandler(URL[].class, URLArrayOptionHandler.class);
    	
        SWTReplayerConfiguration configuration = new SWTReplayerConfiguration();
        CmdLineParser parser = new CmdLineParser(configuration);
        
        try {
            parser.parseArgument(args);
            if (configuration.getHelp()) {
            	parser.printUsage(System.err);
            	return;
            }
               
            SWTReplayer replayer = new SWTReplayer(configuration, Thread.currentThread());
            new SWTGuitarRunner(replayer).run();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println();
            parser.printUsage(System.err);
        }
	}

}

