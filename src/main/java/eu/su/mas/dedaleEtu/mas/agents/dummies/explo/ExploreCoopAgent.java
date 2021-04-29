package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;

import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMessageBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
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
	private ArrayList<ArrayList<String>> listDetectedAgents;	// list of nearby agents (self included)
	private ArrayList<String> listAgentsPos;		// pos list of nearby agents
	private String sdType = "coopExplo";
	DFAgentDescription dfd;
	

	protected void setup(){
		super.setup();
		this.idAgent = String.valueOf(cpt);
		cpt++;
		this.dfd = new DFAgentDescription();
		resetDetectedAgents();
		registerYellowPage();
		List<Behaviour> lb=new ArrayList<Behaviour>();
		lb.add(new ExploCoopBehaviour(this));
		addBehaviour(new startMyBehaviours(this,lb));
		System.out.println("the  agent "+this.getLocalName()+ " is started");
		try {
			this.doWait(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("the  agent "+this.getLocalName()+ " dfd : " + this.getYellowPageContent());
	}
	
	public DFAgentDescription getAgentDescription() {
		return this.dfd;
	}
	
	private void registerYellowPage() {
		this.dfd.setName(this.getAID());		// The agent AID
		ServiceDescription sd = new ServiceDescription ();
		sd.setType(sdType);					// You have to give a name to each service your agent offers
		sd.setName(this.getLocalName());		// local name of the agent
		this.dfd.addServices(sd);
		try {
			DFService.register(this, this.dfd);	// Register the service
		} catch (FIPAException fe){
			fe.printStackTrace();
		}
	}
	
	// Returns the name of agents registered in the sdType yellow page
	public ArrayList<String> getYellowPageContent(){
		ArrayList <String> agentsName= new ArrayList<String>();
		
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription ();
		sd.setType(sdType); // You have to give a name to each service your agent offers
		dfd.addServices(sd);
		
		DFAgentDescription[] result;
		try {
			result = DFService.search(this, dfd);
			for(int i=0; i< result.length; i++)
			for (DFAgentDescription agent : result) {
				if(agentsName.contains(agent.getName().getLocalName()))
					continue;
				agentsName.add(agent.getName().getLocalName());	
			}

		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(this.getLocalName() + " --> Yellow page content : " + agentsName);
		return agentsName;
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
	
	public ArrayList<String> getInfosAgent(){
		ArrayList<String> infos = new ArrayList<String>();
		infos.add(this.idAgent);
		infos.add(this.getLocalName());
		infos.add(this.getCurrentPosition());
		return infos;
	}
	// id list of detected agents functions
	public void resetDetectedAgents() {
		this.listDetectedAgents = new ArrayList<ArrayList<String>>();
		addDetectedAgent(this.getInfosAgent());
	}
	
	public void addDetectedAgent(ArrayList<String> agentInfos) { // do not add duplicate
		for(int i = 0; i < this.listDetectedAgents.size(); i++) 
			if(this.listDetectedAgents.get(i).get(0).equals(agentInfos.get(0)))
				return;
		this.listDetectedAgents.add(agentInfos);
	}
	
	public ArrayList<ArrayList<String>> getDetectedAgents(){
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
