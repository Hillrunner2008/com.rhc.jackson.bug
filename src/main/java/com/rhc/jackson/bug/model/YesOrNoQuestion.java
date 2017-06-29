package com.rhc.jackson.bug.model;

import java.util.Arrays;


public class YesOrNoQuestion extends Question<String> {

    private static final long serialVersionUID = -5256292846928691054L;
    
    public YesOrNoQuestion() {
        possibleAnswers = Arrays.asList("Yes", "No");
    }
    
    public YesOrNoQuestion( String displayText ) {
        super(displayText, Arrays.asList("Yes", "No"));
    }

    @Override
    public String answerAsText() {
        String val = this.getAnswer() == null ? "" : this.getAnswer();
        return val;
    }

}
