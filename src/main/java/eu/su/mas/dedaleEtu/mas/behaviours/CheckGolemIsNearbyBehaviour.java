package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.Random;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;


public class CheckGolemIsNearbyBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = -2058134622078521998L;
	private ExploreCoopAgent myAgent;
	private ArrayList<ArrayList<String>> listDetectedAgents;
	private ArrayList<String> listReceivers;
	private Random r;

	public CheckGolemIsNearbyBehaviour (final ExploreCoopAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
		this.r = new Random();
	}
	
	@Override
	public void action() {
		this.listDetectedAgents = this.myAgent.getDetectedAgents();
		this.listReceivers = new ArrayList<String>();
		for(int i = 1; i < listDetectedAgents.size(); i++) { // start at 1 to exclude itself (first element is always itself
			this.listReceivers.add(listDetectedAgents.get(i).get(1)); // add local name of each detected agents
		}
		Boolean wumpusLocationSent = checkIsWumpus();
		if(wumpusLocationSent) // If a wumpus location has been broadcasted, no need to broadcast a stench location
			return;
		
		// If the agent has adjacent stenched nodes, broadcast one of their location to nearby agents.
		if(!this.myAgent.getStenchedNodes().isEmpty()) {
			if (this.listReceivers.size()>0) {
				String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

				ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
				msg.setSender(this.myAgent.getAID());
				msg.setProtocol("SendDestionationProtocol");
				
				if (myPosition!=""){
					String stenchedNode = this.myAgent.getStenchedNodes().get(this.r.nextInt(this.myAgent.getStenchedNodes().size())); // Select a stenched node among the list
					msg.setContent(stenchedNode);
					for(String agentName : listReceivers) {
						if(this.myAgent.getContactedAgents().contains(agentName)) // Do not send again to same agent
							continue;
						this.myAgent.addContactedAgent(agentName);
						msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
					}
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				}
			}
		}
	}
	
	// Check if wumpus found, and sent its locations to other agents if so
	// Return true if wumpus location was sent to other agents
	private Boolean checkIsWumpus() {
		if (!this.myAgent.didIMove()) {
			for(int i = 1; i< this.listDetectedAgents.size(); i++) {
				if(this.myAgent.getNextNode().equals(this.listDetectedAgents.get(i).get(2))) {
					this.myAgent.setWumpusFound(false);
					this.myAgent.addNodesToAvoid(this.myAgent.getNextNode());
					return false;
				}
			}
			this.myAgent.setWumpusFound(true);
			this.myAgent.incNbTry();
			if(this.myAgent.isItBlocked()) {
				this.myAgent.done();
			}
			if (this.listReceivers.size()>0) {
				String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

				ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
				msg.setSender(this.myAgent.getAID());
				msg.setProtocol("SendDestionationProtocol");
				
				if (myPosition!=""){
					msg.setContent(this.myAgent.getNextNode());
					for(String agentName : listReceivers) {
						if((!this.myAgent.getContactedAgents().isEmpty()) && this.myAgent.getContactedAgents().contains(agentName)) // Do not send again to same agent
							continue;
						this.myAgent.addContactedAgent(agentName);
						msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
					}
					
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
					return true;
				}
			}
		}
		return false;
	}
}