package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import jade.core.behaviours.OneShotBehaviour;


public class TrackGolemBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;
	private int exitValue;
	private Random r;
	private ExploreCoopAgent myAgent;
	/**
 * 
 * @param myagent
 */
	public TrackGolemBehaviour(final ExploreCoopAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
		this.r = new Random();
	}

	@Override
	public void action() {
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Wumpus blocked
		if(this.myAgent.getFinished()) {
			this.exitValue = 3;
			return;
		}
		
		this.exitValue = 0;
		this.myAgent.resetDetectedAgents();		// Reset detected agents list
		this.myAgent.resetStenchedNodes();
		
		// Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			System.out.println(this.myAgent.getLocalName() + " --> Track observations : " + lobs.toString());
			System.out.println(this.myAgent.getLocalName() + " --> nb try :" + this.myAgent.getNbTry());
							
			// If the agent hasn't explored 100% of the map, update open/closed nodes while tracking
			if(!this.myAgent.isExploFinished()) {
				// 1) remove the current node from openlist and add it to closedNodes.
				this.myAgent.getMap().addNode(myPosition, MapAttribute.closed);

				// 2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
				Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
				while(iter.hasNext()){
					String nodeId=iter.next().getLeft();
					boolean isNewNode=this.myAgent.getMap().addNewNode(nodeId);
					//the node may exist, but not necessarily the edge
					if (myPosition!=nodeId) {
						this.myAgent.getMap().addEdge(myPosition, nodeId);
					}
				}
			}
			if(this.myAgent.getDestination() == null) {
				// If golem not found, compute a nextNode
				// Else, golem was found because the agent was not able to move at the previous iteration, Try moving to the same node
				if(this.myAgent.getWumpusFound() == false) {
					this.myAgent.setNextNode(null);
					
					// Add adjacent stenched nodes to the list
					for (int i = 1; i<lobs.size(); i++) {
						ArrayList<Couple> content = (ArrayList) lobs.get(i).getRight();
						if (content.isEmpty() == false) {	// Add stenched nodes to a list
							if (content.get(content.size() - 1).getLeft().toString().equals("Stench")) {
								if(!this.myAgent.getNodesToAvoid().contains(lobs.get(i).getLeft())) // Check if we don't already know there is an agent there
									this.myAgent.addStenchedNodes((String) lobs.get(i).getLeft());
							}						
						}					
					}
					
					// If there are adjacent stenched nodes, move to one of them (randomly choosen)
					// Else if exploration not finshed, go back to explorating mode, Else move to a random adjacent node
					if (this.myAgent.getStenchedNodes().isEmpty() == false) {
						this.myAgent.setNextNode(this.myAgent.getStenchedNodes().get(this.r.nextInt(this.myAgent.getStenchedNodes().size())));
					} else {
						if(!this.myAgent.isExploFinished()) { // Go back to explorating mode
							System.out.println(this.myAgent.getLocalName() + " --> Stench lost, going back to explorating mode");
							this.myAgent.switchState();
							this.exitValue = 2;
							return;
						}
						this.myAgent.setNextNode(lobs.get(this.r.nextInt(lobs.size()-1)+1).getLeft()); // Move to a random adjacent node
					}
				} else { // Wumpus found at previous ite, try moving there again
					this.myAgent.setWumpusFound(false); // No need to update nextNode as its value was not modified since the last iteration
				}
			}else { // I received a location containing either a stench or a wumpus, heading there
				System.out.println(this.myAgent.getLocalName() + " --> I have a destination : " + this.myAgent.getDestination());
				this.myAgent.setNextNode(this.myAgent.getMap().getShortestPath(myPosition, this.myAgent.getDestination()).get(0));
			}
			System.out.println(this.myAgent.getLocalName() + " --> next Node : " + this.myAgent.getNextNode());
			this.myAgent.setOldNode(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
			((AbstractDedaleAgent)this.myAgent).moveTo(this.myAgent.getNextNode());
			this.myAgent.setNewNode(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
			
			if(!this.myAgent.didIMove()){ // If I couldn't move, erase the destination
				if(this.myAgent.getDestination() != null) {
					this.myAgent.setDestination(null);
				}
			}
			else { // I managed to move
				this.myAgent.resetContactedAgents();
				this.myAgent.resetNodesToAvoid();
				this.myAgent.resetNbTry();
			}
			this.exitValue = 1; // Ping nearby agents
			return;
		}
	}

	@Override
	public int onEnd() {
		return this.exitValue;
	}
	
}
