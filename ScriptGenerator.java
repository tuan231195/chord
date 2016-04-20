import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ScriptGenerator {
	private static final int NUM_STMT = 100000;
	private static List<String> list = new ArrayList<>();
	public static void main(String[] args) throws FileNotFoundException {
		PrintWriter printWriter = new PrintWriter(new File("data3.dat"));
		Random rnd = new Random();
		int size = 1 + rnd.nextInt(32);
		long chordSize = (1 << size);
		printWriter.println("initchord " + size);
		for (int i = 0; i < NUM_STMT; i++)
		{
			int randomNum = rnd.nextInt(30);
			if (randomNum < 10)
			{
				long randomPeer = Math.abs(rnd.nextLong() % (chordSize - 1)) + 1;
				printWriter.println("addpeer " + randomPeer);
			}
			if (randomNum >= 10 && randomNum < 20)
			{
				long randomPeer = Math.abs(rnd.nextLong() % (chordSize - 1)) + 1;
				printWriter.println("print " + randomPeer);
			}
			if (randomNum >= 20 && randomNum < 25)
			{
				String randomString = UUID.randomUUID().toString();
				list.add(randomString);
				printWriter.println("insert " + randomString);
			}
			if (25 <= randomNum && randomNum <= 27)
			{
				long randomPeer = Math.abs(rnd.nextLong() % (chordSize - 1)) + 1;
				printWriter.println("removepeer " + randomPeer);
			}
			else if (27 < randomNum && randomNum <= 30)
			{
				if (list.size() != 0)
				{
					int randomIdx = rnd.nextInt(list.size());
					printWriter.println("delete " + list.get(randomIdx));
				}
			}
			
		}
		
		printWriter.close();
	}
}
