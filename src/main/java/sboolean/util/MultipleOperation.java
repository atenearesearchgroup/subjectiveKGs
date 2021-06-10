/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sboolean.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;

import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.logging.FormattedLogProvider;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import org.neo4j.procedure.UserFunction;

import sboolean.core.SBoolean;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import sboolean.core.UBoolean;


/**
 *
 * @author fjnavarrete
 */
public class MultipleOperation {
    
     
    @Context
    public Log log;
    
/*    @UserFunction
    @Description("sboolean.core.foldSBooleanAnd([r1,r2,...], 'personId') - performs the foldSBooleanAnd of the views given by a reviewer of a list of relationships.")
    public String foldSBooleanAnd1(
            @Name("relations") List<Relationship> relations,
            @Name("personId") String personId){
        if (relations == null ) throw new IllegalArgumentException("");
        
        SBoolean result = null;    
        String res = "";
        for (Relationship relation : relations) {
            SBoolean temp = null;
            ArrayList<String> persons =  new ArrayList<String>(
                    Arrays.asList((String[]) relation.getProperty("p_opinion")));
            double[] belief = (double[]) relation.getProperty("belief");
            double[] disbelief = (double[]) relation.getProperty("disbelief");
            double[] uncertainty = (double[]) relation.getProperty("uncertainty");
            double baseRate = (double) relation.getProperty("probability");
            
            
            int position = persons.indexOf(personId);
            if (position != -1) {
                temp = new SBoolean(belief[position],
                                    disbelief[position], 
                                    uncertainty[position], 
                                    baseRate);
            }
            else 
                throw new IllegalArgumentException("Person identifier not found: " + personId );
            
            result = (result == null) ? temp : result.and(temp);
        }
        return  "{project: " + String.valueOf(result.projection()) + ", sboolean: (" + 
                String.valueOf(result.belief()) + "," +
                String.valueOf(result.disbelief()) + "," +
                String.valueOf(result.uncertainty()) + "," +
                String.valueOf(result.baseRate()) + ")}";

    }*/
    
    
    @UserFunction
    @Description("sboolean.core.foldSBooleanAnd2([r1,r2,...], [p1,p2,...]) - performs the foldSBooleanAnd of the views given by a reviewer of a list of relationships.")
    public String foldSBooleanAnd(
            @Name("relations") List<Relationship> relations,
            @Name("agents") List<String> agents){
        if (relations == null ) throw new IllegalArgumentException("");
        
        List<SBoolean> acc = null;    
        for (Relationship relation : relations) {
            List<SBoolean> temp = new ArrayList();
            ArrayList<String> persons =  new ArrayList<String>(
                    Arrays.asList((String[]) relation.getProperty("p_opinion")));
            double[] belief = (double[]) relation.getProperty("belief");
            double[] disbelief = (double[]) relation.getProperty("disbelief");
            double[] uncertainty = (double[]) relation.getProperty("uncertainty");
            double baseRate = (double) relation.getProperty("probability");
            
            
            for(String agent : agents){
                int position = persons.indexOf(agent);
                if (position != -1) {
                    temp.add(new SBoolean(belief[position],
                                        disbelief[position], 
                                        uncertainty[position], 
                                        baseRate));
                }
                else 
                    temp.add(new SBoolean(new UBoolean(baseRate))); //temp.add(new SBoolean(new UBoolean(baseRate)));
            }
            
            
            if (acc != null) { 
                List<SBoolean> andAcc = temp;
                temp = new ArrayList();
                for ( String agent : agents ){                    
                    int index = agents.indexOf(agent);                                       
                    temp.add(acc.get(index).and(andAcc.get(index)));
                    log.info(agent + "[" + index + "] ::" + acc.get(index) + " and " + andAcc.get(index) + " = " + temp.get(index));
                }
            }
            acc = temp;
        }
        String result = "";
        for (String agent : agents){
            int index = agents.indexOf(agent);
            result = result + "{project: " + String.valueOf(acc.get(index).projection()) + ", sboolean: (" + 
                                String.valueOf(acc.get(index).belief()) + "," +
                                String.valueOf(acc.get(index).disbelief()) + "," +
                                String.valueOf(acc.get(index).uncertainty()) + "," +
                                String.valueOf(acc.get(index).baseRate()) + ", agent: " + agent + ")}";
        }
        return  result;

    }
    
    
    
       
    
    @Procedure(value = "sboolean.util.unfoldCategoryRelations")
    @Description("sboolean.util.unfoldCategoryRelations([n1,n2,...], [r1,r2,...], num_branches, criterion) - unfold Category relations of nodes into relationships of a path. Num_branches is the max of IsGeneralizatons branches to explore, 1 or 2 or * for all. Criterion is the selction criterion to explore BESTPROBABILTY or DEFAULT")
    public Stream<UnfoldCategoryRelations> unfoldCategoryRelations(
            @Name("nodes") List<Node> nodes, 
            @Name("relations") List<Relationship> relations,
            @Name("num_branch") String branchs,
            @Name("criterion") String criterion
            ){
//        long startTime = System.nanoTime();
//        log.info("Initial Time: " + startTime);
        if (nodes == null ) throw new IllegalArgumentException("Nodes is Null");
        if (relations == null ) throw new IllegalArgumentException("Relations is Null");
        if (branchs == null ) throw new IllegalArgumentException("Branchs is Null");
        if (criterion == null ) throw new IllegalArgumentException("Criterion is Null");
        if (!(criterion.equals("DEFAULT")||criterion.equals("BESTPROBABILTY"))) throw new IllegalArgumentException("Criterion");
        
        List<UnfoldCategoryRelations> results = new ArrayList();    
        results.add(new UnfoldCategoryRelations(relations, new ArrayList()));
        
        int pos_extend = 0;
        for(Node node : nodes){            
            List<Relationship> isGeneralizationList = new ArrayList<>(); 
            List<UnfoldCategoryRelations> temp = new ArrayList();
            node.getRelationships(Direction.INCOMING).iterator()
                    .forEachRemaining(rel -> 
                            AddIsGeneralization(isGeneralizationList,rel,criterion));  
//            log.info("Node Size: " + isGeneralizationList.size() + " Node: " + ((String) node.getProperty("entityId")));
            int b = (branchNumber(branchs)> isGeneralizationList.size())?isGeneralizationList.size():branchNumber(branchs);
            for (UnfoldCategoryRelations partial : results){
                for (Relationship relCategory : isGeneralizationList.subList(0, b)){
                    List<Relationship> rtemp = new ArrayList(partial.unfoldrelation);
                    List<Node> ntemp = new ArrayList(partial.categorynodes);                    
                    rtemp.add(pos_extend, relCategory);
                    ntemp.add(relCategory.getStartNode());
                    temp.add(new UnfoldCategoryRelations(rtemp,ntemp));                    
                }
            }
            results = temp;
            pos_extend = isGeneralizationList.size() > 0 ? pos_extend +2 : pos_extend + 1; 
           
        }
//        long endTime = System.nanoTime();
//        long elapsedTime = endTime - startTime;
//        log.info("Elapased Time in milliseconds: " + elapsedTime / 1000000);
//        log.info("Elapased Time in seconds: " + elapsedTime / 1000000000);
        return results.stream();
    }
    
    
    @Procedure(value = "sboolean.util.addSBooleanOpinion" , mode = Mode.WRITE)
    @Description("sboolean.util.addSBooleanOpinion(relation, agent, opinion_projection, uncertainty) - add subjetive opinion to a relationship")
    public Stream<SBooleanOpinion> addSBooleanOpinion(             
            @Name("relation") Relationship relation,
            @Name("agent") String agent,
            @Name("opinion_projection") double projection,
            @Name("uncertainty") double uncertainty
            ){
   
        if (relation == null ) throw new IllegalArgumentException("Relation is Null");
        if (agent == null ) throw new IllegalArgumentException("Relation is Null");
        
        SBooleanOpinion result = new SBooleanOpinion(relation, "");
        double baseRate = ((Double) relation.getProperty("probability")).doubleValue();
        double[] beliefs = new double[] {};
        double[] disbeliefs = new double[] {};
        double[] uncertainties = new double[] {};
             
        Object [] persons = (Object[]) relation.getProperty("p_opinion");
        List<String> lagents = new ArrayList<String>();
        if (persons.length > 0){
            lagents = new ArrayList<String>(
                    Arrays.asList((String []) relation.getProperty("p_opinion")));            
            beliefs = (double[]) relation.getProperty("belief");
            disbeliefs = (double[]) relation.getProperty("disbelief");
            uncertainties = (double[]) relation.getProperty("uncertainty");
            
        }
        
        if (!lagents.contains(agent)){
            lagents.add(agent);
            List<Double> lbeliefs = new ArrayList<Double>(
                        Arrays.asList(ArrayUtils.toObject(beliefs)));
            List<Double> ldisbeliefs = new ArrayList<Double>(
                        Arrays.asList(ArrayUtils.toObject(disbeliefs)));
            List<Double> luncertainties = new ArrayList<Double>(
                        Arrays.asList(ArrayUtils.toObject(uncertainties)));
            log.info("Agent:"+ agent +  " Sboolean(" + projection + "," + uncertainty + ","+ baseRate +")");
            SBoolean temp = new SBoolean(projection, uncertainty, baseRate);
            
//            lbeliefs.add(new Double(projection - baseRate * uncertainty)) ;
//            ldisbeliefs.add(new Double(1- (projection - baseRate * uncertainty) - uncertainty));
//            luncertainties.add(new Double( uncertainty));
            
            lbeliefs.add(new Double(temp.belief())) ;
            ldisbeliefs.add(new Double(temp.disbelief()));
            luncertainties.add(new Double(temp.uncertainty()));
            
            
            result.opinion = "{project: " + String.valueOf(projection) + ", sboolean: (" + 
                String.valueOf(lbeliefs.get(lagents.size()-1)) + "," +
                String.valueOf(ldisbeliefs.get(lagents.size()-1)) + "," +
                String.valueOf(luncertainties.get(lagents.size()-1)) + "," +
                String.valueOf(baseRate) + ")}";
            relation.setProperty("p_opinion", lagents.toArray(new String[lagents.size()]));
            relation.setProperty("belief", lbeliefs.stream().mapToDouble(Double::doubleValue).toArray());
            relation.setProperty("disbelief", ldisbeliefs.stream().mapToDouble(Double::doubleValue).toArray());
            relation.setProperty("uncertainty", luncertainties.stream().mapToDouble(Double::doubleValue).toArray());
            
        }        
        return Stream.of(result);
    }
    
    /*@Procedure(value = "sboolean.util.averageBeliefFusion" )
    @Description("sboolean.util.averageBeliefFusion(relation, [a1, a2, ...]) - Average Believe Fusion of a relation and agent opinions")
    public Stream<SBooleanOpinion> averageBeliefFusion(             
            @Name("relation") Relationship relation,
            @Name("agents") List<String> agents
            ){
   
        if (relation == null ) throw new IllegalArgumentException("Relation is Null");
        if (agents == null ) throw new IllegalArgumentException("Agent is Null");
        
        SBooleanOpinion result = new SBooleanOpinion(relation, "");
        double baseRate = ((Double) relation.getProperty("probability")).doubleValue();
        double[] beliefs = new double[] {};
        double[] disbeliefs = new double[] {};
        double[] uncertainties = new double[] {};
             
        Object [] persons = (Object[]) relation.getProperty("p_opinion");
        List<String> lagents = new ArrayList<String>();
        if (persons.length > 0){
            lagents = new ArrayList<String>(
                    Arrays.asList((String []) relation.getProperty("p_opinion")));            
            beliefs = (double[]) relation.getProperty("belief");
            disbeliefs = (double[]) relation.getProperty("disbelief");
            uncertainties = (double[]) relation.getProperty("uncertainty");
            
        }
        
        List<SBoolean> list = new ArrayList();
        int i = 0;
        for (String agent : agents) {
            if (lagents.contains(agent)){
                int index = lagents.indexOf(agent);
                log.info(agent + "_index::" + index );
                list.add( new SBoolean(  beliefs[index],
                                        disbeliefs[index],
                                        uncertainties[index],
                                        baseRate));
            } else
                list.add(new SBoolean(new UBoolean(baseRate)));
            log.info(agent +"::"+list.get(i).toString());
            i++;
    }
    SBoolean sResult = SBoolean.averageBeliefFusion(list);
    result.opinion = "{project: " + String.valueOf(sResult.projection()) + ", sboolean: (" + 
                    String.valueOf(sResult.belief()) + "," +
                    String.valueOf(sResult.disbelief()) + "," +
                    String.valueOf(sResult.uncertainty()) + "," +
                    String.valueOf(baseRate) + ")}";
        return Stream.of(result);
    }*/
    
    @Procedure(value = "sboolean.util.foldAverageBeliefFusion")
    @Description("sboolean.util.foldAverageBeliefFusion([r1, r2, .....], [a1, a2, ...]) - Average Believe Fusion of a relation and agent opinions")
    public Stream<SBooleanOpinionListRelation> foldAverageBeliefFusion(             
            @Name("relation") List<Relationship> relations,
            @Name("agents") List<String> agents
            ){
   
        if (relations == null ) throw new IllegalArgumentException("Relations is Null");
        if (agents == null ) throw new IllegalArgumentException("Agents is Null");
        
        List<SBoolean> acc = null;
        for (Relationship relation : relations){
            List<SBoolean> list = new ArrayList(); //SBooleanOpinion(relation, "");
            
            //Instance Sboolea parameters of agent
            double baseRate = ((Double) relation.getProperty("probability")).doubleValue();
            double[] beliefs = new double[] {};
            double[] disbeliefs = new double[] {};
            double[] uncertainties = new double[] {};
            Object [] persons = (Object[]) relation.getProperty("p_opinion");
            List<String> lagents = new ArrayList<String>();
            if (persons.length > 0){
                lagents = new ArrayList<String>(
                        Arrays.asList((String []) relation.getProperty("p_opinion")));            
                beliefs = (double[]) relation.getProperty("belief");
                disbeliefs = (double[]) relation.getProperty("disbelief");
                uncertainties = (double[]) relation.getProperty("uncertainty");

            }

            for (String agent : agents) {
                if (lagents.contains(agent)){
                    int index = lagents.indexOf(agent);
                    list.add( new SBoolean(  beliefs[index],
                                            disbeliefs[index],
                                            uncertainties[index],
                                            baseRate));
                } else
                    list.add(new SBoolean(new UBoolean(baseRate))); //list.add(new SBoolean(new UBoolean(baseRate)));
            }  
            
            if (acc != null) { 
                List<SBoolean> andAcc = list;
                list = new ArrayList();
                for ( String agent : agents ){                    
                    int index = agents.indexOf(agent);                                       
                    list.add(acc.get(index).and(andAcc.get(index)));
                    log.info(agent + "[" + index + "] ::" + acc.get(index) + " and " + andAcc.get(index) + " = " + list.get(index));
                }
            }
            acc = list;
            /*if (acc != null)          
                for ( SBoolean aOpinion : list ){
                    int index = list.indexOf(aOpinion);
                    aOpinion = acc.get(index).and(aOpinion);
                }
            acc = list; */
        }
        
        SBoolean sResult = SBoolean.averageBeliefFusion(acc);
        SBooleanOpinionListRelation res = new SBooleanOpinionListRelation(relations, "");
        res.opinion = "{project: " + String.valueOf(sResult.projection()) + ", sboolean: (" + 
                        String.valueOf(sResult.belief()) + "," +
                        String.valueOf(sResult.disbelief()) + "," +
                        String.valueOf(sResult.uncertainty()) + "," +
                        String.valueOf(sResult.baseRate()) + ")}";
        return Stream.of(res);
    }
    

    private void AddIsGeneralization(List<Relationship> list, Relationship relationship, String criterion){
        if (relationship.getType().name().equals("IsGeneralization") && !list.contains(relationship))
            if (criterion.equals("DEFAULT"))
                    list.add(relationship);
            else
                addbyprobabiltysort(list, relationship);
    }
    
    
    
    private void addbyprobabiltysort(List<Relationship> l, Relationship r) {        
        if (l.size() != 0 ){
            int pos = -1;
            Double p = (Double) r.getProperty("probability");
//            log.info("probabilty rel: " + p + " Class: " + ((String)r.getStartNode().getProperty("entityId")));
            for(Relationship e: l){
                Double p2 = (Double) e.getProperty("probability");
                if ( p.doubleValue() > p2.doubleValue()){
                    pos = l.indexOf(e);
                    break;
                }               
            }
            if (pos<0)
                l.add(l.size(),r);
            else
                l.add(pos,r);            
        }
        else {
            l.add(r);
            Double p = (Double) r.getProperty("probability");
//            log.info("probabilty rel: " + p + " Class: " + ((String)r.getStartNode().getProperty("entityId")));
        }
        
    }
    
    private int branchNumber(String branches) {
        int b;
        if (branches.equals("*"))
            b=Integer.MAX_VALUE;
        else
            try {
                b = Integer.parseInt(branches);               
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Branchs");
            }        
        return b;
    }
    
    public static class RelationshipList {
        // These records contain a lists of relationships.
        public List<Relationship> output;
        

        public RelationshipList(List<Relationship> output) {
            this.output = output;
        }
    }
    
    public static class SBooleanOpinion {
        // These records contain a lists of relationships.
        
        public Relationship relation;
        public String opinion;

        

        public SBooleanOpinion(
                Relationship relation, 
                String opinion) {
            this.relation = relation;
            this.opinion = opinion;
        }
    }
    
    public static class SBooleanOpinionListRelation {
        // These records contain a lists of relationships.
        
        public List<Relationship> relation;
        public String opinion;

        

        public SBooleanOpinionListRelation(
                List<Relationship> relation, 
                String opinion) {
            this.relation = relation;
            this.opinion = opinion;
        }
    }
    
    public static class UnfoldCategoryRelations {
        // These records contain a lists of relationships.
        
        public List<Relationship> unfoldrelation;
        public List<Node> categorynodes;

        

        public UnfoldCategoryRelations(
                List<Relationship> relations, 
                List<Node> categorynodes) {
            this.unfoldrelation = relations;
            this.categorynodes = categorynodes;
        }
    }
    
    public static class StringList {
        // These records contain a lists of relationships.
        public List<String> output;
        

        public StringList(List<String> output) {
            this.output = output;
        }
    }
   
}
