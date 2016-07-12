package com.javahacks.emf.mt.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.AbstractOverrideableCommand;

/**
 * Utility class that provides convenient methods for a thread safe modification of the global application model.
 * 
 * @author Wolfgang Geck 
 */
public class EMFTransactionHelper {

	public static interface Synchronizer {
		void syncExec(Runnable runnable);
	}

	private static Synchronizer synchronizer = new EMFTransactionHelper.Synchronizer() {

		@Override
		public void syncExec(final Runnable runnable) {

			synchronized (EMFTransactionHelper.class) {
				runnable.run();
			}

		}
	};

	public static void setSynchronizer(final Synchronizer synchronizer) {
		EMFTransactionHelper.synchronizer = synchronizer;
	}

	/**
	 * Run given runnable exclusive and wait until {@link Runnable} was invoked and finished its execution.
	 * 
	 * @throws IllegalArgumentException
	 */
	public static void runExclusive(final Runnable runnable) {
		synchronizer.syncExec(runnable);
	}

	/**
	 * Add a single element into the many-valued feature of the owner
	 * 
	 * @param owner
	 * @param feature
	 * @param element
	 */
	public static void addElementExclusive(final EObject owner, final EStructuralFeature feature, final Object element) {
		runExclusive(() -> AbstractOverrideableCommand.getOwnerList(owner, feature).add(element));
	}

	/**
	 * Remove a single element from the many-valued feature of the owner
	 * 
	 * @param owner
	 * @param feature
	 * @param element
	 */
	public static void removeElementExclusive(final EObject owner, final EStructuralFeature feature, final Object element) {
		runExclusive(() -> AbstractOverrideableCommand.getOwnerList(owner, feature).remove(element));
	}

	/**
	 * Clear collection exclusive
	 * 
	 * @param collection
	 */
	public static void clearCollectionExclusive(final Collection<?> collection) {
		runExclusive(() -> collection.clear());
	}

	/**
	 * Add all elements of a collection to another collection
	 * 
	 * @param collection
	 */
	public static <T> void addAllCollectionExclusive(final Collection<T> source, final Collection<T> elementsToAdd) {
		runExclusive(() -> source.addAll(elementsToAdd));
	}

	/**
	 * Copy source collection in exclusive context.
	 */
	public static <T> List<T> cloneCollectionExclusive(final List<T> source) {

		final List<T> result = new ArrayList<T>();

		runExclusive(() -> result.addAll(source));

		return result;
	}

}
