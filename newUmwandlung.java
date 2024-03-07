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
    private List<MyDataObject> usedDataObjects;

    // Konstruktor
    public MyActivity(Integer sentenceID, Integer tokenID, String label, MyActor performer) {
        super(sentenceID, tokenID);
        this.label = label;
        this.performer = performer;
        this.usedDataObjects = new ArrayList<>();
    }

    // Konstruktor
    public MyActivity(Integer sentenceID, Integer tokenID, String label) {
        super(sentenceID, tokenID);
        this.label = label;
        this.usedDataObjects = new ArrayList<>();
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


    public void addUsedDataObject(MyDataObject dataObject) {
        usedDataObjects.add(dataObject);
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



class MyGateway extends MyFlowNode {
    private String type;

    // Konstruktor
    public MyGateway(String type, Integer sentenceID, Integer tokenID) {
        super(sentenceID, tokenID);
        this.type = type;
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

        // Ich muss hier noch den filePath automatisch machen, dass ich nicht händisch den Dukumentennamen setzen muss
        String filePath = "jsonFiles/doc-5_1.json";

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File jsonFile = new File(filePath);
            JsonNode rootNode = objectMapper.readTree(jsonFile);
            JsonNode relationsNode = rootNode.get("doc-5_1").get(0).get("relations");
            JsonNode sentenceIDsNode = rootNode.get("doc-5_1").get(0).get("sentence-IDs");
            JsonNode ner_tagsNode = rootNode.get("doc-5_1").get(0).get("ner_tags");
            JsonNode tokenIDsNode = rootNode.get("doc-5_1").get(0).get("token-IDs");
            JsonNode tokensNode = rootNode.get("doc-5_1").get(0).get("tokens");

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

                            MyActivity activity = new MyActivity(activitySentenceID, activityTokenID, activityLabel, performer);
                            activities.add(activity);

                            break;
                        }
                        position++;
                    }
                }
            }

            // restliche Aktivitäten einfügen, die nicht in Actor Performer sind
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

            // Gateways und Flows einfügen
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

                            if (nerTag.equals("B-XOR Gateway")) {
                                String type = "xor";
                                MyGateway existingGateway = getGatewayByID(flowInSentenceID, flowInTokenID);

                                if (existingGateway == null) {
                                    MyGateway gateway = new MyGateway(type, flowInSentenceID, flowInTokenID);
                                    gateways.add(gateway);
                                }
                            }
    
                            if (nerTag.equals("B-AND Gateway")) {
                                String type = "and";
                                MyGateway existingGateway = getGatewayByID(flowInSentenceID, flowInTokenID);

                                if (existingGateway == null) {
                                    MyGateway gateway = new MyGateway(type, flowInSentenceID, flowInTokenID);
                                    gateways.add(gateway);
                                }
                            }
                            break;
                        }
                        position++; 
                    }

                    MyActivity existingOutActivity = getActivityBySIDANDTID(flowOutSentenceID, flowOutTokenID);
                    MyGateway existingOutGateway = getGatewayByID(flowOutSentenceID, flowOutTokenID);

                    MyActivity existingInActivity = getActivityBySIDANDTID(flowInSentenceID, flowInTokenID);
                    MyGateway existingInGateway = getGatewayByID(flowInSentenceID, flowInTokenID);

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

                                for (JsonNode relationNodeCond : relationsNode) {
                                    String relationTypeCond = relationNodeCond.get(2).asText();

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
                                            }

                                            if (existingOutActivity != null && existingInGateway != null) {
                                                MyFlow flow = new MyFlow(existingOutActivity, existingInGateway, conditionLabel);
                                                flows.add(flow);
                                            }
                                            
                                            if (existingOutGateway != null && existingInActivity != null) {
                                                MyFlow flow = new MyFlow(existingOutGateway, existingInActivity, conditionLabel);
                                                flows.add(flow);
                                            }

                                            if (existingOutGateway != null && existingInGateway != null) {
                                                MyFlow flow = new MyFlow(existingOutGateway, existingInGateway, conditionLabel);
                                                flows.add(flow);
                                            }
                                        }
                                    }
                                }
                            }
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

        Process process = modelInstance.newInstance(Process.class);
        process.setId("process");
        definitions.addChildElement(process);

        LaneSet laneSet = modelInstance.newInstance(LaneSet.class);
        process.addChildElement(laneSet);

        for (MyActor actor : actors) {
            actorGenerating(actor, modelInstance, laneSet);
        }

        for (MyDataObject dataObject : dataObjects) {
            dataObjectGenerating(dataObject, modelInstance, process);
        }

        for (MyActivity activity : activities) {
            activityGenerating(activity, modelInstance, process);
        }

        for (MyGateway gateway : gateways) {
            gatewayGenerating(gateway, modelInstance, process);
        }

        for (MyFlow flow : flows) {
            flowGenerating(flow, modelInstance, process);
        }

        startEventGenerating(modelInstance, process);

        endEventGenerating(modelInstance, process);

        



        try {
            Bpmn.validateModel(modelInstance);
            File file = new File("doc-5_1.bpmn.xml");
            file.createNewFile();
    
            String bpmnString = Bpmn.convertToString(modelInstance);
            System.out.println("bpmnString");
            System.out.println(bpmnString);
    
            Bpmn.writeModelToFile(file, modelInstance);
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




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

        return actorLabel;
    }




    private static MyActor getActorByName(String actorName) {
        for (MyActor actor : actors) {
            if (actor.getName().equals(actorName)) {
                return actor;
            }
        }
        return null;
    }




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

        return lemmatizedSentence.toString().trim();
    }



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

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                // POS-Tags überprüfen und entfernen 
                if (pos.equals("DT") || pos.equals("IN")) {
                    dataObjectLabel = dataObjectLabel.replace(word, "").trim();
                }
            }
        }

        return dataObjectLabel;
    }



    private static MyDataObject getDataObjectByName(String dataObjectLabel) {
        for (MyDataObject dataObject : dataObjects) {
            if (dataObject.getLabel().equals(dataObjectLabel)) {
                return dataObject;
            }
        }
        return null;
    }



    private static MyActivity getActivityBySIDANDTID(int sentenceID, int tokenID) {
        for (MyActivity activity : activities) {
            if (activity.getSentenceID().equals(sentenceID) && activity.getTokenID().equals(tokenID)) {
                return activity;
            }
        }
        return null;
    }

    

    private static MyGateway getGatewayByID(int sentenceID, int tokenID) {
        for (MyGateway gateway : gateways) {
            if (gateway.getSentenceID().equals(sentenceID) && gateway.getTokenID().equals(tokenID)) {
                return gateway;
            }
        }
        return null;
    }
    


    private static void actorGenerating(MyActor actor, BpmnModelInstance modelInstance, LaneSet laneSet) {
        Lane lane = modelInstance.newInstance(Lane.class);
        lane.setId(actor.getName());
        lane.setName(actor.getName());
        laneSet.addChildElement(lane);
    }



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

        return conditionLabel;
    }



    private static void dataObjectGenerating(MyDataObject myDataObject, BpmnModelInstance modelInstance, Process process) {
        DataObject dataObject = modelInstance.newInstance(DataObject.class);
        dataObject.setId(myDataObject.getLabel());
        dataObject.setName(myDataObject.getLabel());
        process.addChildElement(dataObject);

        DataObjectReference dataObjectReference = modelInstance.newInstance(DataObjectReference.class);
        dataObjectReference.setDataObject(dataObject);
        dataObjectReference.setId(myDataObject.getLabel() + "-Reference");
        process.addChildElement(dataObjectReference);
    }



    private static void activityGenerating(MyActivity activity, BpmnModelInstance modelInstance, Process process) {
        UserTask userTask = modelInstance.newInstance(UserTask.class);
        userTask.setId("ID-" + activity.getSentenceID() + "-" + activity.getTokenID());
        userTask.setName(activity.getLabel());
        process.addChildElement(userTask);

        for (MyDataObject data : activity.getUsedDataObjects()) {
            String dataObjectReferenceId = data.getLabel() + "-Reference";
            DataObjectReference dataObjectReference = getDataObjectReferenceById(modelInstance, dataObjectReferenceId);
            
            DataInputAssociation association = modelInstance.newInstance(DataInputAssociation.class);
            association.setTarget(dataObjectReference);
            userTask.addChildElement(association);
        }
    }



    public static DataObjectReference getDataObjectReferenceById(BpmnModelInstance modelInstance, String dataObjectReferenceId) {
        Collection<DataObjectReference> dataObjectReferences = modelInstance.getModelElementsByType(DataObjectReference.class);
    
        for (DataObjectReference dataObjectReference : dataObjectReferences) {
            if (dataObjectReferenceId.equals(dataObjectReference.getId())) {
                return dataObjectReference;
            }
        }
    
        return null;
    }



    private static void gatewayGenerating(MyGateway mygateway, BpmnModelInstance modelInstance, Process process) {
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



    private static void flowGenerating(MyFlow flow, BpmnModelInstance modelInstance, Process process) {
        if (flow.getCondition() == null) {
            String headID = "ID-" + flow.getHead().getSentenceID() + "-" + flow.getHead().getTokenID();
            String tailID = "ID-" + flow.getTail().getSentenceID() + "-" + flow.getTail().getTokenID();

            SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
            sequenceFlow.setSource(modelInstance.getModelElementById(headID));
            sequenceFlow.setTarget(modelInstance.getModelElementById(tailID));
            process.addChildElement(sequenceFlow);
        }

        if (!(flow.getCondition() == null)) {
            String headID = "ID-" + flow.getHead().getSentenceID() + "-" + flow.getHead().getTokenID();
            String tailID = "ID-" + flow.getTail().getSentenceID() + "-" + flow.getTail().getTokenID();

            SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
            sequenceFlow.setSource(modelInstance.getModelElementById(headID));
            sequenceFlow.setTarget(modelInstance.getModelElementById(tailID));

            ConditionExpression conditionExpression = modelInstance.newInstance(ConditionExpression.class);
            conditionExpression.setTextContent(flow.getCondition());

            sequenceFlow.setConditionExpression(conditionExpression);

            process.addChildElement(sequenceFlow);
        }
    }



    private static void startEventGenerating(BpmnModelInstance modelInstance, Process process) {
        for (MyActivity activity : activities) {
            for (MyFlow flow : flows) {
                if (activity.equals(flow.getTail())) {
                    break;
                }
                if (flow.equals(flows.getLast())) {
                    String firstActivityID = "ID-" + activity.getSentenceID() + "-" + activity.getTokenID();

                    StartEvent startEvent = modelInstance.newInstance(StartEvent.class);
                    startEvent.setId(firstActivityID + "start");
                    process.addChildElement(startEvent);

                    SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                    sequenceFlow.setSource(modelInstance.getModelElementById(firstActivityID + "start"));
                    sequenceFlow.setTarget(modelInstance.getModelElementById(firstActivityID));
                    process.addChildElement(sequenceFlow);

                    break;
                }
            }
        }
    }



    private static void endEventGenerating(BpmnModelInstance modelInstance, Process process) {
        for (MyActivity activity : activities) {
            for (MyFlow flow : flows) {
                if (activity.equals(flow.getHead())) {
                    break;
                }
                if (flow.equals(flows.getLast())) {
                    String lastActivityID = "ID-" + activity.getSentenceID() + "-" + activity.getTokenID();

                    EndEvent endEvent = modelInstance.newInstance(EndEvent.class);
                    endEvent.setId("endEvent-" + lastActivityID);
                    process.addChildElement(endEvent);
    
                    SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                    sequenceFlow.setSource(modelInstance.getModelElementById(lastActivityID));
                    sequenceFlow.setTarget(modelInstance.getModelElementById("endEvent-" + lastActivityID));
                    process.addChildElement(sequenceFlow);

                    break;
                }
            }
        }
    } 
}
