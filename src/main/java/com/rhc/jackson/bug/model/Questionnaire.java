package com.rhc.jackson.bug.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Questionnaire implements Serializable {

    private static final long serialVersionUID = -5256292846928691054L;
    private String name;
    private List<Question> questions = new ArrayList<Question>();
    
    public Questionnaire(){
        
    }
    
    public Questionnaire( String name ) {
        this.name = name;

    }

    public void addQuestion( Question q ) {
        this.questions.add( q );
    }

    public List<Question> getUnansweredQuestions() {
        return questions.stream().filter( question -> question.isAnswered() == false ).collect( Collectors.toList() );
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public Question getQuestion( String questionText ) {
        for ( Question q : questions ) {
            if ( q.getDisplayText().equals( questionText ) ) {
                return q;
            }
        }
        return null;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions( List<Question> questions ) {
        this.questions = questions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        result = prime * result + ( ( questions == null ) ? 0 : questions.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Questionnaire other = (Questionnaire) obj;
        if ( name == null ) {
            if ( other.name != null )
                return false;
        }
        else if ( !name.equals( other.name ) )
            return false;
        if ( questions == null ) {
            if ( other.questions != null )
                return false;
        }
        else if ( !questions.equals( other.questions ) )
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Questionnaire [name=" + name + ", questions=" + questions + "]";
    }

}