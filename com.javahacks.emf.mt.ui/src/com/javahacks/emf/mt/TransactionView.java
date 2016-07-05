package com.javahacks.emf.mt;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.impl.TransactionalEditingDomainImpl;
import org.eclipse.emf.transaction.ui.provider.TransactionalAdapterFactoryLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.javahacks.emf.mt.model.Model;
import com.javahacks.emf.mt.model.ModelFactory;
import com.javahacks.emf.mt.model.ModelPackage;
import com.javahacks.emf.mt.model.Signal;
import com.javahacks.emf.mt.model.provider.ModelItemProviderAdapterFactory;
import com.javahacks.emf.mt.model.util.EMFTransactionHelper;

public class TransactionView {

	private TransactionalEditingDomain editingDomain;

	@Inject
	public TransactionView(Composite parent) {

		AdapterFactory adapterFactory = new ModelItemProviderAdapterFactory();

		editingDomain = new TransactionalEditingDomainImpl(adapterFactory);

		TableViewer tableViewer = new TableViewer(parent, SWT.VIRTUAL);
		tableViewer.getTable().setHeaderVisible(true);

		TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Value");

		column = new TableViewerColumn(tableViewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Total Updates");

		tableViewer.setContentProvider(
				new DelayedTransactionalAdapterFactoryContentProvider(editingDomain, adapterFactory));
		tableViewer.setLabelProvider(new TransactionalAdapterFactoryLabelProvider(editingDomain, adapterFactory));
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

				editingDomain.getCommandStack()
						.execute(AddCommand.create(editingDomain, model, ModelPackage.Literals.MODEL__SIGNALS, signal));

				schedule(10);

				return Status.OK_STATUS;
			}
		}.schedule();
		;

		new Job("Model Remove") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				editingDomain.getCommandStack().execute(RemoveCommand.create(editingDomain, model,
						ModelPackage.Literals.MODEL__SIGNALS, model.getSignals().get(0)));

				schedule(10);

				return Status.OK_STATUS;
			}
		}.schedule();

		new Job("Model Update") {

			private int updates;

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {

					@Override
					protected void doExecute() {
						model.getSignals().forEach(s -> s.setValue(Math.random()));
						model.getSignals().forEach(s -> s.setUpdates(updates++));
					}
				});

				schedule();

				return Status.OK_STATUS;
			}
		}.schedule();

		return model;
	}

}
