package com.bkahlert.devel.nebula.widgets.timeline;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.bkahlert.devel.nebula.utils.StringUtils;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Decorator;
import com.bkahlert.devel.nebula.widgets.timeline.impl.SelectionTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineInput;

public class TimelineJsonGenerator {

	public static Logger logger = Logger.getLogger(TimelineJsonGenerator.class);

	public static String toJson(List<IDecorator> decorators, boolean pretty) {
		JsonFactory factory = new JsonFactory();
		StringWriter writer = new StringWriter();
		JsonGenerator generator;
		try {
			generator = factory.createJsonGenerator(writer);
			generator.setCodec(new ObjectMapper());
			if (pretty)
				generator.setPrettyPrinter(new DefaultPrettyPrinter());

			generator.writeObject(decorators);

			generator.close();
			String generated = writer.toString();
			writer.close();
			return generated;
		} catch (IOException e) {
			logger.fatal("Error using " + StringWriter.class.getSimpleName(), e);
		}
		return null;
	}

	public static String toJson(ITimelineInput input, boolean pretty,
			IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor);

		int eventCount = input.getEventCount();
		subMonitor.beginTask("Genering JSON", 5 + eventCount);

		JsonFactory factory = new JsonFactory();
		StringWriter stringWriter = new StringWriter();
		JsonGenerator generator;
		try {
			generator = factory.createJsonGenerator(stringWriter);

			generator.setCodec(new ObjectMapper());

			if (pretty) {
				generator.setPrettyPrinter(new DefaultPrettyPrinter());
			}

			generator.writeStartObject();
			writeOptions(generator, input.getOptions(), subMonitor.newChild(5));
			writeBands(generator, input.getBands(),
					subMonitor.newChild(eventCount));
			generator.writeEndObject();

			generator.close();

			String json = stringWriter.toString();
			stringWriter.close();
			subMonitor.worked(5);
			subMonitor.done();
			// System.err.println(json);
			return json;
		} catch (IOException e) {
			logger.fatal("Error using " + StringWriter.class.getSimpleName(), e);
		}
		return null;
	}

	public static void writeBands(JsonGenerator generator,
			List<ITimelineBand> bands, IProgressMonitor monitor)
			throws IOException, JsonGenerationException,
			JsonProcessingException {
		int numBands = bands.size();
		int numEvents = new TimelineInput(null, bands).getEventCount();
		SubMonitor subMonitor = SubMonitor.convert(monitor, numBands * 5
				+ numEvents);

		generator.writeFieldName("bands");
		generator.writeStartArray();

		for (int i = 0, m = bands.size(); i < m; i++) {
			ITimelineBand band = bands.get(i);
			generator.writeStartObject();
			writeOptions(generator, band.getOptions(), subMonitor.newChild(5));
			writeEvents(generator, band.getEvents(),
					subMonitor.newChild(band.getEventCount()));
			generator.writeEndObject();
		}

		generator.writeEndArray();
	}

	/**
	 * Writes a {@link Map} denoting the {@link SelectionTimeline}'s options to the JSON.
	 * <p>
	 * 
	 * <pre>
	 * <code>
	 * Map<String, Object> options = new HashMap<String, Object>();
	 * options.put("startDate", "1984-05-15T14:30:00+02:00");
	 * options.put("name", new Name("Björn", "Kahlert"));
	 * </code>
	 * </pre>
	 * 
	 * would result in
	 * <p>
	 * 
	 * <pre>
	 * <code>
	 * {
	 *   options: {
	 *     startDate: "1984-05-15T14:30:00+02:00",
	 *     name: {
	 *       firstName: "Björn",
	 *       lastName: "Kahlert
	 *       }
	 *     }
	 *   }
	 * </code>
	 * </pre>
	 * 
	 * without the outer brackets.
	 * 
	 * @param generator
	 *            to where to add an options property
	 * @param options
	 *            described by means of a {@link Map}
	 * @param monitor
	 * @throws IOException
	 * @throws JsonGenerationException
	 * @throws JsonProcessingException
	 */
	public static void writeOptions(JsonGenerator generator, IOptions options,
			IProgressMonitor monitor) throws IOException,
			JsonGenerationException, JsonProcessingException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, options.size());
		generator.writeFieldName("options");
		generator.writeStartObject();
		if (options != null) {
			for (String option : options.keySet()) {
				generator.writeFieldName(option);
				Object optionValue = options.get(option);
				generator.writeObject(optionValue);
				subMonitor.worked(1);
			}
		}
		generator.writeEndObject();
		subMonitor.done();
	}

	public static void writeEvents(JsonGenerator generator,
			List<ITimelineEvent> events, IProgressMonitor monitor)
			throws IOException, JsonGenerationException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, events.size());
		generator.writeFieldName("events");
		generator.writeStartArray();
		for (int i = 0, m = events.size(); i < m; i++) {
			ITimelineEvent event = events.get(i);
			writeEvent(generator, event);
			subMonitor.worked(1);
		}
		generator.writeEndArray();
		subMonitor.done();
	}

	public static void writeEvent(JsonGenerator generator, ITimelineEvent event)
			throws JsonGenerationException, IOException {
		List<String> classNames = event.getClassNames();
		if (classNames == null)
			classNames = new ArrayList<String>();
		else
			classNames = new ArrayList<String>(classNames);

		generator.writeStartObject();

		generator.writeFieldName("title");
		generator.writeString(event.getTitle());

		if (event.getIcon() != null) {
			generator.writeFieldName("icon");
			generator.writeString(event.getIcon());
		}

		if (event.getImage() != null) {
			generator.writeFieldName("image");
			generator.writeString(event.getImage());
		}

		/**
		 * This is an ugly workaround. The SIMILE timeline has no support for
		 * undefined start dates. No does it support custom properties to still
		 * pass some kind of information to the rendered code so it can be
		 * post-processed by custom JavaScript code.
		 * <p>
		 * If start or end is undefined the corresponding field is set to the
		 * same time minus 100 seconds and a specific class is added.
		 */
		Calendar start = event.getStart();
		Calendar end = event.getEnd();

		if (start == null && end != null) {
			start = (Calendar) end.clone();
			start.add(Calendar.SECOND, -0);
			classNames.add("undefined-start");
		} else if (start != null && end == null) {
			end = (Calendar) start.clone();
			end.add(Calendar.SECOND, 0);
			classNames.add("undefined-end");
		} else if (start == null && end == null) {
			classNames.add("undefined-start");
			classNames.add("undefined-end");
		}

		generator.writeFieldName("start");
		generator.writeString(start != null ? TimelineJsonGenerator
				.toISO8601(start) : "null");
		generator.writeFieldName("end");
		generator.writeString(end != null ? TimelineJsonGenerator
				.toISO8601(end) : "null");

		generator.writeFieldName("durationEvent");
		generator.writeBoolean(true);

		generator.writeFieldName("classname");
		generator.writeString(StringUtils.join(classNames, " "));

		generator.writeEndObject();
	}

	public static String jsonDecoratorList(List<Decorator> decorators,
			boolean pretty) {
		JsonFactory factory = new JsonFactory();
		StringWriter writer = new StringWriter();
		JsonGenerator generator;
		try {
			generator = factory.createJsonGenerator(writer);
			generator.setCodec(new ObjectMapper());
			if (pretty)
				generator.setPrettyPrinter(new DefaultPrettyPrinter());

			generator.writeObject(decorators);

			generator.close();
			String generated = writer.toString();
			writer.close();
			return generated;
		} catch (IOException e) {
			logger.fatal("Error using " + StringWriter.class.getSimpleName(), e);
		}
		return null;
	}

	/**
	 * Escapes quotes of a JSON string to it can be concatenated with JavaScript
	 * code without breaking the commands.
	 * 
	 * @param json
	 * @return
	 */
	public static String escape(String json) {
		return json.replace("'", "\\'").replace("\"", "\\\"");
	}

	/**
	 * Converts a {@link Calendar} object to its ISO 8601 representation.
	 * {@link Date}s are not supported since they don't provide any
	 * {@link TimeZone} information.
	 * <p>
	 * e.g. Tuesday, 15 May 1984 at 2:30pm in timezone 01:00 summertime would be
	 * 1984-05-15T14:30:00+02:00.
	 * 
	 * @param calendar
	 * @return
	 */
	public static String toISO8601(Calendar calendar) {
		SimpleDateFormat iso8601 = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZ");
		iso8601.setTimeZone(calendar.getTimeZone());
		String missingDots = iso8601.format(calendar.getTime()).replace("GMT",
				"");
		return missingDots.substring(0, missingDots.length() - 2) + ":"
				+ missingDots.substring(missingDots.length() - 2);
	}
}
