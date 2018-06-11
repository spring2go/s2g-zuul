package io.spring2go.zuul.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class HttpUtil {


	public static <T> T postData(CloseableHttpClient httpClient,String url, Object par, Map<String, String> header, Class<T> clazz)
			throws ClientProtocolException, IOException {
		HttpPost method = new HttpPost(url);
		method.setHeader("Content-type", "application/json; charset=utf-8");
		method.setHeader("Accept", "application/json");
		if (header != null) {
			for (String key : header.keySet()) {
				method.setHeader(key, header.get(key));
			}
		}
		if (par != null) {
			String parameters = JSON.toJSONString(par, SerializerFeature.DisableCircularReferenceDetect);
			HttpEntity entity = new StringEntity(parameters, Charset.forName("UTF-8"));
			method.setEntity(entity);
		}
		CloseableHttpResponse response = httpClient.execute(method);
		String body = EntityUtils.toString(response.getEntity());
		return JSON.parseObject(body, clazz);
	}
}
