package com.zjgsu.ms.hxy.catalog;

import com.zjgsu.ms.hxy.catalog.model.Course;
import com.zjgsu.ms.hxy.catalog.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class CatalogServiceApplication {

    @Autowired
    private CourseService courseService;

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }

    /**
     * 初始化课程数据（代替原 monolithic 的 initCourses()）
     */
    @Bean
    public CommandLineRunner initCatalogData() {
        return args -> {
            System.out.println("=== catalog-service 初始化课程数据 ===");

            if (courseService.getCourseCount() > 0) {
                System.out.println("课程数据已存在，跳过初始化");
                return;
            }

            Course c1 = new Course("CS101", "计算机科学导论", "INS001", "SCH001", 50);
            Course c2 = new Course("MATH201", "高等数学", "INS002", "SCH002", 60);
            Course c3 = new Course("ENG101", "大学英语", "INS003", "SCH003", 40);
            Course c4 = new Course("PHY301", "大学物理", "INS004", "SCH004", 45);

            courseService.createCourse(c1);
            courseService.createCourse(c2);
            courseService.createCourse(c3);
            courseService.createCourse(c4);

            System.out.println("=== catalog-service 初始化完成 ===");
        };
    }
}
