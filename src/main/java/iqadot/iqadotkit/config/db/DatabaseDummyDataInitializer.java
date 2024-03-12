package iqadot.iqadotkit.config.db;//package nl.fontys.s3.individualprojectbackend.configuration.db;
//
//import fontys.sem3.school.business.*;
//import fontys.sem3.school.domain.*;
//import fontys.sem3.school.repository.*;
//import fontys.sem3.school.repository.entity.*;
//import jakarta.transaction.*;
//import lombok.*;
//import org.springframework.boot.context.event.*;
//import org.springframework.context.event.EventListener;
//import org.springframework.security.crypto.password.*;
//import org.springframework.stereotype.*;
//
//import java.util.*;
//
//@Component
//@AllArgsConstructor
//public class DatabaseDummyDataInitializer {
//
//    private CountryRepository countryRepository;
//    private UserRepository userRepository;
//    private PasswordEncoder passwordEncoder;
//    private CreateStudentUseCase createStudentUseCase;
//
//    @EventListener(ApplicationReadyEvent.class)
//    @Transactional
//    public void populateDatabaseInitialDummyData() {
//        if (isDatabaseEmpty()) {
//            insertSomeCountries();
//            insertAdminUser();
//            insertStudent();
//        }
//    }
//
//    private boolean isDatabaseEmpty() {
//        return countryRepository.count() == 0;
//    }
//
//    private void insertAdminUser() {
//        UserEntity adminUser = UserEntity.builder()
//                .username("admin@fontys.nl")
//                .password(passwordEncoder.encode("test123"))
//                .build();
//        UserRoleEntity adminRole = UserRoleEntity.builder().role(RoleEnum.ADMIN).user(adminUser).build();
//        adminUser.setUserRoles(Set.of(adminRole));
//        userRepository.save(adminUser);
//    }
//
//    private void insertStudent() {
//        CreateStudentRequest createStudentRequest = CreateStudentRequest.builder()
//                .pcn(1234L)
//                .password("test123")
//                .name("Linda")
//                .countryId(1L)
//                .build();
//        createStudentUseCase.createStudent(createStudentRequest);
//    }
//
//    private void insertSomeCountries() {
//        countryRepository.save(CountryEntity.builder().code("NL").name("Netherlands").build());
//        countryRepository.save(CountryEntity.builder().code("BG").name("Bulgaria").build());
//        countryRepository.save(CountryEntity.builder().code("RO").name("Romania").build());
//        countryRepository.save(CountryEntity.builder().code("BR").name("Brazil").build());
//        countryRepository.save(CountryEntity.builder().code("CN").name("China").build());
//    }
//}
