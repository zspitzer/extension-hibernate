package org.lucee.extension.orm.hibernate.compat;

import org.hibernate.Session;

/**
 * Synthetic interface enumerating the H5.6-era Session methods removed in Hibernate 6/7
 * that real-world CFML code (cborm, ColdBox, Slatwall, custom apps) still calls. The
 * wrapper proxy at {@link CompatSessionWrapper} implements this interface alongside
 * {@link Session} and {@link org.hibernate.engine.spi.SessionImplementor} so CFML
 * reflection (which walks declared interface methods) sees the legacy method names.
 *
 * <p>Empty for now — populated incrementally as the {@code CompatSessionWrapper}
 * gains routing branches per family. Batch 1 of the implementation just stands up
 * the wrapping infrastructure without re-exposing any methods; subsequent batches
 * add the mechanical translations (save/update/saveOrUpdate/delete/load/refresh/
 * createSQLQuery/getNamedSQLQuery) and the createCriteria plug-in family.
 *
 * <p>Empirical surface for what to add: 32 overloads across 9 families per the
 * H5.6 → H7.3 reflection diff at {@code tests/compat/sessionApiSurface/}.
 *
 * @see CompatSessionWrapper
 */
public interface CompatSession extends Session {

	// Batch 2 (mechanical): save / update / saveOrUpdate / delete / load /
	// refresh / createSQLQuery / getNamedSQLQuery overloads to be added here.

	// Batch 3 (plug-in): createCriteria overloads dispatching to ormSettings.sessionShim.
}
