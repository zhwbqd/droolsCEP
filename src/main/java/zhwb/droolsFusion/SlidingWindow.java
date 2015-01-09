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

/**
 * @author jack.zhang
 * @since 2015/1/8
 */
public class SlidingWindow {
    public static void main(String[] args) {
        SlidingWindow app = new SlidingWindow();
        app.run();
    }

    public void run() {
        // KnowledgeBuilder: Has a collection of DRL files, so our rules set can be divided in several files
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        URL sliding = App.class.getClassLoader().getResource("sliding.drl");

        kbuilder.add(ResourceFactory.newFileResource(sliding.getPath()), ResourceType.DRL);

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

        // workorders events in Surrey
        entryPointSurrey.insert(new Workorder("ABC1", 2, 50, gang1));
        clock.advanceTime(10, TimeUnit.SECONDS);

        // after 100 hours, gang1 takes another workorder. They are very slow
        entryPointSurrey.insert(new Workorder("ABC2", 2, 200, gang1));
        clock.advanceTime(10, TimeUnit.SECONDS);


        ksession.fireAllRules();
        ksession.dispose();
    }
}
