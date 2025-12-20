package io.github.johntortoise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.johntortoise.core.utils.EmptyUtil;
import io.github.johntortoise.mapper.TortoiseImportFileRecordMapper;
import io.github.johntortoise.model.TortoiseImportFileRecord;
import io.github.johntortoise.service.TortoiseImportFileRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
@Service
@Slf4j
public class TortoiseImportFileRecordServiceImpl extends ServiceImpl<TortoiseImportFileRecordMapper, TortoiseImportFileRecord> implements TortoiseImportFileRecordService {
    @Override
    public Long saveAndReturnId(Long createUserId,String fileName) {
        TortoiseImportFileRecord tortoiseImportFileRecord = new TortoiseImportFileRecord();
        tortoiseImportFileRecord.setStatus(0);
        tortoiseImportFileRecord.setCreateUserId(createUserId);
        tortoiseImportFileRecord.setImportName(fileName);
        this.save(tortoiseImportFileRecord);
        return tortoiseImportFileRecord.getId();
    }

    @Override
    public void success(Long id) {
        updateStatus(1,id,"");
    }

    @Override
    public void fail(Long id,String info) {
        updateStatus(2,id,info);
    }

    @Override
    public Page<TortoiseImportFileRecord> page(Long createId, String fileName, Long current, Long size, Integer status) {
        if (current == null || current < 1) {
            current = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        LambdaQueryWrapper<TortoiseImportFileRecord> queryWrapper = new LambdaQueryWrapper<>();

        if(Objects.nonNull(createId)){
            queryWrapper.eq(TortoiseImportFileRecord::getCreateUserId,createId);
        }

        if(EmptyUtil.isNotEmpty(fileName)){
            queryWrapper.like(TortoiseImportFileRecord::getImportName,fileName);
        }
        if(EmptyUtil.isNotEmpty(status)){
            queryWrapper.eq(TortoiseImportFileRecord::getStatus,status);
        }

        queryWrapper.orderByDesc(TortoiseImportFileRecord::getCreateTime);

        return this.baseMapper.selectPage(new Page<>(current, size),queryWrapper);
    }

    public void updateStatus(Integer status,Long id,String errorInfo){
        TortoiseImportFileRecord tortoiseImportFileRecord = this.getById(id);
        if(Objects.nonNull(tortoiseImportFileRecord)){
            tortoiseImportFileRecord.setStatus(status);
            tortoiseImportFileRecord.setErrorInfo(errorInfo);
            this.updateById(tortoiseImportFileRecord);
        }
    }
}
