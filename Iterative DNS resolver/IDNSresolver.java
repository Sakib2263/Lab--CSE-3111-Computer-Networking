import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.Scanner;

public class IDNSresolver {

    private static String setDNSserver() {
        //------------- Hostname --------------- IP Addresses ---------------------- manager----------- 

        String a = "198.41.0.4"; //a.root-servers.net     198.41.0.4, 2001:503:ba3e::2:30 	VeriSign, Inc.
        String b = "199.9.14.201"; //b.root-servers.net 	199.9.14.201, 2001:500:200::b           University of Southern California (ISI)
        String c = "192.33.4.12"; //c.root-servers.net 	192.33.4.12, 2001:500:2::c 	Cogent Communications
        String d = "199.7.91.13"; //d.root-servers.net 	199.7.91.13, 2001:500:2d::d 	University of Maryland
        String e = "192.203.230.10"; //e.root-servers.net 	192.203.230.10, 2001:500:a8::e 	NASA (Ames Research Center)
        String f = "192.5.5.241"; //f.root-servers.net 	192.5.5.241, 2001:500:2f::f 	Internet Systems Consortium, Inc.
        String g = "192.112.36.4"; //g.root-servers.net 	192.112.36.4, 2001:500:12::d0d 	US Department of Defense (NIC)
        String h = "198.97.190.53"; //h.root-servers.net 	198.97.190.53, 2001:500:1::53 	US Army (Research Lab)
        String i = "192.36.148.17"; //i.root-servers.net 	192.36.148.17, 2001:7fe::53 	Netnod
        String j = "192.58.128.30"; //j.root-servers.net 	192.58.128.30, 2001:503:c27::2:30 	VeriSign, Inc.
        String k = "193.0.14.129"; //k.root-servers.net 	193.0.14.129, 2001:7fd::1 	RIPE NCC
        String l = "199.7.83.42"; //l.root-servers.net 	199.7.83.42, 2001:500:9f::42 	ICANN
        String m = "202.12.27.33"; //m.root-servers.net 	202.12.27.33, 2001:dc3::35 	WIDE Project

        //Randomly chooses a root name server for DNS query
        String x = domainName;
        String rootServer = "";
        boolean isORG = x.contains("org");
        boolean isEdu = x.contains("edu");
		//for .org and .edu extension rootserver is hardcoded for now
        if (isORG == true) {
            rootServer = i;
        } else if (isEdu == true) {
            rootServer = b;
        } else {
            Random rand = new Random();
            int n = rand.nextInt(13) + 1;

            switch (n) {
                case 1:
                    System.out.println("Selected RootServer = a.root-servers.net");
                    rootServer = a;
                    break;
                case 2:
                    System.out.println("Selected RootServer = b.root-servers.net");
                    rootServer = b;
                    break;
                case 3:
                    System.out.println("Selected RootServer = c.root-servers.net");
                    rootServer = c;
                    break;
                case 4:
                    System.out.println("Selected RootServer = d.root-servers.net");
                    rootServer = d;
                    break;
                case 5:
                    System.out.println("Selected RootServer = e.root-servers.net");
                    rootServer = e;
                    break;
                case 6:
                    System.out.println("Selected RootServer = f.root-servers.net");
                    rootServer = f;
                    break;
                case 7:
                    System.out.println("Selected RootServer = g.root-servers.net");
                    rootServer = g;
                    break;
                case 8:
                    System.out.println("Selected RootServer = h.root-servers.net");
                    rootServer = h;
                    break;
                case 9:
                    System.out.println("Selected RootServer = i.root-servers.net");
                    rootServer = i;
                    break;
                case 10:
                    System.out.println("Selected RootServer = j.root-servers.net");
                    rootServer = j;
                    break;
                case 11:
                    System.out.println("Selected RootServer = k.root-servers.net");
                    rootServer = k;
                    break;
                case 12:
                    System.out.println("Selected RootServer = l.root-servers.net");
                    rootServer = l;
                    break;
                case 13:
                    System.out.println("Selected RootServer = m.root-servers.net");
                    rootServer = m;
                    break;

            }
        }

        return rootServer;
    }

    private static String getDomain() {
        //For some reason Domain name with https:// can't be parsed properly
        System.out.println("Enter the Hostame (web address) : ");
        Scanner scanner = new Scanner(System.in);
        String domain = scanner.nextLine();
        return domain;

    }
    private static final String domainName = getDomain();
    private static final String DNSServerAddress = setDNSserver();
    private static final int DNSServerPort = 53;

    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket();			// new datagram socket 
        String currentNSAddress = DNSServerAddress;				// the IP address to which the query will be sent
        String currentDomain = domainName;						// the DNS address the requests will query
        String output, IP_address;								
        output = null;											// response from the rootserver is saved in output 
        IP_address = null;										//IP adress from the response in  IP_adress
        boolean isAuth = false;									//Checking in authoritative server or not

        while (true) {
            String next = null;
            System.out.println("Given Domain name: " + currentDomain);
            System.out.println("Root NS address: " + currentNSAddress + " #" + DNSServerPort);
            sendDNSRequest(socket, currentNSAddress, currentDomain);		// send the DNS request/query
            byte[] buf = getResponseFromServer(socket);						// byte array containing raw bytes of DNS response
            ByteArrayInputStream bin = new ByteArrayInputStream(buf);		
            DataInputStream in = new DataInputStream(bin);					// store the byte array of response as a DataInputStream
            short[] responses = showDNSResponse(in);
            short answers = responses[0], authoritative = responses[1], additional = responses[2];

            System.out.println("--------------------------------------- ANSWER RR(s) ---------------------------------------");
            if (answers == 0) {
                System.out.println("Server DNS address could not be found!\n");
                break;
            } else if (answers > 0) {
                output = printDNSResponse(in, buf);

                for (int i = 1; i < answers; i++) {
                    String current = printDNSResponse(in, buf);
                    if (current != null) {
                        if (next == null) {
                            next = current;
                        }
                    }
                    IP_address = next;
                }
                break;
            }
            if (authoritative > 0) {
                System.out.println(authoritative);
                System.out.println("--------------------------------AUTHORITATIVE RR(s)------------------------");
                for (int i = 0; i < authoritative; i++) {
                    String current = printDNSResponse(in, buf);
                    if (current != null) {
                        if (next == null) {
                            isAuth = true;
                            next = current;
                        }
                    }
                }
            }
            if (additional > 0) {
                System.out.println(additional);
                System.out.println("------------------------ADDITIONAL RR(s)--------------------------------");
                for (int i = 0; i < additional; i++) {
                    String current = printDNSResponse(in, buf);
                    if (current != null) {
                        if (isAuth || next == null) {
                            isAuth = false;
                            next = current;
                        } else {
                        }
                    }
                }
            }
            if (next != null) {
                if (isAuth) {
                    currentDomain = next;
                } else {
                    currentNSAddress = next;
                }
            } else {
                currentNSAddress = next;
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("--------------------------------------------------------------------------------------------");
        if (output != null) {
            System.out.println("For domain :" + domainName +"	 ---> IP Address: " + output + "\n");
        } else {
            if (IP_address == null) {
                // no answer RR(r) provides any ipv4 adress
                System.out.println("\n" + currentDomain + " Does not exist!");
            } else {
                //first Answer RR(s) doesn;t contain any IP adress but others does
                System.out.println("For domain :" + domainName + " ---> IP Address: " + IP_address + "\n");
            }
        }
    }
	
	/** This function will send a DNS request to the required server. The parameters are a socket, the address where the request will be send
	and the address name of the website whose IP we want to know */
    private static DatagramSocket sendDNSRequest(DatagramSocket socket, String DNSAddress, String domainName) throws IOException {
        InetAddress ipAddress = InetAddress.getByName(DNSAddress); // Determines the IP address of a host, given the host's name.
        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("Sending Query to IP: " + DNSAddress);
        //System.out.println("\n");
        ByteArrayOutputStream outs = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(outs);
        //initialize

        /* ---- writing a DNS Query ---Building the DNS query Frame ---- */
        os.writeShort(0x1234);			// Identifier: 
        os.writeShort(0x0000);			// Write Query Flags
        os.writeShort(0x0001);			// Question Count
        os.writeShort(0x0000);			// Answer Record Count
        os.writeShort(0x0000);			// Authority Record Count
        os.writeShort(0x0000);			// Additional Record Count

        String[] domainParts = domainName.split("\\.");
        //System.out.println(domainName + " has " + domainParts.length + " parts");
        for (String domainPart : domainParts) {
            byte[] dB = domainPart.getBytes("UTF-8");
            os.writeByte(dB.length);
            os.write(dB);
        }

        os.writeByte(0x00);				// 0x byte to finish the header
        os.writeShort(0x0001);			// Write Type 0x01 = A
        os.writeShort(0x0001);			// Write Class 0x01 = IN

        byte[] dnsFrame = outs.toByteArray();

        System.out.println("Sent " + dnsFrame.length + " bytes (Query Size)");
		//Printing the query bytcode
        for (int i = 0; i < dnsFrame.length; i++) {
            // Query in Bytecode hexadecimal format
            //System.out.print(String.format("%02x", dnsFrame[i]) + " ");
        }
        System.out.println("\n");

        /* *** Send DNS Request Frame (Query) *** */
        DatagramPacket dnsReqPacket = new DatagramPacket(dnsFrame, dnsFrame.length,
                ipAddress, DNSServerPort);
        socket.send(dnsReqPacket);

        return socket;
    }
	
	
	/** This function receives the response from the DNS server and returns a byte array which will
	    contain the DNS response in raw byte format */
    private static byte[] getResponseFromServer(DatagramSocket socket) throws IOException {
        // Response from DNS server
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        System.out.println("Received Data length: " + packet.getLength() + " bytes");
		
        for (int i = 0; i < packet.getLength(); i++) {
            // Response in Bytecode hexadecimal format
            //System.out.print(String.format("%02x", buf[i]) + " ");
        }

        System.out.println("--------------------------------------------------------------------------------------------");

        return buf;
    }
	
	/*	This method extracts the DNS header. The input is the DNS response as a DataInputStream .
	    the output is an array of 3 short values that contains the number of answers, authoritative answers and additional answers
	    respectively */
    private static short[] showDNSResponse(DataInputStream in) throws IOException {

        System.out.println("* Transaction ID: " + String.format("(%04x)", in.readShort()));
        System.out.println("* Flags: 0x" + String.format("%04x", in.readShort()));
        System.out.println("* Questions: " + String.format("%d", in.readShort()));
        short answers = in.readShort();
        short authoritative = in.readShort();
        short additional = in.readShort();
        short[] responses = new short[3];
        responses[0] = answers;
        responses[1] = authoritative;
        responses[2] = additional;

        System.out.println("* Answers RR(s): " + String.format("%d", answers));
        System.out.println("* Authority RR(s): " + String.format("%d", authoritative));
        System.out.println("* Additional RR(s): " + String.format("%d", additional));

        int domainPartsLength;
        StringBuilder domainRes = new StringBuilder();
        while ((domainPartsLength = in.readByte()) > 0) {
            byte[] dB = new byte[domainPartsLength];
            for (int i = 0; i < domainPartsLength; i++) {
                dB[i] = in.readByte();
            }
            String domain = new String(dB, "UTF-8");
            if (domainRes.length() > 0) {
                domainRes.append(".");
            }
            domainRes.append(domain);
        }
        String responseDomain = domainRes.toString();
        System.out.println("Query Domain: " + responseDomain);
        in.readInt();
        return responses;
    }

    /*  Show Formatted Query response. Each part of answer section is formated and showed by this method */
    private static String printDNSResponse(DataInputStream in, byte[] data) throws IOException {
        System.out.println("\n" + "Name: 0x" + String.format("%04x", in.readShort()));
        short type = in.readShort();
        switch (type) {
            case 1:
                System.out.println("Type: A");
                break;
            case 2:
                System.out.println("Type: NS");
                break;
            case 5:
                System.out.println("Type: CNAME");
                break;
            case 6:
                System.out.println("Type: SOA");
                break;
            case 28:
                System.out.println("Type: AAAA");
                break;
            default:
                System.out.println("Type: 0x" + String.format("%04x", type));
                break;
        }
        short class_ = in.readShort();
        if (class_ == 1) {
            System.out.println("Class: IN");
        } else {
            System.out.println("Class: 0x" + String.format("%04x", class_));
        }
        int ttl = in.readInt();
        System.out.println("TTL: " + ttl);
        short addrLen = in.readShort();
        System.out.println("Length: " + String.format("%d", addrLen) + " byte(s)");
        byte[] ip = new byte[addrLen];
        int ipaddr = (int) in.read(ip, 0, addrLen);

        switch (type) {
            case 1:
                System.out.println("Type: A" + "\n" + "IP Address: " + convertByteArrayToIPv4(ip) + "\n");
                return convertByteArrayToIPv4(ip);
            case 2:
                System.out.println("Type: NS" + "\n" + "IP Address: " + convertByteArrayToNS(ip, data) + "\n");
                return convertByteArrayToNS(ip, data);
			case 5:
                System.out.println("Type: CNAME"+"\n"+ "IP Address: " + convertByteArrayToNS(ip, data) + "\n");
                return convertByteArrayToNS(ip, data);
            case 28:
                System.out.println("Type: AAAA"+"\n"+ "IP Address: " + convertByteArrayToIPv6(ip) + "\n");
                return convertByteArrayToNS(ip, data);

        }
        return null;
    }
	
	/* This method converts the raw data (ByteArray ) containing the IP address and converts
	into a string representation of the IP Address version 4 */
    private static String convertByteArrayToIPv4(byte[] address) {
        short addrLen = (short) address.length;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < addrLen; i++) {
            if (i != 0) {
                stringBuilder.append(".");
            }
            stringBuilder.append(String.format("%d", (address[i] & 0xFF)));
        }
        String output = stringBuilder.toString();
        //System.out.println(output + "\n");
        return output;
    }
	
	/* This method converts the raw data (ByteArray ) containing the IP address and converts
	into a string representation of the IP Address version 6 */
    private static String convertByteArrayToIPv6(byte[] address) {
        short addrLen = (short) address.length;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < addrLen; i++) {
            stringBuilder.append(String.format("%02x", address[i]));
            if (i % 2 == 1 && i != addrLen - 1) {
                stringBuilder.append(":");
            }
        }
        String output = stringBuilder.toString();
        return output;
    }

    /* function to convert Byte array to NS data string. method runs 
	whenever response contains a Compressed representation of NS adress */
    private static String ByteArrayToString(byte[] data, byte offset) throws IOException {
        //System.out.println(offset);
        StringBuilder stringBuilder = new StringBuilder();
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(bin);
        in.skipBytes(offset);					//skip offset bytes and then extract the required part
        byte domainPartLength;
        while ((domainPartLength = in.readByte()) > 0) {
            if (domainPartLength != -64) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(".");
                }
                byte[] dB = new byte[domainPartLength];
                for (int i = 0; i < domainPartLength; i++) {
                    dB[i] = in.readByte();
                }
                String domain = new String(dB, "UTF-8");
                stringBuilder.append(domain);
            } else {
                byte newOffset = in.readByte();
                stringBuilder.append(ByteArrayToString(data, newOffset));
                break;
            }
        }
        String output = stringBuilder.toString();
        //System.out.println(output);
        return output;
    }
	
	// To convert byteStream response in to NS type information (NS name)---- ByteArray to String
    private static String convertByteArrayToNS(byte[] address, byte[] data) throws IOException {
        short len = (short) address.length;
        int j = 0, cnt = 0;
        while (j < len) {
            System.out.print(String.format("%02x", address[j]) + " ");
            j++;
        }
        System.out.println();
        ByteArrayInputStream bin = new ByteArrayInputStream(address);
        DataInputStream in = new DataInputStream(bin);
        StringBuilder stringBuilder = new StringBuilder();

        while (cnt < len) {
            if (cnt != 0) {
                stringBuilder.append(".");
            }
            byte domainPartLength = in.readByte();
            System.out.println(domainPartLength);
            if (domainPartLength != -64) {
                byte[] dB = new byte[domainPartLength];
                for (int i = 0; i < domainPartLength; i++) {
                    dB[i] = in.readByte();
                }
                String domain = new String(dB, "UTF-8");
                stringBuilder.append(domain);
                cnt += domainPartLength;
            } else {
                byte offset = in.readByte();
                stringBuilder.append(ByteArrayToString(data, offset));
                break;
            }
            cnt++;
        }
        String output = stringBuilder.toString();
        //System.out.println("\nConverted : " + output);
        return output;
    }

}
