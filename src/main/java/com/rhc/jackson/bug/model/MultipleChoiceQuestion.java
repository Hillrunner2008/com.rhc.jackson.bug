package com.rhc.jackson.bug.model;

import java.util.List;

public class MultipleChoiceQuestion extends Question<String> { 

    private static final long serialVersionUID = -5256292846928691054L;
    
    public MultipleChoiceQuestion() {
    }

    public MultipleChoiceQuestion( String displayText, List<String> possibleAnswers ) {
        super( displayText, possibleAnswers );
    }

    @Override
    public String answerAsText() {
        String val = this.getAnswer() == null ? "" : this.getAnswer();
        return val;
    }


}
