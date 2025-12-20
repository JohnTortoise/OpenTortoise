package io.github.johntortoise.service;

import io.github.johntortoise.dto.ImportConversationMessageDTO;

import java.io.File;
import java.util.List;

public interface TransactionalService {
    void importConversationFile(File file);

    void processBatchInTransaction(List<ImportConversationMessageDTO> list);

}
