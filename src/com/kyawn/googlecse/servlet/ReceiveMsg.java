package com.kyawn.googlecse.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.kyawn.googlecse.db.DB;
import com.kyawn.googlecse.entity.TextMsg;
import com.kyawn.googlecse.main.Ticker;
import com.kyawn.googlecse.utils.SHA1;
import com.thoughtworks.xstream.XStream;

/**
 * Servlet implementation class ReceiveMsg
 */
@WebServlet("/ReceiveMsg")
public class ReceiveMsg extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public Map<String, String> ReceivedMsgMap;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ReceiveMsg() {
		super();

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 设置编码
		request.setCharacterEncoding("UTF-8");
		response.setContentType("html/text;charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		// 获取输出流

		// 设置一个全局的token,开发者自己设置。api这样解释：Token可由开发者可以任意填写，
		// 用作生成签名（该Token会和接口URL中包含的Token进行比对，从而验证安全性）
		String token = "sanese";
		// 根据api说明，获取上述四个参数
		// 微信加密签名
		String signature = request.getParameter("signature");
		// 随机字符串
		String echostr = request.getParameter("echostr");
		// 时间戳
		String timestamp = request.getParameter("timestamp");
		// 随机数
		String nonce = request.getParameter("nonce");
		if (nonce != null & timestamp != null & echostr != null & signature != null) {
			String[] str = { token, timestamp, nonce };
			Arrays.sort(str); // 字典序排序
			String bigStr = str[0] + str[1] + str[2];
			// SHA1加密
			String digest = new SHA1().getDigestOfString(bigStr.getBytes()).toLowerCase();

			// 确认请求来至微信
			if (digest.equals(signature)) {
				response.getWriter().print(echostr);
			} else {
				response.getWriter().print("invalid signature");
			}
		} else {
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setContentType("html/text;charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		Connection connection = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbPath = getServletContext().getRealPath("/")+"\\WEB-INF\\userurl.db";
			File file = new File(dbPath);
			file.getPath();
			System.out.println("DB　PATH："+dbPath);
			//connection = DriverManager.getConnection("jdbc:slite:"+dbPath);
			connection = DriverManager.getConnection("jdbc:sqlite:F:\\sanese-master\\WebContent\\WEB-INF\\userurl.db");//要写绝对路径
			System.out.println("connection:"+connection);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (SQLException e2) {
		}
		DB db = new DB();
		String msg = null;
		try {
			ReceivedMsgMap = parseXml(request);
			System.out.println("received msg : " + ReceivedMsgMap.toString());
			if (ReceivedMsgMap.get("MsgType").equals("event")) {
				if (ReceivedMsgMap.get("Event").equals("subscribe")) {
					System.out.println("又有新用户订阅啦！");
					msg = "你好！欢迎你的到来！请告诉我你的IFTTT Webhook 地址。";
				}
			} else if (ReceivedMsgMap.get("MsgType").equals("text")) {
				if (Pattern.compile("^[-\\+]?[\\d]*$").matcher(ReceivedMsgMap.get("Content")).matches()) {
					// 如果是数字
					if (db.queryUrl(connection, ReceivedMsgMap.get("FromUserName")) == null) {
						msg = "IFTTT Webhook地址未成功初始化，请重新输入。";
					} else {
						Ticker ticker = new Ticker(ReceivedMsgMap.get("Content"),
								db.queryUrl(connection, ReceivedMsgMap.get("FromUserName")));
						msg = ticker.returnMsg();
						System.out.println("收到价格");
					}
				} else if (ReceivedMsgMap.get("Content").startsWith("http")) {
					// 如果是链接，即IFTTT链接，与用户openid做绑定 fromusername
					System.out.println("收到IFTTT链接地址");
					if (db.queryUrl(connection, ReceivedMsgMap.get("FromUserName")) == null) {
						db.insert(connection, ReceivedMsgMap.get("FromUserName"), ReceivedMsgMap.get("Content"));
						msg = "IFTTT Webhook URL初始化完成，请发送你要订阅的价格。";
					}else {
						db.update(connection, ReceivedMsgMap.get("FromUserName"), ReceivedMsgMap.get("Content"));
						msg = "IFTTT Webhook URL更新成功，请发送你要订阅的价格。";
					}
					

				} else {
					msg = "收到！我会及时回复的！";
					System.out.println("收到其他的信息");
				}
			} else {
				msg = "收到！我会及时回复的！";
				System.out.println("收到其他的信息");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		TextMsg textMsg = new TextMsg();
		textMsg.setToUserName(ReceivedMsgMap.get("FromUserName"));// 发送和接收信息“User”刚好相反
		textMsg.setFromUserName(ReceivedMsgMap.get("ToUserName"));
		textMsg.setCreateTime(new Date().getTime());// 消息创建时间 （整型）
		textMsg.setMsgType("text");// 文本类型消息
		textMsg.setContent(msg);

		// // 第二步，将构造的信息转化为微信识别的xml格式
		XStream xStream = new XStream();
		xStream.alias("xml", textMsg.getClass());
		String textMsg2Xml = xStream.toXML(textMsg);
		System.out.println("sendMsg:" + textMsg2Xml);

		// // 第三步，发送xml的格式信息给微信服务器，服务器转发给用户
		PrintWriter printWriter = response.getWriter();
		printWriter.print(textMsg2Xml);
	}

	public static Map<String, String> parseXml(HttpServletRequest request) throws Exception {
		Map<String, String> map = new HashMap<String, String>();

		InputStream inputStream = request.getInputStream();

		SAXReader reader = new SAXReader();
		Document document = reader.read(inputStream);
		Element root = document.getRootElement();
		@SuppressWarnings("unchecked")
		List<Element> elementList = root.elements();

		for (Element e : elementList)
			map.put(e.getName(), e.getText());

		inputStream.close();
		inputStream = null;
		return map;
	}

}
