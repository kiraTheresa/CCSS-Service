package com.zjgsu.ms.hxy.enrollment;

import com.zjgsu.ms.hxy.enrollment.model.Student;
import com.zjgsu.ms.hxy.enrollment.service.StudentService;
import com.zjgsu.ms.hxy.enrollment.service.EnrollmentService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@EnableDiscoveryClient
public class EnrollmentServiceApplication {

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
    public CommandLineRunner initEnrollmentData(RestTemplate restTemplate, 
                                               StudentService studentService, 
                                               EnrollmentService enrollmentService, 
                                               DiscoveryClient discoveryClient) {
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
            enrollSafely("CS101", s1.getId().toString(), restTemplate, enrollmentService, discoveryClient);
            enrollSafely("ENG101", s1.getId().toString(), restTemplate, enrollmentService, discoveryClient);
            enrollSafely("MATH201", s2.getId().toString(), restTemplate, enrollmentService, discoveryClient);
            enrollSafely("CS101", s3.getId().toString(), restTemplate, enrollmentService, discoveryClient);

            System.out.println("=== enrollment-service 初始化完成 ===");
        };
    }

    /**
     * 远程调用 catalog-service 验证课程是否存在，然后才允许选课
     */
    private void enrollSafely(String courseCode, String studentId, 
                             RestTemplate restTemplate, 
                             EnrollmentService enrollmentService, 
                             DiscoveryClient discoveryClient) {
        try {
            // 使用DiscoveryClient获取catalog-service实例
            List<ServiceInstance> instances = discoveryClient.getInstances("catalog-service");
            if (instances == null || instances.isEmpty()) {
                System.err.println("No instances available for catalog-service");
                return;
            }
            String baseUrl = instances.get(0).getUri().toString();
            
            // 首先通过课程代码获取课程ID
            String courseUrl = baseUrl + "/api/courses/code/" + courseCode;
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
    
    /**
     * 应用启动完成事件监听器，打印Nacos注册信息
     */
    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onApplicationReady(org.springframework.boot.context.event.ApplicationReadyEvent event) {
        try {
            // 获取应用上下文
            org.springframework.context.ApplicationContext context = event.getApplicationContext();
            // 获取DiscoveryClient
            org.springframework.cloud.client.discovery.DiscoveryClient discoveryClient = 
                context.getBean(org.springframework.cloud.client.discovery.DiscoveryClient.class);
            
            // 获取服务名
            String serviceName = context.getEnvironment().getProperty("spring.application.name");
            
            // 打印Nacos注册信息
            System.out.println("\n=== Nacos 服务注册信息 ===");
            System.out.println("服务名: " + serviceName);
            System.out.println("已注册到 Nacos: true");
            System.out.println("Nacos 服务器地址: " + context.getEnvironment().getProperty("spring.cloud.nacos.discovery.server-addr"));
            System.out.println("命名空间: " + context.getEnvironment().getProperty("spring.cloud.nacos.discovery.namespace"));
            System.out.println("服务分组: " + context.getEnvironment().getProperty("spring.cloud.nacos.discovery.group"));
            
            // 获取当前服务实例信息
            java.util.List<org.springframework.cloud.client.ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            if (instances != null && !instances.isEmpty()) {
                org.springframework.cloud.client.ServiceInstance instance = instances.get(0);
                System.out.println("当前实例地址: " + instance.getUri());
                System.out.println("当前实例IP: " + instance.getHost());
                System.out.println("当前实例端口: " + instance.getPort());
            }
            System.out.println("=========================\n");
        } catch (Exception e) {
            System.err.println("获取Nacos注册信息失败: " + e.getMessage());
        }
    }
}
