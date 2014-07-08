package com.bkahlert.nebula.utils;

import java.security.InvalidParameterException;

public class DateUtils {

	public static int getMinutesOfTimeZone(String iso8601) {
		if (iso8601 == null
				|| iso8601.length() < "2010-05-10T13:15:23+02:00".length()) {
			throw new InvalidParameterException();
		}

		int l = iso8601.length();
		String sign = iso8601.substring(l - 6, l - 5);
		if (!sign.equals("+") && !sign.equals("-")) {
			throw new InvalidParameterException();
		}

		int hourPart = Integer.parseInt(iso8601.substring(l - 5, l - 3));
		int minutesPart = Integer.parseInt(iso8601.substring(l - 2, l));

		return ((sign.equals("+") ? 1 : -1)) * (hourPart * 60 + minutesPart);
	}

	public static String getTimeZoneStringFromMinutes(int minutes) {
		String hourPart = (Math.abs(minutes) / 60) + "";
		String minutesPart = (Math.abs(minutes) % 60) + "";

		if (hourPart.length() > 2) {
			throw new InvalidParameterException();
		}
		if (hourPart.length() == 1) {
			hourPart = "0" + hourPart;
		}
		if (minutesPart.length() == 1) {
			minutesPart = "0" + minutesPart;
		}

		return ((minutes >= 0 ? "+" : "-")) + hourPart + ":" + minutesPart;
	}

	public static String addMinutesToTimeZone(String iso8601, int minutes) {
		int currentMinutes = getMinutesOfTimeZone(iso8601);
		int newMinutes = currentMinutes + minutes;
		String timeZonePart = getTimeZoneStringFromMinutes(newMinutes);
		return iso8601.substring(0, iso8601.length() - timeZonePart.length())
				+ timeZonePart;
	}
}
