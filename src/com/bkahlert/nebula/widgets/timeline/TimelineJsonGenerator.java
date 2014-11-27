package com.bkahlert.nebula.widgets.timeline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.bkahlert.nebula.utils.CalendarUtils;
import com.bkahlert.nebula.utils.StringUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.timeline.impl.TimelineInput;
import com.bkahlert.nebula.widgets.timeline.model.IDecorator;
import com.bkahlert.nebula.widgets.timeline.model.IOptions;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineBand;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineInput;
import com.bkahlert.nebula.widgets.timeline.model.IZoomStep;

public class TimelineJsonGenerator {

	public static Logger logger = Logger.getLogger(TimelineJsonGenerator.class);

	public static String toJson(IDecorator[] decorators, boolean pretty) {
		JsonFactory factory = new JsonFactory();
		StringWriter writer = new StringWriter();
		JsonGenerator generator;
		try {
			generator = factory.createJsonGenerator(writer);
			generator.setCodec(new ObjectMapper());
			if (pretty) {
				generator.setPrettyPrinter(new DefaultPrettyPrinter());
			}

			writeDecorators(decorators, generator);

			generator.close();
			String generated = writer.toString();
			writer.close();
			return generated;
		} catch (IOException e) {
			logger.fatal("Error using " + StringWriter.class.getSimpleName(), e);
		}
		return null;
	}

	public static void writeDecorators(IDecorator[] decorators,
			JsonGenerator generator) throws IOException,
			JsonGenerationException {
		generator.writeStartArray();
		for (int i = 0, m = decorators.length; i < m; i++) {
			IDecorator decorator = decorators[i];
			writeDecorator(generator, decorator);
		}
		generator.writeEndArray();
	}

	public static void writeDecorator(JsonGenerator generator,
			IDecorator decorator) throws IOException, JsonGenerationException {
		if (decorator.getStartDate() == null && decorator.getEndDate() == null) {
			return;
		}

		generator.writeStartObject();

		generator.writeFieldName("startLabel");
		generator.writeString(decorator.getStartLabel());

		generator.writeFieldName("endLabel");
		generator.writeString(decorator.getEndLabel());

		generator.writeFieldName("startDate");
		generator.writeString(decorator.getStartDate() != null ? decorator
				.getStartDate() : decorator.getEndDate());

		generator.writeFieldName("endDate");
		generator.writeString(decorator.getEndDate() != null ? decorator
				.getEndDate() : decorator.getStartDate());

		if (decorator.getStartDate() == null) {
			generator.writeFieldName("classname");
			generator.writeString("undefined-start");
		} else if (decorator.getEndDate() == null) {
			generator.writeFieldName("classname");
			generator.writeString("undefined-end");
		}

		generator.writeEndObject();
	}

	public static void writeZoomSteps(IZoomStep[] zoomSteps,
			JsonGenerator generator) throws IOException,
			JsonGenerationException {
		generator.writeStartArray();
		for (int i = 0, m = zoomSteps.length; i < m; i++) {
			IZoomStep zoomStep = zoomSteps[i];
			writeZoomStep(generator, zoomStep);
		}
		generator.writeEndArray();
	}

	public static void writeZoomStep(JsonGenerator generator, IZoomStep zoomStep)
			throws IOException, JsonGenerationException {
		generator.writeStartObject();

		generator.writeFieldName("pixelsPerInterval");
		generator.writeNumber(zoomStep.getPixelsPerInterval());

		generator.writeFieldName("unit");
		generator.writeString(zoomStep.getUnit().toString().toUpperCase());

		generator.writeFieldName("showLabelEveryUnits");
		generator.writeNumber(zoomStep.getShowLabelEveryUnits());

		generator.writeEndObject();
	}

	public static File toJson(ITimelineInput input, boolean pretty,
			IProgressMonitor monitor) throws IOException {
		Assert.isLegal(input != null && input.getOptions() != null
				&& input.getOptions().getTimeZone() != null);
		int eventCount = input.getEventCount();
		SubMonitor subMonitor = SubMonitor.convert(monitor, 5 + eventCount);

		JsonFactory factory = new JsonFactory();
		File tmpFile = File.createTempFile(
				TimelineJsonGenerator.class.getSimpleName(), ".json");
		tmpFile.deleteOnExit();
		FileWriter fileWriter = new FileWriter(tmpFile);
		JsonGenerator generator;
		try {
			generator = factory.createJsonGenerator(fileWriter);

			generator.setCodec(new ObjectMapper());

			if (pretty) {
				generator.setPrettyPrinter(new DefaultPrettyPrinter());
			}

			generator.writeStartObject();
			writeOptions(generator, input.getOptions(), subMonitor.newChild(5));
			Float timeZoneOffset = input.getOptions().getTimeZone();
			writeBands(generator, input.getBands(), timeZoneOffset,
					subMonitor.newChild(eventCount));
			generator.writeEndObject();

			generator.close();

			fileWriter.close();
			subMonitor.done();
			return tmpFile;
		} catch (IOException e) {
			logger.fatal("Error using " + StringWriter.class.getSimpleName(), e);
		}
		return null;
	}

	public static void writeBands(JsonGenerator generator,
			List<ITimelineBand> bands, Float timeZoneOffset,
			IProgressMonitor monitor) throws IOException,
			JsonGenerationException, JsonProcessingException {
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
			writeEvents(generator, band.getEvents(), timeZoneOffset,
					subMonitor.newChild(band.getEventCount()));
			generator.writeEndObject();
		}

		generator.writeEndArray();
	}

	/**
	 * Writes a {@link Map} denoting the {@link SelectionTimeline}'s options to
	 * the JSON.
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
				if (IDecorator[].class.isInstance(optionValue)) {
					writeDecorators((IDecorator[]) optionValue, generator);
				} else if (IZoomStep[].class.isInstance(optionValue)) {
					writeZoomSteps((IZoomStep[]) optionValue, generator);
				} else {
					generator.writeObject(optionValue);
				}
				subMonitor.worked(1);
			}
		}
		generator.writeEndObject();
		subMonitor.done();
	}

	public static void writeEvents(JsonGenerator generator,
			List<ITimelineEvent> events, Float timeZoneOffset,
			IProgressMonitor monitor) throws IOException,
			JsonGenerationException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, events.size());
		generator.writeFieldName("events");
		generator.writeStartArray();
		for (int i = 0, m = events.size(); i < m; i++) {
			ITimelineEvent event = events.get(i);
			writeEvent(generator, event, timeZoneOffset);
			subMonitor.worked(1);
		}
		generator.writeEndArray();
		subMonitor.done();
	}

	public static void writeEvent(JsonGenerator generator,
			ITimelineEvent event, Float timeZoneOffset)
			throws JsonGenerationException, IOException {
		List<String> classNames = event.getClassNames() != null ? new ArrayList<String>(
				Arrays.asList(event.getClassNames())) : new ArrayList<String>();

		generator.writeStartObject();

		generator.writeFieldName("title");
		generator.writeString(event.getTitle());

		generator.writeFieldName("tooltip");
		generator.writeString(event.getTooltip() != null ? event.getTooltip()
				: "");

		if (event.getIcon() != null) {
			generator.writeFieldName("icon");
			generator.writeString(event.getIcon().toString());
		}

		if (event.getImage() != null) {
			generator.writeFieldName("image");
			generator.writeString(event.getImage().toString());
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

		/**
		 * Timezone check
		 */
		Calendar c = start != null ? start : end;
		if (c != null) {
			int offset = c.getTimeZone().getOffset(new Date().getTime());
			int globalOffset = Math.round(timeZoneOffset * 60f * 60f * 1000f);
			if (offset != globalOffset) {
				classNames.add("deviant_timezone_offset");
			}
		}

		generator.writeFieldName("start");
		generator.writeString(start != null ? CalendarUtils.toISO8601(start)
				: "null");
		generator.writeFieldName("end");
		generator.writeString(end != null ? CalendarUtils.toISO8601(end)
				: "null");

		generator.writeFieldName("durationEvent");
		generator.writeBoolean(true);

		generator.writeFieldName("color");
		RGB[] colors = event.getColors();
		generator.writeString(colors.length > 0 ? colors[0].toDecString()
				: null);

		if (event.isResizable()) {
			classNames.add("resizable");
		}

		generator.writeFieldName("classname");
		generator.writeString(StringUtils.join(classNames, " "));

		generator.writeEndObject();
	}
}
