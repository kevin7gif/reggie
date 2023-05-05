package com.xiehn;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@SpringBootTest
class ReggieApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void test1() {
        Path sourcePath = Paths.get("E:/1.txt");
        Path targetPath = Paths.get("F:/2.txt");

        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("文件复制成功！");
        } catch (IOException e) {
            System.out.println("文件复制失败：" + e.getMessage());
        }
    }
}
