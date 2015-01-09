package zhwb.droolsFusion;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.io.ResourceFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;
import org.drools.time.SessionPseudoClock;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    public void run() {
        // KnowledgeBuilder: Has a collection of DRL files, so our rules set can be divided in several files
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        URL urlWorkorders = App.class.getClassLoader().getResource("workorders.drl");
        URL urlGangs = App.class.getClassLoader().getResource("gangs.drl");

        kbuilder.add(ResourceFactory.newFileResource(urlWorkorders.getPath()), ResourceType.DRL);
        kbuilder.add(ResourceFactory.newFileResource(urlGangs.getPath()), ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            System.err.println(kbuilder.getErrors().toString());
        }

        // KnowledgeBaseConfiguration : We'll use this class to set the Event Processing Mode as STREAM
        KnowledgeBaseConfiguration config = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);


        // KnowledgeBase: We create our KnowledgeBase considering the Collection of DRL files the KnowledgeBuider has
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase(config);
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());


        KnowledgeSessionConfiguration configSession = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        // a clock that is controlled by the application
        // that is called Pseudo Clock. This clock is specially useful for unit testing temporal rules since it
        // can be controlled by the application and so the results become deterministic.
        configSession.setOption(ClockTypeOption.get("pseudo"));

        // StatefulKnowledgeSession: Once we have our KnowledgeBase we create a Session to use it
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession(configSession, null);

        SessionPseudoClock clock = ksession.getSessionClock();

        // Each event that is inserted into our WorkingMemory does it through an entry-point
        WorkingMemoryEntryPoint entryPointSurrey = ksession.getWorkingMemoryEntryPoint("Surrey");

        String gang1 = "gang1";
        String gang2 = "gang2";
        String gang3 = "gang3";


        // workorders events in Surrey
        entryPointSurrey.insert(new Workorder("ABC1", 2, 500, gang1));
        clock.advanceTime(100, TimeUnit.HOURS);

        // after 100 hours, gang1 takes another workorder. They are very slow
        entryPointSurrey.insert(new Workorder("ABC2", 5, 10, gang1));
        clock.advanceTime(10, TimeUnit.SECONDS);


        entryPointSurrey.insert(new Workorder("ABC3", 1, 10, gang2));
        clock.advanceTime(10, TimeUnit.SECONDS);
        // after 10 hours, gang2 takes another workorder. They are quickly!!!
        entryPointSurrey.insert(new Workorder("ABC4", 5, 50, gang2));
        clock.advanceTime(10, TimeUnit.SECONDS);


        entryPointSurrey.insert(new Workorder("ABC5", 2, 1, gang3));
        // after 10 hours, gang2 takes another workorder. This workorder took them 2hours and it is ok!!!
        clock.advanceTime(2, TimeUnit.HOURS);
        entryPointSurrey.insert(new Workorder("ABC6", 2, 100, gang3));
        clock.advanceTime(10, TimeUnit.SECONDS);

        ksession.fireAllRules();
        ksession.dispose();
    }
}
