/*
 * Athena Item Name Checker
 * Copyright (C) 2014 Jeffrey Han
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Checks the item names in an Athena item database against a client-side
 * Ragnarok Online item name table.
 *
 * @author itdelatrisu
 */
public class AthenaItemNameChecker {
	/**
	 * Runs the name checker.
	 * @param serverFile the server-side item database file name
	 * @param clientFile the client-side item table file name
	 * @param outputDiffFile the comma-separated output diff file name
	 * @param outputDiffFormattedFile the formatted output diff file name
	 * @param outputDBFile the output database file name
	 */
	public static void run(String serverFile, String clientFile, String outputDiffFile,
			String outputDiffFormattedFile, String outputDBFile) {
		// client-side ID/name map
		TreeMap<Integer, String> nameMap = new TreeMap<Integer, String>();

		// server-side ID/name map (differences only)
		TreeMap<Integer, String> diffMap = new TreeMap<Integer, String>();

		parseNameDB(new File(clientFile), nameMap);
		parseItemDB(new File(serverFile), nameMap, diffMap);
		writeDifferences(new File(outputDiffFormattedFile), new File(outputDiffFile), nameMap, diffMap);
		writeNewDatabase(new File(outputDBFile), new File(serverFile), new File(outputDiffFile));
	}

	/**
	 * Runs the name checker.
	 * @param serverFile the server-side item database file name
	 * @param diffFile the comma-separated differences file name
	 * @param outputDBFile the output database file name
	 */
	public static void run(String serverFile, String diffFile, String outputDBFile) {
		writeNewDatabase(new File(outputDBFile), new File(serverFile), new File(diffFile));
	}

	/**
	 * Parses client-side item name file.
	 * @param clientDB the client-side item table file
	 * @param nameMap the client-side ID/name map
	 */
	private static void parseNameDB(File clientDB, TreeMap<Integer, String> nameMap) {
		try (BufferedReader in = new BufferedReader(new FileReader(clientDB))) {
			String line;
			while ((line = in.readLine()) != null) {
				if (line.isEmpty() || line.charAt(0) == '/')
					continue;
				String[] tokens = line.split("#");
				try {
					nameMap.put(Integer.parseInt(tokens[0]), tokens[1].replace('_', ' ').trim());
				} catch (Exception e) {
					continue;
				}
			}
		} catch (IOException e) {
			System.err.printf("Failed to read file '%s'.\n", clientDB.getName());
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Parses server-side item database file and checks for differences.
	 * @param serverDB the server-side item database file
	 * @param nameMap the client-side ID/name map
	 * @param diffMap the server-side ID/name map for differences
	 */
	private static void parseItemDB(File serverDB, TreeMap<Integer, String> nameMap,
			TreeMap<Integer, String> diffMap) {
		try (BufferedReader in = new BufferedReader(new FileReader(serverDB))) {
			String line;
			while ((line = in.readLine()) != null) {
				if (line.isEmpty() || line.charAt(0) == '/')
					continue;
				try {
					// parse ID
					int startIndex = 0;
					int endIndex   = line.indexOf(',');
					int id = Integer.parseInt(line.substring(startIndex, endIndex));
					if (!nameMap.containsKey(id))
						continue;  // not in client-side table

					// parse English name
					startIndex = line.indexOf(',', endIndex + 1) + 1;
					endIndex   = line.indexOf(',', startIndex);
					String name = line.substring(startIndex, endIndex);

					// compare against client-side name
					if (!name.equalsIgnoreCase(nameMap.get(id)))
						diffMap.put(id, name);
				} catch (Exception e) {
					continue;
				}
			}
		} catch (IOException e) {
			System.err.printf("Failed to read file '%s'.\n", serverDB.getName());
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Writes item name differences to output file.
	 * @param outputFile the comma-separated output file
	 * @param outputFormattedFile the formatted output file
	 * @param nameMap the client-side ID/name map
	 * @param diffMap the server-side ID/name map for differences
	 */
	private static void writeDifferences(File outputFile, File outputFormattedFile,
			TreeMap<Integer, String> nameMap, TreeMap<Integer, String> diffMap) {
		try (
			BufferedWriter writer1 = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
			BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFormattedFile), "UTF-8"));
		) {
			if (diffMap.isEmpty()) {
				writer1.write("No differences found.");
				writer1.newLine();
				writer2.newLine();
			} else {
				writer1.write(String.format("Found %d differences.", diffMap.size()));
				writer1.newLine();
				writer1.newLine();
				for (Map.Entry<Integer, String> entry : diffMap.entrySet()) {
					int id = entry.getKey();
					String serverName = entry.getValue();
					String clientName = nameMap.get(id);
					writer1.write(String.format("Item %d:", id));
					writer1.newLine();
					writer1.write(String.format("\t(server) %s", serverName));
					writer1.newLine();
					writer1.write(String.format("\t(client) %s", clientName));
					writer1.newLine();
					writer2.write(String.format("%d,%s,%s", id, serverName, clientName));
					writer2.newLine();
				}
			}
		} catch (IOException e) {
			System.err.printf("Failed to write to output files '%s' and '%s'.\n",
					outputFile.getName(), outputFormattedFile.getName());
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Writes a new item database using a comma-separated differences file.
	 * @param outputDB the new database file
	 * @param serverDB the server-side item database file
	 * @param diffFile the comma-separated differences file
	 */
	private static void writeNewDatabase(File outputDB, File serverDB, File diffFile) {
		// client-side ID/name map (differences only)
		TreeMap<Integer, String> diffMap = new TreeMap<Integer, String>();

		// read differences file
		try (BufferedReader in = new BufferedReader(new FileReader(diffFile))) {
			String line;
			while ((line = in.readLine()) != null) {
				if (line.isEmpty())
					continue;
				try {
					String[] tokens = line.split(",");
					diffMap.put(Integer.parseInt(tokens[0]), tokens[2]);
				} catch (Exception e) {
					continue;
				}
			}
		} catch (IOException e) {
			System.err.printf("Failed to read file '%s'.\n", diffFile.getName());
			e.printStackTrace(System.err);
		}

		// write new database file
		try (
			BufferedReader in = new BufferedReader(new FileReader(serverDB));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputDB), "UTF-8"))
		) {
			String line;
			while ((line = in.readLine()) != null) {
				if (line.isEmpty() || line.charAt(0) == '/') {
					writer.write(line);
					writer.newLine();
					continue;
				}

				// parse ID
				int startIndex = 0;
				int endIndex   = line.indexOf(',');
				int id = Integer.parseInt(line.substring(startIndex, endIndex));
				if (!diffMap.containsKey(id)) {
					// not in differences table
					writer.write(line);
					writer.newLine();
					continue;
				}

				// write new entry
				startIndex = line.indexOf(',', endIndex + 1) + 1;
				endIndex   = line.indexOf(',', startIndex);
				writer.write(line.substring(0, startIndex));
				writer.write(diffMap.get(id));
				writer.write(line.substring(endIndex));
				writer.newLine();
			}
		} catch (IOException e) {
			System.err.printf("Failed to read file '%s' and write to output file '%s'.\n",
					serverDB.getName(), outputDB.getName());
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Runs the name checker.
	 * Non-default file names can be supplied as arguments.
	 * @param args server_file client_file output_diff_file output_diff_formatted_file output_db_file
	 */
	public static void main(String[] args) {
		String[] defaultNames = {
			"item_db.txt",
			"idnum2itemdisplaynametable.txt",
			"output_diff.txt",
			"output_diff_formatted.txt",
			"output_db.txt"
		};

		if (args.length == 0)
			run(defaultNames[0], defaultNames[1], defaultNames[2], defaultNames[3], defaultNames[4]);
		else if (args.length == 1 && !args[0].equalsIgnoreCase("help"))
			run(defaultNames[0], args[0], defaultNames[4]);
		else if (args.length == 3)
			run(args[0], args[1], args[2]);
		else if (args.length == 5)
			run(args[0], args[1], args[2], args[3], args[4]);
		else {
			System.out.println("usage:");
			System.out.println("    java AthenaItemNameChecker help");
			System.out.println("    - shows usage information");
			System.out.println();
			System.out.println("    java AthenaItemNameChecker");
			System.out.println("    - runs using default names");
			System.out.printf("      (%s, %s, %s, %s, %s)\n", defaultNames[0], defaultNames[1], defaultNames[2], defaultNames[3], defaultNames[4]);
			System.out.println();
			System.out.println("    java AthenaItemNameChecker server_file client_file output_diff_file output_diff_formatted_file output_db_file");
			System.out.println("    - runs using supplied file names");
			System.out.println();
			System.out.println("    java AthenaItemNameChecker diff_file");
			System.out.println("    - generates new database file using default names");
			System.out.printf("      (%s, %s)\n", defaultNames[0], defaultNames[4]);
			System.out.println();
			System.out.println("    java AthenaItemNameChecker server_file diff_file output_db_file");
			System.out.println("    - generates new database file using supplied file names");
		}
	}
}