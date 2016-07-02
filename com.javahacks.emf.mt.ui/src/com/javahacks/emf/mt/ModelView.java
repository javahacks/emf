package com.javahacks.emf.mt;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.javahacks.emf.mt.model.Model;
import com.javahacks.emf.mt.model.ModelFactory;
import com.javahacks.emf.mt.model.ModelPackage;
import com.javahacks.emf.mt.model.Signal;
import com.javahacks.emf.mt.model.provider.ModelItemProviderAdapterFactory;
import com.javahacks.emf.mt.model.util.EMFTransactionHelper;

public class ModelView {

	@Inject
	public ModelView(Composite parent) {

		EMFTransactionHelper.setSynchronizer((runnable) -> Display.getDefault().syncExec(runnable));

		TableViewer tableViewer = new TableViewer(parent, SWT.VIRTUAL);

		AdapterFactory adapterFactory = new ModelItemProviderAdapterFactory();

		tableViewer.setContentProvider(new DelayedAdapterFactoryContentProvider(adapterFactory));
		tableViewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
		tableViewer.setInput(initModel());

	}

	private Model initModel() {

		final Model model = ModelFactory.eINSTANCE.createModel();

		for (int i = 0; i < 10000; i++) {

			Signal signal = ModelFactory.eINSTANCE.createSignal();
			signal.setName(String.valueOf(i + 1));
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

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				List<Signal> clonedList = EMFTransactionHelper.cloneCollectionExclusive(model.getSignals());

				clonedList.forEach(s -> s.setValue( Math.random()));
				clonedList.forEach(s -> s.setUpdates(s.getUpdates()+1));
				
				System.out.println(clonedList.size());

				schedule();

				return Status.OK_STATUS;
			}
		}.schedule();

		return model;
	}

}
