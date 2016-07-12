package com.javahacks.emf.ui.util;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.ui.provider.TransactionalAdapterFactoryContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
 
/**
 * * Use this {@link AdapterFactoryContentProvider} if the viewer's underlying
 * domain model frequently changes and would produce many notification events.
 * In this case it's much more efficient to trigger a full refresh after a short
 * delay.
 */
public class DelayedTransactionalAdapterFactoryContentProvider extends
        TransactionalAdapterFactoryContentProvider {
 
    public DelayedTransactionalAdapterFactoryContentProvider(TransactionalEditingDomain domain,
			AdapterFactory adapterFactory) {
		super(domain, adapterFactory);
	}

	private static final int DELAY_IN_MS = 100;
    private final AtomicBoolean refresh = new AtomicBoolean(false);
    private Viewer viewer;
 
 
 
    @Override
    public void notifyChanged(Notification notification) {
 
        if (!notification.isTouch() && !refresh.getAndSet(true)) {
 
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
 
                    Display.getDefault().timerExec(DELAY_IN_MS, new Runnable() {
                        @Override
                        public void run() {
                            refresh.set(false);
 
                            if (viewer.getControl() != null && !viewer.getControl().isDisposed()) {
                                viewer.refresh();
                            }
                        }
                    });
                }
            });
 
        }
    }
 
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.viewer = viewer;
        super.inputChanged(viewer, oldInput, newInput);
    }
}