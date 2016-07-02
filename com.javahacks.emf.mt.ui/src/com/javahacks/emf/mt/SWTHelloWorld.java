package com.javahacks.emf.mt;
import java.awt.Component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SWTHelloWorld {

public static void main (String [] args) {
	Display display = new Display ();
	Shell shell = new Shell(display);
	shell.setLayout(new FillLayout());
	
	
	
	new ModelView3(shell);
	
	
	
	shell.open ();
	while (!shell.isDisposed ()) {
		if (!display.readAndDispatch ()) display.sleep ();
	}
	display.dispose ();
}
}