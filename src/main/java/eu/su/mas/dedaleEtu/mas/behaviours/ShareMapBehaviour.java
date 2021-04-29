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


public class ShareMapBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = -2058134622078521998L;
	private ExploreCoopAgent myAgent;
	private String sdType = "coopExplo";
	private ArrayList<String> listReceivers;
	

	public ShareMapBehaviour (final ExploreCoopAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
	}
	
	@Override
	public void action() {
//		System.out.println("-----------------SEND MESSAGE BEHAVIOuR EXECUTED-----------------");
		if(this.myAgent.getMap() != null){
			ArrayList<ArrayList<String>> listDetectedAgents = this.myAgent.getDetectedAgents();
			this.listReceivers = new ArrayList<String>();
			for(int i = 0; i < listDetectedAgents.size(); i++) {
				this.listReceivers.add(listDetectedAgents.get(i).get(1)); // add local name of each detected agents
			}
			if (listReceivers.size()>0) {
				String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

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
					
					for(String agentName : listReceivers) {
						if (agentName.equals(myAgent.getLocalName()))
							continue;
						msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
					}
					
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				}
			}
		}
		
	}
}