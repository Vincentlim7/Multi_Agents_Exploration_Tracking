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
	private DFAgentDescription dfd;

	public PingNeighborsBehaviour (final ExploreCoopAgent myAgent) {
		super(myAgent, 200);
		this.myAgent = myAgent;
		this.dfd = myAgent.getAgentDescription();
//		System.out.println("-----------------PING BEHAVIOUR CREATED-----------------");
	}
	
	@Override
	public void onTick() {
		DFAgentDescription[] result;
		try {
			result = DFService.search(this.myAgent, this.dfd);
			//You get the list of all the agents (AID) offering this service
//			System.out. println ( result.length + "results " ) ;
			if ( result.length>0) {
//				System.out. println ( result[0].getName());
				String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

				//A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
				ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
				msg.setSender(this.myAgent.getAID());
				msg.setProtocol("PingNeighborsProtocol");

				if (myPosition!=""){
//					System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to reach its friends");
					msg.setContent(this.myAgent.getIdAgent());
					
					for(DFAgentDescription agentName : result) {
						if (agentName.getName().getLocalName().equals(myAgent.getLocalName()))
							continue;
//						System.out.println("Local Name");
//						System.out.println("myAgent ; " + myAgent.getLocalName());
//						System.out.println("this.myAgent : " + this.myAgent.getLocalName());
//						System.out.println("Sender : " + this.myAgent.getName() + " Receiver : " + agentName.getName().getLocalName());
//						System.out.println(agentName.getName().getLocalName());
//						System.out.println("TEST");
						msg.addReceiver(new AID(agentName.getName().getLocalName(),AID.ISLOCALNAME));
					}

					//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
//					System.out.println("-----------------"+ this.myAgent.getName()+"PING SENT-----------------");
				}
			}
		} catch (FIPAException e1) {
			e1.printStackTrace();
		}
	}
}