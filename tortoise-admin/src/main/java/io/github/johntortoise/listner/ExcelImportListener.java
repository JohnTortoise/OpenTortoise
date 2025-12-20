package io.github.johntortoise.listner;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.context.SpringContextHolder;
import io.github.johntortoise.dto.ImportConversationMessageDTO;
import io.github.johntortoise.service.TransactionalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ExcelImportListener extends AnalysisEventListener<ImportConversationMessageDTO> {

    private final TransactionalService transactionalService = SpringContextHolder.getBean(TransactionalService.class);

    private List<ImportConversationMessageDTO> batchBuffer = new ArrayList<>();

    private static final int BATCH_SIZE = 1000;

    private long startRow = 1;

    @Override
    public void invoke(ImportConversationMessageDTO data, AnalysisContext context) {
        try {
            batchBuffer.add(data);
            if (batchBuffer.size() >= BATCH_SIZE) {
                transactionalService.processBatchInTransaction(batchBuffer);
                startRow = startRow+BATCH_SIZE;
                batchBuffer = new ArrayList<>();
            }
        } catch (Exception e) {
            String errorMsg = "处理第:"+startRow+"-"+(startRow+BATCH_SIZE)+"行数据失败，错误原因"+e.getMessage();
            throw new RuntimeException(errorMsg);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        try {
            if (!batchBuffer.isEmpty()) {
                transactionalService.processBatchInTransaction(batchBuffer);
            }
            LogUtil.info("doAfterAllAnalysed finish");
        }catch (Exception e){
            String errorMsg = "处理第:"+startRow+"-"+(startRow+BATCH_SIZE)+"行数据失败，错误原因"+e.getMessage();
            throw new RuntimeException(errorMsg);
        }
    }
}