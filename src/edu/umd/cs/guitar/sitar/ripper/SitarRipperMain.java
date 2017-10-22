package edu.umd.cs.guitar.sitar.ripper;

import java.net.URL;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

/**
 * This class provides the <code>main</code> method of SitarRipper.
 * 
 * @author Gabe Gorelick
 * @author <a href="mailto:mattkse@gmail.com"> Matt Kirn </a>
 * @author <a href="mailto:atloeb@gmail.com"> Alex Loeb </a>
 */
public class SitarRipperMain {

	/**
	 * The main entry point into SitarRipper. Used by scripts to run the ripper. Can
	 * also be invoked manually on the command line by users, but this is not
	 * recommended as then the user would have to manage SWTGuitar's classpath.
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {

		CmdLineParser.registerHandler(String[].class, StringArrayOptionHandler.class);
		CmdLineParser.registerHandler(URL[].class, URLArrayOptionHandler.class);

		SitarRipperConfiguration configuration = new SitarRipperConfiguration();
		CmdLineParser parser = new CmdLineParser(configuration);

		try {
			parser.parseArgument(args);
			if (configuration.getHelp()) {
				parser.printUsage(System.err);
				return;
			}

			SitarRipper swtRipper = new SitarRipper(configuration, Thread.currentThread());
			new SitarRunner(swtRipper).run();
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println();
			parser.printUsage(System.err);
		}

	}
}
