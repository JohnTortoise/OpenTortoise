package io.github.johntortoise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.EmptyUtil;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.dto.SaveConversationToolsReq;
import io.github.johntortoise.dto.TortoiseConversationToolDTO;
import io.github.johntortoise.enums.DeletedEnum;
import io.github.johntortoise.mapper.TortoiseConversationToolMapper;
import io.github.johntortoise.model.TortoiseConversationTool;
import io.github.johntortoise.model.TortoiseTool;
import io.github.johntortoise.service.TortoiseConversationToolService;
import io.github.johntortoise.service.TortoiseToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TortoiseConversationToolServiceImpl extends ServiceImpl<TortoiseConversationToolMapper, TortoiseConversationTool>
        implements TortoiseConversationToolService {

    @Resource
    private TortoiseToolService tortoiseToolService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveConversationTools(SaveConversationToolsReq req) {
        try {
            LogUtil.info("保存会话工具关联: conversationId={}, toolIds={}", req.getConversationId(), req.getToolIds());

            if(EmptyUtil.isEmpty(req.getConversationId())){
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"会话id不能为空");
            }
            if(EmptyUtil.isEmpty(req.getToolIds())){
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"工具id不能为空");
            }

            List<TortoiseConversationTool> list = queryByConversationId(req.getConversationId());

            if(Objects.nonNull(list)){
                list.forEach(a->a.setIsDeleted(DeletedEnum.NO_EXIST.getCode()));
                this.updateBatchById(list);
            }

            List<TortoiseConversationTool> saveDBList = new ArrayList<>();

            req.getToolIds().forEach(id->{
                TortoiseConversationTool tortoiseConversationTool = new TortoiseConversationTool();
                tortoiseConversationTool.setConversationId(req.getConversationId());
                tortoiseConversationTool.setToolId(id);
                saveDBList.add(tortoiseConversationTool);

            });

            this.saveBatch(saveDBList);
            LogUtil.info("保存会话工具关联成功: conversationId={}", req.getConversationId());
            return true;

        } catch (Exception e) {
            LogUtil.error("保存会话工具关联失败: conversationId={}", req.getConversationId(), e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "保存会话工具关联失败", e);
        }
    }

    @Override
    public List<TortoiseConversationToolDTO> getConversationTools(String conversationId) {
        try {

            List<TortoiseConversationToolDTO> result = new ArrayList<>();

            List<TortoiseConversationTool> tortoiseConversationTools = queryByConversationId(conversationId);
            if(EmptyUtil.isNotEmpty(tortoiseConversationTools)){
                Set<String> collect = tortoiseConversationTools.stream().map(TortoiseConversationTool::getToolId).collect(Collectors.toSet());
                List<TortoiseTool> tortoiseTools = tortoiseToolService.listByIds(collect);
                tortoiseTools.forEach(tortoiseTool -> {
                    result.add(TortoiseConversationToolDTO.builder().toolId(tortoiseTool.getId()).toolName(tortoiseTool.getName()).toolDesc(tortoiseTool.getDesc()).build());
                });
            }
            return result;

        } catch (Exception e) {
            LogUtil.error("查询会话工具关联失败: conversationId={}", conversationId, e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "查询会话工具关联失败", e);
        }
    }

    public List<TortoiseConversationTool> queryByConversationId(String conversationId){
        return this.list(new QueryWrapper<TortoiseConversationTool>().lambda()
                .eq(TortoiseConversationTool::getConversationId, conversationId)
                .eq(TortoiseConversationTool::getIsDeleted,DeletedEnum.EXIST.getCode()));
    }
}
