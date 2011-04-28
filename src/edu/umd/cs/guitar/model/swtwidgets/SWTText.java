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
package edu.umd.cs.guitar.model.swtwidgets;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.widgets.Text;

import edu.umd.cs.guitar.event.GEvent;
import edu.umd.cs.guitar.event.SWTEditableTextAction;
import edu.umd.cs.guitar.model.SWTWindow;

public class SWTText extends SWTControl {

	private final Text text;
	
	protected SWTText(Text text, SWTWindow window) {
		super(text, window);
		this.text = text;
	}
	
	@Override
	public List<GEvent> getEventList() {
		List<GEvent> events = super.getEventList();
				
		final AtomicBoolean editable = new AtomicBoolean();
		text.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				editable.set(text.getEditable());
			}
		});
		
		if (editable.get()) {
			events.add(new SWTEditableTextAction());
		} 
		
		return events;
	}

}
