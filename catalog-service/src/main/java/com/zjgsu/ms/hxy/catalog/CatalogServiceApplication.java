package com.zjgsu.ms.hxy.catalog;

import com.zjgsu.ms.hxy.catalog.model.Course;
import com.zjgsu.ms.hxy.catalog.service.CourseService;
import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    public void initCourseData() {
        System.out.println("=== 开始初始化课程数据 ===");

        try {
            // 检查是否已经初始化过（避免重复初始化）
            if (isCourseDataAlreadyInitialized()) {
                System.out.println("=== 课程数据已存在，跳过初始化 ===");
                printCourseStatistics();
                return;
            }

            // 创建测试课程
            initCourses();

            System.out.println("=== 课程数据初始化完成 ===");
            printCourseStatistics();

        } catch (Exception e) {
            System.err.println("初始化课程数据时发生错误: " + e.getMessage());
            // 不抛出异常，避免应用启动失败
            e.printStackTrace();
        }
    }

    /**
     * 检查课程数据是否已经初始化过
     */
    private boolean isCourseDataAlreadyInitialized() {
        try {
            return courseService.getCourseCount() > 0;
        } catch (Exception e) {
            // 如果出现异常，认为没有初始化过
            return false;
        }
    }

    private void initCourses() {
        System.out.println("创建测试课程...");

        try {
            Course course1 = new Course("CS101", "计算机科学导论", "INS001", "SCH001", 50);
            courseService.createCourse(course1);
            System.out.println("创建课程: " + course1.getCode() + " - " + course1.getTitle());

            Course course2 = new Course("MATH201", "高等数学", "INS002", "SCH002", 60);
            courseService.createCourse(course2);
            System.out.println("创建课程: " + course2.getCode() + " - " + course2.getTitle());

            Course course3 = new Course("ENG101", "大学英语", "INS003", "SCH003", 40);
            courseService.createCourse(course3);
            System.out.println("创建课程: " + course3.getCode() + " - " + course3.getTitle());

            Course course4 = new Course("PHY301", "大学物理", "INS004", "SCH004", 45);
            courseService.createCourse(course4);
            System.out.println("创建课程: " + course4.getCode() + " - " + course4.getTitle());

        } catch (Exception e) {
            System.err.println("创建课程失败: " + e.getMessage());
            // 继续执行，不中断初始化流程
        }
    }

    /**
     * 安全获取课程（避免Optional操作异常）
     */
    private Course getCourseSafely(String courseCode) {
        try {
            return courseService.getCourseByCode(courseCode).orElse(null);
        } catch (Exception e) {
            System.err.println("获取课程失败: " + courseCode + " - " + e.getMessage());
            return null;
        }
    }

    private void printCourseStatistics() {
        try {
            System.out.println("\n=== 课程目录统计信息 ===");
            System.out.println("课程总数: " + courseService.getCourseCount());

            // 各课程基本信息
            courseService.getAllCourses().forEach(course -> {
                try {
                    System.out.println("课程: " + course.getCode() + " - " + course.getTitle() +
                            " (容量: " + course.getCapacity() + ", 教师: " + course.getInstructorId() + ")");
                } catch (Exception e) {
                    System.err.println("获取课程信息失败: " + course.getCode() + " - " + e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("打印课程统计信息失败: " + e.getMessage());
        }
    }


}
