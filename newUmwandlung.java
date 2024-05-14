//Ausführung zum Beispiel:
//javac -cp .;lib\* newUmwandlung.java
//java -cp .;lib\* newUmwandlung jsonFiles/doc-5_1.json
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.GatewayDirection;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Participant;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.di.Waypoint;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.ParallelGateway;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.Lane;
import org.camunda.bpm.model.bpmn.instance.LaneSet;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.DataObject;
import org.camunda.bpm.model.bpmn.instance.DataObjectReference;
import org.camunda.bpm.model.bpmn.instance.DataAssociation;
import org.camunda.bpm.model.bpmn.instance.DataOutputAssociation;
import org.camunda.bpm.model.bpmn.instance.DataInputAssociation;
import org.camunda.bpm.model.bpmn.instance.Expression;
import org.camunda.bpm.model.bpmn.instance.ConditionExpression;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.*;


import edu.stanford.nlp.simple.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Map;
import java.util.Collection;
import java.util.Iterator;





class MyActor {
    private String name;

    // Konstruktor
    public MyActor(String name) {
        this.name = name;
    }

    // Getter-Methode
    public String getName() {
        return name;
    }
}



class MyActivity extends MyFlowNode {
    private String label;
    private MyActor performer;
    private MyActor recipient;
    private List<MyDataObject> usedDataObjects;
    private String furtherSpecification;
    private List<MyActor> furtherPerformers;

    // Konstruktor
    public MyActivity(Integer sentenceID, Integer tokenID, String label, MyActor performer) {
        super(sentenceID, tokenID);
        this.label = label;
        this.performer = performer;
        this.usedDataObjects = new ArrayList<>();
        this.furtherSpecification = null;
        this.furtherPerformers = new ArrayList<>();
    }

    // Konstruktor
    public MyActivity(Integer sentenceID, Integer tokenID, String label) {
        super(sentenceID, tokenID);
        this.label = label;
        this.usedDataObjects = new ArrayList<>();
        this.furtherSpecification = null;
        this.furtherPerformers = new ArrayList<>();
    }

    // Getter-Methoden
    public Integer getSentenceID() {
        return sentenceID;
    }

    public Integer getTokenID() {
        return tokenID;
    }

    public String getLabel() {
        return label;
    }

    public MyActor getPerformer() {
        return performer;
    }

    public List<MyDataObject> getUsedDataObjects() {
        return usedDataObjects;
    }

    public List<MyActor> getFurtherPerformers() {
        return furtherPerformers;
    }

    public MyActor getRecipient() {
        return recipient;
    }

    public String getFurtherSpecification() {
        return furtherSpecification;
    }

    public void setRecipient(MyActor receiver) {
        recipient = receiver;
    }

    public void setFurtherSpecification(String specification) {
        furtherSpecification = specification;
    }

    public void addUsedDataObject(MyDataObject dataObject) {
        usedDataObjects.add(dataObject);
    }

    public void addFurtherPerformers(MyActor actor) {
        furtherPerformers.add(actor);
    }
}



class MyDataObject {
    private String label;

    // Konstruktor
    public MyDataObject(String label) {
        this.label = label;
    }

    // Getter-Methode
    public String getLabel() {
        return label;
    }
}


// Klasse als Hilfe für Sonderfall Condition Specification ohne Folgeaktivität/-gateway
class MyEndevent extends MyFlowNode {
    // Konstruktor
    public MyEndevent(Integer sentenceID, Integer tokenID) {
        super(sentenceID, tokenID);
    }

    // Getter-Methoden
    public Integer getSentenceID() {
        return sentenceID;
    }

    public Integer getTokenID() {
        return tokenID;
    }
}


class MyGateway extends MyFlowNode {
    private String type;
    private List<String> mentions;

    // Konstruktor
    public MyGateway(String type, Integer sentenceID, Integer tokenID) {
        super(sentenceID, tokenID);
        this.type = type;
        this.mentions = new ArrayList<>();
    }

    // Getter-Methoden
    public String getType() {
        return type;
    }

    public Integer getSentenceID() {
        return sentenceID;
    }

    public Integer getTokenID() {
        return tokenID;
    }

    public List<String> getMentions() {
        return mentions;
    }

    public void addMention(String mention) {
        mentions.add(mention);
    }
}



abstract class MyFlowNode {
    protected Integer sentenceID;
    protected Integer tokenID;

    // Konstruktor
    public MyFlowNode(Integer sentenceID, Integer tokenID) {
        this.sentenceID = sentenceID;
        this.tokenID = tokenID;
    }

    // Getter-Methoden
    public Integer getSentenceID() {
        return sentenceID;
    }

    public Integer getTokenID() {
        return tokenID;
    }
}



class MyFlow {
    private MyFlowNode head;
    private MyFlowNode tail;
    private String condition;

    // Konstruktor
    public MyFlow(MyFlowNode head, MyFlowNode tail) {
        this.head = head;
        this.tail = tail;
    }

    // Konstruktor
    public MyFlow(MyFlowNode head, MyFlowNode tail, String condition) {
        this.head = head;
        this.tail = tail;
        this.condition = condition;
    }

    // Getter-Methoden
    public MyFlowNode getHead() {
        return head;
    }

    public MyFlowNode getTail() {
        return tail;
    }

    public String getCondition() {
        return condition;
    }
}






public class newUmwandlung {

    // Liste für alle schon erstellten Actor Objekte
    private static List<MyActor> actors = new ArrayList<>();
    private static List<MyDataObject> dataObjects = new ArrayList<>();
    private static List<MyActivity> activities = new ArrayList<>();
    private static List<MyGateway> gateways = new ArrayList<>();
    private static List<MyFlow> flows = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Bitte geben Sie den Dateipfad zur JSON-Datei als Argument ein.");
            return;
        }

        String filePath = args[0];
        String fileNameJson = new File(filePath).getName();
        String fileName = fileNameJson.replaceFirst("[.][^.]+$", "");

        System.out.println(filePath);
        System.out.println(fileName);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File jsonFile = new File(filePath);
            JsonNode rootNode = objectMapper.readTree(jsonFile);
            JsonNode docNode = rootNode.get(fileName);
            JsonNode relationsNode = docNode.get(0).get("relations");
            JsonNode sentenceIDsNode = docNode.get(0).get("sentence-IDs");
            JsonNode ner_tagsNode = docNode.get(0).get("ner_tags");
            JsonNode tokenIDsNode = docNode.get(0).get("token-IDs");
            JsonNode tokensNode = docNode.get(0).get("tokens");

            // Actor einfügen + Aktivität über actor performer
            for (JsonNode relationNode : relationsNode) {

                // Bei Relation ist das Element an der Stelle 2 der Typ: (0, 2, 'actor performer', 0, 0)
                String relationType = relationNode.get(2).asText();

                String actorLabel = null;

                if ("actor performer".equals(relationType)) {
                    
                    // An Stelle 3 ist sentenceID und 4 die TokenID des Actors: (0, 2, 'actor performer', 0, 0)
                    int actorSentenceID = relationNode.get(3).asInt();
                    int actorTokenID = relationNode.get(4).asInt();

                    Iterator<JsonNode> sentenceIDIterator = sentenceIDsNode.iterator();
                    Iterator<JsonNode> tokenIDIterator = tokenIDsNode.iterator();

                    int position = 0;

                    // Um die Position des Actors in der Token und der ner_tag Liste herauszufinden
                    while (sentenceIDIterator.hasNext() && tokenIDIterator.hasNext()) {
                        int currentSentenceID = sentenceIDIterator.next().asInt();
                        int currentTokenID = tokenIDIterator.next().asInt();

                        if (currentSentenceID == actorSentenceID && currentTokenID == actorTokenID) {

                            // Den Namen des Actors Objekts korrekt erstellen 
                            actorLabel = actorLabeling(ner_tagsNode, tokensNode, position);

                            // Prüfen, ob das Actor Objekt schon erstellt wurde
                            MyActor existingActor = getActorByName(actorLabel);

                            if (existingActor == null) {

                                // Objekt erstellen
                                MyActor actor = new MyActor(actorLabel); 
                                actors.add(actor);
                            }

                            break;
                        }
                        position++;
                    }

                    // Aktivität einfügen
                    int activitySentenceID = relationNode.get(0).asInt();
                    int activityTokenID = relationNode.get(1).asInt();

                    sentenceIDIterator = sentenceIDsNode.iterator();
                    tokenIDIterator = tokenIDsNode.iterator();

                    position = 0;

                    while (sentenceIDIterator.hasNext() && tokenIDIterator.hasNext()) {
                        int currentSentenceID = sentenceIDIterator.next().asInt();
                        int currentTokenID = tokenIDIterator.next().asInt();

                        if (currentSentenceID == activitySentenceID && currentTokenID == activityTokenID) {
                            String activityLabel = activityLabeling(ner_tagsNode, tokensNode, position);
                            MyActor performer = getActorByName(actorLabel);

                            if (getActivityBySIDANDTID(activitySentenceID, activityTokenID) == null) {
                                MyActivity activity = new MyActivity(activitySentenceID, activityTokenID, activityLabel, performer);
                                activities.add(activity);
                                break;
                            }
                            if (getActivityBySIDANDTID(activitySentenceID, activityTokenID) != null) {
                                MyActivity activity = getActivityBySIDANDTID(activitySentenceID, activityTokenID);
                                activity.addFurtherPerformers(performer);
                            }
                            break;
                        }
                        position++;
                    }
                }
            }

            // restliche Aktivitäten einfügen, die nicht in Actor Performer sind -> dafür alle Flow Relationen überprüfen
            for (JsonNode relationNode : relationsNode) {
                String relationType = relationNode.get(2).asText();

                if ("flow".equals(relationType)) {
                    Integer flowOutSentenceID = relationNode.get(0).asInt();
                    Integer flowOutTokenID = relationNode.get(1).asInt();

                    Integer flowInSentenceID = relationNode.get(3).asInt();
                    Integer flowInTokenID = relationNode.get(4).asInt();

                    Iterator<JsonNode> sentenceIDIterator = sentenceIDsNode.iterator();
                    Iterator<JsonNode> tokenIDIterator = tokenIDsNode.iterator();
                    Iterator<JsonNode> ner_tagsIterator = ner_tagsNode.iterator();

                    int position = 0;

                    while (sentenceIDIterator.hasNext() && tokenIDIterator.hasNext() && ner_tagsIterator.hasNext()) {
                        int currentSentenceID = sentenceIDIterator.next().asInt();
                        int currentTokenID = tokenIDIterator.next().asInt();

                        if (currentSentenceID == flowInSentenceID && currentTokenID == flowInTokenID) {
                            String nerTag = ner_tagsNode.get(position).asText();

                            if (nerTag.equals("B-Activity")) {

                                Iterator<MyActivity> activityIterator = activities.iterator();
                                boolean found = false;

                                while (activityIterator.hasNext()) {
                                    MyActivity activity = activityIterator.next();
                                    if (activity.getSentenceID() == flowInSentenceID && activity.getTokenID() == flowInTokenID) {
                                        found = true;
                                        break;
                                    }
                                }
            
                                if (!found) {
                                    String activityLabel = activityLabeling(ner_tagsNode, tokensNode, position);
                                    MyActivity newActivity = new MyActivity(flowInSentenceID, flowInTokenID, activityLabel);
                                    activities.add(newActivity);
                                }
                            }
                        }

                        if (currentSentenceID == flowOutSentenceID && currentTokenID == flowOutTokenID) {
                            String nerTag = ner_tagsNode.get(position).asText();

                            if (nerTag.equals("B-Activity")) {

                                Iterator<MyActivity> activityIterator = activities.iterator();
                                boolean found = false;

                                while (activityIterator.hasNext()) {
                                    MyActivity activity = activityIterator.next();
                                    if (activity.getSentenceID() == flowOutSentenceID && activity.getTokenID() == flowOutTokenID) {
                                        found = true;
                                        break;
                                    }
                                }
            
                                if (!found) {
                                    String activityLabel = activityLabeling(ner_tagsNode, tokensNode, position);
                                    MyActivity newActivity = new MyActivity(flowOutSentenceID, flowOutTokenID, activityLabel);
                                    activities.add(newActivity);
                                }
                            }
                        }
                        position++;
                    }
                }
            }



            // actor recipient einfügen
            for (JsonNode relationNode : relationsNode) {
                String relationType = relationNode.get(2).asText();

                String actorLabel = null;

                if ("actor recipient".equals(relationType)) {
                    int activitySentenceID = relationNode.get(0).asInt();
                    int activityTokenID = relationNode.get(1).asInt();

                    int actorSentenceID = relationNode.get(3).asInt();
                    int actorTokenID = relationNode.get(4).asInt();

                    Iterator<JsonNode> sentenceIDIterator = sentenceIDsNode.iterator();
                    Iterator<JsonNode> tokenIDIterator = tokenIDsNode.iterator();

                    int position = 0;
                    while (sentenceIDIterator.hasNext() && tokenIDIterator.hasNext()) {
                        int currentSentenceID = sentenceIDIterator.next().asInt();
                        int currentTokenID = tokenIDIterator.next().asInt();

                        if (currentSentenceID == actorSentenceID && currentTokenID == actorTokenID) {
                            actorLabel = actorLabeling(ner_tagsNode, tokensNode, position);

                            MyActor existingActor = getActorByName(actorLabel);

                            if (existingActor == null) {
                                MyActor actor = new MyActor(actorLabel); 
                                actors.add(actor);
                            }

                            break;
                        }
                        position++;
                    }
                    MyActivity activity = getActivityBySIDANDTID(activitySentenceID, activityTokenID);
                    MyActor recipient = getActorByName(actorLabel);

                    if (activity != null && recipient != null) {
                        activity.setRecipient(recipient);
                    }
                }
            }



            // Data Object über uses Relation
            for (JsonNode relationNode : relationsNode) {
                String relationType = relationNode.get(2).asText();

                if ("uses".equals(relationType)) {
                    int dataObjectSentenceID = relationNode.get(3).asInt();
                    int dataObjectTokenID = relationNode.get(4).asInt();

                    Iterator<JsonNode> sentenceIDIterator = sentenceIDsNode.iterator();
                    Iterator<JsonNode> tokenIDIterator = tokenIDsNode.iterator();

                    int position = 0;

                    while (sentenceIDIterator.hasNext() && tokenIDIterator.hasNext()) {
                        int currentSentenceID = sentenceIDIterator.next().asInt();
                        int currentTokenID = tokenIDIterator.next().asInt();

                        if (currentSentenceID == dataObjectSentenceID && currentTokenID == dataObjectTokenID) {
                            String dataObjectLabel = dataObjectLabeling(ner_tagsNode, tokensNode, position);

                            int activitySentenceID = relationNode.get(0).asInt();
                            int activityTokenID = relationNode.get(1).asInt();
                            MyActivity activity = getActivityBySIDANDTID(activitySentenceID, activityTokenID);

                            MyDataObject existingDataObject = getDataObjectByName(dataObjectLabel);

                            if (existingDataObject == null) {

                                // Objekt erstellen
                                MyDataObject dataObject = new MyDataObject(dataObjectLabel); 
                                dataObjects.add(dataObject);                                

                                activity.addUsedDataObject(dataObject);

                                break;
                            }
                            activity.addUsedDataObject(existingDataObject);

                            break;
                        }
                        position++; 
                    }
                }
            }


            // Gateways
            for (JsonNode relationNode : relationsNode) {
                String relationType = relationNode.get(2).asText();

                if ("flow".equals(relationType)) {
                    Integer flowOutSentenceID = relationNode.get(0).asInt();
                    Integer flowOutTokenID = relationNode.get(1).asInt();

                    Integer flowInSentenceID = relationNode.get(3).asInt();
                    Integer flowInTokenID = relationNode.get(4).asInt();

                    // FlowOut Gateways -> Also Relationen, bei denen das Gateway der head ist
                    Iterator<JsonNode> sentenceIDIterator = sentenceIDsNode.iterator();
                    Iterator<JsonNode> tokenIDIterator = tokenIDsNode.iterator();

                    int position = 0;

                    while (sentenceIDIterator.hasNext() && tokenIDIterator.hasNext()) {
                        int currentSentenceID = sentenceIDIterator.next().asInt();
                        int currentTokenID = tokenIDIterator.next().asInt();

                        if (currentSentenceID == flowOutSentenceID && currentTokenID == flowOutTokenID) {
                            String nerTag = ner_tagsNode.get(position).asText();

                            if (nerTag.equals("B-XOR Gateway")) {
                                String type = "xor";

                                // FlowOut(head) XOR Gateways
                                MyGateway existingGateway = getGatewayByID(flowOutSentenceID, flowOutTokenID);

                                if (existingGateway != null) {
                                    break;
                                }

                                boolean sameGatewayExists = sameGatewayExist(relationsNode, flowOutSentenceID, flowOutTokenID);

                                if (sameGatewayExists == false) {
                                    MyGateway gateway = new MyGateway(type, flowOutSentenceID, flowOutTokenID);
                                    gateway.addMention(flowOutSentenceID + "-" + flowOutTokenID);
                                    gateways.add(gateway);
                                    break;
                                }

                                MyGateway sameGateway = getSameGateway(relationsNode, flowOutSentenceID, flowOutTokenID);

                                if (sameGateway == null) {
                                    MyGateway gateway = new MyGateway(type, flowOutSentenceID, flowOutTokenID);
                                    gateway.addMention(flowOutSentenceID + "-" + flowOutTokenID);
                                    gateways.add(gateway);
                                    break;
                                }

                                sameGateway.addMention(flowOutSentenceID + "-" + flowOutTokenID);
                                break;
                            }

                            if (nerTag.equals("B-AND Gateway")) {
                                String type = "and";

                                // FlowOut(head) AND Gateways
                                MyGateway existingGateway = getGatewayByID(flowOutSentenceID, flowOutTokenID);

                                if (existingGateway != null) {
                                    break;
                                }

                                boolean sameGatewayExists = sameGatewayExist(relationsNode, flowOutSentenceID, flowOutTokenID);

                                if (sameGatewayExists == false) {
                                    MyGateway gateway = new MyGateway(type, flowOutSentenceID, flowOutTokenID);
                                    gateway.addMention(flowOutSentenceID + "-" + flowOutTokenID);
                                    gateways.add(gateway);
                                    break;
                                }

                                MyGateway sameGateway = getSameGateway(relationsNode, flowOutSentenceID, flowOutTokenID);

                                if (sameGateway == null) {
                                    MyGateway gateway = new MyGateway(type, flowOutSentenceID, flowOutTokenID);
                                    gateway.addMention(flowOutSentenceID + "-" + flowOutTokenID);
                                    gateways.add(gateway);
                                    break;
                                }

                                sameGateway.addMention(flowOutSentenceID +  "-" + flowOutTokenID);
                                break;
                            }
                            break;
                        }
                        position++; 
                    }


                    sentenceIDIterator = sentenceIDsNode.iterator();
                    tokenIDIterator = tokenIDsNode.iterator();

                    position = 0;

                    // FlowIn Gateways -> Relationen mit Gateway als tail 
                    // -> nur erstellt, falls es noch nicht existiert
                    while (sentenceIDIterator.hasNext() && tokenIDIterator.hasNext()) {
                        int currentSentenceID = sentenceIDIterator.next().asInt();
                        int currentTokenID = tokenIDIterator.next().asInt();

                        if (currentSentenceID == flowInSentenceID && currentTokenID == flowInTokenID) {
                            String nerTag = ner_tagsNode.get(position).asText();

                            if (nerTag.equals("B-XOR Gateway")) {
                                String type = "xor";

                                // FlowIn(tail) XOR Gateways
                                MyGateway existingGateway = getGatewayByID(flowInSentenceID, flowInTokenID);

                                if (existingGateway != null) {
                                    break;
                                }

                                boolean sameGatewayExists = sameGatewayExist(relationsNode, flowInSentenceID, flowInTokenID);

                                if (sameGatewayExists == false) {
                                    MyGateway gateway = new MyGateway(type, flowInSentenceID, flowInTokenID);
                                    gateway.addMention(flowInSentenceID + "-" + flowInTokenID);
                                    gateways.add(gateway);
                                    break;
                                }

                                MyGateway sameGateway = getSameGateway(relationsNode, flowInSentenceID, flowInTokenID);

                                if (sameGateway == null) {
                                    MyGateway gateway = new MyGateway(type, flowInSentenceID, flowInTokenID);
                                    gateway.addMention(flowInSentenceID + "-" + flowInTokenID);
                                    gateways.add(gateway);
                                    break;
                                }

                                sameGateway.addMention(flowInSentenceID + "-" + flowInTokenID);
                                break;

                            }

                            if (nerTag.equals("B-AND Gateway")) {
                                String type = "and";

                                // FlowIn(tail) AND Gateways
                                MyGateway existingGateway = getGatewayByID(flowInSentenceID, flowInTokenID);

                                if (existingGateway != null) {
                                    break;
                                }

                                boolean sameGatewayExists = sameGatewayExist(relationsNode, flowInSentenceID, flowInTokenID);

                                if (sameGatewayExists == false) {
                                    MyGateway gateway = new MyGateway(type, flowInSentenceID, flowInTokenID);
                                    gateway.addMention(flowInSentenceID + "-" + flowInTokenID);
                                    gateways.add(gateway);
                                    break;
                                }

                                MyGateway sameGateway = getSameGateway(relationsNode, flowInSentenceID, flowInTokenID);

                                if (sameGateway == null) {
                                    MyGateway gateway = new MyGateway(type, flowInSentenceID, flowInTokenID);
                                    gateway.addMention(flowInSentenceID + "-" + flowInTokenID);
                                    gateways.add(gateway);
                                    break;
                                }

                                sameGateway.addMention(flowInSentenceID + "-" + flowInTokenID);
                                break;
                            }
                            break;
                        }
                        position++; 
                    }
                }                 
            }


            //Flows
            for (JsonNode relationNode : relationsNode) {
                String relationType = relationNode.get(2).asText();

                if ("flow".equals(relationType)) {
                    Integer flowOutSentenceID = relationNode.get(0).asInt();
                    Integer flowOutTokenID = relationNode.get(1).asInt();

                    Integer flowInSentenceID = relationNode.get(3).asInt();
                    Integer flowInTokenID = relationNode.get(4).asInt();

                    Iterator<JsonNode> sentenceIDIterator = sentenceIDsNode.iterator();
                    Iterator<JsonNode> tokenIDIterator = tokenIDsNode.iterator();

                    int position = 0;

                    MyActivity existingOutActivity = getActivityBySIDANDTID(flowOutSentenceID, flowOutTokenID);
                    MyGateway existingOutGateway = getGatewayByID(flowOutSentenceID, flowOutTokenID);

                    MyActivity existingInActivity = getActivityBySIDANDTID(flowInSentenceID, flowInTokenID);
                    MyGateway existingInGateway = getGatewayByID(flowInSentenceID, flowInTokenID);

                    // Fälle für flow-Vorkommen
                    if (existingOutActivity != null && existingInActivity != null) {
                        MyFlow flow = new MyFlow(existingOutActivity, existingInActivity);
                        flows.add(flow);
                    }

                    if (existingOutActivity != null && existingInGateway != null) {
                        MyFlow flow = new MyFlow(existingOutActivity, existingInGateway);
                        flows.add(flow);
                    }
                    
                    if (existingOutGateway != null && existingInActivity != null) {
                        MyFlow flow = new MyFlow(existingOutGateway, existingInActivity);
                        flows.add(flow);
                    }

                    if (existingOutGateway != null && existingInGateway != null) {
                        MyFlow flow = new MyFlow(existingOutGateway, existingInGateway);
                        flows.add(flow);
                    }
                }
            }



            // Condition Specification einfügen
            for (JsonNode relationNode : relationsNode) {
                String relationType = relationNode.get(2).asText();

                if ("flow".equals(relationType)) {
                    Integer flowOutSentenceID = relationNode.get(0).asInt();
                    Integer flowOutTokenID = relationNode.get(1).asInt();

                    Integer flowInSentenceID = relationNode.get(3).asInt();
                    Integer flowInTokenID = relationNode.get(4).asInt();

                    Iterator<JsonNode> sentenceIDIterator = sentenceIDsNode.iterator();
                    Iterator<JsonNode> tokenIDIterator = tokenIDsNode.iterator();

                    int position = 0;

                    while (sentenceIDIterator.hasNext() && tokenIDIterator.hasNext()) {
                        int currentSentenceID = sentenceIDIterator.next().asInt();
                        int currentTokenID = tokenIDIterator.next().asInt();

                        if (currentSentenceID == flowInSentenceID && currentTokenID == flowInTokenID) {
                            String nerTag = ner_tagsNode.get(position).asText();

                            if (nerTag.equals("B-Condition Specification")) {
                                String conditionLabel = conditionLabeling(ner_tagsNode, tokensNode, position);

                                int index = 0;
                                int maxIterations = relationsNode.size();

                                for (JsonNode relationNodeCond : relationsNode) {
                                    String relationTypeCond = relationNodeCond.get(2).asText();

                                    // In relationen ist flow->cond.Spec. und cond.Spec.->flow
                                    // -> daraus einen Flow machen
                                    if ("flow".equals(relationType)) {
                                        Integer flowOutSentenceIDCond = relationNodeCond.get(0).asInt();
                                        Integer flowOutTokenIDCond = relationNodeCond.get(1).asInt();

                                        if (flowInSentenceID == flowOutSentenceIDCond && flowInTokenID == flowOutTokenIDCond) {
                                            Integer flowInSentenceIDCond = relationNodeCond.get(3).asInt();
                                            Integer flowInTokenIDCond = relationNodeCond.get(4).asInt();

                                            MyActivity existingOutActivity = getActivityBySIDANDTID(flowOutSentenceID, flowOutTokenID);
                                            MyGateway existingOutGateway = getGatewayByID(flowOutSentenceID, flowOutTokenID);

                                            MyActivity existingInActivity = getActivityBySIDANDTID(flowInSentenceIDCond, flowInTokenIDCond);
                                            MyGateway existingInGateway = getGatewayByID(flowInSentenceIDCond, flowInTokenIDCond);

                                            if (existingOutActivity != null && existingInActivity != null) {
                                                MyFlow flow = new MyFlow(existingOutActivity, existingInActivity, conditionLabel);
                                                flows.add(flow);
                                                break;
                                            }

                                            if (existingOutActivity != null && existingInGateway != null) {
                                                MyFlow flow = new MyFlow(existingOutActivity, existingInGateway, conditionLabel);
                                                flows.add(flow);
                                                break;
                                            }
                                            
                                            if (existingOutGateway != null && existingInActivity != null) {
                                                MyFlow flow = new MyFlow(existingOutGateway, existingInActivity, conditionLabel);
                                                flows.add(flow);
                                                break;
                                            }

                                            if (existingOutGateway != null && existingInGateway != null) {
                                                MyFlow flow = new MyFlow(existingOutGateway, existingInGateway, conditionLabel);
                                                flows.add(flow);
                                                break;
                                            }
                                        }
                                    }

                                    if (index == maxIterations - 1) {
                                        // Condition Specification hat keinen augehenden Flow -> Endevent stattdessen(einzigartige Endevent ID finden)
                                        int maxTokens = tokensNode.size();
                                        MyEndevent endevent = new MyEndevent(maxTokens + flowInSentenceID + 1, maxTokens + flowInTokenID + 1);

                                        MyGateway existingOuGateway = getGatewayByID(flowOutSentenceID, flowOutTokenID);

                                        MyFlow flow = new MyFlow(existingOuGateway, endevent, conditionLabel);
                                        flows.add(flow);
                                    }

                                    index++;
                                }                 
                            }
                        }
                        position++;
                    }
                }
            }


            // Further Specification
            for (JsonNode relationNode : relationsNode) {
                String relationType = relationNode.get(2).asText();

                String furtherSpecification = null;

                if ("further specification".equals(relationType)) {
                    int activitySentenceID = relationNode.get(0).asInt();
                    int activityTokenID = relationNode.get(1).asInt();

                    int specSentenceID = relationNode.get(3).asInt();
                    int specTokenID = relationNode.get(4).asInt();

                    Iterator<JsonNode> sentenceIDIterator = sentenceIDsNode.iterator();
                    Iterator<JsonNode> tokenIDIterator = tokenIDsNode.iterator();

                    int position = 0;

                    while (sentenceIDIterator.hasNext() && tokenIDIterator.hasNext()) {
                        int currentSentenceID = sentenceIDIterator.next().asInt();
                        int currentTokenID = tokenIDIterator.next().asInt();

                        if (currentSentenceID == specSentenceID && currentTokenID == specTokenID) {
                            furtherSpecification = furtherSpecificationLabeling(ner_tagsNode, tokensNode, position);

                            MyActivity activity = getActivityBySIDANDTID(activitySentenceID, activityTokenID);
                            activity.setFurtherSpecification(furtherSpecification);

                            break;
                        }
                        position++;
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }











        BpmnModelInstance modelInstance = Bpmn.createEmptyModel();
        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setTargetNamespace("https://camunda.org/examples");
        modelInstance.setDefinitions(definitions);

        Collaboration collaboration = modelInstance.newInstance(Collaboration.class);
        collaboration.setId("collaboration");
        modelInstance.getDefinitions().addChildElement(collaboration);

        // nacheinander alle Elemente einfügen
        for (MyActor actor : actors) {
            actorGenerating(actor, modelInstance, collaboration);
        }

        for (MyDataObject dataObject : dataObjects) {
            dataObjectGenerating(dataObject, modelInstance);
        }

        for (MyActivity activity : activities) {
            activityGenerating(activity, modelInstance, collaboration);
        }

        for (MyGateway gateway : gateways) {
            gatewayGenerating(gateway, modelInstance);
        }

        for (MyFlow flow : flows) {
            flowGenerating(flow, modelInstance, flows, collaboration);
        }

        startEventGenerating(modelInstance);

        endEventGenerating(modelInstance);

        connectProcessParts(modelInstance);

        



        try {
            Bpmn.validateModel(modelInstance);
            File file = new File("generate/ergebnis.bpmn");
            file.createNewFile();
    
            String bpmnString = Bpmn.convertToString(modelInstance);
            System.out.println("bpmnString");
            System.out.println(bpmnString);
    
            Bpmn.writeModelToFile(file, modelInstance);
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    // hilfe beim verbinden von Elementen
    private static void connect(SequenceFlow flow, FlowNode from, FlowNode to) {
        flow.setSource(from);
        from.getOutgoing().add(flow);
        flow.setTarget(to);
        to.getIncoming().add(flow);
    }

    // hilfe beim verbinden von Elementen mit message flow
    private static void messageConnect(MessageFlow flow, InteractionNode from, InteractionNode to) {
        flow.setSource(from);
        flow.setTarget(to);
    }

    // label von actor erstellen
    private static String actorLabeling(JsonNode ner_tagsNode, JsonNode tokensNode, int position) {
        String actorLabel = tokensNode.get(position).asText();
        position++;

        // Prüfen, welche Token zum Namen des Actors gehören
        for(int i = position; i < tokensNode.size(); i++) {
            String tag = ner_tagsNode.get(i).asText();

            if (tag.equals("I-Actor")) {
                String token = tokensNode.get(i).asText();
                actorLabel = actorLabel + " " + token;
            }

            else {
                break;
            }
        }

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(actorLabel);
        pipeline.annotate(document);

        List<String> cleanedTokens = new ArrayList<>();

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                // POS-Tags überprüfen und entfernen 
                if (!pos.equals("DT") && !pos.equals("IN")) {
                    cleanedTokens.add(word);
                }
            }
        }
        actorLabel = String.join("_", cleanedTokens);

        actorLabel = cleanLabel(actorLabel);

        actorLabel = actorLabel.substring(0, 1).toUpperCase() + actorLabel.substring(1);

        return actorLabel;
    }


    // label so aufbereiten, dass es verwedet werden kann
    // manchmal gab es verschiedene labels die nicht zugelassen wurden
    // -> z.B. wenn mit Zahl anfängt
    private static String cleanLabel(String label) {
        StringBuilder cleanedLabel = new StringBuilder();
        boolean isFirstChar = true;
    
        for (char c : label.toCharArray()) {
            if (Character.isLetter(c) || c == '_' || c == '-') {
                cleanedLabel.append(c);
            } else if (Character.isDigit(c)) {
                if (!isFirstChar) {
                    cleanedLabel.append(c);
                }
            }
            isFirstChar = false;
        }
        return cleanedLabel.toString();
    }
        


    //actor zurück geben
    private static MyActor getActorByName(String actorName) {
        for (MyActor actor : actors) {
            if (actor.getName().equals(actorName)) {
                return actor;
            }
        }
        return null;
    }


    // gibt es same Gatway?
    private static boolean sameGatewayExist(JsonNode relationsNode, int sentenceID, int tokenID) {
        for (JsonNode relationNode : relationsNode) {
            String relationType = relationNode.get(2).asText();

            if ("same gateway".equals(relationType)) {
                if (relationNode.get(0).asInt() == sentenceID && relationNode.get(1).asInt() == tokenID) {
                    return true;
                }
                if (relationNode.get(3).asInt() == sentenceID && relationNode.get(4).asInt() == tokenID) {
                    return true;
                }
            }
        }
        return false;
    }

    // same Gateway zurück geben
    private static MyGateway getSameGateway(JsonNode relationsNode, int sentenceID, int tokenID) {
        for (JsonNode relationNode : relationsNode) {
            String relationType = relationNode.get(2).asText();

            if ("same gateway".equals(relationType)) {
                if (relationNode.get(0).asInt() == sentenceID && relationNode.get(1).asInt() == tokenID) {
                    MyGateway sameGateway = getGatewayByID(relationNode.get(3).asInt(), relationNode.get(4).asInt());
                    return sameGateway;
                }
                if (relationNode.get(3).asInt() == sentenceID && relationNode.get(4).asInt() == tokenID) {
                    MyGateway sameGateway = getGatewayByID(relationNode.get(0).asInt(), relationNode.get(1).asInt());
                    return sameGateway;
                }
            }
        }
        return null;
    }

    // label von aktivitäten erstellen
    private static String activityLabeling(JsonNode ner_tagsNode, JsonNode tokensNode, int position) {
        String activityLabel = tokensNode.get(position).asText();
        position++;

        for(int i = position; i < tokensNode.size(); i++) {
            String tag = ner_tagsNode.get(i).asText();

            if (tag.equals("I-Activity")) {
                String token = tokensNode.get(i).asText();
                activityLabel = activityLabel + " " + token;
            }

            else {
                break;
            }
        }

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,pos,lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        CoreDocument document = pipeline.processToCoreDocument(activityLabel);
        StringBuilder lemmatizedSentence = new StringBuilder();


        for (CoreLabel tok : document.tokens()) {
            System.out.println(String.format("%s\t%s", tok.word(), tok.lemma()));
            lemmatizedSentence.append(tok.lemma()).append(" ");
        }

        activityLabel = lemmatizedSentence.toString().trim();
        String[] words = activityLabel.split("\\s+");
        activityLabel = String.join("_", words);

        activityLabel = cleanLabel(activityLabel);

        return activityLabel; 
        
    }


    // label von datenobjekten erstellen
    private static String dataObjectLabeling(JsonNode ner_tagsNode, JsonNode tokensNode, int position) {
        String dataObjectLabel = tokensNode.get(position).asText();
        position++;

        for(int i = position; i < tokensNode.size(); i++) {
            String tag = ner_tagsNode.get(i).asText();

            if (tag.equals("I-Activity Data")) {
                String token = tokensNode.get(i).asText();
                dataObjectLabel = dataObjectLabel + " " + token;
            }

            else {
                break;
            }
        }

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(dataObjectLabel);
        pipeline.annotate(document);

        List<String> cleanedTokens = new ArrayList<>();

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                /* POS-Tags überprüfen und entfernen (bei data-Object erstmal nicht?)
                if (!pos.equals("DT") && !pos.equals("IN")) {
                    cleanedTokens.add(word);
                }*/
                cleanedTokens.add(word);
            }
        }
        dataObjectLabel = String.join("_", cleanedTokens);

        dataObjectLabel = cleanLabel(dataObjectLabel);

        return dataObjectLabel;
    }


    // label von furtherSpecification erstellen
    private static String furtherSpecificationLabeling(JsonNode ner_tagsNode, JsonNode tokensNode, int position) {
        String furtherSpecificationLabel = tokensNode.get(position).asText();
        position++;

        for(int i = position; i < tokensNode.size(); i++) {
            String tag = ner_tagsNode.get(i).asText();

            if (tag.equals("I-Further Specification")) {
                String token = tokensNode.get(i).asText();
                furtherSpecificationLabel = furtherSpecificationLabel + " " + token;
            }

            else {
                break;
            }
        }

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,pos,lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        CoreDocument document = pipeline.processToCoreDocument(furtherSpecificationLabel);
        StringBuilder lemmatizedSentence = new StringBuilder();

        for (CoreLabel tok : document.tokens()) {
            System.out.println(String.format("%s\t%s", tok.word(), tok.lemma()));
            lemmatizedSentence.append(tok.lemma()).append(" ");
        }

        furtherSpecificationLabel = lemmatizedSentence.toString().trim();
        String[] words = furtherSpecificationLabel.split("\\s+");
        furtherSpecificationLabel = String.join("_", words);

        furtherSpecificationLabel = cleanLabel(furtherSpecificationLabel);

        return furtherSpecificationLabel;
    }


    // Data object zurück geben
    private static MyDataObject getDataObjectByName(String dataObjectLabel) {
        for (MyDataObject dataObject : dataObjects) {
            if (dataObject.getLabel().equals(dataObjectLabel)) {
                return dataObject;
            }
        }
        return null;
    }


    // aktivität mit sentence und token ID bekommen
    private static MyActivity getActivityBySIDANDTID(int sentenceID, int tokenID) {
        for (MyActivity activity : activities) {
            if (activity.getSentenceID().equals(sentenceID) && activity.getTokenID().equals(tokenID)) {
                return activity;
            }
        }
        return null;
    }

    
    // gateway mit mention bekommen, falls es in same Gateway vorkommt
    private static MyGateway getGatewayByID(int sentenceID, int tokenID) {
        String mentionID = sentenceID + "-" + tokenID;

        for (MyGateway gateway : gateways) {
            for (String mention : gateway.getMentions()) {
                if (mention.equals(mentionID)) {
                    return gateway;
                }
            }
        }
        return null;
    }


    // label mit data object auffüllen für das korrekte ganze label
    private static String fullLabel(MyActivity activity) {
        String label = activity.getLabel();

        int i = 0;
        for (MyDataObject dataObject : activity.getUsedDataObjects()) {
            if (i == 0) {
                label = label + "_" + dataObject.getLabel();
            }
            if (i != 0) {
                label = label + "_and_" + dataObject.getLabel();
            }
            i++;
        }
        return label;
    }
    

    // actor erstellen
    private static void actorGenerating(MyActor actor, BpmnModelInstance modelInstance, Collaboration collaboration) {
        Process process = modelInstance.newInstance(Process.class);
        process.setId(actor.getName() + "-Process");
        process.setExecutable(true);
        modelInstance.getDefinitions().addChildElement(process);

        Participant participant = modelInstance.newInstance(Participant.class);
        participant.setId(actor.getName());
        participant.setName(actor.getName());
        participant.setProcess(process);
        collaboration.addChildElement(participant);
    }


    // label für condition erstellen
    private static String conditionLabeling(JsonNode ner_tagsNode, JsonNode tokensNode, int position) {
        String conditionLabel = tokensNode.get(position).asText();
        position++;

        for(int i = position; i < tokensNode.size(); i++) {
            String tag = ner_tagsNode.get(i).asText();

            if (tag.equals("I-Condition Specification")) {
                String token = tokensNode.get(i).asText();
                conditionLabel = conditionLabel + " " + token;
            }

            else {
                break;
            }
        }

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Annotation document = new Annotation(conditionLabel);

        pipeline.annotate(document);

        List<String> cleanedTokens = new ArrayList<>();

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                // POS-Tags überprüfen und entfernen 
                if (!pos.equals("DT") && !pos.equals("IN")) {
                    cleanedTokens.add(word);
                }
            }
        }
        conditionLabel = String.join("_", cleanedTokens);

        conditionLabel = cleanLabel(conditionLabel);

        return conditionLabel;
    }


    // überprüfen ob das Element links eine Aktivität ist, wenn ja return
    private static MyActivity nextElementOnLeftIsActivity(BpmnModelInstance modelInstance, MyActivity activity) {
        MyActivity foundActivity = null;

        for (MyFlow flow : flows) {
            if ((flow.getTail() == activity) && (flow.getHead() instanceof MyActivity)) {
                foundActivity = (MyActivity) flow.getHead();
                return foundActivity;
            }
        } 
        return null;
    }


    // überprüfen ob das Element links ein Gateway ist, wenn ja return
    private static MyGateway nextElementOnLeftIsGateway(BpmnModelInstance modelInstance, MyActivity activity) {
        MyGateway foundGateway = null;

        for (MyFlow flow : flows) {
            if ((flow.getTail() == activity) && (flow.getHead() instanceof MyGateway)) {
                foundGateway = (MyGateway) flow.getHead();
                return foundGateway;
            }
        } 
        return null;
    }


    // überprüfen ob das Element rechts eine Aktivität ist, wenn ja return
    private static MyActivity nextElementOnRightIsActivity(BpmnModelInstance modelInstance, MyActivity activity) {
        MyActivity foundActivity = null;

        for (MyFlow flow : flows) {
            if ((flow.getHead() == activity) && (flow.getTail() instanceof MyActivity)) {
                foundActivity = (MyActivity) flow.getTail();
                return foundActivity;
            }
        } 
        return null;
    }


    // überprüfen ob das Element rechts eine Aktivität ist, wenn ja return
    private static MyGateway nextElementOnRightIsGateway(BpmnModelInstance modelInstance, MyActivity activity) {
        MyGateway foundGateway = null;

        for (MyFlow flow : flows) {
            if ((flow.getHead() == activity) && (flow.getTail() instanceof MyGateway)) {
                foundGateway = (MyGateway) flow.getTail();
                return foundGateway;
            }
        } 
        return null;
    }


    // Methode die erst ganz nach links geht, um nächste Aktivität zu finden die in einem Prozess eingefügt ist
    // -> wenn keine links dann geht sie nach rechts
    // -> gibt dann den Prozess zurück, in der diese Aktivität ist
    private static Process findProcessWithActivity(BpmnModelInstance modelInstance, MyActivity activity) {
        if (activity.getPerformer() == null) {
            Process process = findProcessWithActivityLeft(modelInstance, activity);
            if (process == null) {
                process = findProcessWithActivityRight(modelInstance, activity);
                return process;
            }
            return process;
        }

        String processID = activity.getPerformer().getName() + "-Process";
        for (Process process : modelInstance.getModelElementsByType(Process.class)) {
            if (process.getId().equals(processID)) {
                return process;
            }
        }
        return null;
    }


    // -> folgt aus der vorherigen Methode -> hier also nach links
    private static Process findProcessWithActivityLeft(BpmnModelInstance modelInstance, MyActivity activity) {
        MyGateway foundGateway = null;
        MyActivity foundActivity = null;
        Process process = null;

        for (MyFlow flow : flows) {
            if ((flow.getTail() == activity) && (flow.getHead() instanceof MyActivity)) {
                foundActivity = (MyActivity) flow.getHead();
                while(true) {
                    if (foundActivity.getPerformer() == null) {
                        MyActivity newFoundActivity = nextElementOnLeftIsActivity(modelInstance, foundActivity);
                        if (newFoundActivity != null) {
                            if (newFoundActivity.getPerformer() != null) {
                                process = findProcessWithActivity(modelInstance, newFoundActivity);
                                return process;
                            }else {
                                foundActivity = newFoundActivity;
                                continue;
                            }
                        }
                        MyGateway newFoundGateway = nextElementOnLeftIsGateway(modelInstance, foundActivity);
                        if (newFoundGateway != null) {
                            process = findProcessWithGatewayLeft(modelInstance, newFoundGateway);
                            return process;
                        }
                        return null;
                    }else {
                        process = findProcessWithActivity(modelInstance, foundActivity);
                        return process;
                    }
                }
            }

            if ((flow.getTail() == activity) && (flow.getHead() instanceof MyGateway)) {
                foundGateway = (MyGateway) flow.getHead();
                process = findProcessWithGatewayLeft(modelInstance, foundGateway);
                if (process == null) {
                    return null;
                }else {
                    return process;
                }
            }
        } 
        return null;
    }


    // -> folgt aus der vorherigen Methode -> hier also nach rechts
    private static Process findProcessWithActivityRight(BpmnModelInstance modelInstance, MyActivity activity) {
        MyGateway foundGateway = null;
        MyActivity foundActivity = null;
        Process process = null;

        for (MyFlow flow : flows) {
            if ((flow.getHead() == activity) && (flow.getTail() instanceof MyActivity)) {
                foundActivity = (MyActivity) flow.getTail();
                while(true) {
                    if (foundActivity.getPerformer() == null) {
                        MyActivity newFoundActivity = nextElementOnRightIsActivity(modelInstance, foundActivity);
                        if (newFoundActivity != null) {
                            if (newFoundActivity.getPerformer() != null) {
                                process = findProcessWithActivity(modelInstance, newFoundActivity);
                                return process;
                            }else {
                                foundActivity = newFoundActivity;
                                continue;
                            }
                        }
                        MyGateway newFoundGateway = nextElementOnRightIsGateway(modelInstance, foundActivity);
                        if (newFoundGateway != null) {
                            process = findProcessWithGatewayRight(modelInstance, newFoundGateway);
                            return process;
                        }
                        return null;
                    }else {
                        process = findProcessWithActivity(modelInstance, foundActivity);
                        return process;
                    }
                }
            }

            if ((flow.getHead() == activity) && (flow.getTail() instanceof MyGateway)) {
                foundGateway = (MyGateway) flow.getTail();
                process = findProcessWithGatewayRight(modelInstance, foundGateway);
                if (process == null) {
                    return null;
                }else {
                    return process;
                }
            }
        }
        return null;
    }


    // Datenobjekte werden eingefügt -> über die Aktivitäten, die die benutzen Objekte speichern 
    private static void dataObjectGenerating(MyDataObject myDataObject, BpmnModelInstance modelInstance) {
        for (MyActivity activity : activities) {
            for (MyDataObject dataObjectFinder : activity.getUsedDataObjects()) {
                if (dataObjectFinder.getLabel() == myDataObject.getLabel()) {
                    Process process = findProcessWithActivity(modelInstance, activity);

                    DataObject dataObject = modelInstance.newInstance(DataObject.class);
                    dataObject.setId(myDataObject.getLabel());
                    dataObject.setName(myDataObject.getLabel());
                    process.addChildElement(dataObject);

                    DataObjectReference dataObjectReference = modelInstance.newInstance(DataObjectReference.class);
                    dataObjectReference.setDataObject(dataObject);
                    dataObjectReference.setId(myDataObject.getLabel() + "-Reference");
                    process.addChildElement(dataObjectReference);

                    return;
                }
            }
        }  
    }


    // Aktivitäten werden eingefügt
    private static void activityGenerating(MyActivity activity, BpmnModelInstance modelInstance, Collaboration collaboration) {
        if (activity.getRecipient() == null) {
            if (activity.getPerformer() == null) {
                Process process = findProcessWithActivity(modelInstance, activity);

                Task task = modelInstance.newInstance(Task.class);
                task.setId("ID-" + activity.getSentenceID() + "-" + activity.getTokenID());
                task.setName(fullLabel(activity));
                process.addChildElement(task);

                // hier werden immer die verbindungen zu den zugehörigen, schon eingefügten Datenobjekten erstellt
                for (MyDataObject data : activity.getUsedDataObjects()) {
                    String dataObjectReferenceId = data.getLabel() + "-Reference";
                    DataObjectReference dataObjectReference = getDataObjectReferenceById(modelInstance, dataObjectReferenceId);
                    
                    DataOutputAssociation association = modelInstance.newInstance(DataOutputAssociation.class);
                    association.setTarget(dataObjectReference);
                    task.addChildElement(association);
                }

                // Problem mit builder? Kommentare im Modell zunächst nicht höchste Priorität
                /* normaler Task funktioniert damit nicht -> userTask z.B. schon 
                if (activity.getFurtherSpecification() != null) {
                    CamundaProperties camundaProperties = modelInstance.newInstance(CamundaProperties.class);
                    task.builder().addExtensionElement(camundaProperties);

                    CamundaProperty property = modelInstance.newInstance(CamundaProperty.class);
                    property.setCamundaName("Kommentar");
                    property.setCamundaValue(activity.getFurtherSpecification());
                    task.builder().addExtensionElement(property);
                }*/
            }

            if (activity.getPerformer() != null) {
                Process process = findProcessWithActivity(modelInstance, activity);

                // hier die Fallunterscheidung, ob die Aktivität performer hat
                // -> mittlerweile unnötig aber falls die art des Tasks daran abhängen kann sinnvoll
                Task task = modelInstance.newInstance(Task.class);
                task.setId("ID-" + activity.getSentenceID() + "-" + activity.getTokenID());
                task.setName(fullLabel(activity));
                process.addChildElement(task);

                for (MyDataObject data : activity.getUsedDataObjects()) {
                    String dataObjectReferenceId = data.getLabel() + "-Reference";
                    DataObjectReference dataObjectReference = getDataObjectReferenceById(modelInstance, dataObjectReferenceId);
                    
                    DataOutputAssociation association = modelInstance.newInstance(DataOutputAssociation.class);
                    association.setTarget(dataObjectReference);
                    task.addChildElement(association);
                }

                /* normaler Task funktioniert damit nicht -> userTask z.B. schon
                for (MyActor actor : activity.getFurtherPerformers()) {
                    
                    CamundaProperties camundaProperties = modelInstance.newInstance(CamundaProperties.class);
                    task.builder().addExtensionElement(camundaProperties);

                    CamundaProperty property = modelInstance.newInstance(CamundaProperty.class);
                    property.setCamundaName("Kommentar");
                    property.setCamundaValue("Further-Actor_" + actor.getName());
                    task.builder().addExtensionElement(property);
                }*/

                /* normaler Task funktioniert damit nicht -> userTask z.B. schon
                if (activity.getFurtherSpecification() != null) {
                    CamundaProperties camundaProperties = modelInstance.newInstance(CamundaProperties.class);
                    task.builder().addExtensionElement(camundaProperties);

                    CamundaProperty property = modelInstance.newInstance(CamundaProperty.class);
                    property.setCamundaName("Kommentar");
                    property.setCamundaValue(activity.getFurtherSpecification());
                    task.builder().addExtensionElement(property);
                }*/
            }
        }
        else {
            if (activity.getPerformer() == null) {
                Process process = findProcessWithActivity(modelInstance, activity);

                Task sourceTask = modelInstance.newInstance(Task.class);
                sourceTask.setId("ID-" + activity.getSentenceID() + "-" + activity.getTokenID());
                sourceTask.setName(fullLabel(activity));
                process.addChildElement(sourceTask);

                Process recipientProcess = null;
                String processID = activity.getRecipient().getName() + "-Process";
                for (Process foundProcess : modelInstance.getModelElementsByType(Process.class)) {
                    if (foundProcess.getId().equals(processID)) {
                        recipientProcess = foundProcess;
                        break;
                    }
                }
                boolean samePerformerRecipient = nextElementOfActivitySamePerformerRecipient(modelInstance, activity);

                // wenn etwas zu recipient geschickt wird, bekommt dieser receive Aktivität
                // -> nur wenn der Empfänger nicht auch die nächste Aktivität ausführt
                if (!samePerformerRecipient) {
                    Task targetTask = modelInstance.newInstance(Task.class);
                    targetTask.setName("reveice");
                    recipientProcess.addChildElement(targetTask);

                    MessageFlow messageFlow = modelInstance.newInstance(MessageFlow.class);
                    InteractionNode sourceElement = (InteractionNode) sourceTask;
                    InteractionNode targetElement = (InteractionNode) targetTask;
                    messageFlow.setId("Flow-" + sourceElement.getId() + "-to-" + targetElement.getId());
                    collaboration.addChildElement(messageFlow);
                    messageConnect(messageFlow, sourceElement, targetElement);
                }

                for (MyDataObject data : activity.getUsedDataObjects()) {
                    String dataObjectReferenceId = data.getLabel() + "-Reference";
                    DataObjectReference dataObjectReference = getDataObjectReferenceById(modelInstance, dataObjectReferenceId);
                    
                    DataOutputAssociation association = modelInstance.newInstance(DataOutputAssociation.class);
                    association.setTarget(dataObjectReference);
                    sourceTask.addChildElement(association);
                }

                /* 
                if (activity.getFurtherSpecification() != null) {
                    CamundaProperties camundaProperties = modelInstance.newInstance(CamundaProperties.class);
                    sourceTask.builder().addExtensionElement(camundaProperties);

                    CamundaProperty property = modelInstance.newInstance(CamundaProperty.class);
                    property.setCamundaName("Kommentar");
                    property.setCamundaValue(activity.getFurtherSpecification());
                    sourceTask.builder().addExtensionElement(property);
                }*/
            }

            if (activity.getPerformer() != null) {
                Process process = findProcessWithActivity(modelInstance, activity);

                Task sourceTask = modelInstance.newInstance(Task.class);
                sourceTask.setId("ID-" + activity.getSentenceID() + "-" + activity.getTokenID());
                sourceTask.setName(fullLabel(activity));
                process.addChildElement(sourceTask);

                Process recipientProcess = null;
                String processID = activity.getRecipient().getName() + "-Process";
                for (Process foundProcess : modelInstance.getModelElementsByType(Process.class)) {
                    if (foundProcess.getId().equals(processID)) {
                        recipientProcess = foundProcess;
                        break;
                    }
                }
                boolean samePerformerRecipient = nextElementOfActivitySamePerformerRecipient(modelInstance, activity);

                if (!samePerformerRecipient) {
                    Task targetTask = modelInstance.newInstance(Task.class);
                    targetTask.setName("reveice");
                    recipientProcess.addChildElement(targetTask);

                    MessageFlow messageFlow = modelInstance.newInstance(MessageFlow.class);
                    InteractionNode sourceElement = (InteractionNode) sourceTask;
                    InteractionNode targetElement = (InteractionNode) targetTask;
                    messageFlow.setId("Flow-" + sourceElement.getId() + "-to-" + targetElement.getId());
                    collaboration.addChildElement(messageFlow);
                    messageConnect(messageFlow, sourceElement, targetElement);
                }

                /*
                for (MyActor actor : activity.getFurtherPerformers()) {
                    CamundaProperties camundaProperties = modelInstance.newInstance(CamundaProperties.class);
                    sourceTask.builder().addExtensionElement(camundaProperties);

                    CamundaProperty property = modelInstance.newInstance(CamundaProperty.class);
                    property.setCamundaName("Kommentar");
                    property.setCamundaValue("Further-Actor_" + actor.getName());
                    sourceTask.builder().addExtensionElement(property);
                }*/

                for (MyDataObject data : activity.getUsedDataObjects()) {
                    String dataObjectReferenceId = data.getLabel() + "-Reference";
                    DataObjectReference dataObjectReference = getDataObjectReferenceById(modelInstance, dataObjectReferenceId);
                    
                    DataOutputAssociation association = modelInstance.newInstance(DataOutputAssociation.class);
                    association.setTarget(dataObjectReference);
                    sourceTask.addChildElement(association);
                }

                /*
                if (activity.getFurtherSpecification() != null) {
                    CamundaProperties camundaProperties = modelInstance.newInstance(CamundaProperties.class);
                    sourceTask.builder().addExtensionElement(camundaProperties);

                    CamundaProperty property = modelInstance.newInstance(CamundaProperty.class);
                    property.setCamundaName("Kommentar");
                    property.setCamundaValue(activity.getFurtherSpecification());
                    sourceTask.builder().addExtensionElement(property);
                }*/
            }
        }
    }


    // prüfen, ob participant existiert
    public static boolean participantExists(String name, Collaboration collaboration) {
        for (Participant participant : collaboration.getParticipants()) {
            if (participant.getName() != null && participant.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }


    // DataObjectReference bekommen über ID
    public static DataObjectReference getDataObjectReferenceById(BpmnModelInstance modelInstance, String dataObjectReferenceId) {
        Collection<DataObjectReference> dataObjectReferences = modelInstance.getModelElementsByType(DataObjectReference.class);
    
        for (DataObjectReference dataObjectReference : dataObjectReferences) {
            if (dataObjectReferenceId.equals(dataObjectReference.getId())) {
                return dataObjectReference;
            }
        }
    
        return null;
    }



    // Ist das nächste Element nach einer Aktivität in der gleichen Lane wie der Recipient der Ausgangs-Aktivität? 
    private static boolean nextElementOfActivitySamePerformerRecipient(BpmnModelInstance modelInstance, MyActivity activity) {
        for (MyFlow flow : flows) {
            if ((flow.getHead() == activity) && (flow.getTail() instanceof MyGateway)) {
                return false;
            }
            if ((flow.getHead() == activity) && (flow.getTail() instanceof MyActivity)) {
                MyActivity foundActivity = getActivityBySIDANDTID(flow.getTail().getSentenceID(), flow.getTail().getTokenID());
                if (foundActivity.getPerformer() == activity.getRecipient()) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        return false;
    }


    // gleich wie findProcessWithActivityLeft() 
    // -> also zum finden des nächsten Gateways links und zurückgeben des zugehörigen Prozesses
    private static Process findProcessWithGatewayLeft(BpmnModelInstance modelInstance, MyGateway gateway) {
        MyGateway foundGateway = null;
        MyActivity foundActivity = null;
        Process process = null;

        for (MyFlow flow : flows) {
            if ((flow.getTail() == gateway) && (flow.getHead() instanceof MyActivity)) {
                foundActivity = (MyActivity) flow.getHead();
                while(true) {
                    if (foundActivity.getPerformer() == null) {
                        MyActivity newFoundActivity = nextElementOnLeftIsActivity(modelInstance, foundActivity);
                        if (newFoundActivity != null) {
                            if (newFoundActivity.getPerformer() != null) {
                                process = findProcessWithActivity(modelInstance, newFoundActivity);
                                return process;
                            }else {
                                foundActivity = newFoundActivity;
                                continue;
                            }
                        }
                        MyGateway newFoundGateway = nextElementOnLeftIsGateway(modelInstance, foundActivity);
                        if (newFoundGateway != null) {
                            process = findProcessWithGatewayLeft(modelInstance, newFoundGateway);
                            return process;
                        }
                        return null;
                    }else {
                        process = findProcessWithActivity(modelInstance, foundActivity);
                        return process;
                    }
                }
            }

            if ((flow.getTail() == gateway) && (flow.getHead() instanceof MyGateway)) {
                foundGateway = (MyGateway) flow.getHead();
                process = findProcessWithGatewayLeft(modelInstance, foundGateway);
                if (process == null) {
                    return null;
                }else {
                    return process;
                }
            }
        } 
        return null;
    }


    // wie findProcessWithActivityRight()
    // -> also zum finden des nächsten Gateways rechts und zurückgeben des zugehörigen Prozesses
    private static Process findProcessWithGatewayRight(BpmnModelInstance modelInstance, MyGateway gateway) {
        MyGateway foundGateway = null;
        MyActivity foundActivity = null;
        Process process = null;

        for (MyFlow flow : flows) {
            if ((flow.getHead() == gateway) && (flow.getTail() instanceof MyActivity)) {
                foundActivity = (MyActivity) flow.getTail();
                while(true) {
                    if (foundActivity.getPerformer() == null) {
                        MyActivity newFoundActivity = nextElementOnRightIsActivity(modelInstance, foundActivity);
                        if (newFoundActivity != null) {
                            if (newFoundActivity.getPerformer() != null) {
                                process = findProcessWithActivity(modelInstance, newFoundActivity);
                                return process;
                            }else {
                                foundActivity = newFoundActivity;
                                continue;
                            }
                        }
                        MyGateway newFoundGateway = nextElementOnRightIsGateway(modelInstance, foundActivity);
                        if (newFoundGateway != null) {
                            process = findProcessWithGatewayRight(modelInstance, newFoundGateway);
                            return process;
                        }
                        return null;
                    }else {
                        process = findProcessWithActivity(modelInstance, foundActivity);
                        return process;
                    }
                }
            }

            if ((flow.getHead() == gateway) && (flow.getTail() instanceof MyGateway)) {
                foundGateway = (MyGateway) flow.getTail();
                process = findProcessWithGatewayRight(modelInstance, foundGateway);
                if (process == null) {
                    return null;
                }else {
                    return process;
                }
            }
        }
        return null;
    }


    // Gateways einfügen -> dazu herausfinden in welchen Prozess davor
    private static void gatewayGenerating(MyGateway mygateway, BpmnModelInstance modelInstance) {
        Process process = findProcessWithGatewayLeft(modelInstance, mygateway);
        if (process == null) {
            process = findProcessWithGatewayRight(modelInstance, mygateway);
        }

        if (mygateway.getType().equals("xor")) {
            ExclusiveGateway gateway = modelInstance.newInstance(ExclusiveGateway.class);
            gateway.setId("ID-" + mygateway.getSentenceID() + "-" + mygateway.getTokenID());
            process.addChildElement(gateway);
        }

        if (mygateway.getType().equals("and")) {
            ParallelGateway gateway = modelInstance.newInstance(ParallelGateway.class);
            gateway.setId("ID-" + mygateway.getSentenceID() + "-" + mygateway.getTokenID());
            process.addChildElement(gateway);
        }
    }


    // Prozess mit schon eingefügtem Element finden
    private static Process findProcessByElement(BpmnModelInstance modelInstance, FlowNode element) {
        for (Process process : modelInstance.getModelElementsByType(Process.class)) {
            if (element != null && process.getFlowElements().contains(element)) {
                return process;
            }
        }
        return null;
    }


    // Flows einfügen
    private static void flowGenerating(MyFlow flow, BpmnModelInstance modelInstance, List<MyFlow> flows, Collaboration collaboration) {
        // Endevent bei Sonderfall einfügen -> Das endevent, das vorher als Objekt erstellt wurde
        if (flow.getTail() instanceof MyEndevent) {
            if (flow.getCondition() == null) {
                String headID = "ID-" + flow.getHead().getSentenceID() + "-" + flow.getHead().getTokenID();
                String tailID = "ID-" + flow.getTail().getSentenceID() + "-" + flow.getTail().getTokenID();

                FlowNode sourceElement = (FlowNode) modelInstance.getModelElementById(headID);
                Process process = findProcessByElement(modelInstance, sourceElement);

                EndEvent endEvent = modelInstance.newInstance(EndEvent.class);
                endEvent.setId("endEvent-" + tailID);
                process.addChildElement(endEvent);

                FlowNode targetElement = (FlowNode) modelInstance.getModelElementById("endEvent-" + tailID);

                SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                sequenceFlow.setId("Flow-" + sourceElement.getId() + "-to-" + targetElement.getId());
                process.addChildElement(sequenceFlow);
                connect(sequenceFlow, sourceElement, targetElement);
            }

            if (!(flow.getCondition() == null)) {
                String headID = "ID-" + flow.getHead().getSentenceID() + "-" + flow.getHead().getTokenID();
                String tailID = "ID-" + flow.getTail().getSentenceID() + "-" + flow.getTail().getTokenID();

                FlowNode sourceElement = (FlowNode) modelInstance.getModelElementById(headID);
                Process process = findProcessByElement(modelInstance, sourceElement);

                EndEvent endEvent = modelInstance.newInstance(EndEvent.class);
                endEvent.setId("endEvent-" + tailID);
                process.addChildElement(endEvent);

                FlowNode targetElement = (FlowNode) modelInstance.getModelElementById("endEvent-" + tailID);

                SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                sequenceFlow.setId("Flow-" + sourceElement.getId() + "-to-" + targetElement.getId());
                sequenceFlow.setName(flow.getCondition());
                process.addChildElement(sequenceFlow);
                connect(sequenceFlow, sourceElement, targetElement);

                ConditionExpression conditionExpression = modelInstance.newInstance(ConditionExpression.class);
                conditionExpression.setTextContent(flow.getCondition());

                sequenceFlow.setConditionExpression(conditionExpression);

                process.addChildElement(sequenceFlow);
            }
        }
        else {
            if (flow.getCondition() == null) {
                String headID = "ID-" + flow.getHead().getSentenceID() + "-" + flow.getHead().getTokenID();
                String tailID = "ID-" + flow.getTail().getSentenceID() + "-" + flow.getTail().getTokenID();

                FlowNode sourceElement = (FlowNode) modelInstance.getModelElementById(headID);
                FlowNode targetElement = (FlowNode) modelInstance.getModelElementById(tailID);

                Process processSource = findProcessByElement(modelInstance, sourceElement);
                Process processTarget = findProcessByElement(modelInstance, targetElement);

                // überprüfen, ob sich die Elemente im selben Prozess befinden
                // -> Sequenzflow wenn gleicher Prozess, message flow sonst
                if (processSource == processTarget) {
                    SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                    sequenceFlow.setId("Flow-" + sourceElement.getId() + "-to-" + targetElement.getId());
                    processSource.addChildElement(sequenceFlow);
                    connect(sequenceFlow, sourceElement, targetElement);
                }
                // Fall, dass message flow benötigt ist
                // bei gateways auf message flow achten und mit continue-zwischenaktivitäten helfen
                else {
                    if (flow.getHead() instanceof MyGateway) {
                        Task task = modelInstance.newInstance(Task.class);
                        task.setName("continue");
                        processSource.addChildElement(task);

                        SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                        sequenceFlow.setId("Flow-" + sourceElement.getId() + "-to-" + task.getId());
                        processSource.addChildElement(sequenceFlow);
                        connect(sequenceFlow, sourceElement, task);

                        MessageFlow messageFlow = modelInstance.newInstance(MessageFlow.class);
                        InteractionNode interactionSource = (InteractionNode) task;
                        InteractionNode interactionTarget = (InteractionNode) targetElement;
                        messageFlow.setId("Flow-" + task.getId() + "-to-" + targetElement.getId());
                        collaboration.addChildElement(messageFlow);
                        messageConnect(messageFlow, interactionSource, interactionTarget);
                    }
                    else {
                        MessageFlow messageFlow = modelInstance.newInstance(MessageFlow.class);
                        InteractionNode interactionSource = (InteractionNode) sourceElement;
                        InteractionNode interactionTarget = (InteractionNode) targetElement;
                        messageFlow.setId("Flow-" + sourceElement.getId() + "-to-" + targetElement.getId());
                        collaboration.addChildElement(messageFlow);
                        messageConnect(messageFlow, interactionSource, interactionTarget);
                    }
                }

                // Bei gateway die ausgehenden Kanten zählen und bei mehr 0 oder 1 ausgehenden ein endevent einfügen
                if (flow.getHead() instanceof MyGateway) {
                    int numberOfOutgoings = 0;
                    for (MyFlow flow2 : flows) {
                        if (flow2.getHead().getSentenceID() == flow.getHead().getSentenceID() && flow2.getHead().getTokenID() == flow.getHead().getTokenID()) {
                            numberOfOutgoings++;
                        }
                    }
                    if (numberOfOutgoings < 2) {
                        EndEvent endEvent = modelInstance.newInstance(EndEvent.class);
                        endEvent.setId("endEvent-" + headID);
                        processSource.addChildElement(endEvent);

                        SequenceFlow sequenceFlow2 = modelInstance.newInstance(SequenceFlow.class);
                        sourceElement = (FlowNode) modelInstance.getModelElementById(headID);
                        targetElement = (FlowNode) modelInstance.getModelElementById("endEvent-" + tailID);
                        sequenceFlow2.setId("Flow-" + sourceElement.getId() + "-to-" + targetElement.getId());
                        processSource.addChildElement(sequenceFlow2);
                        connect(sequenceFlow2, sourceElement, targetElement);
                    }
                }
            }
            
            // hier der fall, dass der flow eine Condition hat, ansonsten analog
            // -> normalerweise sollten normale Flows keine Condition annotiert haben aber sicherheitshalber
            if (!(flow.getCondition() == null)) {
                String headID = "ID-" + flow.getHead().getSentenceID() + "-" + flow.getHead().getTokenID();
                String tailID = "ID-" + flow.getTail().getSentenceID() + "-" + flow.getTail().getTokenID();

                FlowNode sourceElement = (FlowNode) modelInstance.getModelElementById(headID);
                FlowNode targetElement = (FlowNode) modelInstance.getModelElementById(tailID);

                Process processSource = findProcessByElement(modelInstance, sourceElement);
                Process processTarget = findProcessByElement(modelInstance, targetElement);

                if (processSource == processTarget) {
                    SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                    sequenceFlow.setId("Flow-" + sourceElement.getId() + "-to-" + targetElement.getId());
                    sequenceFlow.setName(flow.getCondition());

                    ConditionExpression conditionExpression = modelInstance.newInstance(ConditionExpression.class);
                    conditionExpression.setTextContent(flow.getCondition());
                    sequenceFlow.setConditionExpression(conditionExpression);

                    processSource.addChildElement(sequenceFlow);
                    connect(sequenceFlow, sourceElement, targetElement);
                }
                else {
                    Task task = modelInstance.newInstance(Task.class);
                    task.setName("continue");
                    processSource.addChildElement(task);

                    SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                    sequenceFlow.setId("Flow-" + sourceElement.getId() + "-to-" + task.getId());
                    sequenceFlow.setName(flow.getCondition());

                    ConditionExpression conditionExpression = modelInstance.newInstance(ConditionExpression.class);
                    conditionExpression.setTextContent(flow.getCondition());
                    sequenceFlow.setConditionExpression(conditionExpression);

                    processSource.addChildElement(sequenceFlow);
                    connect(sequenceFlow, sourceElement, task);

                    MessageFlow messageFlow = modelInstance.newInstance(MessageFlow.class);
                    InteractionNode interactionSource = (InteractionNode) task;
                    InteractionNode interactionTarget = (InteractionNode) targetElement;
                    messageFlow.setId("Flow-" + task.getId() + "-to-" + targetElement.getId());
                    collaboration.addChildElement(messageFlow);
                    messageConnect(messageFlow, interactionSource, interactionTarget);
                }

                if (flow.getHead() instanceof MyGateway) {
                    int numberOfOutgoings = 0;
                    for (MyFlow flow2 : flows) {
                        if (flow2.getHead().getSentenceID() == flow.getHead().getSentenceID() && flow2.getHead().getTokenID() == flow.getHead().getTokenID()) {
                            numberOfOutgoings++;
                        }
                    }
                    if (numberOfOutgoings < 2) {
                        EndEvent endEvent = modelInstance.newInstance(EndEvent.class);
                        endEvent.setId("endEvent-" + headID);
                        processSource.addChildElement(endEvent);

                        SequenceFlow sequenceFlow2 = modelInstance.newInstance(SequenceFlow.class);
                        sourceElement = (FlowNode) modelInstance.getModelElementById(headID);
                        targetElement = (FlowNode) modelInstance.getModelElementById("endEvent-" + headID);
                        sequenceFlow2.setId("Flow-" + sourceElement.getId() + "-to-" + targetElement.getId());
                        processSource.addChildElement(sequenceFlow2);
                        connect(sequenceFlow2, sourceElement, targetElement);
                    }
                }
            }
        }
    }


    // startevent im Prozess mit der Aktivität oder Gateway ohne eingehende flüsse
    // -> dabei dieses vor den supporting Task des Prozesses
    private static void startEventGenerating(BpmnModelInstance modelInstance) {
        for (MyActivity activity : activities) {
            for (MyFlow flow : flows) {
                if (activity.equals(flow.getTail())) {
                    break;
                }
                if (flow.equals(flows.getLast())) {
                    String firstActivityID = "ID-" + activity.getSentenceID() + "-" + activity.getTokenID();

                    FlowNode targetElement = (FlowNode) modelInstance.getModelElementById(firstActivityID);
                    Process process = findProcessByElement(modelInstance, targetElement);

                    StartEvent startEvent = modelInstance.newInstance(StartEvent.class);
                    startEvent.setId(firstActivityID + "start");
                    process.addChildElement(startEvent);

                    Task task = modelInstance.newInstance(Task.class);
                    task.setId("supportingTask-" + process.getId());
                    task.setName("supportingTask");
                    process.addChildElement(task);

                    ExclusiveGateway gateway = modelInstance.newInstance(ExclusiveGateway.class);
                    gateway.setId("supportingTask_Gateway-" + process.getId());
                    process.addChildElement(gateway);

                    SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                    FlowNode sourceElement = (FlowNode) modelInstance.getModelElementById(firstActivityID + "start");
                    sequenceFlow.setId("Flow-" + sourceElement.getId() + "-to-" + task.getId());
                    process.addChildElement(sequenceFlow);
                    connect(sequenceFlow, sourceElement, task);

                    SequenceFlow sequenceFlow2 = modelInstance.newInstance(SequenceFlow.class);
                    sequenceFlow2.setId("Flow-" + task.getId() + "-to-" + gateway.getId());
                    process.addChildElement(sequenceFlow2);
                    connect(sequenceFlow2, task, gateway);

                    SequenceFlow sequenceFlow3 = modelInstance.newInstance(SequenceFlow.class);
                    sequenceFlow3.setId("Flow-" + gateway.getId() + "-to-" + targetElement.getId());
                    sequenceFlow3.setName("Process_Start");
                    process.addChildElement(sequenceFlow3);
                    connect(sequenceFlow3, gateway, targetElement);

                    ConditionExpression conditionExpression = modelInstance.newInstance(ConditionExpression.class);
                    conditionExpression.setTextContent("Process_Start");
                    sequenceFlow3.setConditionExpression(conditionExpression);
                    process.addChildElement(sequenceFlow3);

                    break;
                }
            }
        }

        for (MyGateway gateway : gateways) {
            for (MyFlow flow : flows) {
                if (gateway.equals(flow.getTail())) {
                    break;
                }
                if (flow.equals(flows.getLast())) {
                    String firstGatewayID = "ID-" + gateway.getSentenceID() + "-" + gateway.getTokenID();

                    FlowNode targetElement = (FlowNode) modelInstance.getModelElementById(firstGatewayID);
                    Process process = findProcessByElement(modelInstance, targetElement);

                    StartEvent startEvent = modelInstance.newInstance(StartEvent.class);
                    startEvent.setId(firstGatewayID + "start");
                    process.addChildElement(startEvent);

                    Task task = modelInstance.newInstance(Task.class);
                    task.setId("supportingTask-" + process.getId());
                    task.setName("supportingTask");
                    process.addChildElement(task);

                    ExclusiveGateway gateway2 = modelInstance.newInstance(ExclusiveGateway.class);
                    gateway2.setId("supportingTask_Gateway-" + process.getId());
                    process.addChildElement(gateway2);

                    SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                    FlowNode sourceElement = (FlowNode) modelInstance.getModelElementById(firstGatewayID + "start");
                    sequenceFlow.setId("Flow-" + sourceElement.getId() + "-to-" + task.getId());
                    process.addChildElement(sequenceFlow);
                    connect(sequenceFlow, sourceElement, task);

                    SequenceFlow sequenceFlow2 = modelInstance.newInstance(SequenceFlow.class);
                    sequenceFlow2.setId("Flow-" + task.getId() + "-to-" + gateway2.getId());
                    process.addChildElement(sequenceFlow2);
                    connect(sequenceFlow2, task, gateway2);

                    SequenceFlow sequenceFlow3 = modelInstance.newInstance(SequenceFlow.class);
                    sequenceFlow3.setId("Flow-" + gateway2.getId() + "-to-" + targetElement.getId());
                    sequenceFlow3.setName("Process_Start");
                    process.addChildElement(sequenceFlow3);
                    connect(sequenceFlow3, gateway2, targetElement);

                    ConditionExpression conditionExpression = modelInstance.newInstance(ConditionExpression.class);
                    conditionExpression.setTextContent("Process_Start");
                    sequenceFlow3.setConditionExpression(conditionExpression);
                    process.addChildElement(sequenceFlow3);

                    break;
                }
            }
        }
    }


    // Endevent einfach nach alle Aktivitäten oder Gateways ohne ausgehende flüsse
    private static void endEventGenerating(BpmnModelInstance modelInstance) {
        for (MyActivity activity : activities) {
            for (MyFlow flow : flows) {
                if (activity.equals(flow.getHead())) {
                    break;
                }
                if (flow.equals(flows.getLast())) {
                    String lastActivityID = "ID-" + activity.getSentenceID() + "-" + activity.getTokenID();

                    FlowNode sourceElement = (FlowNode) modelInstance.getModelElementById(lastActivityID);
                    Process process = findProcessByElement(modelInstance, sourceElement);

                    EndEvent endEvent = modelInstance.newInstance(EndEvent.class);
                    endEvent.setId("endEvent-" + lastActivityID);
                    process.addChildElement(endEvent);
    
                    SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                    FlowNode targetElement = (FlowNode) modelInstance.getModelElementById("endEvent-" + lastActivityID);
                    sequenceFlow.setId("Flow-" + sourceElement.getId() + "-to-" + targetElement.getId());
                    process.addChildElement(sequenceFlow);
                    connect(sequenceFlow, sourceElement, targetElement);
                    
                    break;
                }
            }
        }

        for (MyGateway gateway : gateways) {
            for (MyFlow flow : flows) {
                if (gateway.equals(flow.getHead())) {
                    break;
                }
                if (flow.equals(flows.getLast())) {
                    String lastGatewayID = "ID-" + gateway.getSentenceID() + "-" + gateway.getTokenID();

                    FlowNode sourceElement = (FlowNode) modelInstance.getModelElementById(lastGatewayID);
                    Process process = findProcessByElement(modelInstance, sourceElement);

                    EndEvent endEvent = modelInstance.newInstance(EndEvent.class);
                    endEvent.setId("endEvent-" + lastGatewayID);
                    process.addChildElement(endEvent);
    
                    SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                    FlowNode targetElement = (FlowNode) modelInstance.getModelElementById("endEvent-" + lastGatewayID);
                    sequenceFlow.setId("Flow-" + sourceElement.getId() + "-to-" + targetElement.getId());
                    process.addChildElement(sequenceFlow);
                    connect(sequenceFlow, sourceElement, targetElement);

                    break;
                }
            }
        }
    } 

    
    // herausfinden, ob Aktivität in bestimmten Prozess ist
    private static boolean isActivityInProcess(BpmnModelInstance modelInstance, Process process, String id) {
        Collection<Activity> activities = process.getChildElementsByType(Activity.class);
        for (Activity activity : activities) {
            String activityId = activity.getId();
            if (activityId.equals(id)) {
                return true;
            }
        }
        return false;
    }


    // zum verbinden aller Prozessteile zu einem mithilfe des supporting tasks und Gateways
    // -> In einem Pool müssen alle Elemente nämlich irgendwie zusammenhägen als ein ganzer Prozess
    // -> diese einzelnen Elemente kommen nämlich sonst zu Stande, da immer wieder zwischen den Pools gewechselt wird
    // -> Dabei sind die einzelenen Elemente sonst nicht in einem Pool unbedingt zusammenhängend
    private static void connectProcessParts(BpmnModelInstance modelInstance) {
        Collection<Process> processes = modelInstance.getModelElementsByType(Process.class);
        for (Process process : processes) {
            String supportID = "supportingTask-" + process.getId();
            boolean supportingTaskInProcess = isActivityInProcess(modelInstance, process, supportID);

            if (!supportingTaskInProcess) {
                Task task = modelInstance.newInstance(Task.class);
                task.setId("supportingTask-" + process.getId());
                task.setName("supportingTask");
                process.addChildElement(task);

                ExclusiveGateway gateway = modelInstance.newInstance(ExclusiveGateway.class);
                gateway.setId("supportingTask_Gateway-" + process.getId());
                process.addChildElement(gateway);

                SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                sequenceFlow.setId("Flow-" + task.getId() + "-to-" + gateway.getId());
                process.addChildElement(sequenceFlow);
                connect(sequenceFlow, task, gateway);
            }
            FlowNode supportingGateway = (FlowNode) modelInstance.getModelElementById("supportingTask_Gateway-" + process.getId());
            FlowNode supportingTask = (FlowNode) modelInstance.getModelElementById("supportingTask-" + process.getId());

            Collection<FlowNode> flowNodes = process.getChildElementsByType(FlowNode.class);
            for (FlowNode flowNode : flowNodes) {
                if ((flowNode.getIncoming().isEmpty()) && ((flowNode instanceof Activity) || (flowNode instanceof Gateway)) && !(flowNode == supportingTask)) {
                    SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                    process.addChildElement(sequenceFlow);
                    connect(sequenceFlow, supportingGateway, flowNode);

                    ConditionExpression conditionExpression = modelInstance.newInstance(ConditionExpression.class);
                    sequenceFlow.setConditionExpression(conditionExpression);
                    process.addChildElement(sequenceFlow);
                }
            }
        }
    }
}
