import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Chord {
	private static Chord chord;

	// this peer is the one with the lowest id
	private Peer referencedPeer;

	private Chord() {

	}

	public static Chord newInstance(int n) {
		// print student name and student ID
		System.out.println("Student name: Van Do Tuan Nguyen");
		System.out.println("Student number: 4752764");
		System.out.println();

		assert (n <= 32 && n >= 0);
		Chord chord = new Chord();

		chord.referencedPeer = new Peer(0, n);
		chord.referencedPeer.join(chord);

		return chord;
	}

	public Peer getReferencedPeer() {
		return referencedPeer;
	}

	public void setReferencedPeer(Peer peer) {
		referencedPeer = peer;
	}

	public static void read(String fileName) throws IOException {
		BufferedReader reader = null;

		try {
			FileInputStream fis = new FileInputStream(fileName);
			RequestHandler handler = new RequestHandler();
			reader = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			while ((line = reader.readLine()) != null) {
				handler.execute(line);
			}
		} finally {
			if (reader != null)
				reader.close();
		}

	}

	public static void main(String[] args) {
		try {
			read(args[0]);

			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class RequestHandler {

		public void execute(String line) {
			if (line == null || line.trim().isEmpty()) {
				return;
			}
			try {
				Scanner scanner = new Scanner(line);
				String command = scanner.next();
				switch (command) {
				case "initchord":
					chord = Chord.newInstance(scanner.nextInt());
					break;
				case "addpeer":
					Long newId = Long.parseLong(scanner.next());
					chord.referencedPeer.addPeer(newId);
					break;
				case "removepeer":
					Long deletedId = Long.parseLong(scanner.next());
					chord.referencedPeer.removePeer(deletedId);
					break;
				case "insert":
					//skip whitespace
					scanner.skip("\\s+");
					//read until comment
					scanner.useDelimiter("#|\\n");
					chord.referencedPeer.insert(scanner.next());
					break;
				case "delete":
					//skip whitespace
					scanner.skip("\\s+");
					//read until comment
					scanner.useDelimiter("#|\\n");
					chord.referencedPeer.delete(scanner.next());
					break;
				case "print":
					Long printedId = Long.parseLong(scanner.next());
					chord.referencedPeer.print(printedId);
					break;
				case "findkey":
					chord.referencedPeer.findKey(scanner.next());
					break;
				}
				//clean up the rest
				scanner.nextLine();
				scanner.close();
			} catch (Exception e) {
				// ignore erro
			}

		}

	}

}
