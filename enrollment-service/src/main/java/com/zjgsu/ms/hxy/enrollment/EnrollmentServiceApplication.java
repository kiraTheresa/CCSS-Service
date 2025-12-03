package com.zjgsu.ms.hxy.enrollment;

import com.zjgsu.ms.hxy.enrollment.model.Student;
import com.zjgsu.ms.hxy.enrollment.service.StudentService;
import com.zjgsu.ms.hxy.enrollment.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
public class EnrollmentServiceApplication {

    @Autowired
    private StudentService studentService;

    @Autowired
    private EnrollmentService enrollmentService;

    public static void main(String[] args) {
        SpringApplication.run(EnrollmentServiceApplication.class, args);
    }

    /**
     * 给 enrollment-service 提供访问 catalog-service 的能力
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 初始化学生 + 选课数据
     */
    @Bean
    public CommandLineRunner initEnrollmentData(RestTemplate restTemplate) {
        return args -> {
            System.out.println("=== enrollment-service 初始化数据 ===");

            if (studentService.getStudentCount() > 0) {
                System.out.println("学生数据已存在，跳过初始化");
                return;
            }

            // ----- 创建学生 -----
            Student s1 = new Student("2024001", "张三", "计算机科学", 2024, "zhangsan@example.com");
            Student s2 = new Student("2024002", "李四", "软件工程", 2024, "lisi@example.com");
            Student s3 = new Student("2024003", "王五", "数据科学", 2024, "wangwu@example.com");

            studentService.createStudent(s1);
            studentService.createStudent(s2);
            studentService.createStudent(s3);

            // ----- 创建选课（远程校验课程是否存在）-----
            enrollSafely("CS101", s1.getId().toString(), restTemplate);
            enrollSafely("ENG101", s1.getId().toString(), restTemplate);
            enrollSafely("MATH201", s2.getId().toString(), restTemplate);
            enrollSafely("CS101", s3.getId().toString(), restTemplate);

            System.out.println("=== enrollment-service 初始化完成 ===");
        };
    }

    /**
     * 远程调用 catalog-service 验证课程是否存在，然后才允许选课
     */
    private void enrollSafely(String courseCode, String studentId, RestTemplate restTemplate) {
        try {
            // 首先通过课程代码获取课程ID
            String courseUrl = "http://localhost:8081/api/courses/code/" + courseCode;
            var courseResponse = restTemplate.getForObject(courseUrl, java.util.Map.class);
            
            if (courseResponse != null) {
                var courseData = (java.util.Map<String, Object>) courseResponse.get("data");
                String courseId = courseData.get("id").toString();
                
                // 使用课程ID进行选课
                enrollmentService.enrollCourse(courseId, studentId);
                System.out.println("学生 " + studentId + " 成功选课: " + courseCode);
            }
        } catch (Exception e) {
            System.err.println("远程校验课程失败: " + courseCode + " - " + e.getMessage());
        }
    }
}
