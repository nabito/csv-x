package com.dadfha.lod.csv;

import java.net.URI;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
/**
 * Ref: https://logging.apache.org/log4j/2.x/manual/customconfig.html
 */
public class Log4jConfig extends ConfigurationFactory {

	static Configuration createConfiguration(final String name, ConfigurationBuilder<BuiltConfiguration> builder) {
		builder.setConfigurationName(name);
		builder.setStatusLevel(Level.ERROR);
		//builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL)
		//		.addAttribute("level", Level.DEBUG));
		AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE").addAttribute("target",
				ConsoleAppender.Target.SYSTEM_OUT);
		appenderBuilder.add(
				builder.newLayout("PatternLayout").addAttribute("pattern", "%d %-5p [%t] %logger{36} (%F:%L) - %m%n"));
		// addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
		//appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
		//		.addAttribute("marker", "FLOW"));
		builder.add(appenderBuilder);
		builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG).add(builder.newAppenderRef("Stdout"))
				.addAttribute("additivity", false));
		builder.add(builder.newRootLogger(Level.TRACE).add(builder.newAppenderRef("Stdout")));
		return builder.build();
	}

	@Override
	public Configuration getConfiguration(ConfigurationSource source) {
		return getConfiguration(source.toString(), null);
	}

	@Override
	public Configuration getConfiguration(final String name, final URI configLocation) {
		ConfigurationBuilder<BuiltConfiguration> builder = newConfigurationBuilder();
		return createConfiguration(name, builder);
	}

	// @Override
	protected String[] getSupportedTypes() {
		return new String[] { "*" };
	}
}
