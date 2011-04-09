package edu.umd.cs.guitar.model.swtwidgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import edu.umd.cs.guitar.model.GComponent;
import edu.umd.cs.guitar.model.GWindow;

public class SWTTable extends SWTComposite {

	private final Table table;
	
	protected SWTTable(Table table, GWindow window) {
		super(table, window);
		this.table = table;
	}
	
	@Override
	public List<GComponent> getChildren() {
		final List<GComponent> children = new ArrayList<GComponent>();
		final SWTWidgetFactory factory = SWTWidgetFactory.newInstance();
		
		table.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				for (TableColumn col : table.getColumns()) {
					children.add(factory.newSWTWidget(col, getWindow()));
				}
				for (TableItem i : table.getItems()) {
					children.add(factory.newSWTWidget(i, getWindow()));
				}
			}
		});
		 				
		return children;
	}

}
