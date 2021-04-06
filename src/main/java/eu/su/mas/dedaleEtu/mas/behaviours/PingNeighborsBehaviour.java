package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

/**
 * This example behaviour try to send a hello message (every 3s maximum) to agents Collect2 Collect1
 * @author hc
 *
 */
public class PingNeighborsBehaviour extends TickerBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2058134622078521998L;
	private ArrayList<String> listAgent;
	private ExploCoopBehaviour exploB;
	private DFAgentDescription dfd;

	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
//	public SendMessagerBehaviour (final Agent myagent, ArrayList<String> listAgent, ExploSoloBehaviour exploB) {
//		super(myagent, 300);
//		//super(myagent);
//		this.myAgent = myAgent;
//		this.listAgent = listAgent;
//		this.exploB = exploB;
//	}

	public PingNeighborsBehaviour (final Agent myagent, DFAgentDescription dfd, ExploCoopBehaviour exploB) {
		super(myagent, 300);
		this.dfd = dfd;
		this.exploB = exploB;
	}
	
	@Override
	public void onTick() {
		DFAgentDescription[] result;
		try {
			result = DFService.search(this.myAgent, this.dfd);
			//You get the list of all the agents (AID) offering this service
			System.out. println ( result.length + "results " ) ;
			if ( result.length>0) {
				System.out. println ( result[0].getName());
				String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

				//A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
				ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
				msg.setSender(this.myAgent.getAID());
				msg.setProtocol("PingNeighborsProtocol");

				if (myPosition!=""){
					System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to reach its friends");
					msg.setContent(String.valueOf(((ExploreCoopAgent)this.myAgent).getIdAgent()));
					
					for(DFAgentDescription agentName : result) {
						if (agentName.equals(myAgent.getLocalName()))
							continue;
						msg.addReceiver(new AID(agentName.toString(),AID.ISLOCALNAME));
					}

					//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				}
			}
		} catch (FIPAException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}