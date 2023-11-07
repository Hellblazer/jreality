package de.jreality.plugin.geometry;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

import de.jreality.geometry.IndexedLineSetFactory;

/** Announces the customizer {@link IndexedLineSetFactoryCustomizer} for JavaBeans introspection.
 * 
 * @author G. Paul Peters, 03.06.2010
 */
public class IndexedLineSetFactoryBeanInfo extends SimpleBeanInfo {

	private final BeanDescriptor bd = new BeanDescriptor(IndexedLineSetFactory.class, IndexedLineSetFactoryCustomizer.class);
	
	public IndexedLineSetFactoryBeanInfo() {
		bd.setDisplayName("Indexed Line Set Factory Explorer");
	}

	@Override
	public BeanDescriptor getBeanDescriptor() {
		return bd;
	}
}
