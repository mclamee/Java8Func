package com.wicky.violation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DateCleaner {

	private static String projectRoot = "C:/Users/williamz/Desktop/local/acf/CRONS/acf-common";

	private static String defaultCharset = "utf8";

	public static void main(String[] args) {

		projectRoot = Paths.get(".").toAbsolutePath().normalize().toString();

		System.out.println("Date Getter/Setter Cleaner initialized, current Project Root: " + projectRoot);
		System.out.println(
				"--------------------------------------------------------------------------------------------------------");

		int failure = 0;
		// console command
		Scanner scanner = new Scanner(System.in);
		printMenu();
		while (true) {
			System.out.print("> ");
			String line = scanner.nextLine();
			if (line.equals("1")) {
				failure = 0;
				cleanUpDateGetterSetters();
			} else if (line.equals("2")) {
				failure = 0;
				rollback();
			} else if (line.equals("3")) {
				failure = 0;
				// dangerous! Uncomment this you may lose your original file.
				// deleteBackup();
			} else if (line.equalsIgnoreCase("q") || line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
				System.out.println("Exiting ...");
				break;
			} else if (line.equalsIgnoreCase("m") || line.equalsIgnoreCase("h") || line.equalsIgnoreCase("help")
					|| line.equalsIgnoreCase("man") || line.equalsIgnoreCase("menu")) {
				failure = 0;
				printMenu();
			} else {
				failure++;
				if (failure < 3) {
					System.out.println("Invalid command, please retry... Type h for help.");

				} else {
					System.out.println("Too many retry, exiting...");

					break;
				}
			}
		}
	}

	private static void printMenu() {
		System.out.println("Hello, please type a command: \r\n" + "  [1] Cean up Date Getter & Setters.\r\n"
				+ "  [2] Rollback clean.\r\n" + "  [3] (Danger!) Purge all backup files.\r\n" + "  [m] Show menu\r\n"
				+ "  [q] Quit");
	}

	private static void cleanUpDateGetterSetters() {
		try (Stream<Path> stream = Files.find(Paths.get(projectRoot, "/src"), 100,
				(path, attr) -> path.getFileName().toString().endsWith(".java") && attr.isRegularFile());) {

			stream.map(f -> replaceGetterAndSetters(f)).filter(f -> f.changed).forEach(f -> {
				System.out.println(f);
				try {
					Path backPath = Paths.get(f.file.toString() + ".bak");
					System.out.println("Creating Backup File: " + backPath);

					Files.copy(f.file, backPath);
					String content = f.content;
					Files.write(f.file, content.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Date getter setter clean up Done!");
	}

	private static void rollback() {
		rollback(false);
		System.out.println("Rollback Done!");
	}

	private static void deleteBackup() {
		rollback(false);
		System.out.println("Delete Backup Files Done!");
	}

	private static void rollback(boolean deleteBackupOnly) {
		try (Stream<Path> stream = Files.find(Paths.get(projectRoot, "/src"), 100, (path, attr) -> {
			// found file with its backup file
			if (path.getFileName().toString().endsWith(".java.bak") && attr.isRegularFile()) {
				File[] files = path.getParent().toFile()
						.listFiles(f -> (f.toString() + ".bak").equals(path.toFile().toString()));
				if (files.length == 1) {
					return true;
				}
			}
			return false;
		});) {

			stream.forEach(bak -> {
				String file = bak.toString().substring(0, bak.toString().length() - 4);
				System.out.println(file);

				if (deleteBackupOnly) {
					try {
						System.out.println("Deleting backup File: " + bak);
						Files.delete(bak);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						System.out.println("Rolling back file from backup: " + bak);
						Files.delete(Paths.get(file));
						Files.move(bak, Paths.get(file));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			});

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ChangedFile replaceGetterAndSetters(Path filePath) {
		String content = getFileContentAsString(filePath);

		String replaced = regReplace(content,
				"(public\\s+Date\\s+get.+\\s*\\(\\)\\s*\\{\\s*\\n(\\s*))(return\\s+(this\\.)?(.+);)([^.]\\s*})", ma -> {

					String header = ma.group(1);
					String spacer = ma.group(2);
					// String expression = ma.group(3);
					// String thisPrefix = ma.group(4);
					String field = ma.group(5).trim();
					String tail = ma.group(6);

					// System.out.println("Getter >>" + ma.group() + "|" + header + "|" + spacer +
					// "|" + expression + "|"
					// + field + "|" + tail);

					StringBuilder sb = new StringBuilder();
					sb.append(header);
					sb.append("if(this.");
					sb.append(field);
					sb.append(" == null) {\r\n");
					sb.append(spacer);
					sb.append("\treturn null;\r\n");
					sb.append(spacer);
					sb.append("} else {\r\n");
					sb.append(spacer);
					sb.append("\treturn (Date) this.");
					sb.append(field);
					sb.append(".clone();\r\n");
					sb.append(spacer);
					sb.append("}");
					sb.append(tail);

					return sb.toString();
				},
				str -> regReplace(str,
						"(public\\s+void\\s+set.+\\s*\\(Date\\s+(.+)\\)\\s*\\{\\s*\\n(\\s*))((this\\.)?(.+)=\\s*.*\\s*;)(\\s*})",
						ma -> {
							String header = ma.group(1);
							String param = ma.group(2).trim();
							String spacer = ma.group(3);
							// String expression = ma.group(4);
							// String thisPrefix = ma.group(5);
							String field = ma.group(6).trim();
							String tail = ma.group(7);

							// System.out.println("Setter >>" + ma.group() + "|" + header + "|" + param +
							// "|" + spacer
							// + "|" + expression + "|" + thisPrefix + "|" + field + "|" + tail + "|");

							StringBuilder sb = new StringBuilder();
							sb.append(header);
							sb.append("if(");
							sb.append(param);
							sb.append(" == null) {\r\n");
							sb.append(spacer);
							sb.append("\tthis.");
							sb.append(field);
							sb.append(" = null;\r\n");
							sb.append(spacer);
							sb.append("} else {\r\n");
							sb.append(spacer);
							sb.append("\tthis.");
							sb.append(field);
							sb.append(" = (Date) ");
							sb.append(param);
							sb.append(".clone();\r\n");
							sb.append(spacer);
							sb.append("}");
							sb.append(tail);

							return sb.toString();
						}, s -> s));

		// System.out.println("====================================Replaced
		// Content====================================");
		// System.out.println(replaced);
		// System.out.println("========================================================================================");

		return new ChangedFile(filePath, replaced, !content.equals(replaced));
	}

	private static String getFileContentAsString(Path filePath) {
		System.out.println("Reading File: " + filePath);
		byte[] bytes = null;
		try {
			bytes = Files.readAllBytes(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String content = new String(bytes, Charset.forName(defaultCharset));
		return content;
	}

	private static String regReplace(String content, String regex, Function<Matcher, String> getReplacement,
			Function<String, String> postAction) {
		Pattern pattern = Pattern.compile(regex);
		Matcher ma = pattern.matcher(content);
		StringBuffer buffer = new StringBuffer();
		while (ma.find()) {
			System.out.println("Found   > " + ma.group());
			String replacement = getReplacement.apply(ma);
			System.out.println("Replace > " + replacement);

			ma.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
		}
		ma.appendTail(buffer);
		return postAction.apply(buffer.toString());
	}

	static class ChangedFile {
		public ChangedFile(Path filePath, String replaced, boolean changed) {
			this.file = filePath;
			this.content = replaced;
			this.changed = changed;
		}

		boolean changed;
		Path file;
		String content;

		@Override
		public String toString() {
			return ">>>>>>>>>>>>>>>>>>>" + file + ">>>>>>>>>>>>>>>>>>>\r\n" + content
					+ "\r\n<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<";
		}
	}

}
