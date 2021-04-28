package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;

import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMessageBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendMessagerBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.TrackGolemBehaviour;
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
	public static int cpt = 1;
	private String idAgent;
	private ArrayList<String> listDetectedAgents;
	private ArrayList<String> listAgentsPos;
	DFAgentDescription dfd;
	

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){
		super.setup();
		
		this.idAgent = String.valueOf(cpt);
		cpt++;
		
		resetDetectedAgents();
		
		this.dfd = new DFAgentDescription();
		signYP(dfd);
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		lb.add(new ExploCoopBehaviour(this));
//		lb.add(new TrackGolemBehaviour(this));
		
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

	
	public DFAgentDescription getAgentDescription() {
		return this.dfd;
	}
	
	
	public void createMap() {
		this.myMap= new MapRepresentation();
	}
	
	public MapRepresentation getMap() {
		return this.myMap;
	}
	
	
	public String getIdAgent() {
		return this.idAgent;
	}
	
	// id list of detected agents functions
	public void resetDetectedAgents() {
		this.listDetectedAgents = new ArrayList<String>();
		addDetectedAgent(this.idAgent);
	}
	
	public void addDetectedAgent(String idAgent) {
		this.listDetectedAgents.add(idAgent);
	}
	
	public ArrayList<String> getDetectedAgents(){
		return this.listDetectedAgents;
	}
	
	// pos list of detected agents functions
	public void resetAgentsPos() {
		this.listAgentsPos = new ArrayList<String>();
	}
	
	public void addAgentPos(String agentPos) {
		this.listAgentsPos.add(idAgent);
	}
	
	public ArrayList<String> getAgentsPos(){
		return this.listAgentsPos;
	}
	
}
