package com.cl.log.agent.file;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 文件监听器.
 * <pre>
 *     只有当文件内容发生变化时，执行回调函数 FileChangeCallBack.
 * </pre>
 *
 * @author leichu 2020-08-28.
 */
public class FileListener extends FileAlterationListenerAdaptor {

	private static final Logger logger = LoggerFactory.getLogger(FileListener.class);

	private final FileChangeCallBack callBack;

	public FileListener(FileChangeCallBack callBack) {
		this.callBack = callBack;
	}

	/**
	 * 创建文件.
	 *
	 * @param file file.
	 */
	@Override
	public void onFileCreate(File file) {
		logger.debug("[新建]:" + file.getAbsolutePath());
//		callBack.emit(file);
	}

	/**
	 * 修改文件.
	 *
	 * @param file file.
	 */
	@Override
	public void onFileChange(File file) {
		logger.debug("[修改]:" + file.getAbsolutePath());
		callBack.emit(file);
	}

	/**
	 * 删除文件.
	 *
	 * @param file file.
	 */
	@Override
	public void onFileDelete(File file) {
		logger.debug("[删除]:" + file.getAbsolutePath());
//		callBack.emit(file);
	}

	/**
	 * 创建目录.
	 *
	 * @param directory directory.
	 */
	@Override
	public void onDirectoryCreate(File directory) {
		logger.debug("[新建]:" + directory.getAbsolutePath());
//		callBack.emit(directory);
	}

	/**
	 * 修改目录.
	 *
	 * @param directory directory.
	 */
	@Override
	public void onDirectoryChange(File directory) {
		logger.debug("[修改]:" + directory.getAbsolutePath());
//		callBack.emit(directory);
	}

	/**
	 * 删除目录.
	 *
	 * @param directory directory.
	 */
	@Override
	public void onDirectoryDelete(File directory) {
		logger.debug("[删除]:" + directory.getAbsolutePath());
//		callBack.emit(directory);
	}

	@Override
	public void onStart(FileAlterationObserver observer) {
		super.onStart(observer);
	}

	@Override
	public void onStop(FileAlterationObserver observer) {
		super.onStop(observer);
	}
}