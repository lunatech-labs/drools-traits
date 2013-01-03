package com.lunatech.drools.traits;

import com.lunatech.drools.traits.model.NaturalNumber;
import com.lunatech.drools.traits.model.Sequence;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.definition.KnowledgePackage;
import org.drools.factmodel.traits.Thing;
import org.drools.io.ResourceFactory;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Main class for running the rules engine.
 */
public class RulesService {

	private static final String QUERY_ROW_VALUE = "value";
	private final static Logger log = LoggerFactory.getLogger(RulesService.class);

	private static KnowledgeBase knowledgeBase;

	/**
	 * Utility method to construct and cache the {@link KnowledgeBase} instance.
	 */
	private static KnowledgeBase initKnowledgeBase() {
		log.debug("initKnowledgeBase");
        final long start = System.currentTimeMillis();
		if (knowledgeBase == null) {
			final KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
			try {
                for (final Sequence sequence: Sequence.values()) {
                    final String file = sequence.name().toLowerCase() + ".drl";
			        log.debug("Load rules from " + file);
				    builder.add(ResourceFactory.newClassPathResource(file, RulesService.class), ResourceType.DRL);
                }
			}
			catch (final Exception e) {
				log.error("Could not load rules file : " + e.getMessage());
				return null;
			}

			log.debug("Compile rules");
			final Collection<KnowledgePackage> packages = builder.getKnowledgePackages();
            for (KnowledgePackage knowledgePackage : packages) {
                log.debug("  " + knowledgePackage.getName());
            }

			log.debug("Add packages");
			if (builder.hasErrors()) {
				log.error("Rules compilation failed: " + builder.getErrors());
			}
			else {
				final Properties properties = new Properties();
				properties.setProperty("org.drools.sequential", "true");
				final ClassLoader classLoader = RulesService.class.getClassLoader();
				final KnowledgeBaseConfiguration configuration = KnowledgeBaseFactory.newKnowledgeBaseConfiguration(properties, classLoader);
				knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase(configuration);
				knowledgeBase.addKnowledgePackages(packages);
			}
		}
        log.debug(String.format("Knowledge base initialised in %d ms", System.currentTimeMillis() - start));
		return knowledgeBase;
	}

	/**
	 * Executes the rules session, in order to calculate results.
	 */
	public static void main(String[] args) {

		// Get the compiled rules.
		initKnowledgeBase();

		log.debug("Rule session");
		if (knowledgeBase == null) {
			throw new IllegalStateException("KnowledgeBase not initialised");
		}

        final StatelessKnowledgeSession session = knowledgeBase.newStatelessKnowledgeSession();
		final long start = System.currentTimeMillis();
		final List<Command> commands = new ArrayList<Command>();
		commands.add(CommandFactory.newFireAllRules());

        for (final Sequence sequence: Sequence.values()) {
		    commands.add(CommandFactory.newQuery(sequence.name().toLowerCase(), sequence.name().toLowerCase()));
        }
		final ExecutionResults executionResults = session.execute(CommandFactory.newBatchExecution(commands));

		log.debug(String.format("Rules executed in %d ms", System.currentTimeMillis() - start));

        for (final Sequence sequence: Sequence.values()) {
            final String name = sequence.name().toLowerCase();
    		final QueryResults queryResults = (QueryResults) executionResults.getValue(name);
            final StringBuilder output = new StringBuilder(name);
            output.append(" numbers: ");
    		for (final QueryResultsRow row : queryResults) {
                if (name.equals("natural")) {
                    final NaturalNumber number = (NaturalNumber) row.get(QUERY_ROW_VALUE);
                    output.append(number.getValue());
                }
                else {
                    final Thing number = (Thing) row.get(QUERY_ROW_VALUE);
                    output.append(number.getFields().get("value"));
                }
    			output.append(", ");
    		}
            output.append(" …");
    	    log.info(output.toString());
        }
	}
}
