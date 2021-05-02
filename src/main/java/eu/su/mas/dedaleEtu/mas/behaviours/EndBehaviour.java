package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import jade.core.behaviours.OneShotBehaviour;

public class EndBehaviour extends OneShotBehaviour{
	private static final long serialVersionUID = -2058134622078521998L;
	private ExploreCoopAgent myAgent;

	public EndBehaviour (final ExploreCoopAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
	}
	
	@Override
	public void action() {
		System.out.println(this.myAgent.getLocalName()+" ---> J'ai termine");
	}
}
