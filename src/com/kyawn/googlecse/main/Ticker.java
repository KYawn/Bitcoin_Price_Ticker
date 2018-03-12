package com.kyawn.googlecse.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class Ticker {
	private String Price;

	public void setPrice(String price) {
		Price = price;
	}

	private String IFTTTUrl = null;

	public Ticker(String price, String iFTTTUrl) {
		super();
		Price = price;
		IFTTTUrl = iFTTTUrl;
	}

	public static float getPrice() throws Exception {
		String url = "https://www.btc123.com/api/getTicker?symbol=coinbasebtcusd";
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		request.setProtocolVersion(HttpVersion.HTTP_1_1);
		request.addHeader("accept", "application/json");
		HttpResponse response = client.execute(request);
		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent()), "utf-8"));
		String line;
		StringBuilder builder = new StringBuilder();
		while ((line = br.readLine()) != null) {
			builder.append(line);
		}
		JSONObject resultObject = new JSONObject(builder.toString());
		float currentPrice = Float
				.parseFloat(resultObject.getJSONObject("datas").getJSONObject("ticker").getString("dollar"));
		return currentPrice;
	}

	public String returnMsg() {

		String returnMessage = null;
		float tickPrice = Float.parseFloat(Price);
		float currentPrice = 0;
		// 1. 先回复微信消息
		try {
			currentPrice = Ticker.getPrice();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (tickPrice < currentPrice) {
			returnMessage = "收到，现在价格是 " + currentPrice + " ，当价格跌到 " + tickPrice + " 时会打电话给你哟！";
			scheduledTask("below");
		} else if (tickPrice > currentPrice) {
			returnMessage = "收到，现在价格是 " + currentPrice + " ，当价格涨到 " + tickPrice + " 时会打电话给你哟！";
			scheduledTask("above");
		} else {
			returnMessage = "Cannot get price data!";
		}

		return returnMessage;
	}

	public void scheduledTask(String flag) {
		// 2. 再间隔5秒循环获取最新价格，然后触发ITFTT
		System.out.println("flag:" + flag);
		ScheduledExecutorService service1 = Executors.newSingleThreadScheduledExecutor();
		if (flag.equals("below")) {
			// 设置查询跌价的定时任务
			Runnable runnable = new Runnable() {
				public void run() {
					float tickPrice = Float.parseFloat(Price);
					float currentPrice = 0;
					System.out.println("running below method");
					try {
						currentPrice = Ticker.getPrice();
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (currentPrice < tickPrice) {
						triggerIFTTT("below", String.valueOf(tickPrice));
						// System.out.println("triggerIFTTT(\"below\", String.valueOf(tickPrice));");
						// shutdown the scheduled task
						service1.shutdownNow();
					}
				}
			};

			service1.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);

		} else if (flag.equals("above")) {
			ScheduledExecutorService service2 = Executors.newSingleThreadScheduledExecutor();
			// 设置查询涨价的定时任务
			Runnable runnable = new Runnable() {
				public void run() {
					float tickPrice = Float.parseFloat(Price);
					float currentPrice = 0;
					System.out.println("running above method");
					try {
						currentPrice = Ticker.getPrice();
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (currentPrice > tickPrice) {
						// System.out.println("triggerIFTTT(\"below\", String.valueOf(tickPrice));");
						triggerIFTTT("above", String.valueOf(tickPrice));
						service2.shutdownNow();
					}
				}
			};

			service2.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);

		}
	}

	public void triggerIFTTT(String flag, String tickPrice) {
		// String url =
		// "https://maker.ifttt.com/trigger/bitcoin/with/key/by5FQ4gUJMMJTph9lO5gMc";
		String json = "{\"value1\":\"" + flag + "\",\"value2\":\"" + tickPrice + "\"}";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		if (IFTTTUrl == null) {
			System.out.println("URL is NULL!!");
		} else {
			HttpPost httpPost = new HttpPost(IFTTTUrl);
			httpPost.addHeader("Content-Type", "application/json");
			CloseableHttpResponse response = null;
			try {
				httpPost.setEntity(new StringEntity(json));
				response = httpClient.execute(httpPost);
				HttpEntity entity = response.getEntity();
				String responseContent = EntityUtils.toString(entity, "UTF-8");
				if (responseContent != null) {
					response.close();
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
//	public static void main(String[] args) {
//		Ticker ticker = new Ticker("10000", "https://maker.ifttt.com/trigger/bitcointicker/with/key/2Ym5J99waKt5lDwSoIbi_");
//		ticker.triggerIFTTT("below", "10000");
//	}
}
