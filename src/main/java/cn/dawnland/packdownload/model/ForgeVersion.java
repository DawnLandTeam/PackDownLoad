package cn.dawnland.packdownload.model;

import cn.dawnland.packdownload.utils.MessageUtils;
import cn.dawnland.packdownload.utils.OkHttpUtils;
import lombok.Data;

import java.io.IOException;

@Data
public class ForgeVersion {

    private final static String forgeInstallBaseUrl = "http://files.minecraftforge.net/" +
            "maven/net/minecraftforge/" +
            "forge/{forgeVersion}/" +
            "{installVersion}-installer.jar";
    //https://files.minecraftforge.net/maven/net/minecraftforge
    // /forge/1.12.2-14.23.5.2847/forge-1.12.2-14.23.5.2847-installer.jar
    private final static String BMCLAPIForgeUniversalBaseUrl = "https://bmclapi2.bangbang93.com/forge/download?" +
            "mcversion={mcVersion}&version={forgeVersion}&category=universal&format=jar";
    private final static String BMCLAPIForgeInstallerBaseUrl = "https://bmclapi2.bangbang93.com/forge/download?" +
            "mcversion={mcVersion}&version={forgeVersion}&category=installer&format=jar";
    private final static String CURSE_FORGE_VERSION_BASEURL = "https://addons-ecs.forgesvc.net/api/v2/minecraft/modloader/";

    private String mcVersion;
    private String forgeVersion;
    private String forgeVersionStr;

    public ForgeVersion(String mcVersion, String forgeVersion, String forgeVersionStr) {
        this.mcVersion = mcVersion;
        this.forgeVersion = forgeVersion;
        if(forgeVersionStr == null || "".equals(forgeVersionStr)){
            this.forgeVersionStr = "forge-" + forgeVersion;
        }else {
            this.forgeVersionStr = forgeVersionStr;
        }
    }

    public String getForgeInstallUrl(){
        String temp = "1.14.4".equals(mcVersion) || "1.13.2".equals(mcVersion) ?
                BMCLAPIForgeInstallerBaseUrl.replaceFirst("\\{mcVersion}", mcVersion) :
                BMCLAPIForgeUniversalBaseUrl.replaceFirst("\\{mcVersion}", mcVersion);
        temp = temp.replaceFirst("\\{forgeVersion}", forgeVersion);
        return temp;
    }

    public String getForgeVersionJson(){
        try {
            return OkHttpUtils.get().get(CURSE_FORGE_VERSION_BASEURL + forgeVersionStr);
        } catch (IOException e) {
            MessageUtils.error("获取Forge版本异常", "网络超时");
        }
        return null;
    }
}
