package io.github.johntortoise.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageConvertUtil {
    

    public static <S, T> Page<T> convert(Page<S> sourcePage, Function<S, T> converter) {
        if (sourcePage == null || sourcePage.getRecords() == null) {
            return new Page<>();
        }
        

        List<T> targetList = sourcePage.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList());
        

        Page<T> targetPage = new Page<>(
                sourcePage.getCurrent(),
                sourcePage.getSize(),
                sourcePage.getTotal()
        );
        targetPage.setRecords(targetList);
        targetPage.setPages(sourcePage.getPages());
        
        return targetPage;
    }
}