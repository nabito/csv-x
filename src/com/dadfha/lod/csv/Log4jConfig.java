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
 * 
 * For log4j.properties settings ref see:
 * https://www.mkyong.com/logging/log4j-log4j-properties-examples/
 * 
 */
public class Log4jConfig extends ConfigurationFactory {

	/**
	 * Possible PatternLayout format code:
	 *  
	 * m : the msg!
	 * p|level : priority of the log (INFO, DEBUG, WARN, ERROR, etc.)
	 * d : date/time. Ex: %d{HH:mm:ss,SSS} or %d{dd MMM yyyy HH:mm:ss,SSS}. Default ISO8601 (2008-09-06 10:51:45,473). 
	 * c|logger : name of the logger (by default defined as class name of the logger var)
	 * t : current running thread name
	 * n : newline character
	 * ex|exception|throwable : exception print stack trace. added to the pattern's end by default.
	 * 
	 * Expensive code-operations to avoid:
	 * 
	 * C : caller class name
	 * F : caller file name
	 * l : caller location (JVM specific, usually consists of the fully qualified name of the calling method followed by the callers source the file name and line number).
	 * L : caller line number 
	 * M : caller method name
	 * 
	 * Usage: 
	 * 
	 * always preceded by % sign with format:
	 * 
	 * %[-][minwidth][.maxwidth]code
	 * 
	 * and possible modifiers:
	 * 
	 * - : left justify, otherwise right by default.
	 * minwidth : minimum width for the code output (int).
	 * maxwidth : maximum width for the code output (int).
	 * 
	 * For full list of format codes and modifiers see PatternLayout doc at:
	 * https://logging.apache.org/log4j/2.x/manual/layouts.html
	 * 
	 *  
	 * Examples:
	 * 
	 * [%-5p] %d %c - %m%n
	 * 
	 * %d [%t] %-5level: %msg%n%throwable
	 * 
	 * %d{ISO8601} %-5p (%t) [%c{1}(%M:%L)] %m%n
	 * 2013-06-24 10:03:26,892 DEBUG (http-8080-4) [TestClass(logSomething:136)] Hello World!
	 * 
	 * %5p | %d | %F | %L | %m%n
	 * INFO | 2008-09-06 10:51:45,473 | SQLErrorCodesFactory.java | 128 | SQLErrorCodes loaded: [DB2, Derby, H2, HSQL, Informix, MS-SQL, MySQL, Oracle, PostgreSQL, Sybase]
	 * 
	 * 
	 * @param name
	 * @param builder
	 * @return
	 */
	static Configuration createConfiguration(final String name, ConfigurationBuilder<BuiltConfiguration> builder) {
		builder.setConfigurationName(name);
		builder.setStatusLevel(Level.ERROR);
		//builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL)
		//		.addAttribute("level", Level.DEBUG));
		AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout.User", "CONSOLE").addAttribute("target",
				ConsoleAppender.Target.SYSTEM_OUT);
		appenderBuilder.add(
				builder.newLayout("PatternLayout").addAttribute("pattern", "[%-5p] %d - %m%n"));
		//appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
		//		.addAttribute("marker", "FLOW"));
		builder.add(appenderBuilder);
		
		// appender for dev level, show more info only at DEBUG and TRACE
		AppenderComponentBuilder appenderBuilder2 = builder.newAppender("Stdout.Dev", "CONSOLE").addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
		appenderBuilder2.add(builder.newLayout("PatternLayout").addAttribute("pattern", "[%-5p] %d %logger (%F:%L) thread [%t] - %m%n"));
		appenderBuilder2.add(builder.newFilter("ThresholdFilter", Filter.Result.DENY, Filter.Result.ACCEPT).addAttribute("level", Level.INFO));
		builder.add(appenderBuilder2);		
		
		
		builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG).add(builder.newAppenderRef("Stdout.Dev"))
				.addAttribute("additivity", false));
		
		// modify the root logger output level here to control what to show 
		builder.add(builder.newRootLogger(Level.DEBUG).add(builder.newAppenderRef("Stdout.User").addAttribute("level", "INFO")).add(builder.newAppenderRef("Stdout.Dev").addAttribute("level", "TRACE")));
		
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
