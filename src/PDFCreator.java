import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class PDFCreator {

	public static int NUMLINES = 58;
//	public static final int NUMLINES_HEADER = 56;

	// static parts
	public static final String BEGIN = "%PDF-1.4\n";
	public static final String OBJECT2 = "2 0 obj\n<< /Type /Catalog\n/Pages 3 0 R\n>>\nendobj\n";
	public static final String OBJECT3 = "3 0 obj\n<< /Type /Pages\n/MediaBox "
			+ "[0 0 595 842]\n/Resources\n<< /Font << /F1 4 0 R >>\n  /ProcSet "
			+ "[/PDF /Text]\n>>\n/Kids []\n /Count  \n>>\nendobj\n";
	public static final String OBJECT4 = "4 0 obj\n<< /Type /Font\n/Subtype "
			+ "/Type1\n/BaseFont /Helvetica\n/Encoding /WinAnsiEncoding\n>>\nendobj\n";

	// dynamic parts
	public static final String OBJECT5 = "5 0 obj\n<< /Type /Page\n/Parent 3 0 "
			+ "R\n/Contents 6 0 R\n>>\nendobj";

	public static final String REFTAB = "xref\n0 7\n0000000000 65535 f "
			+ "\n0000000009 00000 n \n0000000050 00000 n \n0000000102 00000 n "
			+ "\n0000000268 00000 n \n0000000374 00000 n \n0000000443 00000 n \n";

	public static final String TRAILER = "trailer\n<< /Size 7\n/Info 1 0 R\n/Root 2 0 R\n>>\n";

	// global flag for method createPDFMulti(...)
	// this flag will be set in the method createContentObject(...) 
	public static boolean headerFlag = false;
	
	public static String createObject1(String title) {
		return String.format("1 0 obj\n<< /Title %s >>\nendobj\n", title);
	}

	public static String createObject6(int length, ArrayList<String> content) {
		String ans = String.format("6 0 obj\n<< /Length %d\n>>\nstream\n",
				length);
		int x = 20;
		int y = 800;
		ans += "/F1 12 Tf\n";
		for (int i = 0; i < content.size(); i++) {
			ans += "BT\n";
			ans += String.format("%d %d Td\n", x, y);
			ans += String.format("(%s) Tj\n", content.get(i));
			ans += "ET\n";
			y -= 14; // can be a problem
			if (y < 0)
				break;
		}
		ans += "endstream\nendobj\n";
		return ans;
	}

	public static String createObject3(int[] kids, int count) {
		String ans = "3 0 obj\n<< /Type /Pages\n/MediaBox "
				+ "[0 0 595 842]\n/Resources\n<< /Font << /F1 4 0 R >>\n  /ProcSet "
				+ "[/PDF /Text]\n>>\n/Kids [";
		for (int k : kids) {
			ans += String.format("%d 0 R ", k);
		}
		ans += String.format("]\n /Count %d\n>>\nendobj\n", count);
		return ans;
	}

	public static String createPageObject(int its, int content) {
		return String.format("%d 0 obj\n<< /Type /Page\n/Parent 3 0 "
				+ "R\n/Contents %d 0 R\n>>\nendobj\n", its, content);
	}

	public static int getPositionRef(String file) {
		return file.indexOf("xref");
	}

	public static String createEnd(String file) {
		return String.format("startxref\n%d\n", getPositionRef(file))
				+ "%%EOF\n";
	}

	public static String createPDF(ArrayList<String> lines, String title) {
		String content = "";
		content += BEGIN;
		content += createObject1(title);
		content += OBJECT2 + OBJECT3 + OBJECT4 + OBJECT5;
		content += createObject6(41, lines);
		content += REFTAB + TRAILER;
		content += createEnd(content);
		return content;
	}

	public static ArrayList<String> createPartition(int i,
			ArrayList<String> lines, int numLines) {
		ArrayList<String> list = new ArrayList<String>();
		int j = i;
		int counter = 0;
		boolean loop = true;
		while (j < lines.size() && loop) {
			list.add(lines.get(j));
			counter++;
			if (counter == numLines)
				loop = false;
			j++;
		}
		return list;
	}

	public static String addKids(String content, ArrayList<Integer> pages) {
		String ans = "";

		int indexKids = content.indexOf("/Kids");
		while (content.charAt(indexKids) != '[') {
			indexKids++;
		}

		indexKids++;
		ans += content.substring(0, indexKids);

		for (int page : pages) {
			ans += String.format("%d 0 R ", page);
		}

		ans += "]";
		ans += content.substring(indexKids + 1, content.length() - 1);
		content = ans;

		int indexCount = content.indexOf("/Count");
		while (content.charAt(indexCount) != 't') {
			indexCount++;
		}
		indexCount++;
		ans = content.substring(0, indexCount);
		ans += " " + pages.size();
		ans += content.substring(indexCount + 1);

		return ans;
	}

	public static String createPDFMulti(ArrayList<String> lines, String title) {

		// static parts
		String content = BEGIN;
		content += createObject1(title);
		content += OBJECT2 + OBJECT3 + OBJECT4;

		ArrayList<Integer> pages = new ArrayList<Integer>();
		ArrayList<Integer> contents = new ArrayList<Integer>();

		int currentObj = 4;
		int numObjects = 4;
		int index = 0;

		int n = (lines.size() / NUMLINES) + 1;

		for (int i = 0; i < n; i++) {

			do {
				currentObj++;
			} while (pages.contains(currentObj)
					|| contents.contains(currentObj));

			// content += createContentObject(currentObj, createPartition(index,
			// lines));
			content += createPageObject(currentObj, currentObj + 1);
			content += createContentObject(currentObj + 1,
					createPartition(index, lines, NUMLINES));
			pages.add(currentObj);
			contents.add(currentObj + 1);

			numObjects += 2;
			index += NUMLINES;
			headerFlag = false;
		}

		// System.err.printf("\t AFTER for-loop content=%s\n", content); //DEBUG

		content += createRefTab(content, numObjects);

		// System.err.printf("\t AFTER 1. content=%s\n", content); //DEBUG

		content += createTrailer(numObjects);

		// System.err.printf("\t AFTER 2. content=%s\n", content); //DEBUG

		content += createEnd(content);
		// content += String.format("startxref\n %d \n",
		// getPositionRef(content)) + "%%EOF\n";
		content = addKids(content, pages);

		// dynamic parts
		// System.out.println(content);
		return content;
	}

	public static int findObject(String content, int i) {
		return content.indexOf(String.format("%d 0 obj", i));
	}

	public static String generatePositionRef(int num) {
		String ans = "";
		int digits = Integer.toString(num).length();
		for (int i = 0; i < (10 - digits); i++) {
			ans += "0";
		}
		ans += Integer.toString(num);
		return ans;
	}

	public static String createRefTab(String content, int numObjects) {
		String ans = String.format("xref\n0 %d\n0000000000 65535 f \n",
				numObjects + 1);
		for (int i = 1; i <= numObjects; i++) {
			ans += String.format("%s 00000 n \n",
					generatePositionRef(findObject(content, i)));
		}
		return ans;
	}

	public static String createContentObject(int its, ArrayList<String> lines) {
		String ans = String.format(
				"%d 0 obj\n<< /Length 41\n>>\nstream\n/F1 12 Tf\n", its);
		int x = 20;
		int y = 800;
		int i = 0;
		String format = "";
		NUMLINES = 58;
		for (String line : lines) {
			
			format = "/F1 12 Tf\n";
			if ((i = containsHeader(line)) != -1) {
				format = "/F1 48 Tf\n";
				y -= 40;
				headerFlag = true;
				line = cleanHeader(line, i);
				NUMLINES -= 3;
			} 
//			else if (header){
//				ans += "/F1 12 Tf";
//				header = false;
//			}
			
			ans += String.format("BT\n %s \n%d %d Td\n(%s) Tj\nET\n",format, x, y, line);
			if (y < 0)
				break;
			y -= 14;
			
		}
		ans += "endstream\nendobj\n";
		return ans;
	}

	public static String createTrailer(int numObjects) {
		return String.format(
				"trailer\n<<\n /Size %d\n/Info 1 0 R\n/Root 2 0 R\n>>\n",
				numObjects + 1);
	}

	public static ArrayList<String> generateLines(String content) {
		ArrayList<String> list = new ArrayList<String>();
		char arr[] = content.toCharArray();
		int counter = 0;
		int index = 0;
		final int MAXLINE = 100;

		while (index < arr.length) {
			String line = "";

			while (counter < MAXLINE && index < arr.length) {
				if (arr[index] == '\n') {
					index++;
					break;
				}
				line += arr[index];
				index++;
				counter++;
			}

			counter = 0;
			list.add(line);
		}

		return list;
	}
	
	/**
	 * Scans for formatting statement.
	 * Returns -1 if none formatting statement exists. Otherwise its index.
	 */
	public static int containsHeader(String line) {
		return line.indexOf("/header");
	}
	
	public static String cleanHeader(String line, int index) {
		return line.substring(index+7, line.length());
	}
	
	public static String getName(String path) {
		String name = "";
		int i = 0;
		
		while(path.charAt(i) != '.') {
			name += path.charAt(i);
			i++;
		}
		
		return name;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// String path = "";
		String content = "";

		ArrayList<String> lines;

		if (args.length >= 1) {
			// path = args[0];

			for (String path : args) {
				
				content = "";

				File file = new File(path);
				
				// error case
				if (!file.canRead() || !file.isFile()) {
					System.err.printf("Can't open %s file!\n", path);
					continue;
				}
					

				BufferedReader in = null;
				try {
					in = new BufferedReader(new FileReader(path));
					String line = null;
					while ((line = in.readLine()) != null) {
						content += line + "\n";
					}

					PrintWriter pWriter = null;
					try {
						pWriter = new PrintWriter(new BufferedWriter(
								new FileWriter(getName(path) + ".pdf")), true);
						lines = generateLines(content);
						System.out.printf("The PDF %s contains %d lines\n",
								(getName(path) + ".pdf"),lines.size());
						pWriter.print(createPDFMulti(lines, "Converted"));
					} catch (IOException ioe) {
						ioe.printStackTrace();
					} finally {
						if (pWriter != null) {
							pWriter.flush();
							pWriter.close();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (in != null)
						try {
							in.close();
						} catch (IOException e) {
						}
				}
			}
		} else {
			System.err.println("Please give some text files.\n" +
					"PDFCreator.jar [file1] [file2] ... [fileN]");
		}
	}

}
