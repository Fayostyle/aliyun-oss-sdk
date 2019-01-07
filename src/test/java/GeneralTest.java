import com.fayostyle.oss.support.FileTypePredication;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author keith.huang
 * @date 2019/1/5 16:38
 */
public class GeneralTest {

    @Test
    public void hexString() {
        System.out.println(Integer.toHexString(5));
        System.out.println(Integer.toHexString(15));
        System.out.println(Integer.toHexString(16));
        System.out.println(Integer.toHexString(17));
        System.out.println(Integer.toHexString(255));
        System.out.println(Integer.toHexString(256));
        System.out.println(Integer.toHexString(257));
    }

    @Test
    public void splitString() throws IOException {
        InputStream input = GeneralTest.class.getResourceAsStream("/mime.mapping");
        BufferedReader bf = new BufferedReader(new InputStreamReader(input));
        Map<String, String> map1 = new HashMap<>(10);
        Map<String, String> map2 = new HashMap<>(10);
        String line = null;

        while((line = bf.readLine()) != null) {
            line = line.trim();

            if(line.startsWith("#") || line.length() == 0) {

            } else {
                StringTokenizer st = new StringTokenizer(line, " \t");
                if(st.countTokens() > 1) {
                    String extension = st.nextToken();
                    if(st.hasMoreTokens()) {
                        String mimeType = st.nextToken();
                        map1.put(extension, mimeType);
                    }
                }

                String[] args = line.split(" ", 2);
                map2.put(args[0].trim(), args[1].trim());
            }
        }

        for(Map.Entry entry : map1.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        System.out.println("---------------------");

        for(Map.Entry entry : map2.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    @Test
    public void stTest() throws IOException {
        InputStream input = GeneralTest.class.getResourceAsStream("/mime.mapping");
        Map<String, String> map = FileTypePredication.loadResource(input);
        for(Map.Entry entry : map.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }

    }
}
