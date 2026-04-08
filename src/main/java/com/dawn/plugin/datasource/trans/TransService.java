package com.dawn.plugin.datasource.trans;

import com.dawn.plugin.datasource.datasource.TargetDataSource;
import com.dawn.plugin.entity.ctemp.Temp;
import com.dawn.plugin.mapper.ctemp.TempMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * [事务处理]
 *
 * @author hforest-480s
 */
@Service
@ConditionalOnProperty(name = {"plugin-status.datasource-status"}, havingValue = "enable", matchIfMissing = true)
public class TransService {

    private final TempMapper tempMapper;

    public TransService(TempMapper tempMapper) {
        this.tempMapper = tempMapper;
    }

    @TargetDataSource(name = "master")
    @Transactional(rollbackFor = {RuntimeException.class})
    public List<Temp> findAll() {
        return tempMapper.findAll();
    }

    @TargetDataSource(name = "master")
    @Transactional(rollbackFor = {RuntimeException.class})
    public Temp edit(Temp temp) {
        tempMapper.create(temp);
        temp.setC2("Transactional");
        tempMapper.edit(temp);
        return temp;
    }

}
