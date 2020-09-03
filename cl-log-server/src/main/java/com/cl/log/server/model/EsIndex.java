package com.cl.log.server.model;

import com.alibaba.fastjson.JSONObject;
import com.cl.log.server.persistence.AbstractRepository;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 索引.
 *
 * @author leichu 2020-06-23.
 */
public class EsIndex implements Serializable {

	private static final long serialVersionUID = 2366449256299348711L;

	private String name;
	private Mapping mapping;

	public EsIndex() {
	}

	public EsIndex(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Mapping getMapping() {
		return mapping;
	}

	public void setMapping(Mapping mapping) {
		this.mapping = mapping;
	}

	public static class Mapping implements Serializable {

		private static final long serialVersionUID = -7517642952922568595L;
		private final List<Prop> props = Lists.newArrayList();

		@Override
		public String toString() {
			return JSONObject.toJSONString(this);
		}
	}

	public static class Prop implements Serializable {

		private static final long serialVersionUID = -2303488773655320621L;
		private String name;
		private String type;
		private String format;
		private String analyzer;
		private String search_analyzer;
		private List<Prop> children;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getFormat() {
			return format;
		}

		public void setFormat(String format) {
			this.format = format;
		}

		public String getAnalyzer() {
			return analyzer;
		}

		public void setAnalyzer(String analyzer) {
			this.analyzer = analyzer;
		}

		public String getSearch_analyzer() {
			return search_analyzer;
		}

		public void setSearch_analyzer(String search_analyzer) {
			this.search_analyzer = search_analyzer;
		}

		public List<Prop> getChildren() {
			return children;
		}

		public void setChildren(List<Prop> children) {
			this.children = children;
		}

		@Override
		public String toString() {
			return JSONObject.toJSONString(this);
		}
	}

	public static void main(String[] args) {
		String path = AbstractRepository.class.getResource("/").getPath();
		path = path.startsWith("/") ? path.substring(1) : path;
		xml2Index(path + "mapping_access.xml");
	}

	public static EsIndex xml2Index(String xmlPath) {
		EsIndex index = new EsIndex();
		Document doc;
		try {
			doc = DocumentHelper.parseText(FileUtils.readFileToString(new File(xmlPath), StandardCharsets.UTF_8));
			Element root = doc.getRootElement();
			index.setName(root.attributeValue("name"));
			List<Element> elements = root.elements();
			List<Prop> props = Lists.newArrayList();
			if (!CollectionUtils.isEmpty(elements)) {
				recursiveSubElement(props, elements);
			}
			Mapping mapping = new Mapping();
			mapping.props.addAll(props);
			index.setMapping(mapping);
		} catch (Exception e) {
			throw new RuntimeException(String.format("解析xml[%s]异常！", xmlPath), e);
		}
		return index;
	}

	private static void recursiveSubElement(List<Prop> props, List<Element> elements) {
		for (Element element : elements) {
			Prop prop = new Prop();
			prop.setName(element.attributeValue("name"));
			prop.setType(element.attributeValue("type"));
			prop.setFormat(element.attributeValue("format"));
			prop.setAnalyzer(element.attributeValue("analyzer"));
			prop.setSearch_analyzer(element.attributeValue("search_analyzer"));
			List<Element> children = element.elements();
			if (!CollectionUtils.isEmpty(children)) {
				List<Prop> propChildren = Lists.newArrayList();
				recursiveSubElement(propChildren, children);
				prop.setChildren(propChildren);
			}
			props.add(prop);
		}
	}

	/**
	 * 构建mapping。
	 *
	 * @param index index.
	 * @return XContentBuilder.
	 * @throws Exception Exception.
	 */
	public static XContentBuilder buildMapping(EsIndex index) throws Exception {
		if (null == index) {
			throw new IllegalArgumentException("入参index不能为空！");
		}
		Mapping mapping = index.getMapping();
		if (null == mapping || CollectionUtils.isEmpty(mapping.props)) {
			throw new IllegalArgumentException("入参index.mapping不能为空！");
		}
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
		{
			build(builder, mapping.props);
		}
		builder.endObject();
		return builder;
	}

	/**
	 * 递归处理mapping属性.
	 * <p>
	 * 参考：https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.x/java-rest-high-put-mapping.html
	 * </p>
	 *
	 * @param builder  builder.
	 * @param children children.
	 * @throws IOException IOException.
	 */
	private static void build(XContentBuilder builder, List<Prop> children) throws IOException {
		builder.startObject("properties");
		{
			for (Prop prop : children) {
				builder.startObject(prop.getName());
				{
					builder.field("type", prop.getType());
					if ("date".equals(prop.getType())) {
						builder.field("format", prop.getFormat());
					}
					if (StringUtils.isNotBlank(prop.getAnalyzer())) {
						builder.field("analyzer", prop.getAnalyzer());
					}
					if (StringUtils.isNotBlank(prop.getSearch_analyzer())) {
						builder.field("search_analyzer", prop.getSearch_analyzer());
					}
					if (!CollectionUtils.isEmpty(prop.getChildren())) {
						build(builder, prop.getChildren());
					}
				}
				builder.endObject();
			}
		}
		builder.endObject();
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
