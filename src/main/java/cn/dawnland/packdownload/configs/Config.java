package cn.dawnland.packdownload.configs;

import cn.dawnland.packdownload.utils.HttpUtils;
import cn.dawnland.packdownload.utils.MessageUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author Cap_Sub
 * @version 创建时间：19/02/29 11:29
 */
@Data
public class Config {

    public static float currentVersion = 2.18f;
    public static String batUrl;
    public static String versionUrl;
    public static String exeUrl;
    public static String string2download;
    public static String lancherUrl;
    public static String authlibInjectorsUrl;

    public static void init(){
        try{
            String configJson = HttpUtils.get("https://dawnland.cn/config.json");
            JSONObject jsonObject = JSONObject.parseObject(configJson);
            batUrl = jsonObject.get("batUrl").toString();
            versionUrl = jsonObject.get("versionUrl").toString();
            exeUrl = jsonObject.get("exeUrl").toString();
            string2download = jsonObject.get("string2download").toString();
            lancherUrl = jsonObject.get("lancherUrl").toString();
            authlibInjectorsUrl = jsonObject.get("authlib-injectors-url").toString();
        }catch (Exception e){
            MessageUtils.error(e);
            MessageUtils.error("未能初始化配置", "请带上目录下的error.txt进行反馈");
        }
    }

}
