package de.terrestris.momo.security.access.entity;

import de.terrestris.shogun2.model.PersistentObject;

/**
 * @author Nils Bühner
 *
 */
public abstract class AbstractMomoPersistentObjectPermissionEvaluatorTest<E extends PersistentObject> {

	// the permission evaluator to test
	protected MomoPersistentObjectPermissionEvaluator<E> momoPersistentObjectPermissionEvaluator;

	protected final Class<E> entityClass;

	protected E entityToCheck;

	/**
	 * Constructor that has to be implemented by subclasses
	 *
	 * @param entityClass
	 */
	protected AbstractMomoPersistentObjectPermissionEvaluatorTest(
			Class<E> entityClass,
			MomoPersistentObjectPermissionEvaluator<E> momoPersistentObjectPermissionEvaluator,
			E entityToCheck) {
		this.entityClass = entityClass;
		this.momoPersistentObjectPermissionEvaluator = momoPersistentObjectPermissionEvaluator;
		this.entityToCheck = entityToCheck;
	}

}
