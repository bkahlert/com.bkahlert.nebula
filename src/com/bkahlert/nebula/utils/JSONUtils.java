package com.bkahlert.nebula.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.NotImplementedException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.BooleanNode;
import org.codehaus.jackson.node.DoubleNode;
import org.codehaus.jackson.node.IntNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.LongNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

/**
 * Utility functions for serializing/deserializing json object/string.
 *
 * @see http
 *      ://www.scribblememo.com/2013/08/13/4296/jsonutil-java-package-com-glassmemo
 *      -app-auth
 */
public final class JSONUtils {
	private static final Logger log = Logger.getLogger(JSONUtils.class
			.getName());
	private static ObjectMapper jsonObjectMapper = null;

	private JSONUtils() {
	}

	// TBD:
	private static ObjectMapper getJsonObjectMapper() {
		if (jsonObjectMapper == null) {
			jsonObjectMapper = new ObjectMapper();
			// jsonObjectMapper.setSerializationInclusion(Inclusion.NON_EMPTY);
			// jsonObjectMapper.setSerializationInclusion(Inclusion.NON_NULL);
			// jsonObjectMapper.setDateFormat(DateFormat.getInstance());
			// ...
		}
		return jsonObjectMapper;
	}

	// temporary
	private static int GLOBAL_NODE_COUNTER_FOR_DEBUGTRACING = 0;

	/**
	 * Parse the json string into a structure comprising only three types: a
	 * map, a list, and a scalar value. 1) Map of {String -> Object}. The value
	 * type can be a map, a list, or a scalar value. 2) List of {Object}. Ditto.
	 * 3) Any other objects, primitive type, Object, a collection other than
	 * list and map, are treated as scalar, and they are treated as leaf nodes.
	 * (Even if an object has an internal structure, we do not have the type
	 * information.) Note that a leaf node object cannot be instantiated as an
	 * object (it's only a string).
	 *
	 * @param jsonStr
	 *            Input JSON string representing a map, a list, or an
	 *            object/primitive type.
	 * @return The object deserialized from the jsonStr.
	 * @throws IOException
	 * @throws JsonParseException
	 */
	public static Object parseJson(String jsonStr) throws IOException {
		if (jsonStr == null) {
			log.warning("Input jsonStr is null.");
			return null;
		}

		GLOBAL_NODE_COUNTER_FOR_DEBUGTRACING = 0;
		Object jsonObj = null;
		try {
			JsonFactory factory = new JsonFactory();
			ObjectMapper om = getJsonObjectMapper(); // ????
			factory.setCodec(om); // Do we need this?
			JsonParser parser = factory.createJsonParser(jsonStr);

			JsonNode topNode = parser.readValueAsTree();
			if (topNode != null) {
				// ++GLOBAL_NODE_COUNTER_FOR_DEBUGTRACING;
				if (topNode.isObject()) {
					jsonObj = parseJsonMap(topNode);
				} else if (topNode.isArray()) {
					jsonObj = parseJsonList(topNode);
				} else {
					// Leaf node
					++GLOBAL_NODE_COUNTER_FOR_DEBUGTRACING;
					Object value = getTypedNodeValue(topNode); // toString() vs.
																// getValueAsText()
					// ????
					if (log.isLoggable(Level.FINE)) {
						log.fine("jsonMap: counter = "
								+ GLOBAL_NODE_COUNTER_FOR_DEBUGTRACING
								+ "; TopNode value" + value);
					}
					jsonObj = value;
				}
			} else {
				// ???
				if (log.isLoggable(Level.INFO)) {
					log.log(Level.INFO, "Failed to parse jsonStr = " + jsonStr);
				}
			}
		} catch (JsonParseException e) {
			throw new IOException(e);
		}
		return jsonObj;
	}

	// Parse a json map
	private static Map<String, Object> parseJsonMap(JsonNode parentNode) {
		if (parentNode == null || !parentNode.isObject()) {
			// This should not happen.
			log.warning("Invalid argument: parentNode is not a map.");
		}

		Map<String, Object> jsonObject = new LinkedHashMap<String, Object>();
		try {
			Iterator<String> fieldNames = parentNode.getFieldNames();
			while (fieldNames.hasNext()) {
				// ++GLOBAL_NODE_COUNTER_FOR_DEBUGTRACING;
				String name = fieldNames.next();
				JsonNode node = parentNode.get(name);
				if (node == null) {
					// Can this happen?
					if (log.isLoggable(Level.INFO)) {
						log.info("Empty/null node found: name = " + name);
					}
					continue;
				}
				if (node.isArray()) {
					List<Object> childList = parseJsonList(node);
					jsonObject.put(name, childList);
				} else if (node.isObject()) {
					Map<String, Object> childMap = parseJsonMap(node);
					jsonObject.put(name, childMap);
				} else {
					// Leaf node
					++GLOBAL_NODE_COUNTER_FOR_DEBUGTRACING;
					Object value = getTypedNodeValue(node);
					if (log.isLoggable(Level.FINE)) {
						log.fine("jsonMap: counter = "
								+ GLOBAL_NODE_COUNTER_FOR_DEBUGTRACING
								+ "; name = " + name + "; value" + value);
					}
					jsonObject.put(name, value);
				}
			}
		} catch (Exception e) {
			if (log.isLoggable(Level.WARNING)) {
				log.log(Level.WARNING,
						"Exception while processing parentNode = " + parentNode,
						e);
			}
		}
		return jsonObject;
	}

	// Parses a json list.
	private static List<Object> parseJsonList(JsonNode parentNode) {
		if (parentNode == null || !parentNode.isArray()) {
			// This should not happen.
			log.warning("Invalid argument: parentNode is not a list.");
		}

		List<Object> jsonArray = new ArrayList<Object>();
		try {
			for (Iterator<JsonNode> elements = parentNode.getElements(); elements
					.hasNext();) {
				// ++GLOBAL_NODE_COUNTER_FOR_DEBUGTRACING;
				JsonNode node = elements.next();
				if (node == null) {
					// Can this happen?
					log.info("Empty/null node found.");
					continue;
				}
				if (node.isArray()) {
					List<Object> childList = parseJsonList(node);
					jsonArray.add(childList);
				} else if (node.isObject()) {
					Map<String, Object> childMap = parseJsonMap(node);
					jsonArray.add(childMap);
				} else {
					// Leaf node
					++GLOBAL_NODE_COUNTER_FOR_DEBUGTRACING;
					Object value = getTypedNodeValue(node);
					if (log.isLoggable(Level.FINE)) {
						log.fine("jsonList: counter = "
								+ GLOBAL_NODE_COUNTER_FOR_DEBUGTRACING
								+ "; element: value" + value);
					}
					jsonArray.add(value);
				}
			}
		} catch (Exception e) {
			if (log.isLoggable(Level.WARNING)) {
				log.log(Level.WARNING,
						"Exception while processing parentNode = " + parentNode,
						e);
			}
		}
		return jsonArray;
	}

	/**
	 * Returns a JSON string for the given object. The input can be a map of
	 * {String -> Object}, a list of {Object}, or an Object/primitive type. It
	 * recursively traverses each element as long as they are a list or a map.
	 * All other elements (primitive, object, a collection type other than list
	 * and map) are treated as leaf nodes. Objects are converted to a string
	 * using toString(). (Note: Even if an object has an internal structure, we
	 * do not have the type information.)
	 *
	 * @param jsonObj
	 *            An object that is to be converted to JSON string.
	 * @return The json string representation of jsonObj.
	 */
	public static String buildJson(Object jsonObj) {
		String jsonStr = null;
		JsonNodeFactory factory = JsonNodeFactory.instance; // ?????
		JsonNode topNode = null;
		if (jsonObj instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<Object, Object> map = (Map<Object, Object>) jsonObj;
			topNode = buildJsonObject(map, factory);
		} else if (jsonObj instanceof List<?>) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) jsonObj;
			topNode = buildJsonArray(list, factory);
		} else if (jsonObj.getClass().isArray()) {
			List<Object> list = Arrays.asList((Object[]) jsonObj);
			topNode = buildJsonArray(list, factory);
		} else {
			if (jsonObj instanceof Boolean) {
				Boolean b = (Boolean) jsonObj;
				topNode = BooleanNode.valueOf(b);
			} else if (jsonObj instanceof Character) {
				// Note: char is treated as String (not as int).
				String str = Character.toString((Character) jsonObj);
				topNode = new TextNode(str);
			} else if (jsonObj instanceof Byte) {
				Byte b = (Byte) jsonObj;
				topNode = IntNode.valueOf(b);
			} else if (jsonObj instanceof Short) {
				Short b = (Short) jsonObj;
				topNode = IntNode.valueOf(b);
			} else if (jsonObj instanceof Integer) {
				Integer b = (Integer) jsonObj;
				topNode = IntNode.valueOf(b);
			} else if (jsonObj instanceof Long) {
				Long b = (Long) jsonObj;
				topNode = LongNode.valueOf(b);
			} else if (jsonObj instanceof Float) {
				Float b = (Float) jsonObj;
				topNode = DoubleNode.valueOf(b);
			} else if (jsonObj instanceof Double) {
				Double b = (Double) jsonObj;
				topNode = DoubleNode.valueOf(b);
			} else if (jsonObj instanceof String) {
				String b = (String) jsonObj;
				topNode = new TextNode(b);
			} else {
				String value = jsonObj.toString();
				if (value != null) {
					topNode = new TextNode(value);
				} else {
					// ?????
					log.fine("TopNode value is null.");
					// topNode = null; // ???
				}
			}
		}
		if (topNode != null) {
			jsonStr = topNode.toString();
		} else {
			if (log.isLoggable(Level.INFO)) {
				log.info("Failed to generate a JSON string for the given jsonObj = "
						+ jsonObj);
			}
		}
		if (log.isLoggable(Level.FINE)) {
			log.fine("buildJson(): jsonStr = " + jsonStr);
		}
		return jsonStr;
	}

	// Creates a JsonNode for the given map.
	private static ObjectNode buildJsonObject(Map<Object, Object> map,
			JsonNodeFactory factory) {
		if (map == null) {
			log.info("Argument map is null.");
			return null;
		}
		if (factory == null) {
			log.warning("Argument factory is null");
			return null; // ????
		}

		ObjectNode jsonObj = new ObjectNode(factory);
		for (Object key : map.keySet()) {
			Object o = map.get(key);

			if (o instanceof Map<?, ?>) {
				@SuppressWarnings("unchecked")
				Map<Object, Object> m = (Map<Object, Object>) o;
				ObjectNode jo = buildJsonObject(m, factory);
				if (jo != null) {
					jsonObj.put(key.toString(), jo);
				} else {
					// ????
					if (log.isLoggable(Level.FINE)) {
						log.fine("Value object is null for key = " + key);
					}
					jsonObj.put(key.toString(), jo);
				}
			} else if (o instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<Object> l = (List<Object>) o;
				ArrayNode ja = buildJsonArray(l, factory);
				if (ja != null) {
					jsonObj.put(key.toString(), ja);
				} else {
					// ????
					if (log.isLoggable(Level.FINE)) {
						log.fine("Value array is null for key = " + key);
					}
					jsonObj.put(key.toString(), ja);
				}
			} else {
				// Should be a "primitive type" or an object.
				// Or, everything else (including collection which is not a list
				// or a map...)
				// We always convert them to text (except for boxed values of
				// primitive types).
				if (o instanceof Boolean) {
					Boolean b = (Boolean) o;
					jsonObj.put(key.toString(), b);
				} else if (o instanceof Character) {
					// Note: char is treated as String (not as int).
					String str = Character.toString((Character) o);
					jsonObj.put(key.toString(), str);
				} else if (o instanceof Byte) {
					Byte b = (Byte) o;
					jsonObj.put(key.toString(), b);
				} else if (o instanceof Short) {
					Short b = (Short) o;
					jsonObj.put(key.toString(), b);
				} else if (o instanceof Integer) {
					Integer b = (Integer) o;
					jsonObj.put(key.toString(), b);
				} else if (o instanceof Long) {
					Long b = (Long) o;
					jsonObj.put(key.toString(), b);
				} else if (o instanceof Float) {
					Float b = (Float) o;
					jsonObj.put(key.toString(), b);
				} else if (o instanceof Double) {
					Double b = (Double) o;
					jsonObj.put(key.toString(), b);
				} else if (o instanceof String) {
					String b = (String) o;
					jsonObj.put(key.toString(), b);
				} else {
					String value = o != null ? o.toString() : null;
					jsonObj.put(key.toString(), value);
				}
			}
		}

		if (log.isLoggable(Level.FINE)) {
			log.fine("jsonObj = " + jsonObj);
		}
		return jsonObj;
	}

	// Creates a JsonNode for the given list.
	private static ArrayNode buildJsonArray(List<Object> list,
			JsonNodeFactory factory) {
		if (list == null) {
			log.info("Argument map is null.");
			return null;
		}
		if (factory == null) {
			log.warning("Argument factory is null");
			return null; // ????
		}

		ArrayNode jsonArr = new ArrayNode(factory);
		for (Object o : list) {
			if (o instanceof Map<?, ?>) {
				@SuppressWarnings("unchecked")
				Map<Object, Object> m = (Map<Object, Object>) o;
				ObjectNode jo = buildJsonObject(m, factory);
				if (jo != null) {
					jsonArr.add(jo);
				} else {
					// ????
					log.fine("Object element is null.");
					jsonArr.add(jo);
				}
			} else if (o instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<Object> l = (List<Object>) o;
				ArrayNode ja = buildJsonArray(l, factory);
				if (ja != null) {
					jsonArr.add(ja);
				} else {
					// ????
					log.fine("Array element is null.");
					jsonArr.add(ja);
				}
			} else {
				// Should be a "primitive type" or an object.
				// Or, everything else (including collection which is not a list
				// or a map...)
				// We always convert them to text (except for boxed values of
				// primitive types).
				if (o instanceof Boolean) {
					Boolean b = (Boolean) o;
					jsonArr.add(b);
				} else if (o instanceof Character) {
					// Note: char is treated as String (not as int).
					String str = Character.toString((Character) o);
					jsonArr.add(str);
				} else if (o instanceof Byte) {
					Byte b = (Byte) o;
					jsonArr.add(b);
				} else if (o instanceof Short) {
					Short b = (Short) o;
					jsonArr.add(b);
				} else if (o instanceof Integer) {
					Integer b = (Integer) o;
					jsonArr.add(b);
				} else if (o instanceof Long) {
					Long b = (Long) o;
					jsonArr.add(b);
				} else if (o instanceof Float) {
					Float b = (Float) o;
					jsonArr.add(b);
				} else if (o instanceof Double) {
					Double b = (Double) o;
					jsonArr.add(b);
				} else if (o instanceof String) {
					String b = (String) o;
					jsonArr.add(b);
				} else {
					String value = o.toString();
					if (value != null) {
						jsonArr.add(value);
					} else {
						// ?????
						log.fine("Element value is null.");
						jsonArr.add(value);
					}
				}
			}
		}

		if (log.isLoggable(Level.FINE)) {
			log.fine("jsonArr = " + jsonArr);
		}
		return jsonArr;
	}

	public static Object getTypedNodeValue(JsonNode node) {
		if (!node.isValueNode()) {
			throw new NotImplementedException("Only value nodes are supported");
		} else if (node.isBigDecimal()) {
			return node.getDecimalValue();
		} else if (node.isBigInteger()) {
			return node.getBigIntegerValue();
		} else if (node.isBinary()) {
			try {
				return node.getBinaryValue();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else if (node.isBoolean()) {
			return node.getBooleanValue();
		} else if (node.isDouble()) {
			return node.getDoubleValue();
		} else if (node.isInt()) {
			return node.getIntValue();
		} else if (node.isLong()) {
			return node.getLongValue();
		} else if (node.isNull()) {
			return null;
		} else if (node.isTextual()) {
			return node.getTextValue();
		} else {
			throw new NotImplementedException("Unidentified type for " + node);
		}
	}

	/**
	 * Escapes quotes of a JSON string to it can be concatenated with JavaScript
	 * code without breaking the commands.
	 *
	 * @param json
	 * @return
	 *
	 * @author bkahlert
	 */
	public static String enquote(String s) {
		if (s == null) {
			return "null";
		}

		StringBuffer sb = new StringBuffer();
		sb.append("\"");
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\b':
				sb.append("\\b");
				break; // backspace
			case '\t':
				sb.append("\\t");
				break; // tab
			case '\n':
				sb.append("\\n");
				break; // newline
			case '\f':
				sb.append("\\f");
				break; // formfeed
			case '\r':
				sb.append("\\r");
				break; // return
			case '\"':
				sb.append("\\\"");
				break; // double quote
			case '\'':
				sb.append("\\\'");
				break; // single quote
			case '\\':
				sb.append("\\\\");
				break; // backslash
			default:
				if (c >= ' ' && c < '\u007f') {
					sb.append(c); // readable ASCII
				} else // convert to unicode form
				{
					char[] hexdigits = new char[4];
					for (int x = c, j = hexdigits.length - 1; j >= 0; j--, x /= 16) {
						hexdigits[j] = Character.forDigit(x % 16, 16);
					}
					sb.append("\\u").append(hexdigits);
				}
			}
		}
		sb.append("\"");
		return sb.toString();
	}

}