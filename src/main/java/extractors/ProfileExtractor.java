package extractors;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.tokensregex.SequenceMatchRules;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import entities.Profile;
import entities.Relation;

import java.util.*;

public class ProfileExtractor {

    public List<Profile> extract(String content, boolean useQuote, boolean profileContainsCheck, boolean removeProfilesWitoutInfo , boolean removeProfilesWithFreq1) {
        try {

            if (!useQuote) {
                String quoteRegex = "(\"(.*?)\"|\'(.*?)\'|(``(.*)'')|(''(.*)'')|(``(.*)``)|(`(.*)`)|(`(.*)')|('(.*)`))";
                content = content.replaceAll(quoteRegex, "");
            }
            List<Profile> extractedProfiles = profileExtraction(content, profileContainsCheck);
            Set<Profile> invalidProfiles = new HashSet<>();
            for (Profile profile : extractedProfiles) {

                if (removeProfilesWithFreq1 && profile.getFrequency() <= 1) {
                    invalidProfiles.add(profile);
                }
                if (removeProfilesWitoutInfo && profile.getVerbs().size() == 0 && profile.getAdjs().size() == 0) {
                    invalidProfiles.add(profile);
                }
            }
            extractedProfiles.removeAll(invalidProfiles);
            return extractedProfiles;

        } catch (Exception ex) {
            System.out.print(ex.getMessage());
            return new ArrayList<>();
        }
    }

    public void addPropertiesToCurrentProfiles(List<Profile> profiles, String content){
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos,lemma, ner, parse,sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String PARAGRAPH_SPLIT_REGEX = "\n\n\n\n";

        String[] paragraphs = content.split(PARAGRAPH_SPLIT_REGEX);
        //for timeline extract purpose we want to break between paragraphs
        for (String paragraph : paragraphs) {
        Annotation annotation = pipeline.process(paragraph);
        for (CoreMap sentence:annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            List<CoreLabel> coreLabels = sentence.get(CoreAnnotations.TokensAnnotation.class);
            for (Profile profile:profiles) {
                if(sentence.toString().toLowerCase().contains(profile.getName().toLowerCase())){
                    addLocation(profile, coreLabels);
                    addTime(profile, coreLabels);
                    addSentiment(sentence, profile);
                    SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
                    addAjd(dependencies, profile, profile.getName());
//                    addRoles(dependencies, profile, sentence, profile.getName());
                    addVerb(dependencies, profile, profile.getName());
                }
            }}
        }
    }

    private List<Profile> profileExtraction(String content, boolean profileContainsCheck) {
        List<Profile> profiles = new ArrayList<>();
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos,lemma, parse,sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        CRFClassifier<CoreLabel> classifier = CRFClassifier.getDefaultClassifier();

        List<List<CoreLabel>> classify = classifier.classify(content);
        String namedEntity = "";
        for (List<CoreLabel> coreLabels : classify) {
            for (int i = 0; i < coreLabels.size(); i++) {
                CoreLabel coreLabel = coreLabels.get(i);
                namedEntity = "";
                String category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
                if (category.equals("PERSON") || category.equals("ORGANIZATION")) {

                    // prevent part of a name to be another name.
                    // for example in equalcheck mode we would have
                    //2 names : Donald Trump, Trump
                    if(namedEntity.contains(coreLabel.word()))continue;
                    boolean canAddRole = category.equals("PERSON") ? true : false;
                    while (category.equals("PERSON") || category.equals("ORGANIZATION")) {
                        namedEntity += " " + coreLabel.word();
                        coreLabel = coreLabels.get(++i);
                        category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
                    }

                    Profile profile = new Profile();
                    profile.setName(namedEntity);

                    boolean existed = false;
                    // it it sth else rather than profile name. we profile name, keeps its first
                    //occurrence name, because it is almost always the most complete one
                    //but we prefer to use the short name to capture all the occurrences in tag ans
                    //dependency tree and ..
                    String currentName = namedEntity.trim();
                    for (Profile definedProfile : profiles) {
                        // the reason of using two directional contains instead of equality:
                        //for example Arthur comes first to story. then Arthur becomes a king and we have
                        //King arthur. Or vice versa. for example one person is president Obama and after
                        //some lines his period is finished and he is called Obama
                        String definedProfileClean = definedProfile.getName().toLowerCase().trim();
                        String namedEntityClean = namedEntity.trim().toLowerCase();
                        if((profileContainsCheck && (definedProfileClean.contains(namedEntityClean) || namedEntityClean.contains(definedProfileClean)))
                                ||
                            (!profileContainsCheck  && definedProfileClean.equals(namedEntityClean))){
                            profile = definedProfile;
                            profile.addFrequency();
                            existed = true;
                        }
                    }

                    if (!existed)
                        profiles.add(profile);


                    //sentiment detection of the sentence containing this person
                    StringBuilder sb = new StringBuilder();
                    for (CoreLabel label : coreLabels) {
                        sb.append(label.word());
                        sb.append(" ");
                    }
                    Annotation annotation = pipeline.process(sb.toString());
                    for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                        addLocation(profile, coreLabels);
                        addTime(profile, coreLabels);
                        addSentiment(sentence, profile);
                        SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
                        if(canAddRole)
                            addRoles(dependencies, profile, sentence, currentName);
                        addAjd(dependencies, profile, currentName);
                        addVerb(dependencies, profile, currentName);

                    }

                }
            }
        }
        pipeline = null;
        System.gc();
        return profiles;
    }

    private void addRoles(SemanticGraph dependencies, Profile profile, CoreMap sentence, String currentProfileName) {
if(sentence.toString().equals("All was going well for King Kae Kaus when Afrasiyab received a letter from Gazdam , the care-taker of the fort of Sapid , which was on the border between Iran and Turan .")){
    String f = "";
}
        try {
            List<SemanticGraphEdge> edges = new ArrayList<>();
            for (String profileNamePart : currentProfileName.split(" ")) {
                IndexedWord profileName = dependencies.getNodeByWordPattern(profileNamePart);
                edges.addAll(dependencies.getOutEdgesSorted(profileName));
                edges.addAll(dependencies.getIncomingEdgesSorted(profileName));
            }

            List<CoreLabel> coreLabels = sentence.get(CoreAnnotations.TokensAnnotation.class);
            String firstWord = currentProfileName.split(" ")[0];
            String lastWord = currentProfileName.split(" ")[currentProfileName.split(" ").length - 1];
            int indexOfFirstWord = 0;
            int indexOfLastWord = coreLabels.size() -1;
            for (int i = 0; i < coreLabels.size(); i++) {
                CoreLabel coreLabel = coreLabels.get(i);
                if (coreLabel.word().equals(firstWord)) {
                    indexOfFirstWord = i;
                }
                if (coreLabel.word().equals(lastWord)) {
                    indexOfLastWord = i;
                }
            }
            //find the nouns exatl before Person as his role
            String role = "";
            if(indexOfFirstWord > 0){
            int counter = indexOfFirstWord - 1;
            CoreLabel coreLabel = coreLabels.get(counter);
            String pos_category = coreLabel.tag();
                while (pos_category.startsWith("NN") || pos_category.startsWith("DT")
                        || pos_category.startsWith(",") ) {
                if(pos_category.startsWith("NN")){
                    for (SemanticGraphEdge edge:edges) {
                    if(!edge.getRelation().getShortName().equals("nmod:poss") && (edge.getSource().word().equals(coreLabel.word()) || edge.getTarget().word().equals(coreLabel.word()))){
                        role += " " + coreLabel.word();
                        break;
                    }
                }}
                if (counter > 0) {
                    coreLabel = coreLabels.get(--counter);
                    pos_category = coreLabel.tag();
                } else {
                    pos_category = "o";
                }

            }
            if (role != "")
                profile.addToAdjs(role.trim(), false);
            }
            if(indexOfLastWord < coreLabels.size() -1) {
                role = "";
                int counter = indexOfLastWord + 1;
                CoreLabel coreLabel = coreLabels.get(counter);
                String pos_category = coreLabel.tag();
                while (pos_category.startsWith("NN") || pos_category.startsWith("DT")
                        || pos_category.startsWith(",") || (pos_category.startsWith("VB")&&coreLabel.lemma().equals("be"))) {
                    if(pos_category.startsWith("NN")){
                    for (SemanticGraphEdge edge:edges) {
                        if(!edge.getRelation().getShortName().equals("nmod:poss") && (edge.getSource().word().equals(coreLabel.word()) || edge.getTarget().word().equals(coreLabel.word()))){
                            role += " " + coreLabel.word();
                            break;
                        }
                    }}
                    if (counter < coreLabels.size() - 1) {
                        coreLabel = coreLabels.get(++counter);
                        pos_category = coreLabel.tag();
                    } else {
                        pos_category = "o";
                    }
                }
                if (role != "")
                    profile.addToAdjs(role.trim(), false);
            }
        }
        catch(Exception ex){
            System.out.println("Error in adding role:");
            System.out.println(ex.getMessage());
            System.out.println("---------------------");
        }
    }

    private void addLocation(Profile profile, List<CoreLabel> coreLabels) {
        for (Iterator<CoreLabel> iter = coreLabels.iterator(); iter.hasNext(); ) {
            CoreLabel coreLabel = iter.next();
            String category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
            if(category == null)
            category = coreLabel.ner();
            String location = "";
            while (category.equals("LOCATION")) {
                location += " " + coreLabel.word();
                if (iter.hasNext()) {
                    coreLabel = iter.next();
                    category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
                    if(category == null)
                        category = coreLabel.ner();
                } else {
                    category = "0";
                }
            }
            if (location.trim() != "") {
                profile.addLocation(location.trim());
            }
        }
        /*for (CoreLabel coreLabel : coreLabels) {
            String category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
            if (category.equals("LOCATION")) {
                profile.addLocation(coreLabel.word());
            }
        }*/
    }

    private void addTime(Profile profile, List<CoreLabel> coreLabels) {
        for (Iterator<CoreLabel> iter = coreLabels.iterator(); iter.hasNext(); ) {
            CoreLabel coreLabel = iter.next();
            String category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
            if(category == null)
                category = coreLabel.ner();
            String temporal = "";
            while (category.equals("DATE") || category.equals("TIME")) {
                temporal += " " + coreLabel.word();
                if (iter.hasNext()) {
                    coreLabel = iter.next();
                    category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
                    if(category == null)
                        category = coreLabel.ner();
                } else {
                    category = "0";
                }
            }
            if (temporal.trim() != "") {
                profile.addTemporal(temporal.trim());
            }
        }
    }

    private void addSentiment(CoreMap sentence, Profile profile) {
        //Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
        //int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
        String sentimentClassName = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
        profile.addToSentiment(sentimentClassName);
    }

    private void addAjd(SemanticGraph dependencies, Profile profile, String currentProfileName) {

        try {
            //for example: reformist Ali-Akbar Rafsanjani . here reformist is an adjective for Rafsanji not Ali-Akbar
            for (String profileNamePart : currentProfileName.split(" ")) {
                //old Merlin
                //old and clever Merlin, old clever Merlin
                IndexedWord profileName = dependencies.getNodeByWordPattern(profileNamePart);
                List<SemanticGraphEdge> outgoingEdges = dependencies.getOutEdgesSorted(profileName);
                List<SemanticGraphEdge> incomingEdges = dependencies.getIncomingEdgesSorted(profileName);
                for (SemanticGraphEdge edge : outgoingEdges) {
                    if (edge.getTarget().tag().startsWith("JJ")) {
                        boolean isNegtive = false;
                        List<SemanticGraphEdge> outgoingEdgesFromJJ = dependencies.getOutEdgesSorted(edge.getTarget());
                        for (SemanticGraphEdge edgeFromJJ : outgoingEdgesFromJJ) {
                            if ((edgeFromJJ.getRelation().getShortName().equals("conj") ||
                                    edgeFromJJ.getRelation().getShortName().equals("amod") ||
                                    edgeFromJJ.getRelation().getShortName().equals("punct")) &&
                                    edgeFromJJ.getTarget().tag().startsWith("JJ")) {
                                profile.addToAdjs(edgeFromJJ.getTarget().word(),false);
                            }
                            if(edgeFromJJ.getRelation().getShortName().equals("neg")){
                                isNegtive = true;
                            }
                        }
                        profile.addToAdjs(edge.getTarget().word(), isNegtive);
                    }
                }

                //Merlin was an old and clever Merlin, old clever man
                //Merlin was a good man
                for (SemanticGraphEdge edge : incomingEdges) {
                    if (edge.getRelation().toString().equals("nsubj")) {
                        if ((edge.getSource().tag().equals("NN") || edge.getSource().tag().equals("NNP"))) {
//                        && (!edge.getSource().ner().equals("LOCATION") && !edge.getSource().ner().equals("DATE") && !edge.getSource().ner().equals("TIME"))) {
                            IndexedWord NN = edge.getSource();
                            List<SemanticGraphEdge> edgesOutFromNN = dependencies.getOutEdgesSorted(NN);
                            for (SemanticGraphEdge outedge : edgesOutFromNN) {
                                if (outedge.getTarget().tag().startsWith("JJ")) {
                                    boolean isNegtive = false;
                                    List<SemanticGraphEdge> outgoingEdgesFromJJ = dependencies.getOutEdgesSorted(edge.getTarget());
                                    for (SemanticGraphEdge edgeFromJJ : outgoingEdgesFromJJ) {
                                        if ((edgeFromJJ.getRelation().getShortName().equals("conj") ||
                                                edgeFromJJ.getRelation().getShortName().equals("amod") ||
                                                edgeFromJJ.getRelation().getShortName().equals("punct")) &&
                                                edgeFromJJ.getTarget().tag().startsWith("JJ")) {
                                            profile.addToAdjs(edgeFromJJ.getTarget().word(), false);
                                        }
                                        if(edgeFromJJ.getRelation().getShortName().equals("neg")){
                                            isNegtive = true;
                                        }
                                    }
                                    profile.addToAdjs(outedge.getTarget().word() + " " + edge.getSource().word(), isNegtive);
                                }
                            }
                        }
                        //Arthur was strong// Arthur was strong and young //Arthur was strong, young
                        if (edge.getSource().tag().contains("JJ") || edge.getSource().tag().contains("RB")) {
                            boolean isNegative = false;
                            List<SemanticGraphEdge> outgoingEdgesFromJJ = dependencies.getOutEdgesSorted(edge.getSource());
                            for (SemanticGraphEdge edgeFromJJ : outgoingEdgesFromJJ) {
                                if ((edgeFromJJ.getRelation().getShortName().equals("conj")
                                        ||
                                        edgeFromJJ.getRelation().toString().equals("amod")
                                        || edgeFromJJ.getRelation().toString().equals("punct")) &&
                                        edgeFromJJ.getTarget().tag().startsWith("JJ")) {//Merlin was an old and clever Merlin, old clever man
                                    profile.addToAdjs(edgeFromJJ.getTarget().word(), false);
                                }
                                if(edgeFromJJ.getRelation().getShortName().equals("neg")){
                                    isNegative = true;
                                }
                            }
                            profile.addToAdjs(edge.getSource().word(), isNegative);
                        }
                    }
                }
            }
        }
        catch(Exception ex){
            System.out.println("Error in adding adjectives:");
            System.out.println(ex.getMessage());
            System.out.println("----------------------------");
        }
    }

    private void addVerb(SemanticGraph dependencies, Profile profile, String currentProfileName) {
        IndexedWord profileName = dependencies.getNodeByWordPattern(currentProfileName);
        List<SemanticGraphEdge> incomingEdges = dependencies.getIncomingEdgesSorted(profileName);
        for (SemanticGraphEdge edge : incomingEdges) {
            if (edge.getSource().tag().contains("VB") && edge.getRelation().toString().equals("nsubj")) {
                boolean isNegative = false;
                String normalizedVerb = edge.getSource().lemma();
                List<SemanticGraphEdge> outComingEdgesFromVerb = dependencies.getOutEdgesSorted(edge.getSource());
                for (SemanticGraphEdge fromVerb: outComingEdgesFromVerb) {
                    if(fromVerb.getRelation().getShortName().equals("neg")){
                        isNegative = true;
                    }
                    if(fromVerb.getRelation().getShortName().equals("compound:prt")){
                        normalizedVerb = normalizedVerb + " " + fromVerb.getTarget().word();
                    }
                }
                profile.addToVerbs(normalizedVerb, isNegative);
            }
        }
    }

}
