package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;

import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploSoloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMessageBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendMessagerBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * <pre>
 * ExploreCoop agent. 
 * Basic example of how to "collaboratively" explore the map
 *  - It explore the map using a DFS algorithm and blindly tries to share the topology with the agents within reach.
 *  - The shortestPath computation is not optimized
 *  - Agents do not coordinate themselves on the node(s) to visit, thus progressively creating a single file. It's bad.
 *  - The agent sends all its map, periodically, forever. Its bad x3.
 *   - You should give him the list of agents'name to send its map to in parameter when creating the agent.
 *   Object [] entityParameters={"Name1","Name2};
 *   ag=createNewDedaleAgent(c, agentName, ExploreCoopAgent.class.getName(), entityParameters);
 *  
 * It stops when all nodes have been visited.
 * 
 * 
 *  </pre>
 *  
 * @author hc
 *
 */


public class ExploreCoopAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -7969469610241668140L;
	private MapRepresentation myMap;
	private DFAgentDescription dfd;
	private ExploCoopBehaviour exploB;
	public static int cpt = 1;
	private int idAgent;
	private ArrayList<Integer> listDetectedAgents = new ArrayList<Integer>();
	

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){
		super.setup();
		this.idAgent = cpt;
		cpt++;
		listDetectedAgents.add(this.idAgent);

		final Object[] args = getArguments();
		

		List<Behaviour> lb=new ArrayList<Behaviour>();

		this.dfd = new DFAgentDescription();
		signYP(dfd);
		
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/


		this.exploB = new ExploCoopBehaviour(this,this.myMap);
		lb.add(exploB);
		lb.add(new ReceiveMessageBehaviour(this));
		lb.add(new SendMessagerBehaviour(this, dfd, this.exploB));
		

		
		
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
	
	public MapRepresentation getMap() {
		return myMap;
	}
	
	public ExploCoopBehaviour getExploB() {
		return this.exploB;
	}
	
	public int getIdAgent() {
		return this.idAgent;
	}
	
	public void addDetectedAgents(int idAgent) {
		listDetectedAgents.add(idAgent);
	}
	
	public ArrayList<Integer> getDetectedAgents(){
		return listDetectedAgents;
	}
	
}
