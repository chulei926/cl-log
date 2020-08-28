package com.cl.log.agent.file;

import java.io.File;

@FunctionalInterface
public interface FileChangeCallBack {

	void emit(File file);

}
