package com.iflytek.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import com.baidu.aip.util.Util;
import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUtility;
import com.iflytek.cloud.speech.SynthesizeToUriListener;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.tags.Param;

/**
 * @author wmj
 */
@Controller
@RequestMapping("/contr")
public class TtsController {

	private static final Log log = LogFactory.getLog("tts");

	private static String	kdxf_app_id = "5a377025";
	private static String	speech_url = "E://";
	private static SpeechSynthesizer mTts;
	static {
		mTts = SpeechSynthesizer.createSynthesizer();
		mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
		mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速，范围0~100
		mTts.setParameter(SpeechConstant.PITCH, "50");//设置语调，范围0~100
		mTts.setParameter(SpeechConstant.VOLUME, "50");//设置音量，范围0~100
	}

	private static int num = 0;

	@RequestMapping(value = "demo")
	public void demo(){
		System.out.println(num++);
	}


	@RequestMapping(value = "test",method = RequestMethod.POST)
	@ResponseBody
	public DataModel ttsServer(HttpServletRequest request,HttpServletResponse response) throws IOException {

		String str, requestParams = "";
		// 接收request带来的数据
		// BufferedReader br = request.getReader();
		// while ((str = br.readLine()) != null) {
		// 	requestParams += str;
		// }
		// Map<String, String> paramMap = new HashMap<String, String>(16);
		// if (!StringUtils.isEmpty(requestParams)) {
		// 	List<String> params =  Arrays.asList("filedir","sessionid","txt","vid","speed","volume","pitch","bgsound","audiofmt");
		// 	JSONObject parseObject = JSON.parseObject(requestParams);
		// 	for (String param : params) {
		// 		if (parseObject.containsKey(param)) {
		// 			paramMap.put(param, parseObject.get(param).toString());
		// 		}
		// 	}
		// }

		// 创建properties对象
		// Properties prop = new Properties();
		// 读取属性文件url.properties
		// InputStream in = getClass().getResourceAsStream("url.properties");
		// prop.load(in);

		// 语音文件路径
		// String speech_url = paramMap.get("filedir");

		//百度--属性
//			String bd_app_id = prop.getProperty("BD_APP_ID");
//			String bd_api_key = prop.getProperty("BD_API_KEY");
//			String bd_secret_key = prop.getProperty("BD_SECRET_KEY");
//			String result = bdTts(bd_app_id,bd_api_key,bd_secret_key,speech_url, paramMap);
//			response.setCharacterEncoding("utf-8");
//			response.setHeader("body", result);

		//科大讯飞--APPID
		// Properties prop = new Properties();
		// Resource resource1 = new ClassPathResource("url.properties");
		// InputStream inputStream = resource1.getInputStream();
		// String kdxf_app_id = prop.getProperty("KDXF_APP_ID");


		//调用科大讯飞的tts引擎生成文件
		return kdxtTts(kdxf_app_id, speech_url, null);
	}
	//百度的tts流程
	private String bdTts(String bd_app_id, String bd_api_key, String bd_secret_key, String speech_url, Map<String, String> paramMap){
		UUID uuid = UUID.randomUUID();
		Calendar now = Calendar.getInstance();
		String yyyy = String.valueOf(now.get(Calendar.YEAR));
		String mm = String.valueOf((now.get(Calendar.MONTH) + 1));
		String dd = String.valueOf(now.get(Calendar.DAY_OF_MONTH));
		// 初始化一个AipSpeech
		AipSpeech client = new AipSpeech(bd_app_id, bd_api_key, bd_secret_key);
        // 设置可选参数
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put("spd", "5");
        options.put("pit", "5");
        options.put("per", "4");
        TtsResponse res = client.synthesis(paramMap.get("txt"), "zh", 1, options);
        Object result = res.getResult();    //服务器返回的内容，合成成功时为null,失败时包含error_no等信息
        byte[] data = res.getData();     //生成的音频数据
        if (data != null) {
            try{
            	File file = new File(speech_url+"/"+yyyy+"/"+mm+"/"+dd);
				if (file.exists()) {
					if (file.isDirectory()) {
						Util.writeBytesToFileSystem(data, speech_url+"/"+yyyy+"/"+mm+"/"+dd+"/"+uuid+".mp3");
					}else{
						log.info("出现同名文件!!!");
					}
				} else {
					file.mkdirs();
					Util.writeBytesToFileSystem(data, speech_url+"/"+yyyy+"/"+mm+"/"+dd+"/"+uuid+".mp3");
				}
				
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
        	log.info("百度引擎合成语音成功!!");
        	Map<String, String> map = new HashMap<String, String>();
        	map.put("filename", speech_url+"/"+yyyy+"/"+mm+"/"+dd+"/"+uuid+".mp3");
            map.put("sessionid", paramMap.get("sessionid"));
            map.put("statuscode", "0");
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(map));
            log.info(jsonObject.toJSONString());
            return jsonObject.toJSONString();
		}else{
			log.info("百度引擎合成语音失败!!"+"--原因:"+result.toString());
			Map<String, String> map = new HashMap<String, String>();
        	map.put("filename", speech_url+"/"+yyyy+"/"+mm+"/"+dd+"/"+uuid+".mp3");
            map.put("sessionid", paramMap.get("sessionid"));
            map.put("statuscode", "-1");
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(map));
            return jsonObject.toJSONString();
		}
		
	}
	
	
	//科大讯飞的tts流程
	private DataModel kdxtTts(String kdxf_app_id, final String speech_url, final Map<String,String> paramMap) {
		// final JSON jsonObject = null;
		// final UUID uuid = UUID.randomUUID();
		// Calendar now = Calendar.getInstance();
		// final String yyyy = String.valueOf(now.get(Calendar.YEAR));
		// final String mm = String.valueOf((now.get(Calendar.MONTH) + 1));
		// final String dd = String.valueOf(now.get(Calendar.DAY_OF_MONTH));
		// List<String> requireKey = Arrays.asList("filedir","sessionid","txt");
		// SpeechUtility.createUtility("appid=" + kdxf_app_id);
	
		// Set<String> keys = paramMap.keySet();
		// // 必须值
		// if(!keys.containsAll(requireKey)) {
		// 	throw new RuntimeException("参数传递异常!");
		// }
		
		// 1.创建SpeechSynthesizer对象

		//可选值
		// SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer();
		// mTts = SpeechSynthesizer.createSynthesizer();
		// mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
		// mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速，范围0~100
		// mTts.setParameter(SpeechConstant.PITCH, "50");//设置语调，范围0~100
		// mTts.setParameter(SpeechConstant.VOLUME, "50");//设置音量，范围0~100

		//3.开始合成
		//设置合成音频保存位置（可自定义保存位置），默认保存在“./tts_test.pcm”
		final DataModel dataModel = new DataModel();
		SynthesizeToUriListener synthesizeToUriListener = new SynthesizeToUriListener() {
			//progress为合成进度0~100 
			public void onBufferProgress(int progress) {
				log.info("进入方法 : onBufferProgress" + "--合成进程 :"+progress);
			}
		    //会话合成完成回调接口
			//uri为合成保存地址，error为错误信息，为null时表示合成会话成功
			public void onSynthesizeCompleted(String uri, SpeechError error) {
				if (error == null) {
					/**
					 * 我想在这处理业务并return东西,可是他是重写的方法  不能返回  mmp 
					 */
					dataModel.setStatuscode(0);
				}else{
					dataModel.setStatuscode(1);
					// log.info("code=" + error.getErrorCode() + ",msg=" +error.getErrorDesc());
				}
			}
			public void onEvent(int arg0, int arg1, int arg2, int arg3, Object arg4, Object arg5) {
				log.info("进入方法 : onEvent");
			}
		};
		mTts.synthesizeToUri("我是宫由伟", speech_url+"/", synthesizeToUriListener);
		return dataModel;
	}
}

