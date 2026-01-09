package io.github.johntortoise.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.johntortoise.core.dto.model.Tool;
import io.github.johntortoise.core.dto.sys.BatchGetToolByNamesReq;
import io.github.johntortoise.core.dto.sys.BatchGetToolByNamesResp;
import io.github.johntortoise.dto.*;
import io.github.johntortoise.model.TortoiseTool;

public interface TortoiseToolService extends IService<TortoiseTool> {

    boolean createTool(CreateToolDTO createToolDTO);

    boolean updateTool(TortoiseToolDetailDTO updateToolDTO);

    ParseCUrlDTO parseCurl(ParseCUrlReq parseCUrlReq);

    BatchGetToolByNamesResp batchGetToolByNames(BatchGetToolByNamesReq batchGetToolByNamesReq);


    String callTool(String name,String args);

    Page<TortoiseToolDTO> page(String toolName, Long current,
                                       Long size);

    TortoiseToolDetailDTO detail(Long id);

}
