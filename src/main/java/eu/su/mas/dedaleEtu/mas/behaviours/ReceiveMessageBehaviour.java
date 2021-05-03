package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class ReceiveMessageBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 9088209402507795289L;

	private boolean finished=false;
	private ExploreCoopAgent myAgent;
	
	public ReceiveMessageBehaviour(final ExploreCoopAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
	}


	public void action() {
		//1) receive the message
		final MessageTemplate pingMsgTemplate = MessageTemplate.and(MessageTemplate.MatchProtocol("PingNeighborsProtocol"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		final MessageTemplate mapMsgTemplate = MessageTemplate.and(MessageTemplate.MatchProtocol("ShareMapProtocol"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		final MessageTemplate confimMsgTemplate = MessageTemplate.and(MessageTemplate.MatchProtocol("ConfirmProtocol"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		final MessageTemplate destinationMsgTemplate = MessageTemplate.and(MessageTemplate.MatchProtocol("SendDestionationProtocol"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		
		final ACLMessage pingMsg = this.myAgent.receive(pingMsgTemplate);
		final ACLMessage mapMsg = this.myAgent.receive(mapMsgTemplate);
		final ACLMessage confirmMsg = this.myAgent.receive(confimMsgTemplate);
		final ACLMessage destinationMsg = this.myAgent.receive(destinationMsgTemplate);
		
		if (pingMsg !=null || mapMsg != null || confirmMsg != null || destinationMsg != null) {
			try {
				// Send a confirm message containing your informations (id, name, pos) to the agent who pinged you
				if (pingMsg != null) {
					ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
					msg.setSender(this.myAgent.getAID());
					msg.setProtocol("ConfirmProtocol");
					msg.setContentObject(this.myAgent.getInfosAgent()); 
					msg.addReceiver(new AID(pingMsg.getSender().getLocalName(),AID.ISLOCALNAME));
					this.myAgent.sendMessage(msg);
//					System.out.println(this.myAgent.getLocalName() + " --> Ping message received from " + pingMsg.getSender().getLocalName());
					
				}
				// Merge the received map
				if (mapMsg != null) {
					SerializableSimpleGraph o = (SerializableSimpleGraph) mapMsg.getContentObject();
					this.myAgent.getMap().mergeMap(o);
					this.myAgent.setShortestPath(new ArrayList<String>());
//					System.out.println(this.myAgent.getLocalName() + " --> Map message received from " + mapMsg.getSender().getLocalName());
				}
				// Add the agent who identified himself to the detected agent list
				if(confirmMsg != null) {
					ArrayList<String> infosAgent = (ArrayList<String>) confirmMsg.getContentObject();
					this.myAgent.addDetectedAgent(infosAgent);
//					System.out.println(this.myAgent.getLocalName() + " --> Confirm message received from " + confirmMsg.getSender().getLocalName());
					this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent));
				}
				// If the agent currently isn't detecting a stench or a wumpus, or doesn't already have a destination, accept the destination sent
				if(destinationMsg != null) {
					if(this.myAgent.getDestination() == null && this.myAgent.getWumpusFound() == false && this.myAgent.getStenchedNodes() == null) {
						String destination = destinationMsg.getContent();
						this.myAgent.setDestination(destination);
//						System.out.println(this.myAgent.getLocalName() + " --> Destination message received from " + destinationMsg.getSender().getLocalName());
					}
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}else{
			block();// the behaviour goes to sleep until the arrival of a new message in the agent's Inbox.
		}
	}

	public boolean done() {
		return finished;
	}
	
	public void finished() {
		this.finished = true;
		System.out.println("-----------------Receive BEHAVIOUR FINISHED-----------------");
	}

}

