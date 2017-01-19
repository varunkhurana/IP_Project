import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Date;


public class ClientListener implements Runnable {

	private Counter counter;
	private LastAck lastAck;
	private DatagramSocket d_socket;
	private long start_time; 
	private int max_sequence_number;
	private long end_time;
	byte[] ack = new byte[5000];
	public ClientListener(Counter counter, LastAck lastAck, DatagramSocket d_socket, long start_time, int max_sequence_number) {
		this.counter = counter;
		this.lastAck = lastAck;
		this.d_socket = d_socket;
		this.start_time = start_time;
		this.max_sequence_number = max_sequence_number;
	}

	@Override
	public void run() {

		while (true) {
			//wait for a packet
			DatagramPacket ack_packet = new DatagramPacket(ack, ack.length);

			try {
				d_socket.receive(ack_packet);
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			byte[] rec_ack = ack_packet.getData();
			ByteArrayInputStream bis = new ByteArrayInputStream(rec_ack);
			ObjectInput in = null;
			try
			{

				in = new ObjectInputStream(bis);
				Object o = in.readObject();
				Acknowledgement ack_object = (Acknowledgement)o;
				int acknowledged_seq_num = ack_object.getSequence_number();
				synchronized(lastAck)
				{
					lastAck.value = acknowledged_seq_num;
				//	System.out.println("last_acknowledged_seq_num " + lastAck.value);
					if(lastAck.value == max_sequence_number)
					{
				//		System.out.println("End of program.....calculate time and exit...");
						end_time = new Date().getTime();
						System.out.println("Total time taken in miliseconds: "+ (end_time - start_time));
						System.exit(0);
					}
				}
				
				synchronized (counter) {
					counter.value--;
					counter.notifyAll();
				}

				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		}

	}



}
