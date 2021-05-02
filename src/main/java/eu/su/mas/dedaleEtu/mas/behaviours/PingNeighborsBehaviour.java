package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;


public class PingNeighborsBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = -2058134622078521998L;
	private ExploreCoopAgent myAgent;
	private ArrayList<String> listReceivers;
	private int exitValue;

	public PingNeighborsBehaviour (final ExploreCoopAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
	}
	
	@Override
	public void action() {
		this.listReceivers = this.myAgent.getYellowPageContent(); // Get the receivers list from the yellow page
		if (listReceivers.size() > 0) {
			String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			
			ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
			msg.setSender(this.myAgent.getAID());
			msg.setProtocol("PingNeighborsProtocol");
			
			if (myPosition!=""){
				msg.setContent("");				
				for(String agentName : listReceivers)  {
					if (agentName.equals(myAgent.getLocalName()))
						continue;
					msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
				}
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				if(this.myAgent.isExploring()) {
					this.exitValue = 1;	// Go back to exploring
					return;
				}else {
					this.exitValue = 2; // Go back to tracking
					return;
				}
			}
		}
	}
	
	@Override
	public int onEnd() {
		return this.exitValue;
	}	
}