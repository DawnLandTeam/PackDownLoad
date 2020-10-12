package cn.dawnland.packdownload.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Created by cap_sub@dawnland.cn
 * task信息类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskProfile {

    private String name;
    private String displayTitle;
    private String startMessage;
    private String endMessage;

}
