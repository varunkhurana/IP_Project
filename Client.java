import java.io.FileInputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;




public class Client {

	public static String serverHostName;
	public static int portNum;
	public static int windowSize;
	public static int mss;
	public static DatagramSocket d_socket;
	
	public static Timer timer;
	public static int timeout = 3000;
	static byte[] data;
	private Counter counter;
	static byte[] header_and_data;
	static HashMap<Integer, HeaderAndData> h;
	static int max_sequence_number = 0;
	long lastPacketSent = 0;
	static Set<TimerTask> timerTaskSet = new HashSet<TimerTask>();
	//static Set<TimerTask> timerTaskSet = Collections.synchronizedSet(new HashSet<TimerTask>());
	public static long start_time;
	
	

	public Client(Counter counter) {
		this.counter = counter;
	}

	public static void main(String[] args) throws Exception{

		serverHostName = args[0];
		portNum = Integer.parseInt(args[1]);


		String clientFile = args[2];
		windowSize = Integer.parseInt(args[3]);
		mss = Integer.parseInt(args[4]);
		data = new byte[mss];
		header_and_data = new byte[mss+8];
		h = new HashMap<Integer, HeaderAndData>();
		d_socket = new DatagramSocket();

		d_socket.connect(InetAddress.getByName(serverHostName), portNum);

		int data_length;
		int indication_data = 21845;
		int sequence_number = 1;
		FileInputStream fstream = new FileInputStream(clientFile);

		Counter counter = new Counter(0,false);
		LastAck lastAck = new LastAck(0);
		LastSent lastSent = new LastSent(0);
		
		

		//Insert Data into HashMap

		while(true)
		{
			data_length = fstream.read(data);
			if(data_length != -1)
			{
				//hnd = new HeaderAndData(sequence_number,indication_data,data);
				h.put(sequence_number,new HeaderAndData(sequence_number,indication_data,data));
				sequence_number++;		
				
				data = new byte[mss];
			}
			else
			{
				max_sequence_number = sequence_number - 1;
		//		System.out.println("Max Sequence number is" +max_sequence_number);
				//System.exit(0);

				break;
			}
		}

		start_time = new Date().getTime();
		
		Thread listener = new Thread(new ClientListener(counter, lastAck, d_socket, start_time, max_sequence_number));
		Thread sender1 = new Thread(new Ftp_Sender(counter, h, portNum,serverHostName,d_socket, mss, windowSize, lastSent,timeout, max_sequence_number, lastAck, timerTaskSet));
		//public Ftp_Sender(Counter counter, HashMap<Integer, HeaderAndData> h, int portNum, String serverHostName, DatagramSocket d_socket, int mss, int N, LastSent lastSent, int timeout )
		//Thread timeout_handler = new Thread(new Handle_Timeout(d_socket,hnd,timer,lastSent,lastAck,windowSize, max_sequence_number,h, serverHostName, portNum, timeout,mss,counter));
		
		listener.start();
		sender1.start();
		//timeout_handler.start();
		


	}


}

class Counter {
	int value;
	boolean isTimeoutRunning;
	Counter (int value,boolean isTimeoutRunning) {
		this.value = value;
		this.isTimeoutRunning = isTimeoutRunning;
		
	}
}

class LastAck {
	int value;
	LastAck (int value) {
		this.value = value;
	}
}

class LastSent {
	int value;
	LastSent(int value)
	{
		this.value = value;
	}

}