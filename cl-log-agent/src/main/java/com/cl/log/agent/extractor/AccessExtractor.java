package com.cl.log.agent.extractor;

import com.cl.log.config.model.LogFactory;
import com.cl.log.config.utils.NetUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 访问日志提取器.
 * <pre>
 *     按行解析，每一行解析成一个 Log.
 *     %reqAttribute{traceId}
 *     %reqAttribute{parentTraceId}
 *     %reqAttribute{curTraceId}
 *     %reqAttribute{appName}
 *     %reqAttribute{appPort}
 *     %reqAttribute{userId}
 *     %t{yyyy-MM-ddHH:mm:ss.SSS}
 *     %s
 *     %b
 *     %D
 *     [%i{X-Forwarded-For}]
 *     "%r"
 * </pre>
 * <pre>
 *     b77af958-6795-4174-800f-541ad793d785 000 000 exam-webapp 8080 484 2020-09-0110:00:20.633 200 513 6 [183.160.212.93, 58.218.208.59] "GET /exam-webapp/api/newExam/getExamInfo?_=1598925620320&examId=8369 HTTP/1.0"
 * </pre>
 *
 * @author leichu 2020-09-01.
 */
public class AccessExtractor implements Extractor {

	@Override
	public List<LogFactory.Log> extract(List<String> content) {
		List<LogFactory.Log> logs = Lists.newArrayList();
		if (CollectionUtils.isEmpty(content)) {
			return logs;
		}
		Splitter splitter = Splitter.on(" ");
		LogFactory.Log log;
		String ip = NetUtils.getIp();
		for (String line : content) {
			if (StringUtils.isBlank(line)) {
				continue;
			}
			List<String> split = splitter.splitToList(line);
			log = LogFactory.Log.newBuilder()
					.setCategory(LogFactory.Log.Category.tomcat_access_log)
					.setTomcatAccessLog(
							LogFactory.TomcatAccessLog.newBuilder()
									.setHost(ip)
									.setTraceId(split.get(0))
									.setBiz(split.get(3))
									.setPort(StringUtils.isNotBlank(split.get(4)) && StringUtils.isNumeric(split.get(4)) ? Integer.parseInt(split.get(4)) : 0)
									.setUserId(split.get(5))
									.setDateTime(split.get(6))
									.setStatesCode(split.get(7))
									.setConsume(StringUtils.isNotBlank(split.get(8)) && StringUtils.isNumeric(split.get(8)) ? Integer.parseInt(split.get(8)) : 0)
									.setIp(split.get(10) + split.get(11))
									.setRequestMethod(split.get(12).substring(1))
									.setRequestURL(split.get(13))
					)
					.build();
			logs.add(log);
		}
		return logs;
	}

}