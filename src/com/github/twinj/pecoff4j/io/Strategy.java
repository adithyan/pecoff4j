package com.github.twinj.pecoff4j.io;

import com.github.twinj.pecoff4j.io.PEParse.PEHandle;

public interface Strategy {
	void parse(PEHandle parser);
}
