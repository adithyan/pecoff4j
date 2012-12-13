package com.github.twinj.pecoff4j;

import com.github.twinj.headers.DatumAbstract;

public interface PropertyInterface {
	Class<? extends DatumAbstract<?>> getDatumClass();
	int sizeOf();
	void upSize(int size);
}
