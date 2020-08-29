package com.cl.log.agent.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileReadTest {

	public static void main(String[] args) throws Exception {
		Path path = Paths.get("E:\\logs\\app.log.2020-08-27");
		long start = System.currentTimeMillis();
		try (Stream<String> lines = Files.lines(path)) {
//			System.out.println(lines.count());
			List<String> line = lines.skip(3099921L).collect(Collectors.toList());
			for (String s : line) {
				System.out.println(s);
			}
		} catch (Exception e) {
			System.err.println(e);
		}
		System.out.println(System.currentTimeMillis() - start);


//		com.google.common.io.Files.readLines(path.toFile(), StandardCharsets.UTF_8 );
//		List<String> readLines = Files.readLines(file, Charsets.UTF_8,new ToListLineProcessor(2));
//		List<String> readLines = com.google.common.io.Files.readLines(path.toFile(), Charsets.UTF_8,new ToListLineProcessor(2));
	}

}
