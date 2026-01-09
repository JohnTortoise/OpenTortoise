package io.github.johntortoise.controller;

import io.github.johntortoise.core.dto.sys.TortoiseBaseResult;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.dto.BaseCalcDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/defaultTool")
@Slf4j
public class DefaultToolController {

    @GetMapping("queryNowTime")
    public TortoiseBaseResult<String> queryNowTime(){
        return TortoiseBaseResult.ok(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @GetMapping("queryWeather")
    private TortoiseBaseResult<String> queryWeather(@RequestParam String date){
        LogUtil.info("收到:{}",date);
        List<String> list = Arrays.asList("晴天", "阴天", "多云");
        Random random = new Random();
        int index = random.nextInt(list.size());
        return TortoiseBaseResult.ok(list.get(index));
    }

    @PostMapping("calc")
    public TortoiseBaseResult<String> calc(@RequestBody BaseCalcDTO baseCalcDTO) {
        String sign = baseCalcDTO.getSign();
        Long firstNumber = baseCalcDTO.getFirstNumber();
        Long secondNumber = baseCalcDTO.getSecondNumber();
        try {
            BigDecimal result = calculate(sign, firstNumber, secondNumber);
            return TortoiseBaseResult.ok(result.toString());
        }catch (Exception e){
            throw new RuntimeException("");
        }
    }

    private BigDecimal calculate(String sign, Long firstNumber, Long secondNumber) {
        BigDecimal num1 = BigDecimal.valueOf(firstNumber);
        BigDecimal num2 = BigDecimal.valueOf(secondNumber);

        switch (sign) {
            case "+":
                return num1.add(num2);
            case "-":
                return num1.subtract(num2);
            case "*":
                return num1.multiply(num2);
            case "/":
                if (secondNumber == 0) {
                    throw new ArithmeticException("除数不能为零");
                }
                return num1.divide(num2, 10, RoundingMode.HALF_UP);
            default:
                throw new IllegalArgumentException("不支持的运算符: " + sign + "。支持的运算符: +, -, *, /");
        }
    }

}
