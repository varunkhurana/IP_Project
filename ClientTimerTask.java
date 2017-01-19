import java.util.TimerTask;


public class ClientTimerTask extends TimerTask{
	
	private LastAck lastAck;
	private int seqNumber;
	
	public ClientTimerTask(LastAck lastAck, int seqNumber) {
		this.lastAck = lastAck;
		this.seqNumber = seqNumber;
	}

	@Override
	public void run() {
		if (lastAck.value == seqNumber - 1) {
			synchronized (lastAck) {
				
			}
		} else {
			this.cancel();
		}
		
		
	}

}
