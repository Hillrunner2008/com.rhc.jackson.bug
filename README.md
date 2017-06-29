## kie-server kjar reproducer for jackson serialization bug

### Summary
When using the kie-server client it seems it is impossible to configure the client to communicate using the MarshallingFormat.JSON format without running into serialization issues on one side of the communication or the other (i.e. on the client side deserializing the json sent from the server, or on the server side deserializing the content sent from the client). 

### Steps to reproduce
1. compile the project and deploy it to kie-server
2. ensure the kie-server has the following user:
```bash
username: admin / password: admin
```
3. Run the UserTaskCompleteBugExample unit test
```bash
mvn test
```
4. Observer the kie-server logs and see the error shown below

### Observations
When a variable is not wrapped in a Map as is the case with the kie-client "completeAutoProgress" call the marshalling/unmarshalling does work as expected; however, when it is wrapped in map with the extraClasses having been added to the kie-client to support deserialization it looks like the following:

```json
{
	"questionnaire": {
		"com.rhc.jackson.bug.model.Questionnaire": {
			"name": "NonReceived Dispute Flow",
			"questions": [{
				"@class": "com.rhc.jackson.bug.model.OpenEndedQuestion",
				"displayText": "What type of product was purchased?",
				"possibleAnswers": [],
				"answer": "Yes"
			}, {
				"@class": "com.rhc.jackson.bug.model.MultipleChoiceQuestion",
				"displayText": "Was the product to be picked up or delivered?",
				"possibleAnswers": ["Picked Up", "Delivered to Address on File", "Delivered to Alternate Address"],
				"answer": "Yes"
			}, {
				"@class": "com.rhc.jackson.bug.model.YesOrNoQuestion",
				"displayText": "Was any alternative person authorized to take delivery or pick up the merchandise?",
				"possibleAnswers": ["Yes", "No"],
				"answer": "Yes"
			}, {
				"@class": "com.rhc.jackson.bug.model.YesOrNoQuestion",
				"displayText": "Was a partial amount of the order received?",
				"possibleAnswers": ["Yes", "No"],
				"answer": "Yes"
			}, {
				"@class": "com.rhc.jackson.bug.model.YesOrNoQuestion",
				"displayText": "Has the merchant been contacted about the merchandise not received?",
				"possibleAnswers": ["Yes", "No"],
				"answer": "Yes"
			}, {
				"@class": "com.rhc.jackson.bug.model.OpenEndedQuestion",
				"displayText": "What was the expected date to receive the merchandise?",
				"possibleAnswers": [],
				"answer": "Yes"
			}],
			"unansweredQuestions": []
		}
	}
}
```

This is valid json and normally would not be a problem for jackson; however, on the kie-server side it turns out this format proceeds down a code path that causes it to check the kie-server war application's classpath for the model types instead of kjar's container classpath. 

This leads to the following stacktrace:

```java
14:28:15,239 ERROR [org.kie.server.remote.rest.jbpm.UserTaskResource] (default task-21) Unexpected error during processing Error unmarshalling input: org.kie.server.api.marshalling.MarshallingException: Error unmarshalling input
	at org.kie.server.api.marshalling.json.JSONMarshaller.unmarshall(JSONMarshaller.java:201)
	at org.kie.server.services.impl.marshal.MarshallerHelper.unmarshal(MarshallerHelper.java:97)
	at org.kie.server.services.jbpm.UserTaskServiceBase.completeAutoProgress(UserTaskServiceBase.java:104)
	at org.kie.server.remote.rest.jbpm.UserTaskResource.complete(UserTaskResource.java:116)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.jboss.resteasy.core.MethodInjectorImpl.invoke(MethodInjectorImpl.java:139)
	at org.jboss.resteasy.core.ResourceMethodInvoker.invokeOnTarget(ResourceMethodInvoker.java:295)
	at org.jboss.resteasy.core.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:249)
	at org.jboss.resteasy.core.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:236)
	at org.jboss.resteasy.core.SynchronousDispatcher.invoke(SynchronousDispatcher.java:395)
	at org.jboss.resteasy.core.SynchronousDispatcher.invoke(SynchronousDispatcher.java:202)
	at org.jboss.resteasy.plugins.server.servlet.ServletContainerDispatcher.service(ServletContainerDispatcher.java:221)
	at org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher.service(HttpServletDispatcher.java:56)
	at org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher.service(HttpServletDispatcher.java:51)
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:790)
	at io.undertow.servlet.handlers.ServletHandler.handleRequest(ServletHandler.java:85)
	at io.undertow.servlet.handlers.security.ServletSecurityRoleHandler.handleRequest(ServletSecurityRoleHandler.java:62)
	at io.undertow.servlet.handlers.ServletDispatchingHandler.handleRequest(ServletDispatchingHandler.java:36)
	at org.wildfly.extension.undertow.security.SecurityContextAssociationHandler.handleRequest(SecurityContextAssociationHandler.java:78)
	at io.undertow.server.handlers.PredicateHandler.handleRequest(PredicateHandler.java:43)
	at io.undertow.servlet.handlers.security.SSLInformationAssociationHandler.handleRequest(SSLInformationAssociationHandler.java:131)
	at io.undertow.servlet.handlers.security.ServletAuthenticationCallHandler.handleRequest(ServletAuthenticationCallHandler.java:57)
	at io.undertow.server.handlers.DisableCacheHandler.handleRequest(DisableCacheHandler.java:33)
	at io.undertow.server.handlers.PredicateHandler.handleRequest(PredicateHandler.java:43)
	at io.undertow.security.handlers.AuthenticationConstraintHandler.handleRequest(AuthenticationConstraintHandler.java:51)
	at io.undertow.security.handlers.AbstractConfidentialityHandler.handleRequest(AbstractConfidentialityHandler.java:46)
	at io.undertow.servlet.handlers.security.ServletConfidentialityConstraintHandler.handleRequest(ServletConfidentialityConstraintHandler.java:64)
	at io.undertow.servlet.handlers.security.ServletSecurityConstraintHandler.handleRequest(ServletSecurityConstraintHandler.java:56)
	at io.undertow.security.handlers.AuthenticationMechanismsHandler.handleRequest(AuthenticationMechanismsHandler.java:60)
	at io.undertow.servlet.handlers.security.CachedAuthenticatedSessionHandler.handleRequest(CachedAuthenticatedSessionHandler.java:77)
	at io.undertow.security.handlers.NotificationReceiverHandler.handleRequest(NotificationReceiverHandler.java:50)
	at io.undertow.security.handlers.AbstractSecurityContextAssociationHandler.handleRequest(AbstractSecurityContextAssociationHandler.java:43)
	at io.undertow.server.handlers.PredicateHandler.handleRequest(PredicateHandler.java:43)
	at org.wildfly.extension.undertow.security.jacc.JACCContextIdHandler.handleRequest(JACCContextIdHandler.java:61)
	at io.undertow.server.handlers.PredicateHandler.handleRequest(PredicateHandler.java:43)
	at io.undertow.server.handlers.PredicateHandler.handleRequest(PredicateHandler.java:43)
	at io.undertow.servlet.handlers.ServletInitialHandler.handleFirstRequest(ServletInitialHandler.java:285)
	at io.undertow.servlet.handlers.ServletInitialHandler.dispatchRequest(ServletInitialHandler.java:264)
	at io.undertow.servlet.handlers.ServletInitialHandler.access$000(ServletInitialHandler.java:81)
	at io.undertow.servlet.handlers.ServletInitialHandler$1.handleRequest(ServletInitialHandler.java:175)
	at io.undertow.server.Connectors.executeRootHandler(Connectors.java:202)
	at io.undertow.server.HttpServerExchange$1.run(HttpServerExchange.java:792)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at java.lang.Thread.run(Thread.java:748)
Caused by: org.codehaus.jackson.map.JsonMappingException: Invalid type id 'com.rhc.jackson.bug.model.YesOrNoQuestion' (for id type 'Id.class'): no such class found (through reference chain: com.rhc.jackson.bug.model.Questionnaire["questions"])
	at org.codehaus.jackson.map.JsonMappingException.wrapWithPath(JsonMappingException.java:218)
	at org.codehaus.jackson.map.JsonMappingException.wrapWithPath(JsonMappingException.java:183)
	at org.codehaus.jackson.map.deser.BeanDeserializer.wrapAndThrow(BeanDeserializer.java:1472)
	at org.codehaus.jackson.map.deser.BeanDeserializer.deserializeFromObject(BeanDeserializer.java:699)
	at org.codehaus.jackson.map.deser.BeanDeserializer.deserialize(BeanDeserializer.java:580)
	at org.codehaus.jackson.map.ObjectMapper._readValue(ObjectMapper.java:2704)
	at org.codehaus.jackson.map.ObjectMapper.readValue(ObjectMapper.java:1286)
	at org.kie.server.api.marshalling.json.JSONMarshaller$CustomObjectDeserializer.mapObject(JSONMarshaller.java:452)
	at org.codehaus.jackson.map.deser.std.UntypedObjectDeserializer.deserialize(UntypedObjectDeserializer.java:47)
	at org.codehaus.jackson.map.deser.std.MapDeserializer._readAndBind(MapDeserializer.java:319)
	at org.codehaus.jackson.map.deser.std.MapDeserializer.deserialize(MapDeserializer.java:249)
	at org.codehaus.jackson.map.deser.std.MapDeserializer.deserialize(MapDeserializer.java:33)
	at org.codehaus.jackson.map.ObjectMapper._readMapAndClose(ObjectMapper.java:2732)
	at org.codehaus.jackson.map.ObjectMapper.readValue(ObjectMapper.java:1863)
	at org.kie.server.api.marshalling.json.JSONMarshaller.unmarshall(JSONMarshaller.java:199)
	... 47 more
Caused by: java.lang.IllegalArgumentException: Invalid type id 'com.rhc.jackson.bug.model.YesOrNoQuestion' (for id type 'Id.class'): no such class found
	at org.codehaus.jackson.map.jsontype.impl.ClassNameIdResolver.typeFromId(ClassNameIdResolver.java:57)
	at org.codehaus.jackson.map.jsontype.impl.TypeDeserializerBase._findDeserializer(TypeDeserializerBase.java:113)
	at org.codehaus.jackson.map.jsontype.impl.AsPropertyTypeDeserializer.deserializeTypedFromObject(AsPropertyTypeDeserializer.java:82)
	at org.codehaus.jackson.map.deser.BeanDeserializer.deserializeWithType(BeanDeserializer.java:664)
	at org.codehaus.jackson.map.deser.std.CollectionDeserializer.deserialize(CollectionDeserializer.java:219)
	at org.codehaus.jackson.map.deser.std.CollectionDeserializer.deserialize(CollectionDeserializer.java:194)
	at org.codehaus.jackson.map.deser.std.CollectionDeserializer.deserialize(CollectionDeserializer.java:30)
	at org.codehaus.jackson.map.deser.SettableBeanProperty.deserialize(SettableBeanProperty.java:299)
	at org.codehaus.jackson.map.deser.SettableBeanProperty$MethodProperty.deserializeAndSet(SettableBeanProperty.java:414)
	at org.codehaus.jackson.map.deser.BeanDeserializer.deserializeFromObject(BeanDeserializer.java:697)
	... 58 more
```

Removing the extraClasses from the client will make the output format of the json sent to the task endpoint deserialize correctly; however, this will prevent the client from being able to deserialize the json sent from the server for the Questionnaire object.


