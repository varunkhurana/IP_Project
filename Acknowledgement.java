import java.io.Serializable;



public class Acknowledgement implements Serializable {

	private int sequence_number;
	private int indication_number;
	
	
	public int getSequence_number() {
		return sequence_number;
	}
	public void setSequence_number(int sequence_number) {
		this.sequence_number = sequence_number;
	}
	public int getIndication_number() {
		return indication_number;
	}
	public void setIndication_number(int indication_number) {
		this.indication_number = indication_number;
	}
	
}
