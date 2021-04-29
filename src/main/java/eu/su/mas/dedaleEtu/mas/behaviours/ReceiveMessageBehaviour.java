package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * This behaviour is a one Shot.
 * It receives a message tagged with an inform performative, print the content in the console and destroy itlself
 * 
 * @author Cédric Herpson
 *
 */
public class ReceiveMessageBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 9088209402507795289L;

	private boolean finished=false;
	private ExploreCoopAgent myAgent;
	
	/**
	 * 
	 * This behaviour is a one Shot.
	 * It receives a message tagged with an inform performative, print the content in the console and destroy itlself
	 * @param myagent
	 */
	public ReceiveMessageBehaviour(final ExploreCoopAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
		System.out.println("-----------------RECEIVE BEHAVIOUR CREATED-----------------");
	}


	public void action() {
		//1) receive the message
		final MessageTemplate pingMsgTemplate = MessageTemplate.and(MessageTemplate.MatchProtocol("PingNeighborsProtocol"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		final MessageTemplate mapMsgTemplate = MessageTemplate.and(MessageTemplate.MatchProtocol("ShareMapProtocol"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		final MessageTemplate confimMsgTemplate = MessageTemplate.and(MessageTemplate.MatchProtocol("ConfirmProtocol"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		final MessageTemplate checkGolemMsgTemplate1 = MessageTemplate.and(MessageTemplate.MatchProtocol("CheckGolemProtocol1"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		final MessageTemplate checkGolemMsgTemplate2 = MessageTemplate.and(MessageTemplate.MatchProtocol("CheckGolemProtocol2"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		final ACLMessage pingMsg = this.myAgent.receive(pingMsgTemplate);
		final ACLMessage mapMsg = this.myAgent.receive(mapMsgTemplate);
		final ACLMessage confirmMsg = this.myAgent.receive(confimMsgTemplate);
		final ACLMessage checkGolemMsg1 = this.myAgent.receive(checkGolemMsgTemplate1);
		final ACLMessage checkGolemMsg2 = this.myAgent.receive(checkGolemMsgTemplate2);
		
//		System.out.println("------- Receive behaviour");
//		System.out.println("---------------------------list message" + pingMsg + mapMsg + confirmMsg + checkGolemMsg1 + checkGolemMsg2);
		if (pingMsg !=null || mapMsg != null ||confirmMsg != null || checkGolemMsg1 != null || checkGolemMsg2 != null) {
//			System.out.println("-------------------------Message recu");
			try {
				if (pingMsg != null) {
					ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
					msg.setSender(this.myAgent.getAID());
					msg.setProtocol("ConfirmProtocol");
					msg.setContentObject(this.myAgent.getInfosAgent()); 
					msg.addReceiver(new AID(pingMsg.getSender().getLocalName(),AID.ISLOCALNAME));
					this.myAgent.sendMessage(msg);
					System.out.println(this.myAgent.getLocalName() + " --> Ping message received from " + pingMsg.getSender().getLocalName());
					
				}
				if (mapMsg != null) {
					SerializableSimpleGraph o = (SerializableSimpleGraph) mapMsg.getContentObject();
					this.myAgent.getMap().mergeMap(o);
					System.out.println(this.myAgent.getLocalName() + " --> Map message received from " + mapMsg.getSender().getLocalName());
				}
				if(confirmMsg != null) {
					ArrayList<String> infosAgent = (ArrayList<String>) confirmMsg.getContentObject();
					this.myAgent.addDetectedAgent(infosAgent);
					System.out.println(this.myAgent.getLocalName() + " --> Confirm message received from " + confirmMsg.getSender().getLocalName());
					this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent));
				}
				
				// If ping received, send current position to the ping sender
				if(checkGolemMsg1 != null) {
					System.out.println("-------CheckGolemMSG Received");
					String myPosition = this.myAgent.getCurrentPosition();
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setSender(this.myAgent.getAID());
					msg.setProtocol("CheckGolemProtocol1");
					msg.setContent(myPosition);
					msg.addReceiver(new AID(checkGolemMsg1.getSender().getLocalName(),AID.ISLOCALNAME));
					this.myAgent.sendMessage(msg);
				}
				
				// Add the received position to a list
				if(checkGolemMsg2 != null) {
					String agentPos = checkGolemMsg2.getContent();
					System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
					this.myAgent.addAgentPos(agentPos);
					
				}
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
//			this.finished=true;

		}else{
			block();// the behaviour goes to sleep until the arrival of a new message in the agent's Inbox.
		}
	}

	public boolean done() {
		return finished;
	}
	
	public void finished() {
		finished = true;
		System.out.println("-----------------PING BEHAVIOUR FINISHED-----------------");
	}

}

