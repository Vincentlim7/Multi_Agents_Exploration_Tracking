package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploSoloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendMessagerBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMessageBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * <pre>
 * ExploreSolo agent. 
 * It explore the map using a DFS algorithm.
 * It stops when all nodes have been visited.
 *  </pre>
 *  
 * @author hc
 *
 */

public class ExploreSoloAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -6431752665590433727L;
	private MapRepresentation myMap;
	

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){
		super.setup();
		

		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the initial behaviours of the Agent here
		 * 
		 ************************************************/
		
		// Enlever
//		ArrayList<String> listAgents = new ArrayList<String>();
//		listAgents.add("Explo1");
//		listAgents.add("Explo2");
		
		DFAgentDescription dfd = new DFAgentDescription();
		signYP(dfd);
		
		ExploSoloBehaviour exploB = new ExploSoloBehaviour(this,this.myMap);
		
		lb.add(exploB);
		lb.add(new ReceiveMessageBehaviour(this));
//		lb.add(new SendMessagerBehaviour(this, listAgents, exploB));
//		lb.add(new SendMessagerBehaviour(this, dfd, exploB));
		
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	private void signYP(DFAgentDescription dfd) {
		dfd.setName(getAID()); // The agent AID
		ServiceDescription sd = new ServiceDescription ();
		sd.setType( "EXPLO" ); // You have to give a name to each service your agent offers
		sd.setName(getLocalName());//(local)name of the agent
		dfd.addServices(sd);
		//Register the service
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe){
			fe.printStackTrace();
		}
	}
	
	
	
	
	
}
