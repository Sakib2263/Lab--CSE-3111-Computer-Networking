import java.net.*; 
import java.io.*; 
import java.util.*;

public class Bank { 
	private Socket socket = null; 
	private ServerSocket server = null; 
	private DataInputStream dataIn	 = null;
	private DataOutputStream dataOut = null; 

	public void runBank(int port) { 
		try { 
			server = new ServerSocket(port); 
			// System.out.println("Server started at Port:" + port); 
			socket = server.accept(); 
			// System.out.println("Client accepted"); 
			dataIn = new DataInputStream(new BufferedInputStream(socket.getInputStream())); 
			dataOut = new DataOutputStream (socket.getOutputStream());

			String [] Lines = new String[100];
			Lines = receiveLines(dataIn);
			if(checkFile(Lines, "database.txt")){
				// System.out.println("Initials approved");
				try{
					dataOut.writeUTF("approved");
				} catch(IOException e){
					System.out.println(e);
				}
				String credit, amount;
				try{
					credit = dataIn.readUTF();
					amount = dataIn.readUTF();
					try{
						if(checkAccount(credit, amount, "database.txt")){
							updateFile(credit, "database.txt", amount);
							dataOut.writeUTF("approved");
						} else{
							dataOut.writeUTF("rejected");
						}
					}
					catch(IOException e){
						System.out.println(e);
					}
				}
				catch(IOException e){
					System.out.println(e);
				}
			}

			else{
				System.out.println("Initials rejected. Transaction aborted");
				try{
					dataOut.writeUTF("rejected");
				}
				catch(IOException e){
					System.out.println(e);
				}
			}

			System.out.println("Closing connection"); 

			socket.close(); 
			dataIn.close();
			System.exit(0);
		} 
		catch(IOException i) 
		{ 
			System.out.println(i); 
		} 
	} 

	public static String[] receiveLines(DataInputStream dataIn){
		String line = ""; 
		String[] Lines = new String[100];
			for(int i=0; i <= 3; i++) { 
				try { 
					line = dataIn.readUTF(); 
					Lines[i] = line;
					// System.out.println(line); 
					// Each Line in database.txt
				} 
				catch(IOException e) { 
					System.out.println(e); 
				} 
		}

	    return Lines;

	}

	public static boolean checkFile(String[] Lines, String fileName){
		BufferedReader bufferedReader;
		try{
			bufferedReader = new BufferedReader (new FileReader(fileName));
			String line = bufferedReader.readLine();
			while(line != null){
				System.out.println(line);
				StringTokenizer strtok = new StringTokenizer(line, "#");
				// "database.txt" File data is Tokenizing..
				
				String firstname = strtok.nextToken();
				String familyname = strtok.nextToken();
				String postCode = strtok.nextToken();
				String card = strtok.nextToken();
				String balance = strtok.nextToken();
				String credit = strtok.nextToken();
				line = bufferedReader.readLine();
				
				System.out.println("database: "+firstname+", "+familyname+", "+postCode+", "+card+", "+balance+", "+credit);
				System.out.println("input: "+Lines[0]+", "+Lines[1]+", "+Lines[2]+", "+Lines[3]);
				if( firstname.equals(Lines[0]) && familyname.equals(Lines[1]) && postCode.equals(Lines[2]) && card.equals(Lines[3]) ){
					// checking the buyer is registered or not..
					return true;
				}

			}
			bufferedReader.close();
		}
		catch(IOException e){
			System.out.println("Error : "+e);
		}

		return false;
	}

	public static boolean checkAccount(String credit, String Amount, String fileName){
		BufferedReader bufferedReader;
		try{
			bufferedReader = new BufferedReader(new FileReader(fileName));
			String line = bufferedReader.readLine();
			while(line != null){
				System.out.println("Line: "+line);
				// Here we will Tokenize each Line according to #
				StringTokenizer strtok = new StringTokenizer(line, "#");
				String db_amount = "";
				String db_balance = "";
				String db_credit = "";
				for(int i=0; i <= 3; i++){
					db_credit = strtok.nextToken();
				}
				if(db_credit.equals(credit)){
					System.out.println("Account found!");
					db_balance = strtok.nextToken();
					System.out.println("DB Balance: "+db_balance);
					db_amount = strtok.nextToken();
					System.out.println("DB Credit Amount: "+db_amount);
					int db_int_amount = Integer.parseInt(db_amount);
					int input_int_amount = Integer.parseInt(Amount);
					if (db_int_amount >= input_int_amount){
						//System.out.println("Credit available!");
						return true;
					}
					System.out.println("Credit Amount Short :'-(");
				}
				line = bufferedReader.readLine();
			}
		}
		catch(IOException e){
			System.out.println(e);
		}
		return false;
	}

	public static void updateFile(String credit, String fileName, String Amount){
		System.out.println("Amount to be deducted: "+Amount);
		BufferedReader bufferedReader;
		BufferedWriter bufferedWriter, tempWriter;
		try{
			bufferedReader = new BufferedReader (new FileReader(fileName));
			String [] temp = new String [100000];
			String line = bufferedReader.readLine();
			String intactLine = line;
			String editLine = "";
			int j=0;
			int users = 3;			//no of user on db
			int cnt = 0;
			while(cnt != users && line != null){
					cnt++;
					StringTokenizer strtok = new StringTokenizer(line, "#");
					String db_amount = "";
					String db_credit = "";
					for(int i=0; i <= 3; i++){
						db_credit = strtok.nextToken();
					}
					//checking each line in the database.. finding match with creditCardNo with the buyers CreditCardNo
					if(!db_credit.equals(credit)){
						// Not Matched.. Try Next
						temp[j] = intactLine;
						j++;
					}
					else{
						// Matched.. Here the bank balance will be deduced.
						editLine = intactLine;
						System.out.println("Line to be edited: "+editLine);
					}
					line = bufferedReader.readLine();
					intactLine = line;
				}
				System.out.println("Data Update Completed after Transaction!");
				System.out.println(editLine);
				StringTokenizer strtok = new StringTokenizer(editLine, "#");
				String balance;
				String fname, famname, postcode, card, cred;
				
				fname = strtok.nextToken();
				famname = strtok.nextToken();
				postcode = strtok.nextToken();
				card = strtok.nextToken();
				balance = strtok.nextToken();
				cred = strtok.nextToken();

				int myBalance = Integer.parseInt(balance); 
				System.out.println("Balance: " + myBalance);
				
				int myCredit = Integer.parseInt(cred);
				System.out.println("Credit: " + myCredit);
				
				int myAmount = Integer.parseInt(Amount); 
				System.out.println("Amount: " + myAmount);
				
				int new_balance = myBalance + myAmount;
				int new_credit = myCredit - myAmount;

				String Balance = Integer.toString(new_balance);
				String Credit = Integer.toString(new_credit);

				String db_line = fname + "#" + famname + "#" + postcode + "#" + card + "#" + Balance + "#" + Credit;
				System.out.println(db_line);
				
				bufferedWriter = new BufferedWriter (new FileWriter(fileName));
				
				for(int k=0; k<j; k++){
					bufferedWriter.write(temp[k]);
					bufferedWriter.write("\n");
					// other users info..
				}
				bufferedWriter.close();

				bufferedWriter = new BufferedWriter( new FileWriter(fileName, true));
				//buyer's info...
				bufferedWriter.write(db_line);
				bufferedWriter.close();		
		}
		catch(IOException e){
			System.out.println(e);
		}
	}

	public static int getPort(){
		int port = (int) (Math.random()*((65000-1025)+1))+1025;
		// randomly generated Port..
		return port;
	}

	public static void main(String args[]) 
	{ 
		Bank bank = new Bank();
		String bank_port = args[0];
		int bankPort = Integer.parseInt(bank_port);
		boolean first = true;
		while(true){
			if(first){
				first = false;
				bank.runBank(bankPort);
			} else{
					bankPort = getPort();
					bank.runBank(bankPort);
				}
			}
		}
	} 