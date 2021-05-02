package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.OneShotBehaviour;


public class InitMapBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = -2058134622078521998L;
	private ExploreCoopAgent myAgent;

	public InitMapBehaviour (final ExploreCoopAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
		System.out.println(this.myAgent.getLocalName() + " --> Init Map behaviour created");
	}
	
	@Override
	public void action() {
		MapRepresentation myMap= new MapRepresentation();
		
		this.myAgent.updateMap(myMap);
		
		System.out.println(this.myAgent.getLocalName()+" ---> Map Created");
	}
}