package cn.dawnland.packdownload.model;

public class ForgeVersion {

    private final static String forgeInstallBaseUrl = "http://files.minecraftforge.net/" +
            "maven/net/minecraftforge/" +
            "forge/{forgeVersion}/" +
            "{installVersion}-installer.jar";
    private final static String BMCLAPIForgeInstallBaseUrl = "https://bmclapi2.bangbang93.com/forge/download?" +
            "mcversion={mcVersion}&version={forgeVersion}&category=installer&format=jar";

    private String mcVersion;
    private String forgeVersion;

    public ForgeVersion(String mcVersion, String forgeVersion) {
        this.mcVersion = mcVersion;
        this.forgeVersion = forgeVersion;
    }

    public String getForgeInstallUrl(){
        String temp = BMCLAPIForgeInstallBaseUrl.replaceFirst("\\{mcVersion}", mcVersion);
        temp = temp.replaceFirst("\\{forgeVersion}", forgeVersion);
        return temp;
    }

}
