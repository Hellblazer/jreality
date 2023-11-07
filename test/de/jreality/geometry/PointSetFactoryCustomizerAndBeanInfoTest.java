package de.jreality.geometry;

import org.junit.Before;

import de.jreality.plugin.geometry.PointSetFactoryCustomizer;

public class PointSetFactoryCustomizerAndBeanInfoTest 
	extends AbstractGeometryFactoryCustomizerAndBeanInfoTest<PointSetFactory, PointSetFactoryCustomizer> {

	@Before
	public void turnOffUpdate() {
		customizer.setAlwaysCallUpdateMethdod(false);
	}

	@Override
	PointSetFactory getFactory() {
		if (factory == null) factory = new PointSetFactory();
		return factory;
	}

	@Override
	 PointSetFactoryCustomizer getACustomizer() {
		return new PointSetFactoryCustomizer(factory);
	}

	@Override
	int nbOfIntSliders() {
		return 0;
	}

	@Override
	int nbOfDblSliders() {
		return 0;
	}

	@Override
	int nbOfToggleButtons() {
		return 1;
	}

}
