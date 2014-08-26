/*
 * Athena Item Name Checker
 * Copyright (C) 2014 Jeffrey Han
 *
 * Athena Item Name Checker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Athena Item Name Checker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Athena Item Name Checker.  If not, see <http://www.gnu.org/licenses/>.
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
	 * @param outputFile the output file name
	 */
	public static void run(String serverFile, String clientFile, String outputFile) {
		// client-side ID/name map
		TreeMap<Integer, String> nameMap = new TreeMap<Integer, String>();

		// server-side ID/name map (differences only)
		TreeMap<Integer, String> diffMap = new TreeMap<Integer, String>();

		parseNameDB(new File(clientFile), nameMap);
		parseItemDB(new File(serverFile), nameMap, diffMap);
		writeDifferences(new File(outputFile), nameMap, diffMap);
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
			System.err.printf("Failed to read file '%s'.", clientDB.getName());
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
			System.err.printf("Failed to read file '%s'.", serverDB.getName());
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Writes item name differences to output file.
	 * @param outputFile the output file
	 * @param nameMap the client-side ID/name map
	 * @param diffMap the server-side ID/name map for differences
	 */
	private static void writeDifferences(File outputFile, TreeMap<Integer, String> nameMap,
			TreeMap<Integer, String> diffMap) {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"))) {
			if (diffMap.isEmpty()) {
				writer.write("No differences found.");
				writer.newLine();
			} else {
				writer.write(String.format("Found %d differences.", diffMap.size()));
				writer.newLine();
				writer.newLine();
				for (Map.Entry<Integer, String> entry : diffMap.entrySet()) {
					int id = entry.getKey();
					writer.write(String.format("Item %d:", id));
					writer.newLine();
					writer.write(String.format("\t(server) %s", entry.getValue()));
					writer.newLine();
					writer.write(String.format("\t(client) %s", nameMap.get(id)));
					writer.newLine();
				}
			}
		} catch (IOException e) {
			System.err.printf("Failed to write to file '%s'.", outputFile.getName());
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Runs the name checker.
	 * Non-default file names can be supplied as arguments.
	 * @param args server_file client_file output_file
	 */
	public static void main(String[] args) {
		if (args.length == 0)
			run("item_db.txt", "idnum2itemdisplaynametable.txt", "output.txt");
		else if (args.length == 3)
			run(args[0], args[1], args[2]);
		else {
			System.out.println("usage:");
			System.out.println("    java AthenaItemNameChecker");
			System.out.println("    - runs using default names");
			System.out.println("      (item_db.txt, idnum2itemdisplaynametable.txt, output.txt)");
			System.out.println();
			System.out.println("    java AthenaItemNameChecker server_file client_file output_file");
			System.out.println("    - runs using supplied file names");
		}
	}
}