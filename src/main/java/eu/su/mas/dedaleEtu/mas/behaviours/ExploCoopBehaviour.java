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
public class ExploCoopBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;
	private boolean finished = false;
	private boolean firstIte = true;
	private ArrayList<String> listIdAgent;
	private ExploreCoopAgent myAgent;
	private PingNeighborsBehaviour pingBehaviour;
	private ReceiveMessageBehaviour receiveBehaviour;


/**
 * 
 * @param myagent
 */
	public ExploCoopBehaviour(final ExploreCoopAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
	}

	@Override
	public void action() {
		if(this.myAgent.getMap()==null) {
			this.myAgent.createMap();

		}
		if(firstIte) {
			this.pingBehaviour = new PingNeighborsBehaviour(this.myAgent);
			this.receiveBehaviour = new ReceiveMessageBehaviour(this.myAgent);
			this.myAgent.addBehaviour(pingBehaviour);
			this.myAgent.addBehaviour(receiveBehaviour);
			firstIte = false;
		}
		
		this.myAgent.resetDetectedAgents();		// Reset detected agents list
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition(); // Retrieve current pos

		if (myPosition!=null){
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe(); // List of observable from the agent's current position
			System.out.println(this.myAgent.getLocalName() + " --> Observations : " + lobs.toString());
			try {
				this.myAgent.doWait(500);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 1) remove the current node from openlist and add it to closedNodes.
			this.myAgent.getMap().addNode(myPosition, MapAttribute.closed);

			// 2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				boolean isNewNode=this.myAgent.getMap().addNewNode(nodeId);
				//the node may exist, but not necessarily the edge
				if (myPosition!=nodeId) {
					this.myAgent.getMap().addEdge(myPosition, nodeId);
					if (nextNode==null && isNewNode) {
						nextNode=nodeId; // nextNode is adjacent to current pos
					}
				}
			}

			// 3) while openNodes is not empty, continues.
			if (this.myAgent.getMap().hasOpenNode()){
				// 4) select next move.
				// To avoid agents following each other after sharing their map,
				// they chose a different nextNode (smallest id choose first)
				if (nextNode==null){
					ArrayList<ArrayList<String>> listDetectedAgents = this.myAgent.getDetectedAgents();
					this.listIdAgent = new ArrayList<String>();
					for(int i = 0; i < listDetectedAgents.size(); i++) {
						this.listIdAgent.add(listDetectedAgents.get(i).get(0)); // add id of each detected agents
					}
					Collections.sort(listIdAgent);
					for(int i=0; i < listIdAgent.size(); i++) {
						if (listIdAgent.get(i) == this.myAgent.getIdAgent()) {
							nextNode=this.myAgent.getMap().getShortestPathToClosestOpenNode(myPosition).get(i);
							break;
						}
					}
					
					//System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myAgent.getMap().getOpenNodes()+"| nextNode: "+nextNode);
				}else {
					//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myAgent.getMap().getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}else { // Exploration finished
				// Terminate sub behaviours
				this.pingBehaviour.stop();
				this.receiveBehaviour.finished();
				finished=true;
				System.out.println(this.myAgent.getLocalName() + " --> Exploration successufully done, behaviour removed.");
//				this.myAgent.addBehaviour(new TrackGolemBehaviour(this.myAgent)); // Start next behaviour
			}
		}
	}

	@Override
	public boolean done() {
		return finished;
	}
	
}
