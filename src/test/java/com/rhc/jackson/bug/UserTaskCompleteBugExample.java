package com.rhc.jackson.bug;

import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.rhc.jackson.bug.model.MultipleChoiceQuestion;
import com.rhc.jackson.bug.model.Question;
import com.rhc.jackson.bug.model.Questionnaire;
import com.rhc.jackson.bug.model.YesOrNoQuestion;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UserTaskServicesClient;

/**
 *
 * @author dcnorris
 */
public class UserTaskCompleteBugExample {

    @Test
    public void exampleUserTaskCompleteBug() {
        KieServicesConfiguration configuration = KieServicesFactory
                .newRestConfiguration("http://localhost:8180/kie-server/services/rest/server", "admin", "admin");
        configuration.setMarshallingFormat(MarshallingFormat.JSON);
        
        //with this line the deserialization on the client side will work, without it the outgoing json will lead to deserialization issues on the server
        //when  userTaskServiceClient.completeAutoProgress(containerId, taskSummary.getId(), "admin", params); is called
        configuration.addExtraClasses(getModelClasses());
        
        
        KieServicesClient client = KieServicesFactory.newKieServicesClient(configuration);
        ProcessServicesClient processClient = client.getServicesClient(ProcessServicesClient.class);
        UserTaskServicesClient userTaskServiceClient = client.getServicesClient(UserTaskServicesClient.class);
        KieContainerResource kieContainer = getKieContainer(client);
        final String containerId = kieContainer.getContainerId();
        Long processInstancdId = processClient.startProcess(containerId, "rest.UserTask");
        TaskSummary taskSummary = userTaskServiceClient.findTasks("admin", 0, 10).stream().filter(ts->ts.getProcessInstanceId().equals(processInstancdId)).findFirst().get();
        Questionnaire originalQuestionnaire = processClient.getProcessInstanceVariable(containerId, processInstancdId, "questionnaire", Questionnaire.class);
        Questionnaire updatedQuestionnaireToSend = getQuestionaire();
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("questionnaire", updatedQuestionnaireToSend);
        
        //this call will lead to the server error in kie-server-jackson-error.txt
        userTaskServiceClient.completeAutoProgress(containerId, taskSummary.getId(), "admin", params);
    }
    
     private static Set<Class<?>> getModelClasses() {
        Set<Class<?>> extraClasses = Sets.newHashSet();
        addClassesFromPackage(Questionnaire.class.getPackage().getName(), extraClasses);
        return extraClasses;
    }

    private static void addClassesFromPackage(String packageName, Set<Class<?>> extraClasses) {
        try {
            final ClassLoader loader = Thread.currentThread()
                    .getContextClassLoader();
            ClassPath classpath = ClassPath.from(loader);
            classpath.getTopLevelClasses(packageName).stream()
                    .map((ClassPath.ClassInfo t) -> t.load())
                    .forEach(extraClasses::add);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
