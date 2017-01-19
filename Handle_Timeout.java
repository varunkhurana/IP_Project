import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;




public class Handle_Timeout extends TimerTask{

	private DatagramSocket socket;
	private Timer timer_class;
	private LastSent lastSent;
	private LastAck lastAck;

	private int windowSize;
	private int max_sequence_number;
	private HashMap<Integer, HeaderAndData> h;
	private String serverHostName;
	private int portNum;
	private int timeout;
	private int mss;
	private Counter counter;
	private int seqNum;
	private Set<TimerTask> timerTaskSet;

	public Handle_Timeout(DatagramSocket pass_socket, int seqNum, Timer timer, LastSent lastSent, LastAck lastAck, int windowSize, int max_sequence_number, HashMap<Integer, HeaderAndData> h, String serverHostName, int portNum, int timeout, int mss, Counter counter, Set<TimerTask> timerTaskSet)
	{
		this.socket = pass_socket;
		this.seqNum = seqNum;
		this.timer_class = timer;
		this.lastSent = lastSent;
		this.lastAck = lastAck;
		this.windowSize = windowSize;
		this.max_sequence_number = max_sequence_number;
		this.h = h;
		this.serverHostName = serverHostName;
		this.portNum = portNum;
		this.timeout = timeout;
		this.mss = mss;
		this.counter = counter;
		this.timerTaskSet = timerTaskSet;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub




		//	System.out.println("Next line pe phat jata h code");


		if(seqNum == (lastAck.value+1) )
		{
			synchronized (counter) {
				counter.isTimeoutRunning = true;
			}

			System.out.println("Timeout, sequence number = "+seqNum);

			//Cancel existing timertasks
			synchronized(timerTaskSet)
			{

				Iterator<TimerTask> itr = timerTaskSet.iterator();
				while (itr.hasNext()) {
					itr.next().cancel();						
				}
				timerTaskSet.clear();
			}
			//System.out.println("should not be invoked");
			timer_class.purge();
			int iterations_added;
			if((seqNum + windowSize)>max_sequence_number)
			{
				iterations_added = (max_sequence_number - seqNum) + 1; 
			}
			else
			{
				iterations_added = windowSize;
			}

			for(int i=seqNum; i<(seqNum + iterations_added);i++)
			{
				//HeaderAndData h_n_d=null;
				//	System.out.println("The value of last_acknowledged_seq_num is "+lastAck.value );

				HeaderAndData h_n_d = h.get(i);
				//byte[] tempdata = h_n_d.getData();
				//System.out.println(Arrays.toString(tempdata));
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutput out = null;
				try {
					out = new ObjectOutputStream(bos);   
					out.writeObject(h_n_d);
					//header_and_data = new byte[mss+8];
					byte[] header_and_data = bos.toByteArray();
					DatagramPacket d_packet = new DatagramPacket(header_and_data,header_and_data.length,InetAddress.getByName(serverHostName), portNum);
					Thread.sleep(50);
					socket.send(d_packet);
					//	System.out.println("Inside run method, packet sequence sent is "+h_n_d.getSequence_number());
					Timer timer = new Timer();
					TimerTask t_task = new Handle_Timeout(socket, h_n_d.getSequence_number(),timer,lastSent,lastAck,windowSize, max_sequence_number, h, serverHostName, portNum, timeout, mss, counter, timerTaskSet);
					synchronized (timerTaskSet) {
						timerTaskSet.add(t_task);
						timer.schedule(t_task, timeout);
					}
					
					
					//data = new byte[mss];
					header_and_data = new byte[mss+8];
					System.gc();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}

			}
			synchronized(lastSent)
			{
				lastSent.value = seqNum + iterations_added - 1;
			}
			synchronized (counter) {
				counter.value = windowSize;
				counter.isTimeoutRunning = false;
				counter.notifyAll();
			}

		}
		else
		{
			this.cancel();
		}



	}


}
