package org.lucee.extension.orm.hibernate.tuplizer.accessors;

import org.hibernate.PropertyNotFoundException;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.Setter;

import com.sun.xml.internal.ws.spi.db.PropertyAccessor;

public class CFCAccessor implements PropertyAccessor {
	
	public CFCAccessor(){
	}
	
	@Override
	public Getter getGetter(Class clazz, String propertyName) throws PropertyNotFoundException {
		return new CFCGetter(propertyName);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Setter getSetter(Class clazz, String propertyName)	throws PropertyNotFoundException {
		return new CFCSetter(propertyName);
	}

}
