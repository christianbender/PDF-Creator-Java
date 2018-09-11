import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;


public class TestPDFCreator {

	@Before
	public void setUp() throws Exception {
	}

//	@Test
//	public void testGenerateLines() {
//		String content = "hhijijdiedih1\n2fssssqqejsksllskakkdllslssjaalalksksksjskskaklskjsjsslslsskssffddddd\n" +
//				"ffmmfdd,,s,smdmmfjdjsjkala28291mdlwooqoo2933llslalalalkksllalalalakakaa";
//		ArrayList<String> lines = PDFCreator.generateLines(content);
//		for (String line : lines) {
//			System.out.println(line);
//		}
//	}
	
	@Test
	public void testCreateRefTab() {
		String datName = "testFile.txt";
		String content = "";
		File file = new File(datName);

        if (!file.canRead() || !file.isFile())
            System.exit(0);

            BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(datName));
            String line = null;
            while ((line = in.readLine()) != null) {
                content += line + "\n";
            }
            
            System.out.println(PDFCreator.createRefTab(content, 6));
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

}
