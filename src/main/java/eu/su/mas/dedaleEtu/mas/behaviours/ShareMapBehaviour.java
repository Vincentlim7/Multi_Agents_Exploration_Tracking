package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class ShareMapBehaviour extends OneShotBehaviour{
	
	private MapRepresentation myMap;

	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public ShareMapBehaviour(Agent a, long period,MapRepresentation mymap) {
		super(a);
		this.myMap=mymap;	
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;


	@Override
	public void action() {
		//4) At each time step, the agent blindly send all its graph to its surrounding to illustrate how to share its knowledge (the topology currently) with the the others agents. 	
		// If it was written properly, this sharing action should be in a dedicated behaviour set, the receivers be automatically computed, and only a subgraph would be shared.
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription ();
		sd.setType( "EXPLO" ); // You have to give a name to each service your agent offers
		dfd.addServices(sd);
		DFAgentDescription[] result;
		try {
			result = DFService.search(this.myAgent, dfd);
			//You get the list of all the agents (AID) offering this service
			System.out. println ( result.length + "results " ) ;
			if ( result.length>0) {
				System.out. println ( result[0].getName());
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SHARE-TOPO");
				msg.setSender(this.myAgent.getAID());
				for (DFAgentDescription agentName : result) {
					if (agentName.equals(myAgent.getLocalName()))
						continue;
					System.out.println("AGENT NAME :" + agentName.toString());
					msg.addReceiver(new AID(agentName.getName().getLocalName(),AID.ISLOCALNAME));
				}
				
				SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph();
				try {					
					msg.setContentObject(sg);
				} catch (IOException e) {
					e.printStackTrace();
				}
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			}
		} catch (FIPAException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
