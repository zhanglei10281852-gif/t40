package com.prison.config;

import com.prison.entity.EducationCourse;
import com.prison.repository.EducationCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EducationCourseRepository educationCourseRepository;

    @Override
    public void run(String... args) {
        initEducationCourses();
    }

    private void initEducationCourses() {
        if (educationCourseRepository.count() > 0) {
            return;
        }

        EducationCourse course1 = new EducationCourse();
        course1.setCourseCode("LAW_COMMON");
        course1.setCourseName("法律常识教育");
        course1.setCourseHours(4);
        course1.setDescription("讲解基本法律常识，帮助服刑人员了解法律，树立法治观念");
        course1.setRequired(true);
        course1.setSortOrder(1);
        educationCourseRepository.save(course1);

        EducationCourse course2 = new EducationCourse();
        course2.setCourseCode("SOCIAL_ADAPT");
        course2.setCourseName("社会适应指导");
        course2.setCourseHours(4);
        course2.setDescription("指导服刑人员适应社会生活，掌握基本生活技能");
        course2.setRequired(true);
        course2.setSortOrder(2);
        educationCourseRepository.save(course2);

        EducationCourse course3 = new EducationCourse();
        course3.setCourseCode("EMPLOYMENT_SKILL");
        course3.setCourseName("就业技能培训");
        course3.setCourseHours(8);
        course3.setDescription("培训基本就业技能，提高服刑人员出狱后就业能力");
        course3.setRequired(true);
        course3.setSortOrder(3);
        educationCourseRepository.save(course3);

        EducationCourse course4 = new EducationCourse();
        course4.setCourseCode("PSYCHO_ADJUST");
        course4.setCourseName("心理调适辅导");
        course4.setCourseHours(4);
        course4.setDescription("心理调适辅导，帮助服刑人员建立健康心理状态");
        course4.setRequired(true);
        course4.setSortOrder(4);
        educationCourseRepository.save(course4);
    }
}
