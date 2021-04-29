package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;


public class PingNeighborsBehaviour extends TickerBehaviour{

	private static final long serialVersionUID = -2058134622078521998L;
	private ExploreCoopAgent myAgent;
	private String sdType = "coopExplo";
	private ArrayList<String> listReceivers;

	public PingNeighborsBehaviour (final ExploreCoopAgent myAgent) {
		super(myAgent, 200);
		this.myAgent = myAgent;
		System.out.println(this.myAgent.getLocalName() + " --> Ping behaviour created");
	}
	
	@Override
	public void onTick() {
		this.listReceivers = this.myAgent.getYellowPageContent();
		DFAgentDescription[] result;
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
			}
		}
	}
}