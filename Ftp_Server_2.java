import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Random;


public class Ftp_Server_2 {

	static int max_data_received = 1024*1024;
	static int expected_sequence_no = 1;


	public static void main(String[] args) throws Exception {
		int portNum = Integer.parseInt(args[0]);
		String outputFileName = args[1];
		float probLoss = Float.parseFloat(args[2]);

		byte[] data_received = new byte[max_data_received];	// max 1 MB buffer for receiving data
		DatagramPacket packet_head_and_data;
		byte[] header = new byte[8];
		InetAddress ip_client;
		int port_at_received;
		DatagramSocket socket = new DatagramSocket(portNum);
		socket.setReuseAddress(true);


		FileOutputStream fout = new FileOutputStream(outputFileName);
		while(true)
		{	
			//System.out.println("Ftp_Server");
			packet_head_and_data = new DatagramPacket(data_received, data_received.length);
			socket.receive(packet_head_and_data);
			ip_client = packet_head_and_data.getAddress();
			port_at_received = packet_head_and_data.getPort();
			//System.out.println("IP client is "+ip_client);
			byte[] rec_head_and_data = packet_head_and_data.getData();
			//System.out.println("rec_head_and_data is "+rec_head_and_data);
			ByteArrayInputStream bis = new ByteArrayInputStream(rec_head_and_data);
			ObjectInput in = null;
			try 
			{
				in = new ObjectInputStream(bis);
				Random r = new Random();
				float rand = r.nextFloat();
			//	System.out.println(rand);
				Object o = in.readObject(); 
				HeaderAndData h_n_d = (HeaderAndData)o;
				int seq_num = h_n_d.getSequence_number();
				byte[] data = h_n_d.getData();
			//	System.out.println("Data is "+Arrays.toString(data));
				int indication_number = h_n_d.getIndication_number();

				if(rand<=probLoss)
				{
					System.out.println("Packet loss, sequence number = "+seq_num);
					if(expected_sequence_no == seq_num)
					{
						System.out.println("Packet EXPECTED at the server dropped, sequence number = "+seq_num);
					}
				}
				else
				{
					


					if(expected_sequence_no == seq_num)
					{
						//expected_sequence_no++;

						int data_not_zero = 0;

						for(int i=0;i<data.length;i++)
						{
							if(data[i]!= 0)
							{
								data_not_zero++;
							}
							else
							{
								break;
							}

						}
						byte[] non_zero_data = new byte[data_not_zero];
						for(int i=0;i<data_not_zero;i++)
						{
							non_zero_data[i] = data[i];
						}
				
						fout.write(non_zero_data);

						// Generate Acknowledgements and send it to the client


						Acknowledgement send_ack = new Acknowledgement();
						send_ack.setSequence_number(seq_num);
						send_ack.setIndication_number(43690);

						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ObjectOutput out = null;
						out = new ObjectOutputStream(bos);
						out.writeObject(send_ack);
						bos.toByteArray();
						header = bos.toByteArray();
						DatagramPacket ack_packet = new DatagramPacket(header,header.length,ip_client, port_at_received);
						
				
						Thread.sleep(100);
						socket.send(ack_packet);
						expected_sequence_no++;
						header = new byte[8];

						data_received = new byte[max_data_received];
						System.gc();
					}

					else
					{
				//		System.out.println("Expected sequence number is "+expected_sequence_no);
				//		System.out.println("Received seq_num is "+ seq_num);

					}
				}
			} 
			finally 
			{
				
				//bis.close();
				//in.close();
			}
		}
	}

}
