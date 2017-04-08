import java.io.*;

/**
 * Created by Johan007 on 2017/4/7.
 */
//从文件读入
public class Reader {


    private final FileReader reader;
    private final BufferedReader bufferedReader;

    public Reader(File file) throws FileNotFoundException {
        reader = new FileReader(file);
        bufferedReader = new BufferedReader(reader);
    }

    public String read() throws IOException {
        String line = bufferedReader.readLine();
        return line;
    }

    protected void close() throws IOException, Throwable {
        bufferedReader.close();
        reader.close();
    }
}
