package com.cl.log.server.task;

import com.cl.log.server.model.BizLog;
import com.cl.log.server.persistence.BizLogRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class BizLogTask {

	private static final List<BizLog> biz_logs = Collections.synchronizedList(new ArrayList<>());

	@Resource
	private BizLogRepository bizLogRepository;

	public void save(BizLog bizLog) {
		if (null == bizLog) {
			return;
		}
		biz_logs.add(bizLog);
		if (biz_logs.size() >= 100) { // 每 100 条保存一次

		}
	}


}
