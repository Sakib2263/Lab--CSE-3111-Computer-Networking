import java.net.*; 
import java.io.*; 
import java.util.*;
import java.lang.*;




public class Store implements Runnable{ 
	private Socket socket		 = null; 
	private DataInputStream dataIn = null; 
	private DataOutputStream dataOut	 = null; 
	private static int Store_port;
	private static int Bank_port;
	private static String bank_IP ="";

	static String firstname = "";
	static String familyname = "";
	static String postCode = "";
	static String card = "";
	static String item = "";
	static String quntity = "";
	static Map<String, Integer> map;
	static boolean isApproved = false;
	static boolean credentials = false;

	public Store(Socket skt){
		socket = skt;
	}

	public void runStore(String address, int port) 
	{ 
		try { 
			map = new HashMap<String, Integer>();
			map.put("001", 450);
			map.put("002", 250);
			map.put("003", 350);
			map.put("004", 300);
			// mapping Album ID with its price
			socket = new Socket(address, port); 
			System.out.println("Connected"); 

			dataIn = new DataInputStream(new BufferedInputStream(socket.getInputStream())); 
			dataOut = new DataOutputStream (socket.getOutputStream());
		} 
		catch(UnknownHostException e) { 
			//System.out.println(e); 
		} catch(IOException e) { 
			//System.out.println(e); 
		} 

		sendLines(dataOut);
		String bankStatement = "nothing";
		try {
			bankStatement = dataIn.readUTF();
		}
		catch(IOException e){
			//System.out.println(e);
		}

		System.out.println("Bank Server says : " + bankStatement);
		if(bankStatement.equals("approved")){
			credentials = true;
			String takaa = calculate();
			sendAmount(dataOut, takaa, card);
			System.out.println("Sent amount: "+takaa);
			try{
				bankStatement = dataIn.readUTF();
				System.out.println("Bank Statement is: " + bankStatement);
				if(bankStatement.equals("approved")){
					System.out.println("Transaction Approved");
					isApproved = true;
				}
				else{
					System.out.println("Transaction aborted: Insufficient Funds");
					isApproved = false;
				}
			}
			catch(IOException e){
				//System.out.println(e);
			}
		}
		else {
			credentials = false;
		}
		try { 
			dataIn.close(); 
			dataOut.close(); 
			socket.close(); 
		} 
		catch(IOException i) 
		{ 
			System.out.println(i); 
		} 
	} 

	public void run(){
		System.out.println("Running");
		BufferedReader br = null;
		PrintWriter pw = null;
		BufferedOutputStream bos = null;
		try{
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pw = new PrintWriter (socket.getOutputStream(), true);
			bos = new BufferedOutputStream (socket.getOutputStream());

			String htmlData = br.readLine();
			System.out.println("htmlData: "+htmlData);
			
			StringTokenizer strtok = new StringTokenizer (htmlData);
			String method_up = strtok.nextToken();
			String method = method_up.toLowerCase();
			String reqFile_up = strtok.nextToken();
			String reqFile = reqFile_up.toLowerCase();
			
			if(htmlData.contains("POST")) {
                String str = null;
                while ((str = br.readLine()).length() != 0) {
                	// System.out.println(br.readLine());
                }

                StringBuilder payload = new StringBuilder();
                while (br.ready()) {
                    payload.append((char) br.read());
                }
                String userinfo = payload.toString();
                String infos[] = userinfo.split("=");
            	// getting data from the FORM using POST method
                for (int i = 1; i < infos.length; i++) {
                    String info[] = infos[i].split("&");
                    if (i == 1) firstname = info[0];
                    if (i == 2) familyname = info[0];
                    if (i == 3) postCode = info[0];
                    if (i == 4) card = info[0];
                    if (i == 5) item = info[0];
                    if (i == 6) quntity = info[0];
                }
            	// got all the data from the FORM using POST method
                System.out.println("Info : "+firstname+", "+familyname+", "+postCode+", "+card+", "+item+", "+quntity);
                runStore(bank_IP, Bank_port);
                if(isApproved){
                	// If Transition succedded, Then go to "success.html"
                	loadPage(pw, "success.html");
                	bos.flush();
                }
                else{
                	loadPage(pw, "error.html");
                }
            }

            if(htmlData.contains("GET")){
            	System.out.println("Method : GET");
            	loadPage(pw, "buySongs.html");
            	bos.flush();
            }
 		}
		catch(IOException e){
			//System.out.println(e);
		}
		finally{
			try{
				br.close();
				pw.close();
				bos.close();
			}
			catch(IOException e){
				//System.out.println(e);
			}
		}
	}


		public void loadPage(PrintWriter printWriter, String fileAddress){
		BufferedReader bufferedReader;
		try{
			File file = new File(fileAddress);
			int fileLength = (int)file.length();
			System.out.println("File length: "+fileLength);
			bufferedReader = new BufferedReader (new FileReader(fileAddress));
			String line = bufferedReader.readLine();		
			printWriter.println("HTTP/1.1 200 OK");
			printWriter.println("Content-Type: text/html");
			printWriter.println("Content-length: " + fileLength);
			printWriter.println();
			printWriter.flush();
			while(line != null){
				// System.out.println(line);
				printWriter.println(line);
				line = bufferedReader.readLine();
			}
		}
		catch(IOException e){
			//System.out.println("Error : "+e);
		}
	}

	// calculating Total Price
	public static String calculate(){
		int Quantiy = Integer.parseInt(quntity);
		int unit = map.get(item);
		int totalPrice = Quantiy * unit;
		String amount = Integer.toString(totalPrice);
		System.out.println("Total: "+amount);
		return amount;
	}

	public static void sendLines(DataOutputStream dataOut){

		try{
			dataOut.writeUTF(firstname);
			dataOut.writeUTF(familyname);
			dataOut.writeUTF(postCode);
			dataOut.writeUTF(card);
			System.out.println("Bill for: "+firstname+" "+familyname+" Card num: " + card);
		}
		catch(IOException e){
			System.out.println(e);
		}
	}

	public static void sendAmount(DataOutputStream dataOut, String amount, String card){
		try{
			dataOut.writeUTF(card);
			dataOut.writeUTF(amount);
			System.out.println("Bill : "+amount+"/= (taka) for credit card no: "+card);
		}
		catch(IOException e){
			System.out.println(e);
		}
	}

	public static void main(String args[]){ 
		String store_port = args[0];
		String bank_host_ip = args[1];
		String bank_port = args[2];
		Bank_port = Integer.parseInt(bank_port);
		Store_port = Integer.parseInt(store_port);
		bank_IP = bank_host_ip;
		// store.runStore("127.0.0.1", bankPort); 
		// Connects to the bank

		try{
			ServerSocket serverSocket = new ServerSocket(Store_port);
			while(true){	
				Store store = new Store(serverSocket.accept()); // Connects to the browser
				Thread thread = new Thread(store);
				thread.start();
			}
		}
		catch(IOException e){
			//System.out.println(e);
		}
	} 
} 
