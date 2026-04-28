package com.dawn.plugin.task.service;

import com.dawn.plugin.config.LoadParams;
import com.dawn.plugin.enmu.CodeEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.entity.ccore.TabTask;
import com.dawn.plugin.util.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * [样例模板]
 *
 * @author forest
 * @date 2021/2/3 0:01
 */
@Data
@Slf4j
@Scope("prototype")
@EqualsAndHashCode(callSuper = false)
@Service(value = "sampleTaskServiceImpl")
@ConditionalOnProperty(name = {"plugin-status.task-status"}, havingValue = "enable", matchIfMissing = true)
public class SampleTaskServiceImpl extends AbstractHandleService<Object> implements HandleService {

    private final LoadParams loadParams;

    public SampleTaskServiceImpl(LoadParams loadParams) {
        super.transCode = "sampleTaskServiceImpl";
        this.loadParams = loadParams;
    }

    /**
     * [程序处理]
     *
     * @return Response<Object>
     **/
    @Override
    public Response<Object> handle() {
        statMsg = "handle";
        tabTask = Objects.isNull(tabTask) ? new TabTask() : tabTask;
        var params = loadParams.loadKeys("ftp");
        var val = loadParams.loadKey("post-comp-code", "nam");
        var val2 = loadParams.loadKey("post-comp-code", "nam1");
        log.info(LogEnmu.LOG6.value(), "测试任务.handle", redisKeyService.roundNo(),
                tabTask.getTaskId(), params.size(), val, val2);
        return new Response<>().success().code(CodeEnmu.HANDLE_OK.icode());
    }

}
