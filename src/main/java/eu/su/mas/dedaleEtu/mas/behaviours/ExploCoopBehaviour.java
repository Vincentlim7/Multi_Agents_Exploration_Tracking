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
public class ExploCoopBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;
	
	private boolean finished = false;
	
	private ArrayList<String> listIdAgent;
	private String myId;
	ExploreCoopAgent myAgent;
	PingNeighborsBehaviour pingBehaviour;
	ReceiveMessageBehaviour receiveBehaviour;


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

//		System.out.println("------- " +this.myAgent.getLocalName() + " ITERATION SUIVANTE -------");
		if(this.myAgent.getMap()==null) {
			this.myAgent.createMap();
			this.pingBehaviour = new PingNeighborsBehaviour(this.myAgent);
			this.receiveBehaviour = new ReceiveMessageBehaviour(this.myAgent);
			this.myAgent.addBehaviour(pingBehaviour);
			this.myAgent.addBehaviour(receiveBehaviour);
		}
		
		this.myAgent.resetDetectedAgents();
		// 0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			// List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
//			System.out.println("Observations de l'agent : " + lobs.toString());
//			System.out.println("TYPE :" + lobs.get(0).getRight().getClass());

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
						nextNode=nodeId;
//						System.out.println("NEXT NODE : : " + nextNode.toString());
//						System.out.println("TYPE DE NEXT NODE : : " + nextNode.toString().getClass());
					}
				}
			}

			// 3) while openNodes is not empty, continues.
			if (!this.myAgent.getMap().hasOpenNode()){
				// Explo finished
				System.out.println("-----------------PING BEHEVIOUR FINISHED-----------------");
				this.pingBehaviour.stop();
				this.receiveBehaviour.finished();
				finished=true;
				System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
				this.myAgent.addBehaviour(new TrackGolemBehaviour(this.myAgent));
			}else{
				// 4) select next move.
				// 4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				// no directly accessible openNode
				// chose one, compute the path and take the first step.
				// To avoid agents following each other after sharing their map,
				// they chose a different nextNode (smallest id choose first)
				if (nextNode==null){
					listIdAgent =this.myAgent.getDetectedAgents();
					myId = this.myAgent.getIdAgent();
					Collections.sort(listIdAgent);
					for(int i=0; i < listIdAgent.size(); i++) {
						if (listIdAgent.get(i) == myId) {
							nextNode=this.myAgent.getMap().getShortestPathToClosestOpenNode(myPosition).get(i);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
							break;
						}
					}
					
					//System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myAgent.getMap().getOpenNodes()+"| nextNode: "+nextNode);
				}else {
					//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myAgent.getMap().getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
				System.out.println(this.myAgent.getLocalName() + " current pos : " + myPosition + " / next post : " + nextNode);
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}
			
		}
	}

	@Override
	public boolean done() {
		return finished;
	}
	
}
