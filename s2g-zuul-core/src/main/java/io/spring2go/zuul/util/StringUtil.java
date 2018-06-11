package io.spring2go.zuul.util;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class StringUtil {

	public static boolean contains(String[] srcTags, String[] dstTags) {
		if (dstTags == null || dstTags.length == 0)
			return true;

		if (srcTags == null || srcTags.length == 0)
			return false;

		List<String> srcTagList = Arrays.asList(srcTags);
		for (String t : dstTags) {
			if (!srcTagList.contains(t)) {
				return false;
			}
		}
		return true;
	}

	public static String trimEnd(String data, char trim) {
		data = StringUtils.trim(data);
		if (!StringUtils.isEmpty(data) && (data.toCharArray()[data.length() - 1]) == trim) {
			data = data.substring(0, data.length() - 1);
		}
		return data;
	}
}
