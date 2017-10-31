package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import entities.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pegah on 9/24/17.
 */
public class Evaluator {

    private boolean quoteConsideration;
    private String taggedRelationFile;
    private String taggedProfileFile;

    private String generatedProfileFile;
    private String generatedRelationFile;
    private String taggedQuoteFile;

    public Evaluator(boolean quoteConsideration, String taggedRelationFile, String taggedProfileFile, String generatedProfileFile, String generatedRelationFile, String taggedQuoteFile) {
        this.quoteConsideration = quoteConsideration;
        this.taggedProfileFile = taggedProfileFile;
        this.taggedRelationFile = taggedRelationFile;
        this.generatedProfileFile = generatedProfileFile;
        this.generatedRelationFile = generatedRelationFile;
        this.taggedQuoteFile = taggedQuoteFile;
    }

    public String strongCompareResults(boolean readFromAddress) {

        List<Profile> predictedProfiles = readResult(generatedProfileFile, readFromAddress);
        Relation[] predictedRelations = readRelationResult(generatedRelationFile, readFromAddress);
        RelationEvaluate[] groundTruthRelations = readGroundTruth_relations(taggedRelationFile, readFromAddress);
        ProfileEvaluator[] groundTruthProfiles = readGroundTruth(taggedProfileFile, readFromAddress);
        QuoteEvaluator[] groundTruthQuotes = readQuoteGroundTruth(taggedQuoteFile, readFromAddress);
        List<String> TP_adj = new ArrayList<>();
        List<String> FP_adj = new ArrayList<>();
        List<String> FN_adj = new ArrayList<>();

        List<String> TP_verb = new ArrayList<>();
        List<String> FP_verb = new ArrayList<>();
        List<String> FN_verb = new ArrayList<>();


        List<String> TP_loc = new ArrayList<>();
        List<String> FP_loc = new ArrayList<>();
        List<String> FN_loc = new ArrayList<>();

        List<String> TP_sentiment = new ArrayList<>();
        List<String> FP_sentiment = new ArrayList<>();
        List<String> FN_sentiment = new ArrayList<>();

        List<String> TP_character = new ArrayList<>();
        List<String> FP_character = new ArrayList<>();
        List<String> FN_character = new ArrayList<>();

        List<String> TP_quote = new ArrayList<>();
        List<String> FP_quote = new ArrayList<>();
        List<String> FN_quote = new ArrayList<>();


        for (ProfileEvaluator groundTruthProfile : groundTruthProfiles) {
            boolean isThereCharacter = false;
            for (Profile predictedProfile : predictedProfiles) {
                if (groundTruthProfile.getName().contains(predictedProfile.getName()) ||
                        predictedProfile.getName().contains(groundTruthProfile.getName())) {

                    isThereCharacter = true;
                    //Adjective/////////////////////////////////////////////////////////
                    for (String predictedAdj : predictedProfile.getAdjs().keySet()) {
                        predictedAdj = predictedAdj.toLowerCase().trim();
                        boolean isthere = false;
                        for (String groundAdj : groundTruthProfile.getAdj()) {
                            groundAdj = groundAdj.toLowerCase().trim();
                            if (groundAdj.contains("'") && !quoteConsideration) continue;
                            if (groundAdj.contains(predictedAdj) || predictedAdj.contains(groundAdj))
                                isthere = true;
                        }
                        if (isthere)
                            TP_adj.add(predictedAdj);
                        else
                            FP_adj.add(predictedAdj);
                    }
                    for (String groundAdj : groundTruthProfile.getAdj()) {
                        groundAdj = groundAdj.toLowerCase().trim();
                        if (groundAdj.contains("'") && !quoteConsideration) continue;
                        boolean isthere = false;
                        for (String predictedAdj : predictedProfile.getAdjs().keySet()) {
                            predictedAdj = predictedAdj.toLowerCase().trim();
                            if (groundAdj.contains(predictedAdj) || predictedAdj.contains(groundAdj))
                                isthere = true;
                        }
                        if (!isthere)
                            FN_adj.add(groundAdj);
                    }
                    //Verb////////////////////////////////////////////////////////
                    for (String predictedVerb : predictedProfile.getVerbs().keySet()) {
                        predictedVerb = predictedVerb.toLowerCase().trim();
                        boolean isthere = false;
                        for (String groundVerb : groundTruthProfile.getVerb()) {
                            groundVerb = groundVerb.toLowerCase().trim();
                            if (groundVerb.contains("'") && !quoteConsideration) continue;
                            if (groundVerb.contains(predictedVerb) || predictedVerb.contains(groundVerb))
                                isthere = true;
                        }
                        if (isthere)
                            TP_verb.add(predictedVerb);
                        else
                            FP_verb.add(predictedVerb);
                    }
                    for (String groundVerb : groundTruthProfile.getVerb()) {
                        groundVerb = groundVerb.toLowerCase().trim();
                        if (groundVerb.contains("'") && !quoteConsideration) continue;
                        boolean isthere = false;
                        for (String predictedVerb : predictedProfile.getVerbs().keySet()) {
                            predictedVerb = predictedVerb.toLowerCase().trim();
                            if (groundVerb.contains(predictedVerb) || predictedVerb.contains(groundVerb))
                                isthere = true;
                        }
                        if (!isthere)
                            FN_verb.add(groundVerb);
                    }
                    //Location////////////////////////////////////////////////////////
                    for (String predictedLocation : predictedProfile.getLocations()) {
                        predictedLocation = predictedLocation.toLowerCase().trim();
                        boolean isthere = false;
                        for (String groundLoc : groundTruthProfile.getLocation()) {
                            groundLoc = groundLoc.toLowerCase().trim();
                            if (groundLoc.contains("'") && !quoteConsideration) continue;
                            if (groundLoc.contains(predictedLocation) || predictedLocation.contains(groundLoc))
                                isthere = true;
                        }
                        if (isthere)
                            TP_loc.add(predictedLocation);
                        else
                            FP_loc.add(predictedLocation);
                    }
                    for (String groundLoc : groundTruthProfile.getLocation()) {
                        groundLoc = groundLoc.toLowerCase().trim();
                        if (groundLoc.contains("'") && !quoteConsideration) continue;
                        boolean isthere = false;
                        for (String predictedLoc : predictedProfile.getLocations()) {
                            predictedLoc = predictedLoc.toLowerCase().trim();
                            if (groundLoc.contains(predictedLoc) || predictedLoc.contains(groundLoc))
                                isthere = true;
                        }
                        if (!isthere)
                            FN_loc.add(groundLoc);
                    }
                    //Sentiment/////////////////////////////////////////////////////
                    int nCount = predictedProfile.getNegativeSentimentCount();
                    int pCount = predictedProfile.getPositiveSentimentCount();
                    int vPCount = predictedProfile.getVeryPositiveSentimentCount();
                    int vNCount = predictedProfile.getVeryNegativeSentimentCount();
                    int max = 0;
                    String sentimentClass = "";
                    if (nCount > pCount) {
                        max = nCount;
                        sentimentClass = "negative";
                    } else {
                        max = pCount;
                        sentimentClass = "positive";
                    }

                    if (max < vPCount) {
                        max = pCount;
                        sentimentClass = "very positive";
                    }
                    if (max < vNCount) {
                        sentimentClass = "very negative";
                    }
                    if (groundTruthProfile.getSentiment().equals(sentimentClass)) {
                        TP_sentiment.add(sentimentClass);
                    } else {
                        FN_sentiment.add(sentimentClass);
                    }
                    ///////////////////////////////////////////////////////////
                }
            }
            if (isThereCharacter)
                TP_character.add(groundTruthProfile.getName());
            else
                FN_character.add(groundTruthProfile.getName());

        }

        for (Profile predictedProfile : predictedProfiles) {
            boolean isThereCharacter = false;
            for (ProfileEvaluator groundTruthProfile : groundTruthProfiles) {
                if (groundTruthProfile.getName().contains(predictedProfile.getName()) ||
                        predictedProfile.getName().contains(groundTruthProfile.getName())) {
                    isThereCharacter = true;
                }
            }
            if (!isThereCharacter)
                FP_character.add(predictedProfile.getName());
        }


        int quoteCount = 0;
        int maxQuoteCount = 0;
        for (Profile predictedProfile : predictedProfiles) {
            if (predictedProfile.getQuote() == null || predictedProfile.getQuote().trim().isEmpty()) continue;
            quoteCount += predictedProfile.getQuote().trim().split(" ").length;
            if (predictedProfile.getQuote().trim().split(" ").length > maxQuoteCount) {
                maxQuoteCount = predictedProfile.getQuote().trim().split(" ").length;
            }

        }

        //QUOTE///////////////////////////////////////////////////
        for (Profile predictedProfile : predictedProfiles) {
            for (QuoteEvaluator quoteEval : groundTruthQuotes) {
                if (quoteEval.getName().trim().toLowerCase().contains(predictedProfile.getName().toLowerCase().trim())
                        || predictedProfile.getName().trim().toLowerCase().contains((quoteEval.getName().trim().toLowerCase()))) {
                    if (predictedProfile.getQuote() != null) {
                        String[] preficateQuotes = predictedProfile.getQuote().split(" \n ");
                        for (String predictedQuote : preficateQuotes) {
                            boolean is_there = false;
                            for (String taggedQuote : quoteEval.getQuotes()) {
                                if (taggedQuote.trim().contains(predictedQuote.trim())
                                        || predictedQuote.trim().contains(taggedQuote.trim()) ||
                                        (predictedQuote.trim().split(" ").length == taggedQuote.trim().split(" ").length))
                                {
                                    is_there = true;
                                }
                            }
                            if (is_there) {
                                TP_quote.add(predictedQuote);
                            } else {
                                FP_quote.add(predictedQuote);
                            }
                        }

                        for (String taggedQuote : quoteEval.getQuotes()) {
                            boolean is_there = false;
                            if (predictedProfile.getQuote() != null) {
                                for (String predicateQuote : predictedProfile.getQuote().split(" \n ")) {
                                    if (taggedQuote.trim().contains(predicateQuote.trim())
                                            || predicateQuote.trim().contains(taggedQuote.trim()) ||
                                            (predicateQuote.trim().split(" ").length == taggedQuote.trim().split(" ").length)) {
                                        is_there = true;
                                    }
                                }
                            }
                            if (is_there) {
//                                    /*TP_quote.add(taggedQuote);*/ //dont want to add duplicate TP
                            } else {
                                FN_quote.add(taggedQuote);
                            }
                        }

                    }
                }
            }}
        StringBuilder sb=new StringBuilder();

        //////////////////////////////////////////////////////////
        sb.append("Quote length average per character: " + quoteCount / predictedProfiles.size());
        sb.append("\n");
        sb.append("Quote max length for a character: " + maxQuoteCount);
        sb.append("\n");
        sb.append("\n");

        float recall_character = Float.parseFloat(String.valueOf(TP_character.size())) / (Float.parseFloat(String.valueOf(TP_character.size())) + Float.parseFloat(String.valueOf(FN_character.size())));
        float precision_character = Float.parseFloat(String.valueOf(TP_character.size())) / (Float.parseFloat(String.valueOf(TP_character.size())) + Float.parseFloat(String.valueOf(FP_character.size())));
        float fMeasure_character = (2 * (recall_character * precision_character)) / (recall_character + precision_character);
        sb.append("recall for character: " + recall_character);
        sb.append("\n");
        sb.append("precision for character: " + precision_character);
        sb.append("\n");
        sb.append("fMeasure for character: " + fMeasure_character);
        sb.append("\n");
        sb.append("\n");

        float recall_adj = Float.parseFloat(String.valueOf(TP_adj.size())) / (Float.parseFloat(String.valueOf(TP_adj.size())) + Float.parseFloat(String.valueOf(FN_adj.size())));
        float precision_adj = Float.parseFloat(String.valueOf(TP_adj.size())) / (Float.parseFloat(String.valueOf(TP_adj.size())) + Float.parseFloat(String.valueOf(FP_adj.size())));
        float fMeasure_adj = (2 * (recall_adj * precision_adj)) / (recall_adj + precision_adj);
        sb.append("recall for adjectives: " + recall_adj);
        sb.append("\n");
        sb.append("precision for adjectives: " + precision_adj);
        sb.append("\n");
        sb.append("fMeasure for adjectives: " + fMeasure_adj);
        sb.append("\n");
        sb.append("\n");

        float recall_verb = Float.parseFloat(String.valueOf(TP_verb.size())) / (Float.parseFloat(String.valueOf(TP_verb.size())) + Float.parseFloat(String.valueOf(FN_verb.size())));
        float precision_verb = Float.parseFloat(String.valueOf(TP_verb.size())) / (Float.parseFloat(String.valueOf(TP_verb.size())) + Float.parseFloat(String.valueOf(FP_verb.size())));
        float fMeasure_verb = (2 * (recall_verb * precision_verb)) / (recall_verb + precision_verb);
        sb.append("recall for verbs: " + recall_verb);
        sb.append("\n");
        sb.append("precision for verbs: " + precision_verb);
        sb.append("\n");
        sb.append("fMeasure for verbs: " + fMeasure_verb);
        sb.append("\n");
        sb.append("\n");

        float recall_loc = Float.parseFloat(String.valueOf(TP_loc.size())) / (Float.parseFloat(String.valueOf(TP_loc.size())) + Float.parseFloat(String.valueOf(FN_loc.size())));
        float precision_loc = Float.parseFloat(String.valueOf(TP_loc.size())) / (Float.parseFloat(String.valueOf(TP_loc.size())) + Float.parseFloat(String.valueOf(FP_loc.size())));
        float fMeasure_loc = (2 * (recall_loc * precision_loc)) / (recall_loc + precision_loc);
        sb.append("recall for location: " + recall_loc);
        sb.append("\n");
        sb.append("precision for location: " + precision_loc);
        sb.append("\n");
        sb.append("fMeasure for location: " + fMeasure_loc);
        sb.append("\n");
        sb.append("\n");

        float recall_sentiment = Float.parseFloat(String.valueOf(TP_sentiment.size())) / (Float.parseFloat(String.valueOf(TP_sentiment.size())) + Float.parseFloat(String.valueOf(FN_sentiment.size())));
        float precision_sentiment = Float.parseFloat(String.valueOf(TP_sentiment.size())) / (Float.parseFloat(String.valueOf(TP_sentiment.size())) + Float.parseFloat(String.valueOf(FP_sentiment.size())));
        float fMeasure_sentiment = (2 * (recall_sentiment * precision_sentiment)) / (recall_sentiment + precision_sentiment);
        sb.append("recall for sentiment: " + recall_sentiment);
        sb.append("\n");
        sb.append("precision for sentiment: " + precision_sentiment);
        sb.append("\n");
        sb.append("fMeasure for sentiment: " + fMeasure_sentiment);
        sb.append("\n");
        sb.append("\n");

        float recall_quote = Float.parseFloat(String.valueOf(TP_quote.size())) / (Float.parseFloat(String.valueOf(TP_quote.size())) + Float.parseFloat(String.valueOf(FN_quote.size())));
        float precision_quote = Float.parseFloat(String.valueOf(TP_quote.size())) / (Float.parseFloat(String.valueOf(TP_quote.size())) + Float.parseFloat(String.valueOf(FP_quote.size())));
        float fMeasure_quote = (2 * (recall_quote * precision_quote)) / (recall_quote + precision_quote);
        sb.append("recall for quote: " + recall_quote);
        sb.append("\n");
        sb.append("precision for quote: " + precision_quote);
        sb.append("\n");
        sb.append("fMeasure for quote: " + fMeasure_quote);
        sb.append("\n");
        sb.append("\n");


        //Relation/////////////////////////////////////////////////////
        if(predictedRelations != null && groundTruthRelations != null)
        sb.append(evaluateRelation(predictedRelations, groundTruthRelations));

        return sb.toString();
    }

    private List<Profile> readResult(String content, boolean isFileAddress) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Profile[] arrProfiles;
            if(isFileAddress)
                arrProfiles = mapper.readValue(new File(content), Profile[].class);
            else
                arrProfiles = mapper.readValue(content, Profile[].class);
            List<Profile> profiles = new ArrayList<>();
            for (Profile profile : arrProfiles) {
                profiles.add(profile);
            }
            return profiles;
        } catch (Exception ex) {
            System.out.print("ERROR IN convertFromJson: " + ex.getMessage());
            return null;
        }
    }

    private Relation[] readRelationResult(String content, boolean isFileAddress) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Relation[] arrRelations;
            if(isFileAddress)
                arrRelations = mapper.readValue(new File(content), Relation[].class);
            else
                arrRelations = mapper.readValue(content, Relation[].class);
            return arrRelations;
        } catch (Exception ex) {
            System.out.print("ERROR IN convertFromJson: " + ex.getMessage());
            return null;
        }
    }

    private ProfileEvaluator[] readGroundTruth(String content, boolean isFileAddress) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ProfileEvaluator[] arrProfiles;
            if(isFileAddress)
                arrProfiles = mapper.readValue(new File(content), ProfileEvaluator[].class);
            else
                arrProfiles = mapper.readValue(content, ProfileEvaluator[].class);
            return arrProfiles;
        } catch (Exception ex) {
            System.out.print("ERROR IN convertFromJson: " + ex.getMessage());
            return null;
        }
    }

    private QuoteEvaluator[] readQuoteGroundTruth(String content, boolean isFileAddress) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            QuoteEvaluator[] arrProfiles;
            if(isFileAddress)
                arrProfiles = mapper.readValue(new File(content), QuoteEvaluator[].class);
            else
                arrProfiles = mapper.readValue(content, QuoteEvaluator[].class);
            return arrProfiles;
        } catch (Exception ex) {
            System.out.print("ERROR IN convertFromJson: " + ex.getMessage());
            return null;
        }
    }

    private RelationEvaluate[] readGroundTruth_relations(String content, boolean isFileAddress) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            RelationEvaluate[] arrRelations ;
            if(isFileAddress)
                arrRelations = mapper.readValue(new File(content), RelationEvaluate[].class);
            else
                arrRelations = mapper.readValue(content, RelationEvaluate[].class);
            return arrRelations;
        } catch (Exception ex) {
            System.out.print("ERROR IN convertFromJson: " + ex.getMessage());
            return null;
        }
    }

    private String evaluateRelation(Relation[] predictedRelations, RelationEvaluate[] groundTruthRelation) {
        List<String> TP_rel = new ArrayList<>();
        List<String> FP_rel = new ArrayList<>();
        List<String> FN_rel = new ArrayList<>();


        for (Relation relation : predictedRelations) {
            String[] predictedProfileNames = relation.getProfileNames();
            for (RelationEvaluate grounftruthRel : groundTruthRelation) {
                String[] groundTruthProfileNames = grounftruthRel.getProfileNames();
                Arrays.sort(predictedProfileNames);
                Arrays.sort(groundTruthProfileNames);
                if (Arrays.equals(predictedProfileNames, groundTruthProfileNames)) {
                    for (String predictedTypeName : relation.getTypeName().keySet()) {
                        boolean isThere = false;
                        predictedTypeName = predictedTypeName.toLowerCase().trim();
                        for (String GroundTypeName : grounftruthRel.getTypeNames()) {
                            GroundTypeName = GroundTypeName.trim().toLowerCase();
                            if (GroundTypeName.equals(predictedTypeName)) isThere = true;
                        }
                        if (isThere)
                            TP_rel.add(predictedTypeName);
                        else
                            FP_rel.add(predictedTypeName);
                    }

                    for (String GroundTypeName : grounftruthRel.getTypeNames()) {
                        boolean isThere = false;
                        GroundTypeName = GroundTypeName.toLowerCase().trim();
                        for (String predictedTypeName : relation.getTypeName().keySet()) {
                            predictedTypeName = predictedTypeName.toLowerCase().trim();
                            if (GroundTypeName.equals(predictedTypeName)) isThere = true;

                        }
                        if (!isThere)
                            FN_rel.add(GroundTypeName);
                    }
                }


            }
        }

        StringBuilder sb = new StringBuilder();
        float recall_rel = Float.parseFloat(String.valueOf(TP_rel.size())) / (Float.parseFloat(String.valueOf(TP_rel.size())) + Float.parseFloat(String.valueOf(FN_rel.size())));
        float precision_rel = Float.parseFloat(String.valueOf(TP_rel.size())) / (Float.parseFloat(String.valueOf(TP_rel.size())) + Float.parseFloat(String.valueOf(FP_rel.size())));
        float fMeasure_rel = (2 * (recall_rel * precision_rel)) / (recall_rel + precision_rel);
        sb.append("recall for relation: " + recall_rel);
        sb.append("\n");
        sb.append("precision for relation: " + precision_rel);
        sb.append("\n");
        sb.append("fMeasure for relation: " + fMeasure_rel);
        sb.append("\n");
        return sb.toString();
    }

    private String readFile(String inputFileAddress) {
        try {
            Path ph = Paths.get(inputFileAddress);
            String content = new String(Files.readAllBytes(ph));
            return content;
        } catch (Exception ex) {
            System.out.print("ERROR IN readFile for interview mode:" + ex.getMessage());
            return "";
        }

    }
}
