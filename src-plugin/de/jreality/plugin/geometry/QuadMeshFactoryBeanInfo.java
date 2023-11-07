package de.jreality.plugin.geometry;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

import de.jreality.geometry.QuadMeshFactory;

/** Announces the customizer {@link QuadMeshFactoryCustomizer} for JavaBeans introspection.
 * 
 * @author G. Paul Peters, 03.06.2010
 */
public class QuadMeshFactoryBeanInfo extends SimpleBeanInfo {

	private final BeanDescriptor bd = new BeanDescriptor(QuadMeshFactory.class, QuadMeshFactoryCustomizer.class);
	
	public QuadMeshFactoryBeanInfo() {
		bd.setDisplayName("Quad Mesh Factory Explorer");
	}

	@Override
	public BeanDescriptor getBeanDescriptor() {
		return bd;
	}
}
