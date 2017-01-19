import java.io.Serializable;

public class HeaderAndData implements Serializable{
	
	private int sequence_number;
	private byte[] data;
	private int indication_number;
	private static final long serialVersionUID = 1L;
	
	
	public HeaderAndData(int sequence_num, int indication_num, byte[] data)
	{
		this.sequence_number = sequence_num;
		this.indication_number = indication_num;
		
		this.data = data;
	}
	public int getSequence_number() {
		return sequence_number;
	}
	public void setSequence_number(int sequence_number) {
		this.sequence_number = sequence_number;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public int getIndication_number() {
		return indication_number;
	}
	public void setIndication_number(int indication_number) {
		this.indication_number = indication_number;
	}
	
}
