package com.rhc.jackson.bug.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class Question<T> implements Serializable {

    private static final long serialVersionUID = -5256292846928691054L;
    protected String displayText;
    protected List<T> possibleAnswers;
    protected T answer;

    public Question() {
        possibleAnswers = new ArrayList<>();
    }

    public Question( String displayText ) {
        this( displayText, new ArrayList<T>() );
    }

    public Question( String displayText, List<T> arrayList ) {
        this.displayText = displayText;
        this.possibleAnswers = arrayList;
        this.answer = null;
    }

    public String answerAsText() {
        return answer.toString();
    }

    @JsonIgnore
    public boolean isAnswered() {
        return answer != null;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText( String displayText ) {
        this.displayText = displayText;
    }

    public List<T> getPossibleAnswers() {
        return possibleAnswers;
    }

    public void setPossibleAnswer( List<T> possibleAnswers ) {
        this.possibleAnswers = possibleAnswers;
    }

    public T getAnswer() {
        return answer;
    }

    public void setAnswer( T answer ) {
        this.answer = answer;
    }

    @JsonIgnore
    public String getAnswerAsText() {
        String val = answer != null ? answer.toString() : "";
        return val;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode( this.displayText );
        hash = 37 * hash + Objects.hashCode( this.possibleAnswers );
        hash = 37 * hash + Objects.hashCode( this.answer );
        return hash;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final Question<?> other = (Question<?>) obj;
        if ( !Objects.equals( this.displayText, other.displayText ) ) {
            return false;
        }
        if ( !Objects.equals( this.possibleAnswers, other.possibleAnswers ) ) {
            return false;
        }
        if ( !Objects.equals( this.answer, other.answer ) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Question{" + "displayText=" + displayText + ", possibleAnswers=" + possibleAnswers + ", answer=" + answer + '}';
    }

}