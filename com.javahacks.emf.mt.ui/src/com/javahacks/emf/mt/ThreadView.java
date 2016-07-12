package com.javahacks.emf.mt;

import java.util.List;
import java.util.UUID;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.IDisposable;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.javahacks.emf.mt.model.Model;
import com.javahacks.emf.mt.model.ModelFactory;
import com.javahacks.emf.mt.model.ModelPackage;
import com.javahacks.emf.mt.model.Signal;
import com.javahacks.emf.mt.model.provider.ModelItemProviderAdapterFactory;
import com.javahacks.emf.ui.util.DelayedAdapterFactoryContentProvider;
import com.javahacks.emf.ui.util.EMFTransactionHelper;

/**
 * View that uses the UI thread to synchronize the domain model
 */
public class ThreadView {

	private AdapterFactory adapterFactory;

	@Inject
	public ThreadView(Composite parent) {

		EMFTransactionHelper.setSynchronizer((runnable) -> Display.getDefault().syncExec(runnable));

		TableViewer tableViewer = new TableViewer(parent, SWT.VIRTUAL);
		tableViewer.getTable().setHeaderVisible(true);

		TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Value");

		column = new TableViewerColumn(tableViewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Total Updates");

		adapterFactory = new ModelItemProviderAdapterFactory();
		tableViewer.setContentProvider(new DelayedAdapterFactoryContentProvider(adapterFactory));
		tableViewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
		tableViewer.setInput(initModel());

	}

	private Model initModel() {

		final Model model = ModelFactory.eINSTANCE.createModel();

		for (int i = 0; i < 10000; i++) {

			Signal signal = ModelFactory.eINSTANCE.createSignal();
			signal.setName(UUID.randomUUID().toString());
			signal.setValue(i);

			model.getSignals().add(signal);

		}

		new Job("Model Append") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				Signal signal = ModelFactory.eINSTANCE.createSignal();
				signal.setName(UUID.randomUUID().toString());
				signal.setValue((int) Math.random());

				EMFTransactionHelper.addElementExclusive(model, ModelPackage.Literals.MODEL__SIGNALS, signal);

				schedule(10);

				return Status.OK_STATUS;
			}
		}.schedule();
		;

		new Job("Model Remove") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				EMFTransactionHelper.removeElementExclusive(model, ModelPackage.Literals.MODEL__SIGNALS,
						model.getSignals().get(0));

				schedule(10);

				return Status.OK_STATUS;
			}
		}.schedule();

		new Job("Model Update") {

			private int updates;

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				EMFTransactionHelper.runExclusive(() -> {

					model.getSignals().forEach(s -> s.setValue(Math.random()));
					model.getSignals().forEach(s -> s.setUpdates(updates++));

				});

				schedule();

				return Status.OK_STATUS;
			}
		}.schedule();

		new Job("Model Iterate") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				List<Signal> clonedSignals = EMFTransactionHelper.cloneCollectionExclusive(model.getSignals());

				System.out.println(clonedSignals.stream().count());

				schedule(200);

				return Status.OK_STATUS;
			}
		}.schedule();

		return model;
	}

	@PreDestroy
	private void dispose() {
		((IDisposable) adapterFactory).dispose();
	}
}
