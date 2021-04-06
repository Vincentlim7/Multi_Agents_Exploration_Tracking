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
	private Agent myagent;
	
	/**
	 * 
	 * This behaviour is a one Shot.
	 * It receives a message tagged with an inform performative, print the content in the console and destroy itlself
	 * @param myagent
	 */
	public ReceiveMessageBehaviour(final Agent myagent) {
		super(myagent);
		this.myagent = myagent;

	}


	public void action() {
		//1) receive the message
		final MessageTemplate pingMsgTemplate = MessageTemplate.and(MessageTemplate.MatchProtocol("PingNeighborsProtocol"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		final MessageTemplate mapMsgTemplate = MessageTemplate.and(MessageTemplate.MatchProtocol("ShareMapProtocol"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		final MessageTemplate confimMsgTemplate = MessageTemplate.and(MessageTemplate.MatchProtocol("ConfirmProtocol"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		final ACLMessage pingMsg = this.myAgent.receive(pingMsgTemplate);
		final ACLMessage mapMsg = this.myAgent.receive(mapMsgTemplate);
		final ACLMessage confirmMsg = this.myAgent.receive(confimMsgTemplate);
		
			
		if (pingMsg !=null || mapMsg != null ||confirmMsg != null) {
			try {
				if (pingMsg != null) {
					String idSender = pingMsg.getContent();
					((ExploreCoopAgent)this.myAgent).addDetectedAgents(Integer.valueOf(idSender));
					System.out.println("Ping recu");
					
				}
				if(mapMsg != null) {
					SerializableSimpleGraph o = (SerializableSimpleGraph) mapMsg.getContentObject();
					((ExploreCoopAgent) this.myAgent).getMap().mergeMap(o);
//					System.out.println(this.myAgent.getLocalName()+"<----Result received from "+msg.getSender().getLocalName()+" ,content= "+msg.getContentObject());
					
					ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
					msg.setSender(this.myAgent.getAID());
					msg.setProtocol("ConfirmProtocol");
					msg.setContent("Carte recu");
					msg.addReceiver(new AID(mapMsg.getSender().toString(),AID.ISLOCALNAME));
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
					System.out.println("Carte recu");
				}
				if(confirmMsg != null) {
					System.out.println("Message de confirmation");
				}
				
			} catch (UnreadableException e1) {
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

}

