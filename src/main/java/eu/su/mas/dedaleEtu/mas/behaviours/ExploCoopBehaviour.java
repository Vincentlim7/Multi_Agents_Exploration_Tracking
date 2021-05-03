package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;


import jade.core.behaviours.OneShotBehaviour;



public class ExploCoopBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;
	private ArrayList<String> listIdAgent;	// id list of detected agents
	private ExploreCoopAgent myAgent;
	private int exitValue;
	


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
		this.exitValue = 0;
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition(); // Retrieve current pos

		if (myPosition!=null){
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe(); // List of observable from the agent's current position
			System.out.println(this.myAgent.getLocalName() + " --> Explo observations : " + lobs.toString());
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Remove the current node from openlist and add it to closedNodes.
			this.myAgent.getMap().addNode(myPosition, MapAttribute.closed);
			
			// Get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			this.myAgent.setNextNode(null);
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				boolean isNewNode=this.myAgent.getMap().addNewNode(nodeId);
				//the node may exist, but not necessarily the edge
				if (myPosition!=nodeId) {
					this.myAgent.getMap().addEdge(myPosition, nodeId);
					if (this.myAgent.getNextNode()==null && isNewNode) {
						this.myAgent.setNextNode(nodeId); // nextNode is adjacent to current pos
					}
				}
			}
			
			// Check if there is an adjacent stenched nodes, if so, switch to tracking mod
			for (int i = 1; i<lobs.size(); i++) {
				ArrayList<Couple> content = (ArrayList) lobs.get(i).getRight();
				if (content.isEmpty() == false) {	// Add stenched nodes to a list
					if (content.get(content.size() - 1).getLeft().toString().equals("Stench")) {
						if(!this.myAgent.getNodesToAvoid().contains(lobs.get(i).getLeft())) {
							this.myAgent.resetShortestPath();
							this.myAgent.switchState();
							this.exitValue = 2;
							System.out.println(this.myAgent.getLocalName() + " ----> My nodes to avoid : "+ this.myAgent.getNodesToAvoid());
							System.out.println(this.myAgent.getLocalName() + " --> Switch to tracking mode without finishing exploration");
							return;
						}
					}						
				}					
			}
			


			// While openNodes is not empty, continues.
			if (this.myAgent.getMap().hasOpenNode()){
				
	            if(!this.myAgent.getShortestPath().isEmpty()) {
//	                System.out.println(this.myAgent.getLocalName() + " ----> Already have a path which is : "+this.myAgent.getShortestPath());
	                
	                this.myAgent.setNextNode(this.myAgent.getShortestPath().get(0));
					this.myAgent.setOldNode(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
//					System.out.println(this.myAgent.getLocalName() + " --> Next Node : " + this.myAgent.getNextNode());
					((AbstractDedaleAgent)this.myAgent).moveTo(this.myAgent.getNextNode());
					this.myAgent.setNewNode(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
					if(this.myAgent.didIMove()) {
						this.myAgent.getShortestPath().remove(0);
					}
					this.myAgent.resetDetectedAgents();		// Reset detected agents list
					this.myAgent.resetNodesToAvoid();
					this.exitValue = 1;			// Ping nearby agents
					return;
	            }else {
					// Select next move.
					// To avoid agents following each other after sharing their map,
					// they chose a different nextNode (smallest id choose first)
					if (this.myAgent.getNextNode()==null){
						ArrayList<ArrayList<String>> listDetectedAgents = this.myAgent.getDetectedAgents();
						this.listIdAgent = new ArrayList<String>();
						for(int i = 0; i < listDetectedAgents.size(); i++) {
							this.listIdAgent.add(listDetectedAgents.get(i).get(0)); // add id of each detected agents
						}
						Collections.sort(listIdAgent);
						for(int i=0; i < listIdAgent.size(); i++) {
							if (listIdAgent.get(i) == this.myAgent.getIdAgent()) {
	                            this.myAgent.setShortestPath(this.myAgent.getMap().getShortestPathToClosestOpenNode(myPosition,i));
	                            System.out.println(this.myAgent.getLocalName() + " ----> Computed path : "+ this.myAgent.getShortestPath());
	                            this.myAgent.setNextNode(this.myAgent.getShortestPath().get(0));
								break;
							}
						}
					}else {
						//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myAgent.getMap().getOpenNodes()+"\n -- nextNode: "+nextNode);
					}
					
					
					this.myAgent.setOldNode(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
//					System.out.println(this.myAgent.getLocalName() + " --> Next Node : " + this.myAgent.getNextNode());
					((AbstractDedaleAgent)this.myAgent).moveTo(this.myAgent.getNextNode());
					this.myAgent.setNewNode(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
					if(this.myAgent.didIMove() && (!this.myAgent.getShortestPath().isEmpty())) {
						this.myAgent.getShortestPath().remove(0);
					}
					this.myAgent.resetDetectedAgents();		// Reset detected agents list
					this.myAgent.resetNodesToAvoid();
					this.exitValue = 1;			// Ping nearby agents
					return;
	            }
			}else { // Exploration finished
				System.out.println(this.myAgent.getLocalName() + " --> Exploration successufully done, behaviour removed.");
				this.myAgent.switchState();
				this.myAgent.exploDone();
				this.exitValue = 2;		// Switch to tracking mod
				return;
			}
		}
	}

	@Override
	public int onEnd() {
		return this.exitValue;
	}	
}
