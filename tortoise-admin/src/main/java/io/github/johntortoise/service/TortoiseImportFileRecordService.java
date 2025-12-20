package io.github.johntortoise.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.johntortoise.model.TortoiseImportFileRecord;


public interface TortoiseImportFileRecordService extends IService<TortoiseImportFileRecord> {
    Long saveAndReturnId(Long createUserId,String fileName);

    void success(Long id);

    void fail(Long id,String info);

    Page<TortoiseImportFileRecord> page(Long createId, String fileName, Long current, Long size, Integer status);

}