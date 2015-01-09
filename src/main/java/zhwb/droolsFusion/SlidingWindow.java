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
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        URL sliding = App.class.getClassLoader().getResource("sliding.drl");

        kbuilder.add(ResourceFactory.newFileResource(sliding.getPath()), ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            System.err.println(kbuilder.getErrors().toString());
        }

        KnowledgeBaseConfiguration config = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);


        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase(config);
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());


        KnowledgeSessionConfiguration configSession = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        configSession.setOption(ClockTypeOption.get("pseudo"));

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession(configSession, null);

        SessionPseudoClock clock = ksession.getSessionClock();

        WorkingMemoryEntryPoint entryPointSurrey = ksession.getWorkingMemoryEntryPoint("Surrey");

        String gang1 = "gang1";

        entryPointSurrey.insert(new Workorder("ABC1", 2, 50, gang1));
        clock.advanceTime(10, TimeUnit.SECONDS);

        entryPointSurrey.insert(new Workorder("ABC2", 2, 200, gang1));
        clock.advanceTime(10, TimeUnit.SECONDS);


        ksession.fireAllRules();
        ksession.dispose();
    }
}
