import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class Ftp_Sender implements Runnable {

	private Counter counter;
	private HashMap<Integer, HeaderAndData> h;
	private int portNum;
	private String serverHostName;
	private DatagramSocket d_socket;
	private int mss;
	private int N;
	private LastSent lastSent;
	private LastAck lastAck;
	private int timeout;
	private int max_sequence_number;
	static byte[] header_and_data;
	static byte[] data;
	private Set<TimerTask> timerTaskSet;

	public Ftp_Sender(Counter counter, HashMap<Integer, HeaderAndData> h, int portNum, String serverHostName, DatagramSocket d_socket, int mss, int N, LastSent lastSent, int timeout, int max_sequence_number, LastAck lastAck, Set<TimerTask> timerTaskSet )
	{
		this.counter = counter;
		this.h = h;
		this.portNum = portNum;
		this.serverHostName = serverHostName;
		this.d_socket = d_socket;
		this.mss = mss;
		this.N = N;
		this.lastSent = lastSent;
		this.timeout = timeout;
		this.max_sequence_number = max_sequence_number;
		this.lastAck = lastAck;
		this.timerTaskSet = timerTaskSet;
	}
	public void run()
	{	
		HeaderAndData hnd = null;
		while(true)
		{
			synchronized (counter) 
			{
				while (counter.value == N || counter.isTimeoutRunning) 
				{
					try {
						counter.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			synchronized (lastSent) {
				int lastSentValue = lastSent.value;
				//System.out.println("last sent " + (lastSentValue));
				hnd =  h.get(lastSentValue + 1);

			//	System.out.println(hnd);
			}
			//byte[] tempdata = h_n_d.getData();
			//System.out.println(Arrays.toString(tempdata));
			if(lastSent.value < max_sequence_number)
			{
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutput out = null;
				try {
					out = new ObjectOutputStream(bos);   
					out.writeObject(hnd);
					header_and_data = bos.toByteArray();
					DatagramPacket d_packet = new DatagramPacket(header_and_data,header_and_data.length,InetAddress.getByName(serverHostName), portNum);
					d_socket.send(d_packet);
					synchronized (counter) {
						counter.value++;
					}
					synchronized (lastSent) {
						lastSent.value++;
					}
				//	System.out.println("Initial window send code, packet sequence number is "+ hnd.getSequence_number());
					Timer timer = new Timer();
					TimerTask t_task = new Handle_Timeout(d_socket,hnd.getSequence_number(),timer,lastSent, lastAck,N,max_sequence_number,h,serverHostName,portNum,timeout,mss,counter, timerTaskSet);
					
					synchronized (timerTaskSet) {
						timerTaskSet.add(t_task);
						timer.schedule(t_task, timeout);
					}
					
					//d_socket,hnd,timer,lastSent,lastAck,windowSize, max_sequence_number,h, serverHostName, portNum, timeout,mss,counter
					
					data = new byte[mss];
					header_and_data = new byte[mss+8];
					System.gc();
				}

				catch(Exception ex)
				{
					ex.printStackTrace();
				}

			}

		}

	}


}