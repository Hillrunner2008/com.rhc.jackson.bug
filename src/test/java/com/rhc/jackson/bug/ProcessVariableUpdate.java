package com.rhc.jackson.bug;

import com.rhc.jackson.bug.model.MultipleChoiceQuestion;
import com.rhc.jackson.bug.model.Question;
import com.rhc.jackson.bug.model.Questionnaire;
import com.rhc.jackson.bug.model.YesOrNoQuestion;
import org.junit.Assert;
import org.junit.Test;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;

/**
 *
 * @author dcnorris
 */
public class ProcessVariableUpdate {

    @Test
    public void createAndUpdateProcessVariable() {
        KieServicesConfiguration configuration = KieServicesFactory
                .newRestConfiguration("http://localhost:8180/kie-server/services/rest/server", "admin", "admin");
        configuration.setMarshallingFormat(MarshallingFormat.JSON);
        KieServicesClient client = KieServicesFactory.newKieServicesClient(configuration);
        ProcessServicesClient processClient = client.getServicesClient(ProcessServicesClient.class);
        KieContainerResource kieContainer = getKieContainer(client);
        final String containerId = kieContainer.getContainerId();
        Long processInstancdId = processClient.startProcess(containerId, "ScriptTask");
        Questionnaire originalQuestionnaire = processClient.getProcessInstanceVariable(containerId, processInstancdId, "questionnaire", Questionnaire.class);
        Questionnaire q = getQuestionaire();
        processClient.setProcessVariable(containerId, processInstancdId, "questionnaire", q);
        Questionnaire updatedQuestionnaire = processClient.getProcessInstanceVariable(containerId, processInstancdId, "questionnaire", Questionnaire.class);
        //confirm the updates
        Assert.assertEquals(q.getName(), updatedQuestionnaire.getName());
    }

    private Questionnaire getQuestionaire() {
        final Questionnaire q = new Questionnaire("Test Questionnaire");
        Question yesOrNoQuestion = new YesOrNoQuestion();
        yesOrNoQuestion.setDisplayText("YesOrNoQuestion Example");
        Question multipleChoice = new MultipleChoiceQuestion();
        multipleChoice.setDisplayText("Multiple Choice Example");
        multipleChoice.getPossibleAnswers().add("possible answer 1");
        multipleChoice.getPossibleAnswers().add("possible answer 2");
        multipleChoice.getPossibleAnswers().add("possible answer 3");
        q.getQuestions().add(yesOrNoQuestion);
        q.getQuestions().add(multipleChoice);
        return q;
    }

    private KieContainerResource getKieContainer(KieServicesClient client) {
        KieContainerResourceFilter filter = new KieContainerResourceFilter.Builder()
                .releaseId("com.rhc", "com.rhc.jackson.bug", "1.0.0")
                .status(KieContainerStatus.STARTED)
                .build();
        // if it blows up here that is fine, no need to handle this in a unit test
        return client.listContainers(filter).getResult().getContainers().stream()
                .findFirst().get();
    }
}
