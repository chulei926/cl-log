package com.cl.log.agent.extractor;

import com.cl.log.agent.config.Alias;
import com.cl.log.config.model.LogFactory;
import com.cl.log.config.utils.NetUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 *     %s 状态码
 *     %b 响应内容长度 response's content length
 *     %D 耗时
 *     [%i{X-Forwarded-For}]
 *     "%r" 请求地址
 * </pre>
 * <pre>
 *     b77af958-6795-4174-800f-541ad793d785 000 000 exam-webapp 8080 484 2020-09-0110:00:20.633 200 513 6 [183.160.212.93, 58.218.208.59] "GET /exam-webapp/api/newExam/getExamInfo?_=1598925620320&examId=8369 HTTP/1.0"
 *
 *
 *     c3323cc4-10f0-424f-9368-da59d70e2f6d 000 000 cmng 8080 - 2020-10-0507:12:21.032 200 450296 421 [36.5.144.132] "GET /cmng//api/index/getBaseInfo HTTP/1.0"
 *     20f763bf-4140-4c65-bad9-e996f2a65299 000 000 cmng 8080 - 2020-10-0507:12:23.794 200 12304 3170 [36.5.144.132] "GET /cmng//api/index/getPage?_=1601853110242&region=&phase=&keyword=&pageIndex=1&pageSize=50 HTTP/1.0"
 *     d4f46e67-c377-4156-8467-4519791e4a0c 000 000 cmng 8080 - 2020-10-0507:12:27.992 200 22 5032 [36.5.144.132] "POST /cmng//login HTTP/1.0"
 *     23254d5f-df01-4217-9eb2-82055d85fd59 000 000 cmng 8080 - 2020-10-0507:12:28.699 200 450296 597 [36.5.144.132] "GET /cmng//api/index/getBaseInfo HTTP/1.0"
 *     fa8a5301-d882-407e-bd70-bced8dc65f75 000 000 cmng 8080 - 2020-10-0507:12:29.092 200 12304 974 [36.5.144.132] "GET /cmng//api/index/getPage?_=1601853117835&region=&phase=&keyword=&pageIndex=1&pageSize=50 HTTP/1.0"
 *     ea56443d-a56f-4645-9fc3-d8cc40c5b332 000 000 cmng 8080 - 2020-10-0507:13:02.355 200 12304 645 [36.5.144.132] "GET /cmng//api/index/getPage?_=1601853151431&region=&phase=&keyword=&pageIndex=1&pageSize=50 HTTP/1.0"
 *     91cee60f-f303-4e76-981b-06582dbb170a 000 000 cmng 8080 - 2020-10-0507:13:27.967 200 12304 827 [36.5.144.132] "GET /cmng//api/index/getPage?_=1601853176867&region=&phase=&keyword=&pageIndex=1&pageSize=50 HTTP/1.0"
 *     3a6b7b9f-a2a3-4cfc-9ac1-e9a28c8e0c22 000 000 cmng 8080 - 2020-10-0507:13:32.975 200 12304 738 [36.5.144.132] "GET /cmng//api/index/getPage?_=1601853181958&region=&phase=&keyword=&pageIndex=1&pageSize=50 HTTP/1.0"
 *     48bae0e4-dbca-4be5-be1f-2d91d740d14f 000 000 cmng 8080 - 2020-10-0507:19:05.434 200 12304 751 [36.5.144.132] "GET /cmng//api/index/getPage?_=1601853514286&region=&phase=&keyword=&pageIndex=1&pageSize=50 HTTP/1.0"
 *     6e452c56-8910-49f8-8d86-adcd1987ecfd 000 000 cmng 8080 - 2020-10-0507:20:36.757 200 12304 717 [36.5.144.132] "GET /cmng//api/index/getPage?_=1601853605687&region=&phase=&keyword=&pageIndex=1&pageSize=50 HTTP/1.0"
 *
 * </pre>
 *
 * @author leichu 2020-09-01.
 */
@Alias("access")
public class AccessExtractor implements Extractor {

	@Override
	public List<LogFactory.Log> extract(List<String> content) {
		List<LogFactory.Log> logs = Collections.synchronizedList(Lists.newArrayList());
		if (CollectionUtils.isEmpty(content)) {
			return logs;
		}
		String ip = NetUtils.getIp();
		content.parallelStream().forEach(line -> {
			if (StringUtils.isBlank(line)) {
				return;
			}
			//  [36.5.144.132, 183.160.212.93, 58.218.208.59] \"POST /cmng//login HTTP/1.0\""
			Matcher ipMatcher = Pattern.compile("\\[(.*?)\\]").matcher(line);
			String clientIp = "";
			int ipEnd = -1;
			if (ipMatcher.find()) {
				clientIp = ipMatcher.group(1);
				ipEnd = ipMatcher.end();
			}
			String subStr = line.substring(ipEnd).trim().replaceAll("^\"(.*?)", "$1").replaceAll("\"$", "");
			String[] subSplit = subStr.split(" ");

			List<String> split = Splitter.on(" ").splitToList(line);
			LogFactory.Log log = LogFactory.Log.newBuilder()
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
									.setConsume(StringUtils.isNotBlank(split.get(9)) && StringUtils.isNumeric(split.get(9)) ? Integer.parseInt(split.get(9)) : 0)
//									.setIp(split.get(10) + split.get(11)) // 此处有bug,因为IP地址可能是一个 形如 [36.5.144.132]，也可能是多个，形如 [183.160.212.93, 58.218.208.59]
									.setIp(clientIp)
									.setRequestMethod(subSplit[0])
									.setRequestURL(subSplit[1])
									.setParams(subSplit[1].contains("?") ? subSplit[1].substring(subSplit[1].indexOf("?") + 1) : "")
					)
					.build();
			logs.add(log);
		});
		return logs;
	}

}
