package com.cl.log.server.service;

import com.cl.log.server.model.BizLog;
import com.cl.log.server.persistence.BizLogRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Component
public class BizLogService {

	@Resource
	private BizLogRepository bizLogRepository;

	public void save(List<BizLog> bizLogs) {
		if (CollectionUtils.isEmpty(bizLogs)) {
			return;
		}
//		bizLogRepository.batchInsert();

	}


}
