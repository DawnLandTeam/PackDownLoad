package cn.dawnland.packdownload.model.manifest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Created by cap_sub@dawnland.cn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManifestFile {

    private Long projectID;
    private Long fileID;
    private boolean required;
    private String disName;
    private String originUrl;
    private String downloadUrl;
    private boolean downloadSucceed;
    private int downloadRetryCount;

}
