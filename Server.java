//You: Put your and your parter's names here
//	   Kevin Carlis

//You: Describe which bullet points you implemented
// Yi Er San Yon Cinco

//You: If you did anything worthy of extra credit, put them here
// changed usage for server ./server.sh <port> [players] and added quiz selector

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
	static Integer PLAYERS = 3;
	static Integer thread_count = 0; //How many clients are connected

	static public class Player {
		static Integer ACTIVE = 0;
		static ConcurrentHashMap<Integer,Player> allplayers = new ConcurrentHashMap<Integer,Player>();
		static ConcurrentHashMap<Integer,Player> players = new ConcurrentHashMap<Integer,Player>();
		Integer thread_id;
		String name;
		Integer score = 0;
		Integer questionnum = 0;
		public Player(Integer id, String username) {
			thread_id = id;
			name = username;
		}
	}

	static public class Quiz {
		static ConcurrentHashMap<Integer,String> questions = new ConcurrentHashMap<Integer,String>();
		static ConcurrentHashMap<Integer,String> answers = new ConcurrentHashMap<Integer,String>();
		static boolean start = false;
		static public void select() {
			int q;
			Scanner s = new Scanner(System.in);
			while (true) {
				System.out.println("Select a quiz(0-3)");
				if (s.hasNextInt()) {
					q = s.nextInt();
					if (q >= 0 && q < 4)
						break;
					s.nextLine();
					System.out.println("Enter a valid Integer");
				}else {
					s.nextLine();
					System.out.println("Enter a valid Integer");
				}
			}
			if (q == 0) {
				questions.put(0, "What is the capital of California?");
				answers.put(0, "sacramento");
				questions.put(1, "What is the capital of New York?");
				answers.put(1, "buffalo");
				questions.put(2, "What is the capital of Nevada?");
				answers.put(2, "carson city");
				questions.put(3, "What is the capital of Rhode Island?");
				answers.put(3, "providence");
				questions.put(4, "What is the capital of Florida?");
				answers.put(4, "tallahassee");
				questions.put(5, "What is the capital of North Dakota?");
				answers.put(5, "bismarck");
				questions.put(6, "What is the capital of Vermont?");
				answers.put(6, "montpelier");
			}
			else if (q == 1) {
				questions.put(0, "What country has the most people?");
				answers.put(0, "china");
				questions.put(1, "What is the tallest mountain?");
				answers.put(1, "everest");
				questions.put(2, "Which ocean is the biggest?");
				answers.put(2, "pacific");
				questions.put(3, "What is the longest river?");
				answers.put(3, "nile");
				questions.put(4, "What country does greenland belong to?");
				answers.put(4, "denmark");
				questions.put(5, "What is the smallest country?");
				answers.put(5, "vatican");
				questions.put(6, "What country shares the longest border with France?");
				answers.put(6, "brazil");
			}
			else if (q == 2) {
				questions.put(0, "What is San Diego's only Big 4 sport team?");
				answers.put(0, "padres");
				questions.put(1, "What is the only NBA team to reach the finals 5 times straight since 1970?");
				answers.put(1, "warrior");
				questions.put(2, "Who was the last baseball player to hit over .400?");
				answers.put(2, "williams");
				questions.put(3, "What NFL team has lost the Super Bowl 4 times straight?");
				answers.put(3, "bill");
				questions.put(4, "What hockey penalty is given for sending the puck across two red lines?");
				answers.put(4, "icing");
				questions.put(5, "What team exists in both the NFL and NHL?");
				answers.put(5, "panther");
				questions.put(6, "Basketball courts are typically constructed of which type of wood?");
				answers.put(6, "maple");
			}
			else if (q == 3) {
				questions.put(0, "This capital was formerly known as Constantinople?");
				answers.put(0, "istanbul");
				questions.put(1, "Who famously rode elephants over the alps?");
				answers.put(1, "hannibal");
				questions.put(2, "The Khmer Rouge ruled which country?");
				answers.put(2, "cambodia");
				questions.put(3, "What year did World War I begin?");
				answers.put(3, "1914");
				questions.put(4, "What country was Cleopatra born?");
				answers.put(4, "greece");
				questions.put(5, "What is the name of the first successful colony in Virginia?");
				answers.put(5, "jamestown");
				questions.put(6, "In which country did the Easter Rising of 1916 take place?");
				answers.put(6, "ireland");
			}
			else {
				questions.put(0, "?");
				answers.put(0, "");
				questions.put(1, "?");
				answers.put(1, "");
				questions.put(2, "?");
				answers.put(2, "");
				questions.put(3, "?");
				answers.put(3, "");
				questions.put(4, "?");
				answers.put(4, "");
				questions.put(5, "?");
				answers.put(5, "");
				questions.put(6, "?");
				answers.put(6, "");
			}
		}
	}

	//This is a "nested class" - a class defined within another class
	static public class ServerThread extends Thread {
		private Socket socket = null;
		private Integer thread_id = -1;

		public ServerThread(Socket socket, int thread_id) {
			super("ServerThread");
			this.socket = socket;
			this.thread_id = thread_id; //Note: Each thread has its own unique thread_id
		}

		public void run() {
			try (
					PrintWriter socket_out = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				) {
				if (Quiz.start) {
					socket_out.println("Server busy");
					return;
				}
				Player.ACTIVE++;
				if (Player.ACTIVE == PLAYERS)
					Quiz.start = true;
				String inputLine, outputLine;
				inputLine = socket_in.readLine(); //Get their name from the network connection
				outputLine = "Welcome " + inputLine;
				socket_out.println(outputLine); //Write a welcome message to the network connection

				Player p = new Player(thread_id,inputLine);
				thread_id = Player.players.size();
				Player.allplayers.put(thread_id, p);
				Player.players.put(thread_id, p);

				boolean asked = false;
				boolean printscore = false;
				while ((inputLine = socket_in.readLine()) != null) {
					System.out.println("Thread " + p.thread_id + " read: " + inputLine);
					if (inputLine.equals("QUIT"))
						break;
					if (Player.ACTIVE != PLAYERS) {
						if (Quiz.start == false) {
							socket_out.println("Waiting for more players");
							continue;
						}
						outputLine = "";
						for (int i = 0; i < Player.players.size(); i++) {
							Player out = Player.players.get(i);
							outputLine += out.name + ": " + out.score + " ";
						}
						socket_out.println(outputLine);
						break;
					}
					int question = p.questionnum;
					boolean next = true;
					for (int i = 0; i < Player.players.size(); i++) { 
						if (Player.players.get(i).questionnum < question) {
							next = false;
							break;
						}
					}
					if (next == false) {
						socket_out.println("Waiting for players to finish");
						continue;
					}
					else {
						if (printscore == true) {
							outputLine = "";
							for (int i = 0; i < Player.players.size(); i++) {
								Player out = Player.players.get(i);
								outputLine += out.name + ": " + out.score + " ";
							}
							socket_out.println(outputLine);
							printscore = false;
							continue;
						}
						if (asked == false) {
							if (Quiz.questions.size() - 1 > question) {
								socket_out.println(Quiz.questions.get(question));
								asked = true;
							}
							else
								break;
							continue;
						}
						else {
							if (inputLine.toLowerCase().contains(Quiz.answers.get(question))) {
								p.score += 100 + question * 50;
								socket_out.println("Correct! Your score is now " + p.score);
							}
							else { //Wrong answer!
								p.score -= 100;
								socket_out.println("Wrong!!!! Score: " + p.score);
							}
							p.questionnum++;
							asked = false;
							printscore = true;
	
							System.out.println("====== Scoreboard ======");
							for (int i = 0; i < Player.players.size(); i++) {
								Player out = Player.players.get(i);
								System.out.println(out.name + ": " + out.score);
							}
						}
					}
				}
				int winner = thread_id;
				int highscore = p.score;
				for (int i = 0; i < Player.players.size(); i++)
					if (Player.players.get(i).score > highscore)
						winner = i;
				if (thread_id == winner)
					socket_out.println("You win!!!");
				else 
					socket_out.println("You lose :(");
				if (Player.ACTIVE == 1) {
					Player.players.clear();
					Quiz.select();
					Quiz.start = false;
				}
				Player.ACTIVE--;
				System.out.println("Thread closing");
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 1 || args.length > 2) {
			System.err.println("Usage: java Server <port number> [player number]");
			System.exit(1);
		}

		int portNumber = Integer.parseInt(args[0]);
		if (args.length == 2)
			PLAYERS = Integer.parseInt(args[1]);
		Quiz.select();

		boolean newquiz = false;
		boolean listening = true;
		try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
			while (listening) {
				ServerThread new_thread = new ServerThread(serverSocket.accept(),thread_count); 
				new_thread.start();
				System.out.println("Client " + Integer.toString(thread_count) + " connected");
				thread_count++;
			}
		} catch (IOException e) {
			System.err.println("Could not listen on port " + portNumber);
			System.exit(-1);
		}

	}
}
