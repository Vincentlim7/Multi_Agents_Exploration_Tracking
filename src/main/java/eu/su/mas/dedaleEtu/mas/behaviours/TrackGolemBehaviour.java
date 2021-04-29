package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;


import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


/**
 * <pre>
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs. 
 * This (non optimal) behaviour is done until all nodes are explored. 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.
 * Warning, the sub-behaviour ShareMap periodically share the whole map
 * </pre>
 * @author hc
 *
 */
public class TrackGolemBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;
	private boolean firstIte = true;
	private ArrayList<Integer> listIdAgent;
	private int myId;
	private Random r;
	private ExploreCoopAgent myAgent;
	private String oldPos;
	private String newPos;
	private Boolean golemFound;
	private String nextNode2;
	private PingNeighborsBehaviour pingBehaviour;
	private ReceiveMessageBehaviour receiveBehaviour;
/**
 * 
 * @param myagent
 */
	public TrackGolemBehaviour(final ExploreCoopAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
		this.r = new Random();
		this.oldPos = "";
		this.golemFound = false;
		System.out.println("-----------------TRACK GOLEM BEHAVIOUR CREATED-----------------");
	}

	@Override
	public void action() {
		if(firstIte) {
			this.pingBehaviour = new PingNeighborsBehaviour(this.myAgent);
			this.receiveBehaviour = new ReceiveMessageBehaviour(this.myAgent);
			this.myAgent.addBehaviour(pingBehaviour);
			this.myAgent.addBehaviour(receiveBehaviour);
			firstIte = false;
		}
		this.myAgent.resetDetectedAgents();		// Reset detected agents list
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		// while(true)
		System.out.println("-----------------DEBUT ITERATION-----------------");
		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			System.out.println(this.myAgent.getLocalName() + " --> Observations : " + lobs.toString());
			String nextNode = null;

			try {
				this.myAgent.doWait(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// If golem not found, compute a nextNode
			// Else, golem was found because the agent was not able to move at the previous iteration
			// Try moving to the same node
			if(golemFound == false) {
				ArrayList<String> stenchedNodes = new ArrayList<String>(); // List of neighboring nodes with a stench
				for (int i = 1; i<lobs.size(); i++) {
					ArrayList<Couple> content = (ArrayList) lobs.get(i).getRight();
//						System.out.println("node" + lobs.get(i));
//						System.out.println("Content :" + content);
					if (content.isEmpty() == false) {
						if (content.get(content.size() - 1).getLeft().toString().equals("Stench")) {
							stenchedNodes.add((String) lobs.get(i).getLeft());
//								System.out.println("POTENTIAL NODE FOUND");
						}						
					}					
				}
//					for(Couple node:lobs) {
//						ArrayList<Couple> content = (ArrayList) node.getRight();
////						System.out.println("node" + node);
////						System.out.println("Content :" + content);
//						if (content.isEmpty() == false) {
//							if (content.get(content.size() - 1).getLeft().toString().equals("Stench")) {
//								potentialNodes.add((String) node.getLeft());
////								System.out.println("POTENTIAL NODE FOUND");
//							}						
//						}
//					}
				
				// If a stench is detected, moving to one of the nodes with a stench (randomly)
				// Else moving randomly
				if (stenchedNodes.isEmpty() == false) {
			        nextNode = stenchedNodes.get(this.r.nextInt(stenchedNodes.size()));
				} else {
					nextNode = lobs.get(this.r.nextInt(lobs.size()-1)+1).getLeft();
					System.out.println("RANDOM MOVE " + nextNode);
				}
			} else {
				nextNode = nextNode2;
				this.golemFound = false;
			}

			
			oldPos = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			System.out.println("OldPos : " + oldPos + " nextNode : " + nextNode);
			newPos = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
//				System.out.println("old Pos :" + oldPos);
//				System.out.println("new Pos :" + newPos);
			
			// If the agent was not able to move
			if (oldPos.equals(newPos)) {
//					System.out.println("PERHAPS GOLEM FOUND");
				this.myAgent.addBehaviour(new CheckGolemIsNearbyBehaviour(this.myAgent));
				try {
					this.myAgent.doWait(300);
				} catch (Exception e) {
					e.printStackTrace();
				}
				// Check if there is an agent in the next node
//					System.out.println("nextNode : " + nextNode + " " + this.myAgent.getAgentsPos().contains(nextNode));
				if (this.myAgent.getAgentsPos().contains(nextNode) == false) {
					System.out.println(this.myAgent.getLocalName() + " : nextNode" + nextNode + " listPos : " + this.myAgent.getAgentsPos());
					nextNode2 = nextNode;
					this.golemFound = true;
					System.out.println(this.myAgent.getLocalName() + " : GOLEM FOUND at " + nextNode + " Current pos : " + newPos);
				}
			}
		}
		
	}

	@Override
	public boolean done() {
		return finished;
	}

	
}
