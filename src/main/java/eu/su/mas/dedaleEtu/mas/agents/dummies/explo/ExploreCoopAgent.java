package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.CheckGolemIsNearbyBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.EndBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.InitMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.PingNeighborsBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMessageBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.TrackGolemBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
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
	
	private static final int NB_MAX_TRY = 500; 	// Number of time the agent try to move toward the wumpus node unsucessfully consecutively before considering he is blocking a wumpus
	private int nbTry;							// Number of time the agent tried to move toward the wumpus node unsucessfully consecutively
	private static int cpt = 1;		// cpt used to affect idAgent
	private String idAgent;			// id of agent
	private List<Behaviour> lb;
	private ArrayList<ArrayList<String>> listDetectedAgents;	// list of nearby agents (self included), array element is [agentId, agentName, agentPosition]
	private ArrayList<String> stenchedNodes;					// List of adjacent stenched nodes, Used in Track Mod
	private ArrayList<String> nodesToAvoid;						// List of adjacent stenched nodes containing an agent, Used in Track Mod
	private ArrayList<String> listContactedAgents;				// List of agent whom have already been sent the latest destination, Used in Track mod
	private ArrayList<String> shortestPath;	// Latest path computed (to avoid computing it everytime)
	private Boolean exploMod; 			// Is the agent in exploring mod ? (either exploring or tracking)
	private Boolean exploFinished;		// Did the agent explored 100% of the map ?
	private String nextNode;			// Next node to move to
	private String oldNode;				// Node beforing moving
	private String newNode;				// Node after moving
	private Boolean wumpusFound;		// Did the agent find a wumpus ?
	private String destination;			// Node sent by other agent containing stench or wumpus
	private Boolean finished;			// Did the agent block a wumpus ?
	private FSMBehaviour fsm;
	// ---------- Setting up state name for the FSM Behaviour ----------
	private static final String INIT = "InitMap";
	private static final String EXPLO = "Exploration";
	private static final String TRACK = "Track";
	private static final String PING = "Ping";
	private static final String CHECKW = "CheckWumpus";
	private static final String END = "End";

	protected void setup(){
		super.setup();
		this.idAgent = String.valueOf(cpt);
		this.exploMod = true;
		this.exploFinished = false;
		this.wumpusFound = false;
		this.destination = null;
		this.finished = false;
		ExploreCoopAgent.cpt++;
		resetDetectedAgents();
		resetContactedAgents();
		resetNodesToAvoid();
		resetShortestPath();
		registerYellowPage();
		
		// ---------- Setting up FSM Behaviour ----------
		
		fsm = new FSMBehaviour(this);
		
		// ---------- Registering states ----------
		
		fsm.registerFirstState(new InitMapBehaviour(this),INIT);
		fsm.registerState(new ExploCoopBehaviour(this), EXPLO);
		fsm.registerState(new TrackGolemBehaviour(this), TRACK);
		fsm.registerState(new PingNeighborsBehaviour(this), PING);
		fsm.registerState(new CheckGolemIsNearbyBehaviour(this), CHECKW);
		fsm.registerLastState(new EndBehaviour(this), END);
		
		// ---------- Registering transitions ----------
		
		// Transitions from init
		fsm.registerDefaultTransition(INIT, EXPLO);
		
		// Transitions from exploration
		fsm.registerTransition(EXPLO, PING, 1);
		fsm.registerTransition(EXPLO, TRACK, 2);
		
//		// Transitions from track
		fsm.registerTransition (TRACK, PING, 1);
		fsm.registerTransition (TRACK, EXPLO, 2);
		fsm.registerTransition (TRACK, END, 3);
//		
//		// Transitions from ping
		fsm.registerTransition (PING, EXPLO, 1);
		fsm.registerTransition (PING, CHECKW, 2);
		
		// Transitions from checkWumpus
		fsm.registerDefaultTransition(CHECKW, TRACK);
		
		// ---------- FSM Behaviour setted up ----------
		
		// ---------- Add behaviours and launch----------
		
		this.lb=new ArrayList<Behaviour>();
		this.lb.add(fsm);
		this.lb.add(new ReceiveMessageBehaviour(this));
		addBehaviour(new startMyBehaviours(this, this.lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");
		
		try {
			this.doWait(3000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Register the agent to the yellow page
	private void registerYellowPage() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.getAID());
		ServiceDescription sd = new ServiceDescription ();
		sd.setType("coopExplo");
		sd.setName(this.getLocalName());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe){
			fe.printStackTrace();
		}
	}
	
	// Returns the name of agents registered in the  yellow page
	public ArrayList<String> getYellowPageContent(){
		ArrayList <String> agentsName= new ArrayList<String>();
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription ();
		sd.setType("coopExplo");
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
			e.printStackTrace();
		}
//		System.out.println(this.getLocalName() + " --> Yellow page content : " + agentsName);
		return agentsName;
	}
	
	public void updateMap(MapRepresentation map) {
		this.myMap = map;
	}
	
	public MapRepresentation getMap() {
		return this.myMap;
	}
	
	
	public String getIdAgent() {
		return this.idAgent;
	}
	
	// exploMod = False means the agent is currently in tracking mod
	public void switchState() {
		this.exploMod = !this.exploMod;
	}
	
	public Boolean isExploring() {
		return this.exploMod;
	}
	
	public void exploDone() {
		this.exploFinished = true;
	}
	
	public Boolean isExploFinished() {
		return this.exploFinished;
	}
	
	public ArrayList<String> getInfosAgent(){
		ArrayList<String> infos = new ArrayList<String>();
		infos.add(this.idAgent);
		infos.add(this.getLocalName());
		infos.add(this.getCurrentPosition());
		return infos;
	}
	
	// ---------- Detected agents functions ----------
	public void resetDetectedAgents() {
		this.listDetectedAgents = new ArrayList<ArrayList<String>>();
		addDetectedAgent(this.getInfosAgent());		// the agent always has itself as the first element
	}
	
	public void addDetectedAgent(ArrayList<String> agentInfos) {
		for(int i = 0; i < this.listDetectedAgents.size(); i++) 
			if(this.listDetectedAgents.get(i).get(0).equals(agentInfos.get(0))) // do not add duplicate
				return;
		this.listDetectedAgents.add(agentInfos);
	}
	
	public ArrayList<ArrayList<String>> getDetectedAgents(){
		return this.listDetectedAgents;
	}
	
	// ---------- Detected agents functions ----------
	
	public void resetContactedAgents() {
		this.listContactedAgents = new ArrayList<String>();
	}
	
	public void addContactedAgent(String agentName) {
		this.listContactedAgents.add(agentName);
	}
	
	public ArrayList<String> getContactedAgents(){
		return this.listContactedAgents;
	}
	
	// ---------- Nodes functions ----------
	// --- Old node ---
	
	public void setOldNode(String node) {
		this.oldNode = node;
	}

	public String getOldNode() {
		return this.oldNode;
	}
	
	// --- New node ---
	
	public void setNewNode(String node) {
		this.newNode = node;
	}

	public String getNewNode() {
		return this.newNode;
	}
	
	// --- Next node ---
	
	public void setNextNode(String node) {
		this.nextNode = node;
	}

	public String getNextNode() {
		return this.nextNode;
	}
	
	// --- Stenched nodes ---
	public void resetStenchedNodes() {
		this.stenchedNodes = new ArrayList<String>();
	}
	
	public void addStenchedNodes(String node) {
		this.stenchedNodes.add(node);
	}
	
	public ArrayList<String> getStenchedNodes(){
		return this.stenchedNodes;
	}
	
	// --- Stenched nodes to avoid---
	public void resetNodesToAvoid() {
		this.nodesToAvoid = new ArrayList<String>();
	}
	
	public void addNodesToAvoid(String node) {
		this.nodesToAvoid.add(node);
	}
	
	public ArrayList<String> getNodesToAvoid(){
		return this.nodesToAvoid;
	}
	
	// ---------- Other functions ----------
	
	public Boolean didIMove() {
		return !this.oldNode.equals(this.newNode);
	}
	
	public void setWumpusFound(Boolean isItFound) {
		this.wumpusFound = isItFound;
	}
	
	public Boolean getWumpusFound() {
		return this.wumpusFound;
	}
	
	public void setDestination(String node) {
		this.destination = node;
	}
	
	public String getDestination() {
		return this.destination;
	}
	
	public void resetNbTry() {
		this.nbTry = 0;
	}
	
	public void incNbTry() {
		this.nbTry++;
	}
	
	public int getNbTry() {
		return this.nbTry;
	}
	
	public Boolean isItBlocked() { // Is the agent blocking a wumpus ?
		return this.nbTry == ExploreCoopAgent.NB_MAX_TRY;
	}
	
	public void done() {
		this.finished = true;
	}
	
	public Boolean getFinished() {
		return this.finished;
	}

	
	public void resetShortestPath() {
		this.shortestPath = new ArrayList<String>();
	}
	
	public void setShortestPath(ArrayList<String> path) {
		this.shortestPath = path;
	}
	
	public ArrayList<String> getShortestPath(){
		return this.shortestPath;
	}

}
