package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;


public class SendMessagerBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = -2058134622078521998L;
	private ArrayList<String> listAgent;
	private ExploreCoopAgent myAgent;
	private DFAgentDescription dfd;


	public SendMessagerBehaviour (final ExploreCoopAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
		this.dfd = myAgent.getAgentDescription();
	}
	
	@Override
	public void action() {
//		System.out.println("-----------------SEND MESSAGE BEHAVIOuR EXECUTED-----------------");
		if(this.myAgent.getMap() != null){
			DFAgentDescription[] result;
			try {
				result = DFService.search(this.myAgent, this.dfd);
				//You get the list of all the agents (AID) offering this service
//				System.out. println ( result.length + "results " ) ;
				if ( result.length>0) {
					String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	
					//A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
					ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
					msg.setSender(this.myAgent.getAID());
					msg.setProtocol("ShareMapProtocol");
	
					if (myPosition!=""){
//						System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to reach its friends");
						try {
							msg.setContentObject((Serializable)this.myAgent.getMap().getSerializableGraph());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						for(DFAgentDescription agentName : result) {
							if (agentName.equals(myAgent.getLocalName()))
								continue;
							msg.addReceiver(new AID(agentName.getName().getLocalName(),AID.ISLOCALNAME));
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
}