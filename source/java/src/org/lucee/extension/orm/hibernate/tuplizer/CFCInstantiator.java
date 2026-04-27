package org.lucee.extension.orm.hibernate.tuplizer;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.spi.EntityInstantiator;
import org.lucee.extension.orm.hibernate.util.CommonUtil;
import org.lucee.extension.orm.hibernate.HibernateCaster;
import org.lucee.extension.orm.hibernate.HibernateORMEngine;
import org.lucee.extension.orm.hibernate.HibernateORMSession;
import org.lucee.extension.orm.hibernate.HibernatePageException;
import org.lucee.extension.orm.hibernate.util.HibernateUtil;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public class CFCInstantiator implements EntityInstantiator {

	private final String entityName;
	private final Set<String> isInstanceEntityNames = new HashSet<>();

	public CFCInstantiator(PersistentClass mappingInfo) {
		this.entityName = mappingInfo.getEntityName();
		isInstanceEntityNames.add(entityName);
		if (mappingInfo.hasSubclasses()) {
			for (PersistentClass subclass : mappingInfo.getSubclassClosure()) {
				isInstanceEntityNames.add(subclass.getEntityName());
			}
		}
	}

	@Override
	public Object instantiate() {
		try {
			PageContext pc = CommonUtil.pc();
			HibernateORMSession session = HibernateUtil.getORMSession(pc, true);
			HibernateORMEngine engine = (HibernateORMEngine) session.getEngine();
			Component c = engine.create(pc, session, entityName, true);
			c.setEntity(true);
			return c;
		} catch (PageException pe) {
			throw new HibernatePageException(pe);
		}
	}

	@Override
	public boolean isInstance(Object object) {
		Component cfc = CommonUtil.toComponent(object, null);
		if (cfc == null) return false;
		return isInstanceEntityNames.contains(HibernateCaster.getEntityName(cfc));
	}

	@Override
	public boolean isSameClass(Object object) {
		Component cfc = CommonUtil.toComponent(object, null);
		if (cfc == null) return false;
		return entityName.equals(HibernateCaster.getEntityName(cfc));
	}
}
